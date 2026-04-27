package com.garward.wurmmodloader.mods.automine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class AutomineStateTest {

    private final List<Short> dispatched = new ArrayList<>();
    private final AtomicReference<Float> stamina = new AtomicReference<>(1.0f);
    private final List<Pattern> stops =
            java.util.Collections.singletonList(Pattern.compile("rock crumbles", Pattern.CASE_INSENSITIVE));

    private AutomineState newState() {
        return new AutomineState(
                actionId -> dispatched.add(actionId),
                stamina::get,
                stops,
                0.99f,
                60_000L
        );
    }

    @Test
    void start_sendsFirstBatch_thenWaitsForStamina() {
        AutomineState s = newState();
        s.start((short) 38, 3);
        s.onTick(0L);
        s.onTick(0L);
        s.onTick(0L);
        assertEquals(AutomineState.Phase.WAITING_STAMINA, s.getPhase());
        assertEquals(java.util.Arrays.asList((short) 38, (short) 38, (short) 38), dispatched);
    }

    @Test
    void staminaFull_kicksNextBatch() {
        AutomineState s = newState();
        s.start((short) 38, 2);
        s.onTick(0L); s.onTick(0L);
        assertEquals(AutomineState.Phase.WAITING_STAMINA, s.getPhase());
        s.onStaminaChanged(0.50f);
        assertEquals(AutomineState.Phase.WAITING_STAMINA, s.getPhase());
        s.onStaminaChanged(0.99f);
        assertEquals(AutomineState.Phase.DISPATCHING, s.getPhase());
        s.onTick(0L); s.onTick(0L);
        assertEquals(4, dispatched.size());
    }

    @Test
    void stopPhrase_haltsLoop() {
        AutomineState s = newState();
        s.start((short) 38, 5);
        s.onTick(0L);
        s.onMessage("Crash! The rock crumbles.");
        assertEquals(AutomineState.Phase.STOPPED, s.getPhase());
        s.onTick(0L);
        assertEquals(1, dispatched.size());
    }

    @Test
    void pause_stops() {
        AutomineState s = newState();
        s.start((short) 38, 5);
        s.onTick(0L);
        s.pause();
        assertEquals(AutomineState.Phase.STOPPED, s.getPhase());
    }

    @Test
    void watchdog_kicksNextBatchIfStaminaEventMissed() {
        AutomineState s = newState();
        s.start((short) 38, 1);
        s.onTick(1_000L);
        assertEquals(AutomineState.Phase.WAITING_STAMINA, s.getPhase());
        stamina.set(1.0f);
        s.onTick(1_000L + 60_000L);
        assertEquals(AutomineState.Phase.DISPATCHING, s.getPhase());
    }

    @Test
    void restartFromStopped() {
        AutomineState s = newState();
        s.start((short) 38, 1);
        s.pause();
        assertEquals(AutomineState.Phase.STOPPED, s.getPhase());
        s.start((short) 38, 1);
        assertEquals(AutomineState.Phase.DISPATCHING, s.getPhase());
    }
}
