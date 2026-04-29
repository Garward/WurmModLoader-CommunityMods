package com.garward.wurmmodloader.mods.caches;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Per-server cache toggles loaded from {@code mod.properties}. Custom
 * template-id lists let other submods (combat, crystals) inject their items
 * into the appropriate cache pool without this submod having to depend on
 * them; unregistered ids are silently skipped at draw time.
 */
final class CachesConfig {

    private static final Logger logger = Logger.getLogger(CachesConfig.class.getName());

    boolean enabled = true;
    int[] artifactCacheCustomTemplateIds = new int[0];
    int[] crystalCacheTemplateIds = new int[0];

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);
        artifactCacheCustomTemplateIds = csvInts(p, "artifactCacheCustomTemplateIds");
        crystalCacheTemplateIds = csvInts(p, "crystalCacheTemplateIds");
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int[] csvInts(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.trim().isEmpty()) return new int[0];
        String[] parts = v.split(",");
        int[] out = new int[parts.length];
        int n = 0;
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try {
                out[n++] = Integer.parseInt(trimmed);
            } catch (NumberFormatException e) {
                logger.warning("[caches] " + key + ": skipping non-integer entry '" + trimmed + "'");
            }
        }
        if (n == out.length) return out;
        int[] truncated = new int[n];
        System.arraycopy(out, 0, truncated, 0, n);
        return truncated;
    }
}
