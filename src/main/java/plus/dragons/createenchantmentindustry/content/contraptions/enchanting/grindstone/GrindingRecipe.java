package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
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
        // TODO: Implement AssemblyGrindingCategory if JEI integration is needed
        return () -> () -> new SequencedAssemblySubCategory(25) {
            @Override
            public void draw(com.simibubi.create.content.processing.sequenced.SequencedRecipe<?> recipe, net.minecraft.client.gui.GuiGraphics graphics, double mouseX, double mouseY, int index) {
                // Placeholder - override with actual rendering when JEI category is implemented
            }
        };
    }

    public static Optional<GrindingRecipe> fromPolishing(SandPaperPolishingRecipe recipe) {
        if (AllRecipeTypes.CAN_BE_AUTOMATED.test(recipe)) {
            // The conversion from polishing to grinding can be done at JEI integration level
            return Optional.empty();
        }
        return Optional.empty();
    }
}
