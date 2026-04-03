package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

/**
 * Interface for accessing XP remainder tracking on furnace block entities.
 * Implemented by {@link AbstractFurnaceBlockEntityMixin} via {@code @Implements}.
 */
public interface FurnaceXpRemainderAccessor {
    double cei$getXpRemainder();
    void cei$setXpRemainder(double remainder);
}
