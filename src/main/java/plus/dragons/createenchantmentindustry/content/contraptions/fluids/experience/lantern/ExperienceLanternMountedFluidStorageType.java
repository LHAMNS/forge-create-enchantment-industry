package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ExperienceLanternMountedFluidStorageType extends MountedFluidStorageType<ExperienceLanternMountedStorage> {
    public ExperienceLanternMountedFluidStorageType() {
        super(ExperienceLanternMountedStorage.CODEC);
    }

    @Override
    public @Nullable ExperienceLanternMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ExperienceLanternBlockEntity lantern)
            return ExperienceLanternMountedStorage.fromLantern(lantern);
        return null;
    }
}
