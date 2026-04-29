package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.AutoEquipMethods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Lives in {@code com.wurmonline.server.questions} so it can extend the
 * package-private {@link Question} machinery. Re-creates Sindusk's PvP
 * respawn dialog (token / random / cross-server transfer); only used when
 * {@code sendNewSpawnQuestionOnPvP=true}.
 */
public class NewSpawnQuestion extends Question {

    public NewSpawnQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
    }

    public void spawn(Player p, Spawnpoint spawnpoint) {
        if (!p.isDead()) return;
        p.addNewbieBuffs();
        p.setLayer(0, false);
        if (p.isUndead()) {
            float[] txty = Player.findRandomSpawnX(false, false);
            p.setTeleportPoints(txty[0], txty[1], 0, 0);
            p.startTeleporting();
            p.getCommunicator().sendNormalServerMessage("You are cast back into the horrible light.");
        } else {
            p.setTeleportPoints(spawnpoint.tilex, spawnpoint.tiley, spawnpoint.surfaced ? 0 : -1, 0);
            p.startTeleporting();
            p.getCommunicator().sendNormalServerMessage("You are cast back into the light.");
        }
        p.getCommunicator().sendTeleport(false);
        try {
            ReflectionUtil.callPrivateMethod(p, ReflectionUtil.getMethod(p.getClass(), "setDead"), false);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        p.spawnpoints = null;
    }

    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("accept") && answer.get("accept") == "true";
        if (accepted) {
            int entry = Integer.parseInt(answer.getProperty("spawnpoint"));
            Spawnpoint spawn = spawns.get(entry);
            this.spawn((Player) this.getResponder(), spawn);
            return;
        }
        boolean transfer = answer.containsKey("transfer") && answer.get("transfer") == "true";
        if (!transfer) {
            this.getResponder().getCommunicator().sendNormalServerMessage(
                    "You can bring the spawn question back by typing /respawn in a chat window.");
            return;
        }
        Spawnpoint spawn = spawns.get(0);
        this.spawn((Player) this.getResponder(), spawn);
        for (Item equip : this.getResponder().getBody().getAllItems()) {
            AutoEquipMethods.unequip(equip, this.getResponder());
        }
        if (!this.getResponder().getPrimWeapon().isBodyPartAttached()) {
            AutoEquipMethods.unequip(this.getResponder().getPrimWeapon(), this.getResponder());
        }
        try {
            ServerEntry targetServer = Servers.localServer.serverSouth;
            Player player = Players.getInstance().getPlayer(this.getResponder().getWurmId());
            if (targetServer == null) {
                player.getCommunicator().sendNormalServerMessage("Error: Something went wrong [TARGETSERVER=NULL].");
                return;
            }
            if (!targetServer.isAvailable(player.getPower(), true)) {
                player.getCommunicator().sendNormalServerMessage(targetServer.name + " is not currently available.");
            } else {
                int tilex = 1010;
                int tiley = 1010;
                player.sendTransfer(Server.getInstance(), targetServer.EXTERNALIP,
                        Integer.parseInt(targetServer.EXTERNALPORT), targetServer.INTRASERVERPASSWORD,
                        targetServer.getId(), tilex, tiley, true, false, player.getKingdomId());
            }
        } catch (NoSuchPlayerException e) {
            e.printStackTrace();
        }
    }

    protected static HashMap<Integer, Spawnpoint> spawns = new HashMap<>();

    public Spawnpoint getRandomSpawnpoint(byte spawnNums) {
        int i = 1000;
        while (i > 0) {
            i--;
            int x = Server.rand.nextInt(Server.surfaceMesh.getSize());
            int y = Server.rand.nextInt(Server.surfaceMesh.getSize());
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
            if (height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30) {
                Village v = Villages.getVillage(x, y, true);
                if (v == null) {
                    for (int vx = -50; vx < 50; vx += 5) {
                        for (int vy = -50; vy < 50 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
                        }
                    }
                }
                if (v != null) continue;
                return new Spawnpoint(spawnNums, "Random location", (short) x, (short) y, true);
            }
        }
        return null;
    }

    public String getSpawnpointOptions() {
        StringBuilder builder = new StringBuilder();
        byte spawnNums = 0;
        if (this.getResponder().citizenVillage != null) {
            Village v = this.getResponder().citizenVillage;
            Spawnpoint home = new Spawnpoint(spawnNums, "Token of " + v.getName(),
                    (short) v.getTokenX(), (short) v.getTokenY(), v.isOnSurface());
            spawns.put((int) spawnNums, home);
            builder.append(home.description).append(',');
            spawnNums++;
        }
        Spawnpoint random = getRandomSpawnpoint(spawnNums);
        spawns.put((int) spawnNums, random);
        if (random != null) builder.append(random.description);
        return builder.toString();
    }

    @Override
    public void sendQuestion() {
        StringBuilder b = new StringBuilder();
        b.append("border{center{text{type='bold';text=\"\"}};null;");
        b.append("scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";");
        b.append("passthrough{id=\"id\";text=\"").append(this.id).append("\"}");
        b.append("text{type='bold';text=\"Where would you like to spawn?\\n\\n\"}");
        b.append("text{text=\"\\n\\n\"}");
        b.append("harray{label{text='Respawn Point:'}dropdown{id='spawnpoint';options='");
        b.append(getSpawnpointOptions());
        b.append("'}}");
        b.append("text{text=\"\\n\\n\"}");
        b.append("harray {button{text='  Accept  ';id='accept'}}");
        b.append("text{text=\"\\n\\n\"}");
        b.append("text{type='bold';text=\"Or transfer back to the PvE server:\"}");
        b.append("text{text=\"\\n\\n\"}");
        b.append("harray {button{text='  Transfer to PvE  ';id='transfer'}}");
        b.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(400, 300, true, true, b.toString(),
                128, 50, 50, this.title);
    }
}
