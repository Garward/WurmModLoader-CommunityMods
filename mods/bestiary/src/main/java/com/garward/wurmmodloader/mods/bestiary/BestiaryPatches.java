package com.garward.wurmmodloader.mods.bestiary;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bytecode patches that route vanilla creature behavior through
 * {@link BestiaryHooks}. Each patch is gated by the matching {@link BestiaryMod}
 * toggle. Patches mirror Sindusk's WyvernMods Bestiary preInit but limited
 * to behavior that doesn't require custom creature templates.
 *
 * <p>The mod runs with {@code sharedClassLoader=true}, so static methods on
 * {@link BestiaryHooks} are resolvable from rewritten vanilla bytecode.
 */
final class BestiaryPatches {

    private static final Logger logger = Logger.getLogger(BestiaryPatches.class.getName());
    private static final String HOOK = BestiaryHooks.class.getName();

    private BestiaryPatches() {}

    static void install(BestiaryMod cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctCreature = pool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = pool.get("com.wurmonline.server.items.Item");
            CtClass ctAction = pool.get("com.wurmonline.server.behaviours.Action");
            CtClass ctString = pool.get("java.lang.String");

            if (cfg.fixSacrificingStrongCreatures) {
                CtClass ctMethodsReligion = pool.get("com.wurmonline.server.behaviours.MethodsReligion");
                CtClass[] params = {ctCreature, ctCreature, ctItem, ctAction, CtClass.floatType};
                String desc = Descriptor.ofMethod(CtClass.booleanType, params);
                String body =
                        "if (" + HOOK + ".isSacrificeImmuneVanilla($2)) {" +
                                "  performer.getCommunicator().sendNormalServerMessage(" +
                                "    \"This creature cannot be sacrificed.\");" +
                                "  return true;" +
                                "}";
                insertBefore(ctMethodsReligion, "sacrifice", desc, body,
                        "fixSacrificingStrongCreatures: reject sacrifice on unique creatures");
            }

            if (cfg.disableAfkTraining) {
                CtClass ctCombatHandler = pool.get("com.wurmonline.server.creatures.CombatHandler");
                String replace =
                        "if (" + HOOK + ".blockSkillFrom($1, $0)) {" +
                                "  $_ = true;" +
                                "} else {" +
                                "  $_ = $proceed($$);" +
                                "}";
                String[] declared = {"setDamage", "checkDefenderParry", "checkShield", "setBonuses"};
                for (String m : declared) {
                    instrumentDeclared(ctCombatHandler, m, "isNoSkillFor", replace,
                            "disableAfkTraining: " + m);
                }
                try {
                    CtMethod[] getDamages = ctCombatHandler.getDeclaredMethods("getDamage");
                    for (CtMethod method : getDamages) {
                        method.instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("isNoSkillFor")) m.replace(replace);
                            }
                        });
                    }
                    logger.info("[bestiary] disableAfkTraining: getDamage overloads instrumented");
                } catch (CannotCompileException e) {
                    logger.log(Level.WARNING,
                            "[bestiary] disableAfkTraining getDamage instrument failed", e);
                }
            }

            if (cfg.conditionWildCreatures) {
                String descDoNew = doNewDescriptor(pool, ctCreature, ctString);
                String body = "$10 = " + HOOK + ".newCreatureType($1, $10);";
                insertBefore(ctCreature, "doNew", descDoNew, body,
                        "conditionWildCreatures: random C_MOD on wild spawn");
            }

            if (cfg.allowGhostArchery) {
                try {
                    CtClass ctArchery = pool.get("com.wurmonline.server.combat.Archery");
                    CtMethod[] attacks = ctArchery.getDeclaredMethods("attack");
                    for (CtMethod method : attacks) {
                        method.instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("isGhost")) m.replace("$_ = false;");
                            }
                        });
                    }
                    logger.info("[bestiary] allowGhostArchery: Archery.attack ghost-checks neutralized");
                } catch (CannotCompileException e) {
                    logger.log(Level.WARNING, "[bestiary] allowGhostArchery instrument failed", e);
                }
            }

            if (cfg.genesisEnchantedGrassNewborns) {
                String replace =
                        HOOK + ".checkEnchantedBreed(newCreature);" +
                                "$_ = $proceed($$);";
                instrumentDeclared(ctCreature, "checkPregnancy", "saveCreatureName", replace,
                        "genesisEnchantedGrassNewborns: auto-Genesis on enchanted grass");
            }

            if (cfg.useCustomCreatureSizes) {
                CtClass ctCreatureStatus = pool.get("com.wurmonline.server.creatures.CreatureStatus");
                String body = "{ return " + HOOK + ".getAdjustedSizeMod(this); }";
                setDeclaredBody(ctCreatureStatus, "getSizeMod", body,
                        "useCustomCreatureSizes: honour C_MOD_SIZE* in CreatureStatus.getSizeMod");
            }

            if (cfg.preventLegendaryHitching) {
                CtClass ctVehicle = pool.get("com.wurmonline.server.behaviours.Vehicle");
                String body =
                        "if (" + HOOK + ".isHitchImmuneVanilla($1)) {" +
                                "  return false;" +
                                "}";
                insertBeforeDeclared(ctVehicle, "addDragger", body,
                        "preventLegendaryHitching: reject unique creatures from vehicle hitch");
            }

            if (cfg.logCreatureSpawns) {
                String descDoNew = doNewDescriptor(pool, ctCreature, ctString);
                String body = HOOK + ".logCreatureSpawn($1, $3, $4);";
                insertBefore(ctCreature, "doNew", descDoNew, body,
                        "logCreatureSpawns: log every Creature.doNew");
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }

    private static String doNewDescriptor(ClassPool pool, CtClass ctCreature, CtClass ctString) {
        CtClass[] params = {
                CtClass.intType,
                CtClass.booleanType,
                CtClass.floatType,
                CtClass.floatType,
                CtClass.floatType,
                CtClass.intType,
                ctString,
                CtClass.byteType,
                CtClass.byteType,
                CtClass.byteType,
                CtClass.booleanType,
                CtClass.byteType,
                CtClass.intType
        };
        return Descriptor.ofMethod(ctCreature, params);
    }

    private static void insertBefore(CtClass ct, String methodName, String descriptor,
                                     String body, String reason) {
        try {
            ct.getMethod(methodName, descriptor).insertBefore(body);
            logger.info("[bestiary] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[bestiary] FAILED " + reason, e);
        }
    }

    private static void insertBeforeDeclared(CtClass ct, String methodName,
                                             String body, String reason) {
        try {
            ct.getDeclaredMethod(methodName).insertBefore(body);
            logger.info("[bestiary] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[bestiary] FAILED " + reason, e);
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
            logger.info("[bestiary] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[bestiary] FAILED " + reason, e);
        }
    }

    private static void setDeclaredBody(CtClass ct, String methodName,
                                        String body, String reason) {
        try {
            ct.getDeclaredMethod(methodName).setBody(body);
            logger.info("[bestiary] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[bestiary] FAILED " + reason, e);
        }
    }
}
