package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern.ExperienceLanternMountedFluidStorageType;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiMountedStorageTypes {
    public static final RegistryEntry<ExperienceLanternMountedFluidStorageType> EXPERIENCE_LANTERN = REGISTRATE
            .mountedFluidStorage("experience_lantern", ExperienceLanternMountedFluidStorageType::new)
            .register();

    public static void register() {}
}
