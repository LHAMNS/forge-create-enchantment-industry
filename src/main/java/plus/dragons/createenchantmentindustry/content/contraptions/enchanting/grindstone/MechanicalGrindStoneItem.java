package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

public class MechanicalGrindStoneItem extends BlockItem {
    public MechanicalGrindStoneItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.place(new PlaceContext(context));
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        if (context instanceof PlaceContext placeContext)
            return placeContext.getPlacementState();
        return super.getPlacementState(context);
    }

    public class PlaceContext extends BlockPlaceContext {
        private final boolean clickedDrain;

        public PlaceContext(UseOnContext context) {
            super(context);
            var clickedPos = context.getClickedPos();
            var clickedState = context.getLevel().getBlockState(clickedPos);
            this.clickedDrain = clickedState.is(AllBlocks.ITEM_DRAIN.get());
            this.replaceClicked |= this.clickedDrain;
        }

        @Nullable
        public BlockState getPlacementState() {
            if (clickedDrain) {
                var facing = getHorizontalDirection().getOpposite();
                return CeiBlocks.GRINDSTONE_DRAIN.getDefaultState()
                        .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, facing);
            }
            return MechanicalGrindStoneItem.super.getPlacementState(this);
        }
    }
}
