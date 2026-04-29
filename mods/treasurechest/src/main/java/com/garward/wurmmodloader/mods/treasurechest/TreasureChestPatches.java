package com.garward.wurmmodloader.mods.treasurechest;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bytecode patches mirroring upstream {@code TreasureChests.preInit}. Both
 * patches are gated on individual config toggles so server owners can pick
 * tier-distribution boost without the loot-table replacement (or vice versa).
 */
final class TreasureChestPatches {

    private static final Logger logger = Logger.getLogger(TreasureChestPatches.class.getName());
    private static final String LOOT = TreasureChestLoot.class.getName();

    private TreasureChestPatches() {}

    static void install(TreasureChestConfig cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();

            if (cfg.boostTierDistribution) {
                CtClass ctZone = pool.get("com.wurmonline.server.zones.Zone");
                CtMethod ctCreate = ctZone.getDeclaredMethod("createTreasureChest");
                ctCreate.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("setAuxData")) {
                            m.replace("$_ = $proceed((byte)(com.wurmonline.server.Server.rand.nextInt(100)));");
                        }
                    }
                });
                logger.info("[treasurechest] tier-distribution boost installed");
            }

            if (cfg.replaceLootTable) {
                CtClass ctItem = pool.get("com.wurmonline.server.items.Item");
                ctItem.getDeclaredMethod("fillTreasureChest").setBody(
                        "{ " + LOOT + ".newFillTreasureChest(this, this.getAuxData()); }");
                logger.info("[treasurechest] loot table replaced");
            }
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[treasurechest] failed to install bytecode patches", e);
        }
    }
}
