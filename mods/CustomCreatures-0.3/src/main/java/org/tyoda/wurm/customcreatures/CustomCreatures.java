package org.tyoda.wurm.customcreatures;

import javassist.*;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreatures;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Logger;

public class CustomCreatures implements WurmServerMod, Versioned, Configurable, Initable, PreInitable {
    public static final Logger logger = Logger.getLogger(CustomCreatures.class.getName());
    public static final String version = "0.3";

    public static final String delimiter = ";";

    private static CustomCreatures instance;

    private boolean dotCreatureOnly = true;
    private boolean logAddedCreatureProperties = false;

    public CustomCreatures() {
        instance = this;
    }

    @Override
    public void configure(Properties p){
        dotCreatureOnly = Boolean.parseBoolean(p.getProperty("dotCreatureOnly", Boolean.toString(dotCreatureOnly)));
        logAddedCreatureProperties = Boolean.parseBoolean(p.getProperty("logAddedCreatureProperties", Boolean.toString(logAddedCreatureProperties)));
    }

    @Override
    public void preInit() {
        try {
            ModCreatures.init();
            ModVehicleBehaviours.init();
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctCreatureTemplate = classPool.get("com.wurmonline.server.creatures.CreatureTemplate");
            // remove all non-static final modifiers
            logger.info("Removing final modifiers.");
            for (CtField field : ctCreatureTemplate.getFields()) {
                if(Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                    field.setModifiers(Modifier.clear(field.getModifiers(), Modifier.FINAL));
                }
            }
        } catch (NotFoundException e){
            throw new HookException(e);
        }
    }

    @Override
    public void init(){
        String creaturesDirPath = "mods/CustomCreatures/creatures";
        if(!Files.isDirectory(Paths.get(creaturesDirPath))) {
            return;
        }

        File creaturesDir = new File(creaturesDirPath);
        File[] files = creaturesDir.listFiles();
        if(files == null) {
            throw new RuntimeException("Failed to get files from creatures directory.");
        }
        try {
            for (final File file : files) {
                if(dotCreatureOnly && !file.getName().endsWith(".creature")) {
                    logger.info("Skipping file because it does not end in '.creature': "+file.getName());
                    continue;
                }
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    Properties properties = new Properties();
                    properties.load(inputStream);
                    if(Boolean.parseBoolean(properties.getProperty("disabled", Boolean.toString(false)))) {
                        logger.info("Skipping file because it is disabled: "+file.getName());
                        continue;
                    }
                    byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(file.getName().getBytes(StandardCharsets.UTF_8));
                    String md5Hash = new BigInteger(1, md5Bytes).toString(16).substring(0, 7);
                    String uniqueId = file.getName().replaceAll("[^A-Za-z0-9]", "") + "." +  md5Hash + ".";
                    logger.info("Adding new creature from file " + file.getName() + " as " + uniqueId);
                    ModCreatures.addCreature(new CustomCreature(properties, uniqueId));
                }
            }
        } catch (NoSuchAlgorithmException ignore) {}
          catch (IOException e) {
            throw new RuntimeException("An IO exception occurred.", e);
        }
    }

    public boolean isLogAddedCreatureProperties() {
        return logAddedCreatureProperties;
    }

    public static CustomCreatures getInstance() {
        return instance;
    }

    @Override
    public String getVersion(){
        return version;
    }

    // TODO: Friya's loottable
    // TODO: color on non-normal mobs
}
