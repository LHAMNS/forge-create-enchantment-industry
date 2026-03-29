package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.world.level.block.DirectionalBlock.FACING;

public class ExperienceLanternBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    protected SmartFluidTankBehaviour tank;
    protected AABB effectiveAABB;
    protected int rate;

    public ExperienceLanternBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        effectiveAABB = new AABB(getBlockPos()).inflate(0.5);
        rate = CeiConfigs.SERVER.experienceLanternDrainRate.get();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, CeiConfigs.SERVER.experienceLanternFluidCapacity.get())
                .allowInsertion()
                .allowExtraction()
                .whenFluidUpdates(this::onFluidStackChanged);
        behaviours.add(tank);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (!level.isClientSide && level.getGameTime() % 10 == 0) {
            drainExp();
        }
        if (!level.isClientSide && CeiConfigs.SERVER.experienceLanternPullToggle.get()) {
            pullExp();
        }
    }

    public SmartFluidTankBehaviour getTank() {
        return tank;
    }

    protected void drainExp() {
        List<Player> players = level.getEntitiesOfClass(Player.class, effectiveAABB,
                player -> player.isAlive() && !player.isSpectator());
        if (!players.isEmpty()) {
            AtomicInteger sum = new AtomicInteger();
            players.forEach(player -> {
                var playerExp = ExperienceHelper.getExperienceForPlayer(player);
                if (playerExp >= rate) sum.addAndGet(rate);
                else if (playerExp != 0) sum.addAndGet(playerExp);
            });
            if (sum.get() != 0) {
                var inserted = tank.getPrimaryHandler().fill(
                        new FluidStack(CeiFluids.EXPERIENCE.get(), sum.get()),
                        IFluidHandler.FluidAction.EXECUTE);
                if (inserted != 0) {
                    for (var player : players) {
                        var total = ExperienceHelper.getExperienceForPlayer(player);
                        if (inserted >= rate) {
                            if (total >= rate) {
                                player.giveExperiencePoints(-rate);
                                inserted -= rate;
                            } else if (total != 0) {
                                inserted -= total;
                                player.giveExperiencePoints(-total);
                            }
                        } else if (inserted > 0) {
                            if (total >= inserted) {
                                player.giveExperiencePoints(-inserted);
                                inserted = 0;
                            } else {
                                inserted -= total;
                                player.giveExperiencePoints(-total);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB);
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                var amount = orb.value;
                var fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get(), amount);
                var inserted = tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                if (inserted == amount) {
                    orb.remove(Entity.RemovalReason.DISCARDED);
                } else {
                    if (inserted != 0) {
                        orb.value -= inserted;
                    }
                    break;
                }
            }
        }
    }

    protected void pullExp() {
        int pullRadius = CeiConfigs.SERVER.experienceLanternPullRadius.get();
        double pullForceMultiplier = CeiConfigs.SERVER.experienceLanternPullForceMultiplier.get();
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(
                ExperienceOrb.class, effectiveAABB.inflate(pullRadius));
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                if (orb.getDeltaMovement().length() <= .5) {
                    var pushForce = pullForceMultiplier / orb.position().distanceTo(getBlockPos().getCenter());
                    var directionToLantern = getBlockPos().getCenter().subtract(orb.position())
                            .normalize().multiply(pushForce, pushForce, pushForce);
                    orb.push(directionToLantern.x, directionToLantern.y, directionToLantern.z);
                }
            }
        }
    }

    protected void onFluidStackChanged() {
        if (tank == null || tank.getPrimaryTank() == null) return;
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        int capacity = tank.getPrimaryHandler().getCapacity();
        int light = capacity > 0 ? (int) (((float) fluid.getAmount() / capacity) * 15f) : 0;
        light = Math.min(Math.max(0, light), 15);
        if (level != null && getBlockState().hasProperty(ExperienceLanternBlock.LIGHT)) {
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(ExperienceLanternBlock.LIGHT, light));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (isFluidHandlerCap(cap)) {
            if (side == null || side.getOpposite() == getBlockState().getValue(FACING))
                return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
    }
}
