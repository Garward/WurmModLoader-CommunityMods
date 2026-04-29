package com.garward.wurmmodloader.mods.arena;

import java.util.Properties;

final class ArenaConfig {

    boolean enabled = true;

    boolean equipHorseGearByLeading = true;
    boolean lockpickingImprovements = true;

    boolean placeDeedsOutsideKingdomInfluence = true;
    boolean disablePMKs = true;
    boolean disablePlayerChampions = true;

    boolean arenaAggression = true;
    boolean enemyTitleHook = true;
    boolean enemyPresenceOnAggression = true;
    boolean useAggressionForNearbyEnemies = true;

    boolean adjustFightSkillGain = true;
    boolean allowSameKingdomFightSkillGains = true;
    boolean disablePvPCorpseProtection = true;
    boolean allowAttackingSameKingdomGuards = true;
    boolean fixGuardsAttackingThemselves = true;
    boolean allowArcheringOnSameKingdomDeeds = true;

    boolean bypassHousePermissions = true;
    boolean allowStealingAgainstDeityWishes = true;
    boolean sameKingdomVehicleTheft = true;
    boolean sameKingdomPermissionsAdjustments = true;
    boolean sameKingdomVillageWarfare = true;
    boolean bypassPlantedPermissionChecks = true;

    boolean disableFarwalkerItems = true;
    boolean alwaysAllowAffinitySteal = true;
    boolean adjustMineDoorDamage = true;
    boolean disableCAHelpOnPvP = true;
    boolean capMaximumGuards = true;
    boolean disableTowerConstruction = true;
    boolean adjustLocalRange = true;
    boolean disableKarmaTeleport = true;
    boolean limitLeadCreatures = true;
    boolean adjustBashTimer = true;
    boolean reducedMineDoorOpenTime = true;
    boolean sendNewSpawnQuestionOnPvP = true;
    boolean makeFreedomFavoredKingdom = true;
    boolean crownInfluenceOnAggression = true;

    boolean disableOWFL = true;
    boolean resurrectionStonesProtectSkill = true;
    boolean resurrectionStonesProtectFightSkill = true;
    boolean resurrectionStonesProtectAffinities = true;

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);

        equipHorseGearByLeading = bool(p, "equipHorseGearByLeading", equipHorseGearByLeading);
        lockpickingImprovements = bool(p, "lockpickingImprovements", lockpickingImprovements);

        placeDeedsOutsideKingdomInfluence = bool(p, "placeDeedsOutsideKingdomInfluence", placeDeedsOutsideKingdomInfluence);
        disablePMKs = bool(p, "disablePMKs", disablePMKs);
        disablePlayerChampions = bool(p, "disablePlayerChampions", disablePlayerChampions);

        arenaAggression = bool(p, "arenaAggression", arenaAggression);
        enemyTitleHook = bool(p, "enemyTitleHook", enemyTitleHook);
        enemyPresenceOnAggression = bool(p, "enemyPresenceOnAggression", enemyPresenceOnAggression);
        useAggressionForNearbyEnemies = bool(p, "useAggressionForNearbyEnemies", useAggressionForNearbyEnemies);

        adjustFightSkillGain = bool(p, "adjustFightSkillGain", adjustFightSkillGain);
        allowSameKingdomFightSkillGains = bool(p, "allowSameKingdomFightSkillGains", allowSameKingdomFightSkillGains);
        disablePvPCorpseProtection = bool(p, "disablePvPCorpseProtection", disablePvPCorpseProtection);
        allowAttackingSameKingdomGuards = bool(p, "allowAttackingSameKingdomGuards", allowAttackingSameKingdomGuards);
        fixGuardsAttackingThemselves = bool(p, "fixGuardsAttackingThemselves", fixGuardsAttackingThemselves);
        allowArcheringOnSameKingdomDeeds = bool(p, "allowArcheringOnSameKingdomDeeds", allowArcheringOnSameKingdomDeeds);

        bypassHousePermissions = bool(p, "bypassHousePermissions", bypassHousePermissions);
        allowStealingAgainstDeityWishes = bool(p, "allowStealingAgainstDeityWishes", allowStealingAgainstDeityWishes);
        sameKingdomVehicleTheft = bool(p, "sameKingdomVehicleTheft", sameKingdomVehicleTheft);
        sameKingdomPermissionsAdjustments = bool(p, "sameKingdomPermissionsAdjustments", sameKingdomPermissionsAdjustments);
        sameKingdomVillageWarfare = bool(p, "sameKingdomVillageWarfare", sameKingdomVillageWarfare);
        bypassPlantedPermissionChecks = bool(p, "bypassPlantedPermissionChecks", bypassPlantedPermissionChecks);

        disableFarwalkerItems = bool(p, "disableFarwalkerItems", disableFarwalkerItems);
        alwaysAllowAffinitySteal = bool(p, "alwaysAllowAffinitySteal", alwaysAllowAffinitySteal);
        adjustMineDoorDamage = bool(p, "adjustMineDoorDamage", adjustMineDoorDamage);
        disableCAHelpOnPvP = bool(p, "disableCAHelpOnPvP", disableCAHelpOnPvP);
        capMaximumGuards = bool(p, "capMaximumGuards", capMaximumGuards);
        disableTowerConstruction = bool(p, "disableTowerConstruction", disableTowerConstruction);
        adjustLocalRange = bool(p, "adjustLocalRange", adjustLocalRange);
        disableKarmaTeleport = bool(p, "disableKarmaTeleport", disableKarmaTeleport);
        limitLeadCreatures = bool(p, "limitLeadCreatures", limitLeadCreatures);
        adjustBashTimer = bool(p, "adjustBashTimer", adjustBashTimer);
        reducedMineDoorOpenTime = bool(p, "reducedMineDoorOpenTime", reducedMineDoorOpenTime);
        sendNewSpawnQuestionOnPvP = bool(p, "sendNewSpawnQuestionOnPvP", sendNewSpawnQuestionOnPvP);
        makeFreedomFavoredKingdom = bool(p, "makeFreedomFavoredKingdom", makeFreedomFavoredKingdom);
        crownInfluenceOnAggression = bool(p, "crownInfluenceOnAggression", crownInfluenceOnAggression);

        disableOWFL = bool(p, "disableOWFL", disableOWFL);
        resurrectionStonesProtectSkill = bool(p, "resurrectionStonesProtectSkill", resurrectionStonesProtectSkill);
        resurrectionStonesProtectFightSkill = bool(p, "resurrectionStonesProtectFightSkill", resurrectionStonesProtectFightSkill);
        resurrectionStonesProtectAffinities = bool(p, "resurrectionStonesProtectAffinities", resurrectionStonesProtectAffinities);
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
