package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.GrindingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class GrindingCategory extends CreateRecipeCategory<GrindingRecipe> {

    private final IDrawable grindstone = new AnimatedGrindstone();

    public GrindingCategory(Info<GrindingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        var ingredient = recipe.getIngredients().get(0);
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 32)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(ingredient);

        var fluidIngredients = recipe.getFluidIngredients();
        if (!fluidIngredients.isEmpty()) {
            var matchingFluids = fluidIngredients.get(0).getMatchingFluidStacks();
            if (!matchingFluids.isEmpty()) {
                addFluidSlot(builder, 27, 51, matchingFluids.get(0));
            }
        }

        List<ProcessingOutput> results = recipe.getRollableResults();
        int i = 0;
        var fluidResults = recipe.getFluidResults();
        if (!fluidResults.isEmpty()) {
            addFluidSlot(builder, 130, 32, fluidResults.get(0));
            i++;
        }
        for (ProcessingOutput output : results) {
            int xOffset = i % 2 == 0 ? 0 : 19;
            int yOffset = (i / 2) * -19;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 130 + xOffset, 32 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            i++;
        }
    }

    @Override
    public void draw(GrindingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 115, 5);
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 52);
        grindstone.draw(graphics, 68, 32);
    }
}
