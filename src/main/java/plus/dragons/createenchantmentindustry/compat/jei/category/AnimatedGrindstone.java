package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

public class AnimatedGrindstone implements IDrawable {

    public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
            .firstLightRotation(12.5f, 45.0f)
            .secondLightRotation(-20.0f, 50.0f)
            .build();

    private final BlockState grindstone = CeiBlocks.MECHANICAL_GRINDSTONE.getDefaultState()
            .setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Z);
    private final BlockState drain = CeiBlocks.GRINDSTONE_DRAIN.getDefaultState()
            .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.SOUTH);

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
        GuiGameElement.of(grindstone)
                .lighting(DEFAULT_LIGHTING)
                .atLocal(0, -1, 0)
                .scale(scale)
                .render(graphics);
        GuiGameElement.of(grindstone)
                .lighting(DEFAULT_LIGHTING)
                .scale(scale)
                .render(graphics);
        GuiGameElement.of(drain)
                .lighting(DEFAULT_LIGHTING)
                .scale(scale)
                .render(graphics);
        poseStack.popPose();
    }
}
