package com.garward.wurmmodloader.mods.teleport;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

class ArenaTeleportPerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(ArenaTeleportPerformer.class.getName());

    @Override
    public short getActionId() {
        return (short) TeleportMod.actionArenaTeleportId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        try {
            if (!(performer instanceof Player)) return true;
            if (Servers.localServer.PVPSERVER) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You cannot enter the arena from here.");
                return true;
            }
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You sit and begin to transfer your mind.");
                performer.playAnimation("meditate", false);
                act.setTimeLeft(600);
                performer.sendActionControl("Transferring", true, act.getTimeLeft());
                return false;
            }
            int sec = act.currentSecond();
            if (sec == 10) {
                performer.getCommunicator().sendAlertServerMessage(
                        "You are about to enter a full PvP environment.", (byte) 3);
            } else if (sec == 20) {
                performer.getCommunicator().sendNormalServerMessage(
                        "Death will not drop your items. Instead they will be damaged heavily and returned to you upon respawn.", (byte) 3);
            } else if (sec == 30) {
                performer.getCommunicator().sendNormalServerMessage(
                        "If you are part of a deed, you will arrive at the token. Otherwise, you will be placed in a random location.", (byte) 3);
            } else if (sec == 40) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You can equip creatures such as horses by simply leading them. Taming is not required in the Arena.", (byte) 3);
            } else if (sec == 55) {
                performer.getCommunicator().sendNormalServerMessage(
                        "It appears you have accepted these conditions. Transferring to the Arena. Good luck.", (byte) 3);
            } else if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                ServerEntry targetserver = Servers.localServer.serverNorth;
                if (targetserver == null) {
                    performer.getCommunicator().sendNormalServerMessage(
                            "Error: Arena server not configured [serverNorth=null].");
                    return true;
                }
                if (!targetserver.isAvailable(performer.getPower(), true)) {
                    performer.getCommunicator().sendNormalServerMessage(
                            targetserver.name + " is not currently available.");
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage(
                        "You transfer to " + targetserver.name + ".");
                Server.getInstance().broadCastAction(
                        performer.getName() + " transfers to " + targetserver.name + ".", performer, 5);
                int tilex = 1010;
                int tiley = 1010;
                performer.sendTransfer(Server.getInstance(),
                        targetserver.INTRASERVERADDRESS,
                        Integer.parseInt(targetserver.INTRASERVERPORT),
                        targetserver.INTRASERVERPASSWORD,
                        targetserver.id, tilex, tiley,
                        true, false, performer.getKingdomId());
                ((Player) performer).transferCounter = 30;
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.WARNING, "[teleport] ArenaTeleportPerformer failed", e);
            return true;
        }
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
