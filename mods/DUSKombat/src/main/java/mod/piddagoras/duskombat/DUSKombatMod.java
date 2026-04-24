package mod.piddagoras.duskombat;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Materials;
import mod.piddagoras.duskombat.util.Prop;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.combat.CombatAttackEvent;
import com.garward.wurmmodloader.api.events.combat.SpecialMoveHandleEvent;
import com.garward.wurmmodloader.api.events.combat.SpecialMoveSendEvent;
import com.garward.wurmmodloader.api.events.item.ItemEnchantmentStringsEvent;
import com.garward.wurmmodloader.api.events.player.PlayerSkillLossEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;

import java.util.Properties;
import java.util.logging.Logger;

public class DUSKombatMod
implements WurmServerMod, Configurable, PreInitable, ItemTemplatesCreatedListener, ServerStartedListener {
	public static Logger logger = Logger.getLogger(DUSKombatMod.class.getName());

	// Global toggle to completely disable the mod if its features are unwanted.
	public static boolean enableDUSKombat = true;

	public static float minimumSwingTimer = 3.0f;
	public static boolean useEpicBloodthirst = true;
	public static boolean showItemCombatInformation = true;
	public static boolean disablePlayerSkillLoss = false;

	// Damage Multipliers
    public static float playerToEnvironmentDamageMultiplier = 1.0f;
    public static float environmentToPlayerDamageMultiplier = 1.0f;
    public static float playerToPlayerDamageMultiplier = 1.0f;

    // Miscellaneous options
    public static float combatEnchantCap = 0; // A setting of 0 disables this.

    public static float getCombatEnchantCap(){
        return combatEnchantCap;
    }

    public static void pollCreatureActionStacks(){
        for(Creature c : Creatures.getInstance().getCreatures()){
            if(c.isFighting()) {
                c.getActions().poll(c);
            }
        }
    }

    public static byte parseMaterialType(String str){
	    byte mat = Materials.convertMaterialStringIntoByte(str);
	    if(mat > 0){
	        return mat;
        }
        return Byte.parseByte(str);
    }

    @Override
	public void configure(Properties properties) {
		logger.info("Beginning configuration...");
		Prop.properties = properties;

		// Base Configuration
        enableDUSKombat = Prop.getBooleanProperty("enableDUSKombat", enableDUSKombat);
		minimumSwingTimer = Prop.getFloatProperty("minimumSwingTimer", minimumSwingTimer);
		useEpicBloodthirst = Prop.getBooleanProperty("useEpicBloodthirst", useEpicBloodthirst);
		showItemCombatInformation = Prop.getBooleanProperty("showItemCombatInformation", showItemCombatInformation);
        disablePlayerSkillLoss = Prop.getBooleanProperty("disablePlayerSkillLoss", disablePlayerSkillLoss);

		// Damage Multipliers
        playerToEnvironmentDamageMultiplier = Prop.getFloatProperty("playerToEnvironmentDamageMultiplier", playerToEnvironmentDamageMultiplier);
        environmentToPlayerDamageMultiplier = Prop.getFloatProperty("environmentToPlayerDamageMultiplier", environmentToPlayerDamageMultiplier);
        playerToPlayerDamageMultiplier = Prop.getFloatProperty("playerToPlayerDamageMultiplier", playerToPlayerDamageMultiplier);

        combatEnchantCap = Prop.getFloatProperty("combatEnchantCap", combatEnchantCap);
        String combatEnchantCapString = String.format("%.2f", Float.valueOf(combatEnchantCap));
        if (combatEnchantCap <= 0){
            combatEnchantCap = Float.MAX_VALUE; // If the setting is 0 or lower, we don't need an enchant cap.
            combatEnchantCapString = "Uncapped";
        }

    	for (String name : properties.stringPropertyNames()) {
            try {
                String value = properties.getProperty(name);
                switch (name) {
                    case "debug":
                    case "classname":
                    case "classpath":
                    case "sharedClassLoader":
                    case "depend.import":
                    case "depend.suggests":
                        break; //ignore
                    default:
                    	/*if (name.startsWith("weaponDamage")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            float newVal = Float.parseFloat(split[1]);
                            weaponDamage.put(weaponId, newVal);
                        } else {
                            logger.warning("Unknown config property: " + name);
                        }*/
                }
            } catch (Exception e) {
                logger.severe("Error processing property " + name);
                e.printStackTrace();
            }
        }
        // Print values of configuration
        logger.info("Enable DUSKombat: "+enableDUSKombat);
        if (!enableDUSKombat){
            return;
        }
        logger.info(" -- Mod Configuration -- ");
    	logger.info(String.format("Minimum Swing Timer: %.2f seconds", Float.valueOf(minimumSwingTimer)));
    	logger.info(String.format("Use Epic Bloodthirst: %s", Boolean.valueOf(useEpicBloodthirst)));
    	logger.info(String.format("Show Item Combat Information: %s", Boolean.valueOf(showItemCombatInformation)));
    	logger.info(String.format("Disable Player Skill Loss: %s", Boolean.valueOf(disablePlayerSkillLoss)));
    	logger.info("> Damage Multipliers <");
        logger.info(String.format("Player to Environment: %.2fx", Float.valueOf(playerToEnvironmentDamageMultiplier)));
        logger.info(String.format("Environment to Player: %.2fx", Float.valueOf(environmentToPlayerDamageMultiplier)));
        logger.info(String.format("Player to Player: %.2fx", Float.valueOf(playerToPlayerDamageMultiplier)));
        logger.info("> Miscellaneous Features <");
        logger.info(String.format("Combat Damage Enchant Cap: %s", combatEnchantCapString));
        logger.info(" -- Configuration complete -- ");
    }

	@Override
	public void preInit(){
        if (!enableDUSKombat){
            return;
        }
		logger.info("DUSKombat using WurmModLoader event-based architecture - no bytecode hooks needed!");
    }

	@Override
	public void onItemTemplatesCreated(){
		logger.info("Beginning onItemTemplatesCreated...");
	}

	@Override
	public void onServerStarted(){
		logger.info("Beginning onServerStarted...");
	}

	// ========================================================================
	// Event Handlers - Replace legacy bytecode hooks
	// ========================================================================

	/**
	 * Replace CombatHandler.attack() hook with event handler
	 */
	@SubscribeEvent
	public void onCombatAttack(CombatAttackEvent event) {
		if (!enableDUSKombat) {
			return;
		}

		// Cancel vanilla processing and handle attack ourselves
		event.cancel();
		boolean result = DUSKombat.attackHandled(
			event.getAttacker(),
			event.getDefender(),
			event.getCombatCounter(),
			event.isOpportunity(),
			event.getActionCounter(),
			event.getAction()
		);
		event.setResult(result);
	}

	/**
	 * Replace Zones.pollNextZones() hook with event handler
	 */
	@SubscribeEvent
	public void onServerPoll(ServerPollEvent event) {
		if (!enableDUSKombat) {
			return;
		}
		pollCreatureActionStacks();
		DUSKombat.onServerPoll();
	}

	/**
	 * Replace Item.sendEnchantmentStrings() hook with event handler
	 */
	@SubscribeEvent
	public void onItemEnchantmentStrings(ItemEnchantmentStringsEvent event) {
		logger.info("[DEBUG] onItemEnchantmentStrings called! Item: " + event.getItem().getTemplate().getName() +
		           ", enableDUSKombat=" + enableDUSKombat + ", showItemCombatInformation=" + showItemCombatInformation);
		if (!enableDUSKombat || !showItemCombatInformation) {
			logger.info("[DEBUG] Skipping - mod disabled or show disabled");
			return;
		}
		logger.info("[DEBUG] Calling ItemInfo.handleExamine...");
		ItemInfo.handleExamine(event.getExaminer().getCommunicator(), event.getItem());
		logger.info("[DEBUG] ItemInfo.handleExamine completed");
	}

	/**
	 * Replace CombatHandler.sendSpecialMoves() hook with event handler
	 */
	@SubscribeEvent
	public void onSpecialMoveSend(SpecialMoveSendEvent event) {
		if (!enableDUSKombat) {
			return;
		}
		// Cancel vanilla special move UI and send our custom one
		event.cancel();
		SpecialMoves.sendSpecialMoves(event.getCreature());
	}

	/**
	 * Replace CreatureBehaviour.handle_SPECMOVE() hook with event handler
	 */
	@SubscribeEvent
	public void onSpecialMoveHandle(SpecialMoveHandleEvent event) {
		if (!enableDUSKombat) {
			return;
		}
		// Cancel vanilla special move handling and use our custom system
		event.cancel();
		boolean result = SpecialMoves.handleSpecialMove(
			event.getPerformer(),
			event.getTarget(),
			event.getAction(),
			event.getCounter()
		);
		event.setResult(result);
	}

	/**
	 * Replace Creature.punishSkills() hook with event handler
	 */
	@SubscribeEvent
	public void onPlayerSkillLoss(PlayerSkillLossEvent event) {
		if (!enableDUSKombat || !disablePlayerSkillLoss) {
			return;
		}

		// Only prevent skill loss for players
		if (event.isPlayer()) {
			event.cancel();
			event.getCreature().getCommunicator().sendSafeServerMessage("Your knowledge is kept safe.");
		}
	}
}
