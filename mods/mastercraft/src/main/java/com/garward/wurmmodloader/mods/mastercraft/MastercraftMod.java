package com.garward.wurmmodloader.mods.mastercraft;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.skill.SkillAdvanceEvent;
import com.garward.wurmmodloader.api.events.spell.SpellFavorCostEvent;
import com.garward.wurmmodloader.api.events.spell.SpellPowerEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Skill mastery + channeling mastery rewards. Three knobs:
 *
 * <ul>
 *   <li>{@link SkillAdvanceEvent} — reduce skill-check difficulty for high
 *       skill / high item rarity / high item QL.</li>
 *   <li>{@link SpellPowerEvent} — boost cast power based on caster's
 *       channeling skill + affinity.</li>
 *   <li>{@link SpellFavorCostEvent} — discount favor cost based on caster's
 *       channeling skill + affinity.</li>
 * </ul>
 *
 * Ported from Sindusk's WyvernMods {@code Mastercraft}; the upstream mod
 * shipped its own bytecode patches against {@code Skill.checkAdvance} and
 * {@code Spell.run}, but the framework already exposes those as events.
 */
public class MastercraftMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(MastercraftMod.class.getName());

    private final MastercraftConfig cfg = new MastercraftConfig();

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
    }

    @SubscribeEvent
    public void onSkillAdvance(SkillAdvanceEvent event) {
        if (!cfg.enabled) return;
        Skill skill = event.getSkill();
        Item item = event.getItem();
        double diff = event.getDifficulty();

        if (cfg.affinityDifficultyBonus && skill.affinity > 0) {
            diff -= skill.affinity;
        }
        double know = skill.getKnowledge();
        if (cfg.legendDifficultyBonus && know > 99.0d) {
            diff -= 2d - ((100d - know) * 2d);
        }
        if (cfg.masterDifficultyBonus && know > 90.0d) {
            diff -= 2d - ((100d - know) * 0.2d);
        }
        if (item != null) {
            if (cfg.itemRarityDifficultyBonus && item.getRarity() > 0) {
                diff -= item.getRarity();
            }
            float ql = item.getCurrentQualityLevel();
            if (cfg.legendItemDifficultyBonus && ql > 99.0f) {
                diff -= 1d - ((100d - ql) * 1d);
            }
            if (cfg.masterItemDifficultyBonus && ql > 90.0f) {
                diff -= 1d - ((100d - ql) * 0.1d);
            }
        }
        event.setDifficulty(diff);
    }

    @SubscribeEvent
    public void onSpellPower(SpellPowerEvent event) {
        if (!cfg.enabled || !cfg.empoweredChannelers) return;
        Skill channel = lookupChanneling(event.getCasterId(), event.getCasterName());
        if (channel == null) return;
        double bonus = 0d;
        if (channel.affinity > 0) {
            bonus += 2 * channel.affinity;
        }
        double know = channel.getKnowledge();
        if (know > 0) {
            float lowFloat1 = Math.min(Server.rand.nextFloat(), Server.rand.nextFloat());
            float lowFloat2 = Math.min(Server.rand.nextFloat(), Server.rand.nextFloat());
            bonus += Math.min(know * lowFloat1, know * lowFloat2);
        }
        if (bonus != 0d) {
            event.add(bonus);
        }
    }

    @SubscribeEvent
    public void onSpellFavorCost(SpellFavorCostEvent event) {
        if (!cfg.enabled || !cfg.channelSkillFavorReduction) return;
        Skill channel = lookupChanneling(event.getCasterId(), event.getCasterName());
        if (channel == null) return;
        double mult = 1d;
        if (channel.affinity > 0) {
            mult -= channel.affinity * 0.02d;
        }
        double know = channel.getKnowledge();
        if (know > 90d) {
            mult -= 0.1d - ((100d - know) * 0.01d);
        }
        if (know > 99d) {
            mult -= 0.1d - ((100d - know) * 0.1d);
        }
        if (mult < 1d) {
            event.multiplyCost(mult);
        }
    }

    private Skill lookupChanneling(long casterId, String casterName) {
        Creature caster = null;
        if (casterId > 0) {
            try {
                caster = Server.getInstance().getCreature(casterId);
            } catch (Exception ignored) {
                // fall through to player lookup
            }
        }
        if (caster == null && casterName != null && !casterName.isEmpty()) {
            try {
                Player p = Players.getInstance().getPlayer(casterName);
                if (p != null) caster = p;
            } catch (NoSuchPlayerException ignored) {
                return null;
            }
        }
        if (caster == null) return null;
        try {
            return caster.getSkills().getSkillOrLearn(SkillList.CHANNELING);
        } catch (Exception e) {
            return null;
        }
    }
}
