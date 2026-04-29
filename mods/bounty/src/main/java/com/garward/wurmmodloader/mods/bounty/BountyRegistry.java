package com.garward.wurmmodloader.mods.bounty;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Public extensibility surface for the bounty submod. Other mods register
 * their hooks here at startup; the patches and event subscribers walk the
 * registered tables at runtime.
 *
 * <p>All registry mutators are thread-confined to the server main thread
 * (Wurm runs single-threaded for game logic). Reads from event handlers and
 * patches happen on the same thread, so we don't synchronise.
 *
 * <h2>Typical extension pattern</h2>
 * <pre>{@code
 * // From a Titan submod's onServerStarted:
 * BountyRegistry.setReward("titan", 5_000_000L);
 * BountyRegistry.addCorpseLootHandler((mob, corpse, str) -> {
 *     if (TitanRegistry.isTitan(mob)) {
 *         // insert titan plate armour
 *     }
 * });
 * BountyRegistry.addKillerRewardHandler((player, victim, str) -> {
 *     if (TitanRegistry.isTitan(victim)) {
 *         player.addTitle(TITAN_SLAYER);
 *     }
 * });
 * }</pre>
 */
public final class BountyRegistry {

    private BountyRegistry() {}

    /** Pluggable strength formula. */
    @FunctionalInterface
    public interface StrengthFunction {
        double compute(Creature mob);
    }

    /** Called from the corpse-loot patch after the creature dies and its
     *  corpse is placed. Implementations may insert items, broadcast
     *  messages, or trigger world effects. */
    @FunctionalInterface
    public interface CorpseLootHandler {
        void onCreatureDeath(Creature victim, Item corpse, double strength);
    }

    /** Called from the player-kill subscriber when a player slays a creature
     *  (after the combatant gate has passed). Implementations may award
     *  items, currency, titles, or stats. */
    @FunctionalInterface
    public interface KillerRewardHandler {
        void onCreatureKilled(Player killer, Creature victim, double strength);
    }

    // ----- Strength formula -----

    private static StrengthFunction strengthFunction = BountyStrength.DEFAULT;

    public static void setStrengthFunction(StrengthFunction fn) {
        strengthFunction = Objects.requireNonNull(fn, "strength function");
    }

    public static double getStrength(Creature mob) {
        return strengthFunction.compute(mob);
    }

    // ----- Fixed reward overrides -----

    private static final Map<String, Long> rewardByName = new HashMap<>();
    private static final Map<Integer, Long> rewardByTemplate = new HashMap<>();

    /** Override the bounty for a specific creature name (case-insensitive
     *  match on {@code creature.getTemplate().getName().toLowerCase()}). */
    public static void setReward(String creatureName, long iron) {
        if (creatureName == null) return;
        rewardByName.put(creatureName.toLowerCase(Locale.ROOT), iron);
    }

    /** Override the bounty for a specific template ID. Beats the
     *  name-based override when both are present. */
    public static void setReward(int templateId, long iron) {
        rewardByTemplate.put(templateId, iron);
    }

    public static void clearRewards() {
        rewardByName.clear();
        rewardByTemplate.clear();
    }

    /** Returns the configured override iron, or {@code -1} if none is set. */
    public static long getRewardOverride(Creature mob) {
        if (mob == null) return -1L;
        Long byTpl = rewardByTemplate.get(mob.getTemplate().getTemplateId());
        if (byTpl != null) return byTpl;
        Long byName = rewardByName.get(
                mob.getTemplate().getName().toLowerCase(Locale.ROOT));
        return byName != null ? byName : -1L;
    }

    // ----- Type prefix multipliers -----

    private static final Map<String, Double> prefixMultipliers = new LinkedHashMap<>();

    public static void setTypePrefixMultiplier(String prefix, double multiplier) {
        if (prefix == null) return;
        prefixMultipliers.put(prefix.toLowerCase(Locale.ROOT), multiplier);
    }

    public static double getTypePrefixMultiplier(Creature mob) {
        if (mob == null || mob.isUnique()) return 1.0;
        String prefixes = mob.getPrefixes();
        if (prefixes == null) return 1.0;
        String lc = prefixes.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Double> e : prefixMultipliers.entrySet()) {
            if (lc.endsWith(e.getKey() + " ") || lc.endsWith(e.getKey())) {
                return e.getValue();
            }
        }
        return 1.0;
    }

    public static Map<String, Double> snapshotPrefixMultipliers() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(prefixMultipliers));
    }

    // ----- Loot / reward handler chains -----

    private static final Set<CorpseLootHandler> corpseLootHandlers = new LinkedHashSet<>();
    private static final Set<KillerRewardHandler> killerRewardHandlers = new LinkedHashSet<>();

    public static void addCorpseLootHandler(CorpseLootHandler handler) {
        if (handler != null) corpseLootHandlers.add(handler);
    }

    public static void addKillerRewardHandler(KillerRewardHandler handler) {
        if (handler != null) killerRewardHandlers.add(handler);
    }

    public static void removeCorpseLootHandler(CorpseLootHandler handler) {
        corpseLootHandlers.remove(handler);
    }

    public static void removeKillerRewardHandler(KillerRewardHandler handler) {
        killerRewardHandlers.remove(handler);
    }

    static Iterable<CorpseLootHandler> corpseLootHandlers() {
        return corpseLootHandlers;
    }

    static Iterable<KillerRewardHandler> killerRewardHandlers() {
        return killerRewardHandlers;
    }
}
