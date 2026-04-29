package com.garward.wurmmodloader.mods.mission;

import com.garward.wurmmodloader.modloader.ReflectionUtil;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.players.PlayerInfo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static methods invoked from bytecode patched by {@link MissionPatches}.
 * Method signatures are part of the patch contract — do not change without
 * updating the matching $proceed/replace strings in MissionPatches.
 */
public final class MissionHooks {

    private static final Logger logger = Logger.getLogger(MissionHooks.class.getName());

    private MissionHooks() {}

    // ----- addMissionCurrencyReward -----

    /** Called from rewritten {@code TriggerEffect.effect}: {@code $0} of the
     *  intercepted {@code addToSleep} call is the {@link PlayerInfo}, so the
     *  reward goes to the player who completed the mission. */
    public static void awardMissionBonus(PlayerInfo info) {
        if (info == null) return;
        try {
            info.setMoney(info.money + 2000 + Server.rand.nextInt(2000));
        } catch (IOException e) {
            logger.log(Level.WARNING, "[mission] awardMissionBonus failed", e);
        }
    }

    // ----- additionalMissionSlayableChecks -----

    public static boolean isMissionOkaySlayable(CreatureTemplate template) {
        if (template == null) return false;
        if (template.isSubmerged()) return false;
        if (template.isUnique()) return false;
        return template.isEpicMissionSlayable();
    }

    // ----- additionalHerbivoreChecks -----

    public static boolean isMissionOkayHerbivore(CreatureTemplate template) {
        if (template == null) return false;
        if (template.isSubmerged()) return false;
        if (template.isUnique()) return false;
        return template.isHerbivore();
    }

    // ----- enableMissionPoll -----

    /** Periodic mission rotation. Drops expired/completed missions, then
     *  generates one new mission for an entity that doesn't currently have
     *  one. Original-Gods (1-4) only — Valrei entities (6-12) need
     *  WyvernMods.useValreiEntities which we haven't ported. */
    public static void pollMissions() {
        int[] deityNums = {1, 2, 3, 4};

        EpicServerStatus es = new EpicServerStatus();
        EpicMission[] missions = EpicServerStatus.getCurrentEpicMissions();
        for (EpicMission mission : missions) {
            if (!mission.isCurrent()) continue;
            if (mission.isCompleted() || mission.getEndTime() <= System.currentTimeMillis()) {
                int entityId = mission.getEpicEntityId();
                logger.info("[mission] expired/completed mission for "
                        + Deities.getDeityName(entityId) + ", removing.");
                destroyLastMissionForEntity(entityId);
            }
        }

        if (EpicServerStatus.getCurrentEpicMissions().length >= deityNums.length) {
            return;
        }

        int attempts = 10;
        int number = -1;
        while (attempts-- > 0) {
            int candidate = deityNums[Server.rand.nextInt(deityNums.length)];
            if (EpicServerStatus.getEpicMissionForEntity(candidate) == null) {
                number = candidate;
                break;
            }
        }
        if (number < 0) {
            logger.info("[mission] could not find an entity without a mission after 10 tries");
            return;
        }

        String entityName = Deities.getDeityName(number);
        int durationSeconds = 604_800;
        if (EpicServerStatus.getCurrentScenario() != null) {
            logger.info("[mission] creating mission for " + entityName);
            es.generateNewMissionForEpicEntity(
                    number, entityName, -1, durationSeconds,
                    EpicServerStatus.getCurrentScenario().getScenarioName(),
                    EpicServerStatus.getCurrentScenario().getScenarioNumber(),
                    EpicServerStatus.getCurrentScenario().getScenarioQuest(),
                    true);
        }
    }

    static void destroyLastMissionForEntity(int entityId) {
        try {
            ReflectionUtil.callPrivateMethod(EpicServerStatus.class,
                    ReflectionUtil.getMethod(EpicServerStatus.class, "destroyLastMissionForEntity"),
                    entityId);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.log(Level.WARNING, "[mission] destroyLastMissionForEntity failed for "
                    + entityId, e);
        }
    }
}
