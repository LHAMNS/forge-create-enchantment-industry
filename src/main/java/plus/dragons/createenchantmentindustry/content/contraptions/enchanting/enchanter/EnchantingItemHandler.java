package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnchantingItemHandler implements IItemHandler {
    private final BlazeEnchanterBlockEntity be;
    private final Direction side;

    public EnchantingItemHandler(BlazeEnchanterBlockEntity be, Direction side) {
        this.be = be;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return be.getHeldItemStack();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!be.getHeldItemStack()
                .isEmpty())
            return stack;

        ItemStack returned = ItemStack.EMPTY;

        boolean canProcess = false;
        if (be.enchanterBehaviour != null && be.enchanterBehaviour.canProcess(stack))
            canProcess = true;
        else if (Enchanting.getValidEnchantment(stack, be.targetItem, be.hyper()) != null)
            canProcess = true;
        if (!canProcess)
            return stack;

        if (stack.getCount() > 1) {
            returned = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
            stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
        }

        if (!simulate) {
            TransportedItemStack heldItem = new TransportedItemStack(stack);
            heldItem.prevBeltPosition = 0;
            be.setHeldItem(heldItem, side.getOpposite());
            be.notifyUpdate();
        }

        return returned;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        TransportedItemStack held = be.heldItem;
        if (held == null)
            return ItemStack.EMPTY;

        ItemStack stack = held.stack.copy();
        ItemStack extracted = stack.split(amount);
        if (!simulate) {
            be.heldItem.stack = stack;
            if (stack.isEmpty())
                be.heldItem = null;
            be.notifyUpdate();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
