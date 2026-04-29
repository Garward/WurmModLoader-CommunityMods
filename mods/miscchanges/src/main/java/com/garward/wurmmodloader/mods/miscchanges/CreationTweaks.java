package com.garward.wurmmodloader.mods.miscchanges;

import com.garward.wurmmodloader.modloader.ReflectionUtil;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.ItemList;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link CreationEntry} skill-gating tweaks that have to run after the full
 * creation matrix is built (i.e. at {@code ServerStartedEvent}, not at
 * template load).
 */
final class CreationTweaks {

    private static final Logger logger = Logger.getLogger(CreationTweaks.class.getName());

    private CreationTweaks() {}

    static void applyAll(MiscChangesMod cfg) {
        if (cfg.removeLockpickSkillRequirement) {
            removeSkillRequirement(ItemList.lockpick, "lockpick");
        }
    }

    private static void removeSkillRequirement(int templateId, String label) {
        CreationEntry entry = CreationMatrix.getInstance().getCreationEntry(templateId);
        if (entry == null) {
            logger.warning("[miscchanges] no creation entry for " + label + " (id=" + templateId + ")");
            return;
        }
        try {
            ReflectionUtil.setPrivateField(entry,
                    ReflectionUtil.getField(entry.getClass(), "hasMinimumSkillRequirement"), false);
            ReflectionUtil.setPrivateField(entry,
                    ReflectionUtil.getField(entry.getClass(), "minimumSkill"), 0.0);
            logger.info("[miscchanges] dropped creation skill requirement for " + label);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "[miscchanges] failed to drop skill requirement for " + label, e);
        }
    }
}
