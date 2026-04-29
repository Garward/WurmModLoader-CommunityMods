# extraactions

Small grab-bag of right-click actions ported from Sindusk's WyvernMods
`enableActionModule` block. Each one is independently toggleable.

## Actions

| Toggle | Menu | Filter | Effect |
|---|---|---|---|
| `unequipAllArmour` | own body | always | unequip every `isArmour()` item back into inventory |
| `receiveAllMail`   | mailbox item | mailbox is courier-enchanted, empty, within 4m | pull every non-CoD/non-rejected mail item into the mailbox in one shot |
| `smoothTerrain`    | tile menu | performer power ≥ 5 | smooth bumps/slants in a 20×20 area |
| `creatureReport`   | own body | performer power ≥ 5 | event-log dump of aggro/passive creature counts |

All toggles default to **off**. Enable in `extraactions.properties`.

## What did NOT come along

- **Arena teleport / Arena escape** — already shipped by `mods/teleport`.
- **Sorcery split / combine** — depend on `SorceryFragment` item template +
  `ItemUtil.createRandomSorcery` + `MiscChanges.sendServerTabMessage`. Will
  land with the Item / Crystals submods.
- **Leaderboard / Leaderboard skill** — depend on upstream's custom
  `LeaderboardQuestion` and `LeaderboardSkillQuestion` (~360 lines of
  question UI). Pending the Question port.
- **Mission add / Mission remove** — belong with the Mission submod.

## Provenance

Ported from `mod/sin/actions/{UnequipAllAction, ReceiveMailAction,
SmoothTerrainAction, CreatureReportAction}.java` (WyvernMods). The upstream
`ModAction` + `BehaviourProvider` wrapper boilerplate is dropped — the
framework's menu-build events (`BodyMenuPopulateEvent`, `ItemMenuBuildEvent`,
`TileMenuBuildEvent`) handle entry filtering, and an `ActionPerformer`
registered with `ModActions.registerActionPerformer(...)` handles dispatch.
