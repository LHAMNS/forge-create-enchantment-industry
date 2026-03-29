package plus.dragons.createenchantmentindustry.compat.tlm;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

/**
 * Handler for draining experience from Touhou Little Maid's EntityMaid.
 * Uses reflection to avoid hard compile-time dependency on TLM.
 */
public class MaidExperienceHandler {
    private static Class<?> maidClass;
    private static Method getExperienceMethod;
    private static Method setExperienceMethod;
    private static boolean initialized = false;
    private static boolean available = false;

    private static void init() {
        if (initialized) return;
        initialized = true;
        try {
            maidClass = Class.forName("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid");
            getExperienceMethod = maidClass.getMethod("getExperience");
            setExperienceMethod = maidClass.getMethod("setExperience", int.class);
            available = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        init();
        return available;
    }

    @SuppressWarnings("unchecked")
    public static int drainMaidExperience(Level level, AABB effectiveAABB, IFluidHandler fluidHandler) {
        init();
        if (!available) return 0;

        var rate = CeiConfigs.SERVER.experienceLanternDrainRate.get();
        List<LivingEntity> maids;
        try {
            maids = (List<LivingEntity>) (List<?>) level.getEntitiesOfClass(
                    (Class<? extends LivingEntity>) maidClass, effectiveAABB,
                    entity -> {
                        try {
                            return entity.isAlive() && (int) getExperienceMethod.invoke(entity) > 0;
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (Exception e) {
            return 0;
        }

        if (maids.isEmpty()) return 0;

        int sum = 0;
        for (var maid : maids) {
            try {
                int maidExp = (int) getExperienceMethod.invoke(maid);
                sum += Math.min(maidExp, rate);
            } catch (Exception ignored) {}
        }
        if (sum == 0) return 0;

        int inserted = fluidHandler.fill(
                new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), sum),
                IFluidHandler.FluidAction.EXECUTE);

        int totalDrained = 0;
        if (inserted > 0) {
            int remaining = inserted;
            for (var maid : maids) {
                if (remaining <= 0) break;
                try {
                    int maidExp = (int) getExperienceMethod.invoke(maid);
                    int toDrain = Math.min(maidExp, Math.min(rate, remaining));
                    if (toDrain > 0) {
                        setExperienceMethod.invoke(maid, maidExp - toDrain);
                        remaining -= toDrain;
                        totalDrained += toDrain;
                    }
                } catch (Exception ignored) {}
            }
        }

        return totalDrained;
    }
}
