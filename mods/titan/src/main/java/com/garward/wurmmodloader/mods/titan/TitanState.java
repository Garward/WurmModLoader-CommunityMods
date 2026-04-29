package com.garward.wurmmodloader.mods.titan;

import com.wurmonline.server.creatures.Creature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TitanState {

    static final List<Creature> titans = new ArrayList<>();
    static final Map<Creature, Integer> titanDamage = new HashMap<>();
    static final Map<Long, Integer> titanAdvancedTimed = new HashMap<>();
    static long lastSpawnedTitan = 0L;

    static boolean isTitan(int templateId) {
        return templateId == Lilith.templateId || templateId == Ifrit.templateId;
    }

    static boolean isTitan(Creature creature) {
        return isTitan(creature.getTemplate().getTemplateId());
    }

    static boolean isTitanMinion(int templateId) {
        return templateId == LilithWraith.templateId
                || templateId == LilithZombie.templateId
                || templateId == IfritFiend.templateId
                || templateId == IfritSpider.templateId;
    }

    static boolean isTitanMinion(Creature creature) {
        return isTitanMinion(creature.getTemplate().getTemplateId());
    }

    private TitanState() {}
}
