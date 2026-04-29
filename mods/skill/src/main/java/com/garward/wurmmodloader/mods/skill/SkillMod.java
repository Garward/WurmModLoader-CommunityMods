package com.garward.wurmmodloader.mods.skill;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.api.events.skill.SkillGainMultiplierEvent;
import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.wurmonline.server.Server;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkillMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(SkillMod.class.getName());

    boolean enableHybridSkillGain = true;
    float hybridNegativeDecayRate = 5f;
    float hybridPositiveDecayRate = 3f;
    float hybridValueAtZero = 3.74f;
    float hybridValueAtOneHundred = 0.9f;

    boolean changePreachingLocation = true;

    final Map<Integer, String> skillName = new HashMap<>();
    final Map<Integer, Float> skillDifficulty = new HashMap<>();
    final Map<Integer, Long> skillTickTime = new HashMap<>();

    private final List<String[]> rawSkillName = new ArrayList<>();
    private final List<String[]> rawSkillDifficulty = new ArrayList<>();
    private final List<String[]> rawSkillTickTime = new ArrayList<>();

    @Override
    public void configure(Properties properties) {
        enableHybridSkillGain = bool(properties, "enableHybridSkillGain", enableHybridSkillGain);
        hybridNegativeDecayRate = floatProp(properties, "hybridNegativeDecayRate", hybridNegativeDecayRate);
        hybridPositiveDecayRate = floatProp(properties, "hybridPositiveDecayRate", hybridPositiveDecayRate);
        hybridValueAtZero = floatProp(properties, "hybridValueAtZero", hybridValueAtZero);
        hybridValueAtOneHundred = floatProp(properties, "hybridValueAtOneHundred", hybridValueAtOneHundred);
        changePreachingLocation = bool(properties, "changePreachingLocation", changePreachingLocation);

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value == null) continue;
            String[] parts = value.split(",", 2);
            if (parts.length != 2) {
                logger.warning("[skill] " + key + ": expected '<skill>,<value>', got: " + value);
                continue;
            }
            String[] raw = new String[] { parts[0].trim(), parts[1].trim() };
            if (key.startsWith("skillName")) {
                rawSkillName.add(raw);
            } else if (key.startsWith("skillDifficulty")) {
                rawSkillDifficulty.add(raw);
            } else if (key.startsWith("skillTickTime")) {
                rawSkillTickTime.add(raw);
            }
        }

        logger.info("[skill] hybrid=" + enableHybridSkillGain
                + " (-decay=" + hybridNegativeDecayRate
                + " +decay=" + hybridPositiveDecayRate
                + " v0=" + hybridValueAtZero
                + " v100=" + hybridValueAtOneHundred + ")"
                + " renames=" + rawSkillName.size()
                + " difficulties=" + rawSkillDifficulty.size()
                + " tickTimes=" + rawSkillTickTime.size()
                + " changePreachingLocation=" + changePreachingLocation);
    }

    @SubscribeEvent
    public void onSkillGainMultiplier(SkillGainMultiplierEvent event) {
        if (!enableHybridSkillGain) return;

        Skill skill = event.getSkill();
        double power = event.getPower();
        double knowledge = skill.getKnowledge();

        // Reproduces the Wyvern hybrid curve while reusing the framework's
        // pre-computed vanilla bonus so the "closeness to check" weighting
        // stays consistent with vanilla.
        double bonus = event.getVanillaBonus();
        double vanillaBase = event.getVanillaMultiplier() / bonus; // strip bonus to get the base

        double mult = hybridValueAtOneHundred * Math.pow(
                hybridValueAtZero / hybridValueAtOneHundred,
                (2.0 - Math.pow(100.0 / (100.0 + Math.max(-99.0, power)), hybridNegativeDecayRate))
                        * Math.pow((100.0 - power) * 0.01, hybridPositiveDecayRate)
        );

        double advance = vanillaBase * bonus;
        if (mult < 0.5 && knowledge < 20.0) {
            advance *= 0.5 + (Server.rand.nextDouble() * 0.5);
        } else if (skill.getNumber() == SkillList.MEDITATING
                || skill.getNumber() == SkillList.LOCKPICKING) {
            advance *= Math.max(mult, 0.8);
        } else if (mult > 0.0001) {
            advance *= mult;
        } else {
            advance = 0.0;
        }

        event.setMultiplier(advance);
        // Hybrid mode always advances on a successful skill check, even when
        // power<0 && knowledge>=20 (where vanilla would skip).
        event.setShouldAdvance(true);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        for (String[] raw : rawSkillName) {
            int id = resolveSkillId(raw[0], "skillName");
            if (id < 0) continue;
            if (skillName.containsKey(id)) {
                logger.warning("[skill] duplicate skillName for id " + id);
            } else {
                skillName.put(id, raw[1]);
            }
        }
        for (String[] raw : rawSkillDifficulty) {
            int id = resolveSkillId(raw[0], "skillDifficulty");
            if (id < 0) continue;
            try {
                if (skillDifficulty.containsKey(id)) {
                    logger.warning("[skill] duplicate skillDifficulty for id " + id);
                } else {
                    skillDifficulty.put(id, Float.parseFloat(raw[1]));
                }
            } catch (NumberFormatException nfe) {
                logger.warning("[skill] skillDifficulty: bad float '" + raw[1] + "' for id " + id);
            }
        }
        for (String[] raw : rawSkillTickTime) {
            int id = resolveSkillId(raw[0], "skillTickTime");
            if (id < 0) continue;
            try {
                if (skillTickTime.containsKey(id)) {
                    logger.warning("[skill] duplicate skillTickTime for id " + id);
                } else {
                    skillTickTime.put(id, Long.parseLong(raw[1]));
                }
            } catch (NumberFormatException nfe) {
                logger.warning("[skill] skillTickTime: bad long '" + raw[1] + "' for id " + id);
            }
        }

        for (Map.Entry<Integer, String> e : skillName.entrySet()) {
            renameSkill(e.getKey(), e.getValue());
        }
        for (Map.Entry<Integer, Float> e : skillDifficulty.entrySet()) {
            SkillTemplate t = SkillSystem.templates.get(e.getKey());
            if (t == null) {
                logger.warning("[skill] difficulty: no template for id " + e.getKey());
                continue;
            }
            t.setDifficulty(e.getValue());
        }
        for (Map.Entry<Integer, Long> e : skillTickTime.entrySet()) {
            setTickTime(e.getKey(), e.getValue());
        }

        if (changePreachingLocation) {
            SkillTemplate preaching = SkillSystem.templates.get(SkillList.PREACHING);
            if (preaching != null) {
                try {
                    ReflectionUtil.setPrivateField(preaching,
                            ReflectionUtil.getField(preaching.getClass(), "dependencies"),
                            new int[] { SkillList.MASONRY });
                } catch (IllegalAccessException | NoSuchFieldException ex) {
                    logger.log(Level.WARNING, "[skill] failed to re-parent Preaching", ex);
                }
            }
        }
    }

    private static void renameSkill(int id, String newName) {
        SkillTemplate t = SkillSystem.templates.get(id);
        if (t == null) {
            logger.warning("[skill] rename: no template for id " + id);
            return;
        }
        try {
            ReflectionUtil.setPrivateField(t,
                    ReflectionUtil.getField(t.getClass(), "name"), newName);
            SkillSystem.skillNames.put(t.getNumber(), newName);
            SkillSystem.namesToSkill.put(newName.toLowerCase(), t.getNumber());
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.log(Level.WARNING, "[skill] failed to rename id " + id, ex);
        }
    }

    private static void setTickTime(int id, long tickTime) {
        SkillTemplate t = SkillSystem.templates.get(id);
        if (t == null) {
            logger.warning("[skill] tickTime: no template for id " + id);
            return;
        }
        try {
            ReflectionUtil.setPrivateField(t,
                    ReflectionUtil.getField(t.getClass(), "tickTime"), tickTime);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            logger.log(Level.WARNING, "[skill] failed to set tickTime for id " + id, ex);
        }
    }

    private static int resolveSkillId(String token, String label) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException nfe) {
            int id = SkillSystem.getSkillByName(token);
            if (id < 0) {
                logger.warning("[skill] " + label + ": unknown skill '" + token + "'");
            }
            return id;
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static float floatProp(Properties p, String key, float def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Float.parseFloat(v.trim());
        } catch (NumberFormatException nfe) {
            logger.warning("[skill] bad float for " + key + "=" + v + ", using " + def);
            return def;
        }
    }
}
