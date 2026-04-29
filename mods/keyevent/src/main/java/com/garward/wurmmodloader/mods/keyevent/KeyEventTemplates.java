package com.garward.wurmmodloader.mods.keyevent;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

final class KeyEventTemplates {

    private static final Logger logger = Logger.getLogger(KeyEventTemplates.class.getName());

    static int keyFragmentTemplateId = -1;
    static int enchantOrbTemplateId = -1;
    static int eternalOrbTemplateId = -1;
    static int affinityOrbTemplateId = -1;

    private KeyEventTemplates() {}

    static void register() {
        keyFragmentTemplateId = createKeyFragment();
        enchantOrbTemplateId = createEnchantOrb();
        eternalOrbTemplateId = createEternalOrb();
        affinityOrbTemplateId = createAffinityOrb();
    }

    private static int createKeyFragment() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.fragment.key");
            b.name("key fragment [1/50]", "key fragments",
                    "A small fragment of a much larger key.");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_MAGIC,
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK,
                    ItemTypes.ITEM_TYPE_SERVERBOUND,
                    ItemTypes.ITEM_TYPE_ARTIFACT
            });
            b.imageNumber((short) 462);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.decoration.gem.resurrectionstone.");
            b.difficulty(5.0f);
            b.weightGrams(250);
            b.material(Materials.MATERIAL_CRYSTAL);
            b.value(5000);
            b.isTraded(true);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[keyevent] registered mod.fragment.key (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[keyevent] failed to register key fragment: " + e.getMessage());
            return -1;
        }
    }

    private static int createEnchantOrb() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("wyvern.enchantorb");
            b.name("enchant orb", "enchant orbs",
                    "It shimmers lightly, the magic inside waiting for a proper vessel.");
            b.descriptions("vibrant", "glowing", "faint", "empty");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_MAGIC,
                    ItemTypes.ITEM_TYPE_INDESTRUCTIBLE
            });
            b.imageNumber((short) 819);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.artifact.orbdoom");
            b.difficulty(5.0f);
            b.weightGrams(500);
            b.material(Materials.MATERIAL_CRYSTAL);
            b.value(50000);
            b.isTraded(true);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[keyevent] registered wyvern.enchantorb (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[keyevent] failed to register enchant orb: " + e.getMessage());
            return -1;
        }
    }

    private static int createEternalOrb() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.item.eternal.orb");
            b.name("eternal orb", "eternal orbs",
                    "Legends say it consumes magic from an object, and moves it to another.");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK,
                    ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
            });
            b.imageNumber((short) 819);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.artifact.orbdoom");
            b.difficulty(5.0f);
            b.weightGrams(500);
            b.material(Materials.MATERIAL_CRYSTAL);
            b.value(100000);
            b.isTraded(true);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[keyevent] registered mod.item.eternal.orb (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[keyevent] failed to register eternal orb: " + e.getMessage());
            return -1;
        }
    }

    private static int createAffinityOrb() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("wyvern.affinityorb");
            b.name("affinity orb", "affinity orbs",
                    "A valuable orb that infuses the user with hidden knowledge.");
            b.descriptions("brilliantly glowing", "strongly glowing", "faintly glowing", "barely glowing");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK
            });
            b.imageNumber((short) 919);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.artifact.orbdoom");
            b.difficulty(5.0f);
            b.weightGrams(500);
            b.material(Materials.MATERIAL_CRYSTAL);
            b.value(500000);
            b.isTraded(true);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[keyevent] registered wyvern.affinityorb (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[keyevent] failed to register affinity orb: " + e.getMessage());
            return -1;
        }
    }
}
