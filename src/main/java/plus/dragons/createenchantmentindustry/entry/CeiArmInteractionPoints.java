package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Registry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerArmInteractionPoint;

/**
 * Registers Mechanical Arm interaction point types for CEI machines.
 * <p>
 * Uses Create 6.0.8's CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE registry.
 * <p>
 * Ported from 1.21.1 CEIArmInterationPoints.
 */
public class CeiArmInteractionPoints {
    public static final BlazeEnchanterArmInteractionPoint.Type BLAZE_ENCHANTER =
            register("blaze_enchanter", new BlazeEnchanterArmInteractionPoint.Type());
    public static final BlazeForgerArmInteractionPoint.Type BLAZE_FORGER =
            register("blaze_forger", new BlazeForgerArmInteractionPoint.Type());

    private static <T extends ArmInteractionPointType> T register(String name, T type) {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE,
                EnchantmentIndustry.genRL(name), type);
        return type;
    }

    public static void register() {
        // Force class loading to trigger static initializers
    }
}
