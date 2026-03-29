package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.ItemStackHandler;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.EnchantmentLevelUtil;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingTemplateItem;
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
    private int mode; // 0 = merge/combine, 1 = apply enchanted book, 2 = strip (not implemented in 1.20.1 without templates)

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
        return Enchanting.expPointFromLevel(cost);
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

    protected void applyResult() {
        stacks.set(2, stacks.get(4).copy());
        stacks.set(3, stacks.get(5).copy());
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

        // Case 0: EnchantingTemplate with stored enchantments - apply template enchantments to item
        if (addition.getItem() instanceof EnchantingTemplateItem && EnchantingTemplateItem.hasStoredEnchantments(addition)) {
            Map<Enchantment, Integer> templateEnchantments = EnchantingTemplateItem.getStoredEnchantments(addition);
            Map<Enchantment, Integer> baseEnchantments = EnchantmentHelper.getEnchantments(base);
            if (applyEnchantments(base, baseEnchantments, templateEnchantments)) {
                stacks.set(5, ItemStack.EMPTY);
                mode = 1;
                applyRepairCost(base, addition);
                return;
            } else {
                cost = 0;
                return;
            }
        }
        if (base.getItem() instanceof EnchantingTemplateItem && EnchantingTemplateItem.hasStoredEnchantments(base)) {
            Map<Enchantment, Integer> templateEnchantments = EnchantingTemplateItem.getStoredEnchantments(base);
            Map<Enchantment, Integer> additionEnchantments = EnchantmentHelper.getEnchantments(addition);
            if (applyEnchantments(addition, additionEnchantments, templateEnchantments)) {
                stacks.set(4, addition);
                stacks.set(5, ItemStack.EMPTY);
                mode = 1;
                applyRepairCost(addition, base);
                return;
            } else {
                cost = 0;
                return;
            }
        }

        Map<Enchantment, Integer> baseEnchantments = EnchantmentHelper.getEnchantments(base);
        Map<Enchantment, Integer> additionEnchantments = EnchantmentHelper.getEnchantments(addition);

        boolean baseIsBook = base.is(Items.ENCHANTED_BOOK);
        boolean additionIsBook = addition.is(Items.ENCHANTED_BOOK);

        // Case 1: Both are enchanted books - merge them
        if (baseIsBook && additionIsBook) {
            if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                stacks.set(5, ItemStack.EMPTY);
            } else {
                cost = 0;
                return;
            }
        }
        // Case 2: Base is an item, addition is an enchanted book - apply book to item
        else if (!baseIsBook && additionIsBook) {
            if (applyEnchantments(base, baseEnchantments, additionEnchantments)) {
                stacks.set(5, ItemStack.EMPTY);
                mode = 1;
            } else {
                cost = 0;
                return;
            }
        }
        // Case 3: Same item type - combine (repair + merge enchantments)
        else if (ItemStack.isSameItem(base, addition) && !baseIsBook) {
            if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                stacks.set(5, ItemStack.EMPTY);
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

        applyRepairCost(base, addition);
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
                int extendedMaxLevel = maxLevel + CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get();

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

        EnchantmentHelper.setEnchantments(resultEnchantments, base);
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
                int extendedMaxLevel = maxLevel + CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get();

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

        EnchantmentHelper.setEnchantments(resultEnchantments, base);
        stacks.set(4, base);
        this.cost += addedCost;
        return true;
    }

    protected void applyRepairCost(ItemStack base, ItemStack addition) {
        int baseCost = base.getBaseRepairCost();
        int additionCost = addition.getBaseRepairCost();
        int resultCost = baseCost + additionCost + 1;
        base.setRepairCost(resultCost);
        stacks.set(4, base);
    }

    boolean forgingCompleted() {
        return !stacks.get(2).isEmpty() && forger.processingTime == -1;
    }

    boolean notEnoughItemToForge() {
        return stacks.get(0).isEmpty() || stacks.get(1).isEmpty();
    }
}
