package com.darkbladedev.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class AcidResistance extends Enchantment {
    
    private final NamespacedKey key;

    public AcidResistance(NamespacedKey key) {
        this.key = key;
    }

    @Override
    public String getName() {
        return "Acid Resistance";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        String type = item.getType().toString();
        return type.endsWith("_HELMET") || 
               type.endsWith("_CHESTPLATE") || 
               type.endsWith("_LEGGINGS") || 
               type.endsWith("_BOOTS");
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public String getTranslationKey() {
        return "enchantment.acid_resistance";
        }

    @Override
    public NamespacedKey getKeyOrNull() {
        return key;
    }

    @Override
    public NamespacedKey getKeyOrThrow() {
        return key;
    }

    @Override
    public boolean isRegistered() {
        return true;
    }
}