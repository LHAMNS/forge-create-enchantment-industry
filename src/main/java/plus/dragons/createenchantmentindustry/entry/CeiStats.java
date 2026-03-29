package plus.dragons.createenchantmentindustry.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiStats {
    private static final DeferredRegister<ResourceLocation> CUSTOM_STATS = DeferredRegister
            .create(Registries.CUSTOM_STAT, EnchantmentIndustry.ID);

    public static final RegistryObject<ResourceLocation> MECHANICAL_GRINDSTONE_EXPERIENCE =
            CUSTOM_STATS.register("mechanical_grindstone_experience", () -> EnchantmentIndustry.genRL("mechanical_grindstone_experience"));
    public static final RegistryObject<ResourceLocation> SUPER_ENCHANT =
            CUSTOM_STATS.register("super_enchant", () -> EnchantmentIndustry.genRL("super_enchant"));
    public static final RegistryObject<ResourceLocation> PRINT =
            CUSTOM_STATS.register("print", () -> EnchantmentIndustry.genRL("print"));
    public static final RegistryObject<ResourceLocation> ENCHANT =
            CUSTOM_STATS.register("enchant", () -> EnchantmentIndustry.genRL("enchant"));
    public static final RegistryObject<ResourceLocation> FORGE =
            CUSTOM_STATS.register("forge", () -> EnchantmentIndustry.genRL("forge"));

    public static void register(IEventBus modBus) {
        CUSTOM_STATS.register(modBus);
        // Stats.CUSTOM.get() must be called AFTER registries are frozen
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(() -> {
            Stats.CUSTOM.get(MECHANICAL_GRINDSTONE_EXPERIENCE.get(), StatFormatter.DEFAULT);
            Stats.CUSTOM.get(SUPER_ENCHANT.get(), StatFormatter.DEFAULT);
            Stats.CUSTOM.get(PRINT.get(), StatFormatter.DEFAULT);
            Stats.CUSTOM.get(ENCHANT.get(), StatFormatter.DEFAULT);
            Stats.CUSTOM.get(FORGE.get(), StatFormatter.DEFAULT);
        }));
    }
}
