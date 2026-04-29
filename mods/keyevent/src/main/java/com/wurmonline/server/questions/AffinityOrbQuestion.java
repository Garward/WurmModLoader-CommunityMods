package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.SkillSystem;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Lives in {@code com.wurmonline.server.questions} so it can extend the
 * package-private {@link Question} machinery. Direct port of Sindusk's
 * AffinityOrbQuestion -- 10 random skill names seeded by the orb's
 * auxData byte; selecting one consumes the orb and grants an affinity in
 * that skill (capped at level 5).
 */
public class AffinityOrbQuestion extends Question {

    private static final Logger LOGGER = Logger.getLogger(AffinityOrbQuestion.class.getName());

    private final Item affinityOrb;
    private final HashMap<Integer, Integer> affinityMap = new HashMap<>();

    public AffinityOrbQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, Item orb) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.affinityOrb = orb;
    }

    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("accept") && "true".equals(answer.get("accept"));
        if (!accepted) return;

        int entry;
        try {
            entry = Integer.parseInt(answer.getProperty("affinity"));
        } catch (NumberFormatException e) {
            return;
        }
        Integer skillNum = affinityMap.get(entry);
        if (skillNum == null) return;

        if (affinityOrb == null || affinityOrb.getOwnerId() != this.getResponder().getWurmId()) {
            this.getResponder().getCommunicator().sendNormalServerMessage(
                    "You must own an affinity orb to obtain an affinity.");
            return;
        }
        if (!(this.getResponder() instanceof Player)) {
            LOGGER.info("[keyevent] non-player answered AffinityOrbQuestion");
            return;
        }
        Player player = (Player) this.getResponder();
        String skillName = SkillSystem.getNameFor(skillNum);

        Affinity[] affs = Affinities.getAffinities(player.getWurmId());
        for (Affinity affinity : affs) {
            if (affinity.getSkillNumber() != skillNum) continue;
            if (affinity.getNumber() >= 5) {
                player.getCommunicator().sendSafeServerMessage(
                        "You already have the maximum amount of affinities for " + skillName);
                return;
            }
            Affinities.setAffinity(player.getWurmId(), skillNum, affinity.getNumber() + 1, false);
            Items.destroyItem(affinityOrb.getWurmId());
            player.getCommunicator().sendSafeServerMessage(
                    "Vynora infuses you with an affinity for " + skillName + "!");
            return;
        }
        Affinities.setAffinity(player.getWurmId(), skillNum, 1, false);
        Items.destroyItem(affinityOrb.getWurmId());
        player.getCommunicator().sendSafeServerMessage(
                "Vynora infuses you with an affinity for " + skillName + "!");
    }

    private String getAffinities() {
        StringBuilder builder = new StringBuilder();
        Random rand = new Random();
        if (affinityOrb.getAuxData() == 0) {
            byte seed = (byte) ((1 + Server.rand.nextInt(120)) * (Server.rand.nextBoolean() ? 1 : -1));
            affinityOrb.setAuxData(seed);
        }
        rand.setSeed(affinityOrb.getAuxData());
        affinityMap.clear();
        int i = 0;
        while (i < 10) {
            int num = rand.nextInt(SkillSystem.getNumberOfSkillTemplates());
            if (!affinityMap.containsValue(num)) {
                builder.append(SkillSystem.getSkillTemplateByIndex(num).getName());
                affinityMap.put(i, SkillSystem.getSkillTemplateByIndex(num).getNumber());
                i++;
                if (i < 10) builder.append(",");
            }
        }
        return builder.toString();
    }

    @Override
    public void sendQuestion() {
        if (affinityOrb == null || affinityOrb.getOwnerId() != this.getResponder().getWurmId()) {
            this.getResponder().getCommunicator().sendNormalServerMessage(
                    "You must own an affinity orb before being infused.");
            return;
        }
        StringBuilder b = new StringBuilder();
        b.append("border{center{text{type='bold';text=\"\"}};null;");
        b.append("scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";");
        b.append("passthrough{id=\"id\";text=\"").append(this.id).append("\"}");
        b.append("text{type='bold';text=\"Select the affinity you would like to obtain\\n\\n\"}");
        b.append("harray{label{text='Select Affinity:'}dropdown{id='affinity';options='");
        b.append(getAffinities());
        b.append("'}}");
        b.append("text{text=\"\\n\\n\"}");
        b.append("harray{button{text='Accept';id='accept'}}");
        b.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(400, 300, true, true, b.toString(),
                255, 255, 255, this.title);
    }
}
