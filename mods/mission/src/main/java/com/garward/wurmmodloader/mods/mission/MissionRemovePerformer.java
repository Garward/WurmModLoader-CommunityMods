package com.garward.wurmmodloader.mods.mission;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.logging.Logger;

/** GM body-menu action: remove the current epic mission for the first
 *  entity that has one. Power &gt;= 5 gated. */
class MissionRemovePerformer implements ActionPerformer {

    private static final Logger logger = Logger.getLogger(MissionRemovePerformer.class.getName());

    @Override
    public short getActionId() {
        return (short) MissionMod.missionRemoveId;
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

        EpicMission[] missions = EpicServerStatus.getCurrentEpicMissions();
        for (EpicMission mission : missions) {
            if (!mission.isCurrent()) continue;
            int entityId = mission.getEpicEntityId();
            String entityName = Deities.getDeityName(entityId);
            MissionHooks.destroyLastMissionForEntity(entityId);
            performer.getCommunicator().sendNormalServerMessage(
                    "Removed mission for " + entityName + ".");
            logger.info("[mission] GM " + performer.getName() + " removed mission for "
                    + entityName + " (" + entityId + ")");
            return true;
        }

        performer.getCommunicator().sendNormalServerMessage("No current missions to remove.");
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target,
                          short num, float counter) {
        return action(act, performer, target, num, counter);
    }
}
