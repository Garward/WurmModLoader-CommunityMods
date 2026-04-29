package com.garward.wurmmodloader.mods.mission;

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
 * Bytecode patches that route epic-mission decisions through
 * {@link MissionHooks}. Each patch is gated by the matching {@link MissionMod}
 * toggle. Patches mirror Sindusk's WyvernMods MissionCreator.preInit.
 *
 * <p>The mod runs with {@code sharedClassLoader=true}, so static methods on
 * {@link MissionHooks} are resolvable from rewritten vanilla bytecode.
 */
final class MissionPatches {

    private static final Logger logger = Logger.getLogger(MissionPatches.class.getName());
    private static final String HOOK = MissionHooks.class.getName();

    private MissionPatches() {}

    static void install(MissionMod cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctTriggerEffect    = pool.get("com.wurmonline.server.tutorial.TriggerEffect");
            CtClass ctEpicServerStatus = pool.get("com.wurmonline.server.epic.EpicServerStatus");
            CtClass ctEpicMissionEnum  = pool.get("com.wurmonline.server.epic.EpicMissionEnum");

            if (cfg.addMissionCurrencyReward) {
                String replace = "$_ = $proceed($$); " + HOOK + ".awardMissionBonus($0);";
                instrumentDeclared(ctTriggerEffect, "effect", "addToSleep", replace,
                        "addMissionCurrencyReward: append iron-coin payout to TriggerEffect.effect");
            }

            if (cfg.preventMissionOceanSpawns) {
                instrumentDeclared(ctEpicServerStatus, "spawnSingleCreature", "isSwimming",
                        "$_ = false;",
                        "preventMissionOceanSpawns: deny isSwimming templates from EpicServerStatus.spawnSingleCreature");
            }

            if (cfg.additionalHerbivoreChecks) {
                String replace = "$_ = " + HOOK + ".isMissionOkayHerbivore($0);";
                instrumentDeclared(ctEpicServerStatus, "createSlayCreatureMission",     "isHerbivore", replace,
                        "additionalHerbivoreChecks: createSlayCreatureMission.isHerbivore -> filter");
                instrumentDeclared(ctEpicServerStatus, "createSlayTraitorMission",      "isHerbivore", replace,
                        "additionalHerbivoreChecks: createSlayTraitorMission.isHerbivore -> filter");
                instrumentDeclared(ctEpicServerStatus, "createSacrificeCreatureMission","isHerbivore", replace,
                        "additionalHerbivoreChecks: createSacrificeCreatureMission.isHerbivore -> filter");
            }

            if (cfg.additionalMissionSlayableChecks) {
                String replace = "$_ = " + HOOK + ".isMissionOkaySlayable($0);";
                instrumentDeclared(ctEpicServerStatus, "createSlayCreatureMission",     "isEpicMissionSlayable", replace,
                        "additionalMissionSlayableChecks: createSlayCreatureMission.isEpicMissionSlayable -> filter");
                instrumentDeclared(ctEpicServerStatus, "createSacrificeCreatureMission","isEpicMissionSlayable", replace,
                        "additionalMissionSlayableChecks: createSacrificeCreatureMission.isEpicMissionSlayable -> filter");
            }

            if (cfg.disableEpicMissionTypes) {
                String body =
                        "{ if ($0.getMissionType() == 108 || $0.getMissionType() == 120 || $0.getMissionType() == 124) {" +
                                "  return 0;" +
                                "}" +
                                "  return $0.missionChance;" +
                                "}";
                setDeclaredBody(ctEpicMissionEnum, "getMissionChance", body,
                        "disableEpicMissionTypes: getMissionChance returns 0 for types 108/120/124");
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }

    private static void instrumentDeclared(CtClass ct, String methodName, String callName,
                                           String replacement, String reason) {
        try {
            ct.getDeclaredMethod(methodName).instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) m.replace(replacement);
                }
            });
            logger.info("[mission] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[mission] FAILED " + reason, e);
        }
    }

    private static void setDeclaredBody(CtClass ct, String methodName,
                                        String body, String reason) {
        try {
            ct.getDeclaredMethod(methodName).setBody(body);
            logger.info("[mission] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[mission] FAILED " + reason, e);
        }
    }
}
