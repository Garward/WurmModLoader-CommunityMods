package org.gotti.wurmunlimited.mods.announcer;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PlayerLoginListener;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.server.ServerMessaging;

import com.wurmonline.server.players.Player;  // Type import for listener interface (acceptable per architecture rules)

public class AnnounceMod implements WurmServerMod, Initable, PreInitable, Configurable, PlayerLoginListener {

	private boolean announcePlayers = true;
	private boolean announcePlayerLogout = true;
	private int announceMaxPower = 0;

	@Override
	public void configure(Properties properties) {
		announcePlayers = Boolean.parseBoolean(properties.getProperty("announcePlayers", String.valueOf(announcePlayers)));
		announcePlayerLogout = Boolean.parseBoolean(properties.getProperty("announcePlayerLogout", String.valueOf(announcePlayerLogout)));
		announceMaxPower = Integer.parseInt(properties.getProperty("announceMaxPower", String.valueOf(announceMaxPower)));

		final Logger logger = Logger.getLogger(AnnounceMod.class.getName());
		logger.log(Level.INFO, "announcePlayers: " + announcePlayers);
		logger.log(Level.INFO, "announcePlayerLogout: " + announcePlayerLogout);
		logger.log(Level.INFO, "announceMaxPower: " + announceMaxPower);
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
	}

	@Override
	public void onPlayerLogin(Player player) {
		if (announcePlayers && player.getPower() <= announceMaxPower) {
			ServerMessaging.broadcastEvent("Player " + player.getName() + " has logged in.");
		}
	}

	public void onPlayerLogout(Player player) {
		if (this.announcePlayerLogout && player.getPower() <= this.announceMaxPower) {
			ServerMessaging.broadcastEvent("Player " + player.getName() + " has logged out.");
		}
	}

}
