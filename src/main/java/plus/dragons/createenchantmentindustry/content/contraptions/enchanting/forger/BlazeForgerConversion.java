package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

/**
 * Handles conversion of a Blaze Burner into a Blaze Forger.
 * <p>
 * Shift+right-clicking a Blaze Burner with an Anvil converts it into a Blaze Forger,
 * consuming the anvil in the process (unless player is in creative mode).
 */
public class BlazeForgerConversion {

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        if (!player.isShiftKeyDown())
            return;
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!heldItem.is(Items.ANVIL))
            return;

        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        var blockState = level.getBlockState(blockPos);
        var blockEntity = level.getBlockEntity(blockPos);

        if (!(blockState.getBlock() instanceof BlazeBurnerBlock) ||
                !(blockEntity instanceof BlazeBurnerBlockEntity))
            return;

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(blockPos, CeiBlocks.BLAZE_FORGER.getDefaultState()
                    .setValue(BlazeForgerBlock.FACING, blockState.getValue(BlazeBurnerBlock.FACING))
            );
            AdvancementBehaviour.setPlacedBy(level, blockPos, player);
            if (!player.getAbilities().instabuild)
                heldItem.shrink(1);
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
