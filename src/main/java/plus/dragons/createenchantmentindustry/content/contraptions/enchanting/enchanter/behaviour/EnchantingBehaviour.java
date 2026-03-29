package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.behaviour;

import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantmentEntry;

import javax.annotation.Nullable;

/**
 * Default enchanting behaviour using the EnchantingGuide system.
 * This is the existing behaviour that was always present in the 1.20.1 codebase.
 * <p>
 * Ported from 1.21.1's EnchantingBehaviour strategy pattern.
 */
public class EnchantingBehaviour {

    /**
     * Check if the given item can be processed by this enchanting behaviour.
     */
    public boolean canProcess(ItemStack stack, ItemStack targetItem, boolean hyper) {
        if (stack.getItem() instanceof EnchantingTemplateItem)
            return false;
        return Enchanting.getValidEnchantment(stack, targetItem, hyper) != null;
    }

    /**
     * Get the enchantment entry to apply.
     */
    @Nullable
    public EnchantmentEntry getEnchantmentEntry(ItemStack stack, ItemStack targetItem, boolean hyper) {
        return Enchanting.getValidEnchantment(stack, targetItem, hyper);
    }

    /**
     * Apply the enchantment to the item.
     */
    public void applyEnchantment(ItemStack stack, ItemStack targetItem, boolean hyper) {
        Pair<Enchantment, Integer> entry = Enchanting.getValidEnchantment(stack, targetItem, hyper);
        if (entry != null) {
            Enchanting.enchantItem(stack, entry);
        }
    }

    /**
     * Get the experience cost in fluid mB.
     */
    public int getExperienceCost(ItemStack targetItem, boolean hyper) {
        var entry = Enchanting.getTargetEnchantment(targetItem, hyper);
        if (entry == null || !entry.valid())
            return 0;
        return Enchanting.getExperienceConsumption(entry.getFirst(), entry.getSecond());
    }
}
