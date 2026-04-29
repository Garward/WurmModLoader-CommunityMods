package com.garward.wurmmodloader.mods.treasurechest;

import java.util.Properties;
import java.util.logging.Logger;

final class TreasureChestConfig {

    private static final Logger logger = Logger.getLogger(TreasureChestConfig.class.getName());

    boolean enabled = true;
    boolean boostTierDistribution = true;
    boolean replaceLootTable = true;
    int affinityOrbTemplateId = 22767;

    void load(Properties p) {
        enabled               = bool(p, "enabled",               enabled);
        boostTierDistribution = bool(p, "boostTierDistribution", boostTierDistribution);
        replaceLootTable      = bool(p, "replaceLootTable",      replaceLootTable);
        affinityOrbTemplateId = integer(p, "affinityOrbTemplateId", affinityOrbTemplateId);
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int integer(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) {
            logger.warning("[treasurechest] bad integer for " + key + "=" + v);
            return def;
        }
    }
}
