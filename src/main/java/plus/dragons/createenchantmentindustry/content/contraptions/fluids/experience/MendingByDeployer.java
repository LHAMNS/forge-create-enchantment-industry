package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nullable;

public class MendingByDeployer {
    
    public static boolean canItemBeMended(ItemStack stack) {
        return stack.isDamaged() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack) > 0;
    }
    
    public static int getRequiredAmountForItem(ItemStack stack) {
        return Mth.ceil(stack.getDamageValue() / stack.getXpRepairRatio());
    }
    public static int getNewXp(int xpAmount, ItemStack stack) {
        int requiredAmount = getRequiredAmountForItem(stack);
        return Math.max(0, xpAmount - requiredAmount);
    }
    @Nullable
    public static ItemStack mendItem(int xpAmount, ItemStack stack) {
        int damage = stack.getDamageValue();
        damage -= (int)(xpAmount * stack.getXpRepairRatio());
        stack.setDamageValue(Math.max(0, damage));
        return stack;
    }


}
