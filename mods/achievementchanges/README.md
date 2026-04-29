# achievementchanges

Renames internal-looking vanilla achievement strings (`Invisible:CutTree` →
"Getting Wood", `PlayerkillBow` → "Arrow To The Knee", …), fills in missing
requirement descriptions, and curates a whitelist of achievements suitable
for a leaderboard. Ported from Sindusk's WyvernMods AchievementChanges
module.

Presence of the jar is the master toggle — there are no upstream sub-flags
for this submod.

## What it does

At `ServerStartedEvent`:

1. **Renames**: ~19 specific achievement template names from internal
   identifiers to player-readable strings, plus a blanket strip of any
   leading `Invisible:` prefix. Carries to the in-game UI through
   `AchievementTemplate.setName`.
2. **Requirement strings**: ~24 achievements that ship with empty
   `requirement` fields get player-facing descriptions ("Pick a mushroom",
   "Win a spar", …). Written via `ReflectionUtil.setPrivateField`.
3. **Curate**: walks the full `Achievement.templates` map and keeps every
   template that has a non-empty requirement, isn't cooking-only, and isn't
   on the upstream blacklist. The result is exposed via the static
   accessor `AchievementChangesMod.getCuratedAchievements()` — leaderboard /
   action submods consume it via reflection so they don't pick up a hard
   classpath dependency on this jar.

## Provenance

Ported from the `mod.sin.wyvern.AchievementChanges` block of Sindusk's
WyvernMods. Upstream gates this on `enableActionModule && actionLeaderboard`
inside the kitchen-sink jar. Under our split, the leaderboard lives in a
separate submod (#35) and consumes the curated map by name — when present.
The rename + requirement passes are useful regardless of leaderboard, so
this jar is independent.

The full blacklist (~110 entries) is preserved verbatim from upstream so
operators get the same curated set. To adjust it, fork this mod — there is
no config surface upstream and we don't add one.
