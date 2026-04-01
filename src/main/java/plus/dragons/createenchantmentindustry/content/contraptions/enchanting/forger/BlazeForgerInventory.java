package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.EnchantmentLevelUtil;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inventory for the Blaze Forger.
 * <p>
 * Slots layout:
 * <ul>
 *   <li>0, 1 - Input slots (base item, addition item)</li>
 *   <li>2, 3 - Output slots (results after forging)</li>
 *   <li>4, 5 - Preview/result slots (computed, not directly accessible)</li>
 * </ul>
 * <p>
 * Ported from 1.21.1 BlazeForgerInventory. The 1.21.1 version uses DataComponents
 * and ItemEnchantments.Mutable; this version uses 1.20.1's Map-based enchantment system.
 */
public class BlazeForgerInventory extends ItemStackHandler {
    private final BlazeForgerBlockEntity forger;
    private int cost;
    private int mode; // 0 = merge/combine, 1 = apply enchanted book/template, 2 = strip enchantment to blank template

    public BlazeForgerInventory(BlazeForgerBlockEntity forger) {
        super(6);
        this.forger = forger;
        this.mode = 0;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public int getSlots() {
        return 4;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot > 1) return stack;
        if (!stacks.get(2).isEmpty() || !stacks.get(3).isEmpty()) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    protected void onLoad() {
        var level = forger.getLevel();
        if (level != null && !level.isClientSide)
            updateResult();
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (slot == 0 || slot == 1)
            updateResult();
        forger.notifyUpdate();
    }

    @Override
    public CompoundTag serializeNBT() {
        var nbt = super.serializeNBT();
        nbt.putInt("Cost", cost);
        nbt.putInt("Mode", mode);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        cost = nbt.getInt("Cost");
        mode = nbt.getInt("Mode");
    }

    public boolean hasRemainingOutput() {
        return !stacks.get(2).isEmpty() || !stacks.get(3).isEmpty();
    }

    /**
     * Returns the experience cost in mB (fluid amount).
     * Uses vanilla's XP level-to-points formula.
     */
    protected int getExperienceCost() {
        if (cost == 0) return 0;
        // Enchantment cost as XP points (non-linear), plus repair penalty as linear addition
        int repairPenalty = stacks.get(0).getBaseRepairCost() + stacks.get(1).getBaseRepairCost();
        return Enchanting.expPointFromLevel(cost) + repairPenalty;
    }

    protected ItemStack getResult(int slot) {
        if (slot < 0 || slot >= 2) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0,2)");
        }
        return stacks.get(slot + 4);
    }

    protected void clearInput() {
        stacks.set(0, ItemStack.EMPTY);
        stacks.set(1, ItemStack.EMPTY);
        cost = 0;
    }

    /** Clear preview slots 4-5. Called on block destroy to prevent item duplication. */
    protected void clearPreview() {
        stacks.set(4, ItemStack.EMPTY);
        stacks.set(5, ItemStack.EMPTY);
    }

    protected void applyResult() {
        stacks.set(2, stacks.get(4).copy());
        stacks.set(3, stacks.get(5).copy());
        stacks.set(4, ItemStack.EMPTY);
        stacks.set(5, ItemStack.EMPTY);
        clearInput();
    }

    protected void updateResult() {
        var base = stacks.get(0).copy();
        var addition = stacks.get(1).copy();
        stacks.set(4, base);
        stacks.set(5, addition);
        cost = 0;
        mode = 0;

        if (base.isEmpty() || addition.isEmpty()) {
            return;
        }

        boolean baseIsTemplate = base.getItem() instanceof EnchantingTemplateItem;
        boolean additionIsTemplate = addition.getItem() instanceof EnchantingTemplateItem;
        boolean baseIsBook = base.is(Items.ENCHANTED_BOOK);
        boolean additionIsBook = addition.is(Items.ENCHANTED_BOOK);

        // ── Template handling ──────────────────────────────────────────

        if (baseIsTemplate) {
            EnchantingTemplateItem baseTemplate = (EnchantingTemplateItem) base.getItem();
            Map<Enchantment, Integer> baseEnchantments = EnchantingTemplateItem.hasStoredEnchantments(base)
                    ? EnchantingTemplateItem.getStoredEnchantments(base) : new LinkedHashMap<>();

            if (additionIsTemplate) {
                EnchantingTemplateItem addTemplate = (EnchantingTemplateItem) addition.getItem();
                // Super enchanting mode type check
                if (forger.hyper() && (!baseTemplate.isSpecial() || !addTemplate.isSpecial())) return;

                Map<Enchantment, Integer> additionEnchantments = EnchantingTemplateItem.hasStoredEnchantments(addition)
                        ? EnchantingTemplateItem.getStoredEnchantments(addition) : new LinkedHashMap<>();

                if (additionEnchantments.isEmpty()) {
                    // Addition is blank template -> strip enchantment from base to addition
                    if (!splitEnchantments(base, addition, baseEnchantments)) return;
                } else if (!baseEnchantments.isEmpty()) {
                    // Both have enchantments -> combine templates
                    if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments))
                        stacks.set(5, ItemStack.EMPTY);
                    else return;
                } else {
                    // Base is blank, addition has enchantments -> nothing to do
                    cost = 0;
                    return;
                }
            } else if (EnchantingTemplateItem.hasStoredEnchantments(base)) {
                // Template with enchantments + non-template item -> apply template to item
                Map<Enchantment, Integer> additionEnchantments = Enchanting.getAllEnchantments(addition);
                if (applyEnchantments(addition, additionEnchantments, baseEnchantments)) {
                    stacks.set(4, addition);
                    stacks.set(5, ItemStack.EMPTY);
                    mode = 1;
                    applyRepairCost(addition, base);
                    return;
                } else {
                    cost = 0;
                    return;
                }
            } else {
                cost = 0;
                return;
            }
        } else if (additionIsTemplate) {
            EnchantingTemplateItem addTemplate = (EnchantingTemplateItem) addition.getItem();
            if (forger.hyper() && !addTemplate.isSpecial()) return;

            if (EnchantingTemplateItem.hasStoredEnchantments(addition)) {
                // Non-template base + template with enchantments -> apply template to base
                Map<Enchantment, Integer> templateEnchantments = EnchantingTemplateItem.getStoredEnchantments(addition);
                Map<Enchantment, Integer> baseEnchantments = Enchanting.getAllEnchantments(base);
                if (applyEnchantments(base, baseEnchantments, templateEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                    mode = 1;
                    applyRepairCost(base, addition);
                    return;
                } else {
                    cost = 0;
                    return;
                }
            } else {
                // Non-template base + blank template -> strip enchantment from base
                Map<Enchantment, Integer> baseEnchantments;
                if (baseIsBook) {
                    baseEnchantments = EnchantmentHelper.deserializeEnchantments(
                            base.getOrCreateTag().getList("StoredEnchantments", 10));
                    if (baseEnchantments.isEmpty()) {
                        baseEnchantments = EnchantmentHelper.getEnchantments(base);
                    }
                } else {
                    baseEnchantments = EnchantmentHelper.getEnchantments(base);
                }
                if (baseEnchantments.isEmpty()) {
                    cost = 0;
                    return;
                }

                // For enchanted books with a single enchantment, convert back to a plain book
                if (baseIsBook && baseEnchantments.size() == 1) {
                    var entry = baseEnchantments.entrySet().iterator().next();
                    ItemStack plainBook = new ItemStack(Items.BOOK);
                    Map<Enchantment, Integer> templateEnch = new LinkedHashMap<>();
                    templateEnch.put(entry.getKey(), entry.getValue());
                    EnchantingTemplateItem.setStoredEnchantments(templateEnch, addition);
                    stacks.set(4, plainBook);
                    stacks.set(5, addition);
                    int anvilCost = entry.getKey().getRarity().ordinal() + 1;
                    cost += Math.max(1, anvilCost * 2) * entry.getValue();
                    mode = 2;
                } else {
                    if (!splitEnchantments(base, addition, baseEnchantments)) return;
                }
            }
        }
        // ── Non-template handling ─────────────────────────────────────
        else {
            Map<Enchantment, Integer> baseEnchantments;
            if (baseIsBook) {
                baseEnchantments = EnchantmentHelper.deserializeEnchantments(
                        base.getOrCreateTag().getList("StoredEnchantments", 10));
                if (baseEnchantments.isEmpty()) {
                    baseEnchantments = EnchantmentHelper.getEnchantments(base);
                }
            } else {
                baseEnchantments = EnchantmentHelper.getEnchantments(base);
            }
            Map<Enchantment, Integer> additionEnchantments;
            if (additionIsBook) {
                additionEnchantments = EnchantmentHelper.deserializeEnchantments(
                        addition.getOrCreateTag().getList("StoredEnchantments", 10));
                if (additionEnchantments.isEmpty()) {
                    additionEnchantments = EnchantmentHelper.getEnchantments(addition);
                }
            } else {
                additionEnchantments = EnchantmentHelper.getEnchantments(addition);
            }

            // Both are enchanted books - merge them
            if (baseIsBook && additionIsBook) {
                if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                } else {
                    cost = 0;
                    return;
                }
            }
            // Base is an item, addition is an enchanted book - apply book to item
            else if (!baseIsBook && additionIsBook) {
                if (applyEnchantments(base, baseEnchantments, additionEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                    mode = 1;
                } else {
                    cost = 0;
                    return;
                }
            }
            // Same item type - combine (repair + merge enchantments)
            else if (ItemStack.isSameItem(base, addition) && !baseIsBook) {
                if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                } else {
                    cost = 0;
                    return;
                }
            }
            // Base is a plain book + addition is template with enchantments -> make enchanted book
            else if (base.is(Items.BOOK) && additionIsTemplate && EnchantingTemplateItem.hasStoredEnchantments(addition)) {
                Map<Enchantment, Integer> templateEnchantments = EnchantingTemplateItem.getStoredEnchantments(addition);
                if (applyEnchantmentsToBook(base, templateEnchantments)) {
                    stacks.set(5, ItemStack.EMPTY);
                    mode = 1;
                } else {
                    cost = 0;
                    return;
                }
            }
            // Otherwise: invalid combination
            else {
                cost = 0;
                return;
            }
        }

        applyRepairCost(base, addition);
    }

    /**
     * Strip the first enchantment from the base item onto a blank EnchantingTemplate.
     * Equivalent to upstream's splitEnchantments (mode 2).
     *
     * @param base               the enchanted item/book/template to strip from
     * @param blankTemplate      the blank template to receive the enchantment
     * @param baseEnchantments   the enchantments currently on the base item
     * @return true if stripping was successful
     */
    protected boolean splitEnchantments(ItemStack base, ItemStack blankTemplate, Map<Enchantment, Integer> baseEnchantments) {
        mode = 2;
        if (baseEnchantments.isEmpty())
            return false;

        // Sort enchantments by registry ID for deterministic ordering
        var registry = ForgeRegistries.ENCHANTMENTS;
        var sorted = baseEnchantments.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> {
                    var id = registry.getKey(e.getKey());
                    return id != null ? id.hashCode() : 0;
                }))
                .toList();

        // In non-hyper mode, skip curses
        Enchantment enchantment = null;
        int level = 0;
        for (var entry : sorted) {
            if (!forger.hyper() && entry.getKey().isCurse())
                continue;
            enchantment = entry.getKey();
            level = entry.getValue();
            break;
        }

        if (enchantment == null)
            return false;

        // Remove the enchantment from base
        Map<Enchantment, Integer> remaining = new LinkedHashMap<>(baseEnchantments);
        remaining.remove(enchantment);

        // If base is an EnchantingTemplate, use the template's stored enchantments
        if (base.getItem() instanceof EnchantingTemplateItem) {
            EnchantingTemplateItem.setStoredEnchantments(remaining, base);
        } else if (base.is(Items.ENCHANTED_BOOK)) {
            // For enchanted books, update stored enchantments
            if (remaining.isEmpty()) {
                // Convert to plain book if no enchantments remain
                base = new ItemStack(Items.BOOK);
                stacks.set(4, base);
            } else {
                setEnchantmentsCorrectly(remaining, base);
                stacks.set(4, base);
            }
        } else {
            EnchantmentHelper.setEnchantments(remaining, base);
            stacks.set(4, base);
        }

        // Clamp level in non-hyper mode to the enchantment's natural max
        if (!forger.hyper()) {
            int maxLevel = EnchantmentLevelUtil.getMaxLevel(enchantment);
            level = Math.min(level, maxLevel);
        }

        // Add the enchantment to the blank template
        Map<Enchantment, Integer> templateEnch = new LinkedHashMap<>();
        templateEnch.put(enchantment, level);
        EnchantingTemplateItem.setStoredEnchantments(templateEnch, blankTemplate);
        stacks.set(5, blankTemplate);

        // Calculate cost based on enchantment rarity and level
        // Use per-enchantment multiplier if available, otherwise global
        double multiplier = CeiDataMaps.getSplittingCostMultiplier(enchantment);
        int anvilCost = enchantment.getRarity().ordinal() + 1;
        cost += (int) (Math.max(1, anvilCost * 2) * level * multiplier);

        return true;
    }

    /**
     * Apply template enchantments directly to a plain book, creating an enchanted book.
     * Equivalent to upstream's applyEnchantmentsToBook.
     */
    protected boolean applyEnchantmentsToBook(ItemStack book, Map<Enchantment, Integer> templateEnchantments) {
        mode = 1;
        if (templateEnchantments.isEmpty())
            return false;

        int addedCost = 0;
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> resultEnchantments = new LinkedHashMap<>();

        boolean hyper = forger.hyper();

        for (Map.Entry<Enchantment, Integer> entry : templateEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            boolean applicable = true;

            // Check compatibility
            for (Enchantment existing : resultEnchantments.keySet()) {
                if (!existing.equals(enchantment) && !existing.isCompatibleWith(enchantment)) {
                    if (hyper && CeiConfigs.SERVER.enableHyperEnchant.get()) {
                        addedCost++;
                    } else {
                        applicable = false;
                    }
                    break;
                }
            }

            if (applicable) {
                resultEnchantments.put(enchantment, entry.getValue());
                int anvilCost = enchantment.getRarity().ordinal() + 1;
                anvilCost = Math.max(1, anvilCost / 2);
                addedCost += anvilCost * entry.getValue();
            }
        }

        if (resultEnchantments.isEmpty())
            return false;

        setEnchantmentsCorrectly(resultEnchantments, enchantedBook);
        stacks.set(4, enchantedBook);
        this.cost += addedCost;
        return true;
    }

    /**
     * Apply enchantments from addition to base item (like applying an enchanted book).
     */
    protected boolean applyEnchantments(ItemStack base, Map<Enchantment, Integer> baseEnchantments, Map<Enchantment, Integer> additionEnchantments) {
        int addedCost = 0;
        Map<Enchantment, Integer> resultEnchantments = new LinkedHashMap<>(baseEnchantments);
        boolean applied = false;

        boolean hyper = forger.hyper();

        for (Map.Entry<Enchantment, Integer> entry : additionEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int baseLevel = resultEnchantments.getOrDefault(enchantment, 0);
            int additionLevel = entry.getValue();
            int resultLevel = baseLevel == additionLevel ? additionLevel + 1 : Math.max(additionLevel, baseLevel);

            // Check if enchantment can be applied to the item
            boolean applicable = enchantment.canEnchant(base) || base.is(Items.ENCHANTED_BOOK);
            // Check compatibility with existing enchantments
            for (Enchantment existing : resultEnchantments.keySet()) {
                if (!existing.equals(enchantment) && !existing.isCompatibleWith(enchantment)) {
                    if (hyper && CeiConfigs.SERVER.enableHyperEnchant.get()) {
                        // Hyper mode allows conflicting enchantments
                        addedCost++;
                    } else {
                        applicable = false;
                    }
                    break;
                }
            }

            if (applicable) {
                applied = true;
                int maxLevel = EnchantmentLevelUtil.getMaxLevel(enchantment);
                int extension = CeiDataMaps.getEffectiveSuperEnchantingLevelExtension(
                        enchantment, CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get());
                int extendedMaxLevel = maxLevel + extension;

                if (resultLevel > extendedMaxLevel) {
                    resultLevel = extendedMaxLevel;
                } else if (resultLevel > maxLevel && !hyper) {
                    resultLevel = maxLevel;
                }

                resultEnchantments.put(enchantment, resultLevel);

                int anvilCost = enchantment.getRarity().ordinal() + 1;
                anvilCost = Math.max(1, anvilCost / 2);
                addedCost += anvilCost * resultLevel;
            }
        }

        if (!applied)
            return false;

        setEnchantmentsCorrectly(resultEnchantments, base);
        stacks.set(4, base);
        this.cost += addedCost;
        return true;
    }

    /**
     * Combine two items of the same type - merge enchantments and repair durability.
     */
    protected boolean combineEnchantments(ItemStack base, ItemStack addition, Map<Enchantment, Integer> baseEnchantments, Map<Enchantment, Integer> additionEnchantments) {
        boolean applied = false;
        boolean hyper = forger.hyper();

        // Durability repair for non-book items
        if (base.isDamaged()) {
            int baseDurability = base.getMaxDamage() - base.getDamageValue();
            int additionDurability = addition.getMaxDamage() - addition.getDamageValue();
            int fix = additionDurability + base.getMaxDamage() * 12 / 100;
            int resultDurability = baseDurability + fix;
            int resultDamage = base.getMaxDamage() - resultDurability;
            if (resultDamage < 0) resultDamage = 0;

            if (resultDamage < base.getDamageValue()) {
                base.setDamageValue(resultDamage);
                cost += 2;
                applied = true;
            }
        }

        // Merge enchantments
        Map<Enchantment, Integer> resultEnchantments = new LinkedHashMap<>(baseEnchantments);
        int addedCost = 0;

        for (Map.Entry<Enchantment, Integer> entry : additionEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int baseLevel = resultEnchantments.getOrDefault(enchantment, 0);
            int additionLevel = entry.getValue();
            int resultLevel = baseLevel == additionLevel ? additionLevel + 1 : Math.max(additionLevel, baseLevel);

            boolean applicable = enchantment.canEnchant(base) || base.is(Items.ENCHANTED_BOOK);
            for (Enchantment existing : resultEnchantments.keySet()) {
                if (!existing.equals(enchantment) && !existing.isCompatibleWith(enchantment)) {
                    if (hyper && CeiConfigs.SERVER.enableHyperEnchant.get()) {
                        addedCost++;
                    } else {
                        applicable = false;
                    }
                    break;
                }
            }

            if (applicable) {
                applied = true;
                int maxLevel = EnchantmentLevelUtil.getMaxLevel(enchantment);
                int extension = CeiDataMaps.getEffectiveSuperEnchantingLevelExtension(
                        enchantment, CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get());
                int extendedMaxLevel = maxLevel + extension;

                if (resultLevel > extendedMaxLevel) {
                    resultLevel = extendedMaxLevel;
                } else if (resultLevel > maxLevel && !hyper) {
                    resultLevel = maxLevel;
                }

                resultEnchantments.put(enchantment, resultLevel);

                int anvilCost = enchantment.getRarity().ordinal() + 1;
                anvilCost = Math.max(1, anvilCost / 2);
                addedCost += anvilCost * resultLevel;
            }
        }

        if (!applied)
            return false;

        setEnchantmentsCorrectly(resultEnchantments, base);
        stacks.set(4, base);
        this.cost += addedCost;
        return true;
    }

    /**
     * Delegates to the shared {@link Enchanting#setAllEnchantments(Map, ItemStack)} utility
     * which correctly writes to "StoredEnchantments" for enchanted books.
     */
    private static void setEnchantmentsCorrectly(Map<Enchantment, Integer> enchantments, ItemStack stack) {
        Enchanting.setAllEnchantments(enchantments, stack);
    }

    protected void applyRepairCost(ItemStack base, ItemStack addition) {
        int baseCost = base.getBaseRepairCost();
        int additionCost = addition.getBaseRepairCost();
        int resultCost = baseCost + additionCost + 1;
        base.setRepairCost(resultCost);
        stacks.set(4, base);
    }

    boolean forgingCompleted() {
        return (!stacks.get(2).isEmpty() || !stacks.get(3).isEmpty()) && forger.processingTime == -1;
    }

    boolean notEnoughItemToForge() {
        return stacks.get(0).isEmpty() || stacks.get(1).isEmpty();
    }
}
