package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ponder scenes for the Blaze Forger.
 * Ported from upstream ForgerScene (1.21.1/6.0.0-dev), adapted for Forge 1.20.1.
 */
public class ForgerScene {

    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_forger.intro", "Introduction to Blaze Forger");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("This is a Blaze Forger, which functions like an Anvil")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 128);
        scene.world().setKineticSpeed(util.select().position(3, 2, 3), -128);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(30);

        scene.overlay().showText(60)
                .text("Provide it Liquid Experience to activate it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().position(1, 1, 2), 128);
        scene.idle(10);
        // Fill the forger's internal tank via capability and set heat level
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> {
                    be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER)
                            .ifPresent(handler -> handler.fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE));
                });
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeForgerBlock.HEAT_LEVEL, BlazeEnchanterBlock.HeatLevel.KINDLED), false);
        scene.idle(55);

        scene.overlay().showText(80)
                .text("Blaze Forger can merge enchantments of the same item like an anvil, but with no Repair Cost")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(90);

        var sword1 = new ItemStack(Items.DIAMOND_SWORD);
        var sword2 = new ItemStack(Items.DIAMOND_SWORD);
        sword1.enchant(Enchantments.SWEEPING_EDGE, 1);
        sword2.enchant(Enchantments.SWEEPING_EDGE, 1);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword1, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword2, false));
        scene.idle(90);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Blaze Forger can also merge Enchanting Templates")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);

        var template1 = CeiItems.ENCHANTING_TEMPLATE.asStack();
        var template2 = CeiItems.ENCHANTING_TEMPLATE.asStack();
        Map<net.minecraft.world.item.enchantment.Enchantment, Integer> enchMap = new LinkedHashMap<>();
        enchMap.put(Enchantments.SWEEPING_EDGE, 1);
        EnchantingTemplateItem.setStoredEnchantments(enchMap, template1);
        EnchantingTemplateItem.setStoredEnchantments(enchMap, template2);

        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template1, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template2, false));
        scene.idle(90);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Most importantly, Blaze Forger is able to apply Enchanting Templates to items!")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);

        var sword3 = new ItemStack(Items.DIAMOND_SWORD);
        sword3.enchant(Enchantments.SWEEPING_EDGE, 1);
        var template3 = CeiItems.ENCHANTING_TEMPLATE.asStack();
        Map<net.minecraft.world.item.enchantment.Enchantment, Integer> enchMap2 = new LinkedHashMap<>();
        enchMap2.put(Enchantments.SWEEPING_EDGE, 2);
        EnchantingTemplateItem.setStoredEnchantments(enchMap2, template3);

        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword3, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template3, false));
        scene.idle(90);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Also, Blaze Forger is able to strip enchantment from equipment, book or Enchanting Template to a blank Enchanting Template!")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(90);

        var sword4 = new ItemStack(Items.DIAMOND_SWORD);
        sword4.enchant(Enchantments.SWEEPING_EDGE, 2);
        sword4.enchant(Enchantments.BANE_OF_ARTHROPODS, 2);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword4, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(CeiItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.idle(90);
    }

    public static void superEnchant(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_forger.super_enchant", "Super Enchanting with Blaze Forger");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .text("While in Super Enchanting mode, Blaze Forger can surpass the vanilla enchantment level cap while merging enchantments. Conflicting enchantments can also be merged together")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(110);

        scene.overlay().showText(60)
                .text("Blaze Forger in Super Enchanting mode exclusively processes and applies Super Enchanting Templates")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(2, 2, 2), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.overlay().showText(60)
                .text("Make sure to place a Lightning Rod nearby")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 2));
        scene.idle(60);
    }

    public static void automate(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_forger.automate", "Automating with Mechanical Arm");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(10);

        var armPos = util.grid().at(4, 1, 3);
        var arm = util.select().position(4, 1, 3);
        var inputDepot = util.select().position(4, 1, 1);
        var inputDepot2 = util.select().position(2, 1, 4);

        scene.world().showSection(arm.add(inputDepot).add(inputDepot2), Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(arm, 128);

        scene.overlay().showText(60)
                .text("Blaze Forger can be automated with Mechanical Arm")
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Mechanical Arm can insert items for forging and extract forged results")
                .attachKeyFrame();
        scene.idle(70);
    }
}
