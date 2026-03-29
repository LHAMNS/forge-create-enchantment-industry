package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.foundation.CreateNBTProcessors;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Equivalent to upstream's CreateNBTProcessorsMixin.
 * Removes CEI-specific template/working data from clipboard and schematic NBT
 * so that schematics/blueprints don't capture internal processing state.
 */
@Mixin(value = CreateNBTProcessors.class, remap = false)
public class CreateNBTProcessorsMixin {

    @Inject(method = "clipboardProcessor", at = @At("HEAD"))
    private static void cei$clipboardProcessor$removeCEITemplates(CompoundTag data, CallbackInfoReturnable<CompoundTag> cir) {
        // Remove Printer's stored copy target
        data.remove("copyTarget");
        // Remove Blaze Enchanter's template item and enchanting level
        data.remove("TemplateItem");
        data.remove("EnchantingLevel");
        data.remove("EnchantLevel");
    }
}
