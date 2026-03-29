package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.Optional;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends Entity {
    @Unique
    private static final String cei$LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY = "ExperienceCharge";

    @Shadow
    protected abstract BlockPos getStrikePosition();

    private LightningBoltMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;clearCopperOnLightningStrike(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void cei$chargeExperienceOnLightingStrike(CallbackInfo ci) {
        Level level = this.level();
        if (level.isClientSide) return;
        if (!this.getPersistentData().getBoolean(cei$LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY))
            if (this.random.nextFloat() > CeiConfigs.SERVER.regularLightningStrikeTransformXpBlockChance.get())
                return;
        BlockPos pos = this.getStrikePosition();
        BlockState blockstate = level.getBlockState(pos);
        // Check forge:lightning_rods tag instead of only vanilla LightningRodBlock
        if (blockstate.is(CeiTags.LIGHTNING_RODS) && blockstate.hasProperty(LightningRodBlock.FACING)) {
            pos = pos.relative(blockstate.getValue(LightningRodBlock.FACING).getOpposite());
            blockstate = level.getBlockState(pos);
        }

        if (blockstate.is(AllBlocks.EXPERIENCE_BLOCK.get())) {
            level.setBlockAndUpdate(pos, CeiBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState());
            BlockPos.MutableBlockPos mutable = pos.mutable();
            int i = level.random.nextInt(3) + 3;

            for (int j = 0; j < i; j++) {
                int k = level.random.nextInt(8) + 1;
                cei$randomWalkChargeExperience(level, pos, mutable, k);
            }
        }
    }

    @Unique
    private static void cei$randomWalkChargeExperience(Level level, BlockPos pos, BlockPos.MutableBlockPos mutable, int steps) {
        mutable.set(pos);
        for (int i = 0; i < steps; i++) {
            Optional<BlockPos> optional = cei$randomWalkChargeExperienceStep(level, mutable);
            if (optional.isEmpty()) {
                break;
            }
            mutable.set(optional.get());
        }
    }

    @Unique
    private static Optional<BlockPos> cei$randomWalkChargeExperienceStep(Level level, BlockPos pos) {
        for (BlockPos blockpos : BlockPos.randomInCube(level.random, 10, pos, 1)) {
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(AllBlocks.EXPERIENCE_BLOCK.get())) {
                level.setBlockAndUpdate(blockpos, CeiBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState());
                level.levelEvent(3002, blockpos, -1);
                return Optional.of(blockpos);
            }
        }
        return Optional.empty();
    }
}
