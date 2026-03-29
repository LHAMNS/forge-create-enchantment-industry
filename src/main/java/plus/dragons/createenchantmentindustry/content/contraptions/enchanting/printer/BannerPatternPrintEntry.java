package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/**
 * Banner Pattern Printing - prints banner patterns onto banners using dye fluids.
 * <p>
 * The target banner must have exactly one pattern (the one to print).
 * The material banner receives that pattern layer.
 * <p>
 * Ported from 1.21.1 BannerPatternPrintingBehavior. Uses NBT instead of DataComponents.
 * In 1.20.1, banner patterns are stored in BlockEntityTag -> Patterns as a ListTag.
 */
public class BannerPatternPrintEntry implements PrintEntry {

    @Override
    public ResourceLocation id() {
        return EnchantmentIndustry.genRL("banner_pattern");
    }

    @Override
    public boolean match(ItemStack toPrint) {
        if (!toPrint.is(ItemTags.BANNERS))
            return false;
        ListTag patterns = getPatterns(toPrint);
        // Must have exactly 1 pattern to serve as a print source
        return patterns != null && patterns.size() == 1;
    }

    @Override
    public boolean valid(ItemStack target, ItemStack tested) {
        if (!tested.is(ItemTags.BANNERS))
            return false;
        ListTag targetPatterns = getPatterns(target);
        if (targetPatterns == null || targetPatterns.isEmpty())
            return false;
        // Can print onto banners with fewer than 6 patterns (vanilla limit)
        ListTag testedPatterns = getPatterns(tested);
        int testedCount = testedPatterns == null ? 0 : testedPatterns.size();
        if (testedCount >= 6)
            return false;
        // Check if the last pattern is already the same
        if (testedPatterns != null && !testedPatterns.isEmpty()) {
            CompoundTag lastPattern = testedPatterns.getCompound(testedPatterns.size() - 1);
            CompoundTag sourcePattern = targetPatterns.getCompound(0);
            if (lastPattern.getString("Pattern").equals(sourcePattern.getString("Pattern"))
                    && lastPattern.getInt("Color") == sourcePattern.getInt("Color")) {
                return false; // Already has this pattern
            }
        }
        return true;
    }

    @Override
    public int requiredInkAmount(ItemStack target) {
        return CeiConfigs.SERVER.copyBannerPatternCost.get();
    }

    @Override
    public Fluid requiredInkType(ItemStack target) {
        // Use experience fluid for banner patterns
        return CeiFluids.EXPERIENCE.get();
    }

    @Override
    public ItemStack print(ItemStack target, ItemStack material) {
        ListTag sourcePatterns = getPatterns(target);
        if (sourcePatterns == null || sourcePatterns.isEmpty())
            return material;

        CompoundTag patternToCopy = sourcePatterns.getCompound(0).copy();
        ItemStack result = material.copy();
        CompoundTag blockEntityTag = result.getOrCreateTagElement("BlockEntityTag");
        ListTag existingPatterns = blockEntityTag.getList("Patterns", 10);
        ListTag newPatterns = existingPatterns.copy();
        newPatterns.add(patternToCopy);
        blockEntityTag.put("Patterns", newPatterns);
        return result;
    }

    @Override
    public boolean isTooExpensive(ItemStack target, int limit) {
        return CeiConfigs.SERVER.copyBannerPatternCost.get() > limit;
    }

    @Override
    public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
        LANG.translate("gui.goggles.printer.banner_pattern").forGoggles(tooltip, 1);
        ListTag patterns = getPatterns(target);
        if (patterns != null && !patterns.isEmpty()) {
            CompoundTag pattern = patterns.getCompound(0);
            String patternId = pattern.getString("Pattern");
            int colorId = pattern.getInt("Color");
            DyeColor color = DyeColor.byId(colorId);
            tooltip.add(Component.literal("     ")
                    .append(Component.translatable("block.minecraft.banner." + patternId + "." + color.getName()))
                    .withStyle(ChatFormatting.GOLD));
        }
        boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
        if (tooExpensive)
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.too_expensive").component()
            ).withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.literal("     ").append(LANG.translate(
                    "gui.goggles.xp_consumption",
                    String.valueOf(CeiConfigs.SERVER.copyBannerPatternCost.get())).component()
            ).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public MutableComponent getDisplaySourceContent(ItemStack target) {
        return LANG.builder()
                .add(LANG.itemName(target))
                .text(" / ")
                .add(Component.translatable("gui.goggles.printer.banner_pattern"))
                .component();
    }

    /**
     * Get the pattern list from a banner's BlockEntityTag.
     */
    private static ListTag getPatterns(ItemStack bannerStack) {
        CompoundTag tag = bannerStack.getTagElement("BlockEntityTag");
        if (tag == null) return null;
        if (!tag.contains("Patterns", 9)) return null;
        return tag.getList("Patterns", 10);
    }
}
