package com.garward.wurmmodloader.mods.extraactions;

import com.garward.wurmmodloader.modsupport.actions.ActionPerformer;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CreatureReportPerformer implements ActionPerformer {

    @Override
    public short getActionId() {
        return (short) ExtraActionsMod.creatureReportId;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short num, float counter) {
        if (num != getActionId()) return false;
        if (!(performer instanceof Player)) return true;
        if (performer.getPower() < 5) {
            performer.getCommunicator().sendNormalServerMessage(
                    "You do not have permission to do that.");
            return true;
        }
        report(performer.getCommunicator());
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short num, float counter) {
        return action(act, performer, target, num, counter);
    }

    private static void report(Communicator comm) {
        HashMap<CreatureTemplate, Integer> counts = new HashMap<>();
        Arrays.stream(CreatureTemplateFactory.getInstance().getTemplates())
                .forEach(ct -> counts.put(ct, 0));
        Arrays.stream(Creatures.getInstance().getCreatures())
                .forEach(cr -> counts.put(cr.getTemplate(), counts.get(cr.getTemplate()) + 1));

        Map<Boolean, List<Map.Entry<CreatureTemplate, Integer>>> tmp = counts.entrySet().stream()
                .collect(Collectors.partitioningBy(e -> e.getKey().isMonster() || e.getKey().isAggHuman()));
        List<Map.Entry<CreatureTemplate, Integer>> aggro = tmp.get(true);
        List<Map.Entry<CreatureTemplate, Integer>> passive = tmp.get(false);
        aggro.sort((a, b) -> -Integer.compare(a.getValue(), b.getValue()));
        passive.sort((a, b) -> -Integer.compare(a.getValue(), b.getValue()));
        int aggroTotal = aggro.stream().mapToInt(Map.Entry::getValue).sum();
        int passiveTotal = passive.stream().mapToInt(Map.Entry::getValue).sum();
        int sum = aggroTotal + passiveTotal;
        if (sum == 0) {
            comm.sendSystemMessage("No creatures present.");
            return;
        }
        comm.sendSystemMessage(String.format(
                "Passive: %d (%.1f) Aggro: %d (%.1f) Total: %d",
                passiveTotal, 100.0 * passiveTotal / sum,
                aggroTotal,   100.0 * aggroTotal   / sum, sum));
        comm.sendSystemMessage("== Aggressive ==");
        if (aggroTotal > 0) {
            aggro.stream().filter(e -> e.getValue() > 0).forEach(e ->
                    comm.sendSystemMessage(String.format("%s: %d (%.1f)",
                            e.getKey().getName(), e.getValue(),
                            100.0 * e.getValue() / aggroTotal)));
        }
        comm.sendSystemMessage("== Passive ==");
        if (passiveTotal > 0) {
            passive.stream().filter(e -> e.getValue() > 0).forEach(e ->
                    comm.sendSystemMessage(String.format("%s: %d (%.1f)",
                            e.getKey().getName(), e.getValue(),
                            100.0 * e.getValue() / passiveTotal)));
        }
        comm.sendSystemMessage("=============");
    }
}
