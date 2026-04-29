package com.garward.wurmmodloader.mods.titan;

import com.garward.wurmmodloader.modsupport.CreatureTemplateBuilder;
import com.garward.wurmmodloader.modsupport.creatures.ModCreature;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.CreatureTypes;

public class Ifrit implements ModCreature, CreatureTypes {
    public static int templateId;

    @Override
    public CreatureTemplateBuilder createCreateTemplateBuilder() {
        int[] types = {
                CreatureTypes.C_TYPE_MOVE_LOCAL,
                CreatureTypes.C_TYPE_AGG_HUMAN,
                CreatureTypes.C_TYPE_CARNIVORE,
                CreatureTypes.C_TYPE_HUNTING,
                CreatureTypes.C_TYPE_NON_NEWBIE,
                CreatureTypes.C_TYPE_NO_REBIRTH
        };

        CreatureTemplateBuilder builder = new CreatureTemplateBuilder(
                "mod.creature.raid.ifrit", "Ifrit",
                "A valiant warrior of the flame. You feel the presence of Magranon.",
                "model.creature.humanoid.giant.juggernaut",
                types, BodyTemplate.TYPE_HUMAN, (short) 5, (byte) 0,
                (short) 350, (short) 100, (short) 60,
                "sound.death.magranon.juggernaut", "sound.death.magranon.juggernaut",
                "sound.combat.hit.magranon.juggernaut", "sound.combat.hit.magranon.juggernaut",
                Servers.localServer.PVPSERVER ? 0.028f : 0.014f,
                10.0f, 13.0f, 0.0f, 0.0f, 0.0f, 0.5f, 400,
                new int[]{}, 40, 100, Materials.MATERIAL_MEAT_HUMANOID);

        builder.skill(SkillList.BODY_STRENGTH, 99.0f);
        builder.skill(SkillList.BODY_STAMINA, 99.0f);
        builder.skill(SkillList.BODY_CONTROL, 99.0f);
        builder.skill(SkillList.MIND_LOGICAL, 99.0f);
        builder.skill(SkillList.MIND_SPEED, 99.0f);
        builder.skill(SkillList.SOUL_STRENGTH, 99.0f);
        builder.skill(SkillList.SOUL_DEPTH, 99.0f);
        builder.skill(SkillList.WEAPONLESS_FIGHTING, 99.0f);
        builder.skill(SkillList.GROUP_FIGHTING, 99.0f);
        builder.skill(SkillList.SCYTHE, 99.0f);

        builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
        builder.handDamString("burn");
        builder.kickDamString("ignite");
        builder.maxAge(200);
        builder.armourType(ArmourTypes.ARMOUR_SCALE_DRAGON);
        builder.baseCombatRating(99.0f);
        builder.combatDamageType(Wound.TYPE_BURN);
        builder.maxGroupAttackSize(150);
        builder.color(255, 128, 128);

        templateId = builder.getTemplateId();
        return builder;
    }

    @Override
    public void addEncounters() {
    }
}
