package plus.dragons.createenchantmentindustry.foundation.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.FurnaceExpExtractor;

@Mixin(AbstractFurnaceBlockEntity.class)
@Implements(@Interface(iface = FurnaceXpRemainderAccessor.class, prefix = "createEnchantmentIndustry$"))
abstract public class AbstractFurnaceBlockEntityMixin<T> extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Final
    @Shadow
    private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

    @Unique
    LazyOptional<IFluidHandler> createEnchantmentIndustry$expExtractor = LazyOptional.of(this::createEnchantmentIndustry$createExpExtractor);

    @Unique
    private double createEnchantmentIndustry$xpRemainder = 0.0;

    @Unique
    private IFluidHandler createEnchantmentIndustry$createExpExtractor(){
        return new FurnaceExpExtractor(recipesUsed,(AbstractFurnaceBlockEntity)(Object)this);
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    private void createEnchantmentIndustry$getCapability(Capability<T> capability, Direction facing, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (!this.remove && facing != null && capability == ForgeCapabilities.FLUID_HANDLER) {
            cir.setReturnValue(createEnchantmentIndustry$expExtractor.cast());
        }
    }

    @Inject(method = "invalidateCaps", at = @At("HEAD"), remap = false)
    private void createEnchantmentIndustry$invalidateCaps(CallbackInfo ci) {
        createEnchantmentIndustry$expExtractor.invalidate();
    }

    @Inject(method = "reviveCaps", at = @At("HEAD"), remap = false)
    private void createEnchantmentIndustry$reviveCaps(CallbackInfo ci) {
        this.createEnchantmentIndustry$expExtractor = LazyOptional.of(this::createEnchantmentIndustry$createExpExtractor);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void createEnchantmentIndustry$saveAdditional(CompoundTag tag, CallbackInfo ci) {
        if (createEnchantmentIndustry$xpRemainder != 0.0) {
            tag.putDouble("cei$XpRemainder", createEnchantmentIndustry$xpRemainder);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void createEnchantmentIndustry$load(CompoundTag tag, CallbackInfo ci) {
        createEnchantmentIndustry$xpRemainder = tag.getDouble("cei$XpRemainder");
    }

    public double createEnchantmentIndustry$cei$getXpRemainder() {
        return createEnchantmentIndustry$xpRemainder;
    }

    public void createEnchantmentIndustry$cei$setXpRemainder(double remainder) {
        createEnchantmentIndustry$xpRemainder = remainder;
    }
}
