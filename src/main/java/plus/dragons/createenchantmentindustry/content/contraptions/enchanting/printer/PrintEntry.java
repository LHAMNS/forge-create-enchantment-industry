package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import java.util.List;

public interface PrintEntry {

    ResourceLocation id();

    /**
     * Whether this print entry type is currently enabled by config.
     * Entries returning false will not match any items.
     */
    default boolean isEnabled() {
        return true;
    }

    boolean match(ItemStack toPrint);

    boolean valid(ItemStack target, ItemStack tested);

    int requiredInkAmount(ItemStack target);

    default int requiredInkAmount(ItemStack target, FluidStack fluid) {
        int configuredCost = CeiDataMaps.getPrintTypeCost(id().toString(), fluid.getFluid());
        return configuredCost >= 0 ? configuredCost : requiredInkAmount(target);
    }

    default Fluid requiredInkType(ItemStack target) {
        return CeiFluids.EXPERIENCE.get();
    }

    default ItemStack print(ItemStack target, ItemStack material){
        return target.copy();
    }

    /**
     * Fluid-aware print method. Override this to apply styling based on the fluid used.
     * The default implementation delegates to {@link #print(ItemStack, ItemStack)}.
     *
     * @param target   the item being used as the print source (e.g., name tag)
     * @param material the item being printed onto
     * @param fluid    the fluid currently in the printer's tank
     * @return the printed result item
     */
    default ItemStack print(ItemStack target, ItemStack material, FluidStack fluid) {
        return print(target, material);
    }

    /**
     * Check if this entry accepts the given fluid for printing.
     * The default checks if the fluid matches {@link #requiredInkType(ItemStack)}.
     * Override to accept multiple fluid types (e.g., experience OR dye fluids).
     */
    default boolean acceptsFluid(FluidStack fluidStack, ItemStack target) {
        return fluidStack.getFluid().isSame(requiredInkType(target));
    }

    boolean isTooExpensive(ItemStack target, int limit);

    void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target);

    MutableComponent getDisplaySourceContent(ItemStack target);
}
