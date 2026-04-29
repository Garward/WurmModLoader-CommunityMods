package com.garward.wurmmodloader.mods.caches;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.spells.SpellEffect;

/**
 * Pared-down port of upstream {@code mod.sin.wyvern.util.ItemUtil} — only
 * the helpers the cache loot routine needs. Lives here rather than a shared
 * library so this submod stays standalone.
 */
final class CacheItemUtil {

    private CacheItemUtil() {}

    static final int[] TOOL_WEAPON_TEMPLATES = {
            ItemList.axeSmall, ItemList.shieldMedium, ItemList.hatchet, ItemList.knifeCarving,
            ItemList.pickAxe, ItemList.swordLong, ItemList.saw, ItemList.shovel, ItemList.rake,
            ItemList.hammerMetal, ItemList.hammerWood, ItemList.anvilSmall, ItemList.cheeseDrill,
            ItemList.swordShort, ItemList.swordTwoHander, ItemList.shieldSmallWood,
            ItemList.shieldSmallMetal, ItemList.shieldMediumWood, ItemList.shieldLargeWood,
            ItemList.shieldLargeMetal, ItemList.axeHuge, ItemList.axeMedium, ItemList.knifeButchering,
            ItemList.fishingRodIronHook, ItemList.stoneChisel, ItemList.spindle, ItemList.anvilLarge,
            ItemList.grindstone, ItemList.needleIron, ItemList.knifeFood, ItemList.sickle,
            ItemList.scythe, ItemList.maulLarge, ItemList.maulSmall, ItemList.maulMedium,
            ItemList.file, ItemList.awl, ItemList.leatherKnife, ItemList.scissors,
            ItemList.clayShaper, ItemList.spatula, ItemList.fruitpress, ItemList.bowShortNoString,
            ItemList.bowMediumNoString, ItemList.bowLongNoString, ItemList.trowel,
            ItemList.groomingBrush, ItemList.spearLong, ItemList.halberd, ItemList.spearSteel,
            ItemList.staffSteel
    };

    static void applyEnchant(Item item, byte enchant, float power) {
        ItemSpellEffects effs = item.getSpellEffects();
        if (effs == null) {
            effs = new ItemSpellEffects(item.getWurmId());
        }
        SpellEffect eff = new SpellEffect(item.getWurmId(), enchant, power, 20000000);
        effs.addSpellEffect(eff);
        if (item.getDescription().length() > 0) {
            item.setDescription(item.getDescription() + " ");
        }
        item.setDescription(item.getDescription() + eff.getName().substring(0, 1) + Math.round(power));
    }

    static Item createRandomToolWeapon(float minQL, float maxQL, String creator) {
        try {
            return ItemFactory.createItem(
                    TOOL_WEAPON_TEMPLATES[Server.rand.nextInt(TOOL_WEAPON_TEMPLATES.length)],
                    minQL + ((maxQL - minQL) * Server.rand.nextFloat()), creator);
        } catch (FailedException | NoSuchTemplateException e) {
            return null;
        }
    }

    // Single-use rune ids — applying these consumes the rune rather than
    // attaching as a permanent enchant, so caches must not roll them.
    static boolean isSingleUseRune(byte rune) {
        switch (rune) {
            case -80:
            case -81:
            case -91:
            case -97:
            case -104:
            case -107:
            case -119:
            case -126:
                return true;
            default:
                return false;
        }
    }
}
