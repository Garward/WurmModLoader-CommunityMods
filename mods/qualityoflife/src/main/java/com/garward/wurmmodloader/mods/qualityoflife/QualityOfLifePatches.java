package com.garward.wurmmodloader.mods.qualityoflife;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.zones.NoSuchZoneException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bytecode patches that wire vanilla mining/chopping action endpoints
 * through to {@link #vehicleHook(Creature, Item)} so the resulting item
 * lands in the player's commanded vehicle instead of on the ground.
 * Also relaxes the statuette material check and the slope-stamina cutoff.
 *
 * <p>Patched bytecode resolves these static methods by FQN; the mod runs
 * with {@code sharedClassLoader=true} so they sit on the same class loader
 * as the vanilla classes that call them.
 */
public final class QualityOfLifePatches {

    private static final Logger logger = Logger.getLogger(QualityOfLifePatches.class.getName());

    private QualityOfLifePatches() {}

    static void install(QualityOfLifeMod cfg) {
        try {
            ClassPool pool = HookManager.getInstance().getClassPool();

            CtClass ctAction = pool.get("com.wurmonline.server.behaviours.Action");
            CtClass ctCreature = pool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = pool.get("com.wurmonline.server.items.Item");
            CtClass ctCaveWall = pool.get("com.wurmonline.server.behaviours.CaveWallBehaviour");
            CtClass ctTileRock = pool.get("com.wurmonline.server.behaviours.TileRockBehaviour");
            CtClass ctMethodsItems = pool.get("com.wurmonline.server.behaviours.MethodsItems");

            String hookFqn = QualityOfLifePatches.class.getName();

            CtClass[] caveActionParams = {
                    ctAction, ctCreature, ctItem,
                    CtClass.intType, CtClass.intType, CtClass.booleanType,
                    CtClass.intType, CtClass.intType, CtClass.intType,
                    CtClass.shortType, CtClass.floatType
            };
            String caveActionDesc = Descriptor.ofMethod(CtClass.booleanType, caveActionParams);

            if (cfg.mineCaveToVehicle) {
                replaceMethodCall(ctCaveWall, "action", caveActionDesc, "putItemInfrontof",
                        "$_ = null; " + hookFqn + ".vehicleHook(performer, $0);",
                        "mineCaveToVehicle: route cave-mined ore into the player's vehicle");
            }

            if (cfg.mineSurfaceToVehicle) {
                // Surface mine ends in a setDataXY — wrap it so the freshly minted ore
                // (the receiver $0 of the call) gets handed to the vehicle hook.
                replaceMethodCallDeclared(ctTileRock, "mine", "setDataXY",
                        "$_ = $proceed($$); " + hookFqn + ".vehicleHook(performer, $0);",
                        "mineSurfaceToVehicle: route surface-mined ore into the player's vehicle");
            }

            if (cfg.chopLogsToVehicle) {
                replaceMethodCallDeclared(ctMethodsItems, "chop", "putItemInfrontof",
                        "$_ = null; " + hookFqn + ".vehicleHook(performer, $0);",
                        "chopLogsToVehicle: route chopped logs into the player's vehicle");
            }

            if (cfg.statuetteAnyMaterial) {
                String isHolyDesc = Descriptor.ofMethod(CtClass.booleanType, new CtClass[]{});
                setMethodBody(ctItem, "isHolyItem", isHolyDesc,
                        "{ return this.template.holyItem; }",
                        "statuetteAnyMaterial: drop the gold/silver material gate on statuette casting");
            }

            if (cfg.mineGemsToVehicle) {
                CtClass[] gemParams = {
                        CtClass.intType, CtClass.intType, CtClass.intType, CtClass.intType,
                        ctCreature, CtClass.doubleType, CtClass.booleanType, ctAction
                };
                String gemDesc = Descriptor.ofMethod(ctItem, gemParams);
                replaceMethodCall(ctTileRock, "createGem", gemDesc, "putItemInfrontof",
                        "$_ = null; " + hookFqn + ".vehicleHook(performer, $0);",
                        "mineGemsToVehicle: route gems / source crystals / flint / salt into the player's vehicle");
            }

            if (cfg.regenerateStaminaOnVehicleAnySlope) {
                // Player.poll() reads its `vehicle` field to gate stamina regen on slopes.
                // Returning -10L makes that read look like "not on a vehicle", which on this
                // code path bypasses the slope cutoff and lets stamina regenerate freely.
                CtClass ctPlayer = pool.get("com.wurmonline.server.players.Player");
                ctPlayer.getMethod("poll", "()Z").instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess f) throws CannotCompileException {
                        if (f.getFieldName().equals("vehicle") && f.isReader()) {
                            f.replace("$_ = -10L;");
                        }
                    }
                });
                logger.info("[qualityoflife] regenerateStaminaOnVehicleAnySlope: patched Player.poll vehicle reads");
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        } catch (CannotCompileException e) {
            logger.log(Level.WARNING, "[qualityoflife] javassist compile failure", e);
        }
    }

    private static void replaceMethodCall(CtClass ct, String methodName, String descriptor,
                                          String callName, String replacement, String reason) {
        try {
            ct.getMethod(methodName, descriptor).instrument(new ExprEditor() {
                @Override
                public void edit(javassist.expr.MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) {
                        m.replace(replacement);
                    }
                }
            });
            logger.info("[qualityoflife] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[qualityoflife] FAILED " + reason, e);
        }
    }

    private static void replaceMethodCallDeclared(CtClass ct, String methodName,
                                                  String callName, String replacement, String reason) {
        try {
            ct.getDeclaredMethod(methodName).instrument(new ExprEditor() {
                @Override
                public void edit(javassist.expr.MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals(callName)) {
                        m.replace(replacement);
                    }
                }
            });
            logger.info("[qualityoflife] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[qualityoflife] FAILED " + reason, e);
        }
    }

    private static void setMethodBody(CtClass ct, String methodName, String descriptor,
                                      String body, String reason) {
        try {
            ct.getMethod(methodName, descriptor).setBody(body);
            logger.info("[qualityoflife] " + reason);
        } catch (NotFoundException | CannotCompileException e) {
            logger.log(Level.SEVERE, "[qualityoflife] FAILED " + reason, e);
        }
    }

    /** Cargo dispatch: BSB → crate → vehicle hold → ground fallback. */
    public static void vehicleHook(Creature performer, Item item) {
        Item vehicle = getVehicleSafe(performer);
        if (vehicle != null && vehicle.isHollow() && insertItemIntoVehicle(item, vehicle, performer)) {
            return;
        }
        try {
            item.putItemInfrontof(performer);
        } catch (NoSuchCreatureException | NoSuchItemException
                | NoSuchPlayerException | NoSuchZoneException e) {
            logger.log(Level.WARNING, "[qualityoflife] vehicleHook ground-fallback failed", e);
        }
    }

    private static Item getVehicleSafe(Creature pilot) {
        try {
            if (pilot.getVehicle() != -10) {
                return Items.getItem(pilot.getVehicle());
            }
        } catch (NoSuchItemException ignored) {
            // pilot reports a vehicle id that no longer resolves — treat as dismounted
        }
        return null;
    }

    public static boolean insertItemIntoVehicle(Item item, Item vehicle, Creature performer) {
        if (item.getTemplate().isBulk() && item.getRarity() == 0) {
            // Try BSBs first.
            for (Item container : vehicle.getAllItems(true)) {
                if (container.getTemplateId() == ItemList.bulkContainer
                        && container.getFreeVolume() >= item.getVolume()
                        && item.AddBulkItem(performer, container)) {
                    performer.getCommunicator().sendNormalServerMessage(String.format(
                            "You put the %s in the %s in your %s.",
                            item.getName(), container.getName(), vehicle.getName()));
                    return true;
                }
            }
            // Then crates.
            for (Item container : vehicle.getAllItems(true)) {
                if (container.isCrate() && container.canAddToCrate(item)
                        && item.AddBulkItemToCrate(performer, container)) {
                    performer.getCommunicator().sendNormalServerMessage(String.format(
                            "You put the %s in the %s in your %s.",
                            item.getName(), container.getName(), vehicle.getName()));
                    return true;
                }
            }
        }
        // Fallback to the vehicle's own hold.
        if (vehicle.getNumItemsNotCoins() < 100
                && vehicle.getFreeVolume() >= item.getVolume()
                && vehicle.insertItem(item)) {
            performer.getCommunicator().sendNormalServerMessage(String.format(
                    "You put the %s in the %s.", item.getName(), vehicle.getName()));
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage(String.format(
                "The %s is too full to hold the %s.", vehicle.getName(), item.getName()));
        return false;
    }
}
