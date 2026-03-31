package plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy;

import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(value = AdvancementBehaviour.class, remap = false)
public interface AdvancementBehaviourAccessor {

    @Accessor(remap = false)
    UUID getPlayerId();
    
}
