package com.garward.wurmmodloader.mods.mounted;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.vehicle.MountSpeedPercentEvent;
import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modloader.internal.classhooks.HookManager;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.shared.constants.BodyPartConstants;
import com.wurmonline.shared.constants.Enchants;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MountedMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(MountedMod.class.getName());

    boolean newMountSpeedScaling = true;
    boolean updateMountSpeedOnDamage = true;

    @Override
    public void configure(Properties properties) {
        newMountSpeedScaling = bool(properties, "newMountSpeedScaling", newMountSpeedScaling);
        updateMountSpeedOnDamage = bool(properties, "updateMountSpeedOnDamage", updateMountSpeedOnDamage);
    }

    @Override
    public void preInit() {
        if (updateMountSpeedOnDamage) {
            installSetWoundedPatch();
        }
    }

    @SubscribeEvent
    public void onMountSpeedPercent(MountSpeedPercentEvent event) {
        if (!newMountSpeedScaling) {
            return;
        }
        Creature creature = event.getCreature();
        boolean mounting = event.isMounting();
        event.setPercent(newMountSpeedMultiplier(creature, mounting));
    }

    private static float newMountSpeedMultiplier(Creature creature, boolean mounting) {
        float hunger = creature.getStatus().getHunger() / 65535f;
        float damage = creature.getStatus().damage / 65535f;
        float factor = ((((1f - damage * damage) * (1f - damage) + (1f - 2f * damage) * damage) * (1f - damage)
                + (1f - damage) * damage) * (1f - 0.4f * hunger * hunger));
        try {
            Method m = ReflectionUtil.getMethod(creature.getClass(), "getTraitMovePercent");
            float traitMove = ReflectionUtil.callPrivateMethod(creature, m, mounting);
            factor += traitMove;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.log(Level.WARNING, "[mounted] failed to read getTraitMovePercent", e);
        }
        if (creature.isHorse() || creature.isUnicorn()) {
            factor *= calcHorseShoeBonus(creature);
        }
        if (creature.getBonusForSpellEffect(Enchants.CRET_OAKSHELL) > 0.0f) {
            factor *= 1f - (0.3f * (creature.getBonusForSpellEffect(Enchants.CRET_OAKSHELL) / 100.0f));
        }
        if (creature.isRidden()) {
            try {
                float saddleFactor = 1.0f;
                Item saddle = creature.getEquippedItem(BodyPartConstants.TORSO);
                if (saddle != null) {
                    saddle.setDamage(saddle.getDamage() + (saddle.getDamageModifier() * 0.001f));
                    saddleFactor += Math.max(10f, saddle.getCurrentQualityLevel()) / 2000f;
                    saddleFactor += saddle.getSpellSpeedBonus() / 2000f;
                    saddleFactor += saddle.getRarity() * 0.03f;
                    factor *= saddleFactor;
                }
            } catch (NoSpaceException ignored) {
            }
            factor *= creature.getMovementScheme().getSpeedModifier();
        }
        return factor;
    }

    private static float calcHorseShoeBonus(Creature creature) {
        float factor = 1.0f;
        ArrayList<Item> gear = new ArrayList<>();
        addShoe(creature, BodyPartConstants.LEFT_FOOT, gear);
        addShoe(creature, BodyPartConstants.RIGHT_FOOT, gear);
        addShoe(creature, BodyPartConstants.LEFT_HAND, gear);
        addShoe(creature, BodyPartConstants.RIGHT_HAND, gear);
        for (Item shoe : gear) {
            factor += Math.max(10f, shoe.getCurrentQualityLevel()) / 2000f;
            factor += shoe.getSpellSpeedBonus() / 2000f;
            factor += shoe.getRarity() * 0.03f;
        }
        return factor;
    }

    private static void addShoe(Creature creature, byte slot, ArrayList<Item> gear) {
        try {
            Item shoe = creature.getEquippedItem(slot);
            if (shoe != null) {
                shoe.setDamage(shoe.getDamage() + (shoe.getDamageModifier() * 0.002f));
                gear.add(shoe);
            }
        } catch (NoSpaceException ignored) {
        }
    }

    private void installSetWoundedPatch() {
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            if (ctCreature.isFrozen()) {
                ctCreature.defrost();
            }
            CtMethod method = ctCreature.getDeclaredMethod("setWounded");
            method.insertBefore("forceMountSpeedChange();");
            logger.info("[mounted] updateMountSpeedOnDamage: Creature.setWounded patched");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[mounted] failed to install setWounded patch", e);
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
