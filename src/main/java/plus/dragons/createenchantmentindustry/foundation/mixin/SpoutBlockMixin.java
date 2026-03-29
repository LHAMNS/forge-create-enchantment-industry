package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.spout.SpoutBlock;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mixin(value = SpoutBlock.class)
public abstract class SpoutBlockMixin extends Block implements IWrenchable, IBE<SpoutBlockEntity> {
    public SpoutBlockMixin(Properties pProperties) {
        super(pProperties);
    }
    
    /**
     * @author CEI
     * @reason Drop experience fluid as XP orbs when spout is broken
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        if (level instanceof ServerLevel serverLevel) {
            withBlockEntityDo(level, pos, te -> {
                var fluidStack = ((SpoutBlockEntityAccessor) te).getTank().getPrimaryHandler().getFluid();
                ExperienceFluid expFluid = CeiDataMaps.asExperienceFluid(fluidStack.getFluid());
                if(expFluid != null) {
                    expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
                }
            });
        }
        level.removeBlockEntity(pos);
    }
}
