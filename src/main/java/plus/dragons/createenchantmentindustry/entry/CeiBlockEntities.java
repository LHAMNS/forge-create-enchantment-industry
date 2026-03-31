package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.GrindstoneDrainRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceHatchBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern.ExperienceLanternBlockEntity;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiBlockEntities {

    public static final BlockEntityEntry<DisenchanterBlockEntity> DISENCHANTER = REGISTRATE
            .blockEntity("disenchanter", DisenchanterBlockEntity::new)
            .validBlocks(CeiBlocks.DISENCHANTER)
            .renderer(() -> DisenchanterRenderer::new)
            .register();

    public static final BlockEntityEntry<PrinterBlockEntity> PRINTER = REGISTRATE
            .blockEntity("printer", PrinterBlockEntity::new)
            .validBlocks(CeiBlocks.PRINTER)
            .renderer(() -> PrinterRenderer::new)
            .register();

    public static final BlockEntityEntry<BlazeEnchanterBlockEntity> BLAZE_ENCHANTER = REGISTRATE
            .blockEntity("blaze_enchanter", BlazeEnchanterBlockEntity::new)
            .validBlocks(CeiBlocks.BLAZE_ENCHANTER)
            .renderer(() -> BlazeEnchanterRenderer::new)
            .register();

    public static final BlockEntityEntry<BlazeForgerBlockEntity> BLAZE_FORGER = REGISTRATE
            .blockEntity("blaze_forger", BlazeForgerBlockEntity::new)
            .validBlocks(CeiBlocks.BLAZE_FORGER)
            .renderer(() -> BlazeForgerRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> MECHANICAL_GRINDSTONE = REGISTRATE
            .blockEntity("mechanical_grindstone", KineticBlockEntity::new)
            .validBlocks(CeiBlocks.MECHANICAL_GRINDSTONE)
            .register();

    public static final BlockEntityEntry<GrindstoneDrainBlockEntity> GRINDSTONE_DRAIN = REGISTRATE
            .blockEntity("grindstone_drain", GrindstoneDrainBlockEntity::new)
            .validBlocks(CeiBlocks.GRINDSTONE_DRAIN)
            .renderer(() -> GrindstoneDrainRenderer::new)
            .register();

    public static final BlockEntityEntry<ExperienceHatchBlockEntity> EXPERIENCE_HATCH = REGISTRATE
            .blockEntity("experience_hatch", ExperienceHatchBlockEntity::new)
            .validBlocks(CeiBlocks.EXPERIENCE_HATCH)
            .register();

    public static final BlockEntityEntry<ExperienceLanternBlockEntity> EXPERIENCE_LANTERN = REGISTRATE
            .blockEntity("experience_lantern", ExperienceLanternBlockEntity::new)
            .validBlocks(CeiBlocks.EXPERIENCE_LANTERN)
            .register();

    public static void register() {}
}
