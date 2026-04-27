package com.garward.wurmmodloader.mods.action;

import com.garward.wurmmodloader.client.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.client.api.events.client.ClientConsoleInputEvent;
import com.garward.wurmmodloader.client.api.events.client.PlayerActionNameResolvedEvent;
import com.garward.wurmmodloader.client.api.events.client.QuickActionRebindEvent;
import com.garward.wurmmodloader.client.api.events.eventlogic.action.ClientItemReflect;
import com.garward.wurmmodloader.client.api.events.eventlogic.action.PlayerActionDispatcher;
import com.garward.wurmmodloader.client.api.events.lifecycle.ClientTickEvent;
import com.garward.wurmmodloader.client.api.events.map.ClientHUDInitializedEvent;
import com.garward.wurmmodloader.client.api.gui.ModHud;
import com.garward.wurmmodloader.client.modloader.ProxyClientHook;

import com.wurmonline.client.renderer.gui.HeadsUpDisplay;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Port of Goldenflamer's fork of bdew's custom-action macro mod. Adds two
 * console commands:
 *
 * <ul>
 *   <li>{@code act_show on|off} — reveal numeric action ids in right-click
 *       menus (so users can discover the id they need).</li>
 *   <li>{@code act <id> <target>[|<id> <target>|…]} — dispatch an action (or
 *       pipe-chained sequence) against one of ~15 target keywords.</li>
 * </ul>
 *
 * <p>Also enables Wurm's dev-only quick keybind / quick mousebind / toggleKey /
 * rebindPrimary / updateWithKeybinds UI for ordinary players, by asking the
 * framework to force {@code isDev()} true (see {@code IsDevOverridePatch}).
 *
 * <p>The heavy-lifting framework patches are {@code PlayerActionNamePatch}
 * (for {@code act_show}) and {@code IsDevOverridePatch} (for the keybind UI
 * unlock). All other hooks (console input, HUD init, reflection) already exist
 * in the shared client-core.
 */
public class ActionClientMod {

    private static final Logger logger = Logger.getLogger("ActionMod");

    private static volatile boolean showActionNums = false;
    private static volatile HeadsUpDisplay hud;
    // Deferred UI open — mutating HUD components from inside rebindPrimary()
    // (which runs during HeadsUpDisplay.gameTick) trips a CME on the HUD's
    // component-list iterator. Stash the request, open it next tick instead.
    private static volatile ActionMacroUI pendingUi;

    @SubscribeEvent
    public void onHudInit(ClientHUDInitializedEvent event) {
        try {
            hud = (HeadsUpDisplay) event.getHud();
            ClientItemReflect.setup();
            ProxyClientHook.setDevOverride(true);
            logger.info("[ActionMod] Initialized — dev UI gates unlocked, macros armed.");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[ActionMod] HUD init failed", t);
        }
    }

    @SubscribeEvent
    public void onActionName(PlayerActionNameResolvedEvent event) {
        if (!showActionNums) return;
        String name = event.getOriginalName();
        if (name == null || name.isEmpty()) return;
        event.setOverrideName(name + " (" + event.getActionId() + ")");
    }

    /**
     * Intercept the hold-key-over-action gesture to open a target-picker UI
     * instead of vanilla's bare {@code bind key action} write. Only kicks in
     * while {@code act_show on} is active — otherwise vanilla runs unchanged.
     */
    @SubscribeEvent
    public void onQuickRebind(QuickActionRebindEvent event) {
        if (!showActionNums || hud == null) return;
        event.cancel();
        String bindKey = ActionMacroUI.formatBindKey(
                event.getRawKey(), event.isCtrlDown(), event.isShiftDown(), event.isAltDown());
        try {
            pendingUi = new ActionMacroUI(event.getActionId(), event.getActionName(), bindKey);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[ActionMod] failed to build macro UI", t);
            hud.consoleOutput("act: macro UI failed (see log)");
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        ActionMacroUI ui = pendingUi;
        if (ui == null) return;
        pendingUi = null;
        try {
            // register() already adds the component to the HUD's visible list;
            // an extra toggle() would remove it the same tick. Just register.
            ModHud.get().register(ui);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[ActionMod] failed to open macro UI", t);
            if (hud != null) hud.consoleOutput("act: macro UI failed (see log)");
        }
    }

    @SubscribeEvent
    public void onConsoleInput(ClientConsoleInputEvent event) {
        if (hud == null) return;
        String cmd = event.getCommand();
        String[] data = event.getArgs();
        if (cmd == null || data == null) return;

        if ("act_show".equals(cmd)) {
            handleActShow(data);
            event.cancel();
        } else if ("act".equals(cmd)) {
            handleAct(data);
            event.cancel();
        }
    }

    private static void handleActShow(String[] data) {
        if (data.length == 2) {
            if ("on".equals(data[1])) {
                showActionNums = true;
                hud.consoleOutput("Action numbers on");
                return;
            } else if ("off".equals(data[1])) {
                showActionNums = false;
                hud.consoleOutput("Action numbers off");
                return;
            }
        }
        hud.consoleOutput("Usage: act_show {on|off}");
    }

    private static void handleAct(String[] data) {
        // Stitch back together & split on pipe so "act 3 toolbelt | 154 tile" works.
        String[] commands = String.join(" ", Arrays.copyOfRange(data, 1, data.length)).split("\\|");
        for (String next : commands) {
            String[] parts = next.trim().split(" ");
            try {
                if (parts.length == 2) {
                    parseAct(Short.parseShort(parts[0]), parts[1]);
                } else {
                    hud.consoleOutput("Usage: act <id> <modifier>[|<id> <modifier>|...]");
                }
            } catch (NumberFormatException nfe) {
                hud.consoleOutput("act: Error parsing id '" + parts[0] + "'");
            } catch (ReflectiveOperationException roe) {
                logger.log(Level.WARNING, "[ActionMod] reflect failure", roe);
                hud.consoleOutput("act: internal error (see log)");
            }
        }
    }

    /**
     * Run one action. Public so the macro-builder UI / other mods can reuse
     * the exact same dispatch path as the console command (no parser drift).
     */
    public static void parseAct(short id, String target) throws ReflectiveOperationException {
        PlayerActionDispatcher.dispatch(hud, id, target);
    }

    /** Exposed so the macro-builder UI can check for readiness. */
    public static HeadsUpDisplay getHud() {
        return hud;
    }
}
