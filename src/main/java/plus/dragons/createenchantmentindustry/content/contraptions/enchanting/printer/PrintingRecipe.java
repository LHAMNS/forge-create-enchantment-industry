package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Recipe type for the Printer - allows data-driven printing recipes.
 * <p>
 * Each recipe specifies:
 * - Item input (what goes on the belt)
 * - Template input (what's placed on the printer)
 * - Fluid input (ink/experience)
 * - Result output
 * <p>
 * Ported from 1.21.1 PrintingRecipe. Uses 1.20.1's ProcessingRecipe system.
 */
public class PrintingRecipe extends ProcessingRecipe<Container> implements IAssemblyRecipe {

    public PrintingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CeiRecipeTypes.PRINTING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        if (inv.getContainerSize() < 2) return false;
        return ingredients.get(0).test(inv.getItem(0))
                && ingredients.get(1).test(inv.getItem(1));
    }

    @Override
    public Component getDescriptionForAssembly() {
        ItemStack[] matchingStacks = ingredients.get(1).getItems();
        if (matchingStacks.length == 0 || fluidIngredients.isEmpty()) {
            return Component.literal("Invalid");
        }
        return Component.translatable("create_enchantment_industry.recipe.assembly.printing",
                matchingStacks[0].getHoverName(),
                fluidIngredients.get(0).getMatchingFluidStacks().get(0).getDisplayName());
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CeiBlocks.PRINTER);
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(getIngredients().get(1));
    }

    @Override
    public Supplier<Supplier<com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> plus.dragons.createenchantmentindustry.compat.jei.category.AssemblyPrintingCategory::new;
    }
}
