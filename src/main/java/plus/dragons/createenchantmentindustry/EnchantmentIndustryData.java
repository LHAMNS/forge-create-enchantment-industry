package plus.dragons.createenchantmentindustry;

/**
 * Data generation entry point.
 * <p>
 * In the upstream CEI (1.21.1/6.0.0-dev), this class drives datagen for blockstates,
 * item models, loot tables, recipes, tags, and lang files. In this Forge 1.20.1 port,
 * all resources are managed manually under {@code src/main/resources/} because:
 * <ul>
 *   <li>The port uses Registrate for most block/item registration, which handles
 *       model and blockstate generation at runtime.</li>
 *   <li>Recipes, loot tables, tags, and lang entries are hand-written JSON files
 *       checked into the repository.</li>
 *   <li>Running datagen would require a working runData Gradle task with all
 *       dependencies resolved, which adds complexity for minimal benefit given
 *       the existing hand-crafted resources.</li>
 * </ul>
 * <p>
 * If datagen is needed in the future, implement {@code GatherDataEvent} handling here
 * and register providers for each resource type.
 */
public class EnchantmentIndustryData {
    // Resources are manually managed. See class Javadoc for rationale.
}
