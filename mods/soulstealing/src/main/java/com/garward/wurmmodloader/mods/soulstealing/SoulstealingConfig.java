package com.garward.wurmmodloader.mods.soulstealing;

import java.util.Properties;
import java.util.logging.Logger;

final class SoulstealingConfig {

    private static final Logger logger = Logger.getLogger(SoulstealingConfig.class.getName());

    boolean enabled = true;
    long pollIntervalMillis = 600_000L;
    boolean registerCreationEntry = false;
    int chaosCrystalTemplateId = -1;

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);

        String raw = p.getProperty("pollIntervalSeconds");
        if (raw != null) {
            try {
                long seconds = Long.parseLong(raw.trim());
                if (seconds > 0) pollIntervalMillis = seconds * 1000L;
            } catch (NumberFormatException e) {
                logger.warning("[soulstealing] pollIntervalSeconds must be a positive integer, got: " + raw);
            }
        }

        registerCreationEntry = bool(p, "registerCreationEntry", registerCreationEntry);
        chaosCrystalTemplateId = intProp(p, "chaosCrystalTemplateId", chaosCrystalTemplateId);

        logger.info("[soulstealing] config loaded — enabled=" + enabled
                + " pollIntervalSeconds=" + (pollIntervalMillis / 1000L)
                + " registerCreationEntry=" + registerCreationEntry
                + " chaosCrystalTemplateId=" + chaosCrystalTemplateId);
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int intProp(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[soulstealing] " + key + " must be an integer, got: " + v);
            return def;
        }
    }
}
