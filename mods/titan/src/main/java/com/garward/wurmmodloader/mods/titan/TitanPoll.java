package com.garward.wurmmodloader.mods.titan;

import com.garward.wurmmodloader.modsupport.spawn.SpawnFilter;
import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.zones.Zones;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TitanPoll {

    private static final Logger logger = Logger.getLogger(TitanPoll.class.getName());
    private static final int CHAT_R = 255, CHAT_G = 105, CHAT_B = 180;

    static void tick(TitanConfig cfg) {
        try {
            ingestNewTitans();
            removeDeadTitans(cfg);
            if (TitanState.titans.isEmpty()) {
                maybeSpawnNewTitan(cfg);
            } else {
                regenerateTitans();
                for (Creature titan : TitanState.titans) {
                    pollSingleTitan(titan);
                }
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[titan] poll tick failed", t);
        }
    }

    private static void ingestNewTitans() {
        Creature[] crets = Creatures.getInstance().getCreatures();
        for (Creature c : crets) {
            if (TitanState.isTitan(c) && !TitanState.titans.contains(c)) {
                TitanState.titans.add(c);
                logger.info("[titan] existing titan identified (" + c.getName() + ")");
            }
        }
    }

    private static void removeDeadTitans(TitanConfig cfg) {
        Iterator<Creature> it = TitanState.titans.iterator();
        while (it.hasNext()) {
            Creature t = it.next();
            if (t.isDead()) {
                logger.info("[titan] titan was found dead (" + t.getName() + ")");
                try {
                    TitanLoot.addTitanLoot(t, cfg);
                } catch (Throwable err) {
                    logger.log(Level.WARNING, "[titan] loot drop failed for " + t.getName(), err);
                }
                it.remove();
                TitanState.titanDamage.remove(t);
                TitanState.titanAdvancedTimed.remove(t.getWurmId());
            }
        }
    }

    private static void regenerateTitans() {
        for (Creature titan : TitanState.titans) {
            if (!titan.getBody().isWounded()) continue;
            Wounds wounds = titan.getBody().getWounds();
            Wound[] arr = wounds.getWounds();
            if (arr.length == 0) continue;
            Wound w = arr[Server.rand.nextInt(arr.length)];
            int toHeal = 5;
            if (w.getSeverity() > toHeal) {
                w.modifySeverity(-toHeal);
            } else {
                w.heal();
            }
        }
    }

    private static void pollSingleTitan(Creature titan) {
        if (!TitanState.titanDamage.containsKey(titan)) {
            TitanState.titanDamage.put(titan, titan.getStatus().damage);
            return;
        }
        int prevDamage = TitanState.titanDamage.get(titan);
        int currentDamage = titan.getStatus().damage;
        pollTimeMechanics(titan, currentDamage);
        if (currentDamage > prevDamage) {
            pollDamageMechanics(titan, prevDamage, currentDamage);
        }
        TitanState.titanDamage.put(titan, currentDamage);
    }

    private static void pollTimeMechanics(Creature titan, int currentDamage) {
        if (currentDamage <= 0) return;
        long wurmid = titan.getWurmId();
        if (titan.isOnSurface()) {
            int chance, range, radius;
            if (currentDamage > 52428) { chance = 40; range = 7; radius = 2; }
            else if (currentDamage > 32767) { chance = 45; range = 5; radius = 1; }
            else if (currentDamage > 16383) { chance = 55; range = 4; radius = 1; }
            else { chance = 60; range = 3; radius = 0; }
            Integer current = TitanState.titanAdvancedTimed.get(wurmid);
            if (current == null) {
                TitanState.titanAdvancedTimed.put(wurmid, chance);
                return;
            }
            int curr = current;
            if (curr > 0 && Server.rand.nextInt(curr) == 0) {
                TitanCombat.performAdvancedAbility(titan, range, radius);
                TitanState.titanAdvancedTimed.put(wurmid, curr + chance - 1);
            } else {
                TitanState.titanAdvancedTimed.put(wurmid, Math.max(0, curr - 1));
            }
        } else if (Server.rand.nextInt(20) == 0) {
            TitanCombat.performAdvancedAbility(titan, 3, 3);
        }
    }

    private static void pollDamageMechanics(Creature titan, int prevDamage, int currentDamage) {
        if (currentDamage > 0 && prevDamage == 0) {
            announceMilestone(titan, "[100%]", "Mere mortals dare to face me?");
        }
        if (currentDamage > 8191 && prevDamage < 8191) {
            announceMilestone(titan, "[88%]", "You actually think you can defeat me?");
        }
        if (currentDamage > 16383 && prevDamage < 16383) {
            announceMilestone(titan, "[75%]", "I am not alone.");
            TitanCombat.summonMinions(titan, Server.rand.nextInt(2) + 2);
        }
        if (currentDamage > 26214 && prevDamage < 26214) {
            announceMilestone(titan, "[60%]", "You will feel my wrath!");
            TitanCombat.performBasicAbility(titan);
        }
        if (currentDamage > 32767 && prevDamage < 32767) {
            announceMilestone(titan, "[50%]", "I've had enough of you. Minions, assemble!");
            TitanCombat.summonMinions(titan, Server.rand.nextInt(4) + 4);
            TitanCombat.performBasicAbility(titan);
        }
        if (currentDamage > 39321 && prevDamage < 39321) {
            announceMilestone(titan, "[40%]", "Let's try something new, shall we?");
            TitanCombat.performAdvancedAbility(titan, 7, 2);
            TitanCombat.performAdvancedAbility(titan, 7, 2);
        }
        if (currentDamage > 45874 && prevDamage < 45874) {
            announceMilestone(titan, "[30%]", "Perhaps minions aren't enough. Now, try my champions!");
            TitanCombat.summonChampions(titan, Server.rand.nextInt(2) + 2);
            TitanCombat.performBasicAbility(titan);
        }
        if (currentDamage > 52428 && prevDamage < 52428) {
            announceMilestone(titan, "[20%]", "Enough! I will end you!");
            TitanCombat.performBasicAbility(titan);
            TitanCombat.performAdvancedAbility(titan, 5, 3);
        }
        if (currentDamage > 58981 && prevDamage < 58981) {
            announceMilestone(titan, "[10%]", "Minions... Champions... Only one way to win a battle: An army!");
            TitanCombat.summonMinions(titan, Server.rand.nextInt(5) + 7);
            TitanCombat.summonChampions(titan, Server.rand.nextInt(3) + 3);
            TitanCombat.performBasicAbility(titan);
            TitanCombat.performAdvancedAbility(titan, 4, 3);
        }
        if (currentDamage > 16383 && Server.rand.nextInt(10) == 0) {
            int extra;
            if (currentDamage > 45874) extra = Server.rand.nextInt(2) + 2;
            else if (currentDamage > 32767) extra = Server.rand.nextInt(3) + 1;
            else extra = Server.rand.nextInt(2) + 1;
            TitanCombat.summonMinions(titan, extra);
        }
        if (currentDamage > 16383 && Server.rand.nextInt(15) == 0) {
            int gate = currentDamage > 45874 ? 10 : currentDamage > 32767 ? 12 : 10;
            if (Server.rand.nextInt(gate) == 0) {
                TitanCombat.performBasicAbility(titan);
            }
        }
        if (currentDamage > 58981 && Server.rand.nextInt(30) == 0) {
            TitanCombat.summonChampions(titan, 1);
        }
    }

    private static void announceMilestone(Creature titan, String pct, String line) {
        String msg = "<" + titan.getName() + " " + pct + "> " + line;
        TitanBroadcast.broadcast(titan, msg, CHAT_R, CHAT_G, CHAT_B);
        Zones.flash(titan.getTileX(), titan.getTileY(), false);
    }

    private static void maybeSpawnNewTitan(TitanConfig cfg) {
        long now = System.currentTimeMillis();
        if (TitanState.lastSpawnedTitan + cfg.respawnTimeMs >= now) return;
        logger.info("[titan] no titan present and timer expired; spawning a new one");
        int[] tile = SpawnFilter.pickSurfaceTile(cfg.spawn, 5000);
        if (tile == null) {
            logger.warning("[titan] could not find a suitable spawn tile after 5000 attempts");
            return;
        }
        int spawnX = tile[0] * 4;
        int spawnY = tile[1] * 4;
        int[] titanTemplates = { Lilith.templateId, Ifrit.templateId };
        try {
            Creature.doNew(titanTemplates[Server.rand.nextInt(titanTemplates.length)],
                    spawnX, spawnY, 360f * Server.rand.nextFloat(), 0, "", (byte) 0);
            TitanDb.updateLastSpawnedTitan();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[titan] failed to create titan", e);
        }
    }

    private TitanPoll() {}
}
