package mod.sin.armoury;

import com.garward.wurmmodloader.core.eventlogic.combat.timing.DualWieldProfile;
import com.garward.wurmmodloader.core.eventlogic.combat.timing.DualWieldRegistry;
import com.garward.wurmmodloader.core.eventlogic.combat.timing.SwingSpeedProfile;
import com.garward.wurmmodloader.core.eventlogic.combat.timing.SwingSpeedRegistry;
import com.garward.wurmmodloader.core.eventlogic.combat.timing.WeaponTimerPolicy;
import com.garward.wurmmodloader.core.eventlogic.combat.timing.WeaponTimerRegistry;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridges Armoury's combat settings into the shared event-logic registries so no custom bytecode is required.
 */
public final class CombatsTweaks {

    private static final Logger LOGGER = Logger.getLogger(CombatsTweaks.class.getName());

    private static final String SWING_PROFILE_ID = "armoury-swing-profile";
    private static final String WEAPON_TIMER_POLICY_ID = "armoury-weapon-reset";
    private static final String DUAL_WIELD_PROFILE_ID = "armoury-dual-wield";

    private CombatsTweaks() {
        // utility
    }

    public static void registerSwingProfile(float minSwingSeconds, boolean rarityEnabled, float rarityReductionPerTier) {
        SwingSpeedProfile.Builder builder = SwingSpeedProfile.builder(SWING_PROFILE_ID)
            .minimumSwingSeconds(minSwingSeconds)
            .rarityEnabled(rarityEnabled)
            .rarityReductionPerTier(rarityEnabled ? rarityReductionPerTier : 0f);
        SwingSpeedRegistry.getInstance().register(builder.build());
        LOGGER.log(Level.FINE, "Registered swing profile with min {0}s (rarity enabled: {1})",
            new Object[]{minSwingSeconds, rarityEnabled});
    }

    public static void registerWeaponTimerReset(float resetValueSeconds) {
        WeaponTimerPolicy policy = WeaponTimerPolicy.builder(WEAPON_TIMER_POLICY_ID)
            .resetValue(resetValueSeconds)
            .build();
        WeaponTimerRegistry.getInstance().register(policy);
        LOGGER.log(Level.FINE, "Registered weapon timer reset policy to {0}s", resetValueSeconds);
    }

    public static void registerDualWieldProfile(boolean requirePlayer) {
        DualWieldProfile profile = new DualWieldProfile(DUAL_WIELD_PROFILE_ID, requirePlayer, true, Collections.emptySet());
        DualWieldRegistry.getInstance().register(profile);
        LOGGER.log(Level.FINE, "Registered dual wield profile (players only: {0})", requirePlayer);
    }
}
