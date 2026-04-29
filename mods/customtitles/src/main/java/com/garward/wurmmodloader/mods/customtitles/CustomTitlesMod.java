package com.garward.wurmmodloader.mods.customtitles;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.player.PlayerLoginEvent;
import com.garward.wurmmodloader.api.titles.TitleRegistry;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomTitlesMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(CustomTitlesMod.class.getName());

    private final List<TitleEntry> definitions = new ArrayList<>();
    private final Map<Integer, List<String>> awards = new HashMap<>();

    @Override
    public void configure(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value == null || value.isEmpty()) continue;
            if (key.startsWith("addCustomTitle")) {
                parseAddCustomTitle(value);
            } else if (key.startsWith("awardTitle")) {
                parseAwardTitle(value);
            }
        }
    }

    @Override
    public void preInit() {
        for (TitleEntry def : definitions) {
            try {
                TitleRegistry.addTitle(def.id, def.male, def.female, def.skillId, def.type);
            } catch (Exception e) {
                logger.log(Level.WARNING, "[customtitles] failed to register title id=" + def.id, e);
            }
        }
        if (!definitions.isEmpty()) {
            logger.info("[customtitles] queued " + definitions.size() + " custom title(s) for injection");
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (awards.isEmpty()) return;
        Object raw = event.getPlayer();
        if (!(raw instanceof Player)) return;
        Player player = (Player) raw;
        String name = player.getName();
        for (Map.Entry<Integer, List<String>> entry : awards.entrySet()) {
            int titleId = entry.getKey();
            if (!entry.getValue().contains(name)) continue;
            try {
                Titles.Title title = Titles.Title.getTitle(titleId);
                if (title == null) {
                    logger.warning("[customtitles] unknown title id " + titleId + " in award list");
                    continue;
                }
                player.addTitle(title);
            } catch (Exception e) {
                logger.log(Level.WARNING, "[customtitles] failed to award title id=" + titleId
                    + " to " + name, e);
            }
        }
    }

    private void parseAddCustomTitle(String value) {
        String[] parts = value.split(",");
        if (parts.length != 5) {
            logger.warning("[customtitles] addCustomTitle expects 5 fields (id,male,female,skillId,type), got: " + value);
            return;
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            String male = parts[1].trim();
            String female = parts[2].trim();
            int skillId = Integer.parseInt(parts[3].trim());
            String type = parts[4].trim();
            definitions.add(new TitleEntry(id, male, female, skillId, type));
        } catch (NumberFormatException e) {
            logger.warning("[customtitles] addCustomTitle has non-numeric id/skill: " + value);
        }
    }

    private void parseAwardTitle(String value) {
        String[] parts = value.split(",");
        if (parts.length < 2) {
            logger.warning("[customtitles] awardTitle expects at least 2 fields (id,player...), got: " + value);
            return;
        }
        int titleId;
        try {
            titleId = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            logger.warning("[customtitles] awardTitle has non-numeric id: " + value);
            return;
        }
        List<String> playerList = awards.computeIfAbsent(titleId, k -> new ArrayList<>());
        for (int i = 1; i < parts.length; i++) {
            String name = parts[i].trim();
            if (name.isEmpty()) continue;
            if (playerList.contains(name)) {
                logger.warning("[customtitles] duplicate player " + name + " for title " + titleId);
            } else {
                playerList.add(name);
            }
        }
    }

    private static final class TitleEntry {
        final int id;
        final String male;
        final String female;
        final int skillId;
        final String type;

        TitleEntry(int id, String male, String female, int skillId, String type) {
            this.id = id;
            this.male = male;
            this.female = female;
            this.skillId = skillId;
            this.type = type;
        }
    }
}
