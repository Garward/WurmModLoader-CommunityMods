package com.garward.wurmmodloader.mods.meditation;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

public class MeditationMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(MeditationMod.class.getName());

    boolean simplifyMeditationTerrain = true;
    boolean removeInsanitySotG = true;
    boolean removeHateWarBonus = true;
    boolean insanitySpeedBonus = true;
    boolean hateMovementBonus = true;
    boolean scalingPowerStaminaBonus = true;
    boolean scalingKnowledgeSkillGain = true;
    boolean removeMeditationTickTimer = true;
    boolean newMeditationBuffs = true;
    boolean enableMeditationAbilityCooldowns = true;

    long loveRefreshCooldown = 64800000L;
    long loveEnchantNatureCooldown = 64800000L;
    long loveLoveEffectCooldown = 64800000L;
    long hateWarDamageCooldown = 64800000L;
    long hateStructureDamageCooldown = 64800000L;
    long hateFearCooldown = 64800000L;
    long powerElementalImmunityCooldown = 64800000L;
    long powerEruptFreezeCooldown = 64800000L;
    long powerIgnoreTrapsCooldown = 64800000L;
    long knowledgeInfoCreatureCooldown = 64800000L;
    long knowledgeInfoTileCooldown = 64800000L;

    @Override
    public void configure(Properties properties) {
        simplifyMeditationTerrain = bool(properties, "simplifyMeditationTerrain", simplifyMeditationTerrain);
        removeInsanitySotG = bool(properties, "removeInsanitySotG", removeInsanitySotG);
        removeHateWarBonus = bool(properties, "removeHateWarBonus", removeHateWarBonus);
        insanitySpeedBonus = bool(properties, "insanitySpeedBonus", insanitySpeedBonus);
        hateMovementBonus = bool(properties, "hateMovementBonus", hateMovementBonus);
        scalingPowerStaminaBonus = bool(properties, "scalingPowerStaminaBonus", scalingPowerStaminaBonus);
        scalingKnowledgeSkillGain = bool(properties, "scalingKnowledgeSkillGain", scalingKnowledgeSkillGain);
        removeMeditationTickTimer = bool(properties, "removeMeditationTickTimer", removeMeditationTickTimer);
        newMeditationBuffs = bool(properties, "newMeditationBuffs", newMeditationBuffs);
        enableMeditationAbilityCooldowns = bool(properties, "enableMeditationAbilityCooldowns",
                enableMeditationAbilityCooldowns);

        loveRefreshCooldown = longProp(properties, "loveRefreshCooldown", loveRefreshCooldown);
        loveEnchantNatureCooldown = longProp(properties, "loveEnchantNatureCooldown", loveEnchantNatureCooldown);
        loveLoveEffectCooldown = longProp(properties, "loveLoveEffectCooldown", loveLoveEffectCooldown);
        hateWarDamageCooldown = longProp(properties, "hateWarDamageCooldown", hateWarDamageCooldown);
        hateStructureDamageCooldown = longProp(properties, "hateStructureDamageCooldown", hateStructureDamageCooldown);
        hateFearCooldown = longProp(properties, "hateFearCooldown", hateFearCooldown);
        powerElementalImmunityCooldown = longProp(properties, "powerElementalImmunityCooldown",
                powerElementalImmunityCooldown);
        powerEruptFreezeCooldown = longProp(properties, "powerEruptFreezeCooldown", powerEruptFreezeCooldown);
        powerIgnoreTrapsCooldown = longProp(properties, "powerIgnoreTrapsCooldown", powerIgnoreTrapsCooldown);
        knowledgeInfoCreatureCooldown = longProp(properties, "knowledgeInfoCreatureCooldown",
                knowledgeInfoCreatureCooldown);
        knowledgeInfoTileCooldown = longProp(properties, "knowledgeInfoTileCooldown", knowledgeInfoTileCooldown);
    }

    @Override
    public void preInit() {
        MeditationPatches.install(this);
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static long longProp(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException nfe) {
            logger.warning("[meditation] bad long for " + key + "=" + v + ", using " + def);
            return def;
        }
    }
}
