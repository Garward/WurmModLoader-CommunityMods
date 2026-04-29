package com.garward.wurmmodloader.mods.achievementchanges;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AchievementChangesMod implements WurmServerMod {

    private static final Logger logger = Logger.getLogger(AchievementChangesMod.class.getName());

    private static final HashMap<Integer, AchievementTemplate> goodAchievements = new HashMap<>();
    private static final ArrayList<Integer> blacklist = new ArrayList<>();

    /**
     * Whitelist of achievements with a non-empty requirement, not blacklisted,
     * and not cooking-only. Future leaderboard/action submods consume this map
     * via reflection so they don't gain a hard dependency on this jar.
     */
    public static Map<Integer, AchievementTemplate> getCuratedAchievements() {
        return Collections.unmodifiableMap(goodAchievements);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        try {
            ConcurrentHashMap<Integer, AchievementTemplate> templates = ReflectionUtil.getPrivateField(
                    Achievement.class,
                    ReflectionUtil.getField(Achievement.class, "templates"));
            generateBlacklist();
            for (int i : templates.keySet()) {
                AchievementTemplate temp = templates.get(i);
                addRequirements(temp);
                if (temp.getRequirement() != null
                        && !temp.getRequirement().equals("")
                        && !temp.isForCooking()
                        && !blacklist.contains(i)) {
                    fixName(temp);
                    goodAchievements.put(i, temp);
                }
            }
            logger.info("[achievementchanges] curated " + goodAchievements.size()
                    + " achievements (blacklisted=" + blacklist.size() + ")");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[achievementchanges] failed to read Achievement.templates", e);
        }
    }

    private static int getNumber(String name) {
        AchievementTemplate temp = Achievement.getTemplate(name);
        return temp != null ? temp.getNumber() : -1;
    }

    private static void blacklist(String name) {
        blacklist.add(getNumber(name));
    }

    private static void blacklist(int number) {
        blacklist.add(number);
    }

    private static void generateBlacklist() {
        blacklist("Adulterator");
        blacklist("All Hell");
        blacklist("Ambitious");
        blacklist("Angry Sailor");
        blacklist("Arachnophile");
        blacklist("Arch Mage");
        blacklist("Ascended");
        blacklist("Backstabber");
        blacklist("Barbarian");
        blacklist("Braaains");
        blacklist(299); // Brilliant!
        blacklist(364); // Brilliant!
        blacklist("Bumble Bee");
        blacklist("Burglar");
        blacklist("Cap'n");
        blacklist("Caravel sailor");
        blacklist("Chief Mate");
        blacklist("Cog sailor");
        blacklist("Corbita sailor");
        blacklist("Cowboy");
        blacklist("Deforestation");
        blacklist("Demolition");
        blacklist("Diabolist");
        blacklist("Die by the Rift");
        blacklist("Die by the Rift a gazillion times");
        blacklist("Drake Spirits");
        blacklist("Eagle Spirits");
        blacklist("Environmental Hero");
        blacklist("Epic finalizer");
        blacklist(298); // Exquisite Gem
        blacklist(363); // Exquisite Gem
        blacklist("Fast Learner");
        blacklist("Fine titles");
        blacklist("Fo's Favourite");
        blacklist("Ghost of the Rift Warmasters");
        blacklist("Hedgehog");
        blacklist("High Spirits");
        blacklist("Hippie");
        blacklist("Humanss!");
        blacklist("Hunter Apprentice");
        blacklist("Incarnated To Hell");
        blacklist("Investigating the Rift");
        blacklist("Jackal Hunter");
        blacklist("Janitor");
        blacklist("Johnny Appleseed");
        blacklist("Joyrider");
        blacklist("Juggernaut's demise");
        blacklist("Kingdom Assault");
        blacklist("Knarr sailor");
        blacklist("Last Rope");
        blacklist("Lord of War");
        blacklist("Mage");
        blacklist("Magician");
        blacklist("Magus");
        blacklist("Manifested No More");
        blacklist("Master Bridgebuilder");
        blacklist("Master Shipbuilder");
        blacklist("Master Winemaker");
        blacklist("Miner on Strike");
        blacklist("Moby Dick");
        blacklist("Mountain Goat");
        blacklist("Moved a Mountain");
        blacklist("Muffin Maker");
        blacklist("Mussst kill");
        blacklist("No Fuel for the Flame Of Udun");
        blacklist("On the Way to the Moon");
        blacklist("Out At Sea");
        blacklist("Out, out, brief candle!");
        blacklist("Own The Rift");
        blacklist("Pasta maker");
        blacklist("Pasta master");
        blacklist("Peace of Mind");
        blacklist("Pirate");
        blacklist("Pizza maker");
        blacklist("Pizza master");
        blacklist("Planeswalker");
        blacklist("Rider of the Apocalypse");
        blacklist("Rift Beast Nemesis");
        blacklist("Rift Ogre Hero");
        blacklist("Rift Opener");
        blacklist("Rift Specialist");
        blacklist("Rift Surfer");
        blacklist("Rowboat sailor");
        blacklist("Ruler");
        blacklist("Sailboat sailor");
        blacklist("Settlement Assault");
        blacklist("Sisyphos Says Hello");
        blacklist("Shadow");
        blacklist("Shadowmage");
        blacklist("Shutting Down");
        blacklist("Singing While Eating");
        blacklist("Sneaky");
        blacklist("Tastes like Chicken");
        blacklist("Tears of the Unicorn");
        blacklist("The Path of Vynora");
        blacklist("The Smell of Freshly Baked Bread");
        blacklist("The Smell of Freshly Made Muffins");
        blacklist("Thin Air");
        blacklist("Tree Hugger");
        blacklist("Trucker");
        blacklist("Truffle Pig");
        blacklist("Vynora commands you");
        blacklist("Waller");
        blacklist("Wanderer");
        blacklist("Went up a Hill");
        blacklist("Wet Feet");
        blacklist("You Beauty");
        blacklist("You Cannot Pass");
        blacklist("Zombie Hunter");
        blacklist("around the world");
        blacklist("brewed 1000 liters");
        blacklist("brewed liters");
        blacklist("distilled 1000 liters");
        blacklist("distilled liters");
    }

    private static void setRequirement(AchievementTemplate temp, String req) {
        try {
            ReflectionUtil.setPrivateField(temp,
                    ReflectionUtil.getField(temp.getClass(), "requirement"), req);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[achievementchanges] failed to set requirement on " + temp.getName(), e);
        }
    }

    private static void addRequirements(AchievementTemplate temp) {
        String name = temp.getName();
        switch (name) {
            case "Meoww!": setRequirement(temp, "Kill a Wild Cat"); break;
            case "Treasure Hunter": setRequirement(temp, "Open a treasure chest"); break;
            case "Mercykiller": setRequirement(temp, "Slay a Diseased creature"); break;
            case "Slayer of the Meek": setRequirement(temp, "Slay a Scared creature"); break;
            case "Willbreaker": setRequirement(temp, "Slay a Hardened creature"); break;
            case "Crocodiles Killed": setRequirement(temp, "Kill a Crocodile"); break;
            case "Tower Builder": setRequirement(temp, "Build a Guard Tower"); break;
            case "Invisible:UseResStone": setRequirement(temp, "Use a Resurrection Stone"); break;
            case "Invisible:ShakerOrbing": setRequirement(temp, "Use a Shaker Orb"); break;
            case "Invisible:CutTree": setRequirement(temp, "Cut down a tree"); break;
            case "Invisible:CatchFish": setRequirement(temp, "Catch a fish"); break;
            case "Invisible:PlantFlower": setRequirement(temp, "Plant a flower"); break;
            case "Invisible:Planting": setRequirement(temp, "Plant a tree"); break;
            case "Invisible:PickLock": setRequirement(temp, "Pick a lock"); break;
            case "Invisible:Stealth": setRequirement(temp, "Successfully hide"); break;
            case "Invisible:ItemInTrash": setRequirement(temp, "Trash an item"); break;
            case "Invisible:BulkBinDeposit": setRequirement(temp, "Deposit a bulk item"); break;
            case "Invisible:Distancemoved": setRequirement(temp, "Walk one tile"); break;
            case "Invisible:Hedges": setRequirement(temp, "Plant a hedge or flower bed"); break;
            case "Invisible:MeditatingAction": setRequirement(temp, "Meditate"); break;
            case "Invisible:PickMushroom": setRequirement(temp, "Pick a mushroom"); break;
            case "Maintenance": setRequirement(temp, "Repair a fence, floor, or wall"); break;
            case "Be Gentle Please": setRequirement(temp, "Win a spar"); break;
            case "Rarity": setRequirement(temp, "Make the best quality of an item"); break;
            default: break;
        }
    }

    private static void fixName(AchievementTemplate temp) {
        if (temp.getName().contains("Invisible:")) {
            temp.setName(temp.getName().replaceAll("Invisible:", ""));
        }
        switch (temp.getName()) {
            case "PlayerkillBow":     temp.setName("Arrow To The Knee"); break;
            case "PlayerkillSword":   temp.setName("Pointing The Right Direction"); break;
            case "PlayerkillMaul":    temp.setName("Trip To The Maul"); break;
            case "PlayerkillAxe":     temp.setName("Can I Axe You A Question?"); break;
            case "OuchThatHurt":      temp.setName("Ouch That Hurt"); break;
            case "UseResStone":       temp.setName("No Risk No Reward"); break;
            case "ShakerOrbing":      temp.setName("This Mine Is Busted"); break;
            case "CutTree":           temp.setName("Getting Wood"); break;
            case "CatchFish":         temp.setName("Delicious Fish"); break;
            case "PlantFlower":       temp.setName("Gardening"); break;
            case "Planting":          temp.setName("Forester"); break;
            case "PickLock":          temp.setName("Vault Hunter"); break;
            case "ItemInTrash":       temp.setName("Another Mans Trash"); break;
            case "Stealth":           temp.setName("The Invisible Man"); break;
            case "BulkBinDeposit":    temp.setName("Bulk Hoarder"); break;
            case "Distancemoved":     temp.setName("Explorer"); break;
            case "Hedges":            temp.setName("Hedging Your Bets"); break;
            case "MeditatingAction":  temp.setName("One With The World"); break;
            case "PickMushroom":      temp.setName("Mushroom Collector"); break;
            default: break;
        }
    }
}
