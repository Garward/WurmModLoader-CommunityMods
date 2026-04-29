package com.garward.wurmmodloader.mods.crystals;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

final class CrystalsTemplates {

    private static final Logger logger = Logger.getLogger(CrystalsTemplates.class.getName());

    static int chaosCrystalId = -1;
    static int enchantersCrystalId = -1;

    private CrystalsTemplates() {}

    static void register() {
        chaosCrystalId = create(
                "mod.chaoscrystal",
                "chaos crystal", "chaos crystals",
                "This volatile crystal will either enhance an item, or destroy it outright.");
        enchantersCrystalId = create(
                "mod.item.crystal.enchanters",
                "enchanters crystal", "enchanters crystals",
                "This crystal can manipulate the magical properties of an item.");
    }

    private static int create(String resourceId, String name, String plural, String description) {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder(resourceId);
            b.name(name, plural, description);
            b.descriptions("brilliantly glowing", "strongly glowing", "faintly glowing", "barely glowing");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_MAGIC,
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK,
                    ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
            });
            b.imageNumber((short) 462);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.valrei.");
            b.difficulty(5.0f);
            b.weightGrams(250);
            b.material(Materials.MATERIAL_CRYSTAL);
            b.value(5000);
            b.isTraded(true);
            ItemTemplate template = b.build();
            int id = template.getTemplateId();
            logger.info("[crystals] registered " + resourceId + " (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[crystals] failed to register " + resourceId + ": " + e.getMessage());
            return -1;
        }
    }
}
