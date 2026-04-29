package com.garward.wurmmodloader.mods.teleport;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Picks a real spawn point for a player whose persisted position fell in the
 * vanilla 4000–4050 transfer-landing box. Patched into PlayerMetaData.save by
 * {@link TeleportPatches} when {@code useArenaTeleportMethod} is enabled.
 *
 * <p>Resolution order: deed token (if citizen) → JENNX/JENNY (PvE) → random
 * walkable surface tile not inside a deed (PvP).
 */
public final class TeleportSpawnPicker {

    private static final Logger logger = Logger.getLogger(TeleportSpawnPicker.class.getName());

    private static final Map<Long, Float> teleX = new HashMap<>();
    private static final Map<Long, Float> teleY = new HashMap<>();

    private TeleportSpawnPicker() {}

    public static float getTeleportPosX(long wurmid) {
        ensureLocation(wurmid);
        Float x = teleX.get(wurmid);
        return x == null ? 4000f : x;
    }

    public static float getTeleportPosY(long wurmid) {
        Float y = teleY.get(wurmid);
        return y == null ? 4000f : y;
    }

    private static void ensureLocation(long wurmid) {
        PlayerInfo pinfo = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        if (pinfo != null) {
            for (Village v : Villages.getVillages()) {
                if (v.isCitizen(wurmid)) {
                    logger.info("[teleport] " + wurmid + " citizen of " + v.getName() + ", landing on token");
                    teleX.put(wurmid, (float) (v.getTokenX() * 4));
                    teleY.put(wurmid, (float) (v.getTokenY() * 4));
                    return;
                }
            }
        }
        if (Servers.localServer.PVPSERVER) {
            logger.info("[teleport] " + wurmid + " no village + PvP server, picking random walkable tile");
            pickRandomLocation(wurmid);
        } else {
            logger.info("[teleport] " + wurmid + " no village + PvE server, landing on JENNX/JENNY");
            teleX.put(wurmid, (float) (Servers.localServer.SPAWNPOINTJENNX * 4));
            teleY.put(wurmid, (float) (Servers.localServer.SPAWNPOINTJENNY * 4));
        }
    }

    private static void pickRandomLocation(long wurmid) {
        for (int tries = 0; tries < 1000; tries++) {
            int x = Server.rand.nextInt(Server.surfaceMesh.getSize());
            int y = Server.rand.nextInt(Server.surfaceMesh.getSize());
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
            if (height <= 0 || height >= 1000) continue;
            if (Creature.getTileSteepness(x, y, true)[1] >= 30) continue;
            if (isNearVillage(x, y)) continue;
            teleX.put(wurmid, (float) (x * 4));
            teleY.put(wurmid, (float) (y * 4));
            return;
        }
        logger.warning("[teleport] random landing search exhausted for " + wurmid + " — falling back to 4000/4000");
        teleX.put(wurmid, 4000f);
        teleY.put(wurmid, 4000f);
    }

    private static boolean isNearVillage(int x, int y) {
        if (Villages.getVillage(x, y, true) != null) return true;
        for (int vx = -50; vx < 50; vx += 5) {
            for (int vy = -50; vy < 50; vy += 5) {
                if (Villages.getVillage(x + vx, y + vy, true) != null) return true;
            }
        }
        return false;
    }
}
