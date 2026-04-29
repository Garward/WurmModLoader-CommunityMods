package com.garward.wurmmodloader.mods.gemaugmentation;

import com.wurmonline.server.items.Item;
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
 * Bytecode patches that route every successful imp/polish/temper action
 * through {@link #setGemmedQuality(Item, double, float, float)} so the
 * material's impalement bonus multiplies the QL gain. Also lifts the
 * 9999.9 ceiling in {@code DbItem.setQualityLevel} so items can scale
 * past vanilla's effective cap.
 *
 * <p>Direct port of {@code mod.sin.wyvern.GemAugmentation}.
 */
final class GemAugmentationPatches {

    private static final Logger logger = Logger.getLogger(GemAugmentationPatches.class.getName());

    private GemAugmentationPatches() {}

    public static boolean setGemmedQuality(Item target, double power, float maxGain, float modifier) {
        float current = target.getQualityLevel();
        float next = (float) Math.min(9999.9f,
                current + power * (double) maxGain * (double) (modifier * target.getMaterialImpBonus()));
        return target.setQualityLevel(next);
    }

    static void install() {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctMethodsReligion = pool.get("com.wurmonline.server.behaviours.MethodsReligion");
            CtClass ctMethodsItems = pool.get("com.wurmonline.server.behaviours.MethodsItems");
            CtClass ctDbItem = pool.get("com.wurmonline.server.items.DbItem");

            String hookFqn = GemAugmentationPatches.class.getName();

            // 1. Disable Gem Augmentation skill from converting (force noChange=true).
            replaceCall(ctMethodsReligion, "listen", "skillCheck",
                    "$_ = $proceed($1, $2, true, $4);",
                    "disable Gem Augmentation skill conversion");

            // 2. Primary Gem Augmentation hook: route every imp/polish/temper through
            //    setGemmedQuality so material impBonus multiplies the QL gain.
            String setQlReplace = "$_ = " + hookFqn
                    + ".setGemmedQuality($0, power, maxGain, modifier);";
            replaceCall(ctMethodsItems, "improveItem", "setQualityLevel", setQlReplace,
                    "improveItem: route QL gain through setGemmedQuality");
            replaceCall(ctMethodsItems, "polishItem", "setQualityLevel", setQlReplace,
                    "polishItem: route QL gain through setGemmedQuality");
            replaceCall(ctMethodsItems, "temper", "setQualityLevel", setQlReplace,
                    "temper: route QL gain through setGemmedQuality");

            // 3. Prevent the action power from being diluted (force raw float).
            String powerReplace = "$_ = $proceed((float)power);";
            replaceCall(ctMethodsItems, "improveItem", "setPower", powerReplace,
                    "improveItem: keep raw action power");
            replaceCall(ctMethodsItems, "polishItem", "setPower", powerReplace,
                    "polishItem: keep raw action power");
            replaceCall(ctMethodsItems, "temper", "setPower", powerReplace,
                    "temper: keep raw action power");

            // 4. Lift DbItem.setQualityLevel ceiling to 9999.9 so scaled items
            //    can persist past vanilla's effective cap.
            replaceCall(ctDbItem, "setQualityLevel", "min",
                    "$_ = $proceed(9999.9f, $2);",
                    "DbItem.setQualityLevel: raise ceiling to 9999.9");

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
            logger.info("[gemaugmentation] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[gemaugmentation] FAILED " + reason, e);
        }
    }
}
