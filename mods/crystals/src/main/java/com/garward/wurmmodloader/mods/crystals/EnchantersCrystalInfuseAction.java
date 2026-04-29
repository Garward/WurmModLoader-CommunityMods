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
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnchantersCrystalInfuseAction implements ModAction {

    private static final Logger logger = Logger.getLogger(EnchantersCrystalInfuseAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public EnchantersCrystalInfuseAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Infuse",
                "infusing",
                new int[] { Actions.ACTION_TYPE_NOMOVE });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object) {
                if (performer instanceof Player && source != null && object != null
                        && CrystalsTemplates.enchantersCrystalId > 0
                        && source.getTemplateId() == CrystalsTemplates.enchantersCrystalId
                        && object.getTemplateId() != CrystalsTemplates.enchantersCrystalId) {
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
                    if (source.getTemplate().getTemplateId() != CrystalsTemplates.enchantersCrystalId) {
                        performer.getCommunicator().sendNormalServerMessage("You must use an enchanters crystal to infuse an item.");
                        return true;
                    }
                    if (CrystalsHelper.shouldCancelEnchantersInfusion(performer, target)) {
                        return true;
                    }
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage(
                                "You begin to infuse the " + target.getName() + " with the " + source.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " begins infusing with a " + source.getName() + ".", performer, 5);
                        act.setTimeLeft(300);
                        performer.sendActionControl("Infusing", true, act.getTimeLeft());
                    } else if (counter * 10f > performer.getCurrentAction().getTimeLeft()) {
                        double diff = CrystalsHelper.getEnchantersInfusionDifficulty(performer, source, target);
                        double power = -100;
                        int i = source.getRarity();
                        while (i >= 0) {
                            power = Math.max(power, performer.getSkills().getSkill(SkillList.SOUL).skillCheck(diff, source, 0d, false, 1));
                            i--;
                        }
                        ItemSpellEffects effs = target.getSpellEffects();
                        if (power > 90) {
                            byte ench = CrystalsHelper.getNewRandomEnchant(target);
                            if (ench != -10) {
                                performer.getCommunicator().sendNormalServerMessage(
                                        "You handle the crystals expertly and infuse the " + target.getName() + ", adding a new enchant!");
                                SpellEffect eff = new SpellEffect(target.getWurmId(), ench, (float) power * Server.rand.nextFloat(), 20000000);
                                effs.addSpellEffect(eff);
                                Items.destroyItem(source.getWurmId());
                            } else {
                                logger.info("[crystals] no valid enchant slot for " + target.getName() + " (player " + performer.getName() + ")");
                                performer.getCommunicator().sendNormalServerMessage("Nothing happens!");
                            }
                        } else if (power > 75) {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "You very carefully infuse the metal " + target.getName() + ", increasing its magical properties!");
                            for (SpellEffect eff : effs.getEffects()) {
                                eff.setPower(eff.getPower() + (eff.getPower() * Server.rand.nextFloat() * 0.2f));
                            }
                            Items.destroyItem(source.getWurmId());
                        } else if (power > 60) {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "You carefully infuse the " + target.getName() + ", changing one of its magical properties!");
                            SpellEffect oldEff = effs.getEffects()[Server.rand.nextInt(effs.getEffects().length)];
                            float oldPower = oldEff.getPower();
                            if (oldEff.type == Enchants.BUFF_BLOODTHIRST) {
                                oldPower *= 0.01f;
                            }
                            effs.removeSpellEffect(oldEff.type);
                            byte ench = CrystalsHelper.getNewRandomEnchant(target);
                            if (ench != -10) {
                                SpellEffect eff = new SpellEffect(target.getWurmId(), ench, oldPower, 20000000);
                                effs.addSpellEffect(eff);
                            } else {
                                performer.getCommunicator().sendNormalServerMessage(
                                        "However, something goes wrong and the " + target.getName() + " instead loses the property!");
                            }
                            Items.destroyItem(source.getWurmId());
                        } else if (power > 35) {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "You manage to infuse the " + target.getName() + ", shifting its magical properties.");
                            for (SpellEffect eff : effs.getEffects()) {
                                eff.setPower(eff.getPower() + ((eff.getPower() * Server.rand.nextFloat() * 0.3f) * (Server.rand.nextBoolean() ? 1 : -1)));
                            }
                            Items.destroyItem(source.getWurmId());
                        } else if (power > 0) {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "You barely manage to infuse the " + target.getName() + ", destroying a magical property but increasing the rest.");
                            SpellEffect oldEff = effs.getEffects()[Server.rand.nextInt(effs.getEffects().length)];
                            effs.removeSpellEffect(oldEff.type);
                            if (effs.getEffects().length >= 1) {
                                for (SpellEffect eff : effs.getEffects()) {
                                    eff.setPower(eff.getPower() + (eff.getPower() * Server.rand.nextFloat() * 0.2f));
                                }
                            } else {
                                performer.getCommunicator().sendNormalServerMessage(
                                        "However, the " + target.getName() + " does not have any other properties, and the effect is wasted!");
                            }
                            Items.destroyItem(source.getWurmId());
                        } else if (power > -30) {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "You barely fail to infuse the " + target.getName() + ", reducing the power of its magical properties.");
                            for (SpellEffect eff : effs.getEffects()) {
                                eff.setPower(eff.getPower() - (eff.getPower() * Server.rand.nextFloat() * 0.2f));
                            }
                            Items.destroyItem(source.getWurmId());
                        } else if (power > -60) {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "You horribly fail to infuse the " + target.getName() + ", removing one of its magical properties.");
                            SpellEffect oldEff = effs.getEffects()[Server.rand.nextInt(effs.getEffects().length)];
                            effs.removeSpellEffect(oldEff.type);
                            Items.destroyItem(source.getWurmId());
                        } else {
                            performer.getCommunicator().sendNormalServerMessage(
                                    "The infusion fails catastrophically, destroying all the magic on the " + target.getName() + "!");
                            for (SpellEffect eff : effs.getEffects()) {
                                effs.removeSpellEffect(eff.type);
                            }
                            Items.destroyItem(source.getWurmId());
                        }
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[crystals] enchanters crystal infusion failed", e);
                    return true;
                }
            }
        };
    }
}
