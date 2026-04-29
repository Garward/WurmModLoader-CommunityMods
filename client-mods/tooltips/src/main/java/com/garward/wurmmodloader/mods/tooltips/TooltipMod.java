package com.garward.wurmmodloader.mods.tooltips;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.client.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.client.api.events.gui.CaveWallPickerHoverNameEvent;
import com.garward.wurmmodloader.client.api.events.gui.CreatureHoverDescriptionEvent;
import com.garward.wurmmodloader.client.api.events.gui.TilePickerHoverNameEvent;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.CellRenderable;
import com.wurmonline.mesh.FieldData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;

/**
 * Port of bdew's better-tooltips client mod. Enriches hover-name labels for
 * tiles, cave walls, and creatures.
 *
 * <p>Original implementation rewrote {@code TilePicker.getHoverName},
 * {@code CaveWallPicker.getHoverName}, and {@code CreatureCellRenderable
 * .getHoverDescription} via {@code HookManager} bytecode injection. This port
 * subscribes to the framework's {@code TilePickerHoverNameEvent},
 * {@code CaveWallPickerHoverNameEvent}, and {@code CreatureHoverDescriptionEvent}
 * — the patches and events live in {@code wurmmodloader-client-core} /
 * {@code wurmmodloader-client-api}.
 */
public class TooltipMod {

    private static final Logger logger = Logger.getLogger("TooltipMod");

    private volatile boolean configLoaded;
    private boolean showSpecialTreesHarvestable;

    private void ensureConfigLoaded() {
        if (configLoaded) return;
        synchronized (this) {
            if (configLoaded) return;
            File props = new File("mods/tooltips/tooltips.properties");
            Properties p = new Properties();
            if (props.isFile()) {
                try (FileInputStream in = new FileInputStream(props)) {
                    p.load(in);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "[tooltips] failed to read " + props + " — using defaults", e);
                }
            }
            this.showSpecialTreesHarvestable = Boolean.parseBoolean(
                    p.getProperty("showSpecialTreesHarvestable", "false").trim());
            this.configLoaded = true;
            logger.info("[tooltips] showSpecialTreesHarvestable=" + showSpecialTreesHarvestable);
        }
    }

    @SubscribeEvent
    public void onTilePickerHoverName(TilePickerHoverNameEvent event) {
        try {
            ensureConfigLoaded();
            World world = (World) event.getWorld();
            if (world == null) return;
            int x = event.getX();
            int y = event.getY();
            int section = event.getSection();

            if (section == 3) {
                event.setOverrideName("Tile corner ("
                        + (int) (world.getNearTerrainBuffer().getHeight(x, y) * 10) + ")");
                return;
            }

            String slope = event.getSlopeSuffix();
            if (slope == null) slope = "";
            if (section != 0) {
                event.setOverrideName("Tile border" + slope);
                return;
            }

            Tiles.Tile type = world.getNearTerrainBuffer().getTileType(x, y);
            byte data = world.getNearTerrainBuffer().getData(x, y);

            slope = slope.replace(" (", "").replace(")", "").trim();
            String suffix;
            if (type == Tiles.Tile.TILE_FIELD || type == Tiles.Tile.TILE_FIELD2) {
                suffix = FieldData.getTypeName(type, data) + ", " + FieldData.getAgeName(data);
                if (!FieldData.isTended(data)) {
                    suffix = suffix + ", untended";
                }
            } else if (type.isTree() || type.isBush()) {
                FoliageAge fage = FoliageAge.getFoliageAge(data);
                suffix = fage.getAgeName();
                if (fage.getAgeId() > FoliageAge.YOUNG_FOUR.getAgeId()
                        && fage.getAgeId() < FoliageAge.OVERAGED.getAgeId()
                        && type.usesNewData()
                        && (type.isNormal() || showSpecialTreesHarvestable)
                        && (data & 0x8) > 0) {
                    suffix = suffix + ", harvestable";
                }
            } else if (type == Tiles.Tile.TILE_GRASS) {
                suffix = GrassData.getFlowerTypeName(data);
            } else {
                suffix = "";
            }

            if (suffix == null || suffix.equals(" ()")) suffix = "";
            if (suffix.length() > 0 && slope.length() > 0) suffix += ", ";
            suffix += slope;
            if (suffix.length() > 0) suffix = " (" + suffix + ")";

            event.setOverrideName(type.getTileName(data) + suffix);
        } catch (Throwable t) {
            logger.log(Level.FINE, "[tooltips] tile hover-name soft-fail", t);
        }
    }

    @SubscribeEvent
    public void onCaveWallPickerHoverName(CaveWallPickerHoverNameEvent event) {
        try {
            World world = (World) event.getWorld();
            if (world == null) return;
            int x = event.getX();
            int y = event.getY();
            int wallSide = event.getWallSide();
            String name = event.getName();

            if (wallSide != 0 && wallSide != 1) {
                if (wallSide == 7) {
                    event.setOverrideName("Tile corner ("
                            + (int) (world.getCaveBuffer().getRawFloor(x, y) * 10) + ")");
                    return;
                }
                if (wallSide == 8 || wallSide == 9 || wallSide == 10 || wallSide == 11) {
                    String slope = event.getSlopeSuffix();
                    if (slope == null) slope = "";
                    event.setOverrideName("Tile border" + slope);
                    return;
                }
            } else {
                short h00, h01, h10, h11;
                if (wallSide == 1) {
                    h00 = world.getCaveBuffer().getRawCeiling(x, y);
                    h01 = world.getCaveBuffer().getRawCeiling(x, y + 1);
                    h10 = world.getCaveBuffer().getRawCeiling(x + 1, y);
                    h11 = world.getCaveBuffer().getRawCeiling(x + 1, y + 1);
                } else {
                    h00 = world.getCaveBuffer().getRawFloor(x, y);
                    h01 = world.getCaveBuffer().getRawFloor(x, y + 1);
                    h10 = world.getCaveBuffer().getRawFloor(x + 1, y);
                    h11 = world.getCaveBuffer().getRawFloor(x + 1, y + 1);
                }
                if (h00 == h01 && h00 == h10 && h00 == h11) {
                    event.setOverrideName(name + " (flat)");
                    return;
                }
            }
            event.setOverrideName(name);
        } catch (Throwable t) {
            logger.log(Level.FINE, "[tooltips] cave-wall hover-name soft-fail", t);
        }
    }

    @SubscribeEvent
    public void onCreatureHoverDescription(CreatureHoverDescriptionEvent event) {
        try {
            World w = CellRenderable.world;
            if (w != null && w.getServerConnection() != null && w.getServerConnection().isDev()) {
                return;
            }
            String model = event.getModelName();
            if (model == null) return;
            PickData pickData = (PickData) event.getPickData();
            if (pickData == null) return;

            String res = "";
            if (model.contains("horse") || model.contains("foal")) {
                if (model.contains(".hell")) {
                    if (model.contains(".cinder")) res = "Cinder";
                    else if (model.contains(".envious")) res = "Envious";
                    else if (model.contains(".shadow")) res = "Shadow";
                    else if (model.contains(".pestilential")) res = "Pestilential";
                    else if (model.contains(".nightshade")) res = "Nightshade";
                    else if (model.contains(".incandescent")) res = "Incandescent";
                    else if (model.contains(".molten")) res = "Molten";
                    else res = "Ash";
                } else {
                    if (model.contains(".brown")) res = "Brown";
                    else if (model.contains(".skewbaldpinto")) res = "Skewbald pinto";
                    else if (model.contains(".goldbuckskin")) res = "Gold buckskin";
                    else if (model.contains(".blacksilver")) res = "Black silver";
                    else if (model.contains(".appaloosa")) res = "Appaloosa";
                    else if (model.contains("horse.chestnut") || model.contains("foal.chestnut")) res = "Chestnut";
                    else if (model.contains(".gold")) res = "Gold";
                    else if (model.contains(".black")) res = "Black";
                    else if (model.contains(".white")) res = "White";
                    else if (model.contains(".piebaldpinto")) res = "Piebald Pinto";
                    else if (model.contains(".bloodbay")) res = "Blood Bay";
                    else if (model.contains(".ebonyblack")) res = "Ebony";
                    else res = "Gray";
                }
                if (model.contains(".male")) res += ", Male";
                else if (model.contains(".female")) res += ", Female";
            } else {
                if (model.contains(".male")) res += "Male";
                else if (model.contains(".female")) res += "Female";
            }
            if (res.length() > 0) pickData.addText(res);
        } catch (Throwable t) {
            logger.log(Level.FINE, "[tooltips] creature hover-description soft-fail", t);
        }
    }
}
