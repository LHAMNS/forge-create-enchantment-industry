package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiCreativeModeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER;
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB;

    public CeiCreativeModeTab() {}

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    static {
        REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnchantmentIndustry.ID);
        CREATIVE_TAB = REGISTER.register("base", () -> {
            return CreativeModeTab.builder().title(Component.literal("CEI"))
                    .withTabsBefore(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey(),AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
                    .icon(CeiItems.ENCHANTING_GUIDE::asStack)
                    .displayItems((params, output) -> {
                        output.accept(CeiBlocks.DISENCHANTER);
                        output.accept(CeiBlocks.PRINTER);
                        output.accept(CeiBlocks.MECHANICAL_GRINDSTONE);
                        output.accept(CeiBlocks.EXPERIENCE_HATCH);
                        output.accept(CeiBlocks.EXPERIENCE_LANTERN);
                        output.accept(CeiBlocks.SUPER_EXPERIENCE_BLOCK);
                        output.accept(CeiItems.ENCHANTING_GUIDE);
                        output.accept(CeiItems.ENCHANTING_TEMPLATE);
                        output.accept(CeiItems.SUPER_ENCHANTING_TEMPLATE);
                        output.accept(CeiItems.EXPERIENCE_ROTOR);
                        output.accept(CeiItems.SUPER_EXPERIENCE_NUGGET);
                        output.accept(CeiFluids.INK.get().getBucket());
                        output.accept(CeiItems.HYPER_EXP_BOTTLE);
                    }).build();
        });
    }
}
