package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceHelper;

/**
 * Equivalent to upstream's ConnectivityHandlerMixin.
 * When a multi-block fluid tank containing experience fluid is split (broken),
 * drops the excess experience as XP orbs.
 */
@Mixin(value = ConnectivityHandler.class, remap = false)
public class ConnectivityHandlerMixin {

    /**
     * Hook into the single-block early return (width==1 && height==1).
     * When a single-tank block is removed while containing experience fluid, drop it as XP.
     *
     * ordinal=2 targets the third RETURN in splitMultiAndInvalidate:
     *   ordinal 0: level == null guard
     *   ordinal 1: be == null after getControllerBE()
     *   ordinal 2: width == 1 && height == 1  <-- this one (single-block early exit)
     * Keep in sync if Create reorders or adds returns before this point.
     */
    @Inject(method = "splitMultiAndInvalidate", at = @At(value = "RETURN", ordinal = 2))
    private static <T extends BlockEntity & IMultiBlockEntityContainer> void cei$splitMulti$dropExperienceFluidSingle(
            T be, @Coerce Object cache, boolean tryReconnect, CallbackInfo ci) {
        if (!(be.getLevel() instanceof ServerLevel level && be.isRemoved()))
            return;
        if (!(be instanceof IMultiBlockEntityContainer.Fluid fluidContainer))
            return;
        if (!fluidContainer.hasTank() || fluidContainer.getTank(0) instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
            return;
        var dropped = fluidContainer.getFluid(0);
        ExperienceHelper.dropExperienceFluid(level, VecHelper.getCenterOf(be.getBlockPos()), dropped);
    }

    /**
     * Hook at the tail of splitMultiAndInvalidate to catch multi-block splits.
     * When the remaining fluid after redistribution is experience, drop it as XP.
     */
    @Inject(method = "splitMultiAndInvalidate", at = @At("TAIL"))
    private static <T extends BlockEntity & IMultiBlockEntityContainer> void cei$splitMulti$dropExperienceFluidMulti(
            T be, @Coerce Object cache, boolean tryReconnect, CallbackInfo ci, @Local(ordinal = 0) FluidStack toDistribute) {
        if (!(be.getLevel() instanceof ServerLevel level))
            return;
        if (!(be instanceof IMultiBlockEntityContainer.Fluid fluidContainer))
            return;
        if (!fluidContainer.hasTank() || fluidContainer.getTank(0) instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
            return;
        if (toDistribute.isEmpty())
            return;
        ExperienceHelper.dropExperienceFluid(level, VecHelper.getCenterOf(be.getBlockPos()), toDistribute);
    }
}
