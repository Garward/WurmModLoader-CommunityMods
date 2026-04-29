package com.garward.wurmmodloader.mods.titan;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

import java.util.ArrayList;
import java.util.logging.Logger;

final class TitanCombat {

    private static final Logger logger = Logger.getLogger(TitanCombat.class.getName());

    static void checkDestroyMineDoor(Creature titan, int x, int y) {
        int tile = Server.surfaceMesh.getTile(x, y);
        if (Tiles.isMineDoor(Tiles.decodeType(tile))) {
            if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_HOLE.id, (byte) 0);
            } else {
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_ROCK.id, (byte) 0);
            }
            Players.getInstance().sendChangedTile(x, y, true, true);
            MineDoorPermission.deleteMineDoor(x, y);
            Server.getInstance().broadCastAction(
                    titan.getName() + "'s ability destroys a mine door!", titan, 50);
        }
    }

    static Creature[] getUndergroundCreatures(int x, int y) {
        VolaTile tCave = Zones.getOrCreateTile(x, y, false);
        if (tCave == null) return null;
        int tileCave = Server.caveMesh.getTile(x, y);
        byte typeCave = Tiles.decodeType(tileCave);
        if (typeCave != Tiles.Tile.TILE_CAVE.id
                && typeCave != Tiles.Tile.TILE_CAVE_EXIT.id
                && typeCave != Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id
                && typeCave != Tiles.Tile.TILE_CAVE_PREPATED_FLOOR_REINFORCED.id) {
            return null;
        }
        return tCave.getCreatures();
    }

    private static void lilithMyceliumVoidAttack(Creature titan, Creature target) {
        if (target.isUnique() || target.isInvulnerable() || target == titan
                || TitanState.isTitanMinion(target)) {
            return;
        }
        target.addWoundOfType(target, Wound.TYPE_INFECTION, 1, true,
                1.0f, true, 50000f, 0f, 0f, true, true);
    }

    private static void ifritMassIncinerateAttack(Creature titan, Creature target) {
        if (target.isUnique() || target.isInvulnerable() || target == titan
                || TitanState.isTitanMinion(target)) {
            return;
        }
        SpellEffects effs = target.getSpellEffects();
        if (effs == null) effs = target.createSpellEffects();
        SpellEffect eff = effs.getSpellEffect((byte) 94);
        if (eff == null) {
            target.getCommunicator().sendAlertServerMessage(
                    "You are engulfed by the flames of Ifrit!", (byte) 4);
            eff = new SpellEffect(target.getWurmId(), (byte) 94, 80f, 180,
                    (byte) 9, (byte) 1, true);
            effs.addSpellEffect(eff);
            Server.getInstance().broadCastAction(
                    titan.getName() + " has engulfed " + target.getNameWithGenus() + " in flames!",
                    titan, 50);
        } else {
            target.getCommunicator().sendAlertServerMessage(
                    "The heat around you increases. The pain is excruciating!", (byte) 4);
            eff.setPower(eff.getPower() + 200f);
            eff.setTimeleft(180);
            target.sendUpdateSpellEffect(eff);
            Server.getInstance().broadCastAction(
                    titan.getName() + " has engulfed " + target.getNameWithGenus()
                            + " in flames again, increasing the intensity!", titan, 50);
        }
    }

    private static void lilithPainRainAttack(Creature titan, Creature target, VolaTile t) {
        if (target.isUnique() || target.isInvulnerable() || target == titan
                || TitanState.isTitanMinion(target)) {
            return;
        }
        t.sendAttachCreatureEffect(target, (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
        try {
            if (target.addWoundOfType(titan, Wound.TYPE_INFECTION,
                    target.getBody().getRandomWoundPos(), false, 1.0f, false,
                    25000.0 * (double) target.addSpellResistance((short) 448),
                    0f, 0f, true, true)) {
                return;
            }
        } catch (Exception e) {
            logger.warning("[titan] pain-rain wound failed: " + e.getMessage());
        }
        target.setTarget(titan.getWurmId(), false);
    }

    static void performAdvancedAbility(Creature titan, int range, int radius) {
        int tilex = titan.getTileX();
        int tiley = titan.getTileY();
        int templateId = titan.getTemplate().getTemplateId();
        if (templateId == Lilith.templateId) {
            performLilithAdvanced(titan, tilex, tiley, range, radius);
        } else if (templateId == Ifrit.templateId) {
            performIfritAdvanced(titan, tilex, tiley, range, radius);
        }
    }

    private static void performLilithAdvanced(Creature titan, int tilex, int tiley, int range, int radius) {
        int tarx = (tilex - range) + Server.rand.nextInt(1 + (range * 2));
        int tary = (tiley - range) + Server.rand.nextInt(1 + (range * 2));
        int sx = Zones.safeTileX(tarx - radius);
        int ex = Zones.safeTileX(tarx + radius);
        int sy = Zones.safeTileY(tary - radius);
        int ey = Zones.safeTileY(tary + radius);
        Zones.flash(tarx, tary, false);
        Server.getInstance().broadCastAction(
                titan.getName() + " casts Mycelium Void, turning the earth to fungus and pulling enemies to "
                        + titan.getHimHerItString() + "!", titan, 50);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getOrCreateTile(x, y, true);
                if (t == null) continue;
                checkDestroyMineDoor(titan, x, y);
                int tile = Server.surfaceMesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                Tiles.Tile theTile = Tiles.getTile(type);
                byte data = Tiles.decodeData(tile);
                if (type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id
                        || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_REED.id
                        || type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_LAWN.id
                        || type == Tiles.Tile.TILE_STEPPE.id
                        || theTile.isNormalTree() || theTile.isEnchanted() || theTile.isNormalBush()) {
                    if (theTile.isNormalTree()) {
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile),
                                theTile.getTreeType(data).asMyceliumTree(), data);
                    } else if (theTile.isEnchantedTree()) {
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile),
                                theTile.getTreeType(data).asNormalTree(), data);
                    } else if (theTile.isNormalBush()) {
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile),
                                theTile.getBushType(data).asMyceliumBush(), data);
                    } else if (theTile.isEnchantedBush()) {
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile),
                                theTile.getBushType(data).asNormalBush(), data);
                    } else if (type == Tiles.Tile.TILE_LAWN.id) {
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile),
                                Tiles.Tile.TILE_MYCELIUM_LAWN.id, (byte) 0);
                    } else {
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile),
                                Tiles.Tile.TILE_MYCELIUM.id, (byte) 0);
                    }
                    Players.getInstance().sendChangedTile(x, y, true, false);
                }
                for (Creature lCret : t.getCreatures()) {
                    lilithMyceliumVoidAttack(titan, lCret);
                }
                Creature[] underground = getUndergroundCreatures(x, y);
                if (underground != null) {
                    for (Creature lCret : underground) {
                        lilithMyceliumVoidAttack(titan, lCret);
                    }
                }
            }
        }
    }

    private static void performIfritAdvanced(Creature titan, int tilex, int tiley, int range, int radius) {
        int tarx = (tilex - range) + Server.rand.nextInt(1 + (range * 2));
        int tary = (tiley - range) + Server.rand.nextInt(1 + (range * 2));
        int sx = Zones.safeTileX(tarx - radius);
        int ex = Zones.safeTileX(tarx + radius);
        int sy = Zones.safeTileY(tary - radius);
        int ey = Zones.safeTileY(tary + radius);
        Zones.flash(tarx, tary, false);
        Server.getInstance().broadCastAction(
                titan.getName() + " casts Mass Incinerate, burning enemies near "
                        + titan.getHimHerItString() + "!", titan, 50);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getOrCreateTile(x, y, true);
                if (t == null) continue;
                checkDestroyMineDoor(titan, x, y);
                new AreaSpellEffect(titan.getWurmId(), x, y, titan.getLayer(), (byte) 35,
                        System.currentTimeMillis() + 5000, 200.0f, titan.getLayer(), 0, true);
                for (Creature lCret : t.getCreatures()) {
                    ifritMassIncinerateAttack(titan, lCret);
                }
                Creature[] underground = getUndergroundCreatures(x, y);
                if (underground != null) {
                    for (Creature lCret : underground) {
                        ifritMassIncinerateAttack(titan, lCret);
                    }
                }
            }
        }
    }

    static void performBasicAbility(Creature titan) {
        int tilex = titan.getTileX();
        int tiley = titan.getTileY();
        int templateId = titan.getTemplate().getTemplateId();
        if (templateId == Lilith.templateId) {
            performLilithBasic(titan, tilex, tiley);
        } else if (templateId == Ifrit.templateId) {
            performIfritBasic(titan, tilex, tiley);
        }
    }

    private static void performLilithBasic(Creature titan, int tilex, int tiley) {
        int sx = Zones.safeTileX(tilex - 10);
        int sy = Zones.safeTileY(tiley - 10);
        int ex = Zones.safeTileX(tilex + 10);
        int ey = Zones.safeTileY(tiley + 10);
        Server.getInstance().broadCastAction(
                titan.getName() + " casts Pain Rain, harming all around "
                        + titan.getHimHerItString() + "!", titan, 50);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                if (t == null) continue;
                for (Creature lCret : t.getCreatures()) {
                    lilithPainRainAttack(titan, lCret, t);
                }
                Creature[] underground = getUndergroundCreatures(x, y);
                if (underground != null) {
                    for (Creature lCret : underground) {
                        lilithPainRainAttack(titan, lCret, t);
                    }
                }
            }
        }
    }

    private static void performIfritBasic(Creature titan, int tilex, int tiley) {
        int sx = Zones.safeTileX(tilex - 10);
        int sy = Zones.safeTileY(tiley - 10);
        int ex = Zones.safeTileX(tilex + 10);
        int ey = Zones.safeTileY(tiley + 10);
        ArrayList<Creature> targets = new ArrayList<>();
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                if (t == null) continue;
                for (Creature lCret : t.getCreatures()) {
                    if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan
                            || TitanState.isTitanMinion(lCret)) continue;
                    targets.add(lCret);
                }
                Creature[] underground = getUndergroundCreatures(x, y);
                if (underground != null) {
                    for (Creature lCret : underground) {
                        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan
                                || TitanState.isTitanMinion(lCret)) continue;
                        targets.add(lCret);
                    }
                }
            }
        }
        if (targets.isEmpty()) return;
        Creature target = null;
        for (Creature cret : targets) {
            if (cret.isHitched() || cret.isRidden()) {
                target = cret;
                break;
            }
        }
        if (target == null) {
            for (Creature cret : targets) {
                if (cret.isPlayer()) {
                    target = cret;
                    break;
                }
            }
        }
        if (target == null) {
            target = targets.get(Server.rand.nextInt(targets.size()));
        }
        if (target == null) return;
        int damage = target.getStatus().damage;
        int minhealth = 65435;
        float maxdam = (float) Math.max(0, minhealth - damage);
        if (maxdam <= 500.0f) return;
        Server.getInstance().broadCastAction(
                titan.getName() + " picks a target at random and Smites " + target.getName() + "!",
                titan, 50);
        target.getCommunicator().sendAlertServerMessage(
                titan.getName() + " smites you.", (byte) 4);
        try {
            target.addWoundOfType(titan, Wound.TYPE_BURN,
                    target.getBody().getRandomWoundPos(), false, 1.0f, false,
                    maxdam, 0f, 0f, true, true);
        } catch (Exception e) {
            logger.warning("[titan] ifrit smite wound failed: " + e.getMessage());
        }
    }

    static void summonChampions(Creature titan, int nums) {
        int templateType = -1;
        String spellName = "";
        if (titan.getTemplate().getTemplateId() == Lilith.templateId) {
            templateType = LilithWraith.templateId;
            spellName = "Raise Wraith";
        } else if (titan.getTemplate().getTemplateId() == Ifrit.templateId) {
            templateType = IfritFiend.templateId;
            spellName = "Summon Fiend";
        }
        if (templateType <= 0) return;
        summonHelper(titan, nums, templateType, spellName, 2, "champions");
    }

    static void summonMinions(Creature titan, int nums) {
        int templateType = -1;
        String spellName = "";
        if (titan.getTemplate().getTemplateId() == Lilith.templateId) {
            templateType = LilithZombie.templateId;
            spellName = "Raise Zombie";
        } else if (titan.getTemplate().getTemplateId() == Ifrit.templateId) {
            templateType = IfritSpider.templateId;
            spellName = "Summon Spider";
        }
        if (templateType <= 0) return;
        summonHelper(titan, nums, templateType, spellName, 10, "minions");
    }

    private static void summonHelper(Creature titan, int nums, int templateType,
                                     String spellName, int searchRadius, String label) {
        try {
            Server.getInstance().broadCastAction(
                    titan.getName() + " casts " + spellName + ", calling " + label + " to "
                            + titan.getHimHerItString() + " aid!", titan, 50);
            for (int i = 0; i < nums; i++) {
                int tilex = ((titan.getTileX() * 4) + 3) - Server.rand.nextInt(7);
                int tiley = ((titan.getTileY() * 4) + 3) - Server.rand.nextInt(7);
                int sx = Zones.safeTileX(tilex - searchRadius);
                int sy = Zones.safeTileY(tiley - searchRadius);
                int ex = Zones.safeTileX(tilex + searchRadius);
                int ey = Zones.safeTileY(tiley + searchRadius);
                Creature target = pickSummonTarget(titan, sx, sy, ex, ey);
                Creature spawned = Creature.doNew(templateType, tilex, tiley,
                        360f * Server.rand.nextFloat(), titan.getLayer(), "", (byte) 0);
                if (target != null) spawned.setOpponent(target);
            }
        } catch (Exception e) {
            logger.warning("[titan] summon failed: " + e.getMessage());
        }
    }

    private static Creature pickSummonTarget(Creature titan, int sx, int sy, int ex, int ey) {
        Creature target = null;
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                if (t != null) {
                    for (Creature lCret : t.getCreatures()) {
                        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan
                                || TitanState.isTitanMinion(lCret)) continue;
                        if (Server.rand.nextInt(3) == 0) {
                            target = lCret;
                            break;
                        }
                    }
                }
                if (target != null) break;
                Creature[] underground = getUndergroundCreatures(x, y);
                if (underground != null) {
                    for (Creature lCret : underground) {
                        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan
                                || TitanState.isTitanMinion(lCret)) continue;
                        if (Server.rand.nextInt(3) == 0) {
                            target = lCret;
                            break;
                        }
                    }
                }
                if (target != null) break;
            }
            if (target != null) break;
        }
        return target;
    }

    private TitanCombat() {}
}
