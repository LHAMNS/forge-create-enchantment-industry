package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.deployer;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.MendingByDeployer;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

/**
 * Deployer Extension that gives the Deployer XP collection capabilities.
 * Ported from 1.21.1/6.0.0-dev, adapted for Forge 1.20.1 events.
 *
 * Note: BlockDropsEvent does not exist in Forge 1.20.1 (it is NeoForge-only).
 * Block mining XP collection for deployers is handled via the existing
 * DeployerFakePlayerMixin which intercepts kill XP. For block break XP,
 * Forge 1.20.1 does not fire a centralized block drops event that includes
 * the breaker reference, so that functionality is omitted.
 */
@Mod.EventBusSubscriber
public class DeployerExtension {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingExperienceDrop(final LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof DeployerFakePlayer deployer))
            return;
        // Scale the XP by the configured multiplier
        int experience = Mth.ceil(event.getDroppedExperience() * CeiConfigs.SERVER.deployerXpDropChance.getF());
        event.setDroppedExperience(experience);
        if (CeiConfigs.SERVER.deployerCollectXp.get()) {
            deployer.giveExperiencePoints(experience);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onXpChange(final PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof DeployerFakePlayer deployer))
            return;
        if (!CeiConfigs.SERVER.deployerCollectXp.get())
            return;
        int total = deployer.totalExperience + event.getAmount();
        int consumed = 0;
        if (CeiConfigs.SERVER.deployerMendItem.get()) {
            ItemStack heldItem = deployer.getMainHandItem();
            if (MendingByDeployer.canItemBeMended(heldItem)) {
                ItemStack mended = MendingByDeployer.mendItem(total, heldItem);
                consumed = total - MendingByDeployer.getNewXp(total, heldItem);
                // The item is already modified in place by mendItem
            }
        }
        int nuggets = (event.getAmount() - consumed) / 3;
        if (nuggets > 0) {
            deployer.getInventory().placeItemBackInInventory(AllItems.EXP_NUGGET.asStack(nuggets));
        }
        event.setAmount(total - consumed - nuggets * 3);
    }
}
