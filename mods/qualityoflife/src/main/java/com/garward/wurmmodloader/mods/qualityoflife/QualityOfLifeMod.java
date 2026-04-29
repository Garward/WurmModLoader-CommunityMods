package com.garward.wurmmodloader.mods.qualityoflife;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

public class QualityOfLifeMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(QualityOfLifeMod.class.getName());

    boolean mineCaveToVehicle = true;
    boolean mineSurfaceToVehicle = true;
    boolean chopLogsToVehicle = true;
    boolean statuetteAnyMaterial = true;
    boolean mineGemsToVehicle = true;
    boolean regenerateStaminaOnVehicleAnySlope = true;

    @Override
    public void configure(Properties properties) {
        mineCaveToVehicle = bool(properties, "mineCaveToVehicle", mineCaveToVehicle);
        mineSurfaceToVehicle = bool(properties, "mineSurfaceToVehicle", mineSurfaceToVehicle);
        chopLogsToVehicle = bool(properties, "chopLogsToVehicle", chopLogsToVehicle);
        statuetteAnyMaterial = bool(properties, "statuetteAnyMaterial", statuetteAnyMaterial);
        mineGemsToVehicle = bool(properties, "mineGemsToVehicle", mineGemsToVehicle);
        regenerateStaminaOnVehicleAnySlope = bool(properties, "regenerateStaminaOnVehicleAnySlope",
                regenerateStaminaOnVehicleAnySlope);
    }

    @Override
    public void preInit() {
        QualityOfLifePatches.install(this);
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
