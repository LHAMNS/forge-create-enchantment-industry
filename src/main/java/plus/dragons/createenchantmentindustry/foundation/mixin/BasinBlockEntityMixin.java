package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BasinBlockEntity.class)
public abstract class BasinBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation {
    public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // SmartBlockEntityMixin already drops XP fluid for SmartFluidTankBehaviour instances.
    @Inject(method = "destroy",
            at = @At(value = "RETURN"), remap = false)
    private void injected(CallbackInfo ci) {
    }
}
