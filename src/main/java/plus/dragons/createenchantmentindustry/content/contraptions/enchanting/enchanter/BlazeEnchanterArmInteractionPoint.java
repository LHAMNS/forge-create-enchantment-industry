package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

import javax.annotation.Nullable;

/**
 * Mechanical Arm interaction point for the Blaze Enchanter.
 * <p>
 * Allows the mechanical arm to insert items for enchanting and extract enchanted results.
 * <p>
 * Ported from 1.21.1 BlazeEnchanterArmInteractionPoint.
 */
public class BlazeEnchanterArmInteractionPoint extends ArmInteractionPoint {
    public BlazeEnchanterArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof BlazeEnchanterBlockEntity enchanter)) {
            return stack;
        }
        ItemStack input = stack.copy();
        ItemStack single = input.split(1);

        // Check if it can be enchanted
        boolean canProcess;
        if (!enchanter.getTemplateItem().isEmpty()) {
            canProcess = enchanter.canProcessWithBehaviour(single);
        } else {
            canProcess = Enchanting.getValidEnchantment(single, enchanter.targetItem, enchanter.hyper()) != null;
        }

        if (!canProcess) {
            return stack;
        }

        if (!enchanter.getHeldItemStack().isEmpty()) {
            return stack;
        }

        if (simulate) {
            return input;
        }

        // Insert the item
        var transported = new com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack(single);
        transported.beltPosition = 0.5f;
        transported.prevBeltPosition = 0.5f;
        enchanter.setHeldItem(transported, Direction.NORTH);
        enchanter.setChanged();
        enchanter.sendData();
        return input;
    }

    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BlazeEnchanterBlockEntity enchanter) {
            ItemStack held = enchanter.getHeldItemStack();
            if (!held.isEmpty() && enchanter.processingTicks <= 0) {
                if (simulate) return held.copy();
                enchanter.heldItem = null;
                enchanter.notifyUpdate();
                return held;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotCount() {
        return 1;
    }

    public static class Type extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return CeiBlocks.BLAZE_ENCHANTER.has(state);
        }

        @Nullable
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BlazeEnchanterArmInteractionPoint(this, level, pos, state);
        }
    }
}
