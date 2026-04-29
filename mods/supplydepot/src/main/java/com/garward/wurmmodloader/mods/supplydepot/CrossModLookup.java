package com.garward.wurmmodloader.mods.supplydepot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optional cross-submod template-id resolver. The capture reward grab-bag
 * pulls from the {@code caches} submod's {@code CacheTemplates} static
 * ids if present (loaded earlier alphabetically). Reflective so we don't
 * need a compile-time dependency on caches and degrade cleanly to "no
 * grab-bag" when caches isn't installed.
 */
final class CrossModLookup {

    private static final Logger logger = Logger.getLogger(CrossModLookup.class.getName());

    private static final String CACHE_TEMPLATES_FQN =
            "com.garward.wurmmodloader.mods.caches.CacheTemplates";

    /**
     * Field names + their multiplicity in the upstream loot weighting
     * (crystal/dragon/moon are weighted higher by appearing multiple times).
     */
    private static final String[][] CACHE_FIELDS = {
            { "armourId", "1" },
            { "artifactId", "1" },
            { "crystalId", "3" },
            { "dragonId", "2" },
            { "gemId", "1" },
            { "moonId", "2" },
            { "riftId", "1" },
            { "treasureMapId", "1" }
    };

    private static int[] resolved;

    static int[] activeCacheTemplates() {
        if (resolved != null) return resolved;
        resolved = resolve();
        return resolved;
    }

    private static int[] resolve() {
        Class<?> clazz;
        try {
            clazz = Class.forName(CACHE_TEMPLATES_FQN);
        } catch (ClassNotFoundException e) {
            logger.info("[supplydepot] caches submod not detected; cache grab-bag disabled");
            return new int[0];
        }
        List<Integer> ids = new ArrayList<>();
        for (String[] entry : CACHE_FIELDS) {
            String fieldName = entry[0];
            int weight = Integer.parseInt(entry[1]);
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                int id = f.getInt(null);
                if (id <= 0) continue;
                for (int i = 0; i < weight; i++) ids.add(id);
            } catch (NoSuchFieldException e) {
                // older caches version without this field; skip silently
            } catch (Throwable t) {
                logger.log(Level.WARNING,
                        "[supplydepot] could not read CacheTemplates." + fieldName, t);
            }
        }
        int[] out = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) out[i] = ids.get(i);
        if (out.length > 0) {
            logger.info("[supplydepot] cache grab-bag resolved "
                    + out.length + " weighted entries");
        } else {
            logger.info("[supplydepot] caches submod present but no template ids resolved");
        }
        return out;
    }

    private CrossModLookup() {}
}
