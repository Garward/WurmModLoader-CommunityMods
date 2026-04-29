package com.garward.wurmmodloader.mods.bestiary;

import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One-shot vanilla creature template adjustments. Run once at server-start
 * after templates are loaded. Mirrors Sindusk's Bestiary.setTemplateVariables
 * but limited to vanilla template IDs — custom-creature submods (Titan,
 * RareSpawn, Wyverns, etc.) own their own template setup.
 */
final class BestiaryTemplates {

    private static final Logger logger = Logger.getLogger(BestiaryTemplates.class.getName());

    private BestiaryTemplates() {}

    static void applyAll() {
        // Vanilla uniques: no rebirth, no regen.
        for (int id : new int[]{
                CreatureTemplate.DRAGON_BLACK_CID,
                CreatureTemplate.DRAGON_BLUE_CID,
                CreatureTemplate.DRAGON_GREEN_CID,
                CreatureTemplate.DRAGON_RED_CID,
                CreatureTemplate.DRAGON_WHITE_CID,
                CreatureTemplate.DRAKE_BLACK_CID,
                CreatureTemplate.DRAKE_BLUE_CID,
                CreatureTemplate.DRAKE_GREEN_CID,
                CreatureTemplate.DRAKE_RED_CID,
                CreatureTemplate.DRAKE_WHITE_CID,
                CreatureTemplate.GOBLIN_LEADER_CID,
                CreatureTemplate.FOREST_GIANT_CID,
                CreatureTemplate.TROLL_KING_CID,
                CreatureTemplate.CYCLOPS_CID
        }) setUniqueTypes(id);

        // Dragon natural armour bumps.
        setNaturalArmour(CreatureTemplate.DRAGON_BLUE_CID, 0.025f);
        setNaturalArmour(CreatureTemplate.DRAGON_WHITE_CID, 0.025f);
        setNaturalArmour(CreatureTemplate.DRAGON_BLACK_CID, 0.035f);
        setNaturalArmour(CreatureTemplate.DRAGON_GREEN_CID, 0.025f);
        setNaturalArmour(CreatureTemplate.DRAGON_RED_CID, 0.025f);

        // Drake natural armour bumps.
        setNaturalArmour(CreatureTemplate.DRAKE_RED_CID, 0.055f);
        setNaturalArmour(CreatureTemplate.DRAKE_BLUE_CID, 0.055f);
        setNaturalArmour(CreatureTemplate.DRAKE_WHITE_CID, 0.065f);
        setNaturalArmour(CreatureTemplate.DRAKE_GREEN_CID, 0.055f);
        setNaturalArmour(CreatureTemplate.DRAKE_BLACK_CID, 0.045f);

        setNaturalArmour(CreatureTemplate.GOBLIN_LEADER_CID, 0.045f);

        // PvP servers reduce worg armour to keep them killable on Arena.
        if (Servers.localServer.PVPSERVER) {
            setNaturalArmour(CreatureTemplate.WORG_CID, 0.3f);
        }

        // Grazers — reduces wandering away from coops.
        setGrazer(CreatureTemplate.HEN_CID);
        setGrazer(CreatureTemplate.CHICKEN_CID);
        setGrazer(CreatureTemplate.ROOSTER_CID);
        setGrazer(CreatureTemplate.PIG_CID);

        // Worg becomes a ridable mount with horse-like flags + skill set.
        setWorgFields(CreatureTemplate.WORG_CID);

        // Cyclops gain fighting skill so they hit harder.
        setSkill(CreatureTemplate.CYCLOPS_CID, SkillList.GROUP_FIGHTING, 80.0f);

        // Boost SoN bounty / xp.
        setCombatRating(CreatureTemplate.SON_OF_NOGUMP_CID, 30.0f);
    }

    private static void setUniqueTypes(int templateId) {
        try {
            CreatureTemplate t = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (t == null) return;
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "isNotRebirthable"), true);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "regenerating"), false);
        } catch (Exception e) {
            warn("setUniqueTypes(" + templateId + ")", e);
        }
    }

    private static void setNaturalArmour(int templateId, float value) {
        try {
            CreatureTemplate t = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (t == null) return;
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "naturalArmour"), value);
        } catch (Exception e) {
            warn("setNaturalArmour(" + templateId + ")", e);
        }
    }

    private static void setGrazer(int templateId) {
        try {
            CreatureTemplate t = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (t == null) return;
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "grazer"), true);
        } catch (Exception e) {
            warn("setGrazer(" + templateId + ")", e);
        }
    }

    private static void setSkill(int templateId, int skillId, float value) {
        try {
            CreatureTemplate t = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (t == null) return;
            Skills skills = ReflectionUtil.getPrivateField(
                    t, ReflectionUtil.getField(t.getClass(), "skills"));
            skills.learnTemp(skillId, value);
            ReflectionUtil.setPrivateField(
                    t, ReflectionUtil.getField(t.getClass(), "skills"), skills);
        } catch (Exception e) {
            warn("setSkill(" + templateId + ", " + skillId + ")", e);
        }
    }

    private static void setCombatRating(int templateId, float value) {
        try {
            CreatureTemplate t = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (t == null) return;
            ReflectionUtil.setPrivateField(
                    t, ReflectionUtil.getField(t.getClass(), "baseCombatRating"), value);
        } catch (Exception e) {
            warn("setCombatRating(" + templateId + ")", e);
        }
    }

    private static void setWorgFields(int templateId) {
        try {
            CreatureTemplate t = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            if (t == null) return;
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "isVehicle"), true);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "dominatable"), true);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "isHorse"), true);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "isDetectInvis"), false);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "monster"), true);
            Skills skills = SkillsFactory.createSkills("Worg");
            skills.learnTemp(SkillList.BODY_STRENGTH, 40.0f);
            skills.learnTemp(SkillList.BODY_CONTROL, 25.0f);
            skills.learnTemp(SkillList.BODY_STAMINA, 35.0f);
            skills.learnTemp(SkillList.MIND_LOGICAL, 10.0f);
            skills.learnTemp(SkillList.MIND_SPEED, 15.0f);
            skills.learnTemp(SkillList.SOUL_STRENGTH, 20.0f);
            skills.learnTemp(SkillList.SOUL_DEPTH, 12.0f);
            skills.learnTemp(SkillList.WEAPONLESS_FIGHTING, 50.0f);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), "skills"), skills);
        } catch (Exception e) {
            warn("setWorgFields(" + templateId + ")", e);
        }
    }

    private static void warn(String op, Exception e) {
        if (e instanceof NoSuchCreatureTemplateException) {
            logger.fine("[bestiary] " + op + ": template not registered");
        } else {
            logger.log(Level.WARNING, "[bestiary] " + op + " failed", e);
        }
    }
}
