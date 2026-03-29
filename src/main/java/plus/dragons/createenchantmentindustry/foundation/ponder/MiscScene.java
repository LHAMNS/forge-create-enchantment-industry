package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

/**
 * Ponder scenes for miscellaneous blocks (Experience Hatch, Printer, Experience Lantern).
 * Ported from upstream MiscScene (1.21.1/6.0.0-dev), adapted for Forge 1.20.1.
 */
public class MiscScene {

    public static void experienceHatch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience_hatch.intro", "Introduction to Experience Hatch");
        scene.configureBasePlate(0, 0, 4);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(3, 1, 1, 1, 1, 3)
                .add(util.select().fromTo(3, 2, 2, 2, 3, 3))
                .add(util.select().position(1, 3, 2)), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(50)
                .text("The Experience Hatch is simple to use. Right click it to store Experience...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        var frontVec = util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST)
                .add(-.125, 0, 0);
        scene.overlay().showControls(frontVec, Pointing.UP, 50).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 10000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(50);

        scene.overlay().showText(60)
                .text("...Shift-Right-Click Hatch to extract stored Experience")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(70);

        scene.overlay().showText(80)
                .text("There are a filter slot and a scroll panel on Hatch. You can configure how much Experience is retrieved or deposited per interaction on the panel")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        for (int i = 0; i < 12; i++) {
            scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                    be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(5);
        }
        scene.idle(30);

        scene.overlay().showText(40)
                .text("The filter slot is used to deal with experience fluids of other mods")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        scene.idle(50);
    }

    public static void printer(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("printer.intro", "Introduction to Printer");
        scene.configureBasePlate(1, 1, 3);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 0, 1, 3, 0, 3)
                .add(util.select().position(2, 1, 2))
                .add(util.select().position(2, 3, 2)), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(40)
                .text("This is a Printer")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(50);

        scene.overlay().showText(80)
                .text("Before use, set the item to print via the filter slot...")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST));
        scene.idle(90);

        scene.overlay().showText(80)
                .text("...and pass in the corresponding fluid")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(10);
        scene.world().showSection(util.select().column(4, 2)
                .add(util.select().position(4, 0, 2))
                .add(util.select().position(3, 3, 2)), Direction.WEST);
        scene.idle(80);

        scene.overlay().showText(80)
                .text("It can copy enchanted books, written books, name tags, schedules, clipboards, banner patterns and more. Use JEI to look up printing recipes")
                .attachKeyFrame()
                .independent();
        scene.idle(80);
    }

    public static void experienceLantern(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience_lantern.intro", "Introduction to Experience Lantern");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        var lantern = scene.world().showIndependentSection(util.select().position(2, 6, 2), Direction.DOWN);
        scene.world().moveSection(lantern, new Vec3(0, -5, 0), 0);
        scene.idle(10);

        scene.overlay().showText(100)
                .text("Experience Lantern absorbs experience from nearby players and experience orbs. It glows according to the amount of experience stored internally")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(100);
        scene.world().hideIndependentSection(lantern, Direction.UP);
        scene.idle(10);

        var contraptionSelection = util.select().fromTo(0, 1, 0, 4, 3, 4);
        scene.world().showSection(util.select().fromTo(2, 4, 2, 2, 5, 2), Direction.DOWN);
        ElementLink<WorldSectionElement> contraption = scene.world().showIndependentSection(contraptionSelection, Direction.DOWN);
        scene.idle(10);

        scene.world().configureCenterOfRotation(contraption, util.vector().centerOf(2, 4, 2));
        scene.overlay().showText(60)
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(util.grid().at(2, 2, 0)))
                .text("Experience Lantern also works on Contraptions");
        scene.idle(70);

        scene.world().setKineticSpeed(util.select().fromTo(2, 4, 2, 2, 5, 2), -24);
        scene.world().rotateBearing(util.grid().at(2, 4, 2), -360, 140);
        scene.world().rotateSection(contraption, 0, -360, 0, 140);
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CeiFluids.EXPERIENCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(70);
        scene.world().setKineticSpeed(util.select().fromTo(2, 4, 2, 2, 5, 2), 0);
    }
}
