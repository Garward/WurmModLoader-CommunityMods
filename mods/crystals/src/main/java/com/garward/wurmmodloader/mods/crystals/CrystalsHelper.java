package com.garward.wurmmodloader.mods.crystals;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import java.util.logging.Level;
import java.util.logging.Logger;

final class CrystalsHelper {

    private static final Logger logger = Logger.getLogger(CrystalsHelper.class.getName());

    private static final byte[] ENCHANTS = {
            Enchants.BUFF_BLESSINGDARK,
            Enchants.BUFF_BLOODTHIRST,
            Enchants.BUFF_CIRCLE_CUNNING,
            Enchants.BUFF_COURIER,
            Enchants.BUFF_FLAMING_AURA,
            Enchants.BUFF_FROSTBRAND,
            Enchants.BUFF_LIFETRANSFER,
            Enchants.BUFF_MINDSTEALER,
            Enchants.BUFF_NIMBLENESS,
            Enchants.BUFF_OPULENCE,
            Enchants.BUFF_ROTTING_TOUCH,
            Enchants.BUFF_SHARED_PAIN,
            Enchants.BUFF_VENOM,
            Enchants.BUFF_WEBARMOUR,
            Enchants.BUFF_WIND_OF_AGES,
            110, // Harden
            114  // Efficiency
    };

    private CrystalsHelper() {}

    static byte getNewRandomEnchant(Item target) {
        for (int i = 0; i < 10; i++) {
            byte ench = ENCHANTS[Server.rand.nextInt(ENCHANTS.length)];
            if (target.getBonusForSpellEffect(ench) == 0f) {
                return ench;
            }
        }
        return -10;
    }

    static double getInfusionDifficulty(Creature performer, Item source, Item target) {
        double diff = 80 - source.getCurrentQualityLevel();
        diff += source.getRarity() * 25;
        diff += 30f - (target.getCurrentQualityLevel() * 0.3f);
        try {
            diff -= performer.getSkills().getSkill(SkillList.MIND).getKnowledge() * 0.3f;
        } catch (NoSuchSkillException e) {
            logger.log(Level.FINE, "[crystals] no MIND skill on " + performer.getName(), e);
        }
        return diff;
    }

    static double getEnchantersInfusionDifficulty(Creature performer, Item source, Item target) {
        double diff = 120 - source.getCurrentQualityLevel();
        diff += 40f - (target.getCurrentQualityLevel() * 0.4f);
        try {
            diff -= performer.getSkills().getSkill(SkillList.MIND).getKnowledge() * 0.3f;
        } catch (NoSuchSkillException e) {
            logger.log(Level.FINE, "[crystals] no MIND skill on " + performer.getName(), e);
        }
        if (target.getSpellEffects() != null) {
            for (SpellEffect eff : target.getSpellEffects().getEffects()) {
                if (eff.type == Enchants.BUFF_BLESSINGDARK) {
                    diff += eff.getPower() * 0.1f;
                }
                if (eff.type != Enchants.BUFF_BLOODTHIRST) {
                    diff += eff.getPower() * 0.1f;
                } else {
                    diff += eff.getPower() * 0.001f;
                }
            }
        }
        return diff;
    }

    static boolean shouldCancelEnchantersInfusion(Creature performer, Item target) {
        if (target.getOwnerId() != performer.getWurmId() && target.getLastOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You must own the item you wish to infuse.");
            return true;
        }
        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null || effs.getEffects().length == 0) {
            performer.getCommunicator().sendNormalServerMessage("The item must be enchanted to be infused.");
            return true;
        }
        return false;
    }

    static boolean shouldCancelInfusion(Creature performer, Item source, Item target) {
        if (target.getOwnerId() != performer.getWurmId() && target.getLastOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You must own the item you wish to infuse.");
            return true;
        }
        if (source.getRarity() > target.getRarity() + 1) {
            performer.getCommunicator().sendNormalServerMessage(
                    "The " + source.getName() + " is too powerful, and would outright destroy the " + target.getName() + ".");
            return true;
        } else if (source.getRarity() < target.getRarity() + 1) {
            performer.getCommunicator().sendNormalServerMessage(
                    "The " + source.getName() + " is not powerful enough to have an effect on the " + target.getName()
                            + ". You will need to combine with other crystals first.");
            return true;
        }
        return false;
    }

    static boolean shouldCancelCombine(Creature performer, Item source, Item target) {
        if (source.getWurmId() == target.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You can't combine a crystal with itself, silly!");
            return true;
        }
        if (!isCrystal(source) || !isCrystal(target)) {
            performer.getCommunicator().sendNormalServerMessage("Both objects must be Crystals to combine.");
            return true;
        }
        if (source.getTemplateId() != target.getTemplateId()) {
            performer.getCommunicator().sendNormalServerMessage("Both crystals must be of the same type to combine.");
            return true;
        }
        try {
            if (source.getOwner() != performer.getWurmId() || target.getOwner() != performer.getWurmId()) {
                performer.getCommunicator().sendNormalServerMessage("You must hold both crystals in your hands to combine them.");
                return true;
            }
        } catch (NotOwnedException e) {
            logger.log(Level.FINE, "[crystals] crystal not owned during combine check", e);
        }
        if (source.getRarity() < target.getRarity()) {
            performer.getCommunicator().sendNormalServerMessage("That crystal is too potent for this combination.");
            return true;
        } else if (source.getRarity() > target.getRarity()) {
            performer.getCommunicator().sendNormalServerMessage("That crystal is not potent enough for this combination.");
            return true;
        } else if (source.getRarity() >= 3 && target.getRarity() >= 3
                && source.getCurrentQualityLevel() + target.getCurrentQualityLevel() >= 100) {
            performer.getCommunicator().sendNormalServerMessage("Those crystals would be far too powerful if combined.");
            return true;
        }
        return false;
    }

    static boolean isCrystal(Item item) {
        int id = item.getTemplateId();
        return (CrystalsTemplates.chaosCrystalId > 0 && id == CrystalsTemplates.chaosCrystalId)
                || (CrystalsTemplates.enchantersCrystalId > 0 && id == CrystalsTemplates.enchantersCrystalId);
    }
}
