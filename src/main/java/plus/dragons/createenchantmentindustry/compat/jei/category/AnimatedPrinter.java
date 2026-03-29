package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

/**
 * Animated Printer drawable for JEI categories.
 * <p>
 * Ported from upstream AnimatedPrinter (1.21.1/6.0.0-dev).
 * Renders the printer block, depot, and animated nozzle/piston using
 * Create's PartialModel API. The nozzle animates up and down to simulate
 * the printing action.
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

        // Animate the nozzle squeeze (cycles over time)
        float animationTick = (System.currentTimeMillis() % 2000) / 2000f;
        float squeeze;
        if (animationTick < 0.1f) {
            squeeze = Mth.lerp(animationTick / 0.1f, 0, -1);
        } else if (animationTick < 0.5f) {
            squeeze = -1;
        } else if (animationTick < 0.6f) {
            squeeze = Mth.lerp((animationTick - 0.5f) / 0.1f, -1, 0);
        } else {
            squeeze = 0;
        }

        // Render printer base block
        GuiGameElement.of(printer)
                .lighting(DEFAULT_LIGHTING)
                .scale(scale)
                .render(graphics);

        // Render nozzle top and middle (animated squeeze)
        float nozzleOffset = -3 * squeeze / 32f;
        GuiGameElement.of(CeiBlockPartials.PRINTER_TOP)
                .lighting(DEFAULT_LIGHTING)
                .atLocal(0, nozzleOffset, 0)
                .scale(scale)
                .render(graphics);

        GuiGameElement.of(CeiBlockPartials.PRINTER_MIDDLE)
                .lighting(DEFAULT_LIGHTING)
                .atLocal(0, nozzleOffset * 2, 0)
                .scale(scale)
                .render(graphics);

        // Render nozzle bottom (piston, moves with squeeze)
        GuiGameElement.of(CeiBlockPartials.PRINTER_BOTTOM)
                .lighting(DEFAULT_LIGHTING)
                .atLocal(0, squeeze / 2f, 0)
                .scale(scale)
                .render(graphics);

        // Render depot below the printer
        GuiGameElement.of(depot)
                .lighting(DEFAULT_LIGHTING)
                .atLocal(0, 2, 0)
                .scale(scale)
                .render(graphics);

        poseStack.popPose();
    }
}
