package com.garward.wurmmodloader.mods.meditation;

import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Player;
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
 * Bytecode patches for the meditation submod. Patches are gated by toggles on
 * the {@link MeditationMod} instance; static helpers below are referenced by
 * FQN from rewritten bytecode (the mod runs with sharedClassLoader=true so
 * they sit on the same loader as the patched vanilla classes).
 */
public final class MeditationPatches {

    private static final Logger logger = Logger.getLogger(MeditationPatches.class.getName());

    private static final String FQN = MeditationPatches.class.getName();

    private MeditationPatches() {}

    static void install(MeditationMod cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();

            CtClass ctCultist = pool.get("com.wurmonline.server.players.Cultist");
            CtClass ctCults = pool.get("com.wurmonline.server.players.Cults");
            CtClass ctActions = pool.get("com.wurmonline.server.behaviours.Actions");
            CtClass ctMovementScheme = pool.get("com.wurmonline.server.creatures.MovementScheme");
            CtClass ctCreatureStatus = pool.get("com.wurmonline.server.creatures.CreatureStatus");
            CtClass ctSkill = pool.get("com.wurmonline.server.skills.Skill");

            if (cfg.simplifyMeditationTerrain) {
                setBody(ctCults, "getPathFor",
                        "{ return " + FQN + ".getNewPathFor($1, $2, $3); }");
            }

            if (cfg.removeInsanitySotG) {
                setBody(ctCultist, "getHalfDamagePercentage", "{ return 0.0f; }");
            }

            if (cfg.removeHateWarBonus) {
                setBody(ctCultist, "mayStartDoubleWarDamage", "{ return false; }");
                setBody(ctCultist, "doubleWarDamage", "{ return false; }");
            }

            if (cfg.insanitySpeedBonus) {
                setBody(ctActions, "getStaminaModiferFor",
                        "{ return " + FQN + ".newStaminaModifierFor($1, $2); }");
            }

            if (cfg.hateMovementBonus) {
                insertAfter(ctMovementScheme, "getSpeedModifier",
                        "if ($_ > 0) { $_ = $_ * " + FQN + ".getCultistSpeedMultiplier(this); }");
            }

            if (cfg.scalingPowerStaminaBonus) {
                instrument(ctCreatureStatus, "modifyStamina", "usesNoStamina",
                        "staminaMod += " + FQN + ".getPowerStaminaBonus(this.statusHolder); $_ = false;");
            }

            if (cfg.scalingKnowledgeSkillGain) {
                CtClass[] params1 = {
                        CtClass.doubleType, CtClass.booleanType, CtClass.floatType,
                        CtClass.booleanType, CtClass.doubleType
                };
                String desc1 = Descriptor.ofMethod(CtClass.voidType, params1);
                instrumentDesc(ctSkill, "alterSkill", desc1, "levelElevenSkillgain",
                        "staminaMod *= " + FQN + ".getKnowledgeSkillGain(player); $_ = false;");
            }

            if (cfg.removeMeditationTickTimer) {
                instrument(ctCults, "meditate", "getLastMeditated", "$_ = 0;");
            }

            if (cfg.newMeditationBuffs) {
                insertBefore(ctCultist, "sendPassiveBuffs", FQN + ".sendPassiveBuffs($0);");
            }

            if (cfg.enableMeditationAbilityCooldowns) {
                // Path of Love
                setBody(ctCultist, "mayRefresh",
                        "return this.path == 1 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > "
                                + cfg.loveRefreshCooldown + "L;");
                setBody(ctCultist, "mayEnchantNature",
                        "return this.path == 1 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "
                                + cfg.loveEnchantNatureCooldown + "L;");
                setBody(ctCultist, "mayStartLoveEffect",
                        "return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > "
                                + cfg.loveLoveEffectCooldown + "L;");

                // Path of Hate (overwrites removeHateWarBonus's mayStartDoubleWarDamage if both on)
                setBody(ctCultist, "mayStartDoubleWarDamage",
                        "return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 > "
                                + cfg.hateWarDamageCooldown + "L;");
                setBody(ctCultist, "mayStartDoubleStructDamage",
                        "return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 > "
                                + cfg.hateStructureDamageCooldown + "L;");
                setBody(ctCultist, "mayStartFearEffect",
                        "return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > "
                                + cfg.hateFearCooldown + "L;");

                // Path of Power
                setBody(ctCultist, "mayStartNoElementalDamage",
                        "return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 > "
                                + cfg.powerElementalImmunityCooldown + "L;");
                setBody(ctCultist, "maySpawnVolcano",
                        "return this.path == 5 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "
                                + cfg.powerEruptFreezeCooldown + "L;");
                setBody(ctCultist, "mayStartIgnoreTraps",
                        "return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 > "
                                + cfg.powerIgnoreTrapsCooldown + "L;");

                // Path of Knowledge
                setBody(ctCultist, "mayCreatureInfo",
                        "return this.path == 3 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > "
                                + cfg.knowledgeInfoCreatureCooldown + "L;");
                setBody(ctCultist, "mayInfoLocal",
                        "return this.path == 3 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > "
                                + cfg.knowledgeInfoTileCooldown + "L;");
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }

    // ---------- Javassist helpers (drop-in replacement for sin.lib.Util) ----------

    private static void setBody(CtClass ct, String method, String body) throws NotFoundException {
        try {
            ct.getDeclaredMethod(method).setBody(body);
        } catch (CannotCompileException e) {
            logger.log(Level.SEVERE, "[meditation] setBody " + ct.getName() + "#" + method, e);
        }
    }

    private static void insertBefore(CtClass ct, String method, String src) throws NotFoundException {
        try {
            ct.getDeclaredMethod(method).insertBefore(src);
        } catch (CannotCompileException e) {
            logger.log(Level.SEVERE, "[meditation] insertBefore " + ct.getName() + "#" + method, e);
        }
    }

    private static void insertAfter(CtClass ct, String method, String src) throws NotFoundException {
        try {
            ct.getDeclaredMethod(method).insertAfter(src);
        } catch (CannotCompileException e) {
            logger.log(Level.SEVERE, "[meditation] insertAfter " + ct.getName() + "#" + method, e);
        }
    }

    private static void instrument(CtClass ct, String method, String callName, String replace) throws NotFoundException {
        try {
            ct.getDeclaredMethod(method).instrument(new CallReplacer(callName, replace));
        } catch (CannotCompileException e) {
            logger.log(Level.SEVERE, "[meditation] instrument " + ct.getName() + "#" + method
                    + " call=" + callName, e);
        }
    }

    private static void instrumentDesc(CtClass ct, String method, String descriptor,
                                       String callName, String replace) throws NotFoundException {
        try {
            ct.getMethod(method, descriptor).instrument(new CallReplacer(callName, replace));
        } catch (CannotCompileException e) {
            logger.log(Level.SEVERE, "[meditation] instrumentDesc " + ct.getName() + "#" + method
                    + descriptor + " call=" + callName, e);
        }
    }

    private static final class CallReplacer extends ExprEditor {
        private final String callName;
        private final String replace;
        CallReplacer(String callName, String replace) {
            this.callName = callName;
            this.replace = replace;
        }
        @Override
        public void edit(MethodCall mc) throws CannotCompileException {
            if (mc.getMethodName().equals(callName)) {
                mc.replace(replace);
            }
        }
    }

    // ---------- Static callbacks invoked from rewritten bytecode ----------

    public static void sendPassiveBuffs(Cultist cultist) {
        byte path = cultist.getPath();
        byte level = cultist.getLevel();
        try {
            Creature cret = Server.getInstance().getCreature(cultist.getWurmId());
            if (path == Cults.PATH_HATE && level >= 4) {
                float levelDiff = level - 3f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_MOVEBONUS, -1, levelDiff);
            }
            if (path == Cults.PATH_INSANITY && level >= 7) {
                float levelDiff = level - 6f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.INSANITY_SHIELD_GONE, -1, levelDiff * 2f);
            }
            if (path == Cults.PATH_KNOWLEDGE && level >= 7) {
                float levelDiff = level - 6f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.KNOWLEDGE_INCREASED_SKILL_GAIN, -1, levelDiff * 5f);
            }
            if (path == Cults.PATH_POWER && level >= 7) {
                float levelDiff = level - 6f;
                cret.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_USES_LESS_STAMINA, -1, levelDiff * 5f);
            }
        } catch (NoSuchPlayerException | NoSuchCreatureException e) {
            logger.log(Level.WARNING, "[meditation] sendPassiveBuffs", e);
        }
    }

    public static float getCultistSpeedMultiplier(MovementScheme scheme) {
        try {
            Creature cret = ReflectionUtil.getPrivateField(scheme,
                    ReflectionUtil.getField(scheme.getClass(), "creature"));
            if (cret != null && cret.getCultist() != null) {
                Cultist path = cret.getCultist();
                if (path.getPath() == Cults.PATH_HATE) {
                    byte level = path.getLevel();
                    if (level >= 3) {
                        float levelDiff = level - 2f;
                        return 1.0f + (levelDiff * 0.01f);
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[meditation] getCultistSpeedMultiplier", e);
        }
        return 1.0f;
    }

    public static float newStaminaModifierFor(Creature performer, int staminaNeeded) {
        int currstam = performer.getStatus().getStamina();
        float staminaMod = 1.0f;
        if (currstam > 60000) {
            staminaMod = 0.8f;
        }
        if (performer.getCultist() != null) {
            Cultist path = performer.getCultist();
            if (path.getPath() == Cults.PATH_INSANITY) {
                byte level = path.getLevel();
                if (level >= 7) {
                    float levelDiff = level - 6f;
                    float insanityMod = 1.0f - (levelDiff * 0.02f);
                    staminaMod *= insanityMod;
                }
            }
        }
        staminaMod += 1.0f - (float) currstam / 65535.0f;
        if (currstam < staminaNeeded) {
            float diff = staminaNeeded - currstam;
            staminaMod += diff / (float) staminaNeeded * performer.getStaminaMod();
        }
        return staminaMod;
    }

    public static float getPowerStaminaBonus(Creature creature) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            if (player.getCultist() != null) {
                Cultist path = player.getCultist();
                if (path.getPath() == Cults.PATH_POWER) {
                    byte level = path.getLevel();
                    if (level >= 7) {
                        float levelDiff = level - 6f;
                        return (levelDiff * 0.05f);
                    }
                }
            }
        }
        return 0f;
    }

    public static float getKnowledgeSkillGain(Player player) {
        if (player.getCultist() != null) {
            Cultist path = player.getCultist();
            if (path.getPath() == Cults.PATH_KNOWLEDGE) {
                byte level = path.getLevel();
                if (level >= 7) {
                    float levelDiff = level - 6f;
                    return 1.0f + (levelDiff * 0.05f);
                }
            }
        }
        return 1.0f;
    }

    public static byte getNewPathFor(int tilex, int tiley, int layer) {
        if (layer < 0) {
            return Cults.PATH_INSANITY;
        }
        int tile = Server.surfaceMesh.getTile(tilex, tiley);
        byte type = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        if (theTile.isGrass() || theTile.isBush() || theTile.isTree()) {
            return Cults.PATH_LOVE;
        }
        if (theTile.isMycelium() || theTile.isMyceliumBush() || theTile.isMyceliumTree()) {
            return Cults.PATH_HATE;
        }
        if (type == Tiles.TILE_TYPE_SAND) {
            return Cults.PATH_KNOWLEDGE;
        }
        if (type == Tiles.TILE_TYPE_ROCK || type == Tiles.TILE_TYPE_CLIFF) {
            return Cults.PATH_POWER;
        }
        return Cults.PATH_NONE;
    }
}
