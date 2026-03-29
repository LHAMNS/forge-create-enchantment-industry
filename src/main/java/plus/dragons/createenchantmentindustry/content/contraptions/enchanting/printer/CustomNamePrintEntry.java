package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/**
 * Custom Name Printing - prints a name tag's custom name onto any item.
 * <p>
 * Ported from 1.21.1 CustomNamePrintingBehaviour with full dye color styling support.
 * When a fluid with a registered style (via {@link CeiDataMaps#registerCustomNameStyle})
 * is used, the custom name will be styled with that color. Experience fluid prints
 * the name without additional color styling.
 * <p>
 * The fluid-to-style and fluid-to-cost mappings are managed by CeiDataMaps,
 * equivalent to the upstream's NeoForge DataMaps (PRINTING_CUSTOM_NAME_INGREDIENT
 * and PRINTING_CUSTOM_NAME_STYLE).
 */
public class CustomNamePrintEntry implements PrintEntry {

    @Override
    public ResourceLocation id() {
        return EnchantmentIndustry.genRL("custom_name");
    }

    @Override
    public boolean isEnabled() {
        return CeiConfigs.SERVER.enableCustomNamePrinting.get();
    }

    @Override
    public boolean match(ItemStack toPrint) {
        if (!isEnabled()) return false;
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
    public boolean acceptsFluid(FluidStack fluidStack, ItemStack target) {
        // Accept any fluid that is registered as a custom name ink
        return CeiDataMaps.isCustomNameInk(fluidStack.getFluid());
    }

    @Override
    public ItemStack print(ItemStack target, ItemStack material) {
        // Fallback without fluid info - just copy the name
        ItemStack result = material.copy();
        Component name = target.getHoverName();
        result.setHoverName(name);
        return result;
    }

    @Override
    public ItemStack print(ItemStack target, ItemStack material, FluidStack fluid) {
        ItemStack result = material.copy();
        MutableComponent name = target.getHoverName().copy();

        // Apply style from the fluid if one is registered
        Style fluidStyle = CeiDataMaps.getCustomNameStyle(fluid.getFluid());
        if (fluidStyle != null) {
            name.withStyle(fluidStyle);
        }

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
