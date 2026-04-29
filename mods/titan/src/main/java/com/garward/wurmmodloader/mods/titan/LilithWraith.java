package com.garward.wurmmodloader.mods.titan;

import com.garward.wurmmodloader.modsupport.CreatureTemplateBuilder;
import com.garward.wurmmodloader.modsupport.creatures.ModCreature;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.CreatureTypes;

public class LilithWraith implements ModCreature, CreatureTypes {
    public static int templateId;

    @Override
    public CreatureTemplateBuilder createCreateTemplateBuilder() {
        int[] types = {
                CreatureTypes.C_TYPE_MOVE_LOCAL,
                CreatureTypes.C_TYPE_AGG_HUMAN,
                CreatureTypes.C_TYPE_HUNTING,
                CreatureTypes.C_TYPE_CARNIVORE,
                CreatureTypes.C_TYPE_DETECTINVIS,
                CreatureTypes.C_TYPE_NON_NEWBIE
        };

        CreatureTemplateBuilder builder = new CreatureTemplateBuilder(
                "mod.creature.lilith.wraith", "Wraith of Lilith", "A champion of Lilith.",
                "model.creature.humanoid.human.spirit.wraith",
                types, BodyTemplate.TYPE_HUMAN, (short) 30, (byte) 0,
                (short) 85, (short) 50, (short) 85,
                "sound.death.spirit.male", "sound.death.spirit.female",
                "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female",
                0.3f, 23.0f, 0.0f, 19.0f, 0.0f, 0.0f, 2.0f, 2000,
                new int[]{}, 15, 70, Materials.MATERIAL_MEAT_HUMANOID);

        builder.skill(SkillList.BODY_STRENGTH, 55.0f);
        builder.skill(SkillList.BODY_STAMINA, 65.0f);
        builder.skill(SkillList.BODY_CONTROL, 60.0f);
        builder.skill(SkillList.MIND_LOGICAL, 50.0f);
        builder.skill(SkillList.MIND_SPEED, 50.0f);
        builder.skill(SkillList.SOUL_STRENGTH, 50.0f);
        builder.skill(SkillList.SOUL_DEPTH, 50.0f);
        builder.skill(SkillList.WEAPONLESS_FIGHTING, 50.0f);
        builder.skill(SkillList.GROUP_FIGHTING, 40.0f);

        builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
        builder.handDamString("claw");
        builder.maxAge(100);
        builder.armourType(ArmourTypes.ARMOUR_LEATHER_DRAGON);
        builder.baseCombatRating(25.0f);
        builder.combatDamageType(Wound.TYPE_INFECTION);
        builder.maxPercentOfCreatures(0.005f);
        builder.maxGroupAttackSize(100);

        templateId = builder.getTemplateId();
        return builder;
    }

    @Override
    public void addEncounters() {
    }
}
