package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

/**
 * Ponder scenes for Experience handling and overview.
 * Ported from upstream ExperienceScene (1.21.1/6.0.0-dev), adapted for Forge 1.20.1.
 */
public class ExperienceScene {

    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.intro", "Introduction to Experience Handling");
        scene.configureBasePlate(0, 0, 12);
        scene.scaleSceneView(.38f);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(11, 1, 9, 9, 7, 11), Direction.DOWN);

        scene.overlay().showText(60)
                .text("This is a tank of Liquid Experience, the primary form of Experience used in Enchantment Industry")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(9, 5, 9));
        for (int i = 0; i < 6; i++) {
            scene.world().modifyBlockEntity(util.grid().at(9, 4, 9), FluidTankBlockEntity.class,
                    be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 10000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(10);
        }
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Converting the Experience you carry to liquid form requires two components")
                .attachKeyFrame()
                .independent();
        scene.idle(70);

        // Show remaining setup
        scene.world().showSection(util.select().fromTo(11, 1, 6, 10, 1, 8).add(util.select().position(9, 1, 8)), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(8, 1, 9, 6, 1, 11), Direction.DOWN);
        scene.idle(10);

        // Two Hatches
        scene.overlay().showText(40)
                .text("Liquid Hatch for accessing liquids directly from items")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(11, 4, 8));
        scene.idle(50);

        scene.overlay().showText(40)
                .text("Experience Hatch for accessing experience directly from the player")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(8, 4, 11));
        scene.idle(50);

        scene.overlay().showText(40)
                .text("But as part of Create, automation is a must, right?")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .independent();
        scene.idle(50);
    }

    public static void advance(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.advance", "Things you might want to know");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().position(4, 1, 0), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(50)
                .text("There are a few more things you need to know before you're ready to start using Enchantment Industry")
                .placeNearTarget()
                .pointAt(util.vector().topOf(4, 1, 0));
        for (int i = 0; i <= 4; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i == 0 && j == 4) continue;
                scene.world().showSection(util.select().position(j, 1, i), Direction.DOWN);
                scene.idle(2);
            }
        }
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Block of Experience is no longer purely decorative and storage block. You'll need it later")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 0));
        scene.idle(70);

        scene.overlay().showText(55)
                .text("Block of Experience is required to make both the Enchantment Template and the Block of Super Experience")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 0));
        scene.idle(45);

        for (int i = 3; i <= 4; i++) {
            for (int j = 4; j >= 0; j--) {
                scene.world().showSection(util.select().position(j, 2, i), Direction.DOWN);
                scene.idle(2);
            }
        }

        scene.overlay().showText(55)
                .text("This is Block of Super Experience, used to make Super Enchanting Templates")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 3));
        scene.idle(65);

        scene.world().showSection(util.select().position(3, 2, 1).add(util.select().position(3, 3, 3)), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(40)
                .text("Blaze Enchanter")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 2, 1));
        scene.world().modifyBlock(util.grid().at(3, 3, 3), bs -> bs.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(50);

        scene.world().showSection(util.select().position(1, 2, 1).add(util.select().position(1, 3, 3)), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(40)
                .text("Blaze Forger")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.world().modifyBlock(util.grid().at(1, 3, 3), bs -> bs.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(50);

        scene.overlay().showText(55)
                .text("Seething Blaze Forgers and Enchanters are in Super Enchanting mode")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(3, 3, 3));
        scene.idle(65);

        scene.overlay().showText(90)
                .text("Super Enchanting allows you to surpass traditional enchanting limits, including exceeding the vanilla level cap and merging conflicting enchantments")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 3, 3));
        scene.idle(100);
    }

    public static void prepare(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.prepare_for_super_enchant", "Prepare materials for Super Enchanting");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(2, 1, 0, 0, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(75)
                .text("First, obtain a Block of Super Experience, which will be used to make the Super Enchanting Template. This requires Block of Experience")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(85);
        scene.overlay().showText(45)
                .text("Next, place a Lightning Rod")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.world().showSection(util.select().position(1, 2, 1), Direction.DOWN);
        scene.idle(55);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(1, 2, 1)));
            return lightning;
        });
        scene.world().replaceBlocks(util.select().layer(1), CeiBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);
        scene.idle(10);
        scene.overlay().showText(45)
                .text("Lightning Strike!")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.idle(55);

        scene.overlay().showText(120)
                .text("The vast majority of lightning strikes only have a certain probability of transforming Block of Experience, and only lightning strikes caused by Super Enchanting are guaranteed to transform")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.idle(130);

        scene.world().replaceBlocks(util.select().position(1, 2, 1), Blocks.AIR.defaultBlockState(), true);
        scene.world().replaceBlocks(util.select().layer(1), Blocks.AIR.defaultBlockState(), true);
        scene.world().hideSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(1, 1, 1), AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), DepotBlockEntity.class,
                be -> be.setHeldItem(CeiItems.SUPER_ENCHANTING_TEMPLATE.asStack()));
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Super Enchanting Templates are crafted from Block of Super Experience. In Super Enchanting mode, Blaze Forgers and Enchanters must use Super Enchanting Templates")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(80);
    }

    public static void beaconBase(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.beacon_base_block", "As Beacon base block");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.8f);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(util.select().layer(2), Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(util.select().layer(3), Direction.DOWN);
        scene.idle(20);
    }
}
