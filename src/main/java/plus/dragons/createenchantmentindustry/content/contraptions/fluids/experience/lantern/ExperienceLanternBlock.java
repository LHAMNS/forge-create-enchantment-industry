package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;

public class ExperienceLanternBlock extends WrenchableDirectionalBlock implements IBE<ExperienceLanternBlockEntity> {
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    // Directional shapes for the lantern
    private static final VoxelShape SHAPE_DOWN  = Block.box(1, 0, 1, 15, 4, 15);
    private static final VoxelShape SHAPE_UP    = Block.box(1, 12, 1, 15, 16, 15);
    private static final VoxelShape SHAPE_NORTH = Block.box(1, 1, 0, 15, 15, 4);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 1, 12, 15, 15, 16);
    private static final VoxelShape SHAPE_WEST  = Block.box(0, 1, 1, 4, 15, 15);
    private static final VoxelShape SHAPE_EAST  = Block.box(12, 1, 1, 16, 15, 15);

    public ExperienceLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        IBE.onRemove(pState, pLevel, pPos, pNewState);
    }

    @Override
    public Class<ExperienceLanternBlockEntity> getBlockEntityClass() {
        return ExperienceLanternBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExperienceLanternBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.EXPERIENCE_LANTERN.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_DOWN;
        };
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIGHT);
    }
}
