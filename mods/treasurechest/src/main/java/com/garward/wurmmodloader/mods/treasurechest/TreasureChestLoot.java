package com.garward.wurmmodloader.mods.treasurechest;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiered treasure-chest loot table ported from Sindusk's WyvernMods
 * {@code TreasureChests.newFillTreasureChest}.
 *
 * <p>Invoked from the {@code Item.fillTreasureChest} bytecode patch in
 * {@link TreasureChestPatches}. The chest's auxData (0-99 with the tier-
 * distribution boost; 0-9 without) selects the tier:
 *
 * <ul>
 *   <li>0-59 = rare</li>
 *   <li>60-89 = supreme</li>
 *   <li>90-99 = fantastic</li>
 * </ul>
 */
public final class TreasureChestLoot {

    private static final Logger logger = Logger.getLogger(TreasureChestLoot.class.getName());

    /** Set by {@link TreasureChestMod#configure} so the patched body sees current config. */
    static volatile int affinityOrbTemplateId = 22767;

    private TreasureChestLoot() {}

    public static void newFillTreasureChest(Item item, int auxdata) {
        int[] normalGems = {ItemList.emerald, ItemList.ruby, ItemList.opal, ItemList.diamond, ItemList.sapphire};
        int[] starGems = {375, 377, 379, 381, 383};
        int[] lumps = {44, 45, 46, 47, 48, 49, 205, 220, 221, 223, 694, 698, 837};
        int[] potions = {871, 874, 875, 876, 877, 878, 879, 881, 883};

        if (auxdata < 60) {
            // Rare tier
            if (item.getTemplateId() == ItemList.treasureChest) {
                item.setRarity((byte) 1);
            }
            doItemSpawn(item, new int[]{ItemList.sourceCrystal, ItemList.adamantineBar,
                    ItemList.glimmerSteelBar, normalGems[Server.rand.nextInt(5)]}, 70.0f, 30.0f, 1);
            if (Server.rand.nextBoolean()) {
                doItemSpawn(item, new int[]{ItemList.seryllBar}, 60.0f, 20.0f, 1);
            }
            if (Server.rand.nextBoolean()) {
                doItemSpawn(item, new int[]{lumps[Server.rand.nextInt(lumps.length)]}, 80.0f, 20.0f, 1);
            }
            if (Server.rand.nextInt(5) == 0) {
                doItemSpawn(item, new int[]{ItemList.potionIllusion}, 10.0f, 90.0f, 1);
            }
            if (Server.rand.nextInt(20) == 0) {
                doItemSpawn(item, new int[]{ItemList.fireworks}, (float) auxdata, 60.0f - (float) auxdata, 1);
            }
            if (affinityOrbTemplateId > 0 && Server.rand.nextInt(200 - auxdata) == 0) {
                doItemSpawn(item, new int[]{affinityOrbTemplateId}, 80.0f, 10.0f, 1);
            }
            if (Server.rand.nextInt(10) == 0) {
                doItemSpawn(item, new int[]{potions[Server.rand.nextInt(potions.length)]}, 50.0f, 50.0f, 1);
            }
            switch (Server.rand.nextInt(3)) {
                case 0: doItemSpawn(item, new int[]{ItemList.riftStone},   90.0f, 10.0f, 1); break;
                case 1: doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1); break;
                case 2: doItemSpawn(item, new int[]{ItemList.riftWood},    90.0f, 10.0f, 1); break;
            }

        } else if (auxdata < 90) {
            // Supreme tier
            if (item.getTemplateId() == ItemList.treasureChest) {
                item.setRarity((byte) 2);
            }
            doItemSpawn(item, new int[]{ItemList.sourceCrystal, 374 + Server.rand.nextInt(10)}, 80.0f, 20.0f, 1);
            doItemSpawn(item, new int[]{ItemList.adamantineBar, ItemList.glimmerSteelBar},
                    80.0f, 20.0f, 2 + Server.rand.nextInt(2));
            doItemSpawn(item, new int[]{ItemList.seryllBar}, 80.0f, 20.0f, 1 + Server.rand.nextInt(3));
            if (Server.rand.nextInt(10) == 0) {
                doItemSpawn(item, new int[]{ItemList.fireworks}, (float) auxdata, 90.0f - (float) auxdata, 1);
            }
            if (affinityOrbTemplateId > 0 && Server.rand.nextInt(150 - auxdata) == 0) {
                doItemSpawn(item, new int[]{affinityOrbTemplateId}, 90.0f, 5.0f, 1);
            }
            if (Server.rand.nextInt(10) == 0) {
                doItemSpawn(item, new int[]{371 + (Server.rand.nextBoolean() ? 0 : 1)}, 80.0f, 20.0f, 1);
            }
            doItemSpawn(item, new int[]{ItemList.riftStone},   90.0f, 10.0f, 1);
            doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1);
            doItemSpawn(item, new int[]{ItemList.riftWood},    90.0f, 10.0f, 1);

        } else {
            // Fantastic tier
            if (item.getTemplateId() == ItemList.treasureChest) {
                item.setRarity((byte) 3);
            }
            doItemSpawn(item, new int[]{ItemList.sourceCrystal, starGems[Server.rand.nextInt(starGems.length)]},
                    90.0f, 10.0f, 1);
            doItemSpawn(item, new int[]{ItemList.adamantineBar, ItemList.glimmerSteelBar},
                    80.0f, 20.0f, 3 + Server.rand.nextInt(3));
            doItemSpawn(item, new int[]{ItemList.seryllBar}, 80.0f, 20.0f, 2 + Server.rand.nextInt(3));
            if (affinityOrbTemplateId > 0 && Server.rand.nextBoolean()) {
                doItemSpawn(item, new int[]{affinityOrbTemplateId}, 99.0f, 1.0f, 1);
            }
            if (Server.rand.nextInt(5) == 0) {
                doItemSpawn(item, new int[]{ItemList.fireworks}, (float) auxdata, 100.0f - (float) auxdata, 1);
            }
            if (Server.rand.nextInt(10) == 0) {
                doItemSpawn(item, new int[]{371 + (Server.rand.nextBoolean() ? 0 : 1)}, 90.0f, 10.0f, 1);
            }
            if (Server.rand.nextInt(100) == 0) {
                doItemSpawn(item, new int[]{ItemList.spyglass}, 99.0f, 1.0f, 1);
            }
            if (Server.rand.nextInt(100) == 0) {
                doItemSpawn(item, new int[]{ItemList.bagKeeping}, 99.0f, 1.0f, 1);
            }
            doItemSpawn(item, new int[]{ItemList.riftStone},   90.0f, 10.0f, 1 + Server.rand.nextInt(3));
            doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));
            doItemSpawn(item, new int[]{ItemList.riftWood},    90.0f, 10.0f, 1 + Server.rand.nextInt(3));

            int rand = Server.rand.nextInt(500);
            int[] fantasticLoot;
            int amount = 1;
            if (rand < 249) {
                fantasticLoot = new int[]{ItemList.drakeHide};
                amount = 3;
            } else if (rand < 349) {
                fantasticLoot = new int[]{ItemList.dragonScale};
                amount = 3;
            } else if (rand < 414) {
                fantasticLoot = new int[]{ItemList.statueHota};
            } else if (rand < 464) {
                fantasticLoot = new int[]{ItemList.boneCollar};
            } else if (rand < 490) {
                // Sorcery items at template ids 795..810.
                fantasticLoot = new int[]{795 + Server.rand.nextInt(16)};
            } else {
                fantasticLoot = new int[]{ItemList.eggLarge};
            }
            doItemSpawn(item, fantasticLoot, 99.0f, 1.0f, amount);
        }

        logger.info("[treasurechest] filled treasure chest level " + auxdata + " at "
                + (item.getPosX() / 4) + ", " + (item.getPosY() / 4));
    }

    private static void doItemSpawn(Item inventory, int[] templateTypes, float startQl, float qlValRange, int maxNums) {
        for (int templateType : templateTypes) {
            if (templateType <= 0) continue;
            // Skip silently if the template isn't registered (e.g. custom-item
            // templates from a submod that's not loaded).
            try {
                ItemTemplateFactory.getInstance().getTemplate(templateType);
            } catch (NoSuchTemplateException e) {
                continue;
            }
            for (int n = 0; n < maxNums; ++n) {
                try {
                    boolean isBoneCollar = templateType == ItemList.boneCollar;
                    byte rarity = (byte) (Server.rand.nextInt(100) == 0 || isBoneCollar ? 1 : 0);
                    if (rarity > 0) {
                        rarity = (byte) (Server.rand.nextInt(100) == 0 && isBoneCollar ? 2 : 1);
                    }
                    if (rarity > 1) {
                        rarity = (byte) (Server.rand.nextInt(100) == 0 && isBoneCollar ? 3 : 2);
                    }
                    float newql = startQl + Server.rand.nextFloat() * qlValRange;

                    Item toInsert = ItemFactory.createItem(templateType, newql, rarity, "");

                    if (templateType == ItemList.statueHota) {
                        toInsert.setAuxData((byte) Server.rand.nextInt(10));
                        toInsert.setWeight(50000, true);
                    }
                    if (templateType == ItemList.eggLarge) {
                        toInsert.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
                    }
                    if (templateType == ItemList.drakeHide) {
                        int colorId = CreatureTemplateCreator.getRandomDrakeId();
                        toInsert.setData1(colorId);
                        CreatureTemplate cTemplate = CreatureTemplateFactory.getInstance().getTemplate(colorId);
                        String creatureName = cTemplate.getName().toLowerCase();
                        if (!toInsert.getName().contains(creatureName)) {
                            toInsert.setName(creatureName + " " + toInsert.getTemplate().getName());
                        }
                        toInsert.setWeight(50 + Server.rand.nextInt(100), true);
                    }
                    if (templateType == ItemList.dragonScale) {
                        int[] dragonIds = {CreatureTemplateIds.DRAGON_BLACK_CID, CreatureTemplateIds.DRAGON_BLUE_CID,
                                CreatureTemplateIds.DRAGON_GREEN_CID, CreatureTemplateIds.DRAGON_RED_CID,
                                CreatureTemplateIds.DRAGON_WHITE_CID};
                        int colorId = dragonIds[Server.rand.nextInt(dragonIds.length)];
                        toInsert.setData1(colorId);
                        CreatureTemplate cTemplate = CreatureTemplateFactory.getInstance().getTemplate(colorId);
                        String creatureName = cTemplate.getName().toLowerCase();
                        if (!toInsert.getName().contains(creatureName)) {
                            toInsert.setName(creatureName + " " + toInsert.getTemplate().getName());
                        }
                        toInsert.setWeight(100 + Server.rand.nextInt(150), true);
                    }
                    if (templateType == ItemList.riftCrystal
                            || templateType == ItemList.riftWood
                            || templateType == ItemList.riftStone) {
                        toInsert.setHasNoDecay(true);
                    }
                    inventory.insertItem(toInsert, true);
                } catch (NoSuchTemplateException | FailedException | NoSuchCreatureTemplateException e) {
                    logger.log(Level.WARNING, "[treasurechest] failed to spawn template " + templateType, e);
                }
            }
        }
    }
}
