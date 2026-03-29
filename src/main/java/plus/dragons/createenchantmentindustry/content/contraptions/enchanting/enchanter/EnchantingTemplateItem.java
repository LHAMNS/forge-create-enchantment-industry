package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Enchanting Template Item - a template that can be enchanted by the Blaze Enchanter
 * to produce "enchantment sigils" (templates with stored enchantments).
 * <p>
 * Uses the NBT tag "StoredEnchantments" (same as enchanted books) to store enchantments.
 * Comes in two variants: normal and special (super).
 * <p>
 * Ported from 1.21.1 EnchantingTemplateItem. Uses NBT instead of DataComponents.
 */
public class EnchantingTemplateItem extends Item {
    private final boolean special;

    public EnchantingTemplateItem(Properties properties, boolean special) {
        super(properties);
        this.special = special;
    }

    public static EnchantingTemplateItem normal(Properties properties) {
        return new EnchantingTemplateItem(properties, false);
    }

    public static EnchantingTemplateItem special(Properties properties) {
        return new EnchantingTemplateItem(properties, true);
    }

    public boolean isSpecial() {
        return special;
    }

    /**
     * Check if this template has stored enchantments.
     */
    public static boolean hasStoredEnchantments(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        return tag.contains("StoredEnchantments") && !tag.getList("StoredEnchantments", 10).isEmpty();
    }

    /**
     * Get stored enchantments from the template (same format as enchanted books).
     */
    public static Map<Enchantment, Integer> getStoredEnchantments(ItemStack stack) {
        return EnchantmentHelper.deserializeEnchantments(
                stack.getOrCreateTag().getList("StoredEnchantments", 10));
    }

    /**
     * Set stored enchantments on the template.
     */
    public static void setStoredEnchantments(Map<Enchantment, Integer> enchantments, ItemStack stack) {
        ListTag listTag = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            CompoundTag enchTag = new CompoundTag();
            enchTag.putString("id", EnchantmentHelper.getEnchantmentId(entry.getKey()).toString());
            enchTag.putShort("lvl", entry.getValue().shortValue());
            listTag.add(enchTag);
        }
        stack.getOrCreateTag().put("StoredEnchantments", listTag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasStoredEnchantments(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (hasStoredEnchantments(stack)) {
            Map<Enchantment, Integer> enchantments = getStoredEnchantments(stack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                tooltip.add(entry.getKey().getFullname(entry.getValue()));
            }
        }
    }
}
