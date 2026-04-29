package com.garward.wurmmodloader.mods.titan;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.util.logging.Level;
import java.util.logging.Logger;

final class TitanLoot {

    private static final Logger logger = Logger.getLogger(TitanLoot.class.getName());

    static void addTitanLoot(Creature titan, TitanConfig cfg) {
        int artifactId = cfg.artifactCacheTemplateId;
        int treasureMapId = cfg.treasureMapCacheTemplateId;
        if (artifactId <= 0 && treasureMapId <= 0) {
            return;
        }
        int chosen;
        if (artifactId > 0 && treasureMapId > 0) {
            chosen = Server.rand.nextBoolean() ? treasureMapId : artifactId;
        } else {
            chosen = artifactId > 0 ? artifactId : treasureMapId;
        }
        try {
            Item cache = ItemFactory.createItem(
                    chosen, 90f + 10f * Server.rand.nextFloat(), titan.getName());
            titan.getInventory().insertItem(cache, true);
        } catch (FailedException | NoSuchTemplateException e) {
            logger.log(Level.WARNING, "[titan] failed to create loot cache for " + titan.getName(), e);
        }
    }

    private TitanLoot() {}
}
