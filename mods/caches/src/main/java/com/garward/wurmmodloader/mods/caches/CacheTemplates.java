package com.garward.wurmmodloader.mods.caches;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Registers the eleven cache item templates (gold gift-box containers) and
 * keeps their assigned ids in package-visible static fields for the loot
 * routine and open-action to dispatch on. Upstream WyvernMods also defined
 * AnimalCache and WeaponCache classes but never wired them into the open
 * pipeline; we omit them here to match the runtime behaviour.
 */
final class CacheTemplates {

    private static final Logger logger = Logger.getLogger(CacheTemplates.class.getName());

    static int armourId = -1;
    static int artifactId = -1;
    static int crystalId = -1;
    static int dragonId = -1;
    static int gemId = -1;
    static int moonId = -1;
    static int potionId = -1;
    static int riftId = -1;
    static int titanId = -1;
    static int toolId = -1;
    static int treasureMapId = -1;

    private CacheTemplates() {}

    static void register() {
        armourId = create("mod.item.cache.armour", "armour cache", "armour caches",
                "A cache of armour. This armour may contain special properties.", 1000);
        artifactId = create("mod.item.cache.artifact", "artifact cache", "artifact caches",
                "A cache containing a special artifact.", 1000);
        crystalId = create("mod.item.cache.crystal", "crystal cache", "crystal caches",
                "A cache of magical crystals.", 1000);
        dragonId = create("mod.item.cache.dragon", "dragon cache", "dragon caches",
                "A cache of dragon material. May also contain drake hide or dragonscale armour.", 1000);
        gemId = create("mod.item.cache.gem", "gem cache", "gem caches",
                "A cache of gems. May also contain star gems.", 1000);
        moonId = create("mod.item.cache.moon", "moon cache", "moon caches",
                "A cache of moon metals. May also contain items created from moon metals.", 1000);
        potionId = create("mod.item.cache.potion", "potion cache", "potion caches",
                "A cache of potions.", 1000);
        riftId = create("mod.item.cache.rift", "rift cache", "rift caches",
                "A cache of rift material.", 1000);
        titanId = create("mod.item.cache.titan", "titan cache", "titan caches",
                "A cache of treasures from a titan.", 1000);
        toolId = create("mod.item.cache.tool", "tool cache", "tool caches",
                "A cache containing a few tools. These tools could have special properties.", 1000);
        treasureMapId = create("mod.item.cache.treasure.map", "treasure map cache", "treasure map caches",
                "A cache containing a new treasure map.", 1000000);
    }

    private static int create(String resourceId, String name, String plural,
                              String description, int value) {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder(resourceId);
            b.name(name, plural, description);
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_MAGIC,
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK,
                    ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
            });
            b.imageNumber((short) 243);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.container.giftbox.");
            b.difficulty(5.0f);
            b.weightGrams(500);
            b.material(Materials.MATERIAL_GOLD);
            b.value(value);
            b.isTraded(true);
            ItemTemplate template = b.build();
            int id = template.getTemplateId();
            logger.info("[caches] registered " + resourceId + " (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[caches] failed to register " + resourceId + ": " + e.getMessage());
            return -1;
        }
    }
}
