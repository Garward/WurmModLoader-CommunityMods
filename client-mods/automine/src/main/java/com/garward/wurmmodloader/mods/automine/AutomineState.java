package com.garward.wurmmodloader.mods.automine;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Pure state machine driving the automine loop. HUD-free so it can be unit
 * tested in isolation. Wire into the live HUD via {@link AutomineClientMod}.
 */
public final class AutomineState {

    public enum Phase { IDLE, DISPATCHING, WAITING_STAMINA, STOPPED }

    @FunctionalInterface
    public interface ActionDispatcher {
        void dispatch(short actionId);
    }

    private final ActionDispatcher dispatcher;
    private final Supplier<Float> staminaProvider;
    private final List<Pattern> stopPhrases;
    private final float fullThreshold;
    private final long watchdogMs;

    private volatile Phase phase = Phase.IDLE;
    private short actionId;
    private int batchSize;
    private int sentInBatch;
    private long waitingSince;
    private String stopReason = "";

    public AutomineState(ActionDispatcher dispatcher,
                         Supplier<Float> staminaProvider,
                         List<Pattern> stopPhrases,
                         float fullThreshold,
                         long watchdogMs) {
        this.dispatcher = dispatcher;
        this.staminaProvider = staminaProvider;
        this.stopPhrases = stopPhrases;
        this.fullThreshold = fullThreshold;
        this.watchdogMs = watchdogMs;
    }

    public Phase getPhase() { return phase; }
    public String getStopReason() { return stopReason; }
    public int getSentInBatch() { return sentInBatch; }
    public int getBatchSize() { return batchSize; }

    public synchronized void start(short actionId, int batchSize) {
        this.actionId = actionId;
        this.batchSize = Math.max(1, batchSize);
        this.sentInBatch = 0;
        this.stopReason = "";
        this.phase = Phase.DISPATCHING;
    }

    public synchronized void pause() {
        this.phase = Phase.STOPPED;
        this.stopReason = "paused";
    }

    public synchronized void onTick(long nowMs) {
        switch (phase) {
            case DISPATCHING:
                if (sentInBatch < batchSize) {
                    dispatcher.dispatch(actionId);
                    sentInBatch++;
                    if (sentInBatch >= batchSize) {
                        phase = Phase.WAITING_STAMINA;
                        waitingSince = nowMs;
                    }
                }
                break;
            case WAITING_STAMINA:
                if (watchdogMs > 0 && (nowMs - waitingSince) >= watchdogMs) {
                    Float s = staminaProvider.get();
                    if (s != null && s >= fullThreshold) {
                        phase = Phase.DISPATCHING;
                        sentInBatch = 0;
                    }
                }
                break;
            default:
                break;
        }
    }

    public synchronized void onStaminaChanged(float newStamina) {
        if (phase == Phase.WAITING_STAMINA && newStamina >= fullThreshold) {
            phase = Phase.DISPATCHING;
            sentInBatch = 0;
        }
    }

    public synchronized void onMessage(String text) {
        if (phase == Phase.STOPPED || phase == Phase.IDLE) return;
        for (Pattern p : stopPhrases) {
            if (p.matcher(text).find()) {
                phase = Phase.STOPPED;
                stopReason = "tile broke / can't mine";
                return;
            }
        }
    }
}
