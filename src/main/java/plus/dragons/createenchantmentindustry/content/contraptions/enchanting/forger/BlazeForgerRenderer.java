package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Renderer for the Blaze Forger block entity.
 * Renders items from inventory slots 0-3 (inputs and outputs) floating
 * above the block with vertical bobbing and horizontal rotation.
 * <p>
 * Ported from upstream BlazeForgerRenderer, adapted to Forge 1.20.1
 * rendering API patterns (SmartBlockEntityRenderer, AnimationTickHolder.getRenderTime).
 */
public class BlazeForgerRenderer extends SmartBlockEntityRenderer<BlazeForgerBlockEntity> {

    public BlazeForgerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BlazeForgerBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                              MultiBufferSource bufferSource, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);

        for (int slot = 0; slot < 4; slot++) {
            ItemStack item = blockEntity.inventory.getStackInSlot(slot);
            if (item.isEmpty())
                continue;
            renderItem(blockEntity, item, slot, blockEntity.processingTime, partialTicks,
                    poseStack, bufferSource, light, overlay);
        }
    }

    protected void renderItem(BlazeForgerBlockEntity blockEntity, ItemStack item, int slot,
                              int processingTime, float partialTicks, PoseStack poseStack,
                              MultiBufferSource bufferSource, int light, int overlay) {
        Level level = blockEntity.getLevel();
        if (level == null)
            return;

        var blockPos = blockEntity.getBlockPos();
        float renderTicks = AnimationTickHolder.getRenderTime(level);

        // Vertical bobbing animation: idle uses a static per-slot offset,
        // processing uses a time-varying sine wave
        float animation = processingTime == -1
                ? Mth.sin(slot * Mth.PI / -2f)
                : Mth.sin((processingTime + partialTicks) / 20f + slot * Mth.PI);
        float height = 1.25f + (1 + animation) * .25f;

        // Horizontal rotation around X and Z axes, offset per slot so items don't overlap
        float xRot = (renderTicks * 5 + blockPos.getX() + slot * 180) % 360;
        float zRot = (renderTicks * 5 + blockPos.getZ() + slot * 180) % 360;

        poseStack.pushPose();
        poseStack.translate(.5f, height, .5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
        poseStack.scale(.5f, .5f, .5f);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(item, ItemDisplayContext.FIXED, light, overlay,
                poseStack, bufferSource, level, blockEntity.hashCode());
        poseStack.popPose();
    }
}
