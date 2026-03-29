package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiMountedStorageTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExperienceLanternMountedStorage extends WrapperMountedFluidStorage<ExperienceLanternMountedStorage.Handler> {
    public static final Codec<ExperienceLanternMountedStorage> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("capacity").forGetter(ExperienceLanternMountedStorage::getCapacity),
            FluidStack.CODEC.fieldOf("fluid").forGetter(ExperienceLanternMountedStorage::getFluid)
    ).apply(i, ExperienceLanternMountedStorage::new));

    private boolean dirty;

    protected ExperienceLanternMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
        super(type, new Handler(capacity, stack));
        this.wrapped.onChange = () -> this.dirty = true;
    }

    protected ExperienceLanternMountedStorage(int capacity, FluidStack stack) {
        this(CeiMountedStorageTypes.EXPERIENCE_LANTERN.get(), capacity, stack);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ExperienceLanternBlockEntity lantern) {
            FluidTank inventory = lantern.getTank().getPrimaryHandler();
            inventory.setFluid(this.wrapped.getFluid());
        }
    }

    public FluidStack getFluid() {
        return this.wrapped.getFluid();
    }

    public int getCapacity() {
        return this.wrapped.getCapacity();
    }

    public static ExperienceLanternMountedStorage fromLantern(ExperienceLanternBlockEntity lantern) {
        FluidTank inventory = lantern.getTank().getPrimaryHandler();
        return new ExperienceLanternMountedStorage(inventory.getCapacity(), inventory.getFluid().copy());
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return super.isFluidValid(tank, stack);
    }

    public static final class Handler extends FluidTank {
        private Runnable onChange = () -> {};

        public Handler(int capacity, FluidStack stack) {
            super(capacity);
            this.setFluid(stack);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            // Lantern only stores base experience fluid
            return stack.getFluid().isSame(CeiFluids.EXPERIENCE.get()) || CeiDataMaps.isXpFluid(stack);
        }

        @Override
        protected void onContentsChanged() {
            this.onChange.run();
        }
    }
}
