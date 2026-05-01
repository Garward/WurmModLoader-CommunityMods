package com.garward.wurmmodloader.mods.serverpacks;

import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.logging.Logger;

/**
 * <strong>Compatibility shim.</strong>
 *
 * <p>Server-pack hosting moved into the framework as of WurmModLoader 0.10.2
 * (Scope B promotion of the {@code serverpacks} subsystem). The framework's
 * {@code com.garward.wurmmodloader.core.serverpacks.ServerPackHost} now owns
 * channel registration, HTTP serving, and the {@code serverpacks:add_pack}
 * ModActionEvent — there is nothing left for this mod to do.
 *
 * <p>This shim is preserved so existing server installs that still ship a
 * {@code mods/serverpacks/} folder don't trip a "no such class" error during
 * mod load. It logs a one-shot deprecation notice and otherwise no-ops.
 *
 * <p>Server admins may safely delete the {@code mods/serverpacks/} folder
 * after upgrading; the framework registers the same channels and the same
 * HTTP endpoint without it.
 *
 * <p>The new public API for mods that need to publish packs is
 * {@code com.garward.wurmmodloader.api.serverpacks.ServerPacks}. Mods compiled
 * against the old {@code com.garward.wurmmodloader.mods.serverpacks.api.ServerPacks}
 * keep working through a deprecated alias in the framework api jar — no
 * source changes required.
 */
public class ServerPackMod implements WurmServerMod {

    private static final Logger logger = Logger.getLogger(ServerPackMod.class.getName());

    @Override
    public void init() {
        logger.info("[ServerPacks shim] mods/serverpacks is now framework-owned. "
            + "This jar can be safely removed; framework registers all pack channels.");
    }
}
