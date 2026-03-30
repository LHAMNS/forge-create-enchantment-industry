package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class BlazeEnchanterEditPacket extends SimplePacketBase {

    private final int index;
    private final ItemStack itemStack;
    private final BlockPos blockPos;


    public BlazeEnchanterEditPacket(int index, ItemStack enchantedBook, BlockPos blockPos) {
        this.index = index;
        itemStack = enchantedBook;
        this.blockPos = blockPos;
    }

    public BlazeEnchanterEditPacket(FriendlyByteBuf buffer) {
        index = buffer.readInt();
        itemStack = buffer.readItem();
        blockPos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeItem(itemStack);
        buffer.writeBlockPos(blockPos);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> {
                    ServerPlayer sender = context.getSender();
                    if (sender == null)
                        return;
                    if (sender.distanceToSqr(Vec3.atCenterOf(blockPos)) > 64)
                        return;
                    if (!(sender.containerMenu instanceof EnchantingGuideMenu menu) || menu.directItemStackEdit)
                        return;
                    if (!blockPos.equals(menu.blockPos) || !menu.stillValid(sender))
                        return;
                    if(!(sender.level().getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity blazeEnchanter))
                        return;
                    if (!blazeEnchanter.targetItem.is(CeiItems.ENCHANTING_GUIDE.get()))
                        return;
                    if (index < 0)
                        return;
                    if (!itemStack.isEmpty()) {
                        if (!itemStack.is(Items.ENCHANTED_BOOK))
                            return;
                        if (!EnchantingGuideItem.isValidTargetBook(itemStack))
                            return;
                        if (index >= EnchantingGuideItem.getSortedEnchantments(itemStack).size())
                            return;
                    }

                    ItemStack updatedGuide = blazeEnchanter.targetItem.copy();
                    CompoundTag tag = updatedGuide.getOrCreateTag();
                    tag.putInt("index", index);
                    tag.put("target", itemStack.serializeNBT());
                    tag.remove("blockPos");
                    blazeEnchanter.setTargetItem(updatedGuide);

                    if(blazeEnchanter.processingTicks>5){
                        blazeEnchanter.processingTicks = BlazeEnchanterBlockEntity.ENCHANTING_TIME;
                    }

                    blazeEnchanter.notifyUpdate();
                });
        return true;
    }
}
