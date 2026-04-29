package com.garward.wurmmodloader.mods.keyevent;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.garward.wurmmodloader.modsupport.actions.BehaviourProvider;
import com.garward.wurmmodloader.modsupport.actions.ModAction;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.AffinityOrbQuestion;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Activate an affinity orb to open a BML chooser of 10 random skill names
 * (deterministic on the orb's auxData seed). Selecting one consumes the
 * orb and grants an affinity in that skill, capped at level 5. Direct port
 * of {@code mod.sin.actions.items.AffinityOrbAction}.
 */
public class AffinityOrbAction implements ModAction {

    private static final Logger logger = Logger.getLogger(AffinityOrbAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public AffinityOrbAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Gain affinity",
                "infusing",
                new int[] { 6 /* ACTION_TYPE_NOMOVE */ });
        ModActions.registerAction(actionEntry);
    }

    public short getActionId() {
        return actionId;
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
                        && object.getTemplateId() == KeyEventTemplates.affinityOrbTemplateId) {
                    return Collections.singletonList(actionEntry);
                }
                return null;
            }
        };
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return new ActionPerformer() {
            @Override
            public short getActionId() {
                return actionId;
            }

            @Override
            public boolean action(Action act, Creature performer, Item target,
                                  short action, float counter) {
                if (!(performer instanceof Player)) {
                    logger.info("[keyevent] non-player attempted Gain affinity");
                    return true;
                }
                if (target.getTemplate().getTemplateId() != KeyEventTemplates.affinityOrbTemplateId) {
                    performer.getCommunicator().sendSafeServerMessage(
                            "You must use an Affinity Orb to be infused.");
                    return true;
                }
                AffinityOrbQuestion aoq = new AffinityOrbQuestion(
                        performer, "Affinity Orb",
                        "Which affinity would you like to receive?",
                        performer.getWurmId(), target);
                aoq.sendQuestion();
                return true;
            }

            @Override
            public boolean action(Action act, Creature performer, Item source, Item target,
                                  short action, float counter) {
                return this.action(act, performer, target, action, counter);
            }
        };
    }
}
