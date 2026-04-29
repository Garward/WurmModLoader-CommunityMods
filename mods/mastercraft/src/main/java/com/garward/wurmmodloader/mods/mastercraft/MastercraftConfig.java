package com.garward.wurmmodloader.mods.mastercraft;

import java.util.Properties;
import java.util.logging.Logger;

final class MastercraftConfig {

    private static final Logger logger = Logger.getLogger(MastercraftConfig.class.getName());

    boolean enabled = true;

    boolean affinityDifficultyBonus = true;
    boolean legendDifficultyBonus = true;
    boolean masterDifficultyBonus = true;
    boolean itemRarityDifficultyBonus = true;
    boolean legendItemDifficultyBonus = true;
    boolean masterItemDifficultyBonus = true;

    boolean empoweredChannelers = true;
    boolean channelSkillFavorReduction = true;

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);

        affinityDifficultyBonus    = bool(p, "affinityDifficultyBonus", affinityDifficultyBonus);
        legendDifficultyBonus      = bool(p, "legendDifficultyBonus", legendDifficultyBonus);
        masterDifficultyBonus      = bool(p, "masterDifficultyBonus", masterDifficultyBonus);
        itemRarityDifficultyBonus  = bool(p, "itemRarityDifficultyBonus", itemRarityDifficultyBonus);
        legendItemDifficultyBonus  = bool(p, "legendItemDifficultyBonus", legendItemDifficultyBonus);
        masterItemDifficultyBonus  = bool(p, "masterItemDifficultyBonus", masterItemDifficultyBonus);

        empoweredChannelers        = bool(p, "empoweredChannelers", empoweredChannelers);
        channelSkillFavorReduction = bool(p, "channelSkillFavorReduction", channelSkillFavorReduction);

        logger.info("[mastercraft] config loaded — difficulty(affinity=" + affinityDifficultyBonus
                + ",legend=" + legendDifficultyBonus + ",master=" + masterDifficultyBonus
                + ",rarity=" + itemRarityDifficultyBonus + ",legendItem=" + legendItemDifficultyBonus
                + ",masterItem=" + masterItemDifficultyBonus + ") spells(power="
                + empoweredChannelers + ",favor=" + channelSkillFavorReduction + ")");
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
