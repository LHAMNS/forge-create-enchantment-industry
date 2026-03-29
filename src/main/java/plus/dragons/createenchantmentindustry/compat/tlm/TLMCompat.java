package plus.dragons.createenchantmentindustry.compat.tlm;

import net.minecraftforge.fml.ModList;

/**
 * Compatibility utility class for Touhou Little Maid mod.
 */
public class TLMCompat {
    public static final String MOD_ID = "touhou_little_maid";
    private static Boolean loaded = null;

    /**
     * Check if Touhou Little Maid mod is loaded.
     *
     * @return true if TLM is present
     */
    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(MOD_ID);
        }
        return loaded;
    }
}
