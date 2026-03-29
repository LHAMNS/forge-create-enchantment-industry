package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.ComparatorUtil;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlazeForgerBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<BlazeForgerBlockEntity> {

    public static final EnumProperty<BlazeEnchanterBlock.HeatLevel> HEAT_LEVEL =
            EnumProperty.create("blaze", BlazeEnchanterBlock.HeatLevel.class);

    public BlazeForgerBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, BlazeEnchanterBlock.HeatLevel.SMOULDERING));
    }

    @Override
    public Class<BlazeForgerBlockEntity> getBlockEntityClass() {
        return BlazeForgerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeForgerBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.BLAZE_FORGER.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT_LEVEL, FACING);
    }

    @Override
    public Item asItem() {
        return AllBlocks.BLAZE_BURNER.get().asItem();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos blockPos, CollisionContext pContext) {
        return AllShapes.HEATER_BLOCK_SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (handIn == InteractionHand.OFF_HAND)
            return InteractionResult.PASS;

        ItemStack heldItem;
        if (player.isCreative()) {
            heldItem = player.getItemInHand(handIn).copy();
        } else {
            heldItem = player.getItemInHand(handIn);
        }

        return onBlockEntityUse(worldIn, pos, be -> {
            if (!heldItem.isEmpty()) {
                // Try inserting item
                ItemStack remainder = be.insertItem(heldItem, false);
                if (remainder.getCount() != heldItem.getCount() || !ItemStack.isSameItemSameTags(remainder, heldItem)) {
                    if (!player.getAbilities().instabuild)
                        player.setItemInHand(handIn, remainder);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            } else {
                // Try extracting item
                ItemStack extracted = be.extractItem(false);
                if (!extracted.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(extracted);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
        });
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (world instanceof ServerLevel) {
            if (player != null)
                player.level().setBlockAndUpdate(pos, AllBlocks.BLAZE_BURNER.getDefaultState()
                        .setValue(BlazeBurnerBlock.FACING, state.getValue(FACING))
                        .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(AllBlocks.BLAZE_BURNER.get());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        var ret = new ArrayList<ItemStack>();
        ret.add(new ItemStack(AllBlocks.BLAZE_BURNER.get()));
        return ret;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    public static int getLight(BlockState state) {
        BlazeEnchanterBlock.HeatLevel level = state.getValue(HEAT_LEVEL);
        return switch (level) {
            case SEETHING -> 15;
            case KINDLED -> 11;
            case SMOULDERING -> 7;
        };
    }
}
