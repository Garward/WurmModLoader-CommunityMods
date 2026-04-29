package com.garward.wurmmodloader.mods.customdeity;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomDeityMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(CustomDeityMod.class.getName());

    private static final Pattern KEY_PATTERN = Pattern.compile("deity-(\\d+)\\.(.+)");

    /** deityId -> (flagName -> rawValue), preserves insertion order for predictable apply. */
    private final Map<Integer, Map<String, String>> overrides = new LinkedHashMap<>();

    @Override
    public void configure(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            Matcher m = KEY_PATTERN.matcher(key);
            if (!m.matches()) continue;

            int id;
            try {
                id = Integer.parseInt(m.group(1));
            } catch (NumberFormatException nfe) {
                logger.warning("[customdeity] bad deity id in key: " + key);
                continue;
            }
            String flag = m.group(2).trim();
            String value = properties.getProperty(key);
            if (value == null) continue;

            overrides.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(flag, value.trim());
        }

        int total = overrides.values().stream().mapToInt(Map::size).sum();
        logger.info("[customdeity] queued " + total + " override(s) across " + overrides.size() + " deity id(s)");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        for (Map.Entry<Integer, Map<String, String>> e : overrides.entrySet()) {
            int id = e.getKey();
            Deity deity = Deities.getDeity(id);
            if (deity == null) {
                logger.warning("[customdeity] deity id " + id + " not loaded — skipping "
                        + e.getValue().size() + " override(s)");
                continue;
            }
            for (Map.Entry<String, String> ov : e.getValue().entrySet()) {
                applyOverride(deity, ov.getKey(), ov.getValue());
            }
        }
    }

    private static void applyOverride(Deity deity, String flag, String rawValue) {
        String setter = "set" + Character.toUpperCase(flag.charAt(0)) + flag.substring(1);

        // Resolve setter by name; pick the first matching arity-1 setter.
        Method method = null;
        for (Method candidate : Deity.class.getMethods()) {
            if (candidate.getName().equals(setter) && candidate.getParameterCount() == 1) {
                method = candidate;
                break;
            }
        }
        if (method == null) {
            logger.warning("[customdeity] unknown deity flag '" + flag + "' (no Deity." + setter + "(...))");
            return;
        }

        Class<?> paramType = method.getParameterTypes()[0];
        Object converted;
        try {
            converted = convert(paramType, rawValue, flag);
        } catch (IllegalArgumentException iae) {
            logger.warning("[customdeity] " + flag + ": " + iae.getMessage());
            return;
        }

        try {
            method.invoke(deity, converted);
            logger.info("[customdeity] deity " + deity.getNumber() + " (" + deity.getName()
                    + ") set " + flag + "=" + rawValue);
        } catch (ReflectiveOperationException roe) {
            logger.log(Level.WARNING, "[customdeity] failed to apply " + flag + "=" + rawValue
                    + " on deity " + deity.getNumber(), roe);
        }
    }

    private static final Map<String, Integer> TEMPLATE_NAMES = new HashMap<>();
    static {
        TEMPLATE_NAMES.put("FO", Deities.DEITY_FO);
        TEMPLATE_NAMES.put("MAGRANON", Deities.DEITY_MAGRANON);
        TEMPLATE_NAMES.put("VYNORA", Deities.DEITY_VYNORA);
        TEMPLATE_NAMES.put("LIBILA", Deities.DEITY_LIBILA);
    }

    private static Object convert(Class<?> paramType, String raw, String flag) {
        if (paramType == boolean.class || paramType == Boolean.class) {
            String v = raw.toLowerCase();
            if (v.equals("true") || v.equals("false")) {
                return Boolean.parseBoolean(v);
            }
            throw new IllegalArgumentException("expected true/false, got '" + raw + "'");
        }
        if (paramType == int.class || paramType == Integer.class) {
            // templateDeity accepts a name alias.
            if (flag.equals("templateDeity")) {
                Integer named = TEMPLATE_NAMES.get(raw.toUpperCase());
                if (named != null) return named;
            }
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("expected integer, got '" + raw + "'");
            }
        }
        if (paramType == byte.class || paramType == Byte.class) {
            try {
                return Byte.parseByte(raw);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("expected byte (-128..127), got '" + raw + "'");
            }
        }
        if (paramType == float.class || paramType == Float.class) {
            try {
                return Float.parseFloat(raw);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("expected float, got '" + raw + "'");
            }
        }
        if (paramType == double.class || paramType == Double.class) {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("expected double, got '" + raw + "'");
            }
        }
        if (paramType == String.class) {
            return raw;
        }
        throw new IllegalArgumentException("unsupported setter parameter type: " + paramType.getName());
    }
}
