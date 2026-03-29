package plus.dragons.createenchantmentindustry.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiDamageTypes {
    public static final ResourceKey<DamageType> GRIND = key("grind");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, EnchantmentIndustry.genRL(name));
    }
}
