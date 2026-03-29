package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;

/**
 * JEI sub-category for Grinding in Sequenced Assembly recipes.
 * Renders the animated grindstone and displays fluid ingredients.
 * <p>
 * Ported from upstream AssemblyGrindingCategory (1.21.1/6.0.0-dev).
 */
public class AssemblyGrindingCategory extends SequencedAssemblySubCategory {
    private final AnimatedGrindstone grindstone = new AnimatedGrindstone();

    public AssemblyGrindingCategory() {
        super(25);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
        // Fluid ingredients are handled by the parent category's rendering
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(-7, 48.5f, 0);
        poseStack.scale(.6f, .6f, .6f);
        grindstone.draw(graphics, getWidth() / 2, 30);
        poseStack.popPose();
    }
}
