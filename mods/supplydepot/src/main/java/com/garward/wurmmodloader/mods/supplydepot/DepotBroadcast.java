package com.garward.wurmmodloader.mods.supplydepot;

import com.wurmonline.server.Message;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

final class DepotBroadcast {

    static void serverTab(String channel, String message, int red, int green, int blue) {
        for (Player rec : Players.getInstance().getPlayers()) {
            Communicator c = rec.getCommunicator();
            if (c == null) continue;
            // Channel param exists upstream but the generic "Server" tab is what
            // upstream routes to via sendServerTabMessage(channel, ...). Match.
            Message m = new Message(rec, (byte) 16, channel == null ? "Server" : channel,
                    message, red, green, blue);
            c.sendMessage(m);
        }
    }

    static void globalFreedomChat(Creature sender, String message, int red, int green, int blue) {
        if (sender == null) {
            // No host yet; degrade to server tab so the message still lands.
            serverTab("Server", message, red, green, blue);
            return;
        }
        for (Player rec : Players.getInstance().getPlayers()) {
            Communicator c = rec.getCommunicator();
            if (c == null) continue;
            Message gl = new Message(sender, (byte) 10, "GL-Freedom",
                    "<" + sender.getNameWithoutPrefixes() + "> " + message, red, green, blue);
            c.sendMessage(gl);
        }
    }

    private DepotBroadcast() {}
}
