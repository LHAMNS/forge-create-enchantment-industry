package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import java.util.Map;
import java.util.Optional;

public class GrindstoneHelper {
    public static boolean canItemBeGrinded(ItemStack top, ItemStack bottom) {
        var event = new GrindstoneEvent.OnPlaceItem(top, bottom, -1);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            return false;
        if (!event.getOutput().isEmpty())
            return true;
        return !computeResult(top, bottom).isEmpty();
    }

    public static Optional<Result> grindItem(Level level, ItemStack top, ItemStack bottom) {
        var place = new GrindstoneEvent.OnPlaceItem(top, ItemStack.EMPTY, -1);
        MinecraftForge.EVENT_BUS.post(place);
        if (place.isCanceled())
            return Optional.empty();
        int experience = place.getXp();
        var output = place.getOutput();
        if (output.isEmpty()) {
            output = computeResult(top, bottom);
            if (output.isEmpty())
                return Optional.empty();
            if (experience == -1) {
                experience = getGrindingExperience(level, top, bottom);
            }
        }
        var take = new GrindstoneEvent.OnTakeItem(top, bottom, experience);
        MinecraftForge.EVENT_BUS.post(take);
        if (take.isCanceled()) {
            return Optional.of(new Result(top, bottom, output, 0));
        }
        return Optional.of(new Result(take.getNewTopItem(), take.getNewBottomItem(), output, Math.max(take.getXp(), 0)));
    }

    private static int getGrindingExperience(Level level, ItemStack top, ItemStack bottom) {
        int experience = 0;
        experience += getExperienceFromItem(top);
        experience += getExperienceFromItem(bottom);
        if (experience > 0) {
            int average = Mth.ceil(experience / 2.0);
            return average + level.random.nextInt(average);
        } else {
            return 0;
        }
    }

    public static int getExperienceFromItem(ItemStack stack) {
        int result = 0;
        Map<Enchantment, Integer> enchantments = Enchanting.getAllEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            if (!enchantment.isCurse()) {
                result += enchantment.getMinCost(level);
            }
        }
        return result;
    }

    public static int getExperienceFromGrindingRecipe(Level level, ItemStack stack) {
        RecipeWrapper wrapper = new RecipeWrapper(new net.minecraftforge.items.ItemStackHandler(1) {{
            setStackInSlot(0, stack);
        }});
        var grinding = SequencedAssemblyRecipe.getRecipe(level, wrapper, CeiRecipeTypes.GRINDING.getType(), GrindingRecipe.class);
        if (grinding.isEmpty())
            grinding = level.getRecipeManager().getRecipeFor(CeiRecipeTypes.GRINDING.getType(), wrapper, level);
        if (grinding.isEmpty()) return 0;
        var f = grinding.get().getFluidResults();
        if (f.isEmpty()) return 0;
        return f.get(0).getAmount();
    }

    private static ItemStack computeResult(ItemStack top, ItemStack bottom) {
        boolean topEmpty = top.isEmpty();
        boolean bottomEmpty = bottom.isEmpty();
        if (topEmpty && bottomEmpty) {
            return ItemStack.EMPTY;
        } else if (top.getCount() <= 1 && bottom.getCount() <= 1) {
            if (topEmpty || bottomEmpty) {
                ItemStack input = topEmpty ? bottom : top;
                return !input.isEnchanted() && !Enchanting.getAllEnchantments(input).isEmpty()
                        ? ItemStack.EMPTY
                        : hasAnyEnchantments(input) ? removeNonCursesFrom(input.copy()) : ItemStack.EMPTY;
            } else {
                return mergeItems(top, bottom);
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static boolean hasAnyEnchantments(ItemStack stack) {
        return !Enchanting.getAllEnchantments(stack).isEmpty();
    }

    private static ItemStack mergeItems(ItemStack top, ItemStack bottom) {
        if (!top.is(bottom.getItem())) {
            return ItemStack.EMPTY;
        } else {
            int maxDamage = Math.max(top.getMaxDamage(), bottom.getMaxDamage());
            int topDurability = top.getMaxDamage() - top.getDamageValue();
            int bottomDurability = bottom.getMaxDamage() - bottom.getDamageValue();
            int l = topDurability + bottomDurability + maxDamage * 5 / 100;
            int count = 1;
            if (!top.isDamageableItem()) {
                if (top.getMaxStackSize() < 2 || !ItemStack.matches(top, bottom)) {
                    return ItemStack.EMPTY;
                }
                count = 2;
            }

            ItemStack result = top.copy();
            result.setCount(count);
            if (result.isDamageableItem()) {
                result.setDamageValue(Math.max(maxDamage - l, 0));
            }

            mergeEnchantsFrom(result, bottom);
            return removeNonCursesFrom(result);
        }
    }

    private static void mergeEnchantsFrom(ItemStack top, ItemStack bottom) {
        Map<Enchantment, Integer> topEnchantments = Enchanting.getAllEnchantments(top);
        Map<Enchantment, Integer> bottomEnchantments = Enchanting.getAllEnchantments(bottom);

        for (Map.Entry<Enchantment, Integer> entry : bottomEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.isCurse() || !topEnchantments.containsKey(enchantment)) {
                topEnchantments.merge(enchantment, entry.getValue(), Math::max);
            }
        }
        Enchanting.setAllEnchantments(topEnchantments, top);
    }

    public static ItemStack removeNonCursesFrom(ItemStack input) {
        Map<Enchantment, Integer> enchantments = Enchanting.getAllEnchantments(input);
        enchantments.entrySet().removeIf(entry -> !entry.getKey().isCurse());
        Enchanting.setAllEnchantments(enchantments, input);

        if (input.is(Items.ENCHANTED_BOOK) && enchantments.isEmpty()) {
            ItemStack book = new ItemStack(Items.BOOK, input.getCount());
            if (input.hasTag()) {
                CompoundTag tag = input.getTag().copy();
                // Remove enchantment tags so the plain book doesn't carry stale data
                tag.remove("StoredEnchantments");
                tag.remove("Enchantments");
                if (!tag.isEmpty()) {
                    book.setTag(tag);
                }
            }
            input = book;
        }

        int repairCost = 0;
        for (int j = 0; j < enchantments.size(); j++) {
            repairCost = AnvilMenu.calculateIncreasedRepairCost(repairCost);
        }
        input.setRepairCost(repairCost);
        return input;
    }

    public record Result(ItemStack top, ItemStack bottom, ItemStack output, int experience) {}
}
