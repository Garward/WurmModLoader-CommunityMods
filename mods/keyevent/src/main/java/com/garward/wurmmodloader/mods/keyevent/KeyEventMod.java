package com.garward.wurmmodloader.mods.keyevent;

import com.garward.wurmmodloader.api.events.action.ItemMenuBuildEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.player.PlayerMessageEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * "Call upon the heavens" ritual + tomes (key fragments, enchant orbs,
 * eternal orbs, affinity orbs). Ported from Sindusk's WyvernMods.
 *
 * <p>Templates register on {@link ItemTemplatesCreatedEvent}; actions
 * register on {@link ServerStartedEvent}; player chat lines from the
 * casting player feed {@link KeyEventState#handlePlayerMessage} for the
 * duration of the ritual.
 *
 * <p>Other submods that want to opt in to enchant-orb drops (e.g.
 * {@code supplydepot}) read this submod's enchant-orb id from the boot
 * log and pin their own config at it.
 */
public class KeyEventMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(KeyEventMod.class.getName());

    private final KeyEventConfig cfg = new KeyEventConfig();
    private KeyCombinationAction keyCombinationAction;
    private EnchantOrbAction enchantOrbAction;
    private EternalOrbAction eternalOrbAction;
    private AffinityOrbAction affinityOrbAction;

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        logger.info("[keyevent] enabled=" + cfg.enabled
                + " fragmentsRequired=" + cfg.fragmentsRequired);
    }

    @SubscribeEvent
    public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
        if (!cfg.enabled) return;
        try {
            KeyEventTemplates.register();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[keyevent] failed to register item templates", t);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!cfg.enabled) return;
        try {
            ModActions.init();
            keyCombinationAction = new KeyCombinationAction(cfg);
            enchantOrbAction = new EnchantOrbAction();
            eternalOrbAction = new EternalOrbAction();
            affinityOrbAction = new AffinityOrbAction();
            ModActions.registerAction(keyCombinationAction);
            ModActions.registerAction(enchantOrbAction);
            ModActions.registerAction(eternalOrbAction);
            ModActions.registerAction(affinityOrbAction);
            KeyEventState.preInit();
            logger.info("[keyevent] actions registered");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[keyevent] failed to register actions", t);
        }
    }

    @SubscribeEvent
    public void onPlayerMessage(PlayerMessageEvent event) {
        if (!cfg.enabled || !KeyEventState.isActive()) return;
        Object commObj = event.getCommunicator();
        if (!(commObj instanceof Communicator)) return;
        Creature sender = ((Communicator) commObj).getPlayer();
        if (sender == null || sender != KeyEventState.getPerformer()) return;
        // Upstream stripped the leading "<name>: " prefix. PlayerMessageEvent
        // delivers the raw message body, so feed it directly.
        KeyEventState.handlePlayerMessage(sender, event.getMessage());
    }

    @SubscribeEvent
    public void onItemMenuBuild(ItemMenuBuildEvent event) {
        if (!cfg.enabled) return;
        try {
            Item target = com.wurmonline.server.Items.getItem(event.getTargetId());
            if (target == null) return;
            int tpl = target.getTemplateId();
            // ItemMenuBuildEvent is the framework's hook for adding actions
            // when no source item is activated. Activated-source flows go
            // through the BehaviourProvider path on each ModAction.
            if (tpl == KeyEventTemplates.keyFragmentTemplateId && keyCombinationAction != null) {
                event.getAvailableActions().add(
                        ModActions.getAction(keyCombinationAction.getActionId()));
            } else if (tpl == KeyEventTemplates.affinityOrbTemplateId && affinityOrbAction != null) {
                event.getAvailableActions().add(
                        ModActions.getAction(affinityOrbAction.getActionId()));
            }
        } catch (NoSuchItemException ignored) {
            // target id no longer resolves; nothing to do
        }
    }
}
