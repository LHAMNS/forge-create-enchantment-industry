package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/**
 * Address Printing - prints an address from one package onto another.
 * <p>
 * The source package must have an address set. The target package will receive that address.
 * <p>
 * Ported from 1.21.1 AddressPrintingBehaviour. Uses Create 6.0.8's PackageItem NBT system.
 */
public class AddressPrintEntry implements PrintEntry {

    @Override
    public ResourceLocation id() {
        return EnchantmentIndustry.genRL("address");
    }

    @Override
    public boolean isEnabled() {
        return CeiConfigs.SERVER.enablePackageAddressPrinting.get();
    }

    @Override
    public boolean match(ItemStack toPrint) {
        if (!isEnabled()) return false;
        if (!(toPrint.getItem() instanceof PackageItem))
            return false;
        String address = PackageItem.getAddress(toPrint);
        return address != null && !address.isEmpty();
    }

    @Override
    public boolean valid(ItemStack target, ItemStack tested) {
        return tested.getItem() instanceof PackageItem;
    }

    @Override
    public int requiredInkAmount(ItemStack target) {
        return CeiConfigs.SERVER.copyAddressCost.get();
    }

    @Override
    public Fluid requiredInkType(ItemStack target) {
        return CeiFluids.INK.get();
    }

    @Override
    public ItemStack print(ItemStack target, ItemStack material) {
        ItemStack result = material.copy();
        String address = PackageItem.getAddress(target);
        if (address != null && !address.isEmpty()) {
            PackageItem.addAddress(result, address);
        }
        return result;
    }

    @Override
    public boolean isTooExpensive(ItemStack target, int limit) {
        return CeiConfigs.SERVER.copyAddressCost.get() > limit;
    }

    @Override
    public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
        String address = PackageItem.getAddress(target);
        LANG.translate("gui.goggles.printer.address").forGoggles(tooltip, 1);
        tooltip.add(Component.literal("     ")
                .append(Component.literal("-> " + address).withStyle(ChatFormatting.GOLD)));
        boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
        if (tooExpensive)
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.too_expensive").component()
            ).withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.ink_consumption",
                    String.valueOf(CeiConfigs.SERVER.copyAddressCost.get())).component()
            ).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public MutableComponent getDisplaySourceContent(ItemStack target) {
        String address = PackageItem.getAddress(target);
        return LANG.builder()
                .add(LANG.itemName(target))
                .text(" / ")
                .add(Component.literal(address))
                .component();
    }
}
