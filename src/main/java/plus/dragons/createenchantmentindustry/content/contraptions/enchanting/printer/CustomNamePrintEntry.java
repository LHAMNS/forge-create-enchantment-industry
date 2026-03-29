package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/**
 * Custom Name Printing - prints a name tag's custom name onto any item.
 * <p>
 * Upgraded from the existing NameTag entry with support for dye color styling.
 * When a dye-colored fluid is used, the custom name can be styled with that color.
 * <p>
 * Ported from 1.21.1 CustomNamePrintingBehaviour. Uses 1.20.1's Component/Style API.
 */
public class CustomNamePrintEntry implements PrintEntry {

    @Override
    public ResourceLocation id() {
        return EnchantmentIndustry.genRL("custom_name");
    }

    @Override
    public boolean match(ItemStack toPrint) {
        // Match name tags that have a custom name set
        if (!toPrint.is(Items.NAME_TAG))
            return false;
        return toPrint.hasCustomHoverName();
    }

    @Override
    public boolean valid(ItemStack target, ItemStack tested) {
        // Can print onto any item that doesn't already have this exact name
        if (!target.hasCustomHoverName())
            return false;
        Component targetName = target.getHoverName();
        Component testedName = tested.getHoverName();
        return !targetName.equals(testedName);
    }

    @Override
    public int requiredInkAmount(ItemStack target) {
        return CeiConfigs.SERVER.copyCustomNameCost.get();
    }

    @Override
    public Fluid requiredInkType(ItemStack target) {
        return CeiFluids.EXPERIENCE.get();
    }

    @Override
    public ItemStack print(ItemStack target, ItemStack material) {
        ItemStack result = material.copy();
        Component name = target.getHoverName();
        // Apply the custom name with styling preserved
        result.setHoverName(name);
        return result;
    }

    @Override
    public boolean isTooExpensive(ItemStack target, int limit) {
        return CeiConfigs.SERVER.copyCustomNameCost.get() > limit;
    }

    @Override
    public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
        LANG.translate("gui.goggles.printer.custom_name").forGoggles(tooltip, 1);
        Component name = target.getHoverName();
        tooltip.add(Component.literal("     ")
                .append(name.copy().withStyle(ChatFormatting.GREEN)));
        boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
        if (tooExpensive)
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.too_expensive").component()
            ).withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.xp_consumption",
                    String.valueOf(CeiConfigs.SERVER.copyCustomNameCost.get())).component()
            ).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public MutableComponent getDisplaySourceContent(ItemStack target) {
        return LANG.builder()
                .add(Component.translatable(target.getDescriptionId()))
                .text(" / ")
                .add(LANG.itemName(target))
                .component();
    }
}
