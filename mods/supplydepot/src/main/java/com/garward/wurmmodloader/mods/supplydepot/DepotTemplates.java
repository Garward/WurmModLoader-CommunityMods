package com.garward.wurmmodloader.mods.supplydepot;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Registers the three custom item templates owned by this submod:
 * the depot itself, the arena cache reward, and the sorcery fragment.
 * Sorcery fragments are decorative drops here -- the {@code keyevent}
 * submod (not yet ported) is what consumes them into a tome.
 */
final class DepotTemplates {

    private static final Logger logger = Logger.getLogger(DepotTemplates.class.getName());

    static int depotTemplateId = -1;
    static int arenaCacheTemplateId = -1;
    static int sorceryFragmentTemplateId = -1;

    private DepotTemplates() {}

    static void register() {
        depotTemplateId = createDepot();
        arenaCacheTemplateId = createArenaCache();
        sorceryFragmentTemplateId = createSorceryFragment();
    }

    private static int createDepot() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.item.arena.depot");
            b.name("arena depot", "arena depots", "Contains a valuable cache of treasures.");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_NAMED,
                    ItemTypes.ITEM_TYPE_WOOD,
                    ItemTypes.ITEM_TYPE_NOTAKE,
                    ItemTypes.ITEM_TYPE_LOCKABLE,
                    ItemTypes.ITEM_TYPE_DECORATION,
                    ItemTypes.ITEM_TYPE_ONE_PER_TILE,
                    ItemTypes.ITEM_TYPE_OWNER_TURNABLE,
                    ItemTypes.ITEM_TYPE_REPAIRABLE,
                    ItemTypes.ITEM_TYPE_MISSION,
                    ItemTypes.ITEM_TYPE_PLANTABLE
            });
            b.imageNumber((short) 462);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(300, 300, 300);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.structure.war.supplydepot.2.0.");
            b.difficulty(5.0f);
            b.weightGrams(50000);
            b.material(Materials.MATERIAL_WOOD_BIRCH);
            b.value(5000);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[supplydepot] registered mod.item.arena.depot (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[supplydepot] failed to register depot template: " + e.getMessage());
            return -1;
        }
    }

    private static int createArenaCache() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.item.arenacache");
            b.name("arena cache", "arena caches",
                    "A cache of goods from a supply depot, waiting to be opened. What could be inside?");
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
            b.value(10000);
            b.isTraded(true);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[supplydepot] registered mod.item.arenacache (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[supplydepot] failed to register arena cache: " + e.getMessage());
            return -1;
        }
    }

    private static int createSorceryFragment() {
        try {
            ItemTemplateBuilder b = new ItemTemplateBuilder("mod.fragment.sorcery");
            b.name("sorcery fragment [1/10]", "sorcery fragments", "A scrap of a tome.");
            b.itemTypes(new short[] {
                    ItemTypes.ITEM_TYPE_MAGIC,
                    ItemTypes.ITEM_TYPE_FULLPRICE,
                    ItemTypes.ITEM_TYPE_NOSELLBACK,
                    ItemTypes.ITEM_TYPE_SERVERBOUND,
                    ItemTypes.ITEM_TYPE_ARTIFACT
            });
            b.imageNumber((short) 331);
            b.behaviourType((short) 1);
            b.combatDamage(0);
            b.decayTime(Long.MAX_VALUE);
            b.dimensions(1, 1, 1);
            b.primarySkill((int) MiscConstants.NOID);
            b.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            b.modelName("model.artifact.scrollbind.paper.");
            b.difficulty(5.0f);
            b.weightGrams(250);
            b.material(Materials.MATERIAL_CRYSTAL);
            b.value(5000);
            b.isTraded(true);
            ItemTemplate tpl = b.build();
            int id = tpl.getTemplateId();
            logger.info("[supplydepot] registered mod.fragment.sorcery (id=" + id + ")");
            return id;
        } catch (IOException e) {
            logger.warning("[supplydepot] failed to register sorcery fragment: " + e.getMessage());
            return -1;
        }
    }
}
