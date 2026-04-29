package com.garward.wurmmodloader.mods.bounty;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;

/**
 * Default strength formula for the bounty submod. Mirrors Sindusk's
 * {@code Bounty.getCreatureStrength}, with the magic numbers exposed
 * through {@link BountyConfig#strengthScale} / {@code strengthFloor} /
 * {@code strengthCap}.
 *
 * <p>Replace the formula entirely via
 * {@code BountyRegistry.setStrengthFunction(...)} — the bounty mod will
 * call into the new function from {@link BountyRegistry#getStrength}.
 */
final class BountyStrength {

    private BountyStrength() {}

    static final BountyRegistry.StrengthFunction DEFAULT = BountyStrength::computeDefault;

    /** Live config; assigned at preInit and re-read on every call so a
     *  /reloadmods refresh picks up new values without restart. */
    static volatile BountyConfig cfg = new BountyConfig();

    private static double computeDefault(Creature mob) {
        BountyConfig c = cfg;

        float combatRating = mob.getBaseCombatRating() + mob.getBonusCombatRating();
        float maxDmg = Math.max(mob.getTemplate().getBreathDamage(), mob.getHandDamage());
        maxDmg = Math.max(maxDmg, mob.getBiteDamage());
        maxDmg = Math.max(maxDmg, mob.getKickDamage());
        maxDmg = Math.max(maxDmg, mob.getHeadButtDamage());
        double fighting = mob.getFightingSkill().getKnowledge();
        double weaponless = mob.getWeaponLessFightingSkill().getKnowledge();
        double fs = Math.max(fighting, weaponless);
        double bodyStr = mob.getBodyStrength().getKnowledge();

        // Armour mod divides into fight skill (better armour → more strength).
        fs /= Math.max(mob.getArmourMod(), 0.001f);

        double cretStr = c.strengthFloor +
                (combatRating * Math.cbrt(maxDmg) * Math.cbrt(fs) * Math.cbrt(bodyStr));
        cretStr *= c.strengthScale;

        // Soft cap: smoothly approaches `cap` without a hard ceiling.
        double k = c.strengthCap;
        if (k > 0) {
            double r = cretStr / k;
            cretStr = (cretStr * Math.pow(2, -r) + k * (1 - Math.pow(2, -r)))
                    / (1 + Math.pow(2, -r));
        }

        // Tail behaviour preserved from upstream — small humans get a small
        // boost; small non-humans get cut down hard.
        if (mob.isAggHuman() && cretStr < 100D) {
            cretStr *= 1 + (Server.rand.nextFloat() * 0.2f);
            cretStr = Math.max(cretStr, 100D);
        } else if (!mob.isAggHuman() && cretStr < 300D) {
            cretStr *= 0.4f;
            cretStr *= 1 + (Server.rand.nextFloat() * 0.2f);
            cretStr = Math.max(cretStr, 10D);
        }
        return cretStr;
    }
}
