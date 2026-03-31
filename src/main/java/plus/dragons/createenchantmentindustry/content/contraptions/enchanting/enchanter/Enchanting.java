package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class Enchanting {

    public static final TagKey<Item> UNENCHANTABLE =
            TagKey.create(Registries.ITEM, EnchantmentIndustry.genRL("unenchantable"));
    /** Thread-safe list of conditions. Mods may add to this during initialization. */
    public static final List<Predicate<ItemStack>> UNENCHANTABLE_CONDITIONS = new CopyOnWriteArrayList<>();

    /**
     * Retrieves enchantments from an ItemStack, correctly handling enchanted books.
     * {@link EnchantmentHelper#getEnchantments(ItemStack)} reads the {@code "Enchantments"}
     * NBT tag, but enchanted books store theirs under {@code "StoredEnchantments"}.
     * This method checks the stored tag first for enchanted books and falls back to the
     * standard tag if the stored map is empty (defensive against unusual NBT states).
     */
    public static Map<Enchantment, Integer> getAllEnchantments(ItemStack itemStack) {
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> stored = EnchantmentHelper.deserializeEnchantments(
                    itemStack.getOrCreateTag().getList("StoredEnchantments", 10));
            if (!stored.isEmpty()) return stored;
        }
        return EnchantmentHelper.getEnchantments(itemStack);
    }

    @Nullable
    public static EnchantmentEntry getTargetEnchantment(ItemStack itemStack, boolean hyper) {
        if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
            var result = EnchantingGuideItem.getEnchantment(itemStack);
            if (!hyper || result == null)
                return result;
            else {
                var enchantment = result.getFirst();
                int level = result.getSecond() + 1;
                return EnchantmentEntry.of(enchantment, level);
            }
        } else
            throw new RuntimeException("TargetItem is not an enchanting guide for blaze!");
    }
    
    @Nullable
    public static EnchantmentEntry getValidEnchantment(ItemStack itemStack, ItemStack targetItem, boolean hyper) {

        if(itemStack.is(UNENCHANTABLE)) return null;
        if(!UNENCHANTABLE_CONDITIONS.isEmpty()){
            if(UNENCHANTABLE_CONDITIONS.stream()
                    .anyMatch(p -> p.test(itemStack))) return null;
        }

        var entry = getTargetEnchantment(targetItem, hyper);
        if (entry == null || !entry.valid())
            return null;
        var enchantment = entry.getFirst();

        // Check enchantment tag availability
        if (hyper) {
            if (!CeiTags.isAvailableForSuperEnchanting(enchantment))
                return null;
        } else {
            if (!CeiTags.isAvailableForNormalEnchanting(enchantment))
                return null;
        }

        ItemStack toCheck = itemStack.copy();
        Map<Enchantment, Integer> modified = EnchantmentHelper.getEnchantments(toCheck);

        if (modified.containsKey(enchantment) && modified.get(enchantment) >= entry.getSecond()) {
            return null;
        }

        // If the item already has the enchantment remove it to pass the checks
        modified.remove(enchantment);
        EnchantmentHelper.setEnchantments(modified, toCheck);

        if (!enchantment.canEnchant(toCheck))
            return null;
        if (!CeiConfigs.SERVER.ignoreEnchantmentCompatibility.get()) {
            for (var e : modified.entrySet()) {
                if (!e.getKey().isCompatibleWith(enchantment))
                    return null;
            }
        }
        return entry;
    }

    public static void enchantItem(ItemStack itemStack, Pair<Enchantment, Integer> enchantment) {
        var map = EnchantmentHelper.getEnchantments(itemStack);
        map.put(enchantment.getFirst(), enchantment.getSecond());
        EnchantmentHelper.setEnchantments(map, itemStack);
    }
    
    public static int expPointFromLevel(int level) {
        if (level > 31) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else {
            return level > 16
                ? (int) (2.5 * level * level - 40.5 * level + 360)
                : level * level + 6 * level;
        }
    }
    
    public static int expPointForNextLevel(int level) {
        if (level > 30) {
            return 9 * level - 158;
        } else {
            return level > 15
                ? 5 * level -38
                : 2 * level + 7;
        }
    }

    public static int rarityLevel(Enchantment.Rarity rarity) {
        return switch(rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case VERY_RARE -> 4;
        };
    }

    public static int getExperienceConsumption(Enchantment enchantment, int level) {
        int xpLevel = enchantment.getMinCost(level) + level * rarityLevel(enchantment.getRarity());
        return expPointForNextLevel(xpLevel);
    }

    /**
     * When in cursed mode (lightning rod deflecting lightning in hyper mode),
     * apply a random curse enchantment to the item alongside the normal enchantment.
     * This matches the upstream behaviour where curses get mixed into the enchantment pool.
     */
    public static void applyCurseEnchantment(ItemStack itemStack, Random random) {
        List<Enchantment> curses = new ArrayList<>();
        for (Enchantment enchantment : net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS) {
            if (enchantment.isCurse() && enchantment.canEnchant(itemStack)) {
                curses.add(enchantment);
            }
        }
        if (!curses.isEmpty()) {
            Enchantment curse = curses.get(random.nextInt(curses.size()));
            Map<Enchantment, Integer> existing = EnchantmentHelper.getEnchantments(itemStack);
            if (!existing.containsKey(curse)) {
                existing.put(curse, 1);
                EnchantmentHelper.setEnchantments(existing, itemStack);
            }
        }
    }

}
