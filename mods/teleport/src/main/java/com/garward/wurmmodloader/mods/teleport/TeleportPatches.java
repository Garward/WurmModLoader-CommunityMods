package com.garward.wurmmodloader.mods.teleport;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bytecode patches that route the vanilla 4000–4050 transfer-landing zone
 * through {@link TeleportSpawnPicker}. The mod runs with
 * {@code sharedClassLoader=true} so the patched bytecode can resolve our
 * static callbacks by FQN.
 */
final class TeleportPatches {

    private static final Logger logger = Logger.getLogger(TeleportPatches.class.getName());
    private static final String PICKER_FQN = TeleportSpawnPicker.class.getName();

    private TeleportPatches() {}

    static void installArenaSpawnRedirect() {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();
            CtClass ctPlayerMetaData = pool.get("com.wurmonline.server.players.PlayerMetaData");

            String redirect =
                    "if (this.posx >= 4000f && this.posx <= 4050f"
                  + " && this.posy >= 4000f && this.posy <= 4050f) {"
                  + "  this.posx = " + PICKER_FQN + ".getTeleportPosX(this.wurmid);"
                  + "  this.posy = " + PICKER_FQN + ".getTeleportPosY(this.wurmid);"
                  + "}"
                  + "$_ = $proceed($$);";

            ctPlayerMetaData.getDeclaredMethod("save").instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getPosition")) {
                        m.replace(redirect);
                    }
                }
            });
            logger.info("[teleport] useArenaTeleportMethod: PlayerMetaData.save patched to redirect 4000-4050 transfer landings");
        } catch (NotFoundException e) {
            throw new HookException(e);
        } catch (CannotCompileException e) {
            logger.log(Level.SEVERE, "[teleport] FAILED to install arena spawn redirect", e);
        }
    }
}
