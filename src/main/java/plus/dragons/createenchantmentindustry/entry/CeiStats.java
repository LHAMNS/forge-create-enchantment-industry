package plus.dragons.createenchantmentindustry.entry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiStats {
    public static final ResourceLocation MECHANICAL_GRINDSTONE_EXPERIENCE = makeCustomStat(
            "mechanical_grindstone_experience", StatFormatter.DEFAULT);
    public static final ResourceLocation SUPER_ENCHANT = makeCustomStat(
            "super_enchant", StatFormatter.DEFAULT);
    public static final ResourceLocation PRINT = makeCustomStat(
            "print", StatFormatter.DEFAULT);
    public static final ResourceLocation ENCHANT = makeCustomStat(
            "enchant", StatFormatter.DEFAULT);
    public static final ResourceLocation FORGE = makeCustomStat(
            "forge", StatFormatter.DEFAULT);

    private static ResourceLocation makeCustomStat(String name, StatFormatter formatter) {
        ResourceLocation resourceLocation = EnchantmentIndustry.genRL(name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, resourceLocation, resourceLocation);
        Stats.CUSTOM.get(resourceLocation, formatter);
        return resourceLocation;
    }

    public static void register() {
        // Class loading triggers static initialization
    }
}
