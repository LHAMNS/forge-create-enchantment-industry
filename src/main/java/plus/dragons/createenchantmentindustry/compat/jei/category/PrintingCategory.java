package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PrintingCategory extends CreateRecipeCategory<PrintingRecipe> {

    public PrintingCategory(Info<PrintingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PrintingRecipe recipe, IFocusGroup focuses) {
        var ingredients = recipe.getIngredients();

        // Item input (belt item)
        if (!ingredients.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 27, 32)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(ingredients.get(0));
        }

        // Template input (placed on printer)
        if (ingredients.size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 27, 12)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(ingredients.get(1));
        }

        // Fluid input
        var fluidIngredients = recipe.getFluidIngredients();
        if (!fluidIngredients.isEmpty()) {
            var matchingFluids = fluidIngredients.get(0).getMatchingFluidStacks();
            if (!matchingFluids.isEmpty()) {
                addFluidSlot(builder, 27, 51, matchingFluids.get(0));
            }
        }

        // Output
        List<ProcessingOutput> results = recipe.getRollableResults();
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 131, 32)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

    @Override
    public void draw(PrintingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 47);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54, 36);
    }
}
