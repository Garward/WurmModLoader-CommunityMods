package com.garward.wurmmodloader.mods.betterfarm.fields;

import com.wurmonline.mesh.Tiles;
import com.garward.wurmmodloader.api.events.eventlogic.area.AreaActionType;
import com.garward.wurmmodloader.modsupport.actions.area.AreaActionRegistry;

public class FieldActions {
    public static void register() {
        FieldActionSow sow = new FieldActionSow();
        FieldActionCultivate cultivate = new FieldActionCultivate();
        FieldActionTend tend = new FieldActionTend();
        FieldActionHarvest harvest = new FieldActionHarvest(false);
        FieldActionHarvest replant = new FieldActionHarvest(true);

        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_DIRT.id, AreaActionType.SOW, sow);

        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_DIRT.id, AreaActionType.CULTIVATE, cultivate);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_DIRT_PACKED.id, AreaActionType.CULTIVATE, cultivate);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_MOSS.id, AreaActionType.CULTIVATE, cultivate);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_GRASS.id, AreaActionType.CULTIVATE, cultivate);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_STEPPE.id, AreaActionType.CULTIVATE, cultivate);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_MYCELIUM.id, AreaActionType.CULTIVATE, cultivate);

        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_FIELD.id, AreaActionType.FARM, tend);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_FIELD2.id, AreaActionType.FARM, tend);

        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_FIELD.id, AreaActionType.HARVEST, harvest);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_FIELD2.id, AreaActionType.HARVEST, harvest);

        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_FIELD.id, AreaActionType.HARVEST_AND_REPLANT, replant);
        AreaActionRegistry.registerTileHandler(Tiles.Tile.TILE_FIELD2.id, AreaActionType.HARVEST_AND_REPLANT, replant);
    }
}
