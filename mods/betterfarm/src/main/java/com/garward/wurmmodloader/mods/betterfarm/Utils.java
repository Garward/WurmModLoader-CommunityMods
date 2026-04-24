package com.garward.wurmmodloader.mods.betterfarm;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

public class Utils {
    public static Item getExistingProduce(Item container, int template) {
        for (Item item : container.getAllItems(true)) {
            if (item.getTemplateId() == template && item.getAuxData() == 0 && item.getRarity() == 0)
                return item;
        }
        return null;
    }

    public static void addStackedItems(Item container, int template, float ql, float amount, String name) {
        Item existing = getExistingProduce(container, template);
        if (existing != null) {
            int addWeight = (int) (amount * existing.getTemplate().getWeightGrams());
            int sumWeight = existing.getWeightGrams() + addWeight;
            float sumQl = (existing.getQualityLevel() * existing.getWeightGrams() / sumWeight) + (ql * addWeight / sumWeight);
            existing.setWeight(sumWeight, false);
            existing.setQualityLevel(sumQl);
            if (!existing.getName().contains("pile of"))
                existing.setName("pile of " + name);
            existing.sendUpdate();
        } else {
            try {
                Item result = ItemFactory.createItem(template, ql, null);
                if (amount > 1) {
                    result.setWeight((int) (result.getTemplate().getWeightGrams() * amount), false);
                    result.setName("pile of " + name);
                }
                container.insertItem(result, true, false);
            } catch (FailedException | NoSuchTemplateException e) {
                BetterFarmMod.logException("Error creating stacked item", e);
            }
        }
    }
}
