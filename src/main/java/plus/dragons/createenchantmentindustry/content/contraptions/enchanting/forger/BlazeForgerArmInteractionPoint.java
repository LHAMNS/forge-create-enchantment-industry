package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

import javax.annotation.Nullable;

/**
 * Mechanical Arm interaction point for the Blaze Forger.
 * <p>
 * Allows the mechanical arm to insert items for forging and extract forged results.
 * The arm can insert into the two input slots and extract from the output slots.
 * <p>
 * Ported from 1.21.1 BlazeForgerArmInteractionPoint.
 */
public class BlazeForgerArmInteractionPoint extends ArmInteractionPoint {
    public BlazeForgerArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger))
            return stack;
        return forger.insertItem(stack, simulate);
    }

    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger) {
            // Extract from output slots (index 2, 3)
            int actualSlot = slot + 2;
            return forger.inventory.extractItem(actualSlot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    public static class Type extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return CeiBlocks.BLAZE_FORGER.has(state);
        }

        @Nullable
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BlazeForgerArmInteractionPoint(this, level, pos, state);
        }
    }
}
