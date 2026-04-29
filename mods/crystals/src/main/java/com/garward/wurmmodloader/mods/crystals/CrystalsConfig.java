package com.garward.wurmmodloader.mods.crystals;

import java.util.Properties;
import java.util.logging.Logger;

final class CrystalsConfig {

    private static final Logger logger = Logger.getLogger(CrystalsConfig.class.getName());

    boolean enabled = true;

    void load(Properties p) {
        String v = p.getProperty("enabled");
        if (v != null) enabled = Boolean.parseBoolean(v.trim());
        logger.info("[crystals] config loaded — enabled=" + enabled);
    }
}
