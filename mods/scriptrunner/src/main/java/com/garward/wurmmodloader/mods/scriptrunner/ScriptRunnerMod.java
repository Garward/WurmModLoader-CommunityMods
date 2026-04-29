package com.garward.wurmmodloader.mods.scriptrunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.garward.wurmmodloader.api.events.ModActionEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.player.PlayerLoginEvent;
import com.garward.wurmmodloader.api.events.player.PlayerLogoutEvent;
import com.garward.wurmmodloader.api.events.player.PlayerMessageEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.api.events.server.ServerStoppingEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.ModEntry;
import com.garward.wurmmodloader.modloader.interfaces.ModListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;
import com.garward.wurmmodloader.modsupport.creatures.ModCreatures;
import com.garward.wurmmodloader.modsupport.vehicles.ModVehicleBehaviours;

/**
 * Server-driven JavaScript runner. Loads Nashorn scripts from
 * {@code mods/<mod>/scripts/<triggerName>/*.js} and dispatches framework
 * events to them.
 *
 * <p>Script-facing triggers (function names expected in each .js):
 * <ul>
 *   <li>{@code onServerStarted()}, {@code onServerShutdown()}</li>
 *   <li>{@code onPlayerLogin(player)}, {@code onPlayerLogout(player)}</li>
 *   <li>{@code onPlayerMessage(communicator, message, title)} — return {@code "DISCARD"}
 *       (or the legacy {@code MessagePolicy.DISCARD}) to swallow the message</li>
 *   <li>{@code onItemTemplatesCreated()}</li>
 *   <li>{@code onServerPoll()} — fires every server tick</li>
 * </ul>
 *
 * <p>Hot reload: set {@code <triggerName>.refresh=true} in the owning mod's
 * {@code .properties} and scripts are re-read on mtime change each dispatch.
 */
public class ScriptRunnerMod implements WurmServerMod, Configurable, ModListener {

	private static final Logger LOGGER = Logger.getLogger(ScriptRunnerMod.class.getName());

	private final Map<String, List<ScriptRunner>> scriptRunners = new HashMap<>();

	@Override
	public void configure(Properties properties) {
	}

	@Override
	public void preInit() {
		ModActions.init();
	}

	@Override
	public void init() {
		ModVehicleBehaviours.init();
		ModCreatures.init();
	}

	private void initRunner(String triggerName, Properties properties, Path scriptsPath, ModEntry<?> modEntry) {
		boolean refresh = Boolean.parseBoolean(properties.getProperty(triggerName + ".refresh", "false"));
		final Path path = scriptsPath.resolve(triggerName);

		if (!Files.exists(path) && !refresh) {
			return;
		}
		LOGGER.info(String.format("script runner %s, path: %s, refresh: %s", triggerName, path, refresh));

		List<Path> importPaths = new ArrayList<>();
		importPaths.add(Paths.get("mods").resolve("scriptrunner/imports"));
		if (properties.getProperty("scriptsImport") != null) {
			Path importPath = Paths.get("mods").resolve(properties.getProperty("scriptsImport"));
			if (Files.isDirectory(importPath)) {
				importPaths.add(importPath);
			}
		}

		final ClassLoader classLoader = modEntry.getModClassLoader();
		scriptRunners.computeIfAbsent(triggerName, key -> new ArrayList<>())
				.add(new ScriptRunner(path, triggerName, refresh, importPaths, classLoader));
	}

	private List<Object> run(List<ScriptRunner> runners, Object... args) {
		if (runners != null) {
			return runners.stream().flatMap(runner -> runner.runScripts(args).stream()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		run(scriptRunners.get("onServerStarted"));
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		run(scriptRunners.get("onServerShutdown"));
	}

	@SubscribeEvent
	public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
		run(scriptRunners.get("onItemTemplatesCreated"));
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoginEvent event) {
		run(scriptRunners.get("onPlayerLogin"), event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLogoutEvent event) {
		run(scriptRunners.get("onPlayerLogout"), event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerMessage(PlayerMessageEvent event) {
		List<Object> results = run(scriptRunners.get("onPlayerMessage"),
				event.getCommunicator(), event.getMessage(), event.getTitle());
		// Accept either the legacy MessagePolicy.DISCARD (toString == "DISCARD")
		// or a plain "DISCARD" string from scripts — both mean swallow.
		if (results.stream().anyMatch(r -> r != null && "DISCARD".equals(r.toString()))) {
			event.setCancelled(true);
		}
	}

	@SubscribeEvent
	public void onServerPoll(ServerPollEvent event) {
		run(scriptRunners.get("onServerPoll"));
	}

	/**
	 * Generic ModActionEvent dispatcher. Each script under
	 * {@code scripts/onModAction/} receives {@code (eventType, event)} and can
	 * filter on {@code eventType} (e.g. {@code "ui:action"}) to handle a
	 * specific action without subclassing the framework's event types.
	 */
	@SubscribeEvent
	public void onModAction(ModActionEvent event) {
		run(scriptRunners.get("onModAction"), event.getEventType(), event);
	}

	@Override
	public void modInitialized(ModEntry<?> entry) {
		if (entry == null || entry.getProperties() == null) {
			return;
		}

		Properties properties = entry.getProperties();
		final String modName = entry.getName();
		final String defaultScriptsFolder = modName + "/scripts";
		final String scriptsFolder = properties.getProperty("scriptsFolder", defaultScriptsFolder);

		Path scriptsPath = Paths.get("mods").resolve(scriptsFolder);
		if (Files.isDirectory(scriptsPath)) {
			LOGGER.info(modName + ": scriptsFolder: " + scriptsFolder);
		} else if (entry.getWurmMod() == this) {
			throw new IllegalArgumentException("ScriptsPath does not exist: " + scriptsPath);
		} else {
			return;
		}

		initRunner("onServerStarted", properties, scriptsPath, entry);
		initRunner("onServerShutdown", properties, scriptsPath, entry);
		initRunner("onPlayerLogin", properties, scriptsPath, entry);
		initRunner("onPlayerLogout", properties, scriptsPath, entry);
		initRunner("onPlayerMessage", properties, scriptsPath, entry);
		initRunner("onItemTemplatesCreated", properties, scriptsPath, entry);
		initRunner("onServerPoll", properties, scriptsPath, entry);
		initRunner("onModAction", properties, scriptsPath, entry);
	}
}
