package com.garward.wurmmodloader.mods.arena;

import com.wurmonline.server.creatures.Creature;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reflective bridges to the {@code titan} and {@code rarespawn} submods.
 *
 * Mod load order is alphabetical, so {@code arena} loads <em>before</em>
 * {@code rarespawn} and {@code titan}; baking direct FQN references into
 * Javassist source would fail when the patches compile because those
 * classes are not yet on the framework classloader. These helpers cache
 * the {@code isTitan(Creature)} / {@code isRareCreature(Creature)} method
 * handles on first use and silently fall through to {@code false} if the
 * upstream submod is absent.
 */
public final class ArenaCrossMod {

    private static final Logger logger = Logger.getLogger(ArenaCrossMod.class.getName());

    private static volatile boolean titanResolved;
    private static volatile Method titanIsTitan;

    private static volatile boolean rareResolved;
    private static volatile Method rareIsRare;

    public static boolean isTitan(Creature c) {
        if (c == null) return false;
        Method m = resolveTitan();
        if (m == null) return false;
        try {
            return Boolean.TRUE.equals(m.invoke(null, c));
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isRareCreature(Creature c) {
        if (c == null) return false;
        Method m = resolveRare();
        if (m == null) return false;
        try {
            return Boolean.TRUE.equals(m.invoke(null, c));
        } catch (Throwable t) {
            return false;
        }
    }

    private static Method resolveTitan() {
        if (titanResolved) return titanIsTitan;
        synchronized (ArenaCrossMod.class) {
            if (titanResolved) return titanIsTitan;
            titanIsTitan = lookup("com.garward.wurmmodloader.mods.titan.TitanState", "isTitan");
            titanResolved = true;
            if (titanIsTitan == null) {
                logger.info("[arena] titan submod not present; isTitan() bridge disabled");
            }
            return titanIsTitan;
        }
    }

    private static Method resolveRare() {
        if (rareResolved) return rareIsRare;
        synchronized (ArenaCrossMod.class) {
            if (rareResolved) return rareIsRare;
            rareIsRare = lookup("com.garward.wurmmodloader.mods.rarespawn.RareSpawnState", "isRareCreature");
            rareResolved = true;
            if (rareIsRare == null) {
                logger.info("[arena] rarespawn submod not present; isRareCreature() bridge disabled");
            }
            return rareIsRare;
        }
    }

    private static Method lookup(String className, String methodName) {
        try {
            Class<?> cls = Class.forName(className);
            Method m = cls.getDeclaredMethod(methodName, Creature.class);
            m.setAccessible(true);
            return m;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e) {
            logger.log(Level.WARNING, "[arena] " + className + "." + methodName + "(Creature) missing", e);
            return null;
        }
    }

    private ArenaCrossMod() {}
}
