package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerArmInteractionPoint;

public class CeiArmInteractionPoints {
    private static final DeferredRegister<ArmInteractionPointType> ARM_POINTS = DeferredRegister
            .create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, EnchantmentIndustry.ID);

    public static final RegistryObject<ArmInteractionPointType> BLAZE_ENCHANTER =
            ARM_POINTS.register("blaze_enchanter", BlazeEnchanterArmInteractionPoint.Type::new);
    public static final RegistryObject<ArmInteractionPointType> BLAZE_FORGER =
            ARM_POINTS.register("blaze_forger", BlazeForgerArmInteractionPoint.Type::new);

    public static void register(IEventBus modBus) {
        ARM_POINTS.register(modBus);
    }
}
