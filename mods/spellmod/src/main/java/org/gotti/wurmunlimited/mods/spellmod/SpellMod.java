package org.gotti.wurmunlimited.mods.spellmod;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.player.PrayerFaithEvent;
import com.garward.wurmmodloader.api.events.player.PriestRestrictionCheckEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.ServerStartedListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.behaviours.ActionEntry;  // Type import (acceptable per architecture rules)
import com.wurmonline.server.behaviours.Actions;  // Type import (acceptable per architecture rules)
import com.wurmonline.server.deities.Deities;  // Type import (acceptable per architecture rules)
import com.wurmonline.server.deities.Deity;  // Type import (acceptable per architecture rules)
import com.wurmonline.server.spells.Spell;  // Type import (acceptable per architecture rules)

/**
 * Framework wrapper to allow accessing private fields safely.
 * TODO: Move this to modsupport utilities if needed by other mods.
 */
class ReflectionUtil {
	static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field;
	}

	static void setPrivateField(Object obj, Field field, Object value) throws IllegalAccessException {
		field.set(obj, value);
	}
}

public class SpellMod implements WurmServerMod, Configurable, ServerStartedListener, Initable, PreInitable {

	private Integer favorLimit = Integer.MAX_VALUE;
	private boolean removePriestRestrictions = true;
	private boolean allowAllSpells = true;
	private boolean allowLightSpells = true;
	private boolean unlimitedPrayers = false;
	private boolean noPrayerDelay = false;
	private static final Logger logger = Logger.getLogger(SpellMod.class.getName());

	@Override
	public void onServerStarted() {
		logger.log(Level.INFO, "Initializing Spell modifications");

		Set<Spell> allGodSpells = new TreeSet<>();
		Set<Spell> whiteLightSpells = new TreeSet<>();
		Set<Spell> blackLightSpells = new TreeSet<>();

		for (Deity deity : Deities.getDeities()) {
			allGodSpells.addAll(deity.getSpells());
			if (deity.isHateGod()) {
				blackLightSpells.addAll(deity.getSpells());
			} else {
				whiteLightSpells.addAll(deity.getSpells());
			}
		}

		try {
			Field buildWallBonus = ReflectionUtil.getField(Deity.class, "buildWallBonus");
			Field roadProtector = ReflectionUtil.getField(Deity.class, "roadProtector");
			Field cost = ReflectionUtil.getField(Spell.class, "cost");

			Field isAllowVynora = ReflectionUtil.getField(ActionEntry.class, "isAllowVynora");
			Field isAllowFo = ReflectionUtil.getField(ActionEntry.class, "isAllowFo");
			Field isAllowMagranon = ReflectionUtil.getField(ActionEntry.class, "isAllowMagranon");
			Field isAllowLibila = ReflectionUtil.getField(ActionEntry.class, "isAllowLibila");

			// Make all spells available to all gods
			for (Deity deity : Deities.getDeities()) {
				if (allowAllSpells || allowLightSpells) {
					final Set<Spell> spells;
					if (allowAllSpells) {
						spells = allGodSpells;
					} else if (deity.isHateGod()) {
						spells = blackLightSpells;
					} else {
						spells = whiteLightSpells;
					}

					for (Spell spell : spells) {
						if (!deity.getSpells().contains(spell)) {
							deity.addSpell(spell);
						}
					}
				}

				if (removePriestRestrictions) {
					try {
						ReflectionUtil.setPrivateField(deity, buildWallBonus, 0.0f);
						ReflectionUtil.setPrivateField(deity, roadProtector, Boolean.FALSE);
					} catch (IllegalAccessException | IllegalArgumentException | ClassCastException e) {
						logger.log(Level.WARNING, e.getMessage(), e);
					}
				}
			}

			if (favorLimit < Integer.MAX_VALUE) {
				for (Spell spell : allGodSpells) {
					if (spell.getCost() > favorLimit) {
						try {
							ReflectionUtil.setPrivateField(spell, cost, favorLimit);
						} catch (IllegalAccessException | IllegalArgumentException | ClassCastException e) {
							logger.log(Level.WARNING, e.getMessage(), e);
						}

					}
				}
			}

			if (removePriestRestrictions) {
				for (ActionEntry action : Actions.actionEntrys) {
					try {
						ReflectionUtil.setPrivateField(action, isAllowVynora, Boolean.TRUE);
						ReflectionUtil.setPrivateField(action, isAllowFo, Boolean.TRUE);
						ReflectionUtil.setPrivateField(action, isAllowMagranon, Boolean.TRUE);
						ReflectionUtil.setPrivateField(action, isAllowLibila, Boolean.TRUE);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						logger.log(Level.WARNING, e.getMessage(), e);
					}
				}
			}

		} catch (NoSuchFieldException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void configure(Properties properties) {
		removePriestRestrictions = Boolean.parseBoolean(properties.getProperty("removePriestRestrictions", Boolean.toString(removePriestRestrictions)));
		favorLimit = Integer.parseInt(properties.getProperty("favorLimit", Integer.toString(favorLimit)));
		allowAllSpells = Boolean.parseBoolean(properties.getProperty("allowAllSpells", Boolean.toString(allowAllSpells)));
		allowLightSpells = Boolean.parseBoolean(properties.getProperty("allowLightSpells", Boolean.toString(allowLightSpells)));
		unlimitedPrayers = Boolean.parseBoolean(properties.getProperty("unlimitedPrayers", Boolean.toString(unlimitedPrayers)));
		noPrayerDelay = Boolean.parseBoolean(properties.getProperty("noPrayerDelay", Boolean.toString(noPrayerDelay)));

		logger.log(Level.INFO, "removePriestRestrictions: " + removePriestRestrictions);
		logger.log(Level.INFO, "favorLimit: " + favorLimit);
		logger.log(Level.INFO, "allowAllSpells: " + allowAllSpells);
		logger.log(Level.INFO, "allowLightSpells: " + allowLightSpells);
		logger.log(Level.INFO, "unlimitedPrayers: " + unlimitedPrayers);
		logger.log(Level.INFO, "noPrayerDelay: " + noPrayerDelay);
	}

	@Override
	public void preInit() {
		// No bytecode hooks needed - framework handles it via PriestRestrictionPatch
	}

	@Override
	public void init() {
		// No bytecode hooks needed - framework handles it via PrayerFaithPatch
	}

	/**
	 * Override priest restrictions for crafting actions.
	 * Fires when isPriest() checks occur in improve/polish/temper methods.
	 */
	@SubscribeEvent
	public void onPriestRestrictionCheck(PriestRestrictionCheckEvent event) {
		if (removePriestRestrictions) {
			// Suppress priest status to allow crafting
			event.setPriest(false);
		}
	}

	/**
	 * Modify prayer counts and delays to allow unlimited prayers.
	 * Fires when prayer faith is being set or checked.
	 */
	@SubscribeEvent
	public void onPrayerFaith(PrayerFaithEvent event) {
		if (unlimitedPrayers) {
			// Reset prayer count to 0 to allow unlimited prayers
			event.setNumFaith((byte) 0);
		}
		if (noPrayerDelay) {
			// Reset last prayer time to 0 to remove time delay
			event.setLastFaith(0L);
		}
	}
}
