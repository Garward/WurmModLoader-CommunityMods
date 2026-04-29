package com.garward.wurmmodloader.mods.supplydepot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

import java.util.ArrayList;
import java.util.List;

final class DepotState {

    /** Active depots tracked across ticks. */
    static final List<Item> depots = new ArrayList<>();

    /** Random unique chosen as the announcement host (resets after each spawn). */
    static Creature host = null;

    /** Last successful depot spawn timestamp. */
    static long lastSpawnedDepot = 0L;

    /** Last "begin capture" broadcast (throttled by captureMessageInterval). */
    static long lastAttemptedDepotCapture = 0L;

    static boolean isSupplyDepot(Item item) {
        return DepotTemplates.depotTemplateId > 0
                && item.getTemplateId() == DepotTemplates.depotTemplateId;
    }

    private DepotState() {}
}
