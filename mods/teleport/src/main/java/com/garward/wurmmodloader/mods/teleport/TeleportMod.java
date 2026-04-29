package com.garward.wurmmodloader.mods.teleport;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.player.BodyMenuPopulateEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ActionEntryBuilder;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.wurmonline.server.Servers;

import java.util.Properties;
import java.util.logging.Logger;

public class TeleportMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(TeleportMod.class.getName());

    boolean useArenaTeleportMethod = true;
    boolean actionArenaTeleports = true;
    boolean actionVillageTeleport = false;

    static int actionVillageTeleportId = -1;
    static int actionArenaTeleportId = -1;
    static int actionArenaEscapeId = -1;

    @Override
    public void configure(Properties properties) {
        useArenaTeleportMethod = bool(properties, "useArenaTeleportMethod", useArenaTeleportMethod);
        actionArenaTeleports = bool(properties, "actionArenaTeleports", actionArenaTeleports);
        actionVillageTeleport = bool(properties, "actionVillageTeleport", actionVillageTeleport);
    }

    @Override
    public void preInit() {
        if (useArenaTeleportMethod) {
            TeleportPatches.installArenaSpawnRedirect();
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        ModActions.init();

        if (actionVillageTeleport) {
            actionVillageTeleportId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) actionVillageTeleportId,
                    "Village Teleport",
                    "teleporting"
            ).build());
            ModActions.registerActionPerformer(new VillageTeleportPerformer());
            logger.info("[teleport] registered VillageTeleport action (id=" + actionVillageTeleportId + ")");
        }

        if (actionArenaTeleports) {
            actionArenaTeleportId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) actionArenaTeleportId,
                    "Teleport to Arena",
                    "preparing for combat"
            ).build());
            ModActions.registerActionPerformer(new ArenaTeleportPerformer());

            actionArenaEscapeId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) actionArenaEscapeId,
                    "Escape the Arena",
                    "escaping"
            ).build());
            ModActions.registerActionPerformer(new ArenaEscapePerformer());

            logger.info("[teleport] registered ArenaTeleport (id=" + actionArenaTeleportId
                    + ") + ArenaEscape (id=" + actionArenaEscapeId + ")");
        }
    }

    @SubscribeEvent
    public void onBodyMenuPopulate(BodyMenuPopulateEvent event) {
        if (!event.isOwnBody() || !event.isBodyAttached()) return;

        boolean isPvp = Servers.localServer.PVPSERVER;

        if (actionVillageTeleport
                && actionVillageTeleportId > 0
                && !isPvp
                && event.getPerformer().getCitizenVillage() != null) {
            event.addMenuItem(ModActions.getAction((short) actionVillageTeleportId));
        }

        if (actionArenaTeleports && actionArenaTeleportId > 0 && !isPvp) {
            event.addMenuItem(ModActions.getAction((short) actionArenaTeleportId));
        }

        if (actionArenaTeleports && actionArenaEscapeId > 0 && isPvp) {
            event.addMenuItem(ModActions.getAction((short) actionArenaEscapeId));
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
