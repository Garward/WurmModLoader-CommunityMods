package com.garward.wurmmodloader.mods.soulstealing;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

final class SoulstealingTemplates {

    private static final Logger logger = Logger.getLogger(SoulstealingTemplates.class.getName());

    static int soulId = -1;
    static int eternalReservoirId = -1;

    private SoulstealingTemplates() {}

    static void register() {
        soulId = registerSoul();
        eternalReservoirId = registerEternalReservoir();
    }

    static void registerCreationEntry(int chaosCrystalTemplateId) {
        if (eternalReservoirId <= 0) {
            logger.warning("[soulstealing] eternal reservoir template id missing, skipping creation entry");
            return;
        }
        if (chaosCrystalTemplateId <= 0) {
            logger.info("[soulstealing] chaosCrystalTemplateId not set, skipping creation entry");
            return;
        }
        AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(
                SkillList.POTTERY,
                ItemList.diamondStar, ItemList.dirtPile, eternalReservoirId,
                false, false, 0.0F, true, true, 0, 0.0D, CreationCategories.ALTAR);
        entry.addRequirement(new CreationRequirement(1, ItemList.dirtPile, 99, true));
        entry.addRequirement(new CreationRequirement(2, ItemList.brickPottery, 200, true));
        entry.addRequirement(new CreationRequirement(3, chaosCrystalTemplateId, 30, true));
        entry.addRequirement(new CreationRequirement(4, ItemList.heart, 20, true));
        logger.info("[soulstealing] eternal reservoir creation entry registered (chaos crystal id=" + chaosCrystalTemplateId + ")");
    }

    private static int registerSoul() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.item.soul");
            b.name("soul", "souls", "The captured soul of a creature.");
            b.descriptions("brilliantly glowing", "strongly glowing", "faintly glowing", "barely glowing");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_MAGIC,
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK,
                    ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
            });
            b.imageNumber((short) 859);
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
            ItemTemplate template = b.build();
            int id = template.getTemplateId();
            logger.info("[soulstealing] registered mod.item.soul (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[soulstealing] failed to register soul template: " + e.getMessage());
            return -1;
        }
    }

    private static int registerEternalReservoir() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.item.eternal.reservoir");
            b.name("eternal reservoir", "eternal reservoir",
                    "Fueled by the souls of the fallen, this black magic device stores souls that tend to nearby creatures and fires.");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_STONE,
                    ItemTypes.ITEM_TYPE_REPAIRABLE,
                    ItemTypes.ITEM_TYPE_NOTAKE,
                    ItemTypes.ITEM_TYPE_DECORATION,
                    ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
                    ItemTypes.ITEM_TYPE_HASDATA,
                    ItemTypes.ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION,
                    ItemTypes.ITEM_TYPE_NOT_MISSION
            });
            b.imageNumber((short) 60);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(500, 500, 1000);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.structure.rift.altar.1.");
            b.difficulty(40.0f);
            b.weightGrams(200000);
            b.material(Materials.MATERIAL_STONE);
            b.value(10000);
            ItemTemplate template = b.build();
            int id = template.getTemplateId();
            logger.info("[soulstealing] registered mod.item.eternal.reservoir (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[soulstealing] failed to register eternal reservoir template: " + e.getMessage());
            return -1;
        }
    }
}
