package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

import javax.annotation.Nullable;

public class ExperienceHatchBlock extends HorizontalDirectionalBlock
        implements IBE<ExperienceHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {

    public ExperienceHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        if (context.getClickedFace().getAxis().isVertical())
            return null;
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND)
            return InteractionResult.PASS;

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (player instanceof FakePlayer)
            return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING)));
        if (blockEntity == null)
            return InteractionResult.PASS;

        IFluidHandler tankCapability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (tankCapability == null)
            return InteractionResult.PASS;

        ExperienceHatchBehaviour filter = BlockEntityBehaviour.get(level, pos, ExperienceHatchBehaviour.TYPE);
        if (filter == null)
            return InteractionResult.PASS;

        if (player.isSecondaryUseActive()) {
            // Drain: pull experience from the tank and give to player
            FluidStack fluid = filter.getFluidToDrain();
            fluid = tankCapability.drain(fluid, FluidAction.EXECUTE);
            if (fluid.isEmpty())
                return InteractionResult.PASS;
            blockEntity.setChanged();
            if (level instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
            int experience = ExperienceHelper.getExperienceFromFluid(fluid);
            player.giveExperiencePoints(experience);
            CeiAdvancements.SPIRIT_TAKING.getTrigger().trigger((ServerPlayer) player);
            return InteractionResult.SUCCESS;
        } else {
            // Fill: take experience from player and put into the tank
            int experience = ExperienceHelper.getExperienceForPlayer(player);
            FluidStack fluid = filter.getFluidToFill(experience);
            int filled = tankCapability.fill(fluid, FluidAction.EXECUTE);
            if (filled == 0)
                return InteractionResult.PASS;
            blockEntity.setChanged();
            if (level instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
            int xpToDeduct = Math.min(ExperienceHelper.getExperienceFromFluid(new FluidStack(fluid.getFluid(), filled)), experience);
            player.giveExperiencePoints(-xpToDeduct);
            CeiAdvancements.SPIRIT_TAKING.getTrigger().trigger((ServerPlayer) player);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<ExperienceHatchBlockEntity> getBlockEntityClass() {
        return ExperienceHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExperienceHatchBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.EXPERIENCE_HATCH.get();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
