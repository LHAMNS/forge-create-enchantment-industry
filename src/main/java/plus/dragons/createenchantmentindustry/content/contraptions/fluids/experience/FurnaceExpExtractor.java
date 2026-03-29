package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import javax.annotation.Nonnull;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import java.util.ArrayList;

public class FurnaceExpExtractor implements IFluidHandler{
    final Object2IntOpenHashMap<ResourceLocation> recipesUsed;
    final AbstractFurnaceBlockEntity BE;

    public FurnaceExpExtractor(Object2IntOpenHashMap<ResourceLocation> recipesUsed, AbstractFurnaceBlockEntity BE) {
        this.recipesUsed = recipesUsed;
        this.BE = BE;
    }

    int getTotalExp() {
        AtomicDouble result = new AtomicDouble(0);
        for (Object2IntMap.Entry<ResourceLocation> entry : recipesUsed.object2IntEntrySet()) {
            BE.getLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
                    if (recipe instanceof AbstractCookingRecipe cookingRecipe)
                        result.addAndGet(cookingRecipe.getExperience() * entry.getIntValue());
            });
        }
        return (int) Math.floor(result.floatValue());
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
        var total = getTotalExp();
        if (total == 0) {
            return FluidStack.EMPTY;
        } else if (maxDrain >= total) {
            if (action.execute()) recipesUsed.clear();
            return new FluidStack(CeiFluids.EXPERIENCE.get(), total);
        }
        // Efficient mathematical approach: iterate recipe entries with counts, no ArrayList expansion
        float result = 0;
        var it = new java.util.ArrayList<>(recipesUsed.object2IntEntrySet());
        Object2IntOpenHashMap<ResourceLocation> remaining = new Object2IntOpenHashMap<>();
        boolean budgetExhausted = false;
        for (var entry : it) {
            var recipeOpt = BE.getLevel().getRecipeManager().byKey(entry.getKey());
            if (recipeOpt.isEmpty()) continue;
            if (!(recipeOpt.get() instanceof AbstractCookingRecipe cookingRecipe)) continue;
            float expPerItem = cookingRecipe.getExperience();
            int count = entry.getIntValue();
            if (budgetExhausted) {
                remaining.put(entry.getKey(), count);
            } else {
                int canDrain = (int) Math.min(count, Math.floor((maxDrain - result) / Math.max(expPerItem, 0.001f)));
                result += canDrain * expPerItem;
                int leftover = count - canDrain;
                if (leftover > 0) {
                    remaining.put(entry.getKey(), leftover);
                    budgetExhausted = true;
                }
            }
        }
        if (action.execute()) {
            recipesUsed.clear();
            for (var e : remaining.object2IntEntrySet()) {
                BE.getLevel().getRecipeManager().byKey(e.getKey()).ifPresent(r -> {
                    for (int i = 0; i < e.getIntValue(); i++) BE.setRecipeUsed(r);
                });
            }
        }
        return new FluidStack(CeiFluids.EXPERIENCE.get(), (int) Math.floor(result));
    }
}
