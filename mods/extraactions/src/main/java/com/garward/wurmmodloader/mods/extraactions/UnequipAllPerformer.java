package com.garward.wurmmodloader.mods.extraactions;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.AutoEquipMethods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

class UnequipAllPerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(UnequipAllPerformer.class.getName());

    @Override
    public short getActionId() {
        return (short) ExtraActionsMod.unequipAllId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        if (!(performer instanceof Player)) return true;
        try {
            Player player = (Player) performer;
            if (target.getParent() == null) {
                player.getCommunicator().sendSafeServerMessage(
                        "You cannot unequip an item that isn't equipped.");
                return true;
            }
            if (target.getParent().getOwnerId() != player.getWurmId()) {
                player.getCommunicator().sendSafeServerMessage(
                        "You cannot unequip an item that you do not own.");
                return true;
            }
            for (Item equip : player.getBody().getAllItems()) {
                if (equip.isArmour() && equip.getParent().getWurmId() != player.getBody().getId()) {
                    AutoEquipMethods.unequip(equip, player);
                }
            }
        } catch (NoSuchItemException e) {
            logger.log(Level.WARNING, "[extraactions] UnequipAll failed", e);
        }
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
