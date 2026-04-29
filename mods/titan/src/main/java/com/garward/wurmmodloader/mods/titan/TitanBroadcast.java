package com.garward.wurmmodloader.mods.titan;

import com.wurmonline.server.Message;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

final class TitanBroadcast {

    static void broadcast(Creature sender, String message, int red, int green, int blue) {
        for (Player rec : Players.getInstance().getPlayers()) {
            Communicator c = rec.getCommunicator();
            if (c == null) continue;
            Message gl = new Message(sender, (byte) 10, "GL-Freedom",
                    "<" + sender.getNameWithoutPrefixes() + "> " + message, red, green, blue);
            c.sendMessage(gl);
            Message tab = new Message(rec, (byte) 16, "Server", message, red, green, blue);
            c.sendMessage(tab);
        }
    }

    private TitanBroadcast() {}
}
