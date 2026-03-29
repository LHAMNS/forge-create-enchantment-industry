package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forge 1.20.1 equivalent of NeoForge DataMaps for CEI.
 * <p>
 * Upstream CEI (1.21.1/6.0.0-dev) uses NeoForge DataMaps to allow any mod to register
 * XP fluids and configure printer/forging/splitting costs. Since Forge 1.20.1 does not
 * have DataMaps, we use simple runtime registries backed by HashMaps.
 * <p>
 * Other mods can call {@link #registerXpFluid(Fluid, int)} during FMLCommonSetupEvent
 * (enqueueWork) to add their own experience fluids.
 */
public class CeiDataMaps {

    // ── ExperienceFuel ──────────────────────────────────────────────────
    // Forge 1.20.1 equivalent of the upstream NeoForge DataMap<Item, ExperienceFuel>.
    // Maps an Item -> ExperienceFuelEntry so that players can right-click fuel items
    // into the Blaze Enchanter or Blaze Forger to supply experience.

    /**
     * A fuel entry that an item can provide when right-clicked into a blaze machine.
     * @param experience  XP amount this item provides (in mB of experience fluid)
     * @param special     if true, fills the special (hyper) tank instead of normal
     * @param usingConvertTo  optional remainder item after using the fuel (e.g., empty bucket)
     */
    public record ExperienceFuelEntry(int experience, boolean special, Optional<ItemStack> usingConvertTo) {
        public static ExperienceFuelEntry normal(int experience) {
            return new ExperienceFuelEntry(experience, false, Optional.empty());
        }
        public static ExperienceFuelEntry normal(int experience, ItemStack convertTo) {
            return new ExperienceFuelEntry(experience, false, Optional.of(convertTo));
        }
        public static ExperienceFuelEntry special(int experience) {
            return new ExperienceFuelEntry(experience, true, Optional.empty());
        }
        public static ExperienceFuelEntry special(int experience, ItemStack convertTo) {
            return new ExperienceFuelEntry(experience, true, Optional.of(convertTo));
        }
    }

    private static final Map<Item, ExperienceFuelEntry> EXPERIENCE_FUEL = new LinkedHashMap<>();

    // ── XP Fluid Units ──────────────────────────────────────────────────
    // Maps a Fluid -> XP per mB. For example, EXPERIENCE=1, HYPER_EXPERIENCE=10.
    private static final Map<Fluid, Integer> XP_FLUID_UNITS = new LinkedHashMap<>();

    // ── Printer Fluid Cost Multipliers ──────────────────────────────────
    // Maps a Fluid -> cost multiplier for printer operations.
    // Default: EXPERIENCE=1.0, HYPER_EXPERIENCE uses its xpRatio inverse.
    private static final Map<Fluid, Double> PRINTER_COST_MULTIPLIERS = new LinkedHashMap<>();

    // ── Forging Cost Multiplier ─────────────────────────────────────────
    private static double forgingCostMultiplier = 1.0;
    // Per-enchantment forging cost multipliers (overrides global when present)
    private static final Map<net.minecraft.world.item.enchantment.Enchantment, Float> PER_ENCHANTMENT_FORGING_COST = new LinkedHashMap<>();

    // ── Splitting Cost Multiplier ───────────────────────────────────────
    private static double splittingCostMultiplier = 1.0;
    // Per-enchantment splitting cost multipliers (overrides global when present)
    private static final Map<net.minecraft.world.item.enchantment.Enchantment, Float> PER_ENCHANTMENT_SPLITTING_COST = new LinkedHashMap<>();

    // ── Super Enchanting Level Extension ─────────────────────────────────
    // Per-enchantment max level extension (overrides config maxHyperEnchantingLevelExtension)
    private static final Map<net.minecraft.world.item.enchantment.Enchantment, Integer> PER_ENCHANTMENT_SUPER_LEVEL_EXT = new LinkedHashMap<>();

    // ── Per-PrintEntry Fluid -> cost override ─────────────────────────────
    // Maps a print entry ID -> (Fluid -> cost) to allow data-driven overrides of
    // printer costs per print type and per fluid. Equivalent to upstream's
    // per-print-type DataMaps (PRINTING_ENCHANTED_BOOK, PRINTING_WRITTEN_BOOK, etc.).
    private static final Map<String, Map<Fluid, Integer>> PRINT_TYPE_COSTS = new LinkedHashMap<>();

    // ── Custom Name Printing: Fluid -> ink amount required ──────────────
    // Maps a Fluid to the amount of fluid required for custom name printing.
    // Equivalent to upstream's PRINTING_CUSTOM_NAME_INGREDIENT DataMap.
    private static final Map<Fluid, Integer> CUSTOM_NAME_INK_AMOUNT = new LinkedHashMap<>();

    // ── Custom Name Printing: Fluid -> text style ───────────────────────
    // Maps a Fluid to the Style to apply to the printed custom name.
    // Equivalent to upstream's PRINTING_CUSTOM_NAME_STYLE DataMap.
    private static final Map<Fluid, Style> CUSTOM_NAME_STYLE = new LinkedHashMap<>();

    /**
     * Called during mod initialization to register the default CEI fluids.
     * Must be called after CeiFluids.register().
     */
    public static void register() {
        // Default experience fluid: 1 mB = 1 XP
        registerXpFluid(CeiFluids.EXPERIENCE.get().getSource(), 1);
        // Hyper experience fluid: 1 mB = 10 XP (matches HyperExperienceFluid xpRatio)
        registerXpFluid(CeiFluids.HYPER_EXPERIENCE.get().getSource(), 10);

        // Also register flowing variants so checks work regardless of fluid state
        registerXpFluid(CeiFluids.EXPERIENCE.get(), 1);
        registerXpFluid(CeiFluids.HYPER_EXPERIENCE.get(), 10);

        // Register experience fluid as a valid custom name printing ink
        // Upstream uses DataMaps: experience=10mB, dye fluids=250mB
        registerCustomNameInk(CeiFluids.EXPERIENCE.get().getSource(), 10);
        registerCustomNameInk(CeiFluids.EXPERIENCE.get(), 10);

        // Register default experience fuel items (matches upstream CEIDataMaps.generate)
        // Create's own items
        registerExperienceFuel(AllBlocks.EXPERIENCE_BLOCK.get().asItem(), ExperienceFuelEntry.normal(27));
        registerExperienceFuel(AllItems.EXP_NUGGET.get(), ExperienceFuelEntry.normal(3));
        // CEI items
        registerExperienceFuel(CeiBlocks.SUPER_EXPERIENCE_BLOCK.get().asItem(), ExperienceFuelEntry.special(27));
        registerExperienceFuel(CeiItems.SUPER_EXPERIENCE_NUGGET.get(), ExperienceFuelEntry.special(3));

        EnchantmentIndustry.LOGGER.debug("CeiDataMaps: Registered default XP fluids, custom name inks, and experience fuels");
    }

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Register a fluid as an XP fluid with a given XP-per-mB ratio.
     * Other mods should call this during FMLCommonSetupEvent (enqueueWork).
     *
     * @param fluid    the fluid to register
     * @param xpPerMb  how many experience points 1 mB of this fluid is worth
     */
    public static void registerXpFluid(Fluid fluid, int xpPerMb) {
        if (xpPerMb <= 0) {
            EnchantmentIndustry.LOGGER.warn("CeiDataMaps: Ignoring XP fluid registration with non-positive ratio: {}", fluid);
            return;
        }
        XP_FLUID_UNITS.put(fluid, xpPerMb);
    }

    /**
     * Unregister a fluid from the XP fluid registry.
     * Useful for mods that want to override default behavior.
     */
    public static void unregisterXpFluid(Fluid fluid) {
        XP_FLUID_UNITS.remove(fluid);
    }

    /**
     * Check if a fluid is a registered XP fluid.
     */
    public static boolean isXpFluid(Fluid fluid) {
        return XP_FLUID_UNITS.containsKey(fluid);
    }

    /**
     * Check if a FluidStack contains a registered XP fluid.
     */
    public static boolean isXpFluid(FluidStack stack) {
        return !stack.isEmpty() && isXpFluid(stack.getFluid());
    }

    /**
     * Get the XP-per-mB ratio for a fluid, or 0 if not an XP fluid.
     */
    public static int getXpPerMb(Fluid fluid) {
        return XP_FLUID_UNITS.getOrDefault(fluid, 0);
    }

    /**
     * Get the mB-per-XP ratio for a fluid (upstream semantic: FLUID_UNIT_EXPERIENCE).
     * <p>
     * Upstream CEI uses "how many mB = 1 XP" as the primary unit model.
     * This method provides the same semantic: for experience (xpPerMb=1) returns 1.0,
     * for hyper experience (xpPerMb=10) returns 0.1.
     *
     * @param fluid the fluid to query
     * @return mB required per 1 XP, or 0.0 if not an XP fluid
     */
    public static double getFluidUnitsPerXp(Fluid fluid) {
        int xpPerMb = getXpPerMb(fluid);
        if (xpPerMb <= 0) return 0.0;
        return 1.0 / xpPerMb;
    }

    /**
     * Convert an XP amount to the required fluid amount in mB for a given fluid.
     * Uses upstream-semantic conversion (mB = xp * fluidUnitsPerXp).
     *
     * @param fluid the XP fluid
     * @param xp    the experience points to convert
     * @return mB of fluid required, or 0 if not an XP fluid
     */
    public static int xpToFluidAmount(Fluid fluid, int xp) {
        int xpPerMb = getXpPerMb(fluid);
        if (xpPerMb <= 0) return 0;
        // mB = ceil(xp / xpPerMb) to avoid rounding down to 0
        return (xp + xpPerMb - 1) / xpPerMb;
    }

    /**
     * Convert a fluid amount in mB to XP for a given fluid.
     *
     * @param fluid    the XP fluid
     * @param fluidMb  the amount of fluid in mB
     * @return experience points, or 0 if not an XP fluid
     */
    public static int fluidAmountToXp(Fluid fluid, int fluidMb) {
        return fluidMb * getXpPerMb(fluid);
    }

    /**
     * Convert a fluid stack to total XP points.
     * Returns 0 if the fluid is not a registered XP fluid.
     */
    public static int getXpFromFluid(FluidStack stack) {
        if (stack.isEmpty()) return 0;
        int ratio = getXpPerMb(stack.getFluid());
        return stack.getAmount() * ratio;
    }

    /**
     * Get the ExperienceFluid instance if the fluid is one, or null.
     * This preserves backward compatibility with code that needs the
     * ExperienceFluid API (convertToOrb, drop, awardOrDrop, etc.).
     */
    @Nullable
    public static ExperienceFluid asExperienceFluid(Fluid fluid) {
        if (fluid instanceof ExperienceFluid expFluid) {
            return expFluid;
        }
        return null;
    }

    /**
     * Returns an unmodifiable view of all registered XP fluids.
     */
    public static Map<Fluid, Integer> getAllXpFluids() {
        return Collections.unmodifiableMap(XP_FLUID_UNITS);
    }

    // ── Experience Fuel ────────────────────────────────────────────────

    /**
     * Register an item as an experience fuel for Blaze Enchanter/Forger.
     * Other mods should call this during FMLCommonSetupEvent (enqueueWork).
     */
    public static void registerExperienceFuel(Item item, ExperienceFuelEntry fuel) {
        EXPERIENCE_FUEL.put(item, fuel);
    }

    /**
     * Get the ExperienceFuelEntry for an item, or null if it is not a registered fuel.
     */
    @Nullable
    public static ExperienceFuelEntry getExperienceFuel(Item item) {
        return EXPERIENCE_FUEL.get(item);
    }

    /**
     * Get the ExperienceFuelEntry for an ItemStack, or null if not a fuel.
     */
    @Nullable
    public static ExperienceFuelEntry getExperienceFuel(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return EXPERIENCE_FUEL.get(stack.getItem());
    }

    /**
     * Returns an unmodifiable view of all registered experience fuels.
     */
    public static Map<Item, ExperienceFuelEntry> getAllExperienceFuels() {
        return Collections.unmodifiableMap(EXPERIENCE_FUEL);
    }

    // ── Printer Cost ────────────────────────────────────────────────────

    public static void setPrinterCostMultiplier(Fluid fluid, double multiplier) {
        PRINTER_COST_MULTIPLIERS.put(fluid, multiplier);
    }

    public static double getPrinterCostMultiplier(Fluid fluid) {
        return PRINTER_COST_MULTIPLIERS.getOrDefault(fluid, 1.0);
    }

    // ── Forging Cost ────────────────────────────────────────────────────

    public static void setForgingCostMultiplier(double multiplier) {
        forgingCostMultiplier = multiplier;
    }

    public static double getForgingCostMultiplier() {
        return forgingCostMultiplier;
    }

    // ── Splitting Cost ──────────────────────────────────────────────────

    public static void setSplittingCostMultiplier(double multiplier) {
        splittingCostMultiplier = multiplier;
    }

    public static double getSplittingCostMultiplier() {
        return splittingCostMultiplier;
    }

    // ── Per-Enchantment Forging Cost ─────────────────────────────────────

    /**
     * Register a per-enchantment forging cost multiplier.
     * When present, this overrides the global forgingCostMultiplier for this enchantment.
     */
    public static void setForgingCostMultiplier(net.minecraft.world.item.enchantment.Enchantment enchantment, float multiplier) {
        PER_ENCHANTMENT_FORGING_COST.put(enchantment, multiplier);
    }

    /**
     * Get the forging cost multiplier for a specific enchantment.
     * Falls back to the global multiplier if no per-enchantment override exists.
     */
    public static double getForgingCostMultiplier(net.minecraft.world.item.enchantment.Enchantment enchantment) {
        Float perEnchantment = PER_ENCHANTMENT_FORGING_COST.get(enchantment);
        return perEnchantment != null ? perEnchantment : forgingCostMultiplier;
    }

    // ── Per-Enchantment Splitting Cost ───────────────────────────────────

    /**
     * Register a per-enchantment splitting cost multiplier.
     * When present, this overrides the global splittingCostMultiplier for this enchantment.
     */
    public static void setSplittingCostMultiplier(net.minecraft.world.item.enchantment.Enchantment enchantment, float multiplier) {
        PER_ENCHANTMENT_SPLITTING_COST.put(enchantment, multiplier);
    }

    /**
     * Get the splitting cost multiplier for a specific enchantment.
     * Falls back to the global multiplier if no per-enchantment override exists.
     */
    public static double getSplittingCostMultiplier(net.minecraft.world.item.enchantment.Enchantment enchantment) {
        Float perEnchantment = PER_ENCHANTMENT_SPLITTING_COST.get(enchantment);
        return perEnchantment != null ? perEnchantment : splittingCostMultiplier;
    }

    // ── Per-Enchantment Super Enchanting Level Extension ─────────────────

    /**
     * Register a per-enchantment super enchanting level extension.
     * When present, this overrides the global maxHyperEnchantingLevelExtension config for this enchantment.
     */
    public static void setSuperEnchantingLevelExtension(net.minecraft.world.item.enchantment.Enchantment enchantment, int extension) {
        PER_ENCHANTMENT_SUPER_LEVEL_EXT.put(enchantment, extension);
    }

    /**
     * Get the super enchanting level extension for a specific enchantment.
     * Returns -1 if no per-enchantment override exists (caller should fall back to config).
     */
    public static int getSuperEnchantingLevelExtension(net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return PER_ENCHANTMENT_SUPER_LEVEL_EXT.getOrDefault(enchantment, -1);
    }

    /**
     * Get the effective max level extension for an enchantment, checking per-enchantment
     * override first, then falling back to the global config value.
     *
     * @param enchantment the enchantment to query
     * @param globalFallback the global config value (CeiConfigs.SERVER.maxHyperEnchantingLevelExtension)
     * @return the effective level extension
     */
    public static int getEffectiveSuperEnchantingLevelExtension(net.minecraft.world.item.enchantment.Enchantment enchantment, int globalFallback) {
        int perEnchantment = getSuperEnchantingLevelExtension(enchantment);
        return perEnchantment >= 0 ? perEnchantment : globalFallback;
    }

    // ── Per-PrintEntry Cost Overrides ──────────────────────────────────

    /**
     * Register a per-print-type fluid cost override.
     * Equivalent to upstream's per-print-type DataMaps (PRINTING_ENCHANTED_BOOK, etc.).
     * <p>
     * When a PrintEntry queries its cost, it can check this registry first for a
     * fluid-specific override before falling back to config values.
     *
     * @param printEntryId the print entry ID (e.g., "create_enchantment_industry:enchanted_book")
     * @param fluid        the fluid to set the cost for
     * @param costMb       the cost in mB
     */
    public static void registerPrintTypeCost(String printEntryId, Fluid fluid, int costMb) {
        PRINT_TYPE_COSTS.computeIfAbsent(printEntryId, k -> new LinkedHashMap<>()).put(fluid, costMb);
    }

    /**
     * Get the per-print-type fluid cost override, or -1 if no override is registered.
     *
     * @param printEntryId the print entry ID
     * @param fluid        the fluid being used
     * @return the cost in mB, or -1 if no override exists
     */
    public static int getPrintTypeCost(String printEntryId, Fluid fluid) {
        Map<Fluid, Integer> costs = PRINT_TYPE_COSTS.get(printEntryId);
        if (costs == null) return -1;
        return costs.getOrDefault(fluid, -1);
    }

    /**
     * Check if a per-print-type fluid cost override exists.
     */
    public static boolean hasPrintTypeCost(String printEntryId, Fluid fluid) {
        Map<Fluid, Integer> costs = PRINT_TYPE_COSTS.get(printEntryId);
        return costs != null && costs.containsKey(fluid);
    }

    /**
     * Returns an unmodifiable view of all per-print-type cost overrides.
     */
    public static Map<String, Map<Fluid, Integer>> getAllPrintTypeCosts() {
        return Collections.unmodifiableMap(PRINT_TYPE_COSTS);
    }

    // ── Custom Name Ink ─────────────────────────────────────────────────

    /**
     * Register a fluid as a valid ink for custom name printing, with the required amount.
     * Equivalent to upstream's PRINTING_CUSTOM_NAME_INGREDIENT DataMap.
     *
     * @param fluid  the fluid to register
     * @param amount mB required per custom name print
     */
    public static void registerCustomNameInk(Fluid fluid, int amount) {
        CUSTOM_NAME_INK_AMOUNT.put(fluid, amount);
    }

    /**
     * Get the ink amount required for custom name printing with a given fluid.
     * Returns 0 if the fluid is not registered for custom name printing.
     */
    public static int getCustomNameInkAmount(Fluid fluid) {
        return CUSTOM_NAME_INK_AMOUNT.getOrDefault(fluid, 0);
    }

    /**
     * Check if a fluid is valid for custom name printing.
     */
    public static boolean isCustomNameInk(Fluid fluid) {
        return CUSTOM_NAME_INK_AMOUNT.containsKey(fluid);
    }

    /**
     * Returns an unmodifiable view of all custom name ink fluids.
     */
    public static Map<Fluid, Integer> getAllCustomNameInks() {
        return Collections.unmodifiableMap(CUSTOM_NAME_INK_AMOUNT);
    }

    // ── Custom Name Style ───────────────────────────────────────────────

    /**
     * Register a fluid-to-style mapping for custom name printing.
     * When this fluid is used, the custom name will be styled with the given Style.
     * Equivalent to upstream's PRINTING_CUSTOM_NAME_STYLE DataMap.
     *
     * @param fluid the fluid
     * @param style the Style to apply (e.g., Style.EMPTY.withColor(DyeColor.RED.getTextColor()))
     */
    public static void registerCustomNameStyle(Fluid fluid, Style style) {
        CUSTOM_NAME_STYLE.put(fluid, style);
    }

    /**
     * Get the Style associated with a fluid for custom name printing.
     * Returns null if no style is registered for this fluid.
     */
    @Nullable
    public static Style getCustomNameStyle(Fluid fluid) {
        return CUSTOM_NAME_STYLE.get(fluid);
    }

    /**
     * Returns an unmodifiable view of all custom name style mappings.
     */
    public static Map<Fluid, Style> getAllCustomNameStyles() {
        return Collections.unmodifiableMap(CUSTOM_NAME_STYLE);
    }
}
