package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.deployer;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.MendingByDeployer;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

/**
 * Deployer Extension that gives the Deployer XP collection capabilities.
 * Ported from 1.21.1/6.0.0-dev, adapted for Forge 1.20.1 events.
 * <p>
 * Block mining XP collection is handled via {@link BlockEvent.BreakEvent},
 * which fires when any player (including DeployerFakePlayer) breaks a block.
 * The event provides the XP that would be dropped, allowing us to intercept
 * it and convert to experience nuggets for the deployer.
 */
@Mod.EventBusSubscriber
public class DeployerExtension {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = false)
    public static void onLivingExperienceDrop(final LivingExperienceDropEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getAttackingPlayer() instanceof DeployerFakePlayer deployer))
            return;
        // Scale the XP by the configured multiplier
        int experience = Mth.ceil(event.getDroppedExperience() * CeiConfigs.SERVER.deployerKillXpScale.getF());
        event.setDroppedExperience(experience);
        if (CeiConfigs.SERVER.deployerCollectXp.get()) {
            deployer.giveExperiencePoints(experience);
            event.setCanceled(true);
        }
    }

    /**
     * Intercept block break XP when a DeployerFakePlayer breaks a block.
     * Forge 1.20.1's BlockEvent.BreakEvent provides getExpToDrop() which returns
     * the XP that would normally be dropped as orbs (e.g., from ore blocks).
     * We scale it and convert to experience nuggets for the deployer.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof DeployerFakePlayer deployer))
            return;
        int xpToDrop = event.getExpToDrop();
        if (xpToDrop <= 0)
            return;

        // Scale the XP by the configured mining multiplier
        int experience = Mth.ceil(xpToDrop * CeiConfigs.SERVER.deployerMineXpScale.getF());
        if (experience <= 0)
            return;

        if (CeiConfigs.SERVER.deployerCollectXp.get()) {
            // Suppress the normal XP drop
            event.setExpToDrop(0);
            // Give XP to deployer (triggers onXpChange below)
            deployer.giveExperiencePoints(experience);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onXpChange(final PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof DeployerFakePlayer deployer))
            return;
        if (!CeiConfigs.SERVER.deployerCollectXp.get())
            return;
        // Only work with the new XP from this event, NOT the deployer's accumulated totalExperience
        int amount = event.getAmount();
        if (amount <= 0)
            return;
        int consumed = 0;
        if (CeiConfigs.SERVER.deployerMendItem.get()) {
            ItemStack heldItem = deployer.getMainHandItem();
            if (MendingByDeployer.canItemBeMended(heldItem)) {
                consumed = amount - MendingByDeployer.getNewXp(amount, heldItem);
                MendingByDeployer.mendItem(amount, heldItem);
            }
        }
        int remaining = Math.max(0, amount - consumed);
        int nuggets = remaining / 3;
        if (nuggets > 0) {
            deployer.getInventory().placeItemBackInInventory(AllItems.EXP_NUGGET.asStack(nuggets));
        }
        // Cancel the event so the deployer doesn't accumulate totalExperience
        // Only pass through leftover XP that wasn't converted to nuggets
        event.setAmount(remaining - nuggets * 3);
    }
}
