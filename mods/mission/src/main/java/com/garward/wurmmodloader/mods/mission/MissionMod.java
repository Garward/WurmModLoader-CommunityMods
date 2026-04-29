package com.garward.wurmmodloader.mods.mission;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.player.BodyMenuPopulateEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ActionEntryBuilder;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Epic mission tweaks ported from Sindusk's WyvernMods MissionCreator plus
 * the MissionAdd / MissionRemove GM body-menu actions. Per
 * memory:feedback_body_menu_event_over_behaviourprovider.md the actions are
 * implemented as bare {@link com.garward.wurmmodloader.modsupport.actions.ActionPerformer}
 * + {@link BodyMenuPopulateEvent} subscribers instead of the upstream
 * {@code ModAction}/{@code BehaviourProvider} wrapper.
 */
public class MissionMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(MissionMod.class.getName());

    boolean addMissionCurrencyReward = true;
    boolean preventMissionOceanSpawns = true;
    boolean additionalHerbivoreChecks = true;
    boolean additionalMissionSlayableChecks = true;
    boolean disableEpicMissionTypes = true;

    private boolean enableMissionPoll = false;
    private long pollIntervalMillis = 3_600_000L;
    private long lastPolled = 0L;

    private boolean enableMissionAddAction = false;
    private boolean enableMissionRemoveAction = false;

    static int missionAddId = -1;
    static int missionRemoveId = -1;

    @Override
    public void configure(Properties properties) {
        addMissionCurrencyReward         = bool(properties, "addMissionCurrencyReward",         addMissionCurrencyReward);
        preventMissionOceanSpawns        = bool(properties, "preventMissionOceanSpawns",        preventMissionOceanSpawns);
        additionalHerbivoreChecks        = bool(properties, "additionalHerbivoreChecks",        additionalHerbivoreChecks);
        additionalMissionSlayableChecks  = bool(properties, "additionalMissionSlayableChecks",  additionalMissionSlayableChecks);
        disableEpicMissionTypes          = bool(properties, "disableEpicMissionTypes",          disableEpicMissionTypes);

        enableMissionPoll                = bool(properties, "enableMissionPoll",                enableMissionPoll);
        String raw = properties.getProperty("pollIntervalSeconds");
        if (raw != null) {
            try {
                long seconds = Long.parseLong(raw.trim());
                if (seconds > 0) pollIntervalMillis = seconds * 1000L;
            } catch (NumberFormatException e) {
                logger.warning("[mission] pollIntervalSeconds must be a positive integer, got: " + raw);
            }
        }

        enableMissionAddAction    = bool(properties, "enableMissionAddAction",    enableMissionAddAction);
        enableMissionRemoveAction = bool(properties, "enableMissionRemoveAction", enableMissionRemoveAction);
    }

    @Override
    public void preInit() {
        MissionPatches.install(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!enableMissionAddAction && !enableMissionRemoveAction) return;

        ModActions.init();

        if (enableMissionAddAction) {
            missionAddId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) missionAddId,
                    "Add Epic Mission",
                    "generating"
            ).build());
            ModActions.registerActionPerformer(new MissionAddPerformer());
            logger.info("[mission] registered MissionAdd (id=" + missionAddId + ")");
        }

        if (enableMissionRemoveAction) {
            missionRemoveId = ModActions.getNextActionId();
            ModActions.registerAction(new ActionEntryBuilder(
                    (short) missionRemoveId,
                    "Remove Epic Mission",
                    "removing"
            ).build());
            ModActions.registerActionPerformer(new MissionRemovePerformer());
            logger.info("[mission] registered MissionRemove (id=" + missionRemoveId + ")");
        }
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (!enableMissionPoll) return;
        long now = System.currentTimeMillis();
        if (now < lastPolled + pollIntervalMillis) return;
        lastPolled = now;
        try {
            MissionHooks.pollMissions();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[mission] poll tick failed", t);
        }
    }

    @SubscribeEvent
    public void onBodyMenuPopulate(BodyMenuPopulateEvent event) {
        if (!event.isOwnBody() || !event.isBodyAttached()) return;
        if (event.getPerformer().getPower() < 5) return;

        if (enableMissionAddAction && missionAddId > 0) {
            event.addMenuItem(ModActions.getAction((short) missionAddId));
        }
        if (enableMissionRemoveAction && missionRemoveId > 0) {
            event.addMenuItem(ModActions.getAction((short) missionRemoveId));
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
