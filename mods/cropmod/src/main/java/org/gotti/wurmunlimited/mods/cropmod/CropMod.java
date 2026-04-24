package org.gotti.wurmunlimited.mods.cropmod;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.api.events.farming.CropGrowthEvent;
import com.garward.wurmmodloader.api.events.farming.CropHarvestEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

public class CropMod implements WurmServerMod, Configurable, Initable, PreInitable {

	private boolean disableWeeds = true;
	private int extraHarvest = 0;
	private static final Logger logger = Logger.getLogger(CropMod.class.getName());

	@Override
	public void configure(Properties properties) {
		disableWeeds = Boolean.parseBoolean(properties.getProperty("disableWeeds", Boolean.toString(disableWeeds)));
		extraHarvest = Integer.parseInt(properties.getProperty("extraHarvest", Integer.toString(extraHarvest)));
		logger.log(Level.INFO, "disableWeeds: " + disableWeeds);
		logger.log(Level.INFO, "extraHarvest: " + extraHarvest);
	}

	@Override
	public void preInit() {
		// No bytecode hooks needed - framework handles it via CropHarvestPatch and CropGrowthPatch
	}

	@Override
	public void init() {
		// No legacy hook registration needed
	}

	/**
	 * Add extra harvest quantity bonus.
	 * Fires when crops are harvested and quantity is calculated.
	 */
	@SubscribeEvent
	public void onCropHarvest(CropHarvestEvent event) {
		if (extraHarvest > 0) {
			event.addQuantity(extraHarvest);
		}
	}

	/**
	 * Cancel growth checks for weeds (age 6) to disable weed growth.
	 * Fires when crop tiles are checked for growth.
	 */
	@SubscribeEvent
	public void onCropGrowth(CropGrowthEvent event) {
		if (disableWeeds && event.isWeeds()) {
			// Cancel growth check for weeds
			event.cancel();
		}
	}
}
