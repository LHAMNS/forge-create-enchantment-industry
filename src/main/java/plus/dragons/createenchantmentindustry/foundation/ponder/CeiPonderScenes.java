package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

/**
 * Registers all Ponder scenes for CEI blocks and items.
 * <p>
 * Ported from upstream CEIPonderScenes (1.21.1/6.0.0-dev) with all scenes.
 */
public class CeiPonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        // ── Experience Nugget / Experience Overview ─────────────────────
        HELPER.forComponents(AllItems.EXP_NUGGET)
                .addStoryBoard("experience/basic", ExperienceScene::basic, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("experience/advance", ExperienceScene::advance)
                .addStoryBoard("experience/prepare_for_super_enchant", ExperienceScene::prepare)
                .addStoryBoard("experience/beacon_base", ExperienceScene::beaconBase)
                // Legacy scenes (retained for backward compatibility)
                .addStoryBoard("collect_experience_nugget", EnchantmentScenes::handleExperienceNugget, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("experience_bottle", EnchantmentScenes::handleExperienceBottle, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("leak", EnchantmentScenes::leak)
                .addStoryBoard("experience_nugget_drop", EnchantmentScenes::dropExperienceNugget, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("crushing_wheel_tweak", EnchantmentScenes::crushingWheelTweak, CeiPonderTags.EXPERIENCE);

        // ── Experience Hatch ───────────────────────────────────────────
        HELPER.forComponents(CeiBlocks.EXPERIENCE_HATCH)
                .addStoryBoard("experience_hatch", MiscScene::experienceHatch, CeiPonderTags.EXPERIENCE);

        // ── Mechanical Grindstone ──────────────────────────────────────
        HELPER.forComponents(CeiBlocks.MECHANICAL_GRINDSTONE)
                .addStoryBoard("grindstone/basic", GrindstoneScene::basic, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("grindstone/extra", GrindstoneScene::extra);

        // ── Disenchanter (legacy) ──────────────────────────────────────
        HELPER.forComponents(CeiBlocks.DISENCHANTER)
                .addStoryBoard("disenchant", EnchantmentScenes::disenchant, CeiPonderTags.EXPERIENCE);

        // ── Blaze Enchanter ────────────────────────────────────────────
        HELPER.forComponents(CeiBlocks.BLAZE_ENCHANTER)
                .addStoryBoard("enchant", EnchantmentScenes::enchant, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("hyper_enchant", EnchantmentScenes::hyperEnchant, CeiPonderTags.EXPERIENCE);

        HELPER.forComponents(CeiItems.ENCHANTING_GUIDE)
                .addStoryBoard("enchanter_transform", EnchantmentScenes::transformBlazeBurner, CeiPonderTags.EXPERIENCE);

        // ── Blaze Forger ───────────────────────────────────────────────
        HELPER.forComponents(CeiBlocks.BLAZE_FORGER)
                .addStoryBoard("forger", ForgerScene::basic, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("forger", ForgerScene::superEnchant)
                .addStoryBoard("automate_forger", ForgerScene::automate, AllCreatePonderTags.ARM_TARGETS);

        // ── Printer ────────────────────────────────────────────────────
        HELPER.forComponents(CeiBlocks.PRINTER)
                .addStoryBoard("printer", MiscScene::printer, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("copy", EnchantmentScenes::copy, CeiPonderTags.EXPERIENCE);

        // ── Experience Lantern ─────────────────────────────────────────
        HELPER.forComponents(CeiBlocks.EXPERIENCE_LANTERN)
                .addStoryBoard("experience_lantern", MiscScene::experienceLantern, CeiPonderTags.EXPERIENCE, AllCreatePonderTags.CONTRAPTION_ACTOR);
    }
}
