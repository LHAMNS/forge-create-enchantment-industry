package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import net.createmod.catnip.data.Pair;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

public class Disenchanting {

    public static ItemStack disenchantAndInsert(DisenchanterBlockEntity be, ItemStack itemStack, boolean simulate) {
        Level level = be.getLevel();
        if (level == null)
            return itemStack;
        RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));
        wrapper.setItem(0, itemStack);
        return CeiRecipeTypes.DISENCHANTING.<RecipeWrapper, DisenchantRecipe>find(wrapper, be.getLevel())
                .map(recipe -> {
                    if (!recipe.hasNoResult())
                        return itemStack;
                    var tank = be.getInternalTank();
                    int amount = recipe.getExperience();
                    var fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), itemStack.getCount() * amount);
                    int inserted;
                    tank.allowInsertion();
                    try {
                        inserted = tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) / amount;
                    } finally {
                        tank.forbidInsertion();
                    }
                    ItemStack ret = itemStack.copy();
                    if (!simulate) {
                        tank.allowInsertion();
                        try {
                            fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), inserted * amount);
                            tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        } finally {
                            tank.forbidInsertion();
                        }
                    }
                    ret.shrink(inserted);
                    return ret;
                }).orElse(itemStack);
    }

    // Produce result only. Do not modify stack.
    // stack always has count of 1.
    @Nullable
    public static Pair<FluidStack, ItemStack> disenchantResult(ItemStack itemStack, Level level) {
        if (EnchantmentHelper.getEnchantments(itemStack).keySet().stream().anyMatch(enchantment -> !enchantment.isCurse())) {
            var xp =
                    new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), getDisenchantExperience(itemStack));
            ItemStack result = disenchant(itemStack);
            return Pair.of(xp, result);
        }
        RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));
        wrapper.setItem(0, itemStack);
        var recipe = CeiRecipeTypes.DISENCHANTING.<RecipeWrapper, DisenchantRecipe>find(wrapper, level).orElse(null);
        if (recipe != null && !recipe.hasNoResult()) {
            var xp = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), recipe.getExperience());
            var result = recipe.getResultItem(level.registryAccess()).copy();
            return Pair.of(xp, result);
        }
        return null;
    }

    public static ItemStack disenchant(ItemStack itemStack) {
        ItemStack result = itemStack.copy();
        result.removeTagKey("Enchantments");
        result.removeTagKey("StoredEnchantments");
        Map<Enchantment, Integer> curses = EnchantmentHelper.getEnchantments(itemStack)
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().isCurse())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (result.is(Items.ENCHANTED_BOOK) && curses.isEmpty()) {
            result = new ItemStack(Items.BOOK);
            var oldTag = itemStack.getTag();
            if (oldTag != null && oldTag.contains("display")) {
                result.getOrCreateTag().put("display", oldTag.getCompound("display").copy());
            }
        } else {
            EnchantmentHelper.setEnchantments(curses, result);
            result.setRepairCost(0);
            for (int i = 0; i < curses.size(); ++i) {
                result.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(result.getBaseRepairCost()));
            }
        }
        return result;
    }

    private static int getDisenchantExperience(ItemStack itemStack) {
        int xp = EnchantmentHelper.getEnchantments(itemStack)
                .entrySet().stream()
                .filter(entry -> !entry.getKey().isCurse())
                .map(entry -> entry.getKey().getMinCost(entry.getValue()))
                .reduce(0, Integer::sum);
        return xp == 0 ? 0 : Mth.ceil(xp * 0.75);
    }

}
