package com.garward.wurmmodloader.mods.supplydepot;

import com.garward.wurmmodloader.modsupport.spawn.SpawnFilterConfig;

import java.util.Properties;
import java.util.logging.Logger;

final class SupplyDepotConfig {

    private static final Logger logger = Logger.getLogger(SupplyDepotConfig.class.getName());

    boolean enabled = true;
    /** When true, only spawn while {@code Servers.localServer.PVPSERVER}. */
    boolean requirePvpServer = false;
    long respawnTimeMs = 7_200_000L; // 2 hours, upstream default
    int pollIntervalSeconds = 30;
    long captureMessageInterval = 600_000L; // 10 minutes
    int captureTimer = 2400;
    float fightingSkillRequirement = 25f;
    float captureRadius = 5f;

    /** Light-effect on depot for clients. */
    boolean useDepotLights = true;

    /** Reward roll. */
    int minCaches = 3;
    int maxCaches = 4; // upstream "3 + rand(2)" inclusive 3-4 (upstream comment says 2-3 but code is 3-4)
    int kingdomTokenTemplateId = 22765;
    int minKingdomTokens = 3;
    int maxKingdomTokens = 5;
    int hotaStatueChancePercent = 1;
    int minCopperReward = 10;
    int maxCopperReward = 30;
    /** When set (>=0) and the caches submod is loaded, the enchant orb in the
     *  reward bundle is created from this template id. -1 disables. */
    int enchantOrbTemplateId = -1;

    final SpawnFilterConfig spawn = new SpawnFilterConfig();

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);
        requirePvpServer = bool(p, "requirePvpServer", requirePvpServer);
        respawnTimeMs = longVal(p, "respawnTimeMs", respawnTimeMs);
        pollIntervalSeconds = intVal(p, "pollIntervalSeconds", pollIntervalSeconds);
        captureMessageInterval = longVal(p, "captureMessageInterval", captureMessageInterval);
        captureTimer = intVal(p, "captureTimer", captureTimer);
        fightingSkillRequirement = floatVal(p, "fightingSkillRequirement", fightingSkillRequirement);
        captureRadius = floatVal(p, "captureRadius", captureRadius);
        useDepotLights = bool(p, "useDepotLights", useDepotLights);

        minCaches = intVal(p, "minCaches", minCaches);
        maxCaches = intVal(p, "maxCaches", maxCaches);
        kingdomTokenTemplateId = intVal(p, "kingdomTokenTemplateId", kingdomTokenTemplateId);
        minKingdomTokens = intVal(p, "minKingdomTokens", minKingdomTokens);
        maxKingdomTokens = intVal(p, "maxKingdomTokens", maxKingdomTokens);
        hotaStatueChancePercent = intVal(p, "hotaStatueChancePercent", hotaStatueChancePercent);
        minCopperReward = intVal(p, "minCopperReward", minCopperReward);
        maxCopperReward = intVal(p, "maxCopperReward", maxCopperReward);
        enchantOrbTemplateId = intVal(p, "enchantOrbTemplateId", enchantOrbTemplateId);

        spawn.load(p, "supplydepot");
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int intVal(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[supplydepot] invalid int for " + key + "=" + v + " (using " + def + ")");
            return def;
        }
    }

    private static long longVal(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[supplydepot] invalid long for " + key + "=" + v + " (using " + def + ")");
            return def;
        }
    }

    private static float floatVal(Properties p, String key, float def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Float.parseFloat(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[supplydepot] invalid float for " + key + "=" + v + " (using " + def + ")");
            return def;
        }
    }
}
