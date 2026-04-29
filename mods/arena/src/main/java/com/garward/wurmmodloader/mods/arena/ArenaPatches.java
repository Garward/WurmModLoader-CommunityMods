package com.garward.wurmmodloader.mods.arena;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The PvP-only patch grab-bag. Mirrors Sindusk's WyvernMods {@code Arena}
 * preInit; every patch is gated on {@code Servers.localServer.PVPSERVER}
 * <em>at runtime</em> via the inserted source code, so on a PvE server the
 * patches load but their branches never fire.
 *
 * <p>Patches reference {@link ArenaAttitude}, {@link ArenaSpawn}, and
 * {@link ArenaCrossMod} by FQN. The mod runs with
 * {@code sharedClassLoader=true} so they sit on the same loader as the
 * vanilla classes that get rewritten.
 *
 * <p>Skipped vs upstream:
 * <ul>
 *   <li>{@code adjustHotARewards} — depends on KeyFragment + AffinityOrb
 *       (KeyEvent submod, #49 — not yet ported)</li>
 *   <li>{@code discordRelayHotAMessages} / {@code sendArtifactDigsToDiscord}
 *       — DiscordRelay was upstream-only</li>
 *   <li>{@code enemyTitleHook} keeps only the {@code (ENEMY)} suffix; the
 *       PlayerTitles hasCustomTitle/getCustomTitle half is dropped (the
 *       customtitles submod ports addTitle/awardTitle but not runtime
 *       title suffixing)</li>
 * </ul>
 */
final class ArenaPatches {

    private static final Logger logger = Logger.getLogger(ArenaPatches.class.getName());
    private static final String ATTITUDE = ArenaAttitude.class.getName();
    private static final String SPAWN = ArenaSpawn.class.getName();
    private static final String CROSS = ArenaCrossMod.class.getName();

    static void install(ArenaConfig cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();

            CtClass ctCommunicator = pool.get("com.wurmonline.server.creatures.Communicator");
            CtClass ctItemBehaviour = pool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            CtClass ctAction = pool.get("com.wurmonline.server.behaviours.Action");
            CtClass ctCreature = pool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = pool.get("com.wurmonline.server.items.Item");
            CtClass ctMethodsItems = pool.get("com.wurmonline.server.behaviours.MethodsItems");
            CtClass ctVillageFoundationQuestion = pool.get("com.wurmonline.server.questions.VillageFoundationQuestion");
            CtClass ctKingdomFoundationQuestion = pool.get("com.wurmonline.server.questions.KingdomFoundationQuestion");
            CtClass ctRealDeathQuestion = pool.get("com.wurmonline.server.questions.RealDeathQuestion");
            CtClass ctPlayer = pool.get("com.wurmonline.server.players.Player");
            CtClass ctVirtualZone = pool.get("com.wurmonline.server.zones.VirtualZone");
            CtClass ctMethodsCreatures = pool.get("com.wurmonline.server.behaviours.MethodsCreatures");
            CtClass ctMethods = pool.get("com.wurmonline.server.behaviours.Methods");
            CtClass ctTerraforming = pool.get("com.wurmonline.server.behaviours.Terraforming");
            CtClass ctVillage = pool.get("com.wurmonline.server.villages.Village");
            CtClass ctGuardPlan = pool.get("com.wurmonline.server.villages.GuardPlan");

            // ---- shared method descriptors --------------------------------
            CtClass[] paramsItemBehaviourAction = {
                    ctAction, ctCreature, ctItem, ctItem, CtClass.shortType, CtClass.floatType
            };
            String descItemBehaviourAction = Descriptor.ofMethod(CtClass.booleanType, paramsItemBehaviourAction);

            CtClass[] paramsAddCreature = {
                    CtClass.longType, CtClass.booleanType, CtClass.longType,
                    CtClass.floatType, CtClass.floatType, CtClass.floatType
            };
            String descAddCreature = Descriptor.ofMethod(CtClass.booleanType, paramsAddCreature);

            CtClass ctString = pool.get("java.lang.String");
            CtClass[] paramsDie = { CtClass.booleanType, ctString, CtClass.booleanType };
            String descDie = Descriptor.ofMethod(CtClass.voidType, paramsDie);

            CtClass[] paramsSetVehicle = {
                    CtClass.longType, CtClass.booleanType, CtClass.byteType,
                    CtClass.intType, CtClass.intType
            };
            String descSetVehicle = Descriptor.ofMethod(CtClass.voidType, paramsSetVehicle);

            CtClass[] paramsIsEnemyCreature = { ctCreature, CtClass.booleanType };
            String descIsEnemyCreature = Descriptor.ofMethod(CtClass.booleanType, paramsIsEnemyCreature);

            CtClass[] paramsIsEnemyVillage = { ctVillage };
            String descIsEnemyVillage = Descriptor.ofMethod(CtClass.booleanType, paramsIsEnemyVillage);

            CtClass[] paramsGetMaxGuards = { CtClass.intType, CtClass.intType };
            String descGetMaxGuards = Descriptor.ofMethod(CtClass.intType, paramsGetMaxGuards);

            // ---- Mounts ---------------------------------------------------
            if (cfg.equipHorseGearByLeading) {
                String body = "if (this.player.getPower() > 0) {"
                        + "  $_ = this.player;"
                        + "} else if (com.wurmonline.server.Servers.isThisAPvpServer() && owner.getDominator() != this.player) {"
                        + "  $_ = owner.getLeader();"
                        + "} else {"
                        + "  $_ = $proceed($$);"
                        + "}";
                replaceCallDeclared(ctCommunicator, "reallyHandle_CMD_MOVE_INVENTORY", "getDominator", body,
                        "equipHorseGearByLeading: bypass taming on movement");
                replaceCallDeclared(ctCommunicator, "equipCreatureCheck", "getDominator", body,
                        "equipHorseGearByLeading: bypass taming on equip");
            }

            // ---- Lockpicking ----------------------------------------------
            if (cfg.lockpickingImprovements) {
                String body = "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                        + "  $_ = true;"
                        + "} else {"
                        + "  $_ = target.getLastOwnerId() == -10 || target.getLastOwnerId() == 0 || target.getTemplateId() == 995;"
                        + "}";
                replaceCallDescribed(ctItemBehaviour, "action", descItemBehaviourAction, "isInPvPZone", body,
                        "lockpickingImprovements: allow on PvP / treasure chests on PvE");
                replaceCallDeclared(ctMethodsItems, "picklock", "getLastOwnerId",
                        "$_ = $proceed($$); if ($_ == -10 || $_ == 0) { ok = true; }",
                        "lockpickingImprovements: allow lockpicking unowned containers");
            }

            // ---- Settlement / kingdoms ------------------------------------
            if (cfg.placeDeedsOutsideKingdomInfluence) {
                replaceCallDeclared(ctVillageFoundationQuestion, "checkSize", "getKingdom",
                        "$_ = (byte) 4;",
                        "placeDeedsOutsideKingdomInfluence: skip kingdom-border check");
            }

            if (cfg.disablePMKs) {
                insertBeforeDeclared(ctKingdomFoundationQuestion, "sendQuestion",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  this.getResponder().getCommunicator().sendSafeServerMessage(\"Player-Made Kingdoms are disabled on this server.\");"
                                + "  return;"
                                + "}",
                        "disablePMKs: block PMK creation on PvP");
            }

            if (cfg.disablePlayerChampions) {
                insertBeforeDeclared(ctRealDeathQuestion, "sendQuestion",
                        "this.getResponder().getCommunicator().sendSafeServerMessage(\"Champion players are disabled on this server.\");"
                                + "return;",
                        "disablePlayerChampions: block real-death/champion question");
            }

            // ---- Aggression / enemy detection -----------------------------
            if (cfg.arenaAggression) {
                insertBeforeDeclared(ctPlayer, "getAttitude",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  return " + ATTITUDE + ".getArenaAttitude(this, $1);"
                                + "}",
                        "arenaAggression: route Player.getAttitude through ArenaAttitude on PvP");

                insertBeforeDeclared(ctCreature, "getAttitude",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER && ($1.isPlayer() || this.isPlayer())) {"
                                + "  if ($1.citizenVillage != null && this.citizenVillage != null) {"
                                + "    if ($1.citizenVillage == this.citizenVillage) { return 1; }"
                                + "    if ($1.citizenVillage.isAlly(this.citizenVillage)) { return 1; }"
                                + "  }"
                                + "}",
                        "arenaAggression: village + ally short-circuit on Creature.getAttitude");
            }

            if (cfg.enemyTitleHook) {
                String body = "if (this.watcher.isPlayer()"
                        + " && com.wurmonline.server.Servers.localServer.PVPSERVER && creature.isPlayer()"
                        + " && " + ATTITUDE + ".getArenaAttitude((com.wurmonline.server.players.Player) this.watcher, creature) == 2) {"
                        + "  suff = suff + \" (ENEMY)\";"
                        + "  enemy = true;"
                        + "}"
                        + "$_ = $proceed($$);";
                replaceCallDescribed(ctVirtualZone, "addCreature", descAddCreature, "isChampion", body,
                        "enemyTitleHook: append (ENEMY) suffix in addCreature");
            }

            if (cfg.enemyPresenceOnAggression) {
                String body = "if (this.watcher.isPlayer() && creature.isPlayer()"
                        + " && com.wurmonline.server.Servers.localServer.PVPSERVER"
                        + " && " + ATTITUDE + ".getArenaAttitude((com.wurmonline.server.players.Player) this.watcher, creature) == 2) {"
                        + "  $_ = 1;"
                        + "} else {"
                        + "  $_ = $proceed($$);"
                        + "}";
                replaceCallDeclared(ctVirtualZone, "checkIfEnemyIsPresent", "getKingdomId", body,
                        "enemyPresenceOnAggression: use attitude instead of kingdom");
            }

            if (cfg.disableFarwalkerItems) {
                replaceCallDeclared(ctMethodsCreatures, "teleportCreature", "isInPvPZone",
                        "$_ = com.wurmonline.server.Servers.localServer.PVPSERVER;",
                        "disableFarwalkerItems: block twigs/stones on PvP");
            }

            if (cfg.alwaysAllowAffinitySteal) {
                replaceCallDeclared(ctPlayer, "modifyRanking", "isEnemyOnChaos",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  $_ = true;"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "alwaysAllowAffinitySteal: force enemy-on-chaos true on PvP");
            }

            if (cfg.adjustFightSkillGain) {
                replaceCallDeclared(ctCreature, "modifyFightSkill", "checkInitialTitle",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  p.getFightingSkill().setKnowledge(pskill + (skillGained * 1.5d), false);"
                                + "}"
                                + "$_ = $proceed($$);",
                        "adjustFightSkillGain: 1.5x fight skill gain on PvP");
            }

            if (cfg.useAggressionForNearbyEnemies) {
                replaceCallDeclared(ctMethodsItems, "isEnemiesNearby", "isFriendlyKingdom",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  $_ = c.getAttitude(performer) != 2 && c.getAttitude(performer) != 1;"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "useAggressionForNearbyEnemies: aggression check via attitude");
            }

            // ---- Death / corpses ------------------------------------------
            if (cfg.disablePvPCorpseProtection) {
                replaceCallDescribed(ctCreature, "die", descDie, "setProtected",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  $_ = $proceed(false);"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "disablePvPCorpseProtection: setProtected(false) on PvP");
                replaceCallDescribed(ctCreature, "die", descDie, "isInPvPZone",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  $_ = true;"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "disablePvPCorpseProtection: force isInPvPZone true on PvP");
            }

            // ---- Permissions / theft --------------------------------------
            if (cfg.bypassHousePermissions) {
                replaceCallDeclared(ctMethods, "isNotAllowedMessage", "isEnemy",
                        "$_ = com.wurmonline.server.Servers.localServer.PVPSERVER;",
                        "bypassHousePermissions: short-circuit enemy check inside houses");
            }

            if (cfg.allowStealingAgainstDeityWishes) {
                replaceCallDeclared(ctAction, "checkLegalMode", "isLibila",
                        "$_ = $proceed($$) || com.wurmonline.server.Servers.localServer.PVPSERVER;",
                        "allowStealingAgainstDeityWishes: PvP bypasses legal-mode punishment");
            }

            if (cfg.sameKingdomVehicleTheft) {
                replaceCallDescribed(ctCreature, "setVehicle", descSetVehicle, "isThisAChaosServer",
                        "$_ = com.wurmonline.server.Servers.localServer.PVPSERVER && !lVehicle.isLocked();",
                        "sameKingdomVehicleTheft: take unlocked vehicles on PvP");
            }

            if (cfg.adjustMineDoorDamage) {
                replaceCallDeclared(ctTerraforming, "destroyMineDoor", "getOrCreateTile",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) { damage *= 3d; }"
                                + "$_ = $proceed($$);",
                        "adjustMineDoorDamage: 3x bash damage on PvP");
            }

            if (cfg.sameKingdomPermissionsAdjustments) {
                String body = "if (com.wurmonline.server.Servers.localServer.PVPSERVER) { return true; }";
                insertBeforeDeclared(ctCreature, "isOkToKillBy", body,
                        "sameKingdomPermissionsAdjustments: isOkToKillBy true on PvP");
                insertBeforeDeclared(ctCreature, "hasBeenAttackedBy", body,
                        "sameKingdomPermissionsAdjustments: hasBeenAttackedBy true on PvP");
            }

            if (cfg.disableCAHelpOnPvP) {
                insertBeforeDeclared(ctPlayer, "seesPlayerAssistantWindow",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) { return false; }",
                        "disableCAHelpOnPvP: hide CA HELP window");
            }

            if (cfg.sameKingdomVillageWarfare) {
                insertBeforeDescribed(ctVillage, "isEnemy", descIsEnemyCreature,
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER && $1.isPlayer()) {"
                                + "  if ($1.getPower() > 0) { return false; }"
                                + "  if (this.isCitizen($1) || this.isAlly($1)) { return false; }"
                                + "  return true;"
                                + "}"
                                + "if (" + CROSS + ".isTitan($1) || " + CROSS + ".isRareCreature($1)) { return false; }",
                        "sameKingdomVillageWarfare: isEnemy(Creature) on PvP + titan/rare guard exemption");

                setBodyDescribed(ctVillage, "isEnemy", descIsEnemyVillage,
                        "{ if ($1 == null) { return false; }"
                                + "  if ($1.kingdom != this.kingdom) { return true; }"
                                + "  if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "    if (this.isAlly($1)) { return false; }"
                                + "    if ($0 == $1) { return false; }"
                                + "    return true;"
                                + "  }"
                                + "  return false;"
                                + "}",
                        "sameKingdomVillageWarfare: isEnemy(Village) replaced");
            }

            if (cfg.capMaximumGuards) {
                insertBeforeDescribed(ctGuardPlan, "getMaxGuards", descGetMaxGuards,
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  return Math.min(5, Math.max(3, $1 * $2 / 49));"
                                + "}",
                        "capMaximumGuards: cap 3..5 on PvP");
            }

            if (cfg.disableTowerConstruction) {
                CtClass ctAdvancedCreationEntry = pool.get("com.wurmonline.server.items.AdvancedCreationEntry");
                String body = "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                        + "  performer.getCommunicator().sendAlertServerMessage(\"Towers are disabled.\");"
                        + "  throw new com.wurmonline.server.NoSuchItemException(\"Towers are disabled.\");"
                        + "} else {"
                        + "  $_ = $proceed($$);"
                        + "}";
                replaceCallDeclared(ctAdvancedCreationEntry, "cont", "isTowerTooNear", body,
                        "disableTowerConstruction: block cont() tower path on PvP");
                replaceCallDeclared(ctAdvancedCreationEntry, "run", "isTowerTooNear", body,
                        "disableTowerConstruction: block run() tower path on PvP");
            }

            if (cfg.adjustLocalRange) {
                replaceCallDeclared(ctVirtualZone, "coversCreature", "isWithinDistanceTo",
                        "if ($3 > 5) { $_ = $proceed($1, $2, 50); } else { $_ = $proceed($$); }",
                        "adjustLocalRange: shrink player local to 50 tiles");
                replaceCallDeclared(ctVirtualZone, "coversCreature", "isPlayer",
                        "$_ = true;",
                        "adjustLocalRange: skip non-player branch");
            }

            if (cfg.disableKarmaTeleport) {
                CtClass ctKarmaQuestion = pool.get("com.wurmonline.server.questions.KarmaQuestion");
                replaceCallDeclared(ctKarmaQuestion, "answer", "isInPvPZone",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  $_ = true;"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "disableKarmaTeleport: block karma teleport on PvP");
            }

            if (cfg.limitLeadCreatures) {
                insertBeforeDeclared(ctPlayer, "mayLeadMoreCreatures",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  return this.followers == null || this.followers.size() < 1;"
                                + "}",
                        "limitLeadCreatures: max one led creature on PvP");
            }

            if (cfg.adjustBashTimer) {
                CtClass ctMethodsStructure = pool.get("com.wurmonline.server.behaviours.MethodsStructure");
                replaceCallDeclared(ctMethodsStructure, "destroyWall", "getStructure",
                        "time = 600; $_ = $proceed($$);",
                        "adjustBashTimer: 600s wall bash on PvP");
            }

            if (cfg.allowAttackingSameKingdomGuards) {
                CtClass ctCreatureBehaviour = pool.get("com.wurmonline.server.behaviours.CreatureBehaviour");
                replaceCallDeclared(ctCreatureBehaviour, "handle_TARGET_and_TARGET_HOSTILE", "isFriendlyKingdom",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  $_ = false;"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "allowAttackingSameKingdomGuards: target same-kingdom guards on PvP");
            }

            if (cfg.fixGuardsAttackingThemselves) {
                insertBeforeDeclared(ctVillage, "addTarget",
                        "if ($1.isSpiritGuard()) { return; }",
                        "fixGuardsAttackingThemselves: spirit guards skip addTarget");
            }

            if (cfg.reducedMineDoorOpenTime) {
                replaceCallDeclared(ctCreature, "checkOpenMineDoor", "isThisAChaosServer",
                        "$_ = true;",
                        "reducedMineDoorOpenTime: 30s mine-door close on any server");
            }

            if (cfg.allowSameKingdomFightSkillGains) {
                replaceCallDeclared(ctCreature, "modifyFightSkill", "getKingdomId",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  if ($0 == this) { $_ = -1; } else { $_ = $proceed($$); }"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "allowSameKingdomFightSkillGains: enable fight gains vs same-kingdom on PvP");
            }

            if (cfg.allowArcheringOnSameKingdomDeeds) {
                replaceCallDeclared(ctVillage, "mayAttack", "isEnemyOnChaos",
                        "$_ = true;",
                        "allowArcheringOnSameKingdomDeeds: bypass enemy-on-chaos in mayAttack");
            }

            if (cfg.sendNewSpawnQuestionOnPvP) {
                CtClass ctSpawnQuestion = pool.get("com.wurmonline.server.questions.SpawnQuestion");
                insertBeforeDeclared(ctSpawnQuestion, "sendQuestion",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) {"
                                + "  " + SPAWN + ".sendNewSpawnQuestion(this);"
                                + "  return;"
                                + "}",
                        "sendNewSpawnQuestionOnPvP: route SpawnQuestion through NewSpawnQuestion");
            }

            if (cfg.makeFreedomFavoredKingdom) {
                CtClass ctDeities = pool.get("com.wurmonline.server.deities.Deities");
                setBodyDeclared(ctDeities, "getFavoredKingdom",
                        "{ return (byte) 4; }",
                        "makeFreedomFavoredKingdom: Deities.getFavoredKingdom = 4");
                // Upstream tries to also re-set this method on Deity but writes ctDeities;
                // intentionally not reproducing that no-op here.
            }

            if (cfg.crownInfluenceOnAggression) {
                replaceCallDeclared(ctPlayer, "spreadCrownInfluence", "isFriendlyKingdom",
                        "$_ = $0.getAttitude(this) == 1;",
                        "crownInfluenceOnAggression: spread by attitude not kingdom");
            }

            if (cfg.disableOWFL) {
                replaceCallDescribedAt(ctCreature, "die", descDie, "isOnCurrentServer", 1,
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER && this.isPlayer()) {"
                                + "  this.getCommunicator().sendSafeServerMessage(\"You have died on the Arena server and your items are kept safe.\");"
                                + "  keepItems = true;"
                                + "}"
                                + "$_ = $proceed($$);",
                        "disableOWFL: keep items on PvP death");
            }

            if (cfg.resurrectionStonesProtectSkill) {
                insertBeforeDeclared(ctCreature, "punishSkills",
                        "if (this.isPlayer() && this.isDeathProtected()) {"
                                + "  this.getCommunicator().sendSafeServerMessage(\"You have died with a Resurrection Stone and your knowledge is kept safe.\");"
                                + "  return;"
                                + "} else {"
                                + "  this.getCommunicator().sendAlertServerMessage(\"You have died without a Resurrection Stone, resulting in some of your knowledge being lost.\");"
                                + "}",
                        "resurrectionStonesProtectSkill: resurrection stones save knowledge");
            }

            if (cfg.resurrectionStonesProtectFightSkill) {
                replaceCallDeclaredAt(ctCreature, "modifyFightSkill", "setKnowledge", 1,
                        "if (this.isPlayer() && this.isDeathProtected()) { $_ = null; } else { $_ = $proceed($$); }",
                        "resurrectionStonesProtectFightSkill: skip first setKnowledge if death-protected");
            }

            if (cfg.resurrectionStonesProtectAffinities) {
                replaceCallDeclared(ctPlayer, "modifyRanking", "getAffinities",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER && this.isPlayer() && this.isDeathProtected()) {"
                                + "  this.getCommunicator().sendSafeServerMessage(\"Your resurrection stone keeps your affinities safe from your slayers.\");"
                                + "  $_ = " + SPAWN + ".getNullAffinities();"
                                + "} else {"
                                + "  $_ = $proceed($$);"
                                + "}",
                        "resurrectionStonesProtectAffinities: null affinities if death-protected");
            }

            if (cfg.bypassPlantedPermissionChecks) {
                insertBeforeDeclared(ctItem, "checkPlantedPermissions",
                        "if (com.wurmonline.server.Servers.localServer.PVPSERVER) { return true; }",
                        "bypassPlantedPermissionChecks: ignore planted-perm checks on PvP");
            }
        } catch (NotFoundException e) {
            throw new HookException(e);
        }
    }

    // -- helpers ----------------------------------------------------------

    private static void thaw(CtClass ct) {
        if (ct.isFrozen()) ct.defrost();
    }

    private static void replaceCallDeclared(CtClass ct, String method, String callName,
                                            String body, String reason) {
        try {
            thaw(ct);
            ct.getDeclaredMethod(method).instrument(new ExprEditor() {
                @Override public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) m.replace(body);
                }
            });
            logger.info("[arena] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void replaceCallDescribed(CtClass ct, String method, String descriptor,
                                             String callName, String body, String reason) {
        try {
            thaw(ct);
            ct.getMethod(method, descriptor).instrument(new ExprEditor() {
                @Override public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) m.replace(body);
                }
            });
            logger.info("[arena] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void replaceCallDeclaredAt(CtClass ct, String method, String callName,
                                              int occurrence, String body, String reason) {
        final int target = occurrence;
        final int[] seen = { 0 };
        try {
            thaw(ct);
            ct.getDeclaredMethod(method).instrument(new ExprEditor() {
                @Override public void edit(MethodCall m) throws CannotCompileException {
                    if (!m.getMethodName().equals(callName)) return;
                    int idx = seen[0]++;
                    if (idx == target) m.replace(body);
                }
            });
            logger.info("[arena] " + reason + " (occurrence #" + target + ")");
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void replaceCallDescribedAt(CtClass ct, String method, String descriptor,
                                               String callName, int occurrence,
                                               String body, String reason) {
        final int target = occurrence;
        final int[] seen = { 0 };
        try {
            thaw(ct);
            ct.getMethod(method, descriptor).instrument(new ExprEditor() {
                @Override public void edit(MethodCall m) throws CannotCompileException {
                    if (!m.getMethodName().equals(callName)) return;
                    int idx = seen[0]++;
                    if (idx == target) m.replace(body);
                }
            });
            logger.info("[arena] " + reason + " (occurrence #" + target + ")");
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void insertBeforeDeclared(CtClass ct, String method, String body, String reason) {
        try {
            thaw(ct);
            ct.getDeclaredMethod(method).insertBefore(body);
            logger.info("[arena] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void insertBeforeDescribed(CtClass ct, String method, String descriptor,
                                              String body, String reason) {
        try {
            thaw(ct);
            ct.getMethod(method, descriptor).insertBefore(body);
            logger.info("[arena] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void setBodyDeclared(CtClass ct, String method, String body, String reason) {
        try {
            thaw(ct);
            ct.getDeclaredMethod(method).setBody(body);
            logger.info("[arena] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private static void setBodyDescribed(CtClass ct, String method, String descriptor,
                                         String body, String reason) {
        try {
            thaw(ct);
            ct.getMethod(method, descriptor).setBody(body);
            logger.info("[arena] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[arena] FAILED " + reason, e);
        }
    }

    private ArenaPatches() {}
}
