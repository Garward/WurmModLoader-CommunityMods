package com.garward.wurmmodloader.mods.soulstealing;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.garward.wurmmodloader.modsupport.actions.BehaviourProvider;
import com.garward.wurmmodloader.modsupport.actions.ModAction;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EternalReservoirCheckFuelAction implements ModAction {

    private static final Logger logger = Logger.getLogger(EternalReservoirCheckFuelAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public EternalReservoirCheckFuelAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Check fuel",
                "checking",
                new int[0]);
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
                if (performer instanceof Player && object != null
                        && SoulstealingTemplates.eternalReservoirId > 0
                        && object.getTemplateId() == SoulstealingTemplates.eternalReservoirId) {
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
                        return false;
                    }
                    if (target.getTemplateId() != SoulstealingTemplates.eternalReservoirId) {
                        performer.getCommunicator().sendNormalServerMessage("That is not an eternal reservoir.");
                        return true;
                    }
                    if (!performer.isWithinDistanceTo(target, 9)) {
                        performer.getCommunicator().sendNormalServerMessage("You are too far away to check the fuel.");
                        return true;
                    }
                    int fuel = target.getData1();
                    String name = target.getName();
                    String msg;
                    if (fuel < 30) {
                        msg = "The " + name + " has no souls, and is inactive.";
                    } else if (fuel < 1000) {
                        msg = "The " + name + " is very low on souls.";
                    } else if (fuel < 5000) {
                        msg = "The " + name + " has some souls, but yearns for more.";
                    } else if (fuel < 10000) {
                        msg = "The " + name + " has a good amount of souls.";
                    } else if (fuel < 50000) {
                        msg = "The " + name + " has plenty of souls.";
                    } else {
                        msg = "The " + name + " is absolutely flooded with souls, and will last a long time.";
                    }
                    performer.getCommunicator().sendNormalServerMessage(msg);
                    return true;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[soulstealing] check-fuel failed", e);
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
