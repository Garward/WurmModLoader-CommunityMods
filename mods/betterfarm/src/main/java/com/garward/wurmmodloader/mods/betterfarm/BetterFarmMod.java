package com.garward.wurmmodloader.mods.betterfarm;

import com.garward.wurmmodloader.api.events.action.ActionAllowedOnVehicleEvent;
import com.garward.wurmmodloader.api.events.action.ItemMenuBuildEvent;
import com.garward.wurmmodloader.api.events.action.TileMenuBuildEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.eventlogic.area.AreaActionDef;
import com.garward.wurmmodloader.api.events.eventlogic.area.AreaActionType;
import com.garward.wurmmodloader.api.events.farming.PlanterItemAcceptEvent;
import com.garward.wurmmodloader.api.events.farming.TileDirtConsumeEvent;
import com.garward.wurmmodloader.api.events.item.BulkStackNameEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.ModEntry;
import com.garward.wurmmodloader.modloader.interfaces.ModListener;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.ServerStartedListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.garward.wurmmodloader.modsupport.actions.area.AreaActionMenus;
import com.garward.wurmmodloader.modsupport.actions.area.AreaActionRegistrar;
import com.garward.wurmmodloader.mods.betterfarm.fields.FieldActions;
import com.garward.wurmmodloader.mods.betterfarm.planter.PlanterHooks;
import com.garward.wurmmodloader.mods.betterfarm.planter.PlanterRackPickAction;
import com.garward.wurmmodloader.mods.betterfarm.planter.PlanterRackPlantAction;
import com.garward.wurmmodloader.mods.betterfarm.trees.TreeActions;
import com.garward.wurmmodloader.mods.betterfarm.trellis.TrellisActions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BetterFarm — zero-bytecode port of bdew's area-action mod. Reference user
 * of the framework's {@code eventlogic.area} system: radius tiers, handler
 * registration, and menu injection all live in modsupport now; this mod only
 * contributes farming-specific handlers + config.
 */
public class BetterFarmMod implements WurmServerMod, Configurable, PreInitable, Initable, ServerStartedListener, ModListener {

    private static final Logger logger = Logger.getLogger("BetterFarm");

    public static int extraHarvest = 0;

    public static void logException(String msg, Throwable e) { if (logger != null) logger.log(Level.SEVERE, msg, e); }
    public static void logWarning(String msg)   { if (logger != null) logger.log(Level.WARNING, msg); }
    public static void logInfo(String msg)      { if (logger != null) logger.log(Level.INFO, msg); }
    public static void logDebug(String msg)     { if (logger != null) logger.log(Level.FINE, msg); }

    private static List<AreaActionDef> cultivateLevels, sowLevels, tendLevels, harvestLevels,
                                       replantLevels, pickLevels, pruneLevels, plantLevels;

    private static float planterPlantSkill, planterPickSkill;
    private static String addPotables;

    public static boolean allowMountedAreaActions = false;
    public static boolean allowInfectedTrees = false;
    public static Set<Short> allowWhenMountedIds = new HashSet<>();

    private static AreaActionRegistrar registrar;

    private List<AreaActionDef> parseDef(String str) {
        List<AreaActionDef> result = new ArrayList<>();
        if (str == null || str.isEmpty()) return result;
        for (String part : str.trim().split(",")) {
            if (!part.contains("@")) { logWarning("Invalid skill spec: " + part); continue; }
            String[] split = part.split("@");
            result.add(new AreaActionDef(Integer.parseInt(split[0]), Float.parseFloat(split[1])));
        }
        return result;
    }

    @Override
    public void configure(Properties properties) {
        cultivateLevels = parseDef(properties.getProperty("cultivate"));
        sowLevels       = parseDef(properties.getProperty("sow"));
        tendLevels      = parseDef(properties.getProperty("farm"));
        harvestLevels   = parseDef(properties.getProperty("harvest"));
        replantLevels   = parseDef(properties.getProperty("replant"));
        pickLevels      = parseDef(properties.getProperty("pick"));
        pruneLevels     = parseDef(properties.getProperty("prune"));
        plantLevels     = parseDef(properties.getProperty("plant"));
        planterPlantSkill = Float.parseFloat(properties.getProperty("planterPlantSkill", "-1"));
        planterPickSkill  = Float.parseFloat(properties.getProperty("planterPickSkill", "-1"));
        addPotables       = properties.getProperty("addPotables", "");
        allowMountedAreaActions = Boolean.parseBoolean(properties.getProperty("allowMountedAreaActions", "false"));
        allowInfectedTrees      = Boolean.parseBoolean(properties.getProperty("allowInfectedTrees", "false"));
    }

    @Override
    public void preInit() {
        ModActions.init();
    }

    @Override
    public void init() { }

    @Override
    public void modInitialized(ModEntry<?> modEntry) {
        if (modEntry.getWurmMod().getClass().getName().endsWith(".cropmod.CropMod")) {
            extraHarvest = Integer.parseInt(modEntry.getProperties().getProperty("extraHarvest", "0"));
            logInfo("Cropmod detected, extraHarvest = " + extraHarvest);
        }
    }

    @Override
    public void onServerStarted() {
        if (!addPotables.isEmpty()) PlanterHooks.addPotables(addPotables);

        registrar = new AreaActionRegistrar(p -> {
            if (allowMountedAreaActions) allowWhenMountedIds.add(p.actionEntry.getNumber());
        });
        registrar
                .register(AreaActionType.CULTIVATE, cultivateLevels)
                .register(AreaActionType.SOW, sowLevels)
                .register(AreaActionType.FARM, tendLevels)
                .register(AreaActionType.HARVEST, harvestLevels)
                .register(AreaActionType.HARVEST_AND_REPLANT, replantLevels)
                .register(AreaActionType.PICK_SPROUT, pickLevels)
                .register(AreaActionType.PRUNE, pruneLevels)
                .register(AreaActionType.PLANT, plantLevels);

        if (planterPlantSkill > 0) ModActions.registerAction(new PlanterRackPlantAction(planterPlantSkill));
        if (planterPickSkill > 0)  ModActions.registerAction(new PlanterRackPickAction(planterPickSkill));

        TrellisActions.register();
        FieldActions.register();
        TreeActions.register();
    }

    // ---- tile / item menu injection ----

    @SubscribeEvent
    public void onTileMenuBuild(TileMenuBuildEvent e) {
        if (registrar == null) return;
        AreaActionMenus.mutateTileMenu(e.getAvailableActions(), e.getPerformer(),
                e.getTarget(), e.isOnSurface(), e.getSource(), registrar.performers());
    }

    @SubscribeEvent
    public void onItemMenuBuild(ItemMenuBuildEvent e) {
        if (registrar == null) return;
        AreaActionMenus.mutateItemMenu(e.getAvailableActions(), e.getPerformer(),
                e.getTargetId(), e.getSource(), registrar.performers());
    }

    // ---- tile-dirt weight-based consumption ----

    @SubscribeEvent
    public void onTileDirtConsume(TileDirtConsumeEvent e) {
        e.setConsumed(true);
    }

    // ---- planter potables ----

    @SubscribeEvent
    public void onPlanterItemAccept(PlanterItemAcceptEvent e) {
        if (e.isAccepted()) return;
        if (e.getHerb() != null && PlanterHooks.isPotable(e.getHerb().getTemplateId())) {
            e.setAccepted(true);
        }
    }

    // ---- bulk-stack name canonicalization ----

    @SubscribeEvent
    public void onBulkStackName(BulkStackNameEvent e) {
        String n = e.getResolvedName();
        if (n != null && n.startsWith("pile of ")) {
            e.setResolvedName(n.replace("pile of ", ""));
        }
    }

    // ---- mounted-action allowlist ----

    @SubscribeEvent
    public void onActionAllowedOnVehicle(ActionAllowedOnVehicleEvent e) {
        if (!allowMountedAreaActions) return;
        if (allowWhenMountedIds.contains(Short.valueOf(e.getAction()))) {
            e.setAllowed(true);
        }
    }
}
