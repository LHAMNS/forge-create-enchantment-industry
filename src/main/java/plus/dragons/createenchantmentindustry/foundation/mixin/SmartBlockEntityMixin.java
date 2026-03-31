package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceHelper;

import java.util.Collection;

/**
 * Mixin equivalent of upstream's SmartBlockEntityMixin.
 * When any SmartBlockEntity is destroyed, iterates all fluid tank behaviours
 * and drops any experience fluid as XP orbs.
 */
@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin extends BlockEntity {

    @Shadow(remap = false)
    public abstract Collection<BlockEntityBehaviour> getAllBehaviours();

    public SmartBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "destroy", at = @At("HEAD"), remap = false)
    private void destroy$dropExperienceFluid(CallbackInfo ci) {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;
        var state = this.getBlockState();
        for (var behaviour : this.getAllBehaviours()) {
            if (behaviour instanceof SmartFluidTankBehaviour tank) {
                tank.getCapability().ifPresent(handler -> {
                    int numTanks = handler.getTanks();
                    for (int t = 0; t < numTanks; t++) {
                        var fluid = handler.getFluidInTank(t);
                        int experience = ExperienceHelper.getExperienceFromFluid(fluid);
                        if (experience > 0) {
                            state.getBlock().popExperience(serverLevel, this.worldPosition, experience);
                        }
                    }
                });
            }
        }
    }
}
