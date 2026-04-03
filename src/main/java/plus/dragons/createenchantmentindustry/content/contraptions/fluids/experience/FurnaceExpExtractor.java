package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.mixin.FurnaceXpRemainderAccessor;

public class FurnaceExpExtractor implements IFluidHandler{
    final Object2IntOpenHashMap<ResourceLocation> recipesUsed;
    final AbstractFurnaceBlockEntity BE;

    public FurnaceExpExtractor(Object2IntOpenHashMap<ResourceLocation> recipesUsed, AbstractFurnaceBlockEntity BE) {
        this.recipesUsed = recipesUsed;
        this.BE = BE;
    }

    private double getRemainder() {
        if (BE instanceof FurnaceXpRemainderAccessor accessor) {
            return accessor.cei$getXpRemainder();
        }
        return 0.0;
    }

    private void setRemainder(double remainder) {
        if (BE instanceof FurnaceXpRemainderAccessor accessor) {
            accessor.cei$setXpRemainder(remainder);
        }
    }

    int getTotalExp() {
        return getTotalExp(BE.getLevel());
    }

    int getTotalExp(@Nullable Level level) {
        if (level == null) {
            return 0;
        }
        RecipeManager recipeManager = level.getRecipeManager();
        AtomicDouble result = new AtomicDouble(0);
        for (Object2IntMap.Entry<ResourceLocation> entry : recipesUsed.object2IntEntrySet()) {
            recipeManager.byKey(entry.getKey()).ifPresent(recipe -> {
                    if (recipe instanceof AbstractCookingRecipe cookingRecipe)
                        result.addAndGet(cookingRecipe.getExperience() * entry.getIntValue());
            });
        }
        result.addAndGet(getRemainder());
        return (int) Math.floor(result.doubleValue());
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @Nonnull FluidStack getFluidInTank(int tank) {
        var total = getTotalExp();
        if (total > 0) return new FluidStack(CeiFluids.EXPERIENCE.get(), total);
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return getTotalExp();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        // Furnace only outputs base EXPERIENCE, so only that fluid is valid here
        return stack.getFluid().isSame(CeiFluids.EXPERIENCE.get()) || CeiDataMaps.isXpFluid(stack);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        return 0;
    }

    @Override
    public @Nonnull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (getTotalExp() == 0) {
            return FluidStack.EMPTY;
        } else if (resource.getFluid().isSame(CeiFluids.EXPERIENCE.get())) {
            var maxDrain = resource.getAmount();
            return drain(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @Nonnull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        Level level = BE.getLevel();
        if (level == null) {
            return FluidStack.EMPTY;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        // Compute exact available XP including remainder
        double remainder = getRemainder();
        double exactTotal = remainder;
        for (var entry : recipesUsed.object2IntEntrySet()) {
            var recipeOpt = recipeManager.byKey(entry.getKey());
            if (recipeOpt.isEmpty()) continue;
            if (!(recipeOpt.get() instanceof AbstractCookingRecipe cookingRecipe)) continue;
            exactTotal += cookingRecipe.getExperience() * entry.getIntValue();
        }

        int total = (int) Math.floor(exactTotal);
        if (total == 0) {
            return FluidStack.EMPTY;
        }

        if (maxDrain >= total) {
            // Full drain
            int fluidAmount = total;
            if (action.execute()) {
                double newRemainder = exactTotal - fluidAmount;
                recipesUsed.clear();
                setRemainder(newRemainder);
            }
            return new FluidStack(CeiFluids.EXPERIENCE.get(), fluidAmount);
        }

        // Partial drain: consume recipe entries up to maxDrain
        double consumed = 0;
        Object2IntOpenHashMap<ResourceLocation> remaining = new Object2IntOpenHashMap<>();
        boolean budgetExhausted = false;
        double exactConsumedFromRecipes = 0;

        for (var entry : recipesUsed.object2IntEntrySet()) {
            var recipeOpt = recipeManager.byKey(entry.getKey());
            if (recipeOpt.isEmpty()) continue;
            if (!(recipeOpt.get() instanceof AbstractCookingRecipe cookingRecipe)) continue;
            double expPerItem = cookingRecipe.getExperience();
            int count = entry.getIntValue();
            if (budgetExhausted) {
                remaining.put(entry.getKey(), count);
            } else if (expPerItem <= 0) {
                // Zero-XP recipes: skip but preserve
                remaining.put(entry.getKey(), count);
            } else {
                // Include remainder in the budget for consuming
                int canDrain = (int) Math.min(count, Math.floor((maxDrain - consumed) / expPerItem));
                consumed += canDrain * expPerItem;
                exactConsumedFromRecipes += canDrain * expPerItem;
                int leftover = count - canDrain;
                if (leftover > 0) {
                    remaining.put(entry.getKey(), leftover);
                    budgetExhausted = true;
                }
            }
        }

        // The fluid amount we return is the floored total consumed (recipes + old remainder)
        // but capped to maxDrain
        double exactAvailableFromConsumed = exactConsumedFromRecipes + remainder;
        int fluidAmount = (int) Math.min(maxDrain, Math.floor(exactAvailableFromConsumed));

        // Guard: if the floored result is 0, don't consume any recipe entries.
        if (fluidAmount == 0) {
            return FluidStack.EMPTY;
        }

        if (action.execute()) {
            double newRemainder = exactAvailableFromConsumed - fluidAmount;
            recipesUsed.clear();
            for (var e : remaining.object2IntEntrySet()) {
                recipeManager.byKey(e.getKey()).ifPresent(r -> {
                    for (int i = 0; i < e.getIntValue(); i++) BE.setRecipeUsed(r);
                });
            }
            setRemainder(newRemainder);
        }
        return new FluidStack(CeiFluids.EXPERIENCE.get(), fluidAmount);
    }
}
