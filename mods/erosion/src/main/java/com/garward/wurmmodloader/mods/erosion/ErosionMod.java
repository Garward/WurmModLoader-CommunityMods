package com.garward.wurmmodloader.mods.erosion;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErosionMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(ErosionMod.class.getName());

    private long pollIntervalMillis = 30_000L;
    private long lastPolled = 0L;

    @Override
    public void configure(Properties properties) {
        String raw = properties.getProperty("pollIntervalSeconds");
        if (raw != null) {
            try {
                long seconds = Long.parseLong(raw.trim());
                if (seconds > 0) pollIntervalMillis = seconds * 1000L;
            } catch (NumberFormatException e) {
                logger.warning("[erosion] pollIntervalSeconds must be a positive integer, got: " + raw);
            }
        }
        logger.info("[erosion] poll interval " + (pollIntervalMillis / 1000L) + "s");
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (Servers.localServer.PVPSERVER) return;
        long now = System.currentTimeMillis();
        if (now < lastPolled + pollIntervalMillis) return;
        lastPolled = now;
        try {
            int size = Server.surfaceMesh.getSize();
            int tilex = Server.rand.nextInt(size);
            int tiley = Server.rand.nextInt(size);
            TerrainSmoothing.smoothArea(tilex, tiley);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[erosion] poll tick failed", t);
        }
    }
}
