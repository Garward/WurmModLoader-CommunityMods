package com.garward.wurmmodloader.mods.caches;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.garward.wurmmodloader.modsupport.actions.BehaviourProvider;
import com.garward.wurmmodloader.modsupport.actions.ModAction;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Right-click action that surfaces on any registered treasure cache. Two-stage
 * timer (begin → finish) follows the upstream Wyvern flow; on completion the
 * cache item is destroyed and {@link CacheLoot#openCache} drops loot directly
 * into the performer's inventory.
 */
public class TreasureCacheOpenAction implements ModAction {

    private static final Logger logger = Logger.getLogger(TreasureCacheOpenAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public TreasureCacheOpenAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Open cache",
                "opening",
                new int[] { 6 /* ACTION_TYPE_NOMOVE */ });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object) {
                return getBehavioursFor(performer, object);
            }

            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item object) {
                if (performer instanceof Player && object != null && CacheLoot.isTreasureCache(object)) {
                    return Arrays.asList(actionEntry);
                }
                return null;
            }
        };
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            @Override
            public short getActionId() { return actionId; }

            @Override
            public boolean action(Action act, Creature performer, Item target, short action, float counter) {
                try {
                    if (!(performer instanceof Player)) {
                        return true;
                    }
                    if (!CacheLoot.isTreasureCache(target)) {
                        performer.getCommunicator().sendNormalServerMessage("That is not a treasure cache.");
                        return true;
                    }
                    if (target.getLastOwnerId() != performer.getWurmId()
                            && target.getOwnerId() != performer.getWurmId()) {
                        performer.getCommunicator().sendNormalServerMessage(
                                "You must own the " + target.getName() + " to open it.");
                        return true;
                    }
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage(
                                "You begin to open the " + target.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName()
                                + " begins opening " + performer.getHisHerItsString() + " "
                                + target.getName() + ".", performer, 5);
                        act.setTimeLeft(50);
                        performer.sendActionControl("Opening", true, act.getTimeLeft());
                    } else if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                        performer.getCommunicator().sendNormalServerMessage(
                                "You open your " + target.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName()
                                + " opens " + performer.getHisHerItsString() + " "
                                + target.getName() + ".", performer, 5);
                        logger.info("[caches] " + performer.getName() + " opened "
                                + target.getName() + " ql=" + target.getCurrentQualityLevel()
                                + " rarity=" + target.getRarity());
                        CacheLoot.openCache(performer, target);
                        Items.destroyItem(target.getWurmId());
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[caches] cache open failed", e);
                    return true;
                }
            }

            @Override
            public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
                return action(act, performer, target, action, counter);
            }
        };
    }
}
