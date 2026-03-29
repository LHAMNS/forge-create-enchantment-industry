package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import javax.annotation.Nonnull;

public class PrinterTargetItemHandler implements IItemHandler {
    PrinterBlockEntity be;

    public PrinterTargetItemHandler(PrinterBlockEntity be) {
        this.be = be;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @Nonnull ItemStack getStackInSlot(int slot) {
        return be.getCopyTarget();
    }

    @Override
    public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(!be.getCopyTarget().isEmpty()) return stack;
        if(!isItemValid(slot,stack)) return stack; // Prevent strange crash problem from happening. See #170 log. Chute does not check item validity before insertion.
        else{
            if(!simulate){
                be.setCopyTarget(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        var target = be.getCopyTarget();
        if (target.isEmpty()) return ItemStack.EMPTY;
        var ret = target.copy();
        ret.setCount(Math.min(amount, ret.getCount()));
        if(!simulate){
            be.setCopyTarget(ItemStack.EMPTY);
        }
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return Printing.match(stack)!=null;
    }
}
