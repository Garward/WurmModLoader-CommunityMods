package com.garward.wurmmodloader.mods.crystals;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.garward.wurmmodloader.modsupport.actions.BehaviourProvider;
import com.garward.wurmmodloader.modsupport.actions.ModAction;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrystalCombineAction implements ModAction {

    private static final Logger logger = Logger.getLogger(CrystalCombineAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public CrystalCombineAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Combine",
                "combining",
                new int[] { Actions.ACTION_TYPE_NOMOVE });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object) {
                if (performer instanceof Player && source != null && object != null
                        && CrystalsHelper.isCrystal(source) && CrystalsHelper.isCrystal(object)
                        && source != object && source.getTemplateId() == object.getTemplateId()) {
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
            public short getActionId() { return actionId; }

            @Override
            public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
                try {
                    if (!(performer instanceof Player)) {
                        return false;
                    }
                    if (CrystalsHelper.shouldCancelCombine(performer, source, target)) {
                        return true;
                    }
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You begin to combine the crystals together.");
                        Server.getInstance().broadCastAction(performer.getName() + " begins combining crystals.", performer, 5);
                        Skill combineSkill = performer.getSkills().getSkill(SkillList.MIND_LOGICAL);
                        int time = Actions.getStandardActionTime(performer, combineSkill, source, 0d);
                        act.setTimeLeft(time);
                        performer.sendActionControl("Combining", true, act.getTimeLeft());
                    } else if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                        double diff = (source.getCurrentQualityLevel() + target.getCurrentQualityLevel()) * 0.3d;
                        diff += source.getRarity() * 20;
                        diff -= performer.getSkills().getSkill(SkillList.MIND).getKnowledge();
                        double power = performer.getSkills().getSkill(SkillList.SOUL).skillCheck(diff, source, 0d, false, 1);
                        if (power > 0) {
                            performer.getCommunicator().sendNormalServerMessage("You successfully combine the crystals!");
                            Server.getInstance().broadCastAction(performer.getName() + " successfully combines the crystals!", performer, 5);
                            if (source.getCurrentQualityLevel() + target.getCurrentQualityLevel() > 100f) {
                                float newQuality = (source.getCurrentQualityLevel() + target.getCurrentQualityLevel()) - 100f;
                                performer.getCommunicator().sendNormalServerMessage("The crystals combine and change, creating a more powerful component.");
                                Items.destroyItem(source.getWurmId());
                                target.setQualityLevel(newQuality);
                                target.setDamage(0);
                                target.setRarity((byte) (target.getRarity() + 1));
                            } else {
                                performer.getCommunicator().sendNormalServerMessage("The crystals combine together and reinforce.");
                                target.setQualityLevel(source.getCurrentQualityLevel() + target.getCurrentQualityLevel());
                                target.setDamage(0);
                                Items.destroyItem(source.getWurmId());
                            }
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You fail to combine the crystals, damaging them both.");
                            float dam1 = (float) (Server.rand.nextFloat() * power * 0.2f);
                            float dam2 = (float) (Server.rand.nextFloat() * power * 0.2f);
                            source.setDamage((float) (source.getDamage() - dam1));
                            target.setDamage((float) (target.getDamage() - dam2));
                        }
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[crystals] combine failed", e);
                    return true;
                }
            }
        };
    }
}
