package com.garward.wurmmodloader.mods.caches;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.shared.constants.Enchants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cache opening pipeline. Mirrors {@code mod.sin.wyvern.Caches.openCache}
 * with two safety widenings on top of the upstream behaviour:
 *
 * <ol>
 *   <li>Every basic/extra template id is verified against
 *       {@link ItemTemplateFactory} before {@link ItemFactory#createItem}
 *       is called, so unregistered ids (custom items shipped by other
 *       submods that aren't installed) are silently skipped instead of
 *       raising {@link NoSuchTemplateException}.</li>
 *   <li>{@code TreasureMapCache}'s special branch reflectively resolves
 *       the third-party {@code com.pveplands.treasurehunting.Treasuremap}
 *       class. If the treasure-hunting mod isn't deployed, the cache
 *       reports the missing dependency to the player rather than
 *       crashing.</li>
 * </ol>
 */
final class CacheLoot {

    private static final Logger logger = Logger.getLogger(CacheLoot.class.getName());

    private static final float MINIMUM_QUALITY = 10f;

    private static final List<Integer> CACHE_IDS = new ArrayList<>();

    static int[] artifactExtraTemplateIds = new int[0];
    static int[] crystalCacheTemplateIds = new int[0];

    private static volatile boolean treasuremapResolved;
    private static volatile Method treasuremapCreate;

    private CacheLoot() {}

    static void recordCacheIds() {
        CACHE_IDS.clear();
        addIfValid(CacheTemplates.armourId);
        addIfValid(CacheTemplates.artifactId);
        addIfValid(CacheTemplates.crystalId);
        addIfValid(CacheTemplates.dragonId);
        addIfValid(CacheTemplates.gemId);
        addIfValid(CacheTemplates.moonId);
        addIfValid(CacheTemplates.potionId);
        addIfValid(CacheTemplates.riftId);
        addIfValid(CacheTemplates.titanId);
        addIfValid(CacheTemplates.toolId);
        addIfValid(CacheTemplates.treasureMapId);
    }

    private static void addIfValid(int id) {
        if (id > 0) CACHE_IDS.add(id);
    }

    static boolean isTreasureCache(Item item) {
        return CACHE_IDS.contains(item.getTemplateId());
    }

    static void openCache(Creature performer, Item cache) {
        int templateId = cache.getTemplateId();
        Item inv = performer.getInventory();
        float quality = cache.getCurrentQualityLevel();
        float baseQL = quality * 0.25f;
        float randQL = quality * 0.6f;

        if (createsCustomBasic(templateId)) {
            getCustomBasic(performer, cache);
        } else {
            int[] basicTemplates = getBasicTemplates(templateId);
            if (basicTemplates == null || basicTemplates.length == 0) {
                logger.warning("[caches] no basic templates for cache id " + templateId);
                return;
            }
            int basicNums = getBasicNums(templateId) + getExtraBasicNums(templateId, quality);
            for (int i = 0; i < basicNums; i++) {
                try {
                    int candidate = basicTemplates[Server.rand.nextInt(basicTemplates.length)];
                    if (!templateExists(candidate)) continue;
                    float basicQuality = Math.max(
                            baseQL + (randQL * Server.rand.nextFloat()),
                            baseQL + (randQL * Server.rand.nextFloat()));
                    basicQuality = Math.min(MINIMUM_QUALITY + basicQuality, 100f);
                    Item basicItem = ItemFactory.createItem(candidate, basicQuality, "");
                    if (cache.getRarity() > basicItem.getRarity()) {
                        basicItem.setRarity(cache.getRarity());
                    }
                    adjustBasicItem(templateId, quality, basicItem);
                    if (adjustBasicWeight(templateId)) {
                        float weightMult = getWeightMultiplier(templateId, quality);
                        basicItem.setWeight((int) (basicItem.getWeightGrams() * weightMult), true);
                    }
                    inv.insertItem(basicItem, true);
                } catch (FailedException | NoSuchTemplateException e) {
                    logger.log(Level.WARNING, "[caches] failed to create basic item for cache id " + templateId, e);
                }
            }
        }

        int chance = getExtraItemChance(templateId);
        if (chance > 0 && Server.rand.nextInt(chance) <= quality) {
            try {
                int[] extras = getExtraTemplates(templateId);
                if (extras != null && extras.length > 0) {
                    int candidate = extras[Server.rand.nextInt(extras.length)];
                    if (templateExists(candidate)) {
                        float extraQuality = Math.max(
                                baseQL + (randQL * Server.rand.nextFloat()),
                                baseQL + (randQL * Server.rand.nextFloat()));
                        extraQuality = Math.min(MINIMUM_QUALITY + extraQuality, 100f);
                        Item extraItem = ItemFactory.createItem(candidate, extraQuality, "");
                        if (cache.getRarity() > extraItem.getRarity()) {
                            extraItem.setRarity(cache.getRarity());
                        }
                        adjustExtraItem(templateId, extraItem);
                        inv.insertItem(extraItem, true);
                    }
                }
            } catch (FailedException | NoSuchTemplateException e) {
                logger.log(Level.WARNING, "[caches] failed to create extra item for cache id " + templateId, e);
            }
        }
    }

    private static boolean createsCustomBasic(int templateId) {
        return templateId == CacheTemplates.titanId
                || templateId == CacheTemplates.treasureMapId;
    }

    private static void getCustomBasic(Creature performer, Item cache) {
        int templateId = cache.getTemplateId();
        if (templateId == CacheTemplates.titanId) {
            Item tool = CacheItemUtil.createRandomToolWeapon(20f, 40f, cache.getCreatorName());
            if (tool != null) {
                CacheItemUtil.applyEnchant(tool, (byte) 120, 40f + (20f * Server.rand.nextFloat()));
                if (tool.isMetal()) {
                    tool.setMaterial(Server.rand.nextBoolean()
                            ? Materials.MATERIAL_ADAMANTINE
                            : Materials.MATERIAL_GLIMMERSTEEL);
                } else if (tool.isWood()) {
                    tool.setMaterial(Materials.MATERIAL_WOOD_WILLOW);
                }
                performer.getInventory().insertItem(tool, true);
            }
        } else if (templateId == CacheTemplates.treasureMapId) {
            Item map = invokeTreasuremapCreate(performer, cache);
            if (map != null) {
                map.setRarity(cache.getRarity());
                performer.getInventory().insertItem(map, true);
            } else {
                performer.getCommunicator().sendNormalServerMessage(
                        "The map crumbles to dust — no cartographer in this realm can decipher it.");
            }
        }
    }

    private static Item invokeTreasuremapCreate(Creature performer, Item cache) {
        if (!treasuremapResolved) {
            synchronized (CacheLoot.class) {
                if (!treasuremapResolved) {
                    treasuremapResolved = true;
                    try {
                        Class<?> klass = Class.forName("com.pveplands.treasurehunting.Treasuremap");
                        treasuremapCreate = klass.getMethod("CreateTreasuremap",
                                Creature.class, Item.class, Item.class, Item.class, boolean.class);
                    } catch (Throwable t) {
                        logger.info("[caches] treasure-hunting mod not present; treasure-map cache will issue refunds");
                    }
                }
            }
        }
        if (treasuremapCreate == null) return null;
        try {
            return (Item) treasuremapCreate.invoke(null, performer, cache, null, null, true);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[caches] Treasuremap.CreateTreasuremap failed", t);
            return null;
        }
    }

    private static boolean templateExists(int id) {
        if (id <= 0) return false;
        try {
            ItemTemplateFactory.getInstance().getTemplate(id);
            return true;
        } catch (NoSuchTemplateException e) {
            return false;
        }
    }

    private static boolean adjustBasicWeight(int templateId) {
        return templateId == CacheTemplates.dragonId
                || templateId == CacheTemplates.moonId;
    }

    private static float getWeightMultiplier(int templateId, float quality) {
        if (templateId == CacheTemplates.dragonId) {
            return 0.005f + (quality * 0.0005f) + (quality * 0.0001f * Server.rand.nextFloat());
        } else if (templateId == CacheTemplates.moonId) {
            return 1f + (quality * 0.005f) + (quality * 0.005f * Server.rand.nextFloat());
        }
        return 1f + (quality * 0.002f);
    }

    private static int[] getBasicTemplates(int templateId) {
        if (templateId == CacheTemplates.armourId) {
            return new int[] {
                    ItemList.clothGlove, ItemList.clothHood, ItemList.clothHose, ItemList.clothJacket,
                    ItemList.clothJacket, ItemList.clothShirt, ItemList.clothShoes, ItemList.clothSleeve,
                    ItemList.leatherBoot, ItemList.leatherCap, ItemList.leatherGlove, ItemList.leatherHose,
                    ItemList.leatherJacket, ItemList.leatherSleeve,
                    ItemList.studdedLeatherBoot, ItemList.studdedLeatherCap, ItemList.studdedLeatherGlove,
                    ItemList.studdedLeatherHose, ItemList.studdedLeatherHose, ItemList.studdedLeatherJacket,
                    ItemList.studdedLeatherSleeve,
                    ItemList.chainBoot, ItemList.chainCoif, ItemList.chainGlove, ItemList.chainHose,
                    ItemList.chainJacket, ItemList.chainSleeve,
                    ItemList.plateBoot, ItemList.plateGauntlet, ItemList.plateHose, ItemList.plateJacket,
                    ItemList.plateSleeve, ItemList.helmetGreat, ItemList.helmetBasinet, ItemList.helmetOpen
            };
        } else if (templateId == CacheTemplates.artifactId) {
            int[] vanilla = {
                    ItemList.swordShort, ItemList.swordLong, ItemList.swordTwoHander,
                    ItemList.axeSmall, ItemList.axeMedium, ItemList.axeHuge,
                    ItemList.maulSmall, ItemList.maulMedium, ItemList.maulLarge,
                    ItemList.spearLong, ItemList.staffSteel, ItemList.halberd
            };
            return concat(vanilla, artifactExtraTemplateIds);
        } else if (templateId == CacheTemplates.crystalId) {
            return crystalCacheTemplateIds;
        } else if (templateId == CacheTemplates.dragonId) {
            return new int[] { ItemList.drakeHide, ItemList.drakeHide,
                    ItemList.dragonScale, ItemList.dragonScale };
        } else if (templateId == CacheTemplates.gemId) {
            return new int[] { ItemList.diamond, ItemList.emerald, ItemList.opal,
                    ItemList.ruby, ItemList.sapphire };
        } else if (templateId == CacheTemplates.moonId) {
            return new int[] {
                    ItemList.glimmerSteelBar, ItemList.glimmerSteelBar, ItemList.glimmerSteelBar,
                    ItemList.glimmerSteelBar, ItemList.glimmerSteelBar,
                    ItemList.adamantineBar, ItemList.adamantineBar, ItemList.adamantineBar,
                    ItemList.adamantineBar, ItemList.adamantineBar,
                    ItemList.seryllBar
            };
        } else if (templateId == CacheTemplates.potionId) {
            return new int[] {
                    ItemList.potionAcidDamage, ItemList.potionArmourSmithing, ItemList.potionBlacksmithing,
                    ItemList.potionCarpentry, ItemList.potionFireDamage, ItemList.potionFletching,
                    ItemList.potionFrostDamage, ItemList.potionLeatherworking, ItemList.potionMasonry,
                    ItemList.potionMining, ItemList.potionRopemaking, ItemList.potionShipbuilding,
                    ItemList.potionStonecutting, ItemList.potionTailoring, ItemList.potionWeaponSmithing,
                    ItemList.potionWoodcutting
            };
        } else if (templateId == CacheTemplates.riftId) {
            return new int[] { ItemList.riftCrystal, ItemList.riftWood, ItemList.riftStone };
        } else if (templateId == CacheTemplates.toolId) {
            return new int[] {
                    ItemList.hatchet, ItemList.knifeCarving, ItemList.pickAxe, ItemList.saw,
                    ItemList.shovel, ItemList.rake, ItemList.hammerMetal, ItemList.hammerWood,
                    ItemList.anvilSmall, ItemList.cheeseDrill, ItemList.knifeButchering,
                    ItemList.fishingRodIronHook, ItemList.stoneChisel, ItemList.spindle,
                    ItemList.anvilLarge, ItemList.grindstone, ItemList.needleIron, ItemList.knifeFood,
                    ItemList.sickle, ItemList.scythe, ItemList.file, ItemList.awl, ItemList.leatherKnife,
                    ItemList.scissors, ItemList.clayShaper, ItemList.spatula, ItemList.fruitpress,
                    ItemList.trowel, ItemList.groomingBrush
            };
        }
        return null;
    }

    private static int[] concat(int[] a, int[] b) {
        if (b == null || b.length == 0) return a;
        int[] out = new int[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private static void adjustBasicItem(int templateId, float quality, Item item) {
        if (templateId == CacheTemplates.armourId) {
            if (Server.rand.nextInt(800) < quality && item.getRarity() == 0) {
                if (Server.rand.nextInt(1800) < quality) {
                    item.setRarity(MiscConstants.SUPREME);
                } else {
                    item.setRarity(MiscConstants.RARE);
                }
            }
            if (quality > 50) {
                if (quality > 95 && Server.rand.nextBoolean()) {
                    CacheItemUtil.applyEnchant(item, Enchants.BUFF_SHARED_PAIN,
                            quality * Server.rand.nextFloat() * 0.7f);
                    CacheItemUtil.applyEnchant(item, Enchants.BUFF_WEBARMOUR,
                            quality * Server.rand.nextFloat() * 0.7f);
                } else if (Server.rand.nextBoolean()) {
                    byte[] enchants = { Enchants.BUFF_SHARED_PAIN, Enchants.BUFF_WEBARMOUR };
                    CacheItemUtil.applyEnchant(item,
                            enchants[Server.rand.nextInt(enchants.length)],
                            quality * Server.rand.nextFloat() * 1.5f);
                }
            }
            if (quality > 80 && Server.rand.nextInt(4) == 0) {
                byte[] mats = {
                        Materials.MATERIAL_ADAMANTINE, Materials.MATERIAL_COTTON,
                        Materials.MATERIAL_GLIMMERSTEEL, Materials.MATERIAL_IRON,
                        Materials.MATERIAL_LEATHER, Materials.MATERIAL_SERYLL,
                        Materials.MATERIAL_STEEL
                };
                item.setMaterial(mats[Server.rand.nextInt(mats.length)]);
            } else {
                if (item.isMetal()) item.setMaterial(Materials.MATERIAL_IRON);
                else if (item.isLeather()) item.setMaterial(Materials.MATERIAL_LEATHER);
            }
        } else if (templateId == CacheTemplates.artifactId) {
            byte[] mats = {
                    Materials.MATERIAL_ADAMANTINE, Materials.MATERIAL_GLIMMERSTEEL,
                    Materials.MATERIAL_SERYLL, Materials.MATERIAL_STEEL, Materials.MATERIAL_STEEL
            };
            item.setMaterial(mats[Server.rand.nextInt(mats.length)]);
            if (Server.rand.nextInt(400) < quality && item.getRarity() == 0) {
                if (Server.rand.nextInt(900) < quality) {
                    item.setRarity(MiscConstants.SUPREME);
                } else {
                    item.setRarity(MiscConstants.RARE);
                }
            }
            if (quality > 50 && Server.rand.nextBoolean()) {
                byte[] enchants = { Enchants.BUFF_WIND_OF_AGES, Enchants.BUFF_BLESSINGDARK };
                CacheItemUtil.applyEnchant(item, enchants[Server.rand.nextInt(enchants.length)],
                        quality * 0.5f + (quality * 0.5f * Server.rand.nextFloat()));
                CacheItemUtil.applyEnchant(item, Enchants.BUFF_NIMBLENESS,
                        quality * 0.3f + (quality * 0.7f * Server.rand.nextFloat()));
            } else if (quality > 30) {
                CacheItemUtil.applyEnchant(item, Enchants.BUFF_LIFETRANSFER,
                        quality * 0.6f + (quality * 0.6f * Server.rand.nextFloat()));
            }
        } else if (templateId == CacheTemplates.crystalId) {
            if (Server.rand.nextInt(500) < quality) {
                item.setRarity(MiscConstants.RARE);
            }
        } else if (templateId == CacheTemplates.toolId) {
            byte[] mats = {
                    Materials.MATERIAL_SERYLL, Materials.MATERIAL_GLIMMERSTEEL,
                    Materials.MATERIAL_ADAMANTINE, Materials.MATERIAL_STEEL,
                    Materials.MATERIAL_TIN, Materials.MATERIAL_BRONZE,
                    Materials.MATERIAL_BRASS, Materials.MATERIAL_ZINC,
                    Materials.MATERIAL_IRON, Materials.MATERIAL_COPPER,
                    Materials.MATERIAL_GOLD, Materials.MATERIAL_LEAD,
                    Materials.MATERIAL_SILVER
            };
            item.setMaterial(mats[Server.rand.nextInt(mats.length)]);
            if (Server.rand.nextInt(1200) < quality && item.getRarity() == 0) {
                if (Server.rand.nextInt(2700) < quality) {
                    item.setRarity(MiscConstants.SUPREME);
                } else {
                    item.setRarity(MiscConstants.RARE);
                }
            }
            if (Server.rand.nextInt(200) < quality) {
                byte rune = (byte) (Server.rand.nextInt(78) - 128);
                if (!CacheItemUtil.isSingleUseRune(rune)) {
                    CacheItemUtil.applyEnchant(item, rune, 50);
                }
            }
            if (quality > 30 && Server.rand.nextInt(250) < quality) {
                CacheItemUtil.applyEnchant(item, Enchants.BUFF_WIND_OF_AGES,
                        quality * 0.6f + (quality * 0.6f * Server.rand.nextFloat()));
            }
            if (quality > 30 && Server.rand.nextInt(250) < quality) {
                CacheItemUtil.applyEnchant(item, Enchants.BUFF_CIRCLE_CUNNING,
                        quality * 0.6f + (quality * 0.6f * Server.rand.nextFloat()));
            }
            if (quality > 50 && Server.rand.nextInt(250) < quality) {
                // Efficiency enchant id
                CacheItemUtil.applyEnchant(item, (byte) 114,
                        quality * 0.6f + (quality * 0.6f * Server.rand.nextFloat()));
            }
            if (quality > 70 && Server.rand.nextInt(350) < quality) {
                CacheItemUtil.applyEnchant(item, Enchants.BUFF_BLESSINGDARK,
                        quality * 0.6f + (quality * 0.6f * Server.rand.nextFloat()));
            }
            if (quality > 90 && Server.rand.nextInt(5000) < quality) {
                // Titanforged enchant id
                CacheItemUtil.applyEnchant(item, (byte) 120,
                        quality * 0.2f + (quality * 0.2f * Server.rand.nextFloat()));
            }
        }
    }

    private static int getBasicNums(int templateId) {
        if (templateId == CacheTemplates.crystalId) {
            return Server.rand.nextInt(5) + 8;
        } else if (templateId == CacheTemplates.gemId) {
            return 2;
        }
        return 1;
    }

    private static int getExtraBasicNums(int templateId, float quality) {
        if (templateId == CacheTemplates.armourId) {
            return Server.rand.nextInt(2);
        } else if (templateId == CacheTemplates.crystalId) {
            return Server.rand.nextInt(Math.max((int) (quality * 0.08f), 2));
        } else if (templateId == CacheTemplates.dragonId) {
            if (Server.rand.nextInt(200) <= quality) return 1;
        } else if (templateId == CacheTemplates.gemId) {
            return Server.rand.nextInt(Math.max((int) (quality * 0.03f), 2));
        } else if (templateId == CacheTemplates.potionId) {
            if (Server.rand.nextInt(300) <= quality) return 1;
        } else if (templateId == CacheTemplates.riftId) {
            if (Server.rand.nextInt(300) <= quality) return 2;
            if (Server.rand.nextInt(100) <= quality) return 1;
        }
        return 0;
    }

    private static int getExtraItemChance(int templateId) {
        if (templateId == CacheTemplates.dragonId) return 500;
        if (templateId == CacheTemplates.gemId) return 150;
        if (templateId == CacheTemplates.moonId) return 500;
        return -1;
    }

    private static int[] getExtraTemplates(int templateId) {
        if (templateId == CacheTemplates.dragonId) {
            return new int[] {
                    ItemList.dragonLeatherBoot, ItemList.dragonLeatherCap, ItemList.dragonLeatherGlove,
                    ItemList.dragonLeatherHose, ItemList.dragonLeatherJacket, ItemList.dragonLeatherSleeve,
                    ItemList.dragonScaleBoot, ItemList.dragonScaleGauntlet, ItemList.dragonScaleHose,
                    ItemList.dragonScaleJacket, ItemList.dragonScaleSleeve
            };
        } else if (templateId == CacheTemplates.gemId) {
            return new int[] {
                    ItemList.opalBlack, ItemList.diamondStar, ItemList.emeraldStar,
                    ItemList.rubyStar, ItemList.sapphireStar
            };
        } else if (templateId == CacheTemplates.moonId) {
            return new int[] {
                    ItemList.chainSleeve, ItemList.chainJacket, ItemList.chainHose,
                    ItemList.chainGlove, ItemList.chainCoif, ItemList.chainBoot,
                    ItemList.plateSleeve, ItemList.plateJacket, ItemList.plateHose,
                    ItemList.plateBoot, ItemList.plateGauntlet,
                    ItemList.helmetOpen, ItemList.helmetGreat, ItemList.helmetBasinet
            };
        }
        return null;
    }

    private static void adjustExtraItem(int templateId, Item item) {
        if (templateId == CacheTemplates.armourId) {
            item.setColor(WurmColor.createColor(100, 100, 100));
        } else if (templateId == CacheTemplates.dragonId) {
            item.setMaterial(Materials.MATERIAL_LEATHER);
        } else if (templateId == CacheTemplates.moonId) {
            item.setMaterial(Server.rand.nextBoolean()
                    ? Materials.MATERIAL_ADAMANTINE
                    : Materials.MATERIAL_GLIMMERSTEEL);
        }
    }
}
