package com.garward.wurmmodloader.mods.wyverncombat;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Periodic 75-HP heal of one random wound on each living unique creature.
 * Maintains a small running list of unique creatures so we don't sweep
 * the whole creature table every tick.
 *
 * <p>Direct port of {@code CombatChanges.pollUniqueCollection /
 * pollUniqueRegeneration} from upstream WyvernMods.
 */
final class UniqueRegenerationPoll {

    private static final Logger logger = Logger.getLogger(UniqueRegenerationPoll.class.getName());
    private static final int HEAL_PER_TICK = 75;

    private static final ArrayList<Creature> uniques = new ArrayList<>();

    private UniqueRegenerationPoll() {}

    static void tick() {
        refreshUniqueList();
        if (uniques.isEmpty()) return;
        for (Creature cret : uniques) {
            if (cret == null || cret.getBody() == null || !cret.getBody().isWounded()) continue;
            Wounds tWounds = cret.getBody().getWounds();
            if (tWounds == null) continue;
            Wound[] arr = tWounds.getWounds();
            if (arr == null || arr.length == 0) continue;
            Wound w = arr[Server.rand.nextInt(arr.length)];
            if (w.getSeverity() > HEAL_PER_TICK) {
                w.modifySeverity(-HEAL_PER_TICK);
            } else {
                w.heal();
            }
        }
    }

    private static void refreshUniqueList() {
        for (Creature cret : Creatures.getInstance().getCreatures()) {
            if (cret.isUnique() && !uniques.contains(cret)) {
                logger.info("[wyverncombat] tracking unique " + cret.getName());
                uniques.add(cret);
            }
        }
        Iterator<Creature> it = uniques.iterator();
        while (it.hasNext()) {
            Creature c = it.next();
            if (c == null || c.isDead()) {
                if (c != null) {
                    logger.info("[wyverncombat] dropping dead unique " + c.getName());
                }
                it.remove();
            }
        }
    }
}
