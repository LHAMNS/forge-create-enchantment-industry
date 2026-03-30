package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.compat.tlm.TLMCompat;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExperienceLanternMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide) return;
        boolean shouldDrain = context.world.getGameTime() % 10 == 0;
        boolean shouldPull = CeiConfigs.SERVER.experienceLanternPullToggle.get();
        if (!shouldDrain && !shouldPull) return;
        var effectiveAABB = new AABB(
                context.position.subtract(0.5d, 0.5d, 0.5d),
                context.position.add(0.5d, 0.5d, 0.5d)).inflate(0.5);
        if (shouldDrain) {
            drainExp(context.world, effectiveAABB, context.contraption.getStorage().getFluids());
        }
        if (shouldPull) {
            pullExp(context.world, effectiveAABB, context.position);
        }
    }

    protected void drainExp(Level level, AABB effectiveAABB, MountedFluidStorageWrapper tank) {
        var rate = CeiConfigs.SERVER.experienceLanternDrainRate.get();
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
                var inserted = tank.fill(
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
                var inserted = tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
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
        // Drain experience from Touhou Little Maid's maids if the mod is loaded
        if (TLMCompat.isLoaded() && CeiConfigs.SERVER.experienceLanternDrainMaidExperience.get()) {
            plus.dragons.createenchantmentindustry.compat.tlm.MaidExperienceHandler
                    .drainMaidExperience(level, effectiveAABB, tank);
        }
    }

    protected void pullExp(Level level, AABB effectiveAABB, Vec3 position) {
        int pullRadius = CeiConfigs.SERVER.experienceLanternPullRadius.get();
        double pullForceMultiplier = CeiConfigs.SERVER.experienceLanternPullForceMultiplier.get();
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(
                ExperienceOrb.class, effectiveAABB.inflate(pullRadius));
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                if (orb.getDeltaMovement().length() <= .5) {
                    double distance = orb.position().distanceTo(position);
                    if (distance < 0.01) continue;
                    var pushForce = pullForceMultiplier / distance;
                    var directionToLantern = position.subtract(orb.position())
                            .normalize().multiply(pushForce, pushForce, pushForce);
                    orb.push(directionToLantern.x, directionToLantern.y, directionToLantern.z);
                }
            }
        }
    }
}
