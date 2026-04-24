package org.gotti.wurmunlimited.mods.harvesthelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.garward.wurmmodloader.api.events.action.ActionFatigueEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PlayerLoginListener;
import com.garward.wurmmodloader.modloader.interfaces.PlayerMessageListener;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmHarvestables.Harvestable;
import com.wurmonline.server.behaviours.Actions;  // Type import for action ID comparison
import com.wurmonline.server.creatures.Communicator;  // Type import for listener interface (acceptable per architecture rules)
import com.wurmonline.server.players.Player;  // Type import for listener interface (acceptable per architecture rules)

public class HarvestHelperMod implements WurmServerMod, Configurable, Initable, PreInitable, PlayerLoginListener, PlayerMessageListener {

	private static final Logger logger = Logger.getLogger(HarvestHelperMod.class.getName());

	private boolean allowMountedHarvest = true;
	private boolean enableSeasonsCommand = true;
	private boolean enableSeasonsMotd = true;

	@Override
	public void configure(Properties properties) {
		this.allowMountedHarvest = Boolean.parseBoolean(properties.getProperty("allowMountedHarvest", Boolean.toString(this.allowMountedHarvest)));
		this.enableSeasonsCommand = Boolean.parseBoolean(properties.getProperty("enableSeasonsCommand", Boolean.toString(this.enableSeasonsCommand)));
		this.enableSeasonsMotd = Boolean.parseBoolean(properties.getProperty("enableSeasonsMotd", Boolean.toString(this.enableSeasonsMotd)));

		logger.log(Level.INFO, "allowMountedHarvest: " + allowMountedHarvest);
		logger.log(Level.INFO, "enableSeasonsCommand: " + enableSeasonsCommand);
		logger.log(Level.INFO, "enableSeasonsMotd: " + enableSeasonsMotd);
	}

	@Override
	public void preInit() {
		// No bytecode hooks needed - framework handles it via ActionFatiguePatch
	}

	@Override
	public void init() {
	}

	/**
	 * Override fatigue for harvest actions when mounted.
	 * Fires during Action constructor when fatigue is being determined.
	 */
	@SubscribeEvent
	public void onActionFatigue(ActionFatigueEvent event) {
		if (!allowMountedHarvest) {
			return;
		}

		// If this is a harvest action and performer is mounted, disable fatigue
		if (event.getAction() == Actions.HARVEST && event.isPerformerMounted()) {
			event.setFatigue(false);
		}
	}

	private List<Harvestable> getSortedHarvestables() {
		final Comparator<Harvestable> comparator = Comparator.comparing(Harvestable::getSeasonStart);
		return Arrays.stream(Harvestable.values()).filter(harvestable -> harvestable != Harvestable.NONE).sorted(comparator.reversed()).collect(Collectors.toList());
	}

	@Override
	public void onPlayerLogin(Player player) {
		if (enableSeasonsMotd && player != null) {
			long now = WurmCalendar.currentTime;
			for (Harvestable harvestable : getSortedHarvestables()) {
				long start = harvestable.getSeasonStart();
				if (now >= start) {
					player.getCommunicator().sendNormalServerMessage(String.format("%s is in season", capitalize(harvestable.getName())));
				} else if (now >= start - 345600L) {
					player.getCommunicator().sendNormalServerMessage(String.format("%s will soon be in season", capitalize(harvestable.getName())));
				}
			}
		}
	}

	@Override
	public boolean onPlayerMessage(Communicator communicator, String message) {
		if (enableSeasonsCommand && message != null && message.startsWith("/seasons")) {
			long now = WurmCalendar.currentTime;
			for (Harvestable harvestable : getSortedHarvestables()) {
				long start = harvestable.getSeasonStart();
				if (now >= start) {
					communicator.sendNormalServerMessage(String.format("%s is in season", capitalize(harvestable.getName())));
				} else {
					long duration = start - now;
					communicator.sendNormalServerMessage(String.format("%s will be in season in %s", capitalize(harvestable.getName()), Server.getTimeFor(duration * 1000 / 8)));
				}
			}
			return true;
		}
		return false;
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
}
