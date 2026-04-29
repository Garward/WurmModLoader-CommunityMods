package com.garward.wurmmodloader.mods.rarespawn;

import com.wurmonline.server.creatures.Creature;

import java.util.ArrayList;
import java.util.List;

final class RareSpawnState {

    static final List<Creature> rares = new ArrayList<>();

    static boolean isRareCreature(int templateId) {
        return templateId == Reaper.templateId
                || templateId == SpectralDrake.templateId;
    }

    static boolean isRareCreature(Creature c) {
        return isRareCreature(c.getTemplate().getTemplateId());
    }

    private RareSpawnState() {}
}
