package com.garward.wurmmodloader.mods.automine;

import com.garward.wurmmodloader.client.api.gui.ArrayDirection;
import com.garward.wurmmodloader.client.api.gui.Insets;
import com.garward.wurmmodloader.client.api.gui.ModButton;
import com.garward.wurmmodloader.client.api.gui.ModLabel;
import com.garward.wurmmodloader.client.api.gui.ModStackPanel;
import com.garward.wurmmodloader.client.api.gui.ModWindow;

/**
 * Popup window for the automine controls. Direction buttons / batch +/- /
 * Start / Pause. Wires user actions into {@link AutomineWindow.Listener}.
 */
public final class AutomineWindow extends ModWindow {

    public enum Direction { FORWARD, UP, DOWN }

    public interface Listener {
        void onStart(Direction direction, int batchSize);
        void onPause();
        void onClose();
    }

    private final Listener listener;
    private final AutomineConfig config;
    private final ModLabel statusLabel;
    private final ModLabel directionLabel;
    private final ModLabel batchLabel;

    private Direction direction = Direction.FORWARD;
    private int batchSize;

    public AutomineWindow(AutomineConfig config, Listener listener) {
        super("Auto Mine");
        this.config = config;
        this.listener = listener;
        this.batchSize = config.defaultBatchSize;
        this.statusLabel = new ModLabel("Status: idle");
        this.directionLabel = new ModLabel("Direction: Forward");
        this.batchLabel = new ModLabel("Actions per batch: " + batchSize);
        lockSize();
        installContent(buildRoot(), 240, 200);
    }

    private ModStackPanel buildRoot() {
        ModStackPanel root = new ModStackPanel("Automine root", ArrayDirection.VERTICAL)
                .setBackgroundPainted(true)
                .setPadding(Insets.uniform(6))
                .setGap(4);
        root.addChild(directionLabel);

        ModStackPanel dirRow = new ModStackPanel("dir", ArrayDirection.HORIZONTAL).setGap(2);
        dirRow.addChild(new ModButton("Forward", "Mine Forward", () -> setDirection(Direction.FORWARD)));
        dirRow.addChild(new ModButton("Up",      "Mine Up",      () -> setDirection(Direction.UP)));
        dirRow.addChild(new ModButton("Down",    "Mine Down",    () -> setDirection(Direction.DOWN)));
        root.addChild(dirRow);

        root.addChild(batchLabel);
        ModStackPanel batchRow = new ModStackPanel("batch", ArrayDirection.HORIZONTAL).setGap(2);
        batchRow.addChild(new ModButton("-", "Decrease batch size", () -> changeBatch(-1)));
        batchRow.addChild(new ModButton("+", "Increase batch size", () -> changeBatch(+1)));
        root.addChild(batchRow);

        root.addChild(statusLabel);

        ModStackPanel controls = new ModStackPanel("controls", ArrayDirection.HORIZONTAL).setGap(4);
        controls.addChild(new ModButton("Start", "Start automining",
                () -> listener.onStart(direction, batchSize)));
        controls.addChild(new ModButton("Pause", "Stop the loop", listener::onPause));
        root.addChild(controls);
        return root;
    }

    private void setDirection(Direction d) {
        this.direction = d;
        directionLabel.setText("Direction: " + label(d));
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
