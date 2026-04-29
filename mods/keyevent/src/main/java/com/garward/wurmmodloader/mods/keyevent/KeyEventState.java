package com.garward.wurmmodloader.mods.keyevent;

import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;

import java.util.ArrayList;
import java.util.List;

/**
 * Single-active "Call upon the heavens" ritual orchestrator. The casting
 * player has six time-windowed response slots (Desire / Fo / Magranon /
 * Vynora / Libila / Ascend). Chat lines from the casting player are
 * matched against deity-specific power keywords and recorded into the
 * matching open window.
 *
 * <p>Direct port of {@code mod.sin.wyvern.KeyEvent} -- renamed to avoid
 * the framework "Event" naming convention. State is a single static
 * instance because the upstream ritual is also single-instance.
 */
public final class KeyEventState {

    private KeyEventState() {}

    private static final class Response {
        final int index;
        final long startTime;
        final long endTime;
        String response = "";

        Response(int index, int startSecond, int endSecond) {
            this.index = index;
            this.startTime = startSecond * TimeConstants.SECOND_MILLIS;
            this.endTime = endSecond * TimeConstants.SECOND_MILLIS;
        }
    }

    private static final List<Response> responses = new ArrayList<>();

    private static void resetResponses() {
        responses.clear();
        responses.add(new Response(0, 40, 60));    // Desire
        responses.add(new Response(1, 120, 150));  // Fo
        responses.add(new Response(2, 170, 200));  // Magranon
        responses.add(new Response(3, 220, 250));  // Vynora
        responses.add(new Response(4, 270, 300));  // Libila
        responses.add(new Response(5, 340, 370));  // Ascend
        hasWeaponEnchant = false;
        hasCreatureEnchant = false;
        hasIndustryEnchant = false;
        hasHeal = false;
        hasTame = false;
    }

    public static String getResponse(int index) {
        for (Response r : responses) {
            if (r.index == index) return r.response;
        }
        return "";
    }

    public static boolean hasWeaponEnchant = false;
    public static boolean hasCreatureEnchant = false;
    public static boolean hasIndustryEnchant = false;
    public static boolean hasHeal = false;
    public static boolean hasTame = false;

    public static String foPower = "";
    public static String magranonPower = "";
    public static String vynoraPower = "";
    public static String libilaPower = "";
    public static String ascendTemplate = "";

    // ---- Fo --------------------------------------------------------------

    public static String getFoPowers() {
        return "I offer the following: Life Transfer, Oakshell, Light of Fo, Charm";
    }

    public static boolean isValidFo() {
        return isValidFo(getResponse(1).toLowerCase());
    }

    public static boolean isValidFo(String response) {
        if (response.contains("life transfer") || response.contains("lifetransfer") || response.equals("lt")) {
            foPower = "Life Transfer";
            hasWeaponEnchant = true;
            return true;
        } else if (response.contains("oakshell") || response.contains("oak shell")) {
            foPower = "Oakshell";
            hasCreatureEnchant = true;
            return true;
        } else if (response.contains("light of fo") || response.contains("lof") || response.contains("light fo")) {
            foPower = "Light of Fo";
            hasHeal = true;
            return true;
        } else if (response.contains("charm") || response.contains("tame")) {
            foPower = "Charm";
            hasTame = true;
            return true;
        }
        return false;
    }

    // ---- Magranon --------------------------------------------------------

    public static String getMagranonPowers() {
        StringBuilder b = new StringBuilder("I offer the following: ");
        boolean started = false;
        if (!hasWeaponEnchant)   { b.append("Flaming Aura"); started = true; }
        if (!hasCreatureEnchant) { b.append(started ? ", Frantic Charge" : "Frantic Charge"); started = true; }
        if (!hasIndustryEnchant) { b.append(started ? ", Efficiency" : "Efficiency"); started = true; }
        if (!hasHeal)            { b.append(started ? ", Mass Stamina" : "Mass Stamina"); started = true; }
        if (!hasTame)            { b.append(started ? ", Dominate" : "Dominate"); }
        b.append(", Strongwall");
        return b.toString();
    }

    public static void setRandomMagranonPower() {
        if (!hasWeaponEnchant) {
            magranonPower = "Flaming Aura"; hasWeaponEnchant = true;
        } else if (!hasCreatureEnchant) {
            magranonPower = "Frantic Charge"; hasCreatureEnchant = true;
        } else if (!hasIndustryEnchant) {
            magranonPower = "Efficiency"; hasIndustryEnchant = true;
        } else {
            magranonPower = "Strongwall";
        }
    }

    public static boolean isValidMagranon() {
        return isValidMagranon(getResponse(2).toLowerCase());
    }

    public static boolean isValidMagranon(String response) {
        if (response.contains("flaming aura") || response.contains("flamingaura")
                || response.contains("flame aura") || response.equals("fa")) {
            magranonPower = "Flaming Aura"; hasWeaponEnchant = true; return true;
        } else if (response.contains("frantic") || response.contains("charge")) {
            magranonPower = "Frantic Charge"; hasCreatureEnchant = true; return true;
        } else if (response.contains("mass stam") || response.contains("stamina")) {
            magranonPower = "Mass Stamina"; hasHeal = true; return true;
        } else if (response.contains("effic") || response.contains("effec")) {
            magranonPower = "Efficiency"; hasIndustryEnchant = true; return true;
        } else if (response.contains("dominate") || response.contains("dom")) {
            magranonPower = "Dominate"; hasTame = true; return true;
        } else if (response.contains("wall") || response.contains("strong")) {
            magranonPower = "Strongwall"; return true;
        }
        return false;
    }

    // ---- Vynora ----------------------------------------------------------

    public static String getVynoraPowers() {
        StringBuilder b = new StringBuilder("I offer the following: ");
        b.append("Wind of Ages, Circle of Cunning, Aura of Shared Pain");
        if (!hasWeaponEnchant)   b.append(", Frostbrand, Nimbleness, Mind Stealer");
        if (!hasCreatureEnchant) b.append(", Excel");
        b.append(", Opulence");
        return b.toString();
    }

    public static void setRandomVynoraPower() {
        if (!hasWeaponEnchant) {
            vynoraPower = "Nimbleness"; hasWeaponEnchant = true;
        } else if (!hasCreatureEnchant) {
            vynoraPower = "Excel"; hasCreatureEnchant = true;
        } else {
            vynoraPower = "Wind of Ages"; hasIndustryEnchant = true;
        }
    }

    public static boolean isValidVynora() {
        return isValidVynora(getResponse(3).toLowerCase());
    }

    public static boolean isValidVynora(String response) {
        if (response.contains("frost")) {
            vynoraPower = "Frostbrand"; hasWeaponEnchant = true; return true;
        } else if (response.contains("nimb")) {
            vynoraPower = "Nimbleness"; hasWeaponEnchant = true; return true;
        } else if (response.contains("mind stealer") || response.contains("mindstealer")) {
            vynoraPower = "Mind Stealer"; hasWeaponEnchant = true; return true;
        } else if (response.contains("excel")) {
            vynoraPower = "Excel"; hasCreatureEnchant = true; return true;
        } else if (response.contains("wind") || response.equals("woa")) {
            vynoraPower = "Wind of Ages"; hasIndustryEnchant = true; return true;
        } else if (response.contains("circle") || response.contains("cunning") || response.equals("coc")) {
            vynoraPower = "Circle of Cunning"; hasIndustryEnchant = true; return true;
        } else if (response.contains("aura") || response.contains("shared") || response.equals("aosp")) {
            vynoraPower = "Aura of Shared Pain"; return true;
        } else if (response.contains("opulence")) {
            vynoraPower = "Opulence"; return true;
        }
        return false;
    }

    // ---- Libila ----------------------------------------------------------

    public static String getLibilaPowers() {
        StringBuilder b = new StringBuilder("I offer the following: ");
        b.append("Web Armour");
        if (!hasWeaponEnchant)   b.append(", Bloodthirst, Rotting Touch");
        if (!hasCreatureEnchant) b.append(", Truehit");
        if (!hasHeal)            b.append(", Scorn of Libila");
        if (!hasIndustryEnchant) b.append(", Blessings of the Dark");
        if (!hasTame)            b.append(", Rebirth");
        b.append(", Drain Health, Drain Stamina");
        return b.toString();
    }

    public static void setRandomLibilaPower() {
        if (!hasWeaponEnchant) {
            libilaPower = "Rotting Touch"; hasWeaponEnchant = true;
        } else if (!hasCreatureEnchant) {
            libilaPower = "Truehit"; hasCreatureEnchant = true;
        } else if (!hasTame) {
            libilaPower = "Rebirth"; hasTame = true;
        } else {
            libilaPower = "Drain Health";
        }
    }

    public static boolean isValidLibila() {
        return isValidLibila(getResponse(4).toLowerCase());
    }

    public static boolean isValidLibila(String response) {
        if (response.contains("bloodthirst") || response.contains("blood thirst")) {
            libilaPower = "Bloodthirst"; hasWeaponEnchant = true; return true;
        } else if (response.contains("rotting") || response.contains("touch")) {
            libilaPower = "Rotting Touch"; hasWeaponEnchant = true; return true;
        } else if (response.contains("truehit") || response.contains("truhit")) {
            libilaPower = "Truehit"; hasCreatureEnchant = true; return true;
        } else if (response.contains("scorn")) {
            libilaPower = "Scorn of Libila"; hasHeal = true; return true;
        } else if (response.contains("blessing") || response.contains("dark") || response.equals("botd")) {
            libilaPower = "Blessings of the Dark"; hasIndustryEnchant = true; return true;
        } else if (response.contains("rebirth")) {
            libilaPower = "Rebirth"; hasTame = true; return true;
        } else if (response.contains("health")) {
            libilaPower = "Drain Health"; return true;
        } else if (response.contains("stam")) {
            libilaPower = "Drain Stamina"; return true;
        } else if (response.contains("web") || response.contains("armour")) {
            libilaPower = "Web Armour"; return true;
        }
        return false;
    }

    // ---- Ascend ----------------------------------------------------------

    public static void setRandomAscendTemplate() {
        ascendTemplate = Server.rand.nextBoolean() ? "Fo" : "Vynora";
    }

    public static boolean isValidAscendTemplate() {
        return isValidAscendTemplate(getResponse(5).toLowerCase());
    }

    public static boolean isValidAscendTemplate(String response) {
        if (response.equals("fo")) { ascendTemplate = "Fo"; return true; }
        else if (response.contains("mag")) { ascendTemplate = "Magranon"; return true; }
        else if (response.contains("vyn")) { ascendTemplate = "Vynora"; return true; }
        else if (response.contains("lib")) { ascendTemplate = "Libila"; return true; }
        return false;
    }

    private static boolean isValidResponse(int index, String message) {
        switch (index) {
            case 0: return true;
            case 1: return isValidFo(message);
            case 2: return isValidMagranon(message);
            case 3: return isValidVynora(message);
            case 4: return isValidLibila(message);
            case 5: return isValidAscendTemplate(message);
            default: return true;
        }
    }

    // ---- Active state ----------------------------------------------------

    private static boolean active = false;
    private static long startTime = 0L;
    private static Creature performer = null;

    public static boolean isActive() {
        return active;
    }

    public static Creature getPerformer() {
        return performer;
    }

    public static void setActive(long time, Creature aPerformer) {
        active = true;
        startTime = time;
        performer = aPerformer;
        resetResponses();
    }

    public static void clear() {
        active = false;
        performer = null;
        startTime = 0L;
    }

    /**
     * Record a player's chat line into whichever response window is open.
     * Caller filters by sender == performer; we re-check anyway.
     */
    public static void handlePlayerMessage(Creature sender, String rawMessage) {
        if (!active || performer == null || sender != performer) return;
        long currentTime = System.currentTimeMillis() - startTime;
        String lower = rawMessage.toLowerCase();
        for (Response r : responses) {
            if (r.startTime < currentTime && r.endTime > currentTime) {
                if (isValidResponse(r.index, lower)) {
                    r.response = lower;
                }
            }
        }
    }

    public static void preInit() {
        resetResponses();
    }
}
