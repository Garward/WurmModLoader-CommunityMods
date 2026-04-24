package org.gotti.wurmunlimited.mods.bagofholding;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Modern interfaces
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.ServerStartedListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

// Legacy hook utilities (via compatibility wrappers)
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.Spells;

/**
 * Bag of Holding mod - magically enlarges containers.
 *
 * <p><strong>Modernization note:</strong> This mod has been updated to use the framework's
 * {@code ItemContainerPatch} instead of directly manipulating bytecode. The framework now
 * handles the redirection of template container methods to instance methods automatically.
 *
 * <p>This mod now only registers reflection hooks via {@code HookManager.registerHook()},
 * which safely intercept container method calls without freezing classes.
 *
 * <p><strong>Important:</strong> Hooks are registered in {@code preInit()} to ensure they're
 * installed before the framework's ItemContainerPatch freezes the Item class.
 */
public class BagOfHoldingMod implements WurmServerMod, PreInitable, Configurable, ServerStartedListener {
	
	private int spellCost = 30;
	private int spellDifficulty = 20;
	private long spellCooldown = 300000L;
	private int effectModifier = 0;
	private boolean allowComponentItems;
	
	private static final Logger logger = Logger.getLogger(BagOfHoldingMod.class.getName());
	
	@Override
	public void onServerStarted() {
		new Runnable() {

			@Override
			public void run() {
				logger.log(Level.INFO, "Registering BagOfHolding spell");

				BagOfHolding bagOfHolding = new BagOfHolding(spellCost, spellDifficulty, spellCooldown);

				try {
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), bagOfHolding);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e ) {
					throw new RuntimeException(e);
				}

				for (Deity deity : Deities.getDeities()) {
					deity.addSpell(bagOfHolding);
				}
			}
		}.run();
	}
	
	@Override
	public void configure(Properties properties) {
		spellCost = Integer.parseInt(properties.getProperty("spellCost", Integer.toString(spellCost)));
		spellDifficulty = Integer.parseInt(properties.getProperty("spellDifficulty", Integer.toString(spellDifficulty)));
		spellCooldown = Long.parseLong(properties.getProperty("spellCooldown", Long.toString(spellCooldown)));
		effectModifier = Integer.parseInt(properties.getProperty("effectModifier", Integer.toString(effectModifier)));
		allowComponentItems = Boolean.parseBoolean(properties.getProperty("allowComponentItems", "false"));
		
		logger.log(Level.INFO, "spellCost: " + spellCost);
		logger.log(Level.INFO, "spellDifficulty: " + spellDifficulty);
		logger.log(Level.INFO, "spellCooldown: " + spellCooldown);
		logger.log(Level.INFO, "effectModifier: " + effectModifier);
		logger.log(Level.INFO, "allowComponentItems: " + allowComponentItems);
	}
	
	// NOTE: Bytecode patches moved to framework's ItemContainerPatch
	// This preInit() only registers reflection hooks (HookManager.registerHook)
	// Must run BEFORE SystemBootstrap applies ItemContainerPatch to avoid class freezing

	@Override
	public void preInit() {
		HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerVolume", "()I", new InvocationHandlerFactory() {
			
			@Override
			public InvocationHandler createInvocationHandler() {
				return new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object volume = method.invoke(proxy, args);
						
						if (volume instanceof Number && proxy instanceof Item && BagOfHolding.isValidTarget((Item) proxy)) {
							Item target = (Item)proxy;
							
							float modifier = BagOfHolding.getSpellEffect(target);

							if (allowComponentItems && target.isComponentItem()) {
								Item parent = target.getParentOrNull();
								if (parent != null && BagOfHolding.isValidTarget(parent))
									modifier = BagOfHolding.getSpellEffect(parent);
							}
							
							if (effectModifier == 0) {
								if (modifier > 1) {
									double newVolume = Math.min(Integer.MAX_VALUE, modifier * ((Number) volume).doubleValue());
									return (int) newVolume;
								}
							} else if (modifier > 0) {
								double scale = 1 + modifier * modifier * effectModifier * 0.0001;
								double newVolume = Math.min(Integer.MAX_VALUE, scale * ((Number) volume).doubleValue());
								return (int) newVolume;
							}
						}
						
						return volume;
					}
				};
			}
		});
		
		InvocationHandlerFactory invocationHandlerFactory = new InvocationHandlerFactory() {
			
			@Override
			public InvocationHandler createInvocationHandler() {
				return new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object dimension = method.invoke(proxy, args);
						
						if (dimension instanceof Number && proxy instanceof Item && BagOfHolding.isValidTarget((Item) proxy)) {
							Item target = (Item)proxy;
							
							float modifier = BagOfHolding.getSpellEffect(target);
							
							if (effectModifier == 0) {
								if (modifier > 1) {
									double newDimension = Math.min(1200, Math.cbrt(modifier) * ((Number) dimension).doubleValue());
									return (int) newDimension;
								}
							} else if (modifier > 0) {
								double scale = 1 + modifier * modifier * effectModifier * 0.0001;
								double newDimension = Math.min(1200, Math.cbrt(scale) * ((Number) dimension).doubleValue());
								return (int) newDimension;
							}
						}
						
						return dimension;
					}
				};
			}
		};
		
		HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerSizeX", "()I", invocationHandlerFactory);
		HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerSizeY", "()I", invocationHandlerFactory);
		HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerSizeZ", "()I", invocationHandlerFactory);
	}
}
