package com.garward.wurmmodloader.mods.keyevent;

import java.util.Properties;
import java.util.logging.Logger;

final class KeyEventConfig {

    private static final Logger logger = Logger.getLogger(KeyEventConfig.class.getName());

    boolean enabled = true;
    int fragmentsRequired = 50;

    void load(Properties p) {
        enabled = Boolean.parseBoolean(p.getProperty("enabled", String.valueOf(enabled)));
        try {
            fragmentsRequired = Integer.parseInt(
                    p.getProperty("fragmentsRequired", String.valueOf(fragmentsRequired)));
        } catch (NumberFormatException e) {
            logger.warning("[keyevent] invalid fragmentsRequired, using default " + fragmentsRequired);
        }
        if (fragmentsRequired < 1) fragmentsRequired = 1;
    }
}
