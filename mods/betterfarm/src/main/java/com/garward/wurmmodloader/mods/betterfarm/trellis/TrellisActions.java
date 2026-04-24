package com.garward.wurmmodloader.mods.betterfarm.trellis;

import com.garward.wurmmodloader.api.events.eventlogic.area.AreaActionType;
import com.garward.wurmmodloader.modsupport.actions.area.AreaActionRegistry;

public class TrellisActions {
    public static void register() {
        TrellisActionPrune prune = new TrellisActionPrune();
        TrellisActionHarvest harvest = new TrellisActionHarvest();
        TrellisActionPick pick = new TrellisActionPick();

        for (TrellisType t : TrellisType.values()) {
            AreaActionRegistry.registerItemHandler(t.trellisId, AreaActionType.PRUNE, prune);
            AreaActionRegistry.registerItemHandler(t.trellisId, AreaActionType.HARVEST, harvest);
            AreaActionRegistry.registerItemHandler(t.trellisId, AreaActionType.PICK_SPROUT, pick);
        }
    }
}
