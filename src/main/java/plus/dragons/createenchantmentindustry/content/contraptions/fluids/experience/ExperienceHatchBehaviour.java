package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

public class ExperienceHatchBehaviour extends FilteringBehaviour {
    public static final BehaviourType<ExperienceHatchBehaviour> TYPE = new BehaviourType<>();
    public static final int POINTS_PER_SCROLL = 10;

    public ExperienceHatchBehaviour(SmartBlockEntity blockEntity, ValueBoxTransform slot) {
        super(blockEntity, slot);
        forFluids();
        count = 0;
    }

    public FluidStack getFluidToDrain() {
        FluidStack filterFluid = filter.fluid(getWorld());
        if (filterFluid.isEmpty()) {
            // No filter set -> default to experience fluid
            int amount = count == 0 ? Integer.MAX_VALUE : count * POINTS_PER_SCROLL;
            return new FluidStack(CeiFluids.EXPERIENCE.get(), amount);
        }
        // Convert XP points to mB using the fluid's ratio
        int xpPoints = count * POINTS_PER_SCROLL;
        int amount = count == 0 ? Integer.MAX_VALUE : CeiDataMaps.xpToFluidAmount(filterFluid.getFluid(), xpPoints);
        if (amount == 0 && count != 0) amount = xpPoints; // fallback for unregistered fluids
        return new FluidStack(filterFluid.getFluid(), amount);
    }

    public FluidStack getFluidToFill(int available) {
        if (available == 0)
            return FluidStack.EMPTY;
        FluidStack filterFluid = filter.fluid(getWorld());
        if (filterFluid.isEmpty()) {
            // No filter set -> default to experience fluid (1:1 ratio)
            int amount = count == 0 ? available : Math.min(available, count * POINTS_PER_SCROLL);
            return new FluidStack(CeiFluids.EXPERIENCE.get(), amount);
        }
        // Convert XP points to mB using the fluid's ratio
        // 'available' is in XP points, we need to convert to mB for the filtered fluid
        int availableMb = CeiDataMaps.xpToFluidAmount(filterFluid.getFluid(), available);
        if (availableMb == 0) availableMb = available; // fallback for unregistered fluids (assume 1:1)
        if (count == 0) {
            return new FluidStack(filterFluid.getFluid(), availableMb);
        }
        int xpPoints = count * POINTS_PER_SCROLL;
        int mbLimit = CeiDataMaps.xpToFluidAmount(filterFluid.getFluid(), xpPoints);
        if (mbLimit == 0) mbLimit = xpPoints; // fallback for unregistered fluids
        int amount = Math.min(availableMb, mbLimit);
        return new FluidStack(filterFluid.getFluid(), amount);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        nbt.put("Filter", filter.serializeNBT());
        nbt.putInt("Scroll", count);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        filter = FilterItemStack.of(nbt.getCompound("Filter"));
        count = nbt.getInt("Scroll");
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = settings.value();
        blockEntity.setChanged();
        blockEntity.sendData();
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, count);
    }

    @Override
    public boolean isCountVisible() {
        return true;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
                LANG.translate("gui.experience_hatch.exchange").component(),
                100,
                10,
                List.of(LANG.translate("gui.experience_hatch.points").component()),
                new ValueSettingsFormatter(this::formatValue));
    }

    @Override
    public MutableComponent formatValue(ValueSettings value) {
        int count = value.value();
        if (count == 0)
            return LANG.translate("gui.experience_hatch.all").component();
        return Component.literal(String.valueOf(count * POINTS_PER_SCROLL));
    }

    @Override
    public MutableComponent getCountLabelForValueBox() {
        if (count == 0)
            return Component.literal("*");
        return Component.literal(String.valueOf(count * POINTS_PER_SCROLL));
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        FilterItemStack filterStack = FilterItemStack.of(stack.copy());
        if (!filterStack.isEmpty()) {
            FluidStack fluid = filterStack.fluid(getWorld());
            // Only allow registered XP fluids (EXPERIENCE, HYPER_EXPERIENCE, or mod-added)
            if (!CeiDataMaps.isXpFluid(fluid.getFluid()))
                return false;
        }
        this.filter = filterStack;
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    @Override
    public String getClipboardKey() {
        return "ExperienceHatch";
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
