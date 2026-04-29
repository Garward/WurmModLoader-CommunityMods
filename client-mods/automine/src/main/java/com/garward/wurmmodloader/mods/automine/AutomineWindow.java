package com.garward.wurmmodloader.mods.automine;

import com.garward.wurmmodloader.client.api.gui.ArrayDirection;
import com.garward.wurmmodloader.client.api.gui.Insets;
import com.garward.wurmmodloader.client.api.gui.ModButton;
import com.garward.wurmmodloader.client.api.gui.ModInputField;
import com.garward.wurmmodloader.client.api.gui.ModLabel;
import com.garward.wurmmodloader.client.api.gui.ModStackPanel;
import com.garward.wurmmodloader.client.api.gui.ModWindow;

/**
 * Popup window for the automine controls. Direction buttons / batch +/- /
 * Start / Pause. Wires user actions into {@link AutomineWindow.Listener}.
 */
public final class AutomineWindow extends ModWindow {

    public enum Direction { FORWARD, UP, DOWN }
    public enum Mode { TILE, CRAFT, BODY }
    public enum LoopBy { STAMINA, TIMER }

    public interface Listener {
        void onStart(Mode mode, Direction direction, LoopBy loopBy, int batchSize);
        void onPause();
        void onClose();
    }

    private final Listener listener;
    private final AutomineConfig config;
    private final ModLabel statusLabel;
    private final ModLabel directionLabel;
    private final ModLabel batchLabel;
    private final ModLabel modeLabel;
    private final ModLabel loopLabel;

    private Mode mode = Mode.TILE;
    private Direction direction = Direction.FORWARD;
    private LoopBy loopBy = LoopBy.STAMINA;
    private int batchSize;

    public AutomineWindow(AutomineConfig config, Listener listener) {
        super("Auto Mine");
        this.config = config;
        this.listener = listener;
        this.batchSize = config.defaultBatchSize;
        this.statusLabel = new ModLabel("Status: idle");
        this.directionLabel = new ModLabel("Direction: Forward");
        this.batchLabel = new ModLabel("Actions per batch: " + batchSize);
        this.modeLabel = new ModLabel("Mode: Tile (mining)");
        this.loopLabel = new ModLabel("Loop: wait for full stamina");
        lockSize();
        installContent(buildRoot(), 280, 560);
    }

    private static ModInputField idField(short initial,
                                         java.util.function.IntConsumer commit) {
        ModInputField f = new ModInputField("idField", 60);
        f.setValue(Short.toString(initial));
        java.util.function.Consumer<String> write = text -> {
            try {
                int v = Integer.parseInt(text.trim());
                if (v < 0 || v > Short.MAX_VALUE) return;
                commit.accept(v);
            } catch (NumberFormatException ignored) { }
        };
        f.onChange(write);
        f.onSubmit(write);
        return f;
    }

    private ModStackPanel buildRoot() {
        ModStackPanel root = new ModStackPanel("Automine root", ArrayDirection.VERTICAL)
                .setBackgroundPainted(true)
                .setPadding(Insets.uniform(6))
                .setGap(4);

        root.addChild(modeLabel);
        ModStackPanel modeRow = new ModStackPanel("mode", ArrayDirection.HORIZONTAL).setGap(2);
        modeRow.addChild(new ModButton("Tile", "Use selected tile (mining)", () -> setMode(Mode.TILE)));
        modeRow.addChild(new ModButton("Craft", "Use inventory selection (crafting)", () -> setMode(Mode.CRAFT)));
        modeRow.addChild(new ModButton("Body", "Use the player's body (e.g. meditate)", () -> setMode(Mode.BODY)));
        root.addChild(modeRow);

        root.addChild(directionLabel);

        ModStackPanel dirRow = new ModStackPanel("dir", ArrayDirection.HORIZONTAL).setGap(2);
        dirRow.addChild(new ModButton("Forward", "Mine Forward", () -> setDirection(Direction.FORWARD)));
        dirRow.addChild(new ModButton("Up",      "Mine Up",      () -> setDirection(Direction.UP)));
        dirRow.addChild(new ModButton("Down",    "Mine Down",    () -> setDirection(Direction.DOWN)));
        root.addChild(dirRow);

        root.addChild(new ModLabel("Action IDs (Forward / Up / Down):"));
        ModStackPanel idRow = new ModStackPanel("ids", ArrayDirection.HORIZONTAL).setGap(2);
        idRow.addChild(idField(config.actionForward, v -> config.setActionForward((short) v)));
        idRow.addChild(idField(config.actionUp,      v -> config.setActionUp((short) v)));
        idRow.addChild(idField(config.actionDown,    v -> config.setActionDown((short) v)));
        root.addChild(idRow);

        root.addChild(batchLabel);
        ModStackPanel batchRow = new ModStackPanel("batch", ArrayDirection.HORIZONTAL).setGap(2);
        batchRow.addChild(new ModButton("-", "Decrease batch size", () -> changeBatch(-1)));
        batchRow.addChild(new ModButton("+", "Increase batch size", () -> changeBatch(+1)));
        root.addChild(batchRow);

        root.addChild(loopLabel);
        ModStackPanel loopRow = new ModStackPanel("loop", ArrayDirection.HORIZONTAL).setGap(2);
        loopRow.addChild(new ModButton("Stamina", "Wait for full stamina between batches",
                () -> setLoopBy(LoopBy.STAMINA)));
        loopRow.addChild(new ModButton("Timer", "Wait a fixed interval between batches (e.g. spells)",
                () -> setLoopBy(LoopBy.TIMER)));
        root.addChild(loopRow);

        root.addChild(new ModLabel("Timer interval (ms):"));
        ModInputField timerField = new ModInputField("timerMs", 80);
        timerField.setValue(Long.toString(config.timerIntervalMs));
        java.util.function.Consumer<String> writeTimer = text -> {
            try {
                long v = Long.parseLong(text.trim());
                if (v < 0) return;
                config.setTimerIntervalMs(v);
            } catch (NumberFormatException ignored) { }
        };
        timerField.onChange(writeTimer);
        timerField.onSubmit(writeTimer);
        root.addChild(timerField);

        root.addChild(statusLabel);

        ModStackPanel controls = new ModStackPanel("controls", ArrayDirection.HORIZONTAL).setGap(4);
        controls.addChild(new ModButton("Start", "Start the loop",
                () -> listener.onStart(mode, direction, loopBy, batchSize)));
        controls.addChild(new ModButton("Pause", "Stop the loop", listener::onPause));
        root.addChild(controls);
        return root;
    }

    private void setDirection(Direction d) {
        this.direction = d;
        directionLabel.setText("Direction: " + label(d));
    }

    private void setLoopBy(LoopBy l) {
        this.loopBy = l;
        loopLabel.setText(l == LoopBy.TIMER
                ? "Loop: fixed timer"
                : "Loop: wait for full stamina");
    }

    private void setMode(Mode m) {
        this.mode = m;
        String desc;
        switch (m) {
            case CRAFT: desc = "Craft (inventory)"; break;
            case BODY:  desc = "Body";              break;
            default:    desc = "Tile (mining)";     break;
        }
        modeLabel.setText("Mode: " + desc);
    }

    private void changeBatch(int delta) {
        this.batchSize = Math.max(1, Math.min(10, batchSize + delta));
        batchLabel.setText("Actions per batch: " + batchSize);
    }

    public void setStatus(String text) {
        statusLabel.setText("Status: " + text);
    }

    private static String label(Direction d) {
        switch (d) { case UP: return "Up"; case DOWN: return "Down"; default: return "Forward"; }
    }

    @Override
    public void closePressed() {
        super.closePressed();
        listener.onClose();
    }
}
