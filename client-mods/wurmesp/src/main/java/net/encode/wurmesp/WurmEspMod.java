package net.encode.wurmesp;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.client.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.client.api.events.client.ClientConsoleInputEvent;
import com.garward.wurmmodloader.client.api.events.lifecycle.ClientWorldLoadedEvent;
import com.garward.wurmmodloader.client.api.events.map.ClientHUDInitializedEvent;
import com.garward.wurmmodloader.client.api.events.render.CellRenderableInitEvent;
import com.garward.wurmmodloader.client.api.events.render.CellRenderableRemovedEvent;
import com.garward.wurmmodloader.client.api.events.render.WorldRenderPostEvent;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickRenderer;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.cell.CellRenderable;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.MainMenu;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.renderer.gui.WurmEspWindow;
import com.wurmonline.client.settings.SavePosManager;
import com.wurmonline.client.sound.FixedSoundSource;
import com.wurmonline.client.sound.SoundSource;
import com.wurmonline.mesh.Tiles;

/**
 * WurmEsp ported to the Garward client modloader framework.
 *
 * <p><b>Port status:</b>
 * <ul>
 *   <li>Config load — reads {@code mods/wurmesp.properties} directly.</li>
 *   <li>HUD window injection — via {@link ClientHUDInitializedEvent}.</li>
 *   <li>Render pass — via {@link PickRenderPostEvent}.</li>
 *   <li>⚠ Unit/ground-item tracking — framework lacks CellRenderable
 *       lifecycle events, so the mod scans {@code World} each frame.</li>
 *   <li>⚠ Console {@code esp ...} commands — framework lacks a console-input
 *       event; toggle via config file + restart for now.</li>
 *   <li>⚠ Deed-plan packet hook — deferred.</li>
 *   <li>⚠ Visible overlay — {@link PickRenderPostEvent} writes to the pick
 *       buffer; a follow-up {@code WorldRenderPostEvent} on
 *       {@code WorldRender.renderPickedItem} is needed for on-screen ESP.</li>
 * </ul>
 */
public class WurmEspMod {

    public static final Logger logger = Logger.getLogger("WurmEspMod");

    public static HeadsUpDisplay hud;
    public static Properties modProperties = new Properties();

    private final List<Unit> pickableUnits = new ArrayList<Unit>();
    private final java.util.HashSet<Long> knownIds = new java.util.HashSet<Long>();
    /** id → epoch ms of last alert. Throttles re-alerts on out→in transitions
     *  so LOD/culling doesn't spam. */
    private final java.util.HashMap<Long, Long> lastAlertMs = new java.util.HashMap<Long, Long>();
    /** Earliest time (epoch ms) at which alerts may sound. Bumped forward on
     *  world load to absorb the login burst of CellRenderableInit events. */
    private long alertsAllowedAfter = System.currentTimeMillis() + 30_000L;
    /** Last time any alert played (epoch ms). Enforces global anti-spam gap. */
    private long lastAnyAlertMs = 0L;

    private CronoManager xrayCronoManager;
    private CronoManager tilesCloseByCronoManager;
    private CronoManager tilesHighlightCronoManager;
    private CronoManager tilesCloseByWalkableCronoManager;
    private XRayManager xrayManager;
    private TilesCloseByManager tilesCloseByManager;
    public static TilesHighlightManager tilesHighlightManager;
    private TilesWalkableManager tilesCloseByWalkableManager;

    public static CaveDataBuffer _caveBuffer = null;
    public static NearTerrainDataBuffer _terrainBuffer = null;
    public static NearTerrainDataBuffer _terrainBuffer2 = null;
    public static List<float[]> _terrain = new ArrayList<float[]>();
    public static List<float[]> _closeByTerrain = new ArrayList<float[]>();
    public static List<float[]> _closeByWalkableTerrain = new ArrayList<float[]>();
    public static List<int[]> _tilesHighlightBase = new ArrayList<int[]>();
    public static List<float[]> _tilesHighlightTerrain = new ArrayList<float[]>();

    public enum SEARCHTYPE { NONE, HOVER, MODEL, BOTH }

    public static String search = "defaultnosearch";
    public static SEARCHTYPE searchType = SEARCHTYPE.NONE;

    public static boolean players = true;
    public static boolean mobs = false;
    public static boolean specials = true;
    public static boolean items = true;
    public static boolean uniques = true;
    public static boolean conditioned = true;
    public static boolean tilescloseby = false;
    public static boolean deedsize = false;
    public static boolean tileshighlight = false;
    public static boolean tilesclosebynotrideable = false;
    public static boolean xray = false;
    public static boolean xraythread = true;
    public static boolean xrayrefreshthread = true;
    public static int xraydiameter = 32;
    public static int xrayrefreshrate = 5;
    public static int tilenotrideable = 40;
    public static boolean playsoundspecial = false;
    public static boolean playsounditem = false;
    public static boolean playsoundunique = true;
    public static String soundspecial = "sound.bell.handbell";
    public static String sounditem = "sound.bell.handbell";
    public static String soundunique = "sound.bell.handbell";
    /** Per-id alert cooldown in seconds. */
    public static int soundCooldownSeconds = 60;
    /** Volume multiplier applied to alert sounds (0.0–1.0). */
    public static float soundVolume = 0.25f;
    /** Grace period after world load during which all alerts are suppressed.
     *  At login, the world streams in dozens of units at once — without this,
     *  every unique would alert at the same moment. */
    public static int loginGraceSeconds = 30;
    /** Minimum seconds between any two alerts globally (anti-spam). */
    public static int globalAlertGapSeconds = 3;
    public static boolean conditionedcolorsallways = false;
    public static boolean championmcoloralways = false;

    public static PickRenderer _pickRenderer;

    public WurmEspMod() {
        xrayManager = new XRayManager();
        tilesCloseByManager = new TilesCloseByManager();
        tilesHighlightManager = new TilesHighlightManager();
        tilesCloseByWalkableManager = new TilesWalkableManager();
        xrayCronoManager = new CronoManager(xrayrefreshrate * 1000);
        tilesCloseByCronoManager = new CronoManager(1000);
        tilesHighlightCronoManager = new CronoManager(5000);
        tilesCloseByWalkableCronoManager = new CronoManager(1000);

        loadConfig();
        logger.info("[WurmEspMod] Constructed — config loaded");
    }

    @SubscribeEvent
    public void onHudInit(ClientHUDInitializedEvent event) {
        try {
            hud = (HeadsUpDisplay) event.getHud();
            initEspWR();
            logger.info("[WurmEspMod] ESP window registered on HUD");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[WurmEspMod] HUD init failed", t);
        }
    }

    @SubscribeEvent
    public void onWorldRenderPost(WorldRenderPostEvent event) {
        Queue queue = (Queue) event.getQueue();
        if (_pickRenderer == null) {
            _pickRenderer = (PickRenderer) event.getPickRenderer();
        }
        if (hud == null) return;
        World world = hud.getWorld();
        if (world == null) return;

        for (Unit unit : pickableUnits) {
            if ((players && unit.isPlayer()) || (uniques && unit.isUnique())
                    || (conditioned && unit.isConditioned()) || (mobs && unit.isMob())
                    || (specials && unit.isSpecial()) || (items && unit.isSpotted())) {
                boolean colorOverride =
                        (unit.isConditioned() && conditioned)
                        || (unit.isConditioned() && conditionedcolorsallways)
                        || (unit.isChampion() && championmcoloralways);
                unit.renderUnit(queue, colorOverride);
            }
        }

        if (tileshighlight) {
            tilesHighlightManager._setWQ(world, queue);
            if (tilesHighlightManager._first) {
                tilesHighlightManager._refreshData();
                tilesHighlightManager._first = false;
            } else if (tilesHighlightCronoManager.hasEnded()) {
                tilesHighlightManager._refreshData();
                tilesHighlightCronoManager.restart(5000);
            }
            tilesHighlightManager._queueTilesHighlight();
        } else {
            tilesHighlightManager._setW(world);
        }

        if (tilescloseby && world.getPlayer().getPos().getLayer() >= 0) {
            tilesCloseByManager._setWQ(world, queue);
            if (tilesCloseByManager._first) {
                tilesCloseByManager._refreshData();
                tilesCloseByManager._first = false;
            } else if (tilesCloseByCronoManager.hasEnded()) {
                tilesCloseByManager._refreshData();
                tilesCloseByCronoManager.restart(1000);
            }
            tilesCloseByManager._queueTiles();
        }

        if (tilesclosebynotrideable && world.getPlayer().getPos().getLayer() >= 0) {
            tilesCloseByWalkableManager._setWQ(world, queue);
            if (tilesCloseByWalkableManager._first) {
                tilesCloseByWalkableManager._refreshData();
                tilesCloseByWalkableManager._first = false;
            } else if (tilesCloseByWalkableCronoManager.hasEnded()) {
                tilesCloseByWalkableManager._refreshData();
                tilesCloseByWalkableCronoManager.restart(1000);
            }
            tilesCloseByWalkableManager._queueTiles();
        }

        if (xray && world.getPlayer().getPos().getLayer() < 0) {
            xrayManager._setWQ(world, queue);
            if (xrayManager._first) {
                xrayManager._refreshData();
                xrayManager._first = false;
            } else if (xrayCronoManager.hasEnded()) {
                xrayManager._refreshData();
                xrayCronoManager.restart(xrayrefreshrate * 1000);
            }
            xrayManager._queueXray();
        }
    }

    @SubscribeEvent
    public void onCellRenderableInit(CellRenderableInitEvent event) {
        Object obj = event.getRenderable();
        if (!(obj instanceof CreatureCellRenderable)) return;
        CreatureCellRenderable c = (CreatureCellRenderable) obj;
        try {
            long id = c.getId();
            if (knownIds.contains(id)) return;
            String modelName = c.getModelName() != null ? c.getModelName().toString() : "";
            Unit u = new Unit(id, c, modelName, c.getHoverName());
            if (u.isPlayer() || u.isMob() || u.isSpecial() || u.isSpotted()) {
                pickableUnits.add(u);
                knownIds.add(id);
                maybePlayNewUnitSound(u);
            }
        } catch (Throwable t) {
            logger.log(Level.FINE, "[WurmEspMod] onCellRenderableInit soft-fail", t);
        }
    }

    @SubscribeEvent
    public void onCellRenderableRemoved(CellRenderableRemovedEvent event) {
        Object obj = event.getRenderable();
        if (!(obj instanceof CreatureCellRenderable)) return;
        try {
            long id = ((CreatureCellRenderable) obj).getId();
            if (knownIds.remove(id)) {
                pickableUnits.removeIf(u -> u.getId() == id);
            }
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public void onWorldLoaded(ClientWorldLoadedEvent event) {
        alertsAllowedAfter = System.currentTimeMillis() + loginGraceSeconds * 1000L;
        lastAlertMs.clear();
        knownIds.clear();
        pickableUnits.clear();
        logger.info("[WurmEspMod] World loaded — alerts suppressed for "
                + loginGraceSeconds + "s");
    }

    @SubscribeEvent
    public void onConsoleInput(ClientConsoleInputEvent event) {
        String cmd = event.getCommand();
        if (cmd == null || !"esp".equalsIgnoreCase(cmd)) return;
        String[] args = event.getArgs();
        if (args == null || args.length < 2) {
            event.cancel();
            return;
        }
        String feature = args[1].toLowerCase();
        switch (feature) {
            case "players": players = !players; break;
            case "mobs": mobs = !mobs; break;
            case "specials": specials = !specials; break;
            case "items": items = !items; break;
            case "uniques": uniques = !uniques; break;
            case "conditioned": conditioned = !conditioned; break;
            case "tilescloseby": tilescloseby = !tilescloseby; break;
            case "tilesclosebynotrideable": tilesclosebynotrideable = !tilesclosebynotrideable; break;
            case "tileshighlight": tileshighlight = !tileshighlight; break;
            case "xray": xray = !xray; break;
            case "deedsize": deedsize = !deedsize; break;
            default:
                return; // let vanilla handle or not
        }
        logger.info("[WurmEspMod] toggled " + feature);
        event.cancel();
    }

    private static Field findField(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private void maybePlayNewUnitSound(Unit u) {
        try {
            String sound = null;
            if (u.isUnique() && uniques && playsoundunique) sound = soundunique;
            else if (u.isSpecial() && specials && playsoundspecial) sound = soundspecial;
            else if (u.isSpotted() && items && playsounditem) sound = sounditem;
            if (sound == null) return;
            long now = System.currentTimeMillis();
            if (now < alertsAllowedAfter) return;
            if (now - lastAnyAlertMs < globalAlertGapSeconds * 1000L) return;
            long id = u.getId();
            Long last = lastAlertMs.get(id);
            if (last != null && now - last < soundCooldownSeconds * 1000L) return;
            lastAlertMs.put(id, now);
            lastAnyAlertMs = now;
            playSound(sound);
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings("unchecked")
    private void initEspWR() throws Exception {
        WurmEspWindow wurmEspWindow = new WurmEspWindow();

        Field mainMenuField = findField(hud.getClass(), "mainMenu");
        MainMenu mainMenu = (MainMenu) mainMenuField.get(hud);
        mainMenu.registerComponent("Esp", wurmEspWindow);

        Field componentsField = findField(hud.getClass(), "components");
        List<WurmComponent> components = (List<WurmComponent>) componentsField.get(hud);
        components.add(wurmEspWindow);

        Field savePosField = findField(hud.getClass(), "savePosManager");
        SavePosManager savePosManager = (SavePosManager) savePosField.get(hud);
        savePosManager.registerAndRefresh(wurmEspWindow, "wurmespwindow");
    }

    private void loadConfig() {
        // Canonical layout: mods/wurmesp/mod.config. Fallbacks kept for
        // legacy users who still drop wurmesp.properties next to mods/.
        Path[] candidates = new Path[] {
                Paths.get("mods", "wurmesp", "mod.config"),
                Paths.get("mods", "wurmesp.properties"),
        };
        Path found = null;
        for (Path c : candidates) if (Files.exists(c)) { found = c; break; }
        InputStream in = null;
        try {
            in = (found != null)
                    ? Files.newInputStream(found)
                    : getClass().getResourceAsStream("/wurmesp.properties");
            if (in != null) {
                modProperties.load(in);
                logger.info("[WurmEspMod] Config loaded from "
                        + (found != null ? found.toString() : "classpath"));
            } else {
                logger.warning("[WurmEspMod] mod.config not found — using defaults");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "[WurmEspMod] failed reading config", e);
        } finally {
            if (in != null) try { in.close(); } catch (IOException ignored) {}
        }
        DoConfig(modProperties);
    }

    private static float[] colorStringToFloatA(String color) {
        String[] colors = color.split(",");
        return new float[]{
                Float.valueOf(colors[0]) / 255.0f,
                Float.valueOf(colors[1]) / 255.0f,
                Float.valueOf(colors[2]) / 255.0f
        };
    }

    private static String colorFloatAToString(float[] color) {
        return (color[0] * 255.0f) + "," + (color[1] * 255.0f) + "," + (color[2] * 255.0f);
    }

    public static void DoConfig(Properties p) {
        players = Boolean.valueOf(p.getProperty("players", Boolean.toString(players)));
        mobs = Boolean.valueOf(p.getProperty("mobs", Boolean.toString(mobs)));
        specials = Boolean.valueOf(p.getProperty("specials", Boolean.toString(specials)));
        items = Boolean.valueOf(p.getProperty("items", Boolean.toString(items)));
        uniques = Boolean.valueOf(p.getProperty("uniques", Boolean.toString(uniques)));
        conditioned = Boolean.valueOf(p.getProperty("conditioned", Boolean.toString(conditioned)));
        tilescloseby = Boolean.valueOf(p.getProperty("tilescloseby", Boolean.toString(tilescloseby)));
        deedsize = Boolean.valueOf(p.getProperty("deedsize", Boolean.toString(deedsize)));
        xray = Boolean.valueOf(p.getProperty("xray", Boolean.toString(xray)));
        xraythread = Boolean.valueOf(p.getProperty("xraythread", Boolean.toString(xraythread)));
        xrayrefreshthread = Boolean.valueOf(p.getProperty("xrayrefreshthread", Boolean.toString(xrayrefreshthread)));
        xraydiameter = Integer.parseInt(p.getProperty("xraydiameter", Integer.toString(xraydiameter)));
        xrayrefreshrate = Integer.parseInt(p.getProperty("xrayrefreshrate", Integer.toString(xrayrefreshrate)));
        tilenotrideable = Integer.parseInt(p.getProperty("tilenotrideable", Integer.toString(tilenotrideable)));
        playsoundspecial = Boolean.valueOf(p.getProperty("playsoundspecial", Boolean.toString(playsoundspecial)));
        playsounditem = Boolean.valueOf(p.getProperty("playsounditem", Boolean.toString(playsounditem)));
        playsoundunique = Boolean.valueOf(p.getProperty("playsoundunique", Boolean.toString(playsoundunique)));
        soundspecial = p.getProperty("soundspecial", soundspecial);
        sounditem = p.getProperty("sounditem", sounditem);
        soundunique = p.getProperty("soundunique", soundunique);
        soundCooldownSeconds = Integer.parseInt(p.getProperty("soundCooldownSeconds", Integer.toString(soundCooldownSeconds)));
        soundVolume = Float.parseFloat(p.getProperty("soundVolume", Float.toString(soundVolume)));
        loginGraceSeconds = Integer.parseInt(p.getProperty("loginGraceSeconds", Integer.toString(loginGraceSeconds)));
        globalAlertGapSeconds = Integer.parseInt(p.getProperty("globalAlertGapSeconds", Integer.toString(globalAlertGapSeconds)));
        conditionedcolorsallways = Boolean.valueOf(p.getProperty("conditionedcolorsallways", Boolean.toString(conditionedcolorsallways)));
        championmcoloralways = Boolean.valueOf(p.getProperty("championmcoloralways", Boolean.toString(championmcoloralways)));

        Unit.colorPlayers = colorStringToFloatA(p.getProperty("colorPlayers", colorFloatAToString(Unit.colorPlayers)));
        Unit.colorPlayersEnemy = colorStringToFloatA(p.getProperty("colorPlayersEnemy", colorFloatAToString(Unit.colorPlayersEnemy)));
        Unit.colorMobs = colorStringToFloatA(p.getProperty("colorMobs", colorFloatAToString(Unit.colorMobs)));
        Unit.colorMobsAggro = colorStringToFloatA(p.getProperty("colorMobsAggro", colorFloatAToString(Unit.colorMobsAggro)));
        Unit.colorSpecials = colorStringToFloatA(p.getProperty("colorSpecials", colorFloatAToString(Unit.colorSpecials)));
        Unit.colorSpotted = colorStringToFloatA(p.getProperty("colorSpotted", colorFloatAToString(Unit.colorSpotted)));
        Unit.colorUniques = colorStringToFloatA(p.getProperty("colorUniques", colorFloatAToString(Unit.colorUniques)));
        Unit.colorAlert = colorStringToFloatA(p.getProperty("colorAlert", colorFloatAToString(Unit.colorAlert)));
        Unit.colorAngry = colorStringToFloatA(p.getProperty("colorAngry", colorFloatAToString(Unit.colorAngry)));
        Unit.colorChampion = colorStringToFloatA(p.getProperty("colorChampion", colorFloatAToString(Unit.colorChampion)));
        Unit.colorDiseased = colorStringToFloatA(p.getProperty("colorDiseased", colorFloatAToString(Unit.colorDiseased)));
        Unit.colorFierce = colorStringToFloatA(p.getProperty("colorFierce", colorFloatAToString(Unit.colorFierce)));
        Unit.colorGreenish = colorStringToFloatA(p.getProperty("colorGreenish", colorFloatAToString(Unit.colorGreenish)));
        Unit.colorHardened = colorStringToFloatA(p.getProperty("colorHardened", colorFloatAToString(Unit.colorHardened)));
        Unit.colorLurking = colorStringToFloatA(p.getProperty("colorLurking", colorFloatAToString(Unit.colorLurking)));
        Unit.colorRaging = colorStringToFloatA(p.getProperty("colorRaging", colorFloatAToString(Unit.colorRaging)));
        Unit.colorScared = colorStringToFloatA(p.getProperty("colorScared", colorFloatAToString(Unit.colorScared)));
        Unit.colorSlow = colorStringToFloatA(p.getProperty("colorSlow", colorFloatAToString(Unit.colorSlow)));
        Unit.colorSly = colorStringToFloatA(p.getProperty("colorSly", colorFloatAToString(Unit.colorSly)));

        applyOreColor(p, "oreColorOreIron", Tiles.Tile.TILE_CAVE_WALL_ORE_IRON, Color.RED.darker());
        applyOreColor(p, "oreColorOreCopper", Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER, Color.GREEN);
        applyOreColor(p, "oreColorOreTin", Tiles.Tile.TILE_CAVE_WALL_ORE_TIN, Color.GRAY);
        applyOreColor(p, "oreColorOreGold", Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD, Color.YELLOW.darker());
        applyOreColor(p, "oreColorOreAdamantine", Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE, Color.CYAN);
        applyOreColor(p, "oreColorOreGlimmersteel", Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL, Color.YELLOW.brighter());
        applyOreColor(p, "oreColorOreSilver", Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER, Color.LIGHT_GRAY);
        applyOreColor(p, "oreColorOreLead", Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD, Color.PINK.darker().darker());
        applyOreColor(p, "oreColorOreZinc", Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC, new Color(235, 235, 235));
        applyOreColor(p, "oreColorSlate", Tiles.Tile.TILE_CAVE_WALL_SLATE, Color.BLACK);
        applyOreColor(p, "oreColorMarble", Tiles.Tile.TILE_CAVE_WALL_MARBLE, Color.WHITE);
        applyOreColor(p, "oreColorSandstone", Tiles.Tile.TILE_CAVE_WALL_SANDSTONE, Color.YELLOW.darker().darker());
        applyOreColor(p, "oreColorRocksalt", Tiles.Tile.TILE_CAVE_WALL_ROCKSALT, Color.WHITE.darker());

        Unit.aggroMOBS = p.getProperty("aggroMOBS", "").split(";");
        Unit.uniqueMOBS = p.getProperty("uniqueMOBS", "").split(";");
        Unit.specialITEMS = p.getProperty("specialITEMS", "").split(";");
        Unit.spottedITEMS = p.getProperty("spottedITEMS", "").split(";");
        Unit.conditionedMOBS = p.getProperty("conditionedMOBS", "").split(";");
    }

    private static void applyOreColor(Properties p, String key, Tiles.Tile tile, Color fallback) {
        String v = p.getProperty(key, "default");
        if (!"default".equals(v)) XrayColors.addMapping(tile, colorStringToFloatA(v));
        else XrayColors.addMapping(tile, fallback);
    }

    private void playSound(String sound) {
        try {
            PlayerPosition pos = CellRenderable.world.getPlayer().getPos();
            CellRenderable.world.getSoundEngine().play(
                    sound,
                    (SoundSource) new FixedSoundSource(pos.getX(), pos.getY(), 2.0f),
                    soundVolume, 5.0f, 1.0f, false, false);
        } catch (Throwable ignored) {}
    }
}
