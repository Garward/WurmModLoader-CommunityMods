package com.garward.wurmmodloader.mods.spellcraft;

import com.wurmonline.server.spells.Spell;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating ItemEnchantment spells using reflection.
 * Avoids direct class extension which would freeze ItemEnchantment before patches run.
 */
public class SpellFactory {
    
    private static final Logger logger = Logger.getLogger(SpellFactory.class.getName());
    
    /**
     * Create an ItemEnchantment spell using reflection.
     */
    public static Spell createItemEnchantment(
        String name,
        int castTime,
        int cost,
        int difficulty,
        int faith,
        long cooldown,
        byte enchantmentId,
        String effectDesc,
        String description,
        boolean targetItem
    ) {
        try {
            // Get the ItemEnchantment class
            Class<?> itemEnchantmentClass = Class.forName("com.wurmonline.server.spells.ItemEnchantment");
            
            // Get the constructor
            Constructor<?> constructor = itemEnchantmentClass.getDeclaredConstructor(
                String.class, int.class, int.class, int.class, int.class, int.class, long.class
            );
            constructor.setAccessible(true);
            
            // Create instance
            int actionId = ModActions.getNextActionId();
            Object spell = constructor.newInstance(name, actionId, castTime, cost, difficulty, faith, cooldown);
            
            // Set fields
            setField(spell, "enchantment", enchantmentId);
            setField(spell, "effectdesc", effectDesc);
            setField(spell, "description", description);
            setField(spell, "targetItem", targetItem);
            
            logger.log(Level.INFO, "Created spell: " + name + " (action " + actionId + ")");
            
            return (Spell) spell;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create spell: " + name, e);
            throw new RuntimeException("Failed to create spell: " + name, e);
        }
    }
    
    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        Field field = null;
        
        // Search up the inheritance hierarchy
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        
        if (field == null) {
            throw new NoSuchFieldException("Field " + fieldName + " not found");
        }
        
        field.setAccessible(true);
        field.set(obj, value);
    }
}
