package com.garward.wurmmodloader.mods.supplydepot;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One install-time patch: when a client's altars sync runs, also push the
 * depot light effects so freshly-logged-in players see active depots.
 * Calls {@link DepotEffects#sendDepotEffectsToPlayer} via FQN so the
 * vanilla Players class doesn't need a compile-time reference to us.
 */
final class DepotPatches {

    private static final Logger logger = Logger.getLogger(DepotPatches.class.getName());

    static void install() {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctPlayers = pool.get("com.wurmonline.server.Players");
            ctPlayers.getDeclaredMethod("sendAltarsToPlayer").insertBefore(
                    "com.garward.wurmmodloader.mods.supplydepot.DepotEffects.sendDepotEffectsToPlayer($1);");
            logger.info("[supplydepot] installed sendAltarsToPlayer light hook");
        } catch (NotFoundException e) {
            logger.log(Level.WARNING, "[supplydepot] could not find Players.sendAltarsToPlayer", e);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[supplydepot] failed to install patch", t);
        }
    }

    private DepotPatches() {}
}
