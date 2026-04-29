package com.garward.wurmmodloader.mods.bestiary;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.BodyPartConstants;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.Enchants;
import com.garward.wurmmodloader.modloader.ReflectionUtil;

import java.util.logging.Logger;

/**
 * Static methods invoked from bytecode patched by {@link BestiaryPatches}.
 * Method signatures and return types are part of the patch contract — do
 * not change without updating the corresponding $proceed/replace strings.
 */
public final class BestiaryHooks {

    private static final Logger logger = Logger.getLogger(BestiaryHooks.class.getName());

    private BestiaryHooks() {}

    // ----- disableAfkTraining -----

    /**
     * Returns true if the defender shouldn't gain skill from this hit.
     * Originally Sindusk's Bestiary.blockSkillFrom — a player who isn't
     * actively targeting their attacker, or whose weapon can't deal real
     * damage, gets no skill tick.
     */
    public static boolean blockSkillFrom(Creature defender, Creature attacker) {
        if (defender == null || attacker == null) return false;
        if (defender.isPlayer() && defender.getTarget() != attacker) return true;
        if (defender.isPlayer()) {
            Item weap = defender.getPrimWeapon();
            if (weap != null && weap.isWeapon()) {
                try {
                    double dam = Weapon.getModifiedDamageForWeapon(
                            weap, defender.getSkills().getSkill(SkillList.BODY_STRENGTH), true) * 1000.0;
                    dam += Server.getBuffedQualityEffect(weap.getCurrentQualityLevel() / 100.0f)
                            * (double) Weapon.getBaseDamageForWeapon(weap) * 2400.0;
                    if (attacker.getArmourMod() < 0.1f) return false;
                    if (dam * attacker.getArmourMod() < 3000) return true;
                } catch (NoSuchSkillException e) {
                    logger.warning("[bestiary] blockSkillFrom: missing body strength: " + e.getMessage());
                }
            } else {
                if (defender.getBonusForSpellEffect(Enchants.CRET_BEARPAW) < 50f) return true;
            }
        }
        try {
            if (defender.isPlayer() && attacker.getArmour(BodyPartConstants.TORSO) != null) {
                return true;
            }
        } catch (NoArmourException | NoSpaceException ignored) {
        }
        return false;
    }

    // ----- fixSacrificingStrongCreatures / preventLegendaryHitching -----

    public static boolean isSacrificeImmuneVanilla(Creature creature) {
        return creature != null && creature.isUnique();
    }

    public static boolean isHitchImmuneVanilla(Creature creature) {
        return creature != null && creature.isUnique();
    }

    // ----- conditionWildCreatures -----

    public static byte newCreatureType(int templateId, byte ctype) {
        try {
            CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (template == null) return ctype;
            if (ctype == 0
                    && (template.isAggHuman() || template.getBaseCombatRating() > 10)
                    && !template.isUnique()) {
                if (Server.rand.nextInt(5) == 0) {
                    ctype = (byte) (Server.rand.nextInt(11) + 1);
                    if (Server.rand.nextInt(50) == 0) {
                        ctype = CreatureTypes.C_MOD_CHAMPION;
                    }
                }
            }
        } catch (NoSuchCreatureTemplateException ignored) {
        }
        return ctype;
    }

    // ----- useCustomCreatureSizes -----

    /**
     * Drop-in for {@code CreatureStatus.getSizeMod()}. Honors
     * C_MOD_SIZESMALL / C_MOD_SIZEMINI / C_MOD_SIZETINY in addition to
     * the existing combat modifiers.
     */
    public static float getAdjustedSizeMod(CreatureStatus status) {
        try {
            Creature statusHolder = ReflectionUtil.getPrivateField(
                    status, ReflectionUtil.getField(status.getClass(), "statusHolder"));
            float aiDataModifier = 1.0f;
            if (statusHolder.getCreatureAIData() != null) {
                aiDataModifier = statusHolder.getCreatureAIData().getSizeModifier();
            }
            byte modtype = ReflectionUtil.getPrivateField(
                    status, ReflectionUtil.getField(status.getClass(), "modtype"));
            float ageSizeModifier = ReflectionUtil.callPrivateMethod(
                    status, ReflectionUtil.getMethod(status.getClass(), "getAgeSizeModifier"));
            float floatToRet = 1.0f;
            if (modtype != 0) {
                float change = 0.0f;
                switch (modtype) {
                    case CreatureTypes.C_MOD_RAGING:    change = 0.4f; break;
                    case CreatureTypes.C_MOD_SLOW:      change = 0.7f; break;
                    case CreatureTypes.C_MOD_GREENISH:  change = 1.0f; break;
                    case CreatureTypes.C_MOD_LURKING:   change = -0.2f; break;
                    case CreatureTypes.C_MOD_SLY:       change = -0.1f; break;
                    case CreatureTypes.C_MOD_HARDENED:  change = 0.5f; break;
                    case CreatureTypes.C_MOD_SCARED:    change = 0.3f; break;
                    case CreatureTypes.C_MOD_CHAMPION:  change = 2.0f; break;
                    case CreatureTypes.C_MOD_SIZESMALL: change = -0.5f; break;
                    case CreatureTypes.C_MOD_SIZEMINI:  change = -0.75f; break;
                    case CreatureTypes.C_MOD_SIZETINY:  change = -0.875f; break;
                    default: change = 0.0f;
                }
                floatToRet += change;
            }
            return floatToRet * aiDataModifier * ageSizeModifier;
        } catch (Exception e) {
            logger.warning("[bestiary] getAdjustedSizeMod fallback: " + e.getMessage());
            return 1.0f;
        }
    }

    // ----- genesisEnchantedGrassNewborns -----

    public static void checkEnchantedBreed(Creature creature) {
        int tile = Server.surfaceMesh.getTile(creature.getTileX(), creature.getTileY());
        byte type = Tiles.decodeType(tile);
        if (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id) {
            logger.info("[bestiary] " + creature.getName()
                    + " was born on enchanted grass; removing a negative trait.");
            Server.getInstance().broadCastAction(
                    creature.getName() + " was born on enchanted grass, and feels more healthy!",
                    creature, 10);
            creature.removeRandomNegativeTrait();
        }
    }

    // ----- logCreatureSpawns -----

    public static void logCreatureSpawn(int templateId, float aPosX, float aPosY) {
        try {
            CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            String name = template != null ? template.getName() : "unknown";
            logger.info(String.format("[bestiary] new creature: %d - %.0f, %.0f [%s]",
                    templateId, aPosX / 4f, aPosY / 4f, name));
        } catch (NoSuchCreatureTemplateException ignored) {
        }
    }
}
