package com.garward.wurmmodloader.mods.supplydepot;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Capture-depot ActionPerformer. Counter-driven: the first tick (counter
 * == 1.0f) starts the timer + broadcasts; subsequent ticks check against
 * the action's time-left to detect completion.
 */
final class CaptureDepotPerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(CaptureDepotPerformer.class.getName());
    private static final int CAPTURE_BROADCAST_RANGE = 50;

    private final SupplyDepotConfig cfg;

    CaptureDepotPerformer(SupplyDepotConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public short getActionId() {
        return (short) SupplyDepotMod.captureActionId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        return performCapture(act, performer, target, counter);
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        return performCapture(act, performer, target, counter);
    }

    private boolean performCapture(Action act, Creature performer, Item target, float counter) {
        try {
            if (!(performer instanceof Player)) return true;
            if (target == null || !DepotState.isSupplyDepot(target)) {
                performer.getCommunicator().sendNormalServerMessage("That is not a supply depot.");
                return true;
            }
            if (!performer.isWithinDistanceTo(target, cfg.captureRadius)) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You must be closer to capture the depot.");
                return true;
            }
            if (!Items.exists(target)) {
                performer.getCommunicator().sendNormalServerMessage(
                        "The supply depot has already been captured.");
                return true;
            }
            if (performer.getFightingSkill().getKnowledge() < cfg.fightingSkillRequirement) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You must have at least " + (int) cfg.fightingSkillRequirement
                                + " fighting skill to capture a depot.");
                return true;
            }

            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage(
                        "You begin to capture the depot.");
                Server.getInstance().broadCastAction(
                        performer.getName() + " begins capturing the depot.",
                        performer, CAPTURE_BROADCAST_RANGE);
                act.setTimeLeft(cfg.captureTimer);
                performer.sendActionControl("Capturing", true, act.getTimeLeft());
                maybeAnnounceBegin(performer);
                return false;
            }

            if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                DepotRewards.giveCaptureReward(performer, cfg);
                performer.getCommunicator().sendSafeServerMessage(
                        "You have successfully captured the depot!");
                Server.getInstance().broadCastAction(
                        performer.getName() + " successfully captures the depot!",
                        performer, CAPTURE_BROADCAST_RANGE);
                announceCapture(performer);
                DepotPoll.onDepotCaptured(target);
                Items.destroyItem(target.getWurmId());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.WARNING, "[supplydepot] capture action failed", e);
            return true;
        }
    }

    private void maybeAnnounceBegin(Creature performer) {
        long now = System.currentTimeMillis();
        if (now > DepotState.lastAttemptedDepotCapture + cfg.captureMessageInterval) {
            DepotState.lastAttemptedDepotCapture = now;
            String msg = performer.getName() + " is beginning to capture an Arena depot!";
            DepotBroadcast.serverTab("arena", msg, 255, 128, 0);
            DepotBroadcast.globalFreedomChat(performer, msg, 255, 128, 0);
        }
    }

    private void announceCapture(Creature performer) {
        String msg = performer.getName() + " has claimed an Arena depot!";
        DepotBroadcast.serverTab("arena", msg, 255, 128, 0);
        DepotBroadcast.globalFreedomChat(performer, msg, 255, 128, 0);
    }
}
