package com.garward.wurmmodloader.mods.keyevent;

import com.wurmonline.server.Message;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

/**
 * Single-server GL-Freedom broadcast helper used by the ritual to deliver
 * deity dialogue. Upstream additionally cross-server-wired this through
 * {@code WcKingdomChat}; this port targets single-server PvE so we simply
 * walk {@link Players#getPlayers()}.
 */
final class FreedomBroadcast {

    private FreedomBroadcast() {}

    static void send(Creature sender, String name, String message, int r, int g, int b) {
        for (Player rec : Players.getInstance().getPlayers()) {
            Message m = new Message(sender, (byte) 10, "GL-Freedom",
                    "<" + name + "> " + message, r, g, b);
            rec.getCommunicator().sendMessage(m);
        }
    }
}
