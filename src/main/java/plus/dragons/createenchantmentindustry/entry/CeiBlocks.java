package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.materials.ExperienceBlock;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.ForgeSoundType;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.client.renderer.RenderType;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceHatchBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern.ExperienceLanternBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.lantern.ExperienceLanternMovementBehaviour;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;


public class CeiBlocks {
    public static final BlockEntry<DisenchanterBlock> DISENCHANTER = REGISTRATE
            .block("disenchanter", DisenchanterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .simpleItem()
            .register();

    public static final BlockEntry<PrinterBlock> PRINTER = REGISTRATE
            .block("printer", PrinterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(DisplaySource.displaySource(CeiDisplaySources.COPY_CONTENT))
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.partialBaseModel(ctx, pov)))
            .item(AssemblyOperatorBlockItem::new)
            .model(AssetLookup::customItemModel)
            .build()
            .register();

    public static final BlockEntry<BlazeEnchanterBlock> BLAZE_ENCHANTER = REGISTRATE
            .block("blaze_enchanter", BlazeEnchanterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.lightLevel(BlazeEnchanterBlock::getLight))
            .transform(DisplaySource.displaySource(CeiDisplaySources.TARGET_ENCHANTMENT))
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .simpleItem()
            .register();

    public static final BlockEntry<BlazeForgerBlock> BLAZE_FORGER = REGISTRATE
            .block("blaze_forger", BlazeForgerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.lightLevel(BlazeForgerBlock::getLight))
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .simpleItem()
            .register();

    public static final BlockEntry<MechanicalGrindstoneBlock> MECHANICAL_GRINDSTONE = REGISTRATE
            .block("mechanical_grindstone", MechanicalGrindstoneBlock::new)
            .initialProperties(SharedProperties::stone)
                        .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .item(MechanicalGrindStoneItem::new)
            .build()
            .register();

    public static final BlockEntry<GrindstoneDrainBlock> GRINDSTONE_DRAIN = REGISTRATE
            .block("grindstone_drain", p -> new GrindstoneDrainBlock(MECHANICAL_GRINDSTONE.get(), p))
            .initialProperties(SharedProperties::stone)
                        .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.partialBaseModel(ctx, pov)))
            .loot((lt, block) -> lt.dropOther(block, MECHANICAL_GRINDSTONE.get()))
            .register();

    public static final BlockEntry<ExperienceBlock> SUPER_EXPERIENCE_BLOCK = REGISTRATE
            .block("super_experience_block", ExperienceBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.DIAMOND)
                    .sound(new ForgeSoundType(1, .5f, () -> SoundEvents.AMETHYST_BLOCK_BREAK,
                            () -> SoundEvents.AMETHYST_BLOCK_STEP, () -> SoundEvents.AMETHYST_BLOCK_PLACE,
                            () -> SoundEvents.AMETHYST_BLOCK_HIT, () -> SoundEvents.AMETHYST_BLOCK_FALL))
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
                    .withExistingParent(ctx.getName(), Create.asResource("block/experience_block"))
                    .texture("all", prov.modLoc("block/" + ctx.getName()))
                    .texture("particle", prov.modLoc("block/" + ctx.getName()))))
            .transform(TagGen.pickaxeOnly())
            .lang("Block of Super Experience")
            .tag(Tags.Blocks.STORAGE_BLOCKS)
            .tag(BlockTags.BEACON_BASE_BLOCKS)
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(Tags.Items.STORAGE_BLOCKS)
            .build()
            .register();

    public static final BlockEntry<ExperienceHatchBlock> EXPERIENCE_HATCH = REGISTRATE
            .block("experience_hatch", ExperienceHatchBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GREEN).lightLevel(state -> 12))
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.get(), AssetLookup.standardModel(ctx, prov)))
            .simpleItem()
            .register();

    public static final BlockEntry<ExperienceLanternBlock> EXPERIENCE_LANTERN = REGISTRATE
            .block("experience_lantern", ExperienceLanternBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN))
            .transform(TagGen.pickaxeOnly())
            .transform(MountedFluidStorageType.mountedFluidStorage(CeiMountedStorageTypes.EXPERIENCE_LANTERN))
            .onRegister(block -> MovementBehaviour.REGISTRY.register(block, new ExperienceLanternMovementBehaviour()))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.directionalBlock(ctx.get(), AssetLookup.standardModel(ctx, prov)))
            .simpleItem()
            .register();

    public static void register() {}

}
