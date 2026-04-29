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

class ArenaEscapePerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(ArenaEscapePerformer.class.getName());

    @Override
    public short getActionId() {
        return (short) TeleportMod.actionArenaEscapeId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        try {
            if (!(performer instanceof Player)) return true;
            if (!Servers.localServer.PVPSERVER) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You must be in the Arena in order to escape it!");
                return true;
            }
            if (performer.isStealth()) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You cannot escape while in stealth mode.");
                return true;
            }
            if (performer.getEnemyPresense() > 0 || performer.isFighting()) {
                performer.getCommunicator().sendNormalServerMessage(
                        "Nearby enemies prevent your escape. Clear the area and try again.");
                return true;
            }
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You prepare your body and mind to transfer to another realm.");
                performer.playAnimation("meditate", false);
                act.setTimeLeft(1800);
                performer.sendActionControl("Preparing", true, act.getTimeLeft());
                return false;
            }
            if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                ServerEntry targetserver = Servers.localServer.serverSouth;
                if (targetserver == null) {
                    performer.getCommunicator().sendNormalServerMessage(
                            "Error: Home server not configured [serverSouth=null].");
                    return true;
                }
                if (!targetserver.isAvailable(performer.getPower(), true)) {
                    performer.getCommunicator().sendNormalServerMessage(
                            targetserver.name + " is not currently available.");
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage("You successfully escape the arena.");
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
            logger.log(Level.WARNING, "[teleport] ArenaEscapePerformer failed", e);
            return true;
        }
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
