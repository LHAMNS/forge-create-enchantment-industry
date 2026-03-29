package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.compat.jei.category.AssemblyGrindingCategory;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class GrindingRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {
    public GrindingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CeiRecipeTypes.GRINDING, params);
        if (fluidIngredients.size() + fluidResults.size() > 1)
            throw new IllegalArgumentException("Grinding recipe can only have either 1 fluid input or 1 fluid result");
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level level) {
        return ingredients.get(0).test(inv.getItem(0));
    }

    @Override
    public Component getDescriptionForAssembly() {
        if (fluidIngredients.isEmpty()) {
            return Component.translatable("create_enchantment_industry.recipe.assembly.grinding");
        } else {
            List<FluidStack> matchingFluidStacks = fluidIngredients.get(0).getMatchingFluidStacks();
            if (matchingFluidStacks.isEmpty()) {
                return Component.literal("Invalid");
            }
            return Component.translatable("create_enchantment_industry.recipe.assembly.grinding.needs_fluid",
                    matchingFluidStacks.get(0).getDisplayName());
        }
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CeiBlocks.MECHANICAL_GRINDSTONE.get());
        required.add(AllBlocks.ITEM_DRAIN.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {}

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> AssemblyGrindingCategory::new;
    }

    /**
     * Convert a SandPaperPolishingRecipe to a GrindingRecipe for JEI display.
     * The mechanical grindstone can perform all automatable sandpaper polishing recipes.
     */
    public static Optional<GrindingRecipe> fromPolishing(SandPaperPolishingRecipe recipe) {
        if (AllRecipeTypes.CAN_BE_AUTOMATED.test(recipe)) {
            ResourceLocation id = recipe.getId();
            ResourceLocation grindingId = new ResourceLocation(id.getNamespace(), id.getPath() + "_using_grindstone");
            var builder = new ProcessingRecipeBuilder<>(GrindingRecipe::new, grindingId);
            if (!recipe.getIngredients().isEmpty()) {
                builder.require(recipe.getIngredients().get(0));
            }
            if (!recipe.getRollableResults().isEmpty()) {
                builder.output(recipe.getRollableResults().get(0));
            }
            return Optional.of(builder.build());
        }
        return Optional.empty();
    }
}
