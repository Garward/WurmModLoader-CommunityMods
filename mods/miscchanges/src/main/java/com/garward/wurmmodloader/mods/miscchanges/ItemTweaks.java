package com.garward.wurmmodloader.mods.miscchanges;

import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vanilla item-template tweaks ported from Sindusk's WyvernMods
 * {@code ItemMod.modifyItems()}. Each tweak reflects one private field on the
 * matching {@link ItemTemplate}; runs at {@code ItemTemplatesCreatedEvent}
 * (after the vanilla {@code Items.createItems()} pass).
 */
final class ItemTweaks {

    private static final Logger logger = Logger.getLogger(ItemTweaks.class.getName());

    private ItemTweaks() {}

    static void applyAll(MiscChangesMod cfg) {
        if (cfg.combineLeather)            tryBool(ItemList.leather, "combine", true);
        if (cfg.combineLogs)               tryBool(ItemList.log,     "combine", true);
        if (cfg.reduceLogVolume)           reduceVolume(ItemList.log,     -1, -1, 50);
        if (cfg.reduceKindlingVolume)      reduceVolume(ItemList.kindling, -1, 10, 10);

        if (cfg.droppableSleepPowder)      tryBool(ItemList.sleepPowder, "nodrop", false);
        if (cfg.oneHandedLongSpear)        tryBool(ItemList.spearLong,   "isTwohanded", false);

        if (cfg.loadableMailbox && !Servers.localServer.PVPSERVER) {
            tryBool(ItemList.mailboxWood,     "isTransportable", true);
            tryBool(ItemList.mailboxStone,    "isTransportable", true);
            tryBool(ItemList.mailboxWoodTwo,  "isTransportable", true);
            tryBool(ItemList.mailboxStoneTwo, "isTransportable", true);
        }
        if (cfg.loadableBellTower) tryBool(ItemList.bellTower, "isTransportable", true);
        if (cfg.loadableTrashBin)  tryBool(ItemList.trashBin,  "isTransportable", true);
        if (cfg.loadableAltars) {
            tryBool(ItemList.altarStone,  "isTransportable", true);
            tryBool(ItemList.altarWood,   "isTransportable", true);
            tryBool(ItemList.altarSilver, "isTransportable", true);
            tryBool(ItemList.altarGold,   "isTransportable", true);
        }

        if (cfg.decorationStoneKeystone)   tryBool(ItemList.stoneKeystone,  "decoration", true);
        if (cfg.decorationMarbleKeystone)  tryBool(ItemList.marbleKeystone, "decoration", true);
        if (cfg.decorationSkull)           tryBool(ItemList.skull,          "decoration", true);

        if (cfg.reduceDirtDifficulty)      tryFloat(ItemList.dirtPile,  "difficulty", 50.0f);
        if (cfg.reduceSandDifficulty)      tryFloat(ItemList.sand,      "difficulty", 50.0f);
        if (cfg.reduceSandstoneDifficulty) tryFloat(ItemList.sandstone, "difficulty", 50.0f);

        if (cfg.setSilverMirrorPrice)      tryInt(ItemList.handMirror,        "value", cfg.silverMirrorPriceIron);
        if (cfg.setGoldMirrorPrice)        tryInt(ItemList.goldenMirror,      "value", cfg.goldMirrorPriceIron);
        if (cfg.setCreatureCratePrice) {
            tryInt (ItemList.creatureCrate, "value",     cfg.creatureCratePriceIron);
            tryBool(ItemList.creatureCrate, "fullprice", true);
        }
        if (cfg.setResurrectionStonePrice) tryInt(ItemList.resurrectionStone, "value", cfg.resurrectionStonePriceIron);
        if (cfg.setShakerOrbPrice)         tryInt(ItemList.shakerOrb,         "value", cfg.shakerOrbPriceIron);

        if (cfg.adjustStatueFragmentCount) {
            setFragments(ItemList.statueWorg,  40);
            setFragments(ItemList.statueEagle, 40);
            setFragments(ItemList.statueHellHorse, 45);
            setFragments(ItemList.statueDrake,     45);
            setFragments(ItemList.statueFo,       50);
            setFragments(ItemList.statueMagranon, 50);
            setFragments(ItemList.statueLibila,   50);
            setFragments(ItemList.statueVynora,   50);
        }
    }

    private static void tryBool(int templateId, String fieldName, boolean value) {
        try {
            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(templateId);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), fieldName), value);
        } catch (NoSuchTemplateException | IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[miscchanges] failed to set " + fieldName
                    + "=" + value + " on template " + templateId, e);
        }
    }

    private static void tryInt(int templateId, String fieldName, int value) {
        try {
            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(templateId);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), fieldName), value);
        } catch (NoSuchTemplateException | IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[miscchanges] failed to set " + fieldName
                    + "=" + value + " on template " + templateId, e);
        }
    }

    private static void tryFloat(int templateId, String fieldName, float value) {
        try {
            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(templateId);
            ReflectionUtil.setPrivateField(t, ReflectionUtil.getField(t.getClass(), fieldName), value);
        } catch (NoSuchTemplateException | IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[miscchanges] failed to set " + fieldName
                    + "=" + value + " on template " + templateId, e);
        }
    }

    private static void reduceVolume(int templateId, int newX, int newY, int newZ) {
        try {
            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(templateId);
            Field cx = ReflectionUtil.getField(t.getClass(), "centimetersX");
            Field cy = ReflectionUtil.getField(t.getClass(), "centimetersY");
            Field cz = ReflectionUtil.getField(t.getClass(), "centimetersZ");
            if (newX >= 0) ReflectionUtil.setPrivateField(t, cx, newX);
            if (newY >= 0) ReflectionUtil.setPrivateField(t, cy, newY);
            if (newZ >= 0) ReflectionUtil.setPrivateField(t, cz, newZ);
            int volume = t.getSizeX() * t.getSizeY() * t.getSizeZ();
            ReflectionUtil.setPrivateField(t,
                    ReflectionUtil.getField(t.getClass(), "volume"), volume);
        } catch (NoSuchTemplateException | IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[miscchanges] failed to reduce volume of template " + templateId, e);
        }
    }

    private static void setFragments(int templateId, int fragmentCount) {
        try {
            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(templateId);
            ReflectionUtil.setPrivateField(t,
                    ReflectionUtil.getField(t.getClass(), "fragmentAmount"), fragmentCount);
        } catch (NoSuchTemplateException | IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[miscchanges] failed to set fragmentAmount="
                    + fragmentCount + " on template " + templateId, e);
        }
    }
}
