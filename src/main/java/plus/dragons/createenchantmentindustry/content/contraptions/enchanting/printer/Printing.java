package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;


public class Printing {

    @Nullable
    public static PrintEntry match(ItemStack toPrint){
        for(var entry:PrintEntries.ENTRIES.values()){
            if(entry.match(toPrint)) return entry;
        } return null;
    }
    public static boolean valid(PrintEntry printEntry, ItemStack printTarget, ItemStack tested) {
        return printEntry.valid(printTarget,tested);
    }

    public static int getRequiredAmountForItem(PrintEntry printEntry, ItemStack target) {
        return getRequiredAmountForItem(printEntry, target, new FluidStack(printEntry.requiredInkType(target), 1));
    }

    public static int getRequiredAmountForItem(PrintEntry printEntry, ItemStack target, FluidStack availableFluid) {
        return printEntry.requiredInkAmount(target, availableFluid);
    }
    
    @SuppressWarnings("deprecation") //Fluid Tags are still useful for mod interaction
    public static boolean isCorrectInk(PrintEntry printEntry, FluidStack fluidStack, ItemStack target) {
        return printEntry.acceptsFluid(fluidStack, target);
    }

    /**
     * Perform a print operation. This method mutates both {@code stack} (shrinks by 1)
     * and {@code availableFluid} (shrinks by {@code requiredAmount}).
     * Callers are expected to write the mutated fluid back to the tank after this call.
     */
    public static ItemStack print(PrintEntry printEntry, ItemStack target, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        var copy = stack.copy();
        copy.setCount(1);
        stack.shrink(1);
        var fluidBeforeShrink = availableFluid.copy();
        availableFluid.shrink(requiredAmount);
        return printEntry.print(target, copy, fluidBeforeShrink);
    }

    public static boolean isTooExpensive(PrintEntry printEntry, ItemStack target, int limit) {
        return getRequiredAmountForItem(printEntry, target) > limit;
    }



}
