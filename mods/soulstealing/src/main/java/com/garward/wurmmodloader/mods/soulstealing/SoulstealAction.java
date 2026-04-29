package com.garward.wurmmodloader.mods.soulstealing;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.garward.wurmmodloader.modsupport.actions.BehaviourProvider;
import com.garward.wurmmodloader.modsupport.actions.ModAction;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoulstealAction implements ModAction {

    private static final Logger logger = Logger.getLogger(SoulstealAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public SoulstealAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Soulsteal",
                "soulstealing",
                new int[] { Actions.ACTION_TYPE_IGNORERANGE });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object) {
                if (performer instanceof Player && source != null && object != null
                        && source.getTemplateId() == ItemList.sacrificialKnife
                        && object.getTemplateId() == ItemList.corpse) {
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
            public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
                try {
                    if (!(performer instanceof Player)) {
                        return false;
                    }
                    Player player = (Player) performer;
                    if (source.getTemplate().getTemplateId() != ItemList.sacrificialKnife) {
                        player.getCommunicator().sendNormalServerMessage("You must use a sacrificial knife to steal souls.");
                        return true;
                    }
                    if (target.getTemplate().getTemplateId() != ItemList.corpse) {
                        player.getCommunicator().sendNormalServerMessage("You can only steal the soul from a corpse.");
                        return true;
                    }
                    if (target.getData1() == 1
                            && target.getLastOwnerId() != performer.getWurmId()
                            && !Servers.isThisAPvpServer()) {
                        player.getCommunicator().sendNormalServerMessage(Action.NOT_ALLOWED_ACTION_ON_FREEDOM_MESSAGE);
                        return true;
                    }
                    if (target.isButchered()) {
                        player.getCommunicator().sendNormalServerMessage("The corpse has been butchered and there is no soul left.");
                        return true;
                    }
                    if (target.getTopParentOrNull() != performer.getInventory()
                            && !Methods.isActionAllowed(performer, (short) 120, target)) {
                        player.getCommunicator().sendNormalServerMessage("You are not allowed to soulsteal that.");
                        return true;
                    }
                    if (!performer.isWithinDistanceTo(target, 5)) {
                        player.getCommunicator().sendNormalServerMessage("You are too far away to steal that soul.");
                        return true;
                    }
                    if (counter == 1.0f) {
                        CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                        performer.getCommunicator().sendNormalServerMessage("You begin to steal the soul of the " + template.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " begins to steal the " + template.getName() + " soul.", performer, 5);
                        Skill stealing = performer.getStealSkill();
                        int time = Actions.getStandardActionTime(performer, stealing, source, 0d);
                        act.setTimeLeft(time);
                        performer.sendActionControl("Soulstealing", true, act.getTimeLeft());
                    } else if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                        CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                        Skill stealing = performer.getStealSkill();
                        double power = stealing.skillCheck(40f - (template.getBaseCombatRating() * 0.4f), source, 0f, false, 1);
                        if (power > 0) {
                            if (SoulstealingTemplates.soulId <= 0) {
                                player.getCommunicator().sendNormalServerMessage("The soul slips away — no vessel exists to capture it.");
                                logger.warning("[soulstealing] soul template id missing, cannot create soul item");
                                return true;
                            }
                            Item soul = ItemFactory.createItem(SoulstealingTemplates.soulId, (float) power, performer.getName());
                            soul.setName("Soul of " + template.getName());
                            performer.getInventory().insertItem(soul, true);
                            performer.getCommunicator().sendNormalServerMessage("You obtain the soul of the " + template.getName() + ".");
                            Server.getInstance().broadCastAction(performer.getName() + " obtains the soul of the " + template.getName() + ".", performer, 5);
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You fail to steal the soul of the " + template.getName() + ".");
                            Server.getInstance().broadCastAction(performer.getName() + " fails to steal the soul of the " + template.getName() + ".", performer, 5);
                        }
                        source.setDamage(source.getDamage() + (0.001f * source.getDamageModifier()));
                        Items.destroyItem(target.getWurmId());
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[soulstealing] soulsteal failed", e);
                    return true;
                }
            }
        };
    }
}
