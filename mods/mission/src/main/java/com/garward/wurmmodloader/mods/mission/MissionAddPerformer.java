package com.garward.wurmmodloader.mods.mission;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Logger;

/** GM body-menu action: pick a random Original-Gods or Valrei entity that
 *  has no current epic mission and create one for it. Power &gt;= 5 gated. */
class MissionAddPerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(MissionAddPerformer.class.getName());

    private static final int[] ENTITY_NUMBERS = {1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12};

    @Override
    public short getActionId() {
        return (short) MissionMod.missionAddId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        if (!(performer instanceof Player)) return true;
        if (performer.getPower() < 5) {
            performer.getCommunicator().sendNormalServerMessage(
                    "You do not have permission to do that.");
            return true;
        }

        if (EpicServerStatus.getCurrentEpicMissions().length >= ENTITY_NUMBERS.length) {
            performer.getCommunicator().sendNormalServerMessage(
                    "All entities already have a mission.");
            return true;
        }

        int number = -1;
        for (int attempt = 0; attempt < 10; attempt++) {
            int candidate = ENTITY_NUMBERS[Server.rand.nextInt(ENTITY_NUMBERS.length)];
            if (EpicServerStatus.getEpicMissionForEntity(candidate) == null) {
                number = candidate;
                break;
            }
        }
        if (number < 0) {
            performer.getCommunicator().sendNormalServerMessage(
                    "Could not find an entity without a mission after 10 tries.");
            return true;
        }

        String entityName = Deities.getDeityName(number);
        int durationSeconds = 604_800;
        if (EpicServerStatus.getCurrentScenario() == null) {
            performer.getCommunicator().sendNormalServerMessage(
                    "No current Epic scenario; cannot create a mission.");
            return true;
        }
        EpicServerStatus es = new EpicServerStatus();
        es.generateNewMissionForEpicEntity(
                number, entityName, -1, durationSeconds,
                EpicServerStatus.getCurrentScenario().getScenarioName(),
                EpicServerStatus.getCurrentScenario().getScenarioNumber(),
                EpicServerStatus.getCurrentScenario().getScenarioQuest(),
                true);
        performer.getCommunicator().sendNormalServerMessage(
                "Created an epic mission for " + entityName + ".");
        logger.info("[mission] GM " + performer.getName() + " created mission for "
                + entityName + " (" + number + ")");
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target,
                          short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
