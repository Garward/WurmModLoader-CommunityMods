package com.garward.wurmmodloader.mods.betterdig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.api.events.action.ActionAllowedOnVehicleEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.farming.DigCapacityOverrideEvent;
import com.garward.wurmmodloader.api.events.farming.DirtDestinationResolveEvent;
import com.garward.wurmmodloader.api.events.farming.DirtSourceResolveEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.ServerStartedListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.Items;                          // Type import (acceptable per architecture rules)
import com.wurmonline.server.NoSuchItemException;            // Type import (acceptable per architecture rules)
import com.wurmonline.server.behaviours.Actions;             // Type import (acceptable per architecture rules)
import com.wurmonline.server.creatures.Creature;             // Type import (acceptable per architecture rules)
import com.wurmonline.server.items.Item;                     // Type import (acceptable per architecture rules)
import com.wurmonline.server.items.ItemFactory;              // Type import (acceptable per architecture rules)
import com.wurmonline.server.items.ItemList;                 // Type import (acceptable per architecture rules)
import com.wurmonline.server.items.ItemTemplate;             // Type import (acceptable per architecture rules)
import com.wurmonline.server.items.ItemTemplateFactory;      // Type import (acceptable per architecture rules)
import com.wurmonline.server.zones.VolaTile;                 // Type import (acceptable per architecture rules)
import com.wurmonline.server.zones.Zones;                    // Type import (acceptable per architecture rules)

/**
 * BetterDig — zero-bytecode port of bdew's mod. Routes freshly-dug dirt / sand /
 * clay / peat / tar / moss into vehicles, dragged containers, and crates; and
 * lets {@code level}/{@code flatten} pull dirt from the same containers and the
 * ground. Also extends the mounted-action allowlist so users can dig from
 * horseback etc.
 *
 * <p>All behaviour is driven by framework events fired from
 * {@code TerraformingDigInnerPatch} / {@code FlatteningInnerPatch} /
 * {@code ActionAllowedOnVehiclePatch}. No Javassist here.</p>
 */
public class BetterDigMod implements WurmServerMod, Configurable, Initable, PreInitable, ServerStartedListener {

    private static final Logger logger = Logger.getLogger(BetterDigMod.class.getName());

    private static final Set<Integer> DIRT_TEMPLATES = new HashSet<>(Arrays.asList(
        ItemList.dirtPile, ItemList.clay, ItemList.sand, ItemList.tar, ItemList.peat, ItemList.moss));

    private int overrideClayWeight = -1;
    private int overrideMossWeight = -1;
    private int overridePeatWeight = -1;
    private int overrideTarWeight  = -1;

    private boolean digToVehicle     = false;
    private boolean dredgeToShip     = false;
    private boolean levelToVehicle   = false;
    private boolean digToCrates      = false;
    private boolean digToDragged     = false;
    private boolean levelToDragged   = false;
    private boolean levelFromVehicle = false;
    private boolean levelFromDragged = false;
    private boolean levelFromCrates  = false;
    private boolean levelFromGround  = false;

    private String allowWhenMounted = "";
    private final Set<Short> allowWhenMountedIds = new HashSet<>();

    @Override
    public void configure(Properties properties) {
        overrideClayWeight = Integer.parseInt(properties.getProperty("overrideClayWeight", "-1"), 10);
        overrideTarWeight  = Integer.parseInt(properties.getProperty("overrideTarWeight",  "-1"), 10);
        overridePeatWeight = Integer.parseInt(properties.getProperty("overridePeatWeight", "-1"), 10);
        overrideMossWeight = Integer.parseInt(properties.getProperty("overrideMossWeight", "-1"), 10);

        digToVehicle     = Boolean.parseBoolean(properties.getProperty("digToVehicle",     "false"));
        dredgeToShip     = Boolean.parseBoolean(properties.getProperty("dredgeToShip",     "false"));
        levelToVehicle   = Boolean.parseBoolean(properties.getProperty("levelToVehicle",   "false"));
        digToCrates      = Boolean.parseBoolean(properties.getProperty("digToCrates",      "false"));
        digToDragged     = Boolean.parseBoolean(properties.getProperty("digToDragged",     "false"));
        levelToDragged   = Boolean.parseBoolean(properties.getProperty("levelToDragged",   "false"));
        levelFromVehicle = Boolean.parseBoolean(properties.getProperty("levelFromVehicle", "false"));
        levelFromDragged = Boolean.parseBoolean(properties.getProperty("levelFromDragged", "false"));
        levelFromCrates  = Boolean.parseBoolean(properties.getProperty("levelFromCrates",  "false"));
        levelFromGround  = Boolean.parseBoolean(properties.getProperty("levelFromGround",  "false"));

        allowWhenMounted = properties.getProperty("allowWhenMounted", "");

        logger.info("overrideClayWeight=" + overrideClayWeight
            + " overrideTarWeight=" + overrideTarWeight
            + " overridePeatWeight=" + overridePeatWeight
            + " overrideMossWeight=" + overrideMossWeight);
        logger.info("digToVehicle=" + digToVehicle
            + " dredgeToShip=" + dredgeToShip
            + " levelToVehicle=" + levelToVehicle
            + " digToCrates=" + digToCrates
            + " digToDragged=" + digToDragged
            + " levelToDragged=" + levelToDragged);
        logger.info("levelFromVehicle=" + levelFromVehicle
            + " levelFromDragged=" + levelFromDragged
            + " levelFromCrates=" + levelFromCrates
            + " levelFromGround=" + levelFromGround);
        logger.info("allowWhenMounted=" + allowWhenMounted);
    }

    @Override public void preInit() {}
    @Override public void init()    {}

    @Override
    public void onServerStarted() {
        if (allowWhenMounted.isEmpty()) return;
        for (String raw : allowWhenMounted.split(",")) {
            String name = raw.trim().toUpperCase();
            if (name.isEmpty()) continue;
            try {
                short actionNum = Actions.class.getField(name).getShort(null);
                allowWhenMountedIds.add(actionNum);
                logger.info("Allowing mounted action " + name + " (" + actionNum + ")");
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.log(Level.SEVERE, "Unknown action name in allowWhenMounted: " + name, e);
            }
        }
    }

    // ───────────────────────── Action allowlist ─────────────────────────

    @SubscribeEvent
    public void onActionAllowedOnVehicle(ActionAllowedOnVehicleEvent event) {
        if (!event.isAllowed() && allowWhenMountedIds.contains(event.getAction())) {
            event.setAllowed(true);
        }
    }

    // ───────────────────────── Dirt destination ─────────────────────────

    @SubscribeEvent
    public void onDirtDestinationResolve(DirtDestinationResolveEvent event) {
        Item dirt = event.getDirt();
        if (dirt == null || !DIRT_TEMPLATES.contains(dirt.getTemplateId())) return;

        applyWeightOverrides(dirt);

        Creature performer = event.getPerformer();
        boolean dredging   = event.isDredging();
        boolean flattening = event.getContext() == DirtDestinationResolveEvent.Context.FLATTENING_GETDIRT;

        try {
            Item vehicle = getVehicleSafe(performer);
            if (vehicle != null && vehicle.isHollow()) {
                if ((dredging && dredgeToShip)
                        || (flattening && levelToVehicle)
                        || (!flattening && !dredging && digToVehicle)) {
                    if (insertIntoContainer(dirt, vehicle, performer)) {
                        event.setResolvedTarget(vehicle);
                        return;
                    }
                }
            }

            Item dragged = performer.getDraggedItem();
            if (dragged != null && dragged.isHollow()) {
                if ((flattening && levelToDragged)
                        || (!flattening && !dredging && digToDragged)) {
                    if (insertIntoContainer(dirt, dragged, performer)) {
                        event.setResolvedTarget(dragged);
                        return;
                    }
                }
            }

            // Fallback: drop in front of player (matches BetterDig behaviour when
            // no routing target matched) — only for non-flatten, non-dredge dig.
            if (!flattening && !dredging) {
                dirt.putItemInfrontof(performer);
                event.setResolvedTarget(null);
                return;
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error routing dirt destination", t);
        }
    }

    private void applyWeightOverrides(Item dirt) {
        int templateId = dirt.getTemplateId();
        if (templateId == ItemList.clay && overrideClayWeight > 0) {
            dirt.setWeight(overrideClayWeight * 1000, false);
        } else if (templateId == ItemList.moss && overrideMossWeight > 0) {
            dirt.setWeight(overrideMossWeight * 1000, false);
        } else if (templateId == ItemList.peat && overridePeatWeight > 0) {
            dirt.setWeight(overridePeatWeight * 1000, false);
        } else if (templateId == ItemList.tar && overrideTarWeight > 0) {
            dirt.setWeight(overrideTarWeight * 1000, false);
        }
    }

    private boolean insertIntoContainer(Item item, Item vehicle, Creature performer) {
        if (digToCrates && item.getTemplate().isBulk() && item.getRarity() == 0) {
            for (Item container : vehicle.getAllItems(true)) {
                if (container.isCrate() && container.canAddToCrate(item)) {
                    if (item.AddBulkItemToCrate(performer, container)) {
                        performer.getCommunicator().sendNormalServerMessage(
                            String.format("You put the %s in the %s in your %s.",
                                item.getName(), container.getName(), vehicle.getName()));
                        return true;
                    }
                }
            }
        }
        if (vehicle.getNumItemsNotCoins() < 100
                && vehicle.getFreeVolume() >= item.getVolume()
                && vehicle.insertItem(item)) {
            performer.getCommunicator().sendNormalServerMessage(
                String.format("You put the %s in the %s.", item.getName(), vehicle.getName()));
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage(
            String.format("The %s is too full to hold the %s.", vehicle.getName(), item.getName()));
        return false;
    }

    // ─────────────────────────── Dirt source ────────────────────────────

    @SubscribeEvent
    public void onDirtSourceResolve(DirtSourceResolveEvent event) {
        if (event.getResolvedItem() != null) return;
        Creature performer = event.getPerformer();
        int templateId     = event.getTemplateId();

        if (levelFromGround) {
            VolaTile tile = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
            if (tile != null) {
                for (Item ground : tile.getItems()) {
                    if (ground.getTemplateId() == templateId) {
                        event.setResolvedItem(ground);
                        return;
                    }
                }
            }
        }

        if (levelFromVehicle) {
            Item vehicle = getVehicleSafe(performer);
            if (vehicle != null && vehicle.isHollow()) {
                Item found = findInContainer(templateId, vehicle, performer);
                if (found != null) { event.setResolvedItem(found); return; }
            }
        }

        if (levelFromDragged) {
            Item dragged = performer.getDraggedItem();
            if (dragged != null && dragged.isHollow()) {
                Item found = findInContainer(templateId, dragged, performer);
                if (found != null) { event.setResolvedItem(found); return; }
            }
        }
    }

    private Item findInContainer(int templateId, Item vehicle, Creature performer) {
        try {
            Item direct = vehicle.findItem(templateId);
            if (direct != null) return direct;
            if (!levelFromCrates) return null;

            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
            for (Item container : vehicle.getAllItems(true)) {
                if (!container.isCrate()) continue;
                for (Item bulk : container.getItems()) {
                    if (bulk.getRealTemplateId() == templateId && bulk.getBulkNumsFloat(false) >= 1f) {
                        bulk.setWeight(bulk.getWeightGrams() - template.getVolume(), true);
                        Item newItem = ItemFactory.createItem(templateId, bulk.getQualityLevel(),
                            template.getMaterial(), (byte) 0, null);
                        newItem.setLastOwnerId(performer.getWurmId());
                        performer.getInventory().insertItem(newItem, true);
                        performer.getCommunicator().sendNormalServerMessage(
                            String.format("You grab a %s from the %s in your %s.",
                                newItem.getName(), container.getName(), vehicle.getName()));
                        return newItem;
                    }
                }
            }
            return null;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error looking up item " + templateId + " in " + vehicle, t);
            return null;
        }
    }

    // ────────────────────────── Capacity gates ──────────────────────────

    @SubscribeEvent
    public void onDigCapacityOverride(DigCapacityOverrideEvent event) {
        // BetterDig only skips the caps when NOT piling — i.e. when dirt is being
        // redirected into a vehicle / dragged / crates (toPile=false means
        // "treat this as the vehicle-routing path"). Match that exactly.
        if (event.isToPile()) return;

        switch (event.getKind()) {
            case NUM_ITEMS_NOT_COINS:
                event.setOverrideValue(0L);
                break;
            case CAN_CARRY:
                event.setOverrideValue(1L);
                break;
            case FREE_VOLUME:
                event.setOverrideValue(1000L);
                break;
        }
    }

    // ───────────────────────────── Helpers ──────────────────────────────

    private static Item getVehicleSafe(Creature pilot) {
        try {
            long id = pilot.getVehicle();
            if (id != -10L) return Items.getItem(id);
        } catch (NoSuchItemException ignored) {}
        return null;
    }
}
