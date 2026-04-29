package com.garward.wurmmodloader.mods.automine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Loads {@code automine.properties} from the deployed mod folder and exposes
 * typed accessors. Action IDs are mutable so the UI can rewrite them live and
 * persist back to disk; other fields are immutable.
 */
public final class AutomineConfig {

    private static final Logger logger = Logger.getLogger(AutomineConfig.class.getName());

    public short actionForward;
    public short actionUp;
    public short actionDown;
    public final int defaultBatchSize;
    public final float staminaFullThreshold;
    public final long staminaWatchdogMs;
    public long timerIntervalMs;
    public final List<Pattern> stopPhrases;

    private final File propertiesFile;
    private final Properties properties;

    private AutomineConfig(File file, Properties props,
                           short fwd, short up, short down, int batch,
                           float thr, long watchdog, long timerMs,
                           List<Pattern> phrases) {
        this.propertiesFile = file;
        this.properties = props;
        this.actionForward = fwd;
        this.actionUp = up;
        this.actionDown = down;
        this.defaultBatchSize = batch;
        this.staminaFullThreshold = thr;
        this.staminaWatchdogMs = watchdog;
        this.timerIntervalMs = timerMs;
        this.stopPhrases = Collections.unmodifiableList(phrases);
    }

    public static AutomineConfig load(File propertiesFile) {
        Properties p = new Properties();
        if (propertiesFile != null && propertiesFile.isFile()) {
            try (FileInputStream in = new FileInputStream(propertiesFile)) {
                p.load(in);
            } catch (IOException e) {
                logger.log(Level.WARNING, "[automine] failed to read " + propertiesFile + " — using defaults", e);
            }
        }
        short fwd  = (short) parseInt(p, "actionId.forward", 38);
        short up   = (short) parseInt(p, "actionId.up", 39);
        short down = (short) parseInt(p, "actionId.down", 40);
        int batch  = clamp(parseInt(p, "defaultBatchSize", 3), 1, 10);
        float thr  = (float) parseDouble(p, "staminaFullThreshold", 0.98);
        long wd    = parseLong(p, "staminaWatchdogMs", 60_000);
        long tmr   = parseLong(p, "timerIntervalMs", 5_000);

        List<Pattern> phrases = new ArrayList<>();
        for (int i = 0; ; i++) {
            String key = "stopPhrase." + i;
            String v = p.getProperty(key);
            if (v == null) break;
            try { phrases.add(Pattern.compile(v, Pattern.CASE_INSENSITIVE)); }
            catch (Exception e) { logger.warning("[automine] bad regex at " + key + ": " + v); }
        }
        if (phrases.isEmpty()) {
            for (String def : new String[]{
                    "you are too unskilled to mine",
                    "the topology here makes it impossible",
                    "the water is too deep to mine",
                    "this tile is protected by the gods",
                    "the surrounding area needs to be rock",
                    "you cannot mine"}) {
                phrases.add(Pattern.compile(def, Pattern.CASE_INSENSITIVE));
            }
        }
        return new AutomineConfig(propertiesFile, p, fwd, up, down, batch, thr, wd, tmr, phrases);
    }

    public void setActionForward(short id) { this.actionForward = id; persist("actionId.forward", Short.toString(id)); }
    public void setActionUp(short id)      { this.actionUp = id;      persist("actionId.up", Short.toString(id)); }
    public void setActionDown(short id)    { this.actionDown = id;    persist("actionId.down", Short.toString(id)); }
    public void setTimerIntervalMs(long ms) {
        this.timerIntervalMs = Math.max(0, ms);
        persist("timerIntervalMs", Long.toString(this.timerIntervalMs));
    }

    private void persist(String key, String value) {
        if (properties == null || propertiesFile == null) return;
        properties.setProperty(key, value);
        try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
            properties.store(out, "automine — updated by UI");
        } catch (IOException e) {
            logger.log(Level.WARNING, "[automine] failed to persist " + key + " to " + propertiesFile, e);
        }
    }

    private static int parseInt(Properties p, String k, int d) {
        try { return Integer.parseInt(p.getProperty(k, Integer.toString(d)).trim()); }
        catch (Exception e) { return d; }
    }
    private static long parseLong(Properties p, String k, long d) {
        try { return Long.parseLong(p.getProperty(k, Long.toString(d)).trim()); }
        catch (Exception e) { return d; }
    }
    private static double parseDouble(Properties p, String k, double d) {
        try { return Double.parseDouble(p.getProperty(k, Double.toString(d)).trim()); }
        catch (Exception e) { return d; }
    }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    public boolean isStopPhrase(String text) {
        if (text == null) return false;
        for (Pattern pat : stopPhrases) if (pat.matcher(text).find()) return true;
        return false;
    }
}
