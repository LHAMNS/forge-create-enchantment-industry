package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

@Mixin(Player.class)
public class PlayerMixin {
    // ordinal=3 targets the fourth boolean local in Player.attack():
    //   ordinal 0: flag  – full attack strength (f2 > 0.9F)
    //   ordinal 1: flag1 – knockback/sprint attack
    //   ordinal 2: flag2 – critical hit eligibility
    //   ordinal 3: flag3 – sweeping-edge attack eligibility  <-- this one
    // We intercept the STORE of flag3 so deployers (FakePlayers) can sweep
    // when configured and holding a sword-sweep-capable item.
    @ModifyVariable(method = "attack",
            at = @At("STORE"), ordinal = 3)
    private boolean enableSweepingEdgeForDeployer(boolean value){
        var self = (Player)(Object) this;
        if(self instanceof DeployerFakePlayer fakePlayer){
            if (!CeiConfigs.SERVER.deployerSweepAttack.get()) {
                return false;
            }
            ItemStack itemstack = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND);
            return itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP);
        }
        return value;
    }
}
