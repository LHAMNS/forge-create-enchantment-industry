package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.antlr.v4.runtime.misc.NotNull;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import javax.annotation.Nullable;
import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/**
 * Block entity for the Blaze Forger - an automated anvil that uses experience fluid
 * to merge/upgrade enchanted items and books.
 * <p>
 * Ported from 1.21.1 BlazeForgerBlockEntity. Adapted to use 1.20.1's
 * SmartBlockEntity + SmartFluidTankBehaviour + LazyOptional capabilities pattern
 * (matching the existing BlazeEnchanterBlockEntity approach).
 */
public class BlazeForgerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public static final int FORGING_TIME = 200;

    SmartFluidTankBehaviour internalTank;
    protected int processingTime = -1;
    protected final BlazeForgerInventory inventory;

    // Client-side animation
    LerpedFloat headAnimation;
    LerpedFloat headAngle;

    public BlazeForgerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new BlazeForgerInventory(this);
        headAnimation = LerpedFloat.linear();
        headAngle = LerpedFloat.angular();
        headAngle.startWithValue((AngleHelper
                .horizontalAngle(state.getOptionalValue(BlazeForgerBlock.FACING)
                        .orElse(Direction.SOUTH)) + 180) % 360
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(internalTank = FilteringFluidTankBehaviour
                .single(fluidStack -> fluidStack.getFluid().is(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag),
                        this, CeiConfigs.SERVER.blazeForgerTankCapacity.get())
                .whenFluidUpdates(() -> {
                    var fluid = internalTank.getPrimaryHandler().getFluid().getFluid();
                    if (CeiFluids.EXPERIENCE.is(fluid))
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.KINDLED);
                    else if (CeiFluids.HYPER_EXPERIENCE.is(fluid))
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.SEETHING);
                    else
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.SMOULDERING);
                }));
    }

    @Override
    public void tick() {
        super.tick();

        boolean onClient = level.isClientSide && !isVirtual();

        if (onClient) {
            blazeTick();
        }

        if (level.isClientSide && isVirtual()) {
            // Ponder rendering
            var cost = inventory.getExperienceCost();
            if (cost > 0 && canConsumeExperience(cost)) {
                if (processingTime < 0) {
                    processingTime = FORGING_TIME / 4;
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    return;
                }
                processingTime = -1;
                inventory.applyResult();
            } else if (processingTime != -1) {
                processingTime = -1;
            }
            return;
        }

        if (!(level instanceof ServerLevel))
            return;

        var cost = inventory.getExperienceCost();
        if (cost > 0 && canConsumeExperience(cost)) {
            if (processingTime < 0) {
                processingTime = FORGING_TIME;
                notifyUpdate();
                return;
            }
            if (processingTime > 0) {
                processingTime--;
                notifyUpdate();
                return;
            }
            // Forging complete
            consumeExperience(cost);
            processingTime = -1;
            inventory.applyResult();
            notifyUpdate();
            level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS,
                    1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        } else if (processingTime != -1) {
            processingTime = -1;
            notifyUpdate();
        }
    }

    protected void blazeTick() {
        boolean active = processingTime > 0;

        if (!active) {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x = player.getX();
                double z = player.getZ();
                double dx = x - (getBlockPos().getX() + 0.5);
                double dz = z - (getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
            headAngle.chase(target, .25f, LerpedFloat.Chaser.exp(5));
            headAngle.tickChaser();
        } else {
            headAngle.chase((AngleHelper.horizontalAngle(getBlockState().getOptionalValue(BlazeForgerBlock.FACING)
                    .orElse(Direction.SOUTH)) + 180) % 360, .125f, LerpedFloat.Chaser.EXP);
            headAngle.tickChaser();
        }
        headAnimation.chase(1, .25f, LerpedFloat.Chaser.exp(.25f));
        headAnimation.tickChaser();

        spawnBlazeParticles();
    }

    protected void spawnBlazeParticles() {
        if (level == null)
            return;
        BlazeEnchanterBlock.HeatLevel heatLevel = getBlockState().getValue(BlazeForgerBlock.HEAT_LEVEL);

        var r = level.random;
        Vec3 c = VecHelper.getCenterOf(worldPosition);
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
                .multiply(1, 0, 1));

        if (r.nextInt(3) == 0)
            level.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);
        if (r.nextInt(2) != 0)
            return;

        boolean empty = level.getBlockState(worldPosition.above())
                .getCollisionShape(level, worldPosition.above())
                .isEmpty();

        double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
        Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
                        .multiply(1, .25f, 1)
                        .normalize()
                        .scale((empty ? .25f : .5) + r.nextDouble() * .125f))
                .add(0, .5, 0);

        if (heatLevel.isAtLeast(BlazeEnchanterBlock.HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(BlazeEnchanterBlock.HeatLevel.KINDLED)) {
            level.addParticle(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        }
    }

    public boolean hyper() {
        return CeiFluids.HYPER_EXPERIENCE.is(internalTank.getPrimaryHandler().getFluid().getFluid());
    }

    protected boolean canConsumeExperience(int amount) {
        var tankFluid = internalTank.getPrimaryHandler().getFluid().getFluid();
        if (!CeiFluids.EXPERIENCE.is(tankFluid) && !CeiFluids.HYPER_EXPERIENCE.is(tankFluid))
            return false;
        return internalTank.getPrimaryHandler().getFluidAmount() >= amount;
    }

    protected void consumeExperience(int amount) {
        FluidStack exp = new FluidStack(
                hyper() ? CeiFluids.HYPER_EXPERIENCE.get().getSource() : CeiFluids.EXPERIENCE.get().getSource(),
                amount);
        internalTank.getPrimaryHandler().drain(exp, IFluidHandler.FluidAction.EXECUTE);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        var original = stack;
        if (inventory.hasRemainingOutput()) return stack;
        if (!stack.isEmpty())
            stack = inventory.insertItem(0, stack, simulate);
        if (!stack.isEmpty())
            stack = inventory.insertItem(1, stack, simulate);
        if (!simulate && (original.getCount() != stack.getCount() || !ItemStack.isSameItemSameTags(original, stack))) {
            inventory.updateResult();
            notifyUpdate();
        }
        return stack;
    }

    public ItemStack extractItem(boolean simulate) {
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack extracted = inventory.extractItem(i, 1, simulate);
            if (!extracted.isEmpty()) {
                if (!simulate && i < 2) {
                    inventory.updateResult();
                    notifyUpdate();
                }
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public void updateHeatLevel(BlazeEnchanterBlock.HeatLevel heatLevel) {
        if (level != null)
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlazeForgerBlock.HEAT_LEVEL, heatLevel));
    }

    @Override
    public void write(CompoundTag compoundTag, boolean clientPacket) {
        super.write(compoundTag, clientPacket);
        compoundTag.putInt("ProcessingTime", processingTime);
        compoundTag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    protected void read(CompoundTag compoundTag, boolean clientPacket) {
        super.read(compoundTag, clientPacket);
        processingTime = compoundTag.getInt("ProcessingTime");
        inventory.deserializeNBT(compoundTag.getCompound("Inventory"));
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level instanceof ServerLevel serverLevel) {
            var pos = getBlockPos();
            // Drop inventory contents
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
            // Drop experience fluid as orbs
            var tank = internalTank.getPrimaryHandler();
            var fluidStack = tank.getFluid();
            if (fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if ((side == Direction.DOWN || side == null) && isFluidHandlerCap(capability))
            return internalTank.getCapability().cast();
        return super.getCapability(capability, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LANG.translate("gui.goggles.blaze_forger").forGoggles(tooltip);

        int cost = inventory.getExperienceCost();
        if (cost > 0) {
            tooltip.add(Component.literal("     ")
                    .append(LANG.translate("gui.goggles.xp_consumption", cost).component())
                    .withStyle(ChatFormatting.GREEN));
            for (int i = 0; i < 2; i++) {
                var result = inventory.getResult(i);
                if (result.isEmpty()) continue;
                tooltip.add(Component.literal("     ")
                        .append(LANG.translate("gui.goggles.forging.result").component()));
                tooltip.add(Component.literal("       ")
                        .append(result.getHoverName())
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (inventory.forgingCompleted()) {
                tooltip.add(Component.literal("     ")
                        .append(LANG.translate("gui.goggles.forging.completed").component())
                        .withStyle(ChatFormatting.GREEN));
            } else if (!inventory.notEnoughItemToForge()) {
                tooltip.add(Component.literal("     ")
                        .append(LANG.translate("gui.goggles.forging.invalid_items").component())
                        .withStyle(ChatFormatting.RED));
            }
        }

        containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
        return true;
    }
}
