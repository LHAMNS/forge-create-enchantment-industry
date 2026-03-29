package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

/**
 * Animated Printer drawable for JEI categories.
 * <p>
 * Ported from upstream AnimatedPrinter (1.21.1/6.0.0-dev).
 * Simplified for Forge 1.20.1 - renders the printer block and depot without
 * the animated nozzle/piston (which requires partial models not available in
 * the Forge version's rendering pipeline).
 */
public class AnimatedPrinter implements IDrawable {

    public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
            .firstLightRotation(12.5f, 45.0f)
            .secondLightRotation(-20.0f, 50.0f)
            .build();

    private final BlockState printer = CeiBlocks.PRINTER.getDefaultState();
    private final BlockState depot = AllBlocks.DEPOT.getDefaultState();

    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset + 30, 100);
        poseStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        poseStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 20;

        GuiGameElement.of(printer)
                .lighting(DEFAULT_LIGHTING)
                .scale(scale)
                .render(graphics);

        GuiGameElement.of(depot)
                .lighting(DEFAULT_LIGHTING)
                .atLocal(0, 2, 0)
                .scale(scale)
                .render(graphics);

        poseStack.popPose();
    }
}
