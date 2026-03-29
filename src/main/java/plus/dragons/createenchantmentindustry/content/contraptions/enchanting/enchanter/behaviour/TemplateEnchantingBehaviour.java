package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.behaviour;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantmentEntry;
import plus.dragons.createenchantmentindustry.entry.CeiTags;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Template-based enchanting behaviour. When a template item (non-EnchantingTemplateItem)
 * is placed as the template, the Blaze Enchanter will enchant blank EnchantingTemplateItems
 * based on the template's enchantability.
 * <p>
 * The result is an EnchantingTemplateItem with stored enchantments determined by the
 * template item's properties and the enchanting level setting.
 * <p>
 * Ported from 1.21.1's TemplateEnchantingBehaviour. Adapted for 1.20.1's enchantment system.
 */
public class TemplateEnchantingBehaviour extends EnchantingBehaviour {
    private final ItemStack templateTarget;

    public TemplateEnchantingBehaviour(ItemStack templateTarget) {
        this.templateTarget = templateTarget;
    }

    public ItemStack getTemplateTarget() {
        return templateTarget;
    }

    @Override
    public boolean canProcess(ItemStack stack, ItemStack targetItem, boolean hyper) {
        if (!(stack.getItem() instanceof EnchantingTemplateItem template))
            return false;
        // Only process blank templates (no stored enchantments yet)
        if (EnchantingTemplateItem.hasStoredEnchantments(stack))
            return false;
        // Normal templates with normal enchanter, special templates with hyper enchanter
        if (!hyper && template.isSpecial())
            return false;
        if (hyper && !template.isSpecial())
            return false;
        // Check that the template target can actually produce enchantments
        return !getAvailableEnchantments(hyper).isEmpty();
    }

    @Override
    @Nullable
    public EnchantmentEntry getEnchantmentEntry(ItemStack stack, ItemStack targetItem, boolean hyper) {
        // Not used in template mode - we use applyEnchantment directly
        return null;
    }

    @Override
    public void applyEnchantment(ItemStack stack, ItemStack targetItem, boolean hyper) {
        List<EnchantmentInstance> available = getAvailableEnchantments(hyper);
        if (available.isEmpty()) return;

        Random random = new Random();
        // Select a random enchantment from available ones
        EnchantmentInstance selected = available.get(random.nextInt(available.size()));

        Map<Enchantment, Integer> stored = new java.util.LinkedHashMap<>();
        stored.put(selected.enchantment, selected.level);
        EnchantingTemplateItem.setStoredEnchantments(stored, stack);
    }

    @Override
    public int getExperienceCost(ItemStack targetItem, boolean hyper) {
        List<EnchantmentInstance> available = getAvailableEnchantments(hyper);
        if (available.isEmpty()) return 0;
        // Average cost of available enchantments
        int totalCost = 0;
        for (EnchantmentInstance inst : available) {
            totalCost += Enchanting.getExperienceConsumption(inst.enchantment, inst.level);
        }
        return totalCost / available.size();
    }

    /**
     * Get enchantments that can be applied to the template target item at the current level.
     */
    private List<EnchantmentInstance> getAvailableEnchantments(boolean hyper) {
        List<EnchantmentInstance> result = new ArrayList<>();
        if (templateTarget.isEmpty()) return result;

        // Get all enchantments that can be applied to the template target,
        // filtered by the enchantment tag system
        for (Enchantment enchantment : net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS) {
            // Check enchantment tag availability
            if (hyper) {
                if (!CeiTags.isAvailableForSuperEnchanting(enchantment))
                    continue;
            } else {
                if (!CeiTags.isAvailableForNormalEnchanting(enchantment))
                    continue;
            }
            if (enchantment.canEnchant(templateTarget) || enchantment.category.canEnchant(templateTarget.getItem())) {
                int maxLevel = enchantment.getMaxLevel();
                // In hyper mode, we can go 1 level higher
                if (hyper) maxLevel++;
                for (int level = maxLevel; level >= enchantment.getMinLevel(); level--) {
                    result.add(new EnchantmentInstance(enchantment, level));
                    break; // Only add highest available level
                }
            }
        }
        return result;
    }

    /**
     * Simple holder for an enchantment + level pair used internally.
     */
    private record EnchantmentInstance(Enchantment enchantment, int level) {}
}
