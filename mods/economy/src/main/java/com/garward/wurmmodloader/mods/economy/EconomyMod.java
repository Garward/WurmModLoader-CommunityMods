package com.garward.wurmmodloader.mods.economy;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.trade.ShopDiffEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modloader.internal.classhooks.HookManager;

import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Trade;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Properties;
import java.util.logging.Logger;

public class EconomyMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(EconomyMod.class.getName());

    boolean voidTraderMoney = true;
    boolean disableTraderRefill = true;

    @Override
    public void configure(Properties properties) {
        voidTraderMoney = bool(properties, "voidTraderMoney", voidTraderMoney);
        disableTraderRefill = bool(properties, "disableTraderRefill", disableTraderRefill);
    }

    @Override
    public void preInit() {
        if (disableTraderRefill) {
            installDisableTraderRefillPatch();
        }
    }

    @SubscribeEvent
    public void onShopDiff(ShopDiffEvent event) {
        if (!voidTraderMoney) {
            return;
        }
        Trade trade = event.getTrade();
        Shop shop = null;
        if (trade.creatureOne.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(trade.creatureOne);
        }
        if (trade.creatureTwo.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(trade.creatureTwo);
        }
        if (shop == null || shop.isPersonal()) {
            return;
        }
        long money = event.getMoney();
        long shopDiff = event.getCurrentShopDiff();
        if (money > 0 && money + shopDiff > 0) {
            long newDiff = (long) ((money + shopDiff) * 0.2);
            event.setMoney(-shopDiff + newDiff);
        }
    }

    private void installDisableTraderRefillPatch() {
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            if (ctCreature.isFrozen()) {
                ctCreature.defrost();
            }
            CtMethod method = ctCreature.getDeclaredMethod("removeRandomItems");
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws javassist.CannotCompileException {
                    if ("nextInt".equals(m.getMethodName())) {
                        m.replace("$_ = 1;");
                    }
                }
            });
            logger.info("[economy] disableTraderRefill: Creature.removeRandomItems patched");
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE,
                "[economy] failed to install disableTraderRefill patch", e);
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
