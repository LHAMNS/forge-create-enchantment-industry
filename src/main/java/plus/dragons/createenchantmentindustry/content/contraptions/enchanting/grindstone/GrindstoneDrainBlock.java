package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import javax.annotation.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;

import java.util.List;

public class GrindstoneDrainBlock extends HorizontalKineticBlock implements IBE<GrindstoneDrainBlockEntity>, SpecialBlockItemRequirement {
    protected static VoxelShape SHAPE = new AllShapes.Builder(AllShapes.CASING_13PX.get(Direction.UP))
            .add(3, 3, 3, 13, 13, 13)
            .build();
    final MechanicalGrindstoneBlock grindstone;

    public GrindstoneDrainBlock(MechanicalGrindstoneBlock grindstone, Properties properties) {
        super(properties);
        this.grindstone = grindstone;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP)
            return this.grindstone.use(level.getBlockState(pos.above()), level, pos.above(), player, hand, hitResult);
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityAfterFallOn(worldIn, entityIn);

        if (entityIn.level().isClientSide)
            return;
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;
        GrindstoneDrainBlockEntity drain = getBlockEntity(worldIn, entityIn.blockPosition());
        if (drain == null)
            return;

        var cap = drain.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (!cap.isPresent())
            return;

        IItemHandler handler = cap.orElseThrow(IllegalStateException::new);
        ItemStack remainder = handler.insertItem(0, itemEntity.getItem(), false);
        if (remainder.isEmpty())
            itemEntity.discard();
        if (remainder.getCount() < itemEntity.getItem().getCount())
            itemEntity.setItem(remainder);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING) == face;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(worldIn, pos, placer);
    }

    @Override
    public @Nullable Direction getPreferredHorizontalFacing(BlockPlaceContext context) {
        Direction prefferedSide = super.getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return prefferedSide;

        for (Direction facing : Iterate.horizontalDirections) {
            BlockPos pos = context.getClickedPos().relative(facing);
            BlockState blockState = context.getLevel().getBlockState(pos);
            if (FluidPipeBlock.canConnectTo(context.getLevel(), pos, blockState, facing))
                if (prefferedSide != null && prefferedSide.getAxis() != facing.getAxis()) {
                    prefferedSide = null;
                    break;
                } else
                    prefferedSide = facing;
        }
        return prefferedSide == null ? null : prefferedSide.getOpposite();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hitResult, BlockGetter level, BlockPos pos, Player player) {
        return AllBlocks.ITEM_DRAIN.asStack();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<GrindstoneDrainBlockEntity> getBlockEntityClass() {
        return GrindstoneDrainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GrindstoneDrainBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.GRINDSTONE_DRAIN.get();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity) {
        return new ItemRequirement(List.of(
                new ItemRequirement.StackRequirement(new ItemStack(grindstone), ItemRequirement.ItemUseType.CONSUME),
                new ItemRequirement.StackRequirement(AllBlocks.ITEM_DRAIN.asStack(), ItemRequirement.ItemUseType.CONSUME)));
    }
}
