package com.darkbladedev.events;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class AcidResistance extends Enchantment {

    public AcidResistance(NamespacedKey key) {
        super();
    }

    @Override
    public String getTranslationKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTranslationKey'");
    }

    @Override
    public NamespacedKey getKeyOrThrow() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeyOrThrow'");
    }

    @Override
    public NamespacedKey getKeyOrNull() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeyOrNull'");
    }

    @Override
    public boolean isRegistered() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isRegistered'");
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
        return EnchantmentTarget.ARMOR_HEAD;
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
        return item.getType().toString().endsWith("_HELMET"); // Aplicable a cascos
    }

    @Override
    public NamespacedKey getKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKey'");
    }

}
