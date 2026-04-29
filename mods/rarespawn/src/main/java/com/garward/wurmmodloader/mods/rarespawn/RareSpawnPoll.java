package com.garward.wurmmodloader.mods.rarespawn;

import com.garward.wurmmodloader.modsupport.spawn.SpawnFilter;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

final class RareSpawnPoll {

    private static final Logger logger = Logger.getLogger(RareSpawnPoll.class.getName());

    static void tick(RareSpawnConfig cfg) {
        ingestExisting();
        removeDead();
        if (RareSpawnState.rares.isEmpty()) {
            spawnNewWave(cfg);
        }
    }

    private static void ingestExisting() {
        Creature[] creatures = Creatures.getInstance().getCreatures();
        for (Creature c : creatures) {
            if (RareSpawnState.isRareCreature(c) && !RareSpawnState.rares.contains(c)) {
                RareSpawnState.rares.add(c);
                logger.info("[rarespawn] tracking existing " + c.getName());
            }
        }
    }

    private static void removeDead() {
        Iterator<Creature> it = RareSpawnState.rares.iterator();
        while (it.hasNext()) {
            Creature c = it.next();
            if (c.isDead()) {
                logger.info("[rarespawn] " + c.getName() + " is dead; removing from registry");
                it.remove();
            }
        }
    }

    private static void spawnNewWave(RareSpawnConfig cfg) {
        int[] choices = { Reaper.templateId, SpectralDrake.templateId };
        int chosen = choices[Server.rand.nextInt(choices.length)];
        spawnRandomLocationCreature(chosen, cfg);
        if (cfg.includeWyvernBlue && WyvernBlue.templateId > 0) {
            spawnRandomLocationCreature(WyvernBlue.templateId, cfg);
        }
    }

    private static void spawnRandomLocationCreature(int templateId, RareSpawnConfig cfg) {
        if (templateId <= 0) return;
        int[] tile = SpawnFilter.pickSurfaceTile(cfg.spawn, 200);
        if (tile == null) {
            logger.warning("[rarespawn] failed to find a valid spawn tile after 200 attempts");
            return;
        }
        int spawnX = tile[0] * 4;
        int spawnY = tile[1] * 4;
        try {
            logger.info("[rarespawn] spawning template " + templateId + " at "
                    + (spawnX * 0.25f) + ", " + (spawnY * 0.25f));
            Creature.doNew(templateId, spawnX, spawnY,
                    360f * Server.rand.nextFloat(), 0, "",
                    Server.rand.nextBoolean() ? (byte) 0 : (byte) 1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[rarespawn] failed to spawn template " + templateId, e);
        }
    }

    private RareSpawnPoll() {}
}
