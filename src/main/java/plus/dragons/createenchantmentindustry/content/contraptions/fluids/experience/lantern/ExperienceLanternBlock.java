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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;

public class ExperienceLanternBlock extends WrenchableDirectionalBlock implements IBE<ExperienceLanternBlockEntity> {
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    // Shape: base plate + center column
    private static final VoxelShape SHAPE_DOWN = Block.box(1, 0, 1, 15, 4, 15);

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
        // Use a simple shape; a proper VoxelShaper would need CEIShapes setup
        return SHAPE_DOWN;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIGHT);
    }
}
