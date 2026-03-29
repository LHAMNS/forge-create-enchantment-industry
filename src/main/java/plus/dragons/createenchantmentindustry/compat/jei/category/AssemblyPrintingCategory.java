package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

/**
 * JEI sub-category for Printing in Sequenced Assembly recipes.
 * Displays the input ingredient and fluid ingredient slots.
 * <p>
 * Ported from upstream AssemblyPrintingCategory (1.21.1/6.0.0-dev).
 */
public class AssemblyPrintingCategory extends SequencedAssemblySubCategory {

    public AssemblyPrintingCategory() {
        super(36);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
        var ingredients = recipe.getRecipe().getIngredients();
        if (ingredients.size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, 15)
                    .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                    .addIngredients(ingredients.get(1));
        }
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(-7, 50, 0);
        poseStack.scale(.75f, .75f, .75f);
        // Draw the printer block icon for visual reference
        var printer = new AnimatedPrinter();
        printer.draw(graphics, getWidth() / 2, 0);
        poseStack.popPose();
    }
}
