package com.garward.wurmmodloader.mods.soulstealing;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import java.util.logging.Level;
import java.util.logging.Logger;

final class SoulstealingPoll {

    private static final Logger logger = Logger.getLogger(SoulstealingPoll.class.getName());

    private SoulstealingPoll() {}

    static void pollAll() {
        if (SoulstealingTemplates.eternalReservoirId <= 0) return;
        for (Item item : Items.getAllItems()) {
            if (item.getTemplateId() == SoulstealingTemplates.eternalReservoirId) {
                try {
                    pollOne(item);
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "[soulstealing] poll failed for reservoir " + item.getWurmId(), t);
                }
            }
        }
    }

    private static void pollOne(Item soulForge) {
        int tilex = soulForge.getTileX();
        int tiley = soulForge.getTileY();
        int range = (int) (soulForge.getCurrentQualityLevel() / 10f);
        int fuel = soulForge.getData1();
        if (fuel < 1) {
            return;
        }
        int sx = Zones.safeTileX(tilex - range);
        int sy = Zones.safeTileY(tiley - range);
        int ex = Zones.safeTileX(tilex + range);
        int ey = Zones.safeTileY(tiley + range);
        for (int x = sx; x <= ex; x++) {
            for (int y = sy; y <= ey; y++) {
                VolaTile t = Zones.getTileOrNull(x, y, soulForge.isOnSurface());
                if (t == null) continue;
                for (Creature lCret : t.getCreatures()) {
                    if (lCret.isBranded() && lCret.isCarnivore()) {
                        int hunger = lCret.getStatus().getHunger();
                        if (hunger > 10000 && fuel > 50) {
                            lCret.getStatus().modifyHunger(-10000, 1);
                            Server.getInstance().broadCastAction(
                                    "The " + lCret.getName() + " is visited by an ethereal creature, and seems less hungry.",
                                    lCret, 10);
                            fuel -= 50;
                        }
                    }
                }
                for (Item item : t.getItems()) {
                    if (item.isForgeOrOven() && item.isOnFire()
                            && item.getTemperature() < 20000 && fuel > 15) {
                        item.setTemperature((short) (item.getTemperature() + 10000));
                        Server.getInstance().broadCastMessage(
                                "The " + item.getName() + " is visited by an ethereal creature, and is refueled.",
                                item.getTileX(), item.getTileY(), item.isOnSurface(), 10);
                        fuel -= 15;
                    }
                }
            }
        }
        soulForge.setData1(fuel);
    }
}
