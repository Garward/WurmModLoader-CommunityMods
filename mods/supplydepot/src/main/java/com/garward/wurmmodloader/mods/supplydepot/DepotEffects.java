package com.garward.wurmmodloader.mods.supplydepot;

import com.wurmonline.server.Players;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Logger;

/**
 * Visual light effect (effect id 25) hung on each active depot. The patch
 * in {@link DepotPatches} fires {@link #sendDepotEffectsToPlayer} when a
 * client logs in / changes server.
 */
public final class DepotEffects {

    private static final Logger logger = Logger.getLogger(DepotEffects.class.getName());
    static boolean useDepotLights = true;

    public static void sendDepotEffect(Player player, Item depot) {
        if (!useDepotLights) return;
        if (player == null || depot == null) return;
        player.getCommunicator().sendAddEffect(depot.getWurmId(), (byte) 25,
                depot.getPosX(), depot.getPosY(), depot.getPosZ(), (byte) 0);
    }

    public static void sendDepotEffectsToPlayer(Player player) {
        if (!useDepotLights || player == null) return;
        logger.info("[supplydepot] sending depot effects to " + player.getName());
        for (Item depot : DepotState.depots) {
            sendDepotEffect(player, depot);
        }
    }

    static void sendDepotEffectsToPlayers(Item depot) {
        for (Player p : Players.getInstance().getPlayers()) {
            sendDepotEffect(p, depot);
        }
    }

    static void removeDepotEffect(Item depot) {
        for (Player player : Players.getInstance().getPlayers()) {
            player.getCommunicator().sendRemoveEffect(depot.getWurmId());
        }
    }

    private DepotEffects() {}
}
