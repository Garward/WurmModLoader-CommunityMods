package com.garward.wurmmodloader.mods.titan;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bytecode patches that suppress natural healing, infection progression, and
 * poison ticks on titans. Mirrors Sindusk's WyvernMods Titans preInit.
 */
final class TitanPatches {

    private static final Logger logger = Logger.getLogger(TitanPatches.class.getName());

    private static final String GUARD =
            "if (!com.garward.wurmmodloader.mods.titan.TitanState.isTitan(this.creature)) {"
                    + "  $_ = $proceed($$);"
                    + "}";

    static void install() {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctWound = pool.get("com.wurmonline.server.bodys.Wound");
            instrumentInPoll(ctWound, "modifySeverity");
            instrumentInPoll(ctWound, "checkInfection");
            instrumentInPoll(ctWound, "checkPoison");
        } catch (NotFoundException e) {
            throw new HookException(e);
        }
    }

    private static void instrumentInPoll(CtClass ctWound, final String callName) {
        try {
            ctWound.getDeclaredMethod("poll").instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) m.replace(GUARD);
                }
            });
            logger.info("[titan] suppressed Wound.poll/" + callName + " for titans");
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[titan] FAILED to patch Wound.poll/" + callName, e);
        }
    }

    private TitanPatches() {}
}
