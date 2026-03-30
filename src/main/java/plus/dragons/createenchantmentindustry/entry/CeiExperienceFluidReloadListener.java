package plus.dragons.createenchantmentindustry.entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.Map;

/**
 * Reload listener that loads XP fluid registrations from data packs.
 * <p>
 * Data packs can register third-party XP fluids by placing JSON files in:
 * {@code data/<namespace>/create_enchantment_industry/experience_fluids/<name>.json}
 * <p>
 * JSON format:
 * <pre>{@code
 * {
 *   "fluid": "modid:fluid_name",
 *   "xp_per_mb": 1
 * }
 * }</pre>
 * <p>
 * To remove a previously registered fluid, use:
 * <pre>{@code
 * {
 *   "fluid": "modid:fluid_name",
 *   "remove": true
 * }
 * }</pre>
 * <p>
 * This allows data packs to register XP fluids without writing Java code,
 * providing equivalent functionality to NeoForge DataMaps on Forge 1.20.1.
 */
public class CeiExperienceFluidReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DIRECTORY = "create_enchantment_industry/experience_fluids";

    public CeiExperienceFluidReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        int loaded = 0;
        int removed = 0;
        CeiDataMaps.resetXpFluidsFromDatapacks();

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();

                if (!json.has("fluid")) {
                    EnchantmentIndustry.LOGGER.warn("Experience fluid entry {} is missing 'fluid' field, skipping", id);
                    continue;
                }

                String fluidId = json.get("fluid").getAsString();
                ResourceLocation fluidRL = new ResourceLocation(fluidId);
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidRL);

                if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                    EnchantmentIndustry.LOGGER.warn("Experience fluid entry {} references unknown fluid '{}', skipping", id, fluidId);
                    continue;
                }

                if (json.has("remove") && json.get("remove").getAsBoolean()) {
                    CeiDataMaps.unregisterXpFluidFromDatapack(fluid);
                    removed++;
                    continue;
                }

                if (!json.has("xp_per_mb")) {
                    EnchantmentIndustry.LOGGER.warn("Experience fluid entry {} is missing 'xp_per_mb' field, skipping", id);
                    continue;
                }

                int xpPerMb = json.get("xp_per_mb").getAsInt();
                if (xpPerMb <= 0) {
                    EnchantmentIndustry.LOGGER.warn("Experience fluid entry {} has non-positive xp_per_mb ({}), skipping", id, xpPerMb);
                    continue;
                }

                CeiDataMaps.registerXpFluidFromDatapack(fluid, xpPerMb);
                loaded++;
            } catch (Exception e) {
                EnchantmentIndustry.LOGGER.error("Failed to parse experience fluid entry {}: {}", id, e.getMessage());
            }
        }

        EnchantmentIndustry.LOGGER.info("CeiExperienceFluidReloadListener: Loaded {} XP fluid entries, removed {}", loaded, removed);
    }
}
