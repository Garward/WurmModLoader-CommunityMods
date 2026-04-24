package mod.sin.armoury;

import com.wurmonline.server.items.Materials;
import com.garward.wurmmodloader.api.config.ModProperties;
import com.garward.wurmmodloader.api.events.ModQueryEvent;
import com.garward.wurmmodloader.api.events.ModActionEvent;
import com.garward.wurmmodloader.api.events.action.ActionSpeedModifierEvent;
import com.garward.wurmmodloader.api.events.action.ActionTimeCalculationEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.core.event.EventBus;
import com.garward.wurmmodloader.api.events.combat.CombatDualWieldEvent;
import com.garward.wurmmodloader.api.events.combat.CombatSwingSpeedEvent;
import com.garward.wurmmodloader.api.events.combat.WeaponUseEvent;
import com.garward.wurmmodloader.api.events.combat.shield.ShieldCheckEvent;
import com.garward.wurmmodloader.api.events.combat.shield.ShieldDamageEvent;
import com.garward.wurmmodloader.api.events.combat.weapon.WeaponStatQueryEvent;
import com.garward.wurmmodloader.api.events.item.material.MaterialBonusEvent;
import com.garward.wurmmodloader.api.events.item.material.MaterialDamageModifierEvent;
import com.garward.wurmmodloader.api.events.item.material.MaterialDecayModifierEvent;
import com.garward.wurmmodloader.api.events.item.material.MaterialImpBonusEvent;
import com.garward.wurmmodloader.api.events.item.material.MaterialRepairTimeEvent;
import com.garward.wurmmodloader.api.events.skill.SkillAdvanceEvent;
import com.garward.wurmmodloader.api.support.ArmourTypeRegistry;
import com.garward.wurmmodloader.api.support.WoundTypeRegistry;
import com.garward.wurmmodloader.modloader.interfaces.ItemTemplatesCreatedListener;
import com.garward.wurmmodloader.modloader.interfaces.ServerStartedListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArmouryModMain
implements WurmServerMod, Configurable, PreInitable, ItemTemplatesCreatedListener, ServerStartedListener {
	public static Logger logger = Logger.getLogger(ArmouryModMain.class.getName());

	// - Armour Configuration - //
	public static boolean enableArmourModifications = true;

	// - Shield Configuration -- //
	public static boolean enableShieldDamageEnchants = true;
	public static boolean enableShieldSpeedEnchants = true;
	
	// -- Weapon Configuration -- //
	public static float minimumSwingTime = 3.0f;
	public static boolean raresReduceSwingTime = true;
	public static float rareSwingSpeedReduction = 0.2f;
	public static boolean fixSavedSwingTimer = true;
	public static boolean betterDualWield = false; // HIGHLY EXPERIMENTAL
    public static boolean enableWeaponMaterialChanges = true;
    public static boolean enableItemMaterialChanges = true;
	// Weapon variable changes
	public static HashMap<Integer, Float> weaponDamage = new HashMap<>();
	public static HashMap<Integer, Float> weaponSpeed = new HashMap<>();
	public static HashMap<Integer, Float> weaponCritChance = new HashMap<>();
	public static HashMap<Integer, Integer> weaponReach = new HashMap<>();
	public static HashMap<Integer, Integer> weaponWeightGroup = new HashMap<>();
	public static HashMap<Integer, Float> weaponParryPercent = new HashMap<>();
	public static HashMap<Integer, Double> weaponSkillPenalty = new HashMap<>();

    public static byte parseArmourType(String str){
        return (byte) ArmourTypeRegistry.getArmourTypeId(str);
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

        ModProperties config = ModProperties.from(properties);

        // Initialization sequences
        MaterialsTweaks.initializeMaterialMaps();
        WeaponsTweaks.initializeWeaponMaps();

        // Armour Configuration
        enableArmourModifications = config.getBoolean("enableArmourModifications", enableArmourModifications);
        // Shield Configuration
        enableShieldDamageEnchants = config.getBoolean("enableShieldDamageEnchants", enableShieldDamageEnchants);
        // Weapon Configuration
        minimumSwingTime = config.getFloat("minimumSwingTime", minimumSwingTime);
        raresReduceSwingTime = config.getBoolean("raresReduceSwingTime", raresReduceSwingTime);
        rareSwingSpeedReduction = config.getFloat("rareSwingSpeedReduction", rareSwingSpeedReduction);
        fixSavedSwingTimer = config.getBoolean("fixSavedSwingTimer", fixSavedSwingTimer);
        betterDualWield = config.getBoolean("betterDualWield", betterDualWield);

        enableWeaponMaterialChanges = config.getBoolean("enableWeaponMaterialChanges", enableWeaponMaterialChanges);
        enableItemMaterialChanges = config.getBoolean("enableItemMaterialChanges", enableItemMaterialChanges);
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
                    case "minimumSwingTime":
                    case "rareSwingSpeedReduction":
                    case "fixedSavedSwingTimer":
                    case "betterDualWield":
                        break; // ignore properties that are already configured
                    default:
                        if (name.startsWith("armourDamageReduction")) {
                            String[] split = value.split(",");
                            byte armourType = parseArmourType(split[0]);
                            float reduction = Float.parseFloat(split[1]);
                            ArmourTemplateTweaks.addArmourDamageReduction(armourType, reduction);
                        } else if (name.startsWith("armourEffectiveness")) {
                            String[] split = value.split(";");
                            byte armourType = parseArmourType(split[0]);
                            String[] split2 = split[1].split(",");
                            ArmourTemplateTweaks.addArmourEffectiveness(armourType, split2);
                        } else if (name.startsWith("armourGlanceRate")) {
                            String[] split = value.split(";");
                            byte armourType = parseArmourType(split[0]);
                            String[] split2 = split[1].split(",");
                            ArmourTemplateTweaks.addArmourGlanceRate(armourType, split2);
                        } else if (name.startsWith("armourLimitFactor")) {
                            String[] split = value.split(",");
                            byte armourType = parseArmourType(split[0]);
                            float reduction = Float.parseFloat(split[1]);
                            ArmourTemplateTweaks.addArmourLimitFactor(armourType, reduction);
                        } else if (name.startsWith("armourMovement")) {
                            String[] split = value.split(",");
                            String itemTemplate = split[0];
                            float movementPenalty = Float.parseFloat(split[1]);
                            ArmourTemplateTweaks.addArmourMovement(itemTemplate, movementPenalty);
                        } else if (name.startsWith("materialDamageReduction")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float reduction = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialDamageReduction(material, reduction);
                        } else if (name.startsWith("materialMovementModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float modifier = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialMovementModifier(material, modifier);
                        } else if (name.startsWith("materialWeaponDamage")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            double mult = Double.parseDouble(split[1]);
                            WeaponsTweaks.addMaterialWeaponDamage(material, mult);
                        } else if (name.startsWith("materialWeaponSpeed")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float mult = Float.parseFloat(split[1]);
                            WeaponsTweaks.addMaterialWeaponSpeed(material, mult);
                        } else if (name.startsWith("materialWeaponParry")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float mult = Float.parseFloat(split[1]);
                            WeaponsTweaks.addMaterialWeaponParry(material, mult);
                        } else if (name.startsWith("materialWeaponArmourDamage")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            double mult = Double.parseDouble(split[1]);
                            WeaponsTweaks.addMaterialWeaponArmourDamage(material, mult);
                        } else if (name.startsWith("materialDamageModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float mult = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialDamageModifier(material, mult);
                        } else if (name.startsWith("materialDecayModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float mult = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialDecayModifier(material, mult);
                        } else if (name.startsWith("materialCreationBonus")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float bonus = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialCreationBonus(material, bonus);
                        } else if (name.startsWith("materialImproveBonus")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float bonus = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialImproveBonus(material, bonus);
                        } else if (name.startsWith("materialShatterResistance")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float resistance = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialShatterResistance(material, resistance);
                        } else if (name.startsWith("materialLockpickBonus")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float bonus = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialLockpickBonus(material, bonus);
                        } else if (name.startsWith("materialAnchorBonus")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float bonus = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialAnchorBonus(material, bonus);
                        } else if (name.startsWith("materialPendulumEffect")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float bonus = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialPendulumEffect(material, bonus);
                        } else if (name.startsWith("materialRepairSpeed")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float mult = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialRepairSpeed(material, mult);
                        } else if (name.startsWith("materialBashModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            double mult = Double.parseDouble(split[1]);
                            MaterialsTweaks.addMaterialBashModifier(material, mult);
                        } else if (name.startsWith("materialSpellEffectModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            float mult = Float.parseFloat(split[1]);
                            MaterialsTweaks.addMaterialSpellEffectModifier(material, mult);
                        } else if (name.startsWith("materialSpecificSpellEffectModifier")) {
                            String[] split = value.split(";");
                            byte material = parseMaterialType(split[0]);
                            String[] split2 = split[1].split(",");
                            MaterialsTweaks.addMaterialSpecificSpellEffectModifier(material, split2);
                        } else if (name.startsWith("materialDifficultyModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            double mult = Double.parseDouble(split[1]);
                            MaterialsTweaks.addMaterialDifficultyModifier(material, mult);
                        } else if (name.startsWith("materialActionSpeedModifier")) {
                            String[] split = value.split(",");
                            byte material = parseMaterialType(split[0]);
                            double mult = Double.parseDouble(split[1]);
                            MaterialsTweaks.addMaterialActionSpeedModifier(material, mult);
                        } else if (name.startsWith("weaponDamage")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            float newVal = Float.parseFloat(split[1]);
                            weaponDamage.put(weaponId, newVal);
                        } else if (name.startsWith("weaponSpeed")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            float newVal = Float.parseFloat(split[1]);
                            weaponSpeed.put(weaponId, newVal);
                        } else if (name.startsWith("weaponCritChance")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            float newVal = Float.parseFloat(split[1]);
                            weaponCritChance.put(weaponId, newVal);
                        } else if (name.startsWith("weaponReach")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            int newVal = Integer.parseInt(split[1]);
                            weaponReach.put(weaponId, newVal);
                        } else if (name.startsWith("weaponWeightGroup")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            int newVal = Integer.parseInt(split[1]);
                            weaponWeightGroup.put(weaponId, newVal);
                        } else if (name.startsWith("weaponParryPercent")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            float newVal = Float.parseFloat(split[1]);
                            weaponParryPercent.put(weaponId, newVal);
                        } else if (name.startsWith("weaponSkillPenalty")) {
                        	String[] split = value.split(",");
                            int weaponId = Integer.parseInt(split[0]);
                            double newVal = Double.parseDouble(split[1]);
                            weaponSkillPenalty.put(weaponId, newVal);
                        } else {
                            logger.warning("Unknown config property: " + name);
                        }
                }
            } catch (Exception e) {
                logger.severe("Error processing property " + name);
                e.printStackTrace();
            }
        }
        // Print configuration values
        logger.info(" -- Armour Configuration -- ");
    	logger.info("enableArmourModifications: " + enableArmourModifications);
        logger.info("> Armour Damage Reduction Settings <");
        for(byte armourType : ArmourTemplateTweaks.armourDamageReduction.keySet()){
            logger.info(String.format("Damage reduction for armour %s: %.2f%%", ArmourTypeRegistry.getArmourTypeName(armourType), ArmourTemplateTweaks.armourDamageReduction.get(armourType)*100f));
        }
        logger.info("> Armour Effectiveness Settings <");
        for(byte armourType : ArmourTemplateTweaks.armourEffectiveness.keySet()){
            HashMap<Byte, Float> woundMap = ArmourTemplateTweaks.armourEffectiveness.get(armourType);
            for(byte woundType : woundMap.keySet()){
                String wound = WoundTypeRegistry.getWoundName(woundType);
                logger.info(String.format("Effectiveness for armour %s against %s: %.2f%%", ArmourTypeRegistry.getArmourTypeName(armourType), wound, woundMap.get(woundType)*100f));
            }
        }
        logger.info("> Armour Glance Rate Settings <");
        for(byte armourType : ArmourTemplateTweaks.armourGlanceRates.keySet()){
            HashMap<Byte, Float> woundMap = ArmourTemplateTweaks.armourGlanceRates.get(armourType);
            for(byte woundType : woundMap.keySet()){
                String wound = WoundTypeRegistry.getWoundName(woundType);
                logger.info(String.format("Glance rate for armour %s against %s: %.2f%%", ArmourTypeRegistry.getArmourTypeName(armourType), wound, woundMap.get(woundType)*100f));
            }
        }
        logger.info("> Armour Limit Factor Settings <");
        for(byte armourType : ArmourTemplateTweaks.armourLimitFactors.keySet()){
            logger.info(String.format("Limit factor for armour %s: %.2f%%", ArmourTypeRegistry.getArmourTypeName(armourType), ArmourTemplateTweaks.armourLimitFactors.get(armourType)*100f));
        }
        logger.info("> Armour Movement Rate Changes <");
        for(String armourName : ArmourTemplateTweaks.armourMovement.keySet()){
            logger.info(String.format("Movement penalty for armour %s changed to %.2f%%", armourName, ArmourTemplateTweaks.armourMovement.get(armourName)*100f));
        }
        logger.info(" -- Material Configuration -- ");
        logger.info("> Armour Material Damage Reduction Settings <");
        for(byte material : MaterialsTweaks.materialDamageReduction.keySet()){
            logger.info(String.format("Base DR modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialDamageReduction.get(material)*100f));
        }
        logger.info("> Armour Material Movement Modifier Settings <");
        for(byte material : MaterialsTweaks.materialMovementModifier.keySet()){
            logger.info(String.format("Movement Speed modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialMovementModifier.get(material)*100f));
        }
        logger.info("> Weapon Material Damage Settings <");
        for(byte material : WeaponsTweaks.materialWeaponDamage.keySet()){
            logger.info(String.format("Damage modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), WeaponsTweaks.materialWeaponDamage.get(material)*100f));
        }
        logger.info("> Weapon Material Speed Settings <");
        for(byte material : WeaponsTweaks.materialWeaponSpeed.keySet()){
            logger.info(String.format("Speed modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), WeaponsTweaks.materialWeaponSpeed.get(material)*100f));
        }
        logger.info("> Weapon Material Parry Settings <");
        for(byte material : WeaponsTweaks.materialWeaponParry.keySet()){
            logger.info(String.format("Parry modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), WeaponsTweaks.materialWeaponParry.get(material)*100f));
        }
        logger.info("> Weapon Material Armour Damage Settings <");
        for(byte material : WeaponsTweaks.materialWeaponArmourDamage.keySet()){
            logger.info(String.format("Armour Damage modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), WeaponsTweaks.materialWeaponArmourDamage.get(material)*100f));
        }
        logger.info("> Item Material Damage Modifier Settings <");
        for(byte material : MaterialsTweaks.materialDamageModifier.keySet()){
            logger.info(String.format("Damage modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialDamageModifier.get(material)*100f));
        }
        logger.info("> Item Material Decay Modifier Settings <");
        for(byte material : MaterialsTweaks.materialDecayModifier.keySet()){
            logger.info(String.format("Decay modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialDecayModifier.get(material)*100f));
        }
        logger.info("> Item Material Creation Bonus Settings <");
        for(byte material : MaterialsTweaks.materialCreationBonus.keySet()){
            logger.info(String.format("Creation bonus for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialCreationBonus.get(material)*100f));
        }
        logger.info("> Item Material Improve Bonus Settings <");
        for(byte material : MaterialsTweaks.materialImproveBonus.keySet()){
            logger.info(String.format("Improve bonus for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialImproveBonus.get(material)*100f));
        }
        logger.info("> Item Material Shatter Resistance Settings <");
        for(byte material : MaterialsTweaks.materialShatterResistance.keySet()){
            logger.info(String.format("Shatter resistance for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialShatterResistance.get(material)*100f));
        }
        logger.info("> Item Material Lockpick Bonus Settings <");
        for(byte material : MaterialsTweaks.materialLockpickBonus.keySet()){
            logger.info(String.format("Lockpick bonus for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialLockpickBonus.get(material)*100f));
        }
        logger.info("> Item Material Anchor Bonus Settings <");
        for(byte material : MaterialsTweaks.materialAnchorBonus.keySet()){
            logger.info(String.format("Anchor bonus for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialAnchorBonus.get(material)*100f));
        }
        logger.info("> Item Material Pendulum Effect Settings <");
        for(byte material : MaterialsTweaks.materialPendulumEffect.keySet()){
            logger.info(String.format("Pendulum effect for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialPendulumEffect.get(material)*100f));
        }
        logger.info("> Item Material Repair Speed Settings <");
        for(byte material : MaterialsTweaks.materialRepairSpeed.keySet()){
            logger.info(String.format("Repair speed for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialRepairSpeed.get(material)*100f));
        }
        logger.info("> Item Material Bash Modifier Settings <");
        for(byte material : MaterialsTweaks.materialBashModifier.keySet()){
            logger.info(String.format("Bash modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialBashModifier.get(material)*100f));
        }
        logger.info("> Item Material Spell Effect Modifier Settings <");
        for(byte material : MaterialsTweaks.materialSpellEffectModifier.keySet()){
            logger.info(String.format("Spell effect modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialSpellEffectModifier.get(material)*100f));
        }
        logger.info("> Item Material Specific Spell Effect Modifier Settings <");
        for(byte material : MaterialsTweaks.materialSpecificSpellEffectModifier.keySet()){
            //String name = materialNameReference.containsKey(material) ? materialNameReference.get(material) : String.valueOf(material);
            HashMap<Byte, Float> enchantMap = MaterialsTweaks.materialSpecificSpellEffectModifier.get(material);
            for(byte enchant : enchantMap.keySet()){
                logger.info(String.format("Spell Effect Power for material %s with enchant %s: %.2f%%", MaterialsTweaks.getMaterialName(material), enchant, enchantMap.get(enchant)*100f));
            }
        }
        logger.info("> Item Material Difficulty Modifier Settings <");
        for(byte material : MaterialsTweaks.materialDifficultyModifier.keySet()){
            logger.info(String.format("Difficulty modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialDifficultyModifier.get(material)*100f));
        }
        logger.info("> Item Material Action Speed Modifier Settings <");
        for(byte material : MaterialsTweaks.materialActionSpeedModifier.keySet()){
            logger.info(String.format("Action Speed modifier for material %s: %.2f%%", MaterialsTweaks.getMaterialName(material), MaterialsTweaks.materialActionSpeedModifier.get(material)*100f));
        }
        logger.info(" -- Shield Configuration -- ");
        logger.log(Level.INFO, "enableShieldDamageEnchants: " + enableShieldDamageEnchants);
        logger.info(" -- Weapon Configuration -- ");
        logger.log(Level.INFO, "minimumSwingTime: " + minimumSwingTime);
        logger.log(Level.INFO, "raresReduceSwingTime: " + raresReduceSwingTime);
        logger.log(Level.INFO, "rareSwingSpeedReduction: " + rareSwingSpeedReduction);
        logger.log(Level.INFO, "fixSavedSwingTimer: " + fixSavedSwingTimer);
        logger.log(Level.INFO, "betterDualWield: " + betterDualWield);
        logger.info(" -- Configuration complete -- ");
    }

    @Override
	public void preInit(){
        if (enableItemMaterialChanges || enableWeaponMaterialChanges) {
            MaterialsTweaks.registerMaterialProfiles(enableItemMaterialChanges, enableWeaponMaterialChanges);
        }
        if (minimumSwingTime != 3.0f || raresReduceSwingTime) {
            CombatsTweaks.registerSwingProfile(minimumSwingTime, raresReduceSwingTime, rareSwingSpeedReduction);
        }
        if (fixSavedSwingTimer) {
            CombatsTweaks.registerWeaponTimerReset(0f);
        }
        if (betterDualWield) {
            CombatsTweaks.registerDualWieldProfile(true);
        }
	}
	
	@Override
	public void onItemTemplatesCreated(){
		logger.info("Beginning onItemTemplatesCreated...");
	}
	
	@Override
	public void onServerStarted(){
		WeaponsTweaks.onServerStarted();
        ArmourTemplateTweaks.onServerStarted();
	}

    @SubscribeEvent
    public void onShieldCheck(ShieldCheckEvent event) {
        if (!enableShieldSpeedEnchants) {
            return;
        }
        ShieldsTweaks.handleShieldCheck(event);
    }

    @SubscribeEvent
    public void onShieldDamage(ShieldDamageEvent event) {
        if (!enableShieldDamageEnchants) {
            return;
        }
        ShieldsTweaks.handleShieldDamage(event);
    }

    // ========================================================================
    // Armoury Public API Events (Forge-style)
    // ========================================================================

    /**
     * Expose armor damage reduction calculations to other mods.
     * Event type: "armoury:armor_damage_reduction"
     */
    @SubscribeEvent
    public void onModQuery(ModQueryEvent event) {
        String eventType = event.getEventType();

        try {
            if (eventType.equals("armoury:armor_damage_reduction")) {
                handleArmorDRQuery(event);
            } else if (eventType.equals("armoury:armor_glance_rate")) {
                handleArmorGlanceQuery(event);
            } else if (eventType.equals("armoury:material_weapon_bonus")) {
                handleMaterialWeaponQuery(event);
            } else if (eventType.equals("armoury:armor_set_check")) {
                handleArmorSetQuery(event);
            } else if (eventType.equals("armoury:material_tool_bonus")) {
                handleMaterialToolQuery(event);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to handle Armoury API event: " + eventType, e);
        }
    }

    /**
     * Handle armor damage reduction queries.
     */
    private void handleArmorDRQuery(ModQueryEvent event) {
        byte armorType = event.get("armorType") != null ? ((Number) event.get("armorType")).byteValue() : -1;
        byte woundType = event.get("woundType") != null ? ((Number) event.get("woundType")).byteValue() : 0;

        if (armorType == -1) return;

        // Get base DR from Armoury config
        float baseDR = ArmourTemplateTweaks.armourDamageReduction.getOrDefault(armorType, 0.50f);

        // Get effectiveness multiplier
        float effectiveness = 1.0f;
        if (ArmourTemplateTweaks.armourEffectiveness.containsKey(armorType)) {
            HashMap<Byte, Float> effectMap = ArmourTemplateTweaks.armourEffectiveness.get(armorType);
            effectiveness = effectMap.getOrDefault(woundType, 1.0f);
        }

        // Calculate final DR
        float finalDR = baseDR * effectiveness;

        // Apply material modifier if provided
        if (event.has("material")) {
            byte material = ((Number) event.get("material")).byteValue();
            if (MaterialsTweaks.materialDamageReduction.containsKey(material)) {
                float materialMod = MaterialsTweaks.materialDamageReduction.get(material);
                finalDR *= materialMod;
            }
        }

        event.set("baseDR", baseDR);
        event.set("effectiveness", effectiveness);
        event.set("finalDR", finalDR);
        event.setHandled(true);
    }

    /**
     * Handle armor glance rate queries.
     */
    private void handleArmorGlanceQuery(ModQueryEvent event) {
        byte armorType = event.get("armorType") != null ? ((Number) event.get("armorType")).byteValue() : -1;
        byte woundType = event.get("woundType") != null ? ((Number) event.get("woundType")).byteValue() : 0;

        if (armorType == -1) return;

        float baseGlance = 0.05f; // Default 5%

        if (ArmourTemplateTweaks.armourGlanceRates.containsKey(armorType)) {
            HashMap<Byte, Float> glanceMap = ArmourTemplateTweaks.armourGlanceRates.get(armorType);
            baseGlance = glanceMap.getOrDefault(woundType, baseGlance);
        }

        event.set("baseGlance", baseGlance);
        event.set("finalGlance", baseGlance);
        event.setHandled(true);
    }

    /**
     * Handle material weapon bonus queries.
     */
    private void handleMaterialWeaponQuery(ModQueryEvent event) {
        if (!event.has("material")) return;

        byte material = ((Number) event.get("material")).byteValue();

        // Default values
        double damageMultiplier = 1.0;
        float speedMultiplier = 1.0f;
        float parryBonus = 1.0f;
        double armorDamage = 1.0;

        // Get material bonuses
        if (WeaponsTweaks.materialWeaponDamage.containsKey(material)) {
            damageMultiplier = WeaponsTweaks.materialWeaponDamage.get(material);
        }
        if (WeaponsTweaks.materialWeaponSpeed.containsKey(material)) {
            speedMultiplier = WeaponsTweaks.materialWeaponSpeed.get(material);
        }
        if (WeaponsTweaks.materialWeaponParry.containsKey(material)) {
            parryBonus = WeaponsTweaks.materialWeaponParry.get(material);
        }
        if (WeaponsTweaks.materialWeaponArmourDamage.containsKey(material)) {
            armorDamage = WeaponsTweaks.materialWeaponArmourDamage.get(material);
        }

        event.set("damageMultiplier", damageMultiplier);
        event.set("speedMultiplier", speedMultiplier);
        event.set("parryBonus", parryBonus);
        event.set("armorDamage", armorDamage);
        event.setHandled(true);
    }

    /**
     * Handle armor set check queries.
     * Checks what armor pieces a player is wearing.
     */
    private void handleArmorSetQuery(ModQueryEvent event) {
        // This would require access to player equipment slots
        // For now, provide the structure for other mods to implement
        // or wait for equipment API events

        // Placeholder implementation
        Map<Byte, Integer> pieceCount = new HashMap<>();
        byte fullSetType = -1; // -1 = no full set
        List<Long> armorPieces = new ArrayList<>();

        event.set("fullSetType", fullSetType);
        event.set("pieceCount", pieceCount);
        event.set("armorPieces", armorPieces);
        event.setHandled(true);

        logger.fine("armoury:armor_set_check called but requires equipment slot API (not yet implemented)");
    }

    /**
     * Handle material tool bonus queries.
     */
    private void handleMaterialToolQuery(ModQueryEvent event) {
        if (!event.has("material")) return;

        byte material = ((Number) event.get("material")).byteValue();

        // Default values
        double actionSpeedModifier = 1.0;
        float durabilityModifier = 1.0f;
        double difficultyModifier = 1.0;

        // Get material modifiers
        if (MaterialsTweaks.materialActionSpeedModifier.containsKey(material)) {
            actionSpeedModifier = MaterialsTweaks.materialActionSpeedModifier.get(material);
        }
        if (MaterialsTweaks.materialDamageModifier.containsKey(material)) {
            durabilityModifier = MaterialsTweaks.materialDamageModifier.get(material);
        }
        if (MaterialsTweaks.materialDifficultyModifier.containsKey(material)) {
            difficultyModifier = MaterialsTweaks.materialDifficultyModifier.get(material);
        }

        event.set("actionSpeedModifier", actionSpeedModifier);
        event.set("durabilityModifier", durabilityModifier);
        event.set("difficultyModifier", difficultyModifier);
        event.setHandled(true);
    }

}
