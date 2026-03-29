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
 * Package Pattern Printing - prints the visual style/pattern of one package onto another.
 * <p>
 * The source package must have no address (is a blank/pattern package).
 * The result converts the target package's visual style to match the source.
 * <p>
 * Ported from 1.21.1 PackagePatternPrintingBehaviour. Uses Create 6.0.8's PackageItem system.
 */
public class PackagePatternPrintEntry implements PrintEntry {

    @Override
    public ResourceLocation id() {
        return EnchantmentIndustry.genRL("package_pattern");
    }

    @Override
    public boolean isEnabled() {
        return CeiConfigs.SERVER.enablePackagePatternPrinting.get();
    }

    @Override
    public boolean match(ItemStack toPrint) {
        if (!isEnabled()) return false;
        if (!(toPrint.getItem() instanceof PackageItem))
            return false;
        String address = PackageItem.getAddress(toPrint);
        // Pattern source must have NO address (blank pattern package)
        return address == null || address.isEmpty();
    }

    @Override
    public boolean valid(ItemStack target, ItemStack tested) {
        if (!(tested.getItem() instanceof PackageItem))
            return false;
        // Allow printing if the packages differ in visual style (item type or NBT)
        return !ItemStack.isSameItemSameTags(target, tested);
    }

    @Override
    public int requiredInkAmount(ItemStack target) {
        return CeiConfigs.SERVER.copyPackagePatternCost.get();
    }

    @Override
    public Fluid requiredInkType(ItemStack target) {
        return CeiFluids.INK.get();
    }

    @Override
    public ItemStack print(ItemStack target, ItemStack material) {
        // Convert the material to the same PackageItem type as the target
        ItemStack result = new ItemStack(target.getItem(), material.getCount());
        // Copy over the contents NBT from the material
        if (material.hasTag()) {
            result.setTag(material.getTag().copy());
        }
        return result;
    }

    @Override
    public boolean isTooExpensive(ItemStack target, int limit) {
        return CeiConfigs.SERVER.copyPackagePatternCost.get() > limit;
    }

    @Override
    public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
        LANG.translate("gui.goggles.printer.package_pattern").forGoggles(tooltip, 1);
        tooltip.add(Component.literal("     ")
                .append(target.getHoverName())
                .withStyle(ChatFormatting.GOLD));
        boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
        if (tooExpensive)
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.too_expensive").component()
            ).withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.ink_consumption",
                    String.valueOf(CeiConfigs.SERVER.copyPackagePatternCost.get())).component()
            ).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public MutableComponent getDisplaySourceContent(ItemStack target) {
        return LANG.builder()
                .add(LANG.itemName(target))
                .text(" / ")
                .add(Component.translatable("gui.goggles.printer.package_pattern"))
                .component();
    }
}
