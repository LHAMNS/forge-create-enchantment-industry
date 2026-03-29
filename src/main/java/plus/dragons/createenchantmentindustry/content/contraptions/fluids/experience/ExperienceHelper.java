package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

/**
 * Utility methods for experience-fluid conversions.
 * Adapted from the 1.21.1/6.0.0-dev upstream ExperienceHelper,
 * simplified for 1.20.1 Forge where DataMaps are not available.
 */
public class ExperienceHelper {

    public static int getExperienceForNextLevel(int level) {
        if (level >= 30)
            return 9 * level - 158;
        if (level >= 15)
            return 5 * level - 38;
        return 2 * level + 7;
    }

    public static int getExperienceForTotalLevel(int level) {
        if (level == 0)
            return 0;
        if (level >= 31)
            return (9 * level * level - 325 * level) / 2 + 2220;
        if (level >= 16)
            return (5 * level * level - 91 * level) / 2 + 360;
        return level * level + 6 * level;
    }

    /**
     * Returns the total experience points a player currently has
     * (accumulated from all levels + current progress).
     */
    public static int getExperienceForPlayer(Player player) {
        int experience = getExperienceForTotalLevel(player.experienceLevel);
        experience += Math.round(player.experienceProgress * getExperienceForNextLevel(player.experienceLevel));
        return experience;
    }

    /**
     * Converts a fluid stack to experience points.
     * For the base EXPERIENCE fluid, 1 mB = 1 XP.
     * For HYPER_EXPERIENCE, uses the fluid's xpRatio.
     */
    public static int getExperienceFromFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return 0;
        if (fluid.getFluid() instanceof ExperienceFluid expFluid) {
            return fluid.getAmount() * expFluid.getXpRatio();
        }
        // Fallback: 1 mB = 1 XP for recognized experience fluids
        if (fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get())) {
            return fluid.getAmount();
        }
        return 0;
    }
}
