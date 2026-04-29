package com.garward.wurmmodloader.mods.bounty;

import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Typed view of {@code mod.properties}. Fields are {@code package-private}
 * so the mod, hooks and patches can read the live config without going
 * through getters.
 */
final class BountyConfig {

    private static final Logger logger = Logger.getLogger(BountyConfig.class.getName());

    boolean enablePlayerBounty = true;
    boolean enableCorpseLoot = true;
    boolean enableGoblinMetalDrop = true;
    boolean enableChampionLoot = true;
    boolean broadcastInterestingLoot = false;

    double pvpBountyMultiplier = 1.2;
    long bountyFloorIron = 0L;
    long bountyCeilingIron = 0L;     // 0 == no cap

    double strengthScale = 0.8;
    double strengthFloor = 100.0;
    double strengthCap = 100_000.0;

    void load(Properties p) {
        enablePlayerBounty       = bool(p, "enablePlayerBounty",       enablePlayerBounty);
        enableCorpseLoot         = bool(p, "enableCorpseLoot",         enableCorpseLoot);
        enableGoblinMetalDrop    = bool(p, "enableGoblinMetalDrop",    enableGoblinMetalDrop);
        enableChampionLoot       = bool(p, "enableChampionLoot",       enableChampionLoot);
        broadcastInterestingLoot = bool(p, "broadcastInterestingLoot", broadcastInterestingLoot);

        pvpBountyMultiplier      = dbl(p, "pvpBountyMultiplier", pvpBountyMultiplier);
        bountyFloorIron          = lng(p, "bountyFloorIron",     bountyFloorIron);
        bountyCeilingIron        = lng(p, "bountyCeilingIron",   bountyCeilingIron);

        strengthScale            = dbl(p, "strength.scale", strengthScale);
        strengthFloor            = dbl(p, "strength.floor", strengthFloor);
        strengthCap              = dbl(p, "strength.cap",   strengthCap);

        // Walk every key once and route the prefix.* / rewardOverride.*
        // entries into the registry. This keeps the property surface
        // open-ended without a fixed schema.
        int prefixCount = 0;
        int rewardCount = 0;
        for (String key : p.stringPropertyNames()) {
            if (key.startsWith("prefix.")) {
                String prefix = key.substring("prefix.".length()).trim();
                if (prefix.isEmpty()) continue;
                try {
                    BountyRegistry.setTypePrefixMultiplier(prefix,
                            Double.parseDouble(p.getProperty(key).trim()));
                    prefixCount++;
                } catch (NumberFormatException e) {
                    logger.warning("[bounty] bad prefix multiplier for "
                            + key + "=" + p.getProperty(key));
                }
            } else if (key.startsWith("rewardOverride.")) {
                String name = key.substring("rewardOverride.".length()).trim();
                if (name.isEmpty()) continue;
                try {
                    BountyRegistry.setReward(name.toLowerCase(Locale.ROOT),
                            Long.parseLong(p.getProperty(key).trim()));
                    rewardCount++;
                } catch (NumberFormatException e) {
                    logger.warning("[bounty] bad reward override for "
                            + key + "=" + p.getProperty(key));
                }
            }
        }
        logger.info("[bounty] config loaded: " + prefixCount + " prefix multipliers, "
                + rewardCount + " reward overrides");
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static long lng(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try { return Long.parseLong(v.trim()); }
        catch (NumberFormatException e) {
            logger.warning("[bounty] bad long for " + key + "=" + v);
            return def;
        }
    }

    private static double dbl(Properties p, String key, double def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try { return Double.parseDouble(v.trim()); }
        catch (NumberFormatException e) {
            logger.warning("[bounty] bad double for " + key + "=" + v);
            return def;
        }
    }
}
