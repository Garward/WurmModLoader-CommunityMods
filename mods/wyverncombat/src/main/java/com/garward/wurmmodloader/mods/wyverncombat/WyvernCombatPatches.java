package com.garward.wurmmodloader.mods.wyverncombat;

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
 * Bytecode patches for {@link WyvernCombatMod}. All instrumentation runs
 * inside {@link #install(WyvernCombatMod)}; failures are logged and
 * non-fatal so a single broken patch can be disabled in config without
 * taking the whole mod offline.
 */
final class WyvernCombatPatches {

    private static final Logger logger = Logger.getLogger(WyvernCombatPatches.class.getName());

    private WyvernCombatPatches() {}

    static void install(WyvernCombatMod cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctCombatHandler = pool.get("com.wurmonline.server.creatures.CombatHandler");
            CtClass ctWound = pool.get("com.wurmonline.server.bodys.Wound");
            String hookFqn = WyvernCombatHooks.class.getName();

            if (cfg.combatRatingAdjustments) {
                String replace = ""
                        + "combatRating += " + hookFqn
                        + ".combatRatingAdditive(this.creature, $1);"
                        + "crmod *= " + hookFqn
                        + ".combatRatingMultiplicative(this.creature);"
                        + "$_ = $proceed($$);";
                replaceCall(ctCombatHandler, "getCombatRating", "getFlankingModifier", replace,
                        "combatRatingAdjustments: royal-exec / pet-soul-depth / vehicle-CR injection");
            }

            if (cfg.adjustCombatRatingSpellPower) {
                replaceCall(ctCombatHandler, "getCombatRating", "getBonusForSpellEffect",
                        "$_ = $proceed($$) * 0.5f;",
                        "adjustCombatRatingSpellPower: halve TrueHit/Excel CR contribution");
            }

            if (cfg.disableLegendaryRegeneration) {
                String guard = "if(!this.creature.isUnique()){ $_ = $proceed($$); }";
                replaceCall(ctWound, "poll", "modifySeverity", guard,
                        "disableLegendaryRegeneration: skip Wound.poll modifySeverity for uniques");
                replaceCall(ctWound, "poll", "checkInfection", guard,
                        "disableLegendaryRegeneration: skip Wound.poll checkInfection for uniques");
                replaceCall(ctWound, "poll", "checkPoison", guard,
                        "disableLegendaryRegeneration: skip Wound.poll checkPoison for uniques");
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }

    private static void replaceCall(CtClass ct, String methodName, String callName,
                                    String replacement, String reason) {
        try {
            ct.getDeclaredMethod(methodName).instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) {
                        m.replace(replacement);
                    }
                }
            });
            logger.info("[wyverncombat] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[wyverncombat] FAILED " + reason, e);
        }
    }
}
