package com.garward.wurmmodloader.mods.extraactions;

import com.garward.wurmmodloader.api.events.action.ItemMenuBuildEvent;
import com.garward.wurmmodloader.api.events.action.TileMenuBuildEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.player.BodyMenuPopulateEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ActionEntryBuilder;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;

import java.util.Properties;
import java.util.logging.Logger;

public class ExtraActionsMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(ExtraActionsMod.class.getName());

    private boolean unequipAllArmour = false;
    private boolean receiveAllMail = false;
    private boolean smoothTerrain = false;
    private boolean creatureReport = false;

    static int unequipAllId = -1;
    static int receiveMailId = -1;
    static int smoothTerrainId = -1;
    static int creatureReportId = -1;

    @Override
    public void configure(Properties properties) {
        unequipAllArmour = bool(properties, "unequipAllArmour", unequipAllArmour);
        receiveAllMail   = bool(properties, "receiveAllMail",   receiveAllMail);
        smoothTerrain    = bool(properties, "smoothTerrain",    smoothTerrain);
        creatureReport   = bool(properties, "creatureReport",   creatureReport);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        ModActions.init();

        if (unequipAllArmour) {
            unequipAllId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) unequipAllId,
                    "Unequip all armour",
                    "unequipping"
            ).build());
            ModActions.registerActionPerformer(new UnequipAllPerformer());
            logger.info("[extraactions] registered UnequipAll (id=" + unequipAllId + ")");
        }

        if (receiveAllMail) {
            receiveMailId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) receiveMailId,
                    "Receive all mail",
                    "receiving"
            ).build());
            ModActions.registerActionPerformer(new ReceiveMailPerformer());
            logger.info("[extraactions] registered ReceiveMail (id=" + receiveMailId + ")");
        }

        if (smoothTerrain) {
            smoothTerrainId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) smoothTerrainId,
                    "Smooth Terrain",
                    "smoothing"
            ).build());
            ModActions.registerActionPerformer(new SmoothTerrainPerformer());
            logger.info("[extraactions] registered SmoothTerrain (id=" + smoothTerrainId + ")");
        }

        if (creatureReport) {
            creatureReportId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) creatureReportId,
                    "Creature Report",
                    "reporting"
            ).build());
            ModActions.registerActionPerformer(new CreatureReportPerformer());
            logger.info("[extraactions] registered CreatureReport (id=" + creatureReportId + ")");
        }
    }

    // ----- body-menu (own attached body) -----

    @SubscribeEvent
    public void onBodyMenuPopulate(BodyMenuPopulateEvent event) {
        if (!event.isOwnBody() || !event.isBodyAttached()) return;

        if (unequipAllArmour && unequipAllId > 0) {
            event.addMenuItem(ModActions.getAction((short) unequipAllId));
        }
        if (creatureReport && creatureReportId > 0
                && event.getPerformer().getPower() >= 5) {
            event.addMenuItem(ModActions.getAction((short) creatureReportId));
        }
    }

    // ----- item-menu (mailboxes) -----

    @SubscribeEvent
    public void onItemMenuBuild(ItemMenuBuildEvent event) {
        if (!receiveAllMail || receiveMailId <= 0) return;
        Item target;
        try {
            target = com.wurmonline.server.Items.getItem(event.getTargetId());
        } catch (NoSuchItemException e) {
            return;
        }
        if (target == null) return;
        if (!target.isMailBox()) return;
        if (target.getSpellCourierBonus() <= 0f) return;
        event.getAvailableActions().add(ModActions.getAction((short) receiveMailId));
    }

    // ----- tile-menu (gm terrain smoothing) -----

    @SubscribeEvent
    public void onTileMenuBuild(TileMenuBuildEvent event) {
        if (!smoothTerrain || smoothTerrainId <= 0) return;
        if (event.getPerformer().getPower() < 5) return;
        event.getAvailableActions().add(ModActions.getAction((short) smoothTerrainId));
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
