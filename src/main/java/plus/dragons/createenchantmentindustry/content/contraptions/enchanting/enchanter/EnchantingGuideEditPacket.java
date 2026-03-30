package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent.Context;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class EnchantingGuideEditPacket extends SimplePacketBase {

    private final int index;
    private final ItemStack itemStack;


    public EnchantingGuideEditPacket(int index, ItemStack enchantedBook) {
        this.index = index;
        itemStack = enchantedBook;
    }

    public EnchantingGuideEditPacket(FriendlyByteBuf buffer) {
        index = buffer.readInt();
        itemStack = buffer.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeItem(itemStack);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> {
                    ServerPlayer sender = context.getSender();
                    if (sender == null) return;
                    if (!(sender.containerMenu instanceof EnchantingGuideMenu menu) || !menu.directItemStackEdit)
                        return;
                    if (!menu.stillValid(sender))
                        return;
                    ItemStack mainHandItem = sender.getMainHandItem();
                    if (!CeiItems.ENCHANTING_GUIDE.isIn(mainHandItem))
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
                    if (!mainHandItem.is(CeiItems.ENCHANTING_GUIDE.get()))
                        return;

                    CompoundTag tag = mainHandItem.getOrCreateTag();
                    tag.putInt("index", index);
                    tag.put("target", itemStack.serializeNBT());

                    sender.getCooldowns()
                            .addCooldown(mainHandItem.getItem(), 5);
                });
        return true;
    }
}
