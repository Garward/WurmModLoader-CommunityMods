package com.garward.wurmmodloader.mods.bounty;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bytecode patch for the corpse-loot path. The killer-reward path uses the
 * framework's {@code CreatureDeathEvent} subscriber and needs no patch.
 *
 * <p>Mirrors the upstream {@code Bounty.init} hook: instrument the
 * {@code setRotation} call inside {@code Creature.die(boolean, String, boolean)}
 * so we can reach the local-variable {@code corpse} after it's been placed.
 */
final class BountyPatches {

    private static final Logger logger = Logger.getLogger(BountyPatches.class.getName());
    private static final String HOOK = BountyHooks.class.getName();

    private BountyPatches() {}

    static void install() {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctCreature = pool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctString   = pool.get("java.lang.String");

            CtClass[] params = {
                    CtClass.booleanType,   // killcount
                    ctString,              // reason
                    CtClass.booleanType    // delete
            };
            String descriptor = Descriptor.ofMethod(CtClass.voidType, params);

            String replace =
                    "{ $_ = $proceed($$); " +
                    "  " + HOOK + ".fireCorpseLoot(this, corpse); " +
                    "}";

            ctCreature.getMethod("die", descriptor).instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setRotation")) m.replace(replace);
                }
            });
            logger.info("[bounty] corpse-loot patch installed on Creature.die->setRotation");
        } catch (NotFoundException | CannotCompileException
                 | IllegalArgumentException | ClassCastException e) {
            logger.log(Level.SEVERE, "[bounty] failed to install corpse-loot patch", e);
            throw new HookException(e);
        }
    }
}
