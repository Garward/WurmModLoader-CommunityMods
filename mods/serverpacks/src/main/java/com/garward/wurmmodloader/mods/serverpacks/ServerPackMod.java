package com.garward.wurmmodloader.mods.serverpacks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.garward.wurmmodloader.modcomm.Channel;
import com.garward.wurmmodloader.modcomm.IChannelListener;
import com.garward.wurmmodloader.modcomm.ModComm;
import com.garward.wurmmodloader.modcomm.PacketReader;
import com.garward.wurmmodloader.modcomm.PacketWriter;
import com.garward.wurmmodloader.api.events.ModActionEvent;
import com.garward.wurmmodloader.api.events.ModQueryEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.core.event.EventBus;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.mods.serverpacks.api.ServerPacks;

import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;

public class ServerPackMod implements WurmServerMod, Configurable, ServerPacks {

	private static final byte CMD_REFRESH = 0x01;

	private static ServerPackMod instance;

	private Map<String, PackInfo> packs = new HashMap<>();

	private static final Logger logger = Logger.getLogger(ServerPackMod.class.getName());

	private Channel channel;

	private String prefix;

	public ServerPackMod() {
		instance = this;
	}

	@Override
	public void init() {
		channel = ModComm.registerChannel("ago.serverpacks", new IChannelListener() {
			@Override
			public void onPlayerConnected(Player player) {
				// Check if HTTP server is running using event API
				ModQueryEvent query = new ModQueryEvent("httpserver:is_running");
				EventBus.getInstance().post(query);
				Boolean running = (Boolean) query.get("running");

				if (running == null || !running) {
					logger.log(Level.WARNING, "HTTP server did not start properly. No server packs will be delivered.");
					return;
				}
				notifyPlayer(player, packs);
			}

			@Override
			public void handleMessage(Player player, ByteBuffer message) {
				try (PacketReader reader = new PacketReader(message)) {
					byte cmd = reader.readByte();
					switch (cmd) {
						case CMD_REFRESH:
							CommandHandler.sendModelRefresh(player);
							break;
						default:
							logger.log(Level.WARNING, String.format("Unknown channel command 0x%02x", 128 + cmd));
							break;
					}
				} catch (IOException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void configure(Properties properties) {
		// Configuration is handled framework-side via config/wurmmodloader-http.properties
	}

	/**
	 * Handle actions from other mods to add server packs.
	 *
	 * Supported action types:
	 * - serverpacks:add_pack -> Add a new server pack
	 *   Input: name (String), data (byte[]), force (Boolean), prepend (Boolean)
	 *   OR: name (String), path (Path), force (Boolean), prepend (Boolean)
	 */
	@SubscribeEvent
	public void onModAction(ModActionEvent event) {
		try {
			if (event.getEventType().equals("serverpacks:add_pack")) {
				String name = event.getString("name");
				byte[] data = (byte[]) event.get("data");
				Path path = (Path) event.get("path");
				Boolean force = event.getBoolean("force");
				Boolean prepend = event.getBoolean("prepend");

				if (name == null) {
					logger.warning("serverpacks:add_pack requires 'name' parameter");
					return;
				}

				if (data == null && path == null) {
					logger.warning("serverpacks:add_pack requires either 'data' or 'path' parameter");
					return;
				}

				// Build options array
				java.util.List<ServerPackOptions> optionsList = new java.util.ArrayList<>();
				if (Boolean.TRUE.equals(force)) {
					optionsList.add(ServerPackOptions.FORCE);
				}
				if (Boolean.TRUE.equals(prepend)) {
					optionsList.add(ServerPackOptions.PREPEND);
				}
				ServerPackOptions[] options = optionsList.toArray(new ServerPackOptions[0]);

				// Add the pack
				if (data != null) {
					addServerPack(name, data, options);
				} else {
					addServerPack(name, path, options);
				}
				event.setHandled(true);

				logger.info("Added server pack via event API: " + name);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error handling ModActionEvent: " + event.getEventType(), e);
		}
	}

	private String getSha1Sum(Path packPath) throws IOException, NoSuchAlgorithmException {
		try (InputStream is = Files.newInputStream(packPath)) {
			return getSha1Sum(is);
		}
	}

	private String getSha1Sum(InputStream is) throws NoSuchAlgorithmException, IOException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		messageDigest.reset();
		int n = 0;
		byte[] buffer = new byte[8192];
		while (n != -1) {
			n = is.read(buffer);
			if (n > 0) {
				messageDigest.update(buffer, 0, n);
			}
		}
		byte[] digest = messageDigest.digest();
		return javax.xml.bind.DatatypeConverter.printHexBinary(digest);
	}

	private void addPack(Path packPath, ServerPackOptions... options) throws NoSuchAlgorithmException, IOException {
		String sha1Sum = getSha1Sum(packPath);
		addPack(sha1Sum, new PackInfo(packPath, options));
		logger.info("Added pack " + sha1Sum + " for pack " + packPath);
	}
	
	private void addPack(String name, Path packPath, ServerPackOptions... options) {
		addPack(name, new PackInfo(packPath, options));
		logger.info("Added pack " + name + " for pack " + packPath);
	}

	private void addPack(byte[] data, ServerPackOptions... options) throws NoSuchAlgorithmException, IOException {
		String sha1Sum = getSha1Sum(new ByteArrayInputStream(data));
		addPack(sha1Sum, new PackInfo(data, options));
		logger.info("Added pack " + sha1Sum);
	}

	private void addPack(String name, byte[] data, ServerPackOptions... options) {
		addPack(name, new PackInfo(data, options));
		logger.info("Added pack " + name);
	}
	
	private void addPack(String name, PackInfo packInfo) {
		packs.put(name, packInfo);
		notifyPlayers(Collections.singletonMap(name, packInfo));
	}
	
	private void notifyPlayer(Player player, Map<String, PackInfo> packs) {
		try {
			// Get HTTP server base URI using event API
			ModQueryEvent uriQuery = new ModQueryEvent("httpserver:get_uri");
			EventBus.getInstance().post(uriQuery);
			URI baseUri = (URI) uriQuery.get("uri");

			if (baseUri == null) {
				logger.warning("HTTP server not running, cannot notify player of server packs");
				return;
			}
			URI uri = baseUri.resolve(prefix);
			try (PacketWriter writer = new PacketWriter()) {
				writer.writeInt(packs.size());
				for (Map.Entry<String, PackInfo> entry : packs.entrySet()) {
					final String packId = entry.getKey();
					final PackInfo info = entry.getValue();

					final Set<String> options = new LinkedHashSet<>();
					if (info.prepend) {
						options.add("prepend");
					}
					if (info.force) {
						options.add("force");
					}
					final String query = options.isEmpty() ? "" : options.stream().collect(Collectors.joining("&", "?", ""));
					final URI packUri = uri.resolve(packId);
					writer.writeUTF(packId);
					writer.writeUTF(packUri.toString() + query);
				}
				channel.sendMessage(player, writer.getBytes());
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	private void notifyPlayers(Map<String, PackInfo> packs) {
		if (this.channel != null && this.prefix != null) {
			for (Player player : Players.getInstance().getPlayers()) {
				if (channel.isActiveForPlayer(player)) {
					notifyPlayer(player, packs);
				}
			}
		}
	}

	private InputStream servePack(String packid) {
		try {
			PackInfo info = packs.get(packid);
			if (info != null && info.data != null) {
				return new ByteArrayInputStream(info.data);
			}
			if (info != null && info.path != null) {
				return Files.newInputStream(info.path);
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return null;
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		// Register HTTP endpoint using event API
		ModActionEvent registerEndpoint = new ModActionEvent("httpserver:register_endpoint");
		registerEndpoint.set("modName", "serverpacks");
		registerEndpoint.set("pattern", Pattern.compile("^/(?<path>[^/]*)$"));
		registerEndpoint.set("handler", (java.util.function.Function<String, InputStream>) this::servePack);
		EventBus.getInstance().post(registerEndpoint);

		this.prefix = registerEndpoint.getString("prefix");
		if (prefix == null) {
			throw new RuntimeException("Failed to register server pack http handler");
		}

		logger.info("Registered serverpacks HTTP endpoint at: " + prefix);
	}

	public static ServerPackMod getInstance() {
		return instance;
	}

	@Override
	public void addServerPack(Path path, ServerPackOptions... options) {
		try {
			addPack(path, options);
		} catch (IOException | NoSuchAlgorithmException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

	}

	@Override
	public void addServerPack(byte[] data, ServerPackOptions... options) {
		try {
			addPack(data, options);
		} catch (IOException | NoSuchAlgorithmException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	private void checkPackName(String name) {
		for (char c : name.toCharArray()) {
			if (c == '.' || c == '/' || c == '%' || c == '?' || c == '#') {
				throw new IllegalArgumentException(name);
			}
		}
	}

	@Override
	public void addServerPack(String name, byte[] data, ServerPackOptions... options) {
		checkPackName(name);
		addPack(name, data, options);
	}
	
	@Override
	public void addServerPack(String name, Path path, ServerPackOptions... options) {
		checkPackName(name);
		addPack(name, path, options);
	}
	
}
