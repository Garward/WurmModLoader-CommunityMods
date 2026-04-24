package com.garward.wurmmodloader.mods.betterfarm.trees;

import com.wurmonline.mesh.Tiles;
import com.garward.wurmmodloader.api.events.eventlogic.area.AreaActionType;
import com.garward.wurmmodloader.modsupport.actions.area.AreaActionRegistry;
import com.garward.wurmmodloader.mods.betterfarm.BetterFarmMod;

public class TreeActions {
    public static void register() {
        TreeActionPick pick = new TreeActionPick();
        TreeActionHarvest harvest = new TreeActionHarvest();
        TreeActionPlant plant = new TreeActionPlant();
        TreeActionPrune prune = new TreeActionPrune();

        for (Tiles.Tile t : Tiles.Tile.values()) {
            if (t.isNormalTree() || t.isNormalBush() || (BetterFarmMod.allowInfectedTrees && (t.isMyceliumTree() || t.isMyceliumBush()))) {
                AreaActionRegistry.registerTileHandler(t.id, AreaActionType.PICK_SPROUT, pick);
                AreaActionRegistry.registerTileHandler(t.id, AreaActionType.HARVEST, harvest);
                AreaActionRegistry.registerTileHandler(t.id, AreaActionType.PRUNE, prune);
            }
        }

        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_DIRT.id, AreaActionType.PLANT, plant);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_GRASS.id, AreaActionType.PLANT, plant);
    }
}
