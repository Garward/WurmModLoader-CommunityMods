package com.garward.wurmmodloader.mods.arena;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

/**
 * Per-player attitude resolution used by {@code arenaAggression},
 * {@code enemyTitleHook}, and {@code enemyPresenceOnAggression}. Mirrors
 * Sindusk's WyvernMods {@code Arena.getArenaAttitude}: GMs are gods,
 * pets/team/citizens/allies are friendly, every other player is an enemy.
 */
public final class ArenaAttitude {

    public static byte getArenaAttitude(Player player, Creature aTarget) {
        if (player.getPower() > 0) {
            if (player.getPower() >= 5) return 6;
            return 3;
        }
        if (player == aTarget) return 1;
        if (player.opponent == aTarget) return 2;
        if (player.getSaveFile().pet != -10 && aTarget.getWurmId() == player.getSaveFile().pet) return 1;
        if (aTarget.getDominator() != null && aTarget.getDominator() != player) {
            return player.getAttitude(aTarget.getDominator());
        }
        if (aTarget.isReborn() && player.getKingdomTemplateId() == 3) return 0;
        if (aTarget.hasAttackedUnmotivated()
                && (aTarget.isPlayer() || !aTarget.isDominated() || aTarget.getDominator() != player)) {
            return 2;
        }
        if (aTarget.citizenVillage != null && player.citizenVillage != null) {
            if (player.citizenVillage.isCitizen(aTarget)) return 1;
            if (player.citizenVillage.isAlly(aTarget)) return 1;
        }
        if (aTarget.isPlayer() && player.getTeam() != null && player.getTeam().contains(aTarget)) return 1;
        if (aTarget.isPlayer() && player.isFriend(aTarget.getWurmId())) return 1;
        if (aTarget.isPlayer()) return 2;
        if (aTarget.isAggHuman()) return 2;
        return 0;
    }

    private ArenaAttitude() {}
}
