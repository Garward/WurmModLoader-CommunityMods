package com.garward.wurmmodloader.mods.supplydepot;

import com.garward.wurmmodloader.modsupport.spawn.SpawnFilter;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DepotPoll {

    private static final Logger logger = Logger.getLogger(DepotPoll.class.getName());
    private static final int CHAT_R = 255, CHAT_G = 128, CHAT_B = 0;
    private static final String CHANNEL = "arena";

    static void tick(SupplyDepotConfig cfg) {
        if (DepotTemplates.depotTemplateId <= 0) return;

        try {
            removeDestroyedDepots();
            ingestExistingDepots();

            if (cfg.requirePvpServer && !Servers.localServer.PVPSERVER) return;

            if (DepotState.depots.isEmpty()) {
                ensureHostPicked();
                long now = System.currentTimeMillis();
                long spawnAt = DepotState.lastSpawnedDepot + cfg.respawnTimeMs;
                if (now > spawnAt) {
                    trySpawnDepot(cfg);
                } else if (DepotState.host != null) {
                    announceTimeLeft(spawnAt - now);
                }
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[supplydepot] poll tick failed", t);
        }
    }

    private static void removeDestroyedDepots() {
        Iterator<Item> it = DepotState.depots.iterator();
        while (it.hasNext()) {
            Item depot = it.next();
            if (!Items.exists(depot)) {
                logger.info("[supplydepot] depot was destroyed; cleaning state");
                it.remove();
                DepotEffects.removeDepotEffect(depot);
            }
        }
    }

    private static void ingestExistingDepots() {
        for (Item item : Items.getAllItems()) {
            if (DepotState.isSupplyDepot(item) && !DepotState.depots.contains(item)) {
                logger.info("[supplydepot] adopting existing depot " + item.getWurmId());
                DepotState.depots.add(item);
                DepotEffects.sendDepotEffectsToPlayers(item);
            }
        }
    }

    private static void ensureHostPicked() {
        if (DepotState.host != null && !DepotState.host.isDead()) return;
        List<Creature> uniques = new ArrayList<>();
        for (Creature c : Creatures.getInstance().getCreatures()) {
            if (c.isUnique() && !c.isDead()) uniques.add(c);
        }
        if (uniques.isEmpty()) return;
        DepotState.host = uniques.get(Server.rand.nextInt(uniques.size()));
        DepotBroadcast.globalFreedomChat(DepotState.host,
                "Greetings! I'll be your host, informing you of the next depot to appear.",
                CHAT_R, CHAT_G, CHAT_B);
    }

    private static void announceTimeLeft(long timeLeftMs) {
        long minutesLeft = timeLeftMs / TimeConstants.MINUTE_MILLIS;
        if (minutesLeft <= 0) return;
        if (minutesLeft == 4) {
            DepotBroadcast.serverTab(CHANNEL,
                    "The next Arena depot will appear in 5 minutes!", CHAT_R, CHAT_G, CHAT_B);
        } else if (minutesLeft == 19) {
            DepotBroadcast.serverTab(CHANNEL,
                    "The next Arena depot will appear in 20 minutes!", CHAT_R, CHAT_G, CHAT_B);
            DepotBroadcast.globalFreedomChat(DepotState.host,
                    "The next Arena depot will appear in 20 minutes!", CHAT_R, CHAT_G, CHAT_B);
        } else if (minutesLeft == 59) {
            DepotBroadcast.serverTab(CHANNEL,
                    "The next Arena depot will appear in 60 minutes!", CHAT_R, CHAT_G, CHAT_B);
            DepotBroadcast.globalFreedomChat(DepotState.host,
                    "The next Arena depot will appear in 60 minutes!", CHAT_R, CHAT_G, CHAT_B);
        }
    }

    private static void trySpawnDepot(SupplyDepotConfig cfg) {
        int[] tile = SpawnFilter.pickSurfaceTile(cfg.spawn, 200);
        if (tile == null) {
            logger.warning("[supplydepot] could not find a valid spawn tile (filter too strict?)");
            return;
        }
        int tilex = tile[0];
        int tiley = tile[1];
        try {
            float quality = 50f + Server.rand.nextFloat() * 40f;
            float posX = (float) (tilex << 2) + 2.0f;
            float posY = (float) (tiley << 2) + 2.0f;
            float rotation = Server.rand.nextFloat() * 360.0f;
            Item depot = ItemFactory.createItem(DepotTemplates.depotTemplateId,
                    quality, posX, posY, rotation, true, (byte) 0, -10, null);
            DepotState.depots.add(depot);
            DepotEffects.sendDepotEffectsToPlayers(depot);
            logger.info("[supplydepot] spawned depot at tile " + tilex + "," + tiley);

            if (DepotState.host != null) {
                DepotBroadcast.globalFreedomChat(DepotState.host,
                        "A new Arena depot has appeared!", CHAT_R, CHAT_G, CHAT_B);
            }
            DepotBroadcast.serverTab(CHANNEL,
                    "A new Arena depot has appeared!", CHAT_R, CHAT_G, CHAT_B);

            DepotState.host = null;
            DepotDb.updateLastSpawnedDepot();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[supplydepot] failed to create depot", e);
        }
    }

    static void onDepotCaptured(Item depot) {
        DepotState.depots.remove(depot);
        DepotEffects.removeDepotEffect(depot);
    }

    private DepotPoll() {}
}
