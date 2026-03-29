package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;

/**
 * Utility methods for experience-fluid conversions.
 * Adapted from the 1.21.1/6.0.0-dev upstream ExperienceHelper.
 * Uses {@link CeiDataMaps} for fluid-to-XP lookups instead of hardcoded checks.
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
        if (level >= 32)
            return (9 * level * level - 325 * level) / 2 + 2220;
        if (level >= 17)
            return (5 * level * level - 81 * level) / 2 + 360;
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
     * Uses {@link CeiDataMaps} to look up the XP-per-mB ratio for any registered XP fluid.
     * Falls back to ExperienceFluid.getXpRatio() for backward compatibility.
     */
    public static int getExperienceFromFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return 0;
        // Primary path: check the data map registry
        int xpPerMb = CeiDataMaps.getXpPerMb(fluid.getFluid());
        if (xpPerMb > 0) {
            return fluid.getAmount() * xpPerMb;
        }
        // Fallback for ExperienceFluid subclasses not yet registered in the data map
        if (fluid.getFluid() instanceof ExperienceFluid expFluid) {
            return fluid.getAmount() * expFluid.getXpRatio();
        }
        return 0;
    }
}
