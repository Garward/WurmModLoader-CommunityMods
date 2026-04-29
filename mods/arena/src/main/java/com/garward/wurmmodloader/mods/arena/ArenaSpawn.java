package com.garward.wurmmodloader.mods.arena;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.NewSpawnQuestion;
import com.wurmonline.server.questions.SpawnQuestion;
import com.wurmonline.server.skills.Affinity;

/**
 * Spawn-question / cross-server transfer / null-affinity helpers. Bound by
 * the Arena patches via static FQN — keep these methods public.
 */
public final class ArenaSpawn {

    public static void sendNewSpawnQuestion(SpawnQuestion sq) {
        NewSpawnQuestion nsq = new NewSpawnQuestion(
                sq.getResponder(), "Respawn", "Where would you like to spawn?",
                sq.getResponder().getWurmId());
        nsq.sendQuestion();
    }

    public static void respawnPlayer(Creature player, ServerEntry server) {
        ServerEntry target = server.serverWest;
        if (target == null) return;
        if (player instanceof Player) {
            Player p = (Player) player;
            int tilex = target.SPAWNPOINTJENNX;
            int tiley = target.SPAWNPOINTJENNY;
            p.sendTransfer(Server.getInstance(), target.INTRASERVERADDRESS,
                    Integer.parseInt(target.INTRASERVERPORT), target.INTRASERVERPASSWORD,
                    target.id, tilex, tiley, true, false, p.getKingdomId());
        }
    }

    public static Affinity[] getNullAffinities() {
        return new Affinity[0];
    }

    private ArenaSpawn() {}
}
