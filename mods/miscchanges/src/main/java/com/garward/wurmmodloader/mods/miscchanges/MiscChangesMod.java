package com.garward.wurmmodloader.mods.miscchanges;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MiscChangesMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(MiscChangesMod.class.getName());

    // Info tab — appears for all players on login.
    boolean enableInfoTab = true;
    String infoTabName = "Server";
    final List<String> infoTabLines = new ArrayList<>();

    // Bug fixes & QoL.
    boolean ignoreBridgeChecks = true;
    boolean disableMailboxUsageWhileLoaded = true;
    boolean increasedLegendaryCreatures = true;
    int increasedLegendaryFrequency = 5;
    boolean allowFacebreykerNaturalSpawn = true;
    boolean announcePlayerTitles = true;
    boolean improveCombinedLeather = true;
    boolean allowModdedImproveTemplates = true;
    boolean fatigueActionOverride = true;
    boolean fixPortalIssues = true;
    boolean disableMinimumShieldDamage = true;
    boolean disableGMEmoteLimit = true;
    boolean creatureArcheryWander = true;
    boolean globalDeathTabs = true;
    boolean disablePvPOnlyDeathTabs = true;
    boolean fixLibilaCrossingIssues = true;
    boolean higherFoodAffinities = true;
    boolean fasterCharcoalBurn = true;
    boolean uncapTraderItemCount = true;
    boolean logExcessiveActions = true;
    boolean useDynamicSkillRate = true;
    boolean reduceLockpickBreaking = true;
    boolean allowFreedomMyceliumAbsorb = true;
    boolean largerHouses = true;
    boolean reduceImbuePower = true;
    boolean fixVehicleSpeeds = true;
    boolean reduceMailingCosts = true;
    boolean guardTargetChanges = true;
    boolean enableLibilaStrongwallPvE = true;
    boolean royalCookNoFoodDecay = true;
    boolean mayorsCommandAbandonedVehicles = true;
    boolean opulenceFoodAffinityTimerIncrease = true;
    boolean disableFoodFirstBiteBonus = true;
    boolean bedQualitySleepBonus = true;
    boolean royalSmithImproveFaster = true;
    boolean fixMountedBodyStrength = true;
    boolean adjustedFoodBiteFill = true;
    boolean rareMaterialImprove = true;
    boolean rarityWindowBadLuckProtection = true;
    boolean rareCreationAdjustments = false;
    boolean alwaysArmourTitleBenefits = true;
    boolean tomeUsageAnyAltar = true;
    boolean keyOfHeavensLoginOnly = true;
    boolean lessFillingDrinks = true;
    boolean disableHelpGMCommands = true;
    boolean reduceActionInterruptOnDamage = true;
    boolean fixMissionNullPointerException = true;
    boolean disableSmeltingPots = true;
    boolean hideSorceryBuffBar = true;
    boolean sqlAchievementFix = true;
    boolean changePumpkinKingTitle = true;
    boolean changeDeityPassives = true;

    // ---- Vanilla item-template tweaks (folded from upstream ItemMod) ----
    boolean combineLeather = true;
    boolean combineLogs = true;
    boolean reduceLogVolume = true;
    boolean reduceKindlingVolume = true;
    boolean droppableSleepPowder = true;
    boolean oneHandedLongSpear = false;
    boolean loadableMailbox = true;
    boolean loadableBellTower = true;
    boolean loadableTrashBin = true;
    boolean loadableAltars = true;
    boolean decorationStoneKeystone = true;
    boolean decorationMarbleKeystone = true;
    boolean decorationSkull = true;
    boolean reduceDirtDifficulty = false;
    boolean reduceSandDifficulty = false;
    boolean reduceSandstoneDifficulty = false;
    boolean setSilverMirrorPrice = false;
    int silverMirrorPriceIron = 200000;
    boolean setGoldMirrorPrice = false;
    int goldMirrorPriceIron = 1000000;
    boolean setCreatureCratePrice = false;
    int creatureCratePriceIron = 100000;
    boolean setResurrectionStonePrice = false;
    int resurrectionStonePriceIron = 20000;
    boolean setShakerOrbPrice = false;
    int shakerOrbPriceIron = 20000;
    boolean adjustStatueFragmentCount = false;
    boolean removeLockpickSkillRequirement = false;

    @Override
    public void configure(Properties properties) {
        enableInfoTab = bool(properties, "enableInfoTab", enableInfoTab);
        infoTabName = str(properties, "infoTabName", infoTabName);
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("infoTabLine")) {
                String value = properties.getProperty(key);
                if (value != null && !value.isEmpty()) {
                    infoTabLines.add(value);
                }
            }
        }

        ignoreBridgeChecks = bool(properties, "ignoreBridgeChecks", ignoreBridgeChecks);
        disableMailboxUsageWhileLoaded = bool(properties, "disableMailboxUsageWhileLoaded", disableMailboxUsageWhileLoaded);
        increasedLegendaryCreatures = bool(properties, "increasedLegendaryCreatures", increasedLegendaryCreatures);
        increasedLegendaryFrequency = intp(properties, "increasedLegendaryFrequency", increasedLegendaryFrequency);
        allowFacebreykerNaturalSpawn = bool(properties, "allowFacebreykerNaturalSpawn", allowFacebreykerNaturalSpawn);
        announcePlayerTitles = bool(properties, "announcePlayerTitles", announcePlayerTitles);
        improveCombinedLeather = bool(properties, "improveCombinedLeather", improveCombinedLeather);
        allowModdedImproveTemplates = bool(properties, "allowModdedImproveTemplates", allowModdedImproveTemplates);
        fatigueActionOverride = bool(properties, "fatigueActionOverride", fatigueActionOverride);
        fixPortalIssues = bool(properties, "fixPortalIssues", fixPortalIssues);
        disableMinimumShieldDamage = bool(properties, "disableMinimumShieldDamage", disableMinimumShieldDamage);
        disableGMEmoteLimit = bool(properties, "disableGMEmoteLimit", disableGMEmoteLimit);
        creatureArcheryWander = bool(properties, "creatureArcheryWander", creatureArcheryWander);
        globalDeathTabs = bool(properties, "globalDeathTabs", globalDeathTabs);
        disablePvPOnlyDeathTabs = bool(properties, "disablePvPOnlyDeathTabs", disablePvPOnlyDeathTabs);
        fixLibilaCrossingIssues = bool(properties, "fixLibilaCrossingIssues", fixLibilaCrossingIssues);
        higherFoodAffinities = bool(properties, "higherFoodAffinities", higherFoodAffinities);
        fasterCharcoalBurn = bool(properties, "fasterCharcoalBurn", fasterCharcoalBurn);
        uncapTraderItemCount = bool(properties, "uncapTraderItemCount", uncapTraderItemCount);
        logExcessiveActions = bool(properties, "logExcessiveActions", logExcessiveActions);
        useDynamicSkillRate = bool(properties, "useDynamicSkillRate", useDynamicSkillRate);
        reduceLockpickBreaking = bool(properties, "reduceLockpickBreaking", reduceLockpickBreaking);
        allowFreedomMyceliumAbsorb = bool(properties, "allowFreedomMyceliumAbsorb", allowFreedomMyceliumAbsorb);
        largerHouses = bool(properties, "largerHouses", largerHouses);
        reduceImbuePower = bool(properties, "reduceImbuePower", reduceImbuePower);
        fixVehicleSpeeds = bool(properties, "fixVehicleSpeeds", fixVehicleSpeeds);
        reduceMailingCosts = bool(properties, "reduceMailingCosts", reduceMailingCosts);
        guardTargetChanges = bool(properties, "guardTargetChanges", guardTargetChanges);
        enableLibilaStrongwallPvE = bool(properties, "enableLibilaStrongwallPvE", enableLibilaStrongwallPvE);
        royalCookNoFoodDecay = bool(properties, "royalCookNoFoodDecay", royalCookNoFoodDecay);
        mayorsCommandAbandonedVehicles = bool(properties, "mayorsCommandAbandonedVehicles", mayorsCommandAbandonedVehicles);
        opulenceFoodAffinityTimerIncrease = bool(properties, "opulenceFoodAffinityTimerIncrease", opulenceFoodAffinityTimerIncrease);
        disableFoodFirstBiteBonus = bool(properties, "disableFoodFirstBiteBonus", disableFoodFirstBiteBonus);
        bedQualitySleepBonus = bool(properties, "bedQualitySleepBonus", bedQualitySleepBonus);
        royalSmithImproveFaster = bool(properties, "royalSmithImproveFaster", royalSmithImproveFaster);
        fixMountedBodyStrength = bool(properties, "fixMountedBodyStrength", fixMountedBodyStrength);
        adjustedFoodBiteFill = bool(properties, "adjustedFoodBiteFill", adjustedFoodBiteFill);
        rareMaterialImprove = bool(properties, "rareMaterialImprove", rareMaterialImprove);
        rarityWindowBadLuckProtection = bool(properties, "rarityWindowBadLuckProtection", rarityWindowBadLuckProtection);
        rareCreationAdjustments = bool(properties, "rareCreationAdjustments", rareCreationAdjustments);
        alwaysArmourTitleBenefits = bool(properties, "alwaysArmourTitleBenefits", alwaysArmourTitleBenefits);
        tomeUsageAnyAltar = bool(properties, "tomeUsageAnyAltar", tomeUsageAnyAltar);
        keyOfHeavensLoginOnly = bool(properties, "keyOfHeavensLoginOnly", keyOfHeavensLoginOnly);
        lessFillingDrinks = bool(properties, "lessFillingDrinks", lessFillingDrinks);
        disableHelpGMCommands = bool(properties, "disableHelpGMCommands", disableHelpGMCommands);
        reduceActionInterruptOnDamage = bool(properties, "reduceActionInterruptOnDamage", reduceActionInterruptOnDamage);
        fixMissionNullPointerException = bool(properties, "fixMissionNullPointerException", fixMissionNullPointerException);
        disableSmeltingPots = bool(properties, "disableSmeltingPots", disableSmeltingPots);
        hideSorceryBuffBar = bool(properties, "hideSorceryBuffBar", hideSorceryBuffBar);
        sqlAchievementFix = bool(properties, "sqlAchievementFix", sqlAchievementFix);
        changePumpkinKingTitle = bool(properties, "changePumpkinKingTitle", changePumpkinKingTitle);
        changeDeityPassives = bool(properties, "changeDeityPassives", changeDeityPassives);

        combineLeather             = bool(properties, "combineLeather",             combineLeather);
        combineLogs                = bool(properties, "combineLogs",                combineLogs);
        reduceLogVolume            = bool(properties, "reduceLogVolume",            reduceLogVolume);
        reduceKindlingVolume       = bool(properties, "reduceKindlingVolume",       reduceKindlingVolume);
        droppableSleepPowder       = bool(properties, "droppableSleepPowder",       droppableSleepPowder);
        oneHandedLongSpear         = bool(properties, "oneHandedLongSpear",         oneHandedLongSpear);
        loadableMailbox            = bool(properties, "loadableMailbox",            loadableMailbox);
        loadableBellTower          = bool(properties, "loadableBellTower",          loadableBellTower);
        loadableTrashBin           = bool(properties, "loadableTrashBin",           loadableTrashBin);
        loadableAltars             = bool(properties, "loadableAltars",             loadableAltars);
        decorationStoneKeystone    = bool(properties, "decorationStoneKeystone",    decorationStoneKeystone);
        decorationMarbleKeystone   = bool(properties, "decorationMarbleKeystone",   decorationMarbleKeystone);
        decorationSkull            = bool(properties, "decorationSkull",            decorationSkull);
        reduceDirtDifficulty       = bool(properties, "reduceDirtDifficulty",       reduceDirtDifficulty);
        reduceSandDifficulty       = bool(properties, "reduceSandDifficulty",       reduceSandDifficulty);
        reduceSandstoneDifficulty  = bool(properties, "reduceSandstoneDifficulty",  reduceSandstoneDifficulty);
        setSilverMirrorPrice       = bool(properties, "setSilverMirrorPrice",       setSilverMirrorPrice);
        silverMirrorPriceIron      = intp(properties, "silverMirrorPriceIron",      silverMirrorPriceIron);
        setGoldMirrorPrice         = bool(properties, "setGoldMirrorPrice",         setGoldMirrorPrice);
        goldMirrorPriceIron        = intp(properties, "goldMirrorPriceIron",        goldMirrorPriceIron);
        setCreatureCratePrice      = bool(properties, "setCreatureCratePrice",      setCreatureCratePrice);
        creatureCratePriceIron     = intp(properties, "creatureCratePriceIron",     creatureCratePriceIron);
        setResurrectionStonePrice  = bool(properties, "setResurrectionStonePrice",  setResurrectionStonePrice);
        resurrectionStonePriceIron = intp(properties, "resurrectionStonePriceIron", resurrectionStonePriceIron);
        setShakerOrbPrice          = bool(properties, "setShakerOrbPrice",          setShakerOrbPrice);
        shakerOrbPriceIron         = intp(properties, "shakerOrbPriceIron",         shakerOrbPriceIron);
        adjustStatueFragmentCount  = bool(properties, "adjustStatueFragmentCount",  adjustStatueFragmentCount);
        removeLockpickSkillRequirement = bool(properties, "removeLockpickSkillRequirement",
                                              removeLockpickSkillRequirement);
    }

    @Override
    public void preInit() {
        // Bytecode patches land in follow-up tasks (Wyvern port: MiscChanges
        // behavior batches). Skeleton only — confirms gradle module + class
        // loader pickup work end-to-end for the wyvern split-mod template.
        logger.info("[miscchanges] loaded skeleton — behaviors not yet ported");
    }

    @SubscribeEvent
    public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
        try {
            ItemTweaks.applyAll(this);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[miscchanges] failed to apply item-template tweaks", t);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        try {
            CreationTweaks.applyAll(this);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[miscchanges] failed to apply creation tweaks", t);
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int intp(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[miscchanges] " + key + "=" + v + " is not an integer; using default " + def);
            return def;
        }
    }

    private static String str(Properties p, String key, String def) {
        String v = p.getProperty(key);
        return v == null ? def : v;
    }
}
