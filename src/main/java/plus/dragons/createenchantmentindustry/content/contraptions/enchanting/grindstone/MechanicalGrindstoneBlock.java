package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiDamageTypes;
import plus.dragons.createenchantmentindustry.entry.CeiStats;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

public class MechanicalGrindstoneBlock extends RotatedPillarKineticBlock implements IBE<KineticBlockEntity> {
    protected static VoxelShaper SHAPE = new AllShapes.Builder(Block.box(3, 3, 3, 13, 13, 13))
            .add(AllShapes.SIX_VOXEL_POLE.get(Axis.Y))
            .forAxis();

    public MechanicalGrindstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null)
            return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            var be = level.getBlockEntity(pos.below());
            if (be instanceof GrindstoneDrainBlockEntity drain) {
                var cap = drain.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElseThrow(IllegalStateException::new);
                    if (level.isClientSide) {
                        return handler.extractItem(3000, 64, true).isEmpty()
                                ? InteractionResult.PASS
                                : InteractionResult.SUCCESS;
                    }
                    ItemStack extractItem = handler.extractItem(3000, 64, false);
                    if (!extractItem.isEmpty()) {
                        player.setItemInHand(hand, extractItem);
                        if (!player.isCreative()) {
                            var speed = Math.abs(blockEntity.getSpeed());
                            if (speed >= 32) {
                                player.hurt(level.damageSources().generic(), speed / 32f);
                            }
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive())
            return InteractionResult.PASS;
        if (Math.abs(blockEntity.getSpeed()) < 30)
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        var location = hitResult.getLocation();
        // Sandpaper Polishing
        if (SandPaperPolishingRecipe.canPolish(level, stack)) {
            var item = stack.getItem();
            var fake = player instanceof FakePlayer;
            if (!fake && player.getCooldowns().isOnCooldown(item))
                return InteractionResult.PASS;
            var polished = SandPaperPolishingRecipe.applyPolish(level, Vec3.atCenterOf(pos), stack, null);
            if (!fake)
                player.getCooldowns().addCooldown(item, 10);
            SandPaperItem.spawnParticles(location, stack, level);
            AllSoundEvents.SANDING_SHORT.play(level, player, pos, 1, 1);
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, polished);
            } else {
                player.getInventory().placeItemBackInInventory(polished);
            }
        } else {
            // Grindstone
            var otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            var otherStack = player.getItemInHand(otherHand);
            var optional = GrindstoneHelper.grindItem(level, stack, otherStack);
            if (optional.isEmpty())
                return InteractionResult.PASS;
            var result = optional.get();
            if (result.top().isEmpty()) {
                player.setItemInHand(hand, result.output());
            } else {
                player.setItemInHand(hand, result.top());
                player.getInventory().placeItemBackInInventory(result.output());
            }
            player.setItemInHand(otherHand, result.bottom());
            player.awardStat(Stats.CUSTOM.get(CeiStats.MECHANICAL_GRINDSTONE_EXPERIENCE.get()), result.experience());
            if (player instanceof ServerPlayer serverPlayer)
                serverPlayer.giveExperiencePoints(result.experience());
            level.levelEvent(1042, pos, 0);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(AXIS));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.MECHANICAL_GRINDSTONE.get();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }
}
