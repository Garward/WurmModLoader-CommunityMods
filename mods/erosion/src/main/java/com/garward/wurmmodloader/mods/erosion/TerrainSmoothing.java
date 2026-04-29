package com.garward.wurmmodloader.mods.erosion;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background terrain-smoothing routine. Mirrors the GM-action smoothing
 * logic in {@code mods/extraactions/SmoothTerrainPerformer} — duplicated
 * here so erosion stays independent of extraactions at jar level.
 */
final class TerrainSmoothing {

    private static final Logger logger = Logger.getLogger(TerrainSmoothing.class.getName());
    private static final int RADIUS = 10;

    private TerrainSmoothing() {}

    static void smoothArea(int tilex, int tiley) {
        long start = System.currentTimeMillis();
        for (int x = tilex - RADIUS; x < tilex + RADIUS; x++) {
            for (int y = tiley - RADIUS; y < tiley + RADIUS; y++) {
                smooth(x, y, Server.surfaceMesh);
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 500) {
            logger.info(String.format(
                    "[erosion] smoothing terrain at [%s, %s] took %d ms",
                    tilex, tiley, elapsed));
        }
    }

    private static boolean isValidSmoothTile(byte type) {
        return Tiles.isTree(type) || Tiles.isBush(type)
                || type == Tiles.Tile.TILE_SAND.id
                || type == Tiles.Tile.TILE_GRASS.id
                || type == Tiles.Tile.TILE_TUNDRA.id
                || type == Tiles.Tile.TILE_STEPPE.id
                || type == Tiles.Tile.TILE_SNOW.id;
    }

    private static void smooth(int tilex, int tiley, MeshIO mesh) {
        if (tilex < 50 || tiley < 50
                || tilex > Server.surfaceMesh.getSize() - 50
                || tiley > Server.surfaceMesh.getSize() - 50) return;
        if (tilex > 1 << Constants.meshSize || tiley > 1 << Constants.meshSize) return;

        int digTile = mesh.getTile(tilex, tiley);
        byte digTileType = Tiles.decodeType(digTile);
        if (!isValidSmoothTile(digTileType)) return;

        short digTileHeight = Tiles.decodeHeight(digTile);
        if (digTileHeight <= Tiles.decodeHeight(Server.rockMesh.getTile(tilex, tiley))) return;

        Village village = Villages.getVillageWithPerimeterAt(tilex, tiley, true);
        if (village != null) return;
        Structure structure = Structures.getStructureForTile(tilex, tiley, true);
        if (structure != null) return;

        short minHeight = -300;
        short maxHeight = 20000;

        int minDiff = -5;
        int xMinDiff = 0;
        int yMinDiff = 0;

        int northDiff = Tiles.decodeHeight(mesh.getTile(tilex,     tiley - 1)) - digTileHeight;
        if (northDiff < minDiff) { minDiff = northDiff; xMinDiff = 0;  yMinDiff = -1; }

        int eastDiff  = Tiles.decodeHeight(mesh.getTile(tilex + 1, tiley    )) - digTileHeight;
        if (eastDiff  < minDiff) { minDiff = eastDiff;  xMinDiff = 1;  yMinDiff = 0;  }

        int southDiff = Tiles.decodeHeight(mesh.getTile(tilex,     tiley + 1)) - digTileHeight;
        if (southDiff < minDiff) { minDiff = southDiff; xMinDiff = 0;  yMinDiff = 1;  }

        int westDiff  = Tiles.decodeHeight(mesh.getTile(tilex - 1, tiley    )) - digTileHeight;
        if (westDiff  < minDiff) { xMinDiff = -1; yMinDiff = 0; }

        boolean bump = false;
        boolean pit = false;
        boolean slanted = false;
        if      (westDiff  < 0 && eastDiff  < 0) bump = true;
        else if (westDiff  > 0 && eastDiff  > 0) pit  = true;
        else if (northDiff < 0 && southDiff < 0) bump = true;
        else if (northDiff > 0 && southDiff > 0) pit  = true;
        if (pit) return;
        if (!bump) {
            if (Math.abs(westDiff + eastDiff)  > 10) slanted = true;
            else if (Math.abs(northDiff + southDiff) > 10) slanted = true;
        }
        if (!bump && !slanted) return;

        for (int x = tilex - 2; x < tilex + 2; x++) {
            for (int y = tiley - 2; y < tiley + 2; y++) {
                byte t = Tiles.decodeType(mesh.getTile(x, y));
                if (!isValidSmoothTile(t)) return;
                if (Structures.getStructureForTile(x, y, true) != null) return;
            }
        }

        if (digTileHeight <= minHeight || digTileHeight >= maxHeight) return;

        for (int x = 1; x >= -1; --x) {
            for (int y = 1; y >= -1; --y) {
                boolean changed = false;
                int lTile = mesh.getTile(tilex + x, tiley + y);
                byte type = Tiles.decodeType(lTile);
                short newTileHeight = Tiles.decodeHeight(lTile);
                short rockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(tilex + x, tiley + y));
                if (x == xMinDiff && y == yMinDiff) {
                    changed = true;
                    newTileHeight = (short) Math.max(newTileHeight + 1, rockHeight);
                    mesh.setTile(tilex + x, tiley + y,
                            Tiles.encode(newTileHeight, type, Tiles.decodeData(lTile)));
                }
                if (x == 0 && y == 0 && newTileHeight > rockHeight) {
                    changed = true;
                    newTileHeight = (short) Math.max(newTileHeight - 1, rockHeight);
                    mesh.setTile(tilex + x, tiley + y,
                            Tiles.encode(newTileHeight, type, Tiles.decodeData(lTile)));
                }
                boolean allCornersRock = Terraforming.allCornersAtRockLevel(tilex + x, tiley + y, mesh);
                if (allCornersRock) {
                    int theTile = mesh.getTile(tilex + x, tiley + y);
                    float oldH = Tiles.decodeHeightAsFloat(theTile);
                    Server.modifyFlagsByTileType(tilex + x, tiley + y, Tiles.Tile.TILE_ROCK.id);
                    mesh.setTile(tilex + x, tiley + y,
                            Tiles.encode(oldH, Tiles.Tile.TILE_ROCK.id, (byte) 0));
                    Players.getInstance().sendChangedTile(tilex + x, tiley + y, true, true);
                } else if (type == Tiles.TILE_TYPE_ROCK) {
                    int theTile = mesh.getTile(tilex + x, tiley + y);
                    float oldH = Tiles.decodeHeightAsFloat(theTile);
                    Server.modifyFlagsByTileType(tilex + x, tiley + y, Tiles.Tile.TILE_DIRT.id);
                    mesh.setTile(tilex + x, tiley + y,
                            Tiles.encode(oldH, Tiles.Tile.TILE_DIRT.id, (byte) 0));
                    Players.getInstance().sendChangedTile(tilex + x, tiley + y, true, true);
                }
                if (changed) {
                    Players.getInstance().sendChangedTile(tilex + x, tiley + y, true, true);
                    try {
                        Zone z = Zones.getZone(tilex + x, tiley + y, true);
                        z.changeTile(tilex + x, tiley + y);
                    } catch (NoSuchZoneException nsz) {
                        logger.log(Level.INFO, "no such zone? " + tilex + "," + tiley, nsz);
                    }
                }
            }
        }
    }
}
