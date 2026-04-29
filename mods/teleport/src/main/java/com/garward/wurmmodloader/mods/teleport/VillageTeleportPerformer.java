package com.garward.wurmmodloader.mods.teleport;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

class VillageTeleportPerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(VillageTeleportPerformer.class.getName());

    @Override
    public short getActionId() {
        return (short) TeleportMod.actionVillageTeleportId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        try {
            if (!(performer instanceof Player)) return true;
            if (Servers.localServer.PVPSERVER) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You cannot use Village Teleport on a PvP server.");
                return true;
            }
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You prepare to transport yourself to another location.");
                act.setTimeLeft(600);
                performer.sendActionControl("Teleporting", true, act.getTimeLeft());
                return false;
            }
            if (performer.isFighting()) {
                performer.getCommunicator().sendAlertServerMessage(
                        "Your teleport was interrupted by entering combat.");
                return true;
            }
            if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                Player player = (Player) performer;
                if (player.getCitizenVillage() != null) {
                    act.stop(false);
                    int tilex = player.getCitizenVillage().getTokenX() * 4;
                    int tiley = player.getCitizenVillage().getTokenY() * 4;
                    player.setTeleportPoints(tilex + 2.0f, tiley + 2.0f, 0, 0);
                    if (player.startTeleporting()) {
                        player.getCommunicator().sendNormalServerMessage(
                                "You feel a slight tingle in your spine.");
                        player.getCommunicator().sendTeleport(false);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.WARNING, "[teleport] VillageTeleportPerformer failed", e);
            return true;
        }
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
