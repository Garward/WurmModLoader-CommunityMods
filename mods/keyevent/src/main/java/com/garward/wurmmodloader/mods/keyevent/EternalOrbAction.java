package com.garward.wurmmodloader.mods.keyevent;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;
import com.garward.wurmmodloader.modsupport.actions.BehaviourProvider;
import com.garward.wurmmodloader.modsupport.actions.ModAction;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.spells.SpellEffect;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activate an eternal orb on an enchanted item to consume that item's
 * enchants into a freshly-spawned enchant orb. The eternal orb is consumed.
 * Direct port of {@code mod.sin.actions.items.EternalOrbAction}; the
 * arrowHunting/arrowWar block and the type-120 immunity check are
 * preserved.
 */
public class EternalOrbAction implements ModAction {

    private static final Logger logger = Logger.getLogger(EternalOrbAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;

    public EternalOrbAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Absorb enchants",
                "absorbing",
                new int[0]);
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object) {
                if (performer instanceof Player && source != null && object != null
                        && source.getTemplateId() == KeyEventTemplates.eternalOrbTemplateId
                        && source != object) {
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
            public boolean action(Action act, Creature performer, Item source, Item target,
                                  short action, float counter) {
                if (!(performer instanceof Player)) {
                    logger.info("[keyevent] non-player attempted EternalOrb absorb");
                    return true;
                }
                Player player = (Player) performer;
                if (source.getTemplate().getTemplateId() != KeyEventTemplates.eternalOrbTemplateId) {
                    player.getCommunicator().sendNormalServerMessage(
                            "You must use an Eternal Orb to absorb enchants.");
                    return true;
                }
                if (source.getWurmId() == target.getWurmId()) {
                    player.getCommunicator().sendNormalServerMessage("You cannot absorb the orb with itself!");
                    return true;
                }
                if (target.getTemplateId() == ItemList.arrowHunting
                        || target.getTemplateId() == ItemList.arrowWar) {
                    player.getCommunicator().sendNormalServerMessage("You cannot use Eternal Orbs on arrows.");
                    return true;
                }
                ItemSpellEffects teffs = target.getSpellEffects();
                if (teffs == null || teffs.getEffects().length == 0) {
                    player.getCommunicator().sendNormalServerMessage(
                            "The " + target.getTemplate().getName() + " has no enchants.");
                    return true;
                }
                for (SpellEffect eff : teffs.getEffects()) {
                    if (eff.type == 120) {
                        player.getCommunicator().sendNormalServerMessage(
                                "The " + eff.getName()
                                        + " enchant makes this item immune to the effects of the "
                                        + source.getName() + ".");
                        return true;
                    }
                }
                try {
                    Item enchantOrb = ItemFactory.createItem(
                            KeyEventTemplates.enchantOrbTemplateId, source.getCurrentQualityLevel(), "");
                    ItemSpellEffects effs = enchantOrb.getSpellEffects();
                    if (effs == null) {
                        effs = new ItemSpellEffects(enchantOrb.getWurmId());
                    }
                    for (SpellEffect teff : teffs.getEffects()) {
                        byte type = teff.type;
                        SpellEffect newEff = new SpellEffect(
                                enchantOrb.getWurmId(), type, teff.getPower(), 20000000);
                        effs.addSpellEffect(newEff);
                        teffs.removeSpellEffect(type);
                        player.getCommunicator().sendSafeServerMessage(
                                "The " + teff.getName() + " transfers to the "
                                        + enchantOrb.getTemplate().getName() + ".");
                        String tag = newEff.getName().substring(0, 1)
                                + String.format("%d", (int) newEff.getPower());
                        if (enchantOrb.getDescription().isEmpty()) {
                            enchantOrb.setDescription(tag);
                        } else {
                            enchantOrb.setDescription(enchantOrb.getDescription() + " " + tag);
                        }
                    }
                    performer.getInventory().insertItem(enchantOrb, true);
                    Items.destroyItem(source.getWurmId());
                } catch (FailedException | NoSuchTemplateException e) {
                    logger.log(Level.SEVERE, "[keyevent] failed to spawn enchant orb from eternal orb", e);
                }
                return true;
            }
        };
    }
}
