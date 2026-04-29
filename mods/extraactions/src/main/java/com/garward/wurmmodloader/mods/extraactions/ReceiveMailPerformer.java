package com.garward.wurmmodloader.mods.extraactions;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.players.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class ReceiveMailPerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(ReceiveMailPerformer.class.getName());

    @Override
    public short getActionId() {
        return (short) ExtraActionsMod.receiveMailId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        if (!(performer instanceof Player)) return true;
        Player player = (Player) performer;

        if (!target.isMailBox()) {
            player.getCommunicator().sendSafeServerMessage(
                    "You can only receive mail at a mailbox.");
            return true;
        }
        if (target.getSpellCourierBonus() <= 0f) {
            player.getCommunicator().sendSafeServerMessage(
                    "The mailbox must be enchanted before receiving mail.");
            return true;
        }
        if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f)) {
            player.getCommunicator().sendSafeServerMessage(
                    "You must be closer to collect mail.");
            return true;
        }
        if (!target.isEmpty(false)) {
            player.getCommunicator().sendSafeServerMessage(
                    "Empty the mailbox first.");
            return true;
        }

        Set<WurmMail> mailset = WurmMail.getSentMailsFor(performer.getWurmId(), 100);
        if (mailset.isEmpty()) {
            player.getCommunicator().sendSafeServerMessage(
                    "You have no mail to collect.");
            return true;
        }

        HashSet<Item> itemset = new HashSet<>();
        Iterator<WurmMail> it = mailset.iterator();
        while (it.hasNext()) {
            WurmMail m = it.next();
            if (m.rejected || m.price > 0) continue;
            try {
                itemset.add(Items.getItem(m.itemId));
            } catch (NoSuchItemException e) {
                logger.log(Level.INFO, "[extraactions] missing mailed item " + m.itemId, e);
            }
        }
        if (itemset.isEmpty()) return true;

        player.getCommunicator().sendSafeServerMessage(
                "The " + itemset.size() + " items that were sent via mail are now available.");
        for (Item item : itemset) {
            for (Item contained : item.getAllItems(true)) {
                contained.setMailed(false);
                contained.setLastOwnerId(performer.getWurmId());
            }
            WurmMail.removeMail(item.getWurmId());
            target.insertItem(item, true);
            item.setLastOwnerId(performer.getWurmId());
            item.setMailed(false);
            logger.log(Level.INFO, performer.getName() + " received "
                    + item.getName() + " " + item.getWurmId());
        }
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
