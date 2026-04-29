package com.garward.wurmmodloader.mods.supplydepot;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Drops the capture reward into the performer's inventory. Caches and
 * sorcery fragments come from {@link DepotTemplates}; the wider basket
 * (extra caches, kingdom tokens, seryll, sleep powder, HotA statue, copper)
 * is wired through config so server owners can add or trim entries.
 */
final class DepotRewards {

    private static final Logger logger = Logger.getLogger(DepotRewards.class.getName());

    static void giveCaptureReward(Creature performer, SupplyDepotConfig cfg) {
        Item inv = performer.getInventory();

        // Always: one arena cache + one sorcery fragment from the depot itself.
        try {
            if (DepotTemplates.arenaCacheTemplateId > 0) {
                Item cache = ItemFactory.createItem(DepotTemplates.arenaCacheTemplateId,
                        90f + 10f * Server.rand.nextFloat(), "");
                inv.insertItem(cache, true);
            }
            if (DepotTemplates.sorceryFragmentTemplateId > 0) {
                Item frag = ItemFactory.createItem(DepotTemplates.sorceryFragmentTemplateId,
                        90f + 10f * Server.rand.nextFloat(), "");
                inv.insertItem(frag, true);
            }
        } catch (FailedException | NoSuchTemplateException e) {
            logger.log(Level.WARNING, "[supplydepot] failed primary capture drop", e);
        }

        // Optional: enchant orb (only if config points at a registered template).
        if (cfg.enchantOrbTemplateId > 0) {
            try {
                float power = 60f + Math.min(Server.rand.nextFloat() * 60f,
                        Server.rand.nextFloat() * 60f);
                Item orb = ItemFactory.createItem(cfg.enchantOrbTemplateId, power, "");
                inv.insertItem(orb, true);
            } catch (FailedException | NoSuchTemplateException e) {
                logger.log(Level.WARNING, "[supplydepot] failed enchant orb drop", e);
            }
        }

        // Optional caches grab-bag.
        try {
            int[] cacheIds = CrossModLookup.activeCacheTemplates();
            if (cacheIds.length > 0) {
                int rolls = cfg.minCaches
                        + (cfg.maxCaches > cfg.minCaches
                            ? Server.rand.nextInt(cfg.maxCaches - cfg.minCaches + 1) : 0);
                for (int i = 0; i < rolls; i++) {
                    int id = cacheIds[Server.rand.nextInt(cacheIds.length)];
                    Item cache = ItemFactory.createItem(id,
                            40f + 50f * Server.rand.nextFloat(), "");
                    inv.insertItem(cache, true);
                }
            }
        } catch (FailedException | NoSuchTemplateException e) {
            logger.log(Level.WARNING, "[supplydepot] failed cache grab-bag drop", e);
        }

        // Kingdom tokens.
        if (cfg.kingdomTokenTemplateId > 0 && cfg.minKingdomTokens > 0) {
            int rolls = cfg.minKingdomTokens
                    + (cfg.maxKingdomTokens > cfg.minKingdomTokens
                        ? Server.rand.nextInt(cfg.maxKingdomTokens - cfg.minKingdomTokens + 1) : 0);
            for (int i = 0; i < rolls; i++) {
                try {
                    Item token = ItemFactory.createItem(cfg.kingdomTokenTemplateId,
                            40f + 50f * Server.rand.nextFloat(), "");
                    inv.insertItem(token, true);
                } catch (FailedException | NoSuchTemplateException e) {
                    logger.log(Level.WARNING, "[supplydepot] failed kingdom token drop", e);
                    break;
                }
            }
        }

        try {
            Item seryll = ItemFactory.createItem(ItemList.seryllBar,
                    80f + 20f * Server.rand.nextFloat(), null);
            inv.insertItem(seryll, true);
        } catch (FailedException | NoSuchTemplateException e) {
            logger.log(Level.WARNING, "[supplydepot] failed seryll drop", e);
        }

        try {
            Item sleepPowder = ItemFactory.createItem(ItemList.sleepPowder, 99f, null);
            inv.insertItem(sleepPowder, true);
        } catch (FailedException | NoSuchTemplateException e) {
            logger.log(Level.WARNING, "[supplydepot] failed sleep powder drop", e);
        }

        if (cfg.hotaStatueChancePercent > 0
                && Server.rand.nextFloat() * 100f <= cfg.hotaStatueChancePercent) {
            try {
                Item hotaStatue = ItemFactory.createItem(ItemList.statueHota,
                        80f + 20f * Server.rand.nextFloat(), "");
                hotaStatue.setAuxData((byte) Server.rand.nextInt(10));
                hotaStatue.setWeight(50000, true);
                inv.insertItem(hotaStatue, true);
            } catch (FailedException | NoSuchTemplateException e) {
                logger.log(Level.WARNING, "[supplydepot] failed HotA statue drop", e);
            }
        }

        long minIron = (long) cfg.minCopperReward * 100L;
        long maxIron = (long) cfg.maxCopperReward * 100L;
        long iron = minIron + (maxIron > minIron ? Server.rand.nextInt((int) (maxIron - minIron)) : 0);
        Item[] coins = Economy.getEconomy().getCoinsFor(iron);
        for (Item coin : coins) {
            inv.insertItem(coin, true);
        }
    }

    private DepotRewards() {}
}
