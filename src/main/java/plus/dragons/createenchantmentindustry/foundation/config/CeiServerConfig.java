package plus.dragons.createenchantmentindustry.foundation.config;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.config.ui.ConfigAnnotations;
import net.minecraftforge.common.ForgeConfigSpec;

public class CeiServerConfig extends ConfigBase {

    public final ConfigBase.ConfigInt disenchanterTankCapacity = i(1000, 1,
        "disenchanterTankCapacity",
        Comments.disenchanterTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt copierTankCapacity = i(4000, 1,
        "copierTankCapacity",
        Comments.copierTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt blazeEnchanterTankCapacity = i(2000, 1,
        "blazeEnchanterTankCapacity",
        Comments.blazeEnchanterTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt blazeForgerTankCapacity = i(4000, 1,
        "blazeForgerTankCapacity",
        Comments.blazeForgerTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt maxHyperEnchantingLevelExtension = i(2, 0,
        "maxHyperEnchantingLevelExtension",
        Comments.maxHyperEnchantingLevelExtension);
    public final ConfigFloat deployerXpDropChance = f(1, 0, 1,
        "deployerXpDropChance",
        Comments.deployerXpDropChance);
    public final ConfigBool enableHyperEnchant = b(true, "enableHyperEnchant");
    public final ConfigFloat enchantByBlazeEnchanterCostCoefficient= f(1, 0.01f, 100,
            "enchantByBlazeEnchanterCostCoefficient");
    public final ConfigFloat hyperEnchantByBlazeEnchanterCostCoefficient= f(1, 0.01f, 100,
            "hyperEnchantByBlazeEnchanterCostCoefficient");
    public final ConfigFloat copyEnchantedBookCostCoefficient= f(1, 0.01f, 100,
            "copyEnchantedBookCostCoefficient");
    public final ConfigFloat copyEnchantedBookWithHyperExperienceCostCoefficient = f(1, 0.01f, 100,
            "copyEnchantedBookWithHyperExperienceCostCoefficient");
    public final ConfigInt copyWrittenBookCostPerPage = i(5, 1, 100,
            "copyWrittenBookCostPerPage",
            Comments.copyWrittenBookCostPerPage);
    public final ConfigInt copyNameTagCost = i(7, 1, 1000,
            "copyNameTagCost",
            Comments.copyNameTagCost);
    public final ConfigInt copyTrainScheduleCost = i(10, 1, 1000,
            "copyTrainScheduleCost",
            Comments.copyTrainScheduleCost);
    public final ConfigInt copyClipboardCost = i(10, 1, 1000,
            "copyClipboardCost",
            Comments.copyClipboardCost);
    public final ConfigInt copyBannerPatternCost = i(50, 1, 1000,
            "copyBannerPatternCost",
            Comments.copyBannerPatternCost);
    public final ConfigInt copyCustomNameCost = i(20, 1, 1000,
            "copyCustomNameCost",
            Comments.copyCustomNameCost);
    public final ConfigInt copyAddressCost = i(15, 1, 1000,
            "copyAddressCost",
            Comments.copyAddressCost);
    public final ConfigInt copyPackagePatternCost = i(15, 1, 1000,
            "copyPackagePatternCost",
            Comments.copyPackagePatternCost);
    public final ConfigFloat crushingWheelDropExpRate = f(0.3f, 0, 1,
            "crushingWheelDropExpRate",
            Comments.crushingWheelDropExpRate);
    public final ConfigFloat crushingWheelDropExpScale = f(0.34F, 0.1F, 100,
            "crushingWheelDropExpScale",
            Comments.crushingWheelDropExpScaleScale);
    public final ConfigBool copyingWrittenBookAlwaysGetOriginalVersion = b(true,
            "copyingWrittenBookAlwaysGetOriginalVersion",
            Comments.copyingWrittenBookAlwaysGetOriginalVersion);
    public final ConfigFloat regularLightningStrikeTransformXpBlockChance = f(1, 0, 1,
            "regularLightningStrikeTransformXpBlockChance",
            Comments.regularLightningStrikeTransformXpBlockChance);
    public final ConfigInt mechanicalGrindstoneTankCapacity = i(1000, 1,
            "mechanicalGrindstoneTankCapacity",
            Comments.mechanicalGrindstoneTankCapacity,
            ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigBool deployerCollectXp = b(true,
            "deployerCollectXp",
            Comments.deployerCollectXp);
    public final ConfigBool deployerMendItem = b(true,
            "deployerMendItem",
            Comments.deployerMendItem);

    // Blaze Enchanter limits
    public final ConfigInt blazeEnchanterMaxEnchantLevel = i(30, 1,
            "blazeEnchanterMaxEnchantLevel",
            Comments.blazeEnchanterMaxEnchantLevel);
    public final ConfigInt blazeEnchanterMaxSuperEnchantLevel = i(60, 1,
            "blazeEnchanterMaxSuperEnchantLevel",
            Comments.blazeEnchanterMaxSuperEnchantLevel);
    public final ConfigBool ignoreEnchantmentCompatibility = b(true,
            "ignoreEnchantmentCompatibility",
            Comments.ignoreEnchantmentCompatibility);

    // Deployer scales
    public final ConfigFloat deployerKillXpScale = f(1.0f, 0, 100,
            "deployerKillXpScale",
            Comments.deployerKillXpScale);
    public final ConfigFloat deployerMineXpScale = f(1.0f, 0, 100,
            "deployerMineXpScale",
            Comments.deployerMineXpScale);
    public final ConfigBool deployerSweepAttack = b(true,
            "deployerSweepAttack",
            Comments.deployerSweepAttack);

    // Crushing wheel
    public final ConfigBool crushingWheelKillDropXp = b(true,
            "crushingWheelKillDropXp",
            Comments.crushingWheelKillDropXp);

    // Printer toggles and settings
    public final ConfigBool enableWrittenBookPrinting = b(true,
            "enableWrittenBookPrinting",
            Comments.enableWrittenBookPrinting);
    public final ConfigBool enableEnchantedBookPrinting = b(true,
            "enableEnchantedBookPrinting",
            Comments.enableEnchantedBookPrinting);
    public final ConfigBool enableNameTagPrinting = b(true,
            "enableNameTagPrinting",
            Comments.enableNameTagPrinting);
    public final ConfigBool enableSchedulePrinting = b(true,
            "enableSchedulePrinting",
            Comments.enableSchedulePrinting);
    public final ConfigBool enableCustomNamePrinting = b(true,
            "enableCustomNamePrinting",
            Comments.enableCustomNamePrinting);
    public final ConfigBool enableBannerPatternPrinting = b(true,
            "enableBannerPatternPrinting",
            Comments.enableBannerPatternPrinting);
    public final ConfigBool enablePackagePatternPrinting = b(true,
            "enablePackagePatternPrinting",
            Comments.enablePackagePatternPrinting);
    public final ConfigBool enablePackageAddressPrinting = b(true,
            "enablePackageAddressPrinting",
            Comments.enablePackageAddressPrinting);
    public final ConfigBool printingCustomNameAsItemName = b(false,
            "printingCustomNameAsItemName",
            Comments.printingCustomNameAsItemName);
    public final ConfigInt printingProcessingTime = i(50, 1, 1000,
            "printingProcessingTime",
            Comments.printingProcessingTime);
    public final ConfigInt printingGenerationChange = i(-1, -3, 1,
            "printingGenerationChange",
            Comments.printingGenerationChange);
    public final ConfigFloat printingEnchantedBookCostMultiplier = f(1, 0.01f, 100,
            "printingEnchantedBookCostMultiplier",
            Comments.printingEnchantedBookCostMultiplier);
    public final ConfigBool printingEnchantedBookDenylistStopCopying = b(true,
            "printingEnchantedBookDenylistStopCopying",
            Comments.printingEnchantedBookDenylistStopCopying);
    public final ConfigBool experienceVaporizeOnPlacement = b(true,
            "experienceVaporizeOnPlacement",
            Comments.experienceVaporizeOnPlacement);

    // Experience Lantern
    public final ConfigInt experienceLanternFluidCapacity = i(1000, 100,
            "experienceLanternFluidCapacity",
            Comments.experienceLanternFluidCapacity,
            ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt experienceLanternDrainRate = i(50, 1,
            "experienceLanternDrainRate",
            Comments.experienceLanternDrainRate);
    public final ConfigBool experienceLanternPullToggle = b(true,
            "experienceLanternPullToggle",
            Comments.experienceLanternPullToggle);
    public final ConfigInt experienceLanternPullRadius = i(10, 0,
            "experienceLanternPullRadius",
            Comments.experienceLanternPullRadius);
    public final ConfigFloat experienceLanternPullForceMultiplier = f(.075f, 0.0f, .5f,
            "experienceLanternPullForceMultiplier",
            Comments.experienceLanternPullForceMultiplier);
    public final ConfigBool experienceLanternDrainMaidExperience = b(true,
            "experienceLanternDrainMaidExperience",
            Comments.experienceLanternDrainMaidExperience);

    @Override
    public void registerAll(ForgeConfigSpec.Builder builder) {
        super.registerAll(builder);
    }

    @Override
    public String getName() {
        return "server";
    }
    
    private static class Comments {
    
        static String disenchanterTankCapacity =
            "The Tank Capacity of the Disenchanter";
        static String copierTankCapacity =
            "The Tank Capacity of the Copier";
        static String blazeEnchanterTankCapacity =
            "The Tank Capacity of the Blaze Enchanter";
        static String blazeForgerTankCapacity =
            "The Tank Capacity of the Blaze Forger";
        static String maxHyperEnchantingLevelExtension =
            "The Maximum Extended Levels beyond Enchantment's Max Level that can be reached through Hyper-Enchanting";
        static String deployerXpDropChance =
            "The Chance of whether Deployer-killed entities will drop Experience Nugget";
        static String crushingWheelDropExpScaleScale =
                "The Scale of Experience Nugget dropped by Crushing-Wheel-killed entities";
        static String copyWrittenBookCostPerPage = "The amount of ink needed to be consumed by Copying one page of Written Book";
        static String copyNameTagCost = "The amount of liquid experience needed to be consumed by Copying Name Tag";
        static String copyTrainScheduleCost = "The amount of ink needed to be consumed by Copying Train Schedule";
        static String copyClipboardCost = "The amount of ink needed to be consumed by Copying Clipboard";
        static String copyBannerPatternCost = "The amount of liquid experience needed to print a banner pattern";
        static String copyCustomNameCost = "The amount of liquid experience needed to print a custom name onto an item";
        static String copyAddressCost = "The amount of ink needed to print an address onto a package";
        static String copyPackagePatternCost = "The amount of ink needed to print a pattern onto a package";
        static String crushingWheelDropExpRate = "The probability of dropping Experience Nugget after killing a creature on the Crushing Wheel";
        static String copyingWrittenBookAlwaysGetOriginalVersion =
                "Whether or not copying a written book always get original version. Setting it to false let you always get copy version of the book.";
        static String regularLightningStrikeTransformXpBlockChance =
                "Probability of natural lightning strikes transforming Blocks of Experience into Super Experience Blocks.";
        static String mechanicalGrindstoneTankCapacity =
                "The Tank Capacity of the Mechanical Grindstone Drain";
        static String deployerCollectXp =
                "Whether Deployer should collect Experience Nuggets instead of dropping XP orbs";
        static String deployerMendItem =
                "Whether Deployer should attempt to mend its held item with collected XP";
        static String blazeEnchanterMaxEnchantLevel =
                "Maximum enchantment level the Blaze Enchanter can apply (vanilla table equivalent)";
        static String blazeEnchanterMaxSuperEnchantLevel =
                "Maximum enchantment level the Blaze Enchanter can apply when using Super Experience";
        static String ignoreEnchantmentCompatibility =
                "Whether the Blaze Enchanter ignores enchantment compatibility rules (allows conflicting enchantments)";
        static String deployerKillXpScale =
                "Scale factor for XP dropped when a Deployer kills an entity";
        static String deployerMineXpScale =
                "Scale factor for XP dropped when a Deployer mines a block";
        static String deployerSweepAttack =
                "Whether the Deployer can perform sweep attacks";
        static String crushingWheelKillDropXp =
                "Whether entities killed by Crushing Wheels drop Experience Nuggets";
        static String enableWrittenBookPrinting =
                "Whether the Printer can copy Written Books";
        static String enableEnchantedBookPrinting =
                "Whether the Printer can copy Enchanted Books";
        static String enableNameTagPrinting =
                "Whether the Printer can copy Name Tags";
        static String enableSchedulePrinting =
                "Whether the Printer can copy Train Schedules";
        static String enableCustomNamePrinting =
                "Whether the Printer can print custom names onto items";
        static String enableBannerPatternPrinting =
                "Whether the Printer can print banner patterns";
        static String enablePackagePatternPrinting =
                "Whether the Printer can change package patterns";
        static String enablePackageAddressPrinting =
                "Whether the Printer can assign package addresses";
        static String printingCustomNameAsItemName =
                "Whether printing custom names (displayed in italics) should instead print as item names (displayed in non-italics)";
        static String printingProcessingTime =
                "The time in ticks for the Printer to complete one copy operation";
        static String printingGenerationChange =
                "Generation change applied when copying Written Books (-3 to 1). -1 means the copy is one generation older.";
        static String printingEnchantedBookCostMultiplier =
                "Cost multiplier for printing Enchanted Books";
        static String printingEnchantedBookDenylistStopCopying =
                "Whether the Printer denylist prevents Enchanted Books from being copied";
        static String experienceVaporizeOnPlacement =
                "Whether Liquid Experience will vaporize into Experience Orbs upon placement";
        static String experienceLanternFluidCapacity =
                "The amount of liquid an Experience Lantern can hold (mB)";
        static String experienceLanternDrainRate =
                "The amount of experience an Experience Lantern can drain from player per 0.5 seconds (mB)";
        static String experienceLanternPullToggle =
                "Whether the Experience Lantern will pull in experience orbs from nearby";
        static String experienceLanternPullRadius =
                "The range at which experience orbs will be pulled into the lantern";
        static String experienceLanternPullForceMultiplier =
                "Modifier for the amount of force with which to pull the experience orbs";
        static String experienceLanternDrainMaidExperience =
                "Whether the Experience Lantern will drain experience from nearby Touhou Little Maid's maids (requires TLM mod)";

    }

}
