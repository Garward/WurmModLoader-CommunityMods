package com.garward.wurmmodloader.mods.bounty;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vanilla-applicable corpse-loot handlers ported from
 * {@code mod.sin.wyvern.bounty.LootBounty}. Custom-creature drops
 * (Wyvern uniques, Titans, RareSpawns, AffinityOrbs, the cache items, etc.)
 * stay with their owning submods; once those land they register their own
 * {@link BountyRegistry.CorpseLootHandler} instances against this submod.
 */
final class BuiltInLootHandlers {

    private static final Logger logger = Logger.getLogger(BuiltInLootHandlers.class.getName());
    private static final Random random = new Random();

    private BuiltInLootHandlers() {}

    static final BountyRegistry.CorpseLootHandler GOBLIN_METAL_LUMP =
            (mob, corpse, strength) -> {
                if (mob.getTemplate().getTemplateId() != CreatureTemplateFactory.GOBLIN_CID) return;
                int[] lumpIds = {
                        ItemList.adamantineBar, ItemList.brassBar, ItemList.bronzeBar,
                        ItemList.copperBar, ItemList.glimmerSteelBar, ItemList.goldBar,
                        ItemList.ironBar, ItemList.leadBar, ItemList.silverBar,
                        ItemList.steelBar, ItemList.zincBar
                };
                try {
                    Item lump = ItemFactory.createItem(
                            lumpIds[random.nextInt(lumpIds.length)],
                            20 + (60 * random.nextFloat()), "");
                    corpse.insertItem(lump);
                } catch (NoSuchTemplateException | FailedException e) {
                    logger.log(Level.WARNING, "[bounty] goblin metal drop failed", e);
                }
            };

    static final BountyRegistry.CorpseLootHandler CHAMPION_LOOT =
            (mob, corpse, strength) -> {
                if (mob.getStatus() == null || !mob.getStatus().isChampion()) return;
                try {
                    if (random.nextInt(100) < 75) {
                        int barTemplate = random.nextBoolean()
                                ? ItemList.adamantineBar : ItemList.glimmerSteelBar;
                        corpse.insertItem(ItemFactory.createItem(
                                barTemplate, 30 + (30 * random.nextFloat()), ""));
                    }
                    if (random.nextInt(100) < 5) {
                        int[] maskTemplates = {
                                ItemList.maskEnlightended, ItemList.maskRavager,
                                ItemList.maskPale, ItemList.maskShadow,
                                ItemList.maskChallenge, ItemList.maskIsles,
                                ItemList.maskOfTheReturner
                        };
                        corpse.insertItem(ItemFactory.createItem(
                                maskTemplates[random.nextInt(maskTemplates.length)],
                                90 + (9 * random.nextFloat()), ""));
                    }
                    if (random.nextInt(100) < 1) {
                        Item bone = ItemFactory.createItem(867,
                                90 + (10 * random.nextFloat()), "");
                        bone.setRarity((byte) 1);
                        if (random.nextInt(100) < 1) bone.setRarity((byte) 2);
                        corpse.insertItem(bone);
                    }
                } catch (NoSuchTemplateException | FailedException e) {
                    logger.log(Level.WARNING, "[bounty] champion-loot drop failed", e);
                }
            };

    /** Optional broadcast handler — installed only when
     *  {@link BountyConfig#broadcastInterestingLoot} is true. Uses
     *  {@code Server.broadCastAction} on the dying creature so only nearby
     *  players see it. */
    static BountyRegistry.CorpseLootHandler broadcastInterestingLoot() {
        return (mob, corpse, strength) -> {
            // Only broadcast for "interesting" categories: champions, uniques.
            if (mob.isUnique() || (mob.getStatus() != null && mob.getStatus().isChampion())) {
                Server.getInstance().broadCastAction(
                        mob.getName() + " had something of interest...", mob, 5);
            }
        };
    }

    static void registerEnabledHandlers(BountyConfig cfg) {
        if (cfg.enableGoblinMetalDrop)    BountyRegistry.addCorpseLootHandler(GOBLIN_METAL_LUMP);
        if (cfg.enableChampionLoot)       BountyRegistry.addCorpseLootHandler(CHAMPION_LOOT);
        if (cfg.broadcastInterestingLoot) BountyRegistry.addCorpseLootHandler(broadcastInterestingLoot());
    }

    static void unregisterAll() {
        BountyRegistry.removeCorpseLootHandler(GOBLIN_METAL_LUMP);
        BountyRegistry.removeCorpseLootHandler(CHAMPION_LOOT);
    }
}
