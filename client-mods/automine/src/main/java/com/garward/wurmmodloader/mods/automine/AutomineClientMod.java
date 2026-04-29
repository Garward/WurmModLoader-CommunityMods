package com.garward.wurmmodloader.mods.automine;

import com.garward.wurmmodloader.client.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.client.api.events.client.ClientConsoleInputEvent;
import com.garward.wurmmodloader.client.api.events.client.ClientEventMessageReceivedEvent;
import com.garward.wurmmodloader.client.api.events.client.ClientStaminaChangedEvent;
import com.garward.wurmmodloader.client.api.events.eventlogic.action.PlayerActionDispatcher;
import com.garward.wurmmodloader.client.api.events.lifecycle.ClientTickEvent;
import com.garward.wurmmodloader.client.api.events.map.ClientHUDInitializedEvent;
import com.garward.wurmmodloader.client.api.gui.ModHud;
import com.garward.wurmmodloader.client.modloader.ProxyClientHook;

import com.wurmonline.client.renderer.gui.HeadsUpDisplay;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the automine mod. Owns the window, the state machine, and
 * the event subscriptions. Console command {@code automine} opens / focuses
 * the window.
 */
public class AutomineClientMod {

    private static final Logger logger = Logger.getLogger("AutomineMod");

    private volatile HeadsUpDisplay hud;
    private volatile AutomineConfig config;
    private volatile AutomineState state;
    private volatile AutomineWindow window;
    private volatile boolean pendingOpen;
    private volatile String dispatchTarget = "selected";

    @SubscribeEvent
    public void onHudInit(ClientHUDInitializedEvent event) {
        try {
            this.hud = (HeadsUpDisplay) event.getHud();
            File props = new File("mods/automine/automine.properties");
            this.config = AutomineConfig.load(props);
            this.state = new AutomineState(
                    this::dispatchAction,
                    ProxyClientHook::getCurrentStamina,
                    config.stopPhrases,
                    config.staminaFullThreshold,
                    config.staminaWatchdogMs);
            logger.info("[automine] initialised — batch=" + config.defaultBatchSize
                    + " threshold=" + config.staminaFullThreshold);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[automine] HUD init failed", t);
        }
    }

    @SubscribeEvent
    public void onConsoleInput(ClientConsoleInputEvent event) {
        if (!"automine".equals(event.getCommand())) return;
        event.cancel();
        if (hud == null || state == null) {
            logger.warning("[automine] HUD not ready yet — try again after login");
            return;
        }
        pendingOpen = true;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (pendingOpen) {
            pendingOpen = false;
            ensureWindow();
        }
        if (state != null) state.onTick(System.currentTimeMillis());
        if (window != null) window.setStatus(describe(state));
    }

    @SubscribeEvent
    public void onStamina(ClientStaminaChangedEvent event) {
        if (state != null) state.onStaminaChanged(event.getNewStamina());
    }

    @SubscribeEvent
    public void onEventMessage(ClientEventMessageReceivedEvent event) {
        if (state != null) state.onMessage(event.getText());
    }

    private void ensureWindow() {
        if (window != null) return;
        try {
            window = new AutomineWindow(config, new AutomineWindow.Listener() {
                @Override public void onStart(AutomineWindow.Mode mode,
                                              AutomineWindow.Direction dir,
                                              AutomineWindow.LoopBy loopBy,
                                              int batchSize) {
                    short id;
                    switch (dir) {
                        case UP:   id = config.actionUp; break;
                        case DOWN: id = config.actionDown; break;
                        default:   id = config.actionForward;
                    }
                    switch (mode) {
                        case CRAFT: dispatchTarget = "inventory_selection"; break;
                        case BODY:  dispatchTarget = "body";                break;
                        default:    dispatchTarget = "selected";            break;
                    }
                    AutomineState.LoopMode lm = loopBy == AutomineWindow.LoopBy.TIMER
                            ? AutomineState.LoopMode.TIMER
                            : AutomineState.LoopMode.STAMINA;
                    state.start(id, batchSize, lm, config.timerIntervalMs);
                }
                @Override public void onPause() { state.pause(); }
                @Override public void onClose() { state.pause(); window = null; }
            });
            ModHud.get().register(window);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[automine] failed to open window", t);
            if (hud != null) hud.consoleOutput("automine: window failed (see log)");
        }
    }

    private void dispatchAction(short actionId) {
        try {
            PlayerActionDispatcher.dispatch(hud, actionId, dispatchTarget);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[automine] dispatch failed", t);
            state.pause();
        }
    }

    private static String describe(AutomineState s) {
        if (s == null) return "idle";
        switch (s.getPhase()) {
            case IDLE: return "idle";
            case DISPATCHING: return "mining (" + s.getSentInBatch() + "/" + s.getBatchSize() + ")";
            case WAITING_STAMINA: {
                Float now = ProxyClientHook.getCurrentStamina();
                int pct = now == null || Float.isNaN(now) ? -1 : (int) (now * 100);
                return pct < 0 ? "waiting for stamina" : "waiting for stamina (" + pct + "%)";
            }
            case STOPPED: return "stopped: " + s.getStopReason();
            default: return "?";
        }
    }
}
