# mission

Epic-mission tweaks ported from Sindusk's WyvernMods MissionCreator plus the
two GM body-menu actions (Add Epic Mission / Remove Epic Mission) that
upstream shipped beside it.

## Provenance

Ported from `mod.sin.wyvern.MissionCreator` and `mod.sin.actions.MissionAdd
Action` / `MissionRemoveAction` in upstream WyvernMods. The upstream
`isMissionOkay*` filters also drop creatures that match `RareSpawns.isRare
Creature` or `Titans.isTitan*`; those custom-creature submods aren't ported
yet (see project_wyvernmods_port.md), so this jar drops only `isUnique()`
and `isSubmerged()` templates. Once the RareSpawn / Titan submods land,
they should re-extend the filter via a registry rather than re-implementing
the patches.

## Toggles

| Key | What it does |
| --- | --- |
| `addMissionCurrencyReward` | After completing an epic mission, add 2000-3999 iron coin to the player's wallet. Hooks `PlayerInfo.addToSleep` inside `TriggerEffect.effect`. |
| `preventMissionOceanSpawns` | Reject mission-creature templates that pass `isSwimming()` from spawning at all in `EpicServerStatus.spawnSingleCreature`. |
| `additionalHerbivoreChecks` | Filter the `isHerbivore` check in `createSlay*Mission` / `createSacrificeCreatureMission` to also drop submerged + unique templates. |
| `additionalMissionSlayableChecks` | Filter the `isEpicMissionSlayable` check in `createSlayCreatureMission` / `createSacrificeCreatureMission` to also drop submerged + unique templates. |
| `disableEpicMissionTypes` | Patch `EpicMissionEnum.getMissionChance` to return 0 for mission types 108, 120, 124. |
| `enableMissionPoll` | Periodically (every `pollIntervalSeconds`) drop expired missions and create one new mission for an entity that doesn't have one. Off by default. |
| `pollIntervalSeconds` | Cadence for the poll. Default 3600. |
| `enableMissionAddAction` | Add a GM body-menu entry that creates a random missing-entity mission. Power ≥ 5 only. Off by default. |
| `enableMissionRemoveAction` | Add a GM body-menu entry that removes the current mission for the first entity that has one. Power ≥ 5 only. Off by default. |

## Deferred

- `RareSpawns.isRareCreature` filter clause — comes back when the RareSpawn
  submod (#39) lands.
- `Titans.isTitan(Minion)` filter clauses — Titan submod (#38).
- `useValreiEntities` (Original Gods + Valrei entities) toggle — left out
  because there's no shared WyvernMods config flag yet; this jar uses
  Original Gods only for the periodic poll.
