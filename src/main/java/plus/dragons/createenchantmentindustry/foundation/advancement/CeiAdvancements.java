package plus.dragons.createenchantmentindustry.foundation.advancement;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import net.minecraft.Util;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.AdvancementHolder;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.AccumulativeTrigger;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import java.util.Map;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.ADVANCEMENT_FACTORY;

public class CeiAdvancements {
    private static boolean registered = false;
    public static final AdvancementHolder
    START = null,
    // Root
    EXPERIENCED_ENGINEER = ADVANCEMENT_FACTORY.builder("experienced_engineer")
            .title("Experienced Engineer")
            .description("Get some Nuggets of Experience from crushing ores or killing mobs using deployer")
            .icon(AllItems.EXP_NUGGET)
            .externalTrigger("have_experience_nugget", InventoryChangeTrigger.TriggerInstance.hasItems(AllItems.EXP_NUGGET.get()))
            .parent(Create.asResource("display_board_0"))
            .build(),
    // Printer Branch
    BLACK_AS_INK = ADVANCEMENT_FACTORY.builder("black_as_ink")
            .title("Black as Ink!")
            .description("Get a bucket of Ink for your publishing business")
            .icon(CeiFluids.INK.get().getBucket())
            .externalTrigger("have_bucket_of_ink", InventoryChangeTrigger.TriggerInstance.hasItems(CeiFluids.INK.get().getBucket()))
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    COPIABLE_MASTERPIECE = ADVANCEMENT_FACTORY.builder("copiable_masterpiece")
            .title("Copiable Masterpiece")
            .description("Copy a Written Book using Printer")
            .icon(Items.WRITTEN_BOOK)
            .parent(BLACK_AS_INK)
            .build(),
    COPIABLE_MYSTERY = ADVANCEMENT_FACTORY.builder("copiable_mystery")
            .title("Copiable Mystery")
            .description("Copy a Enchanted Book using Printer")
            .icon(Items.ENCHANTED_BOOK)
            .announce(true)
            .parent(COPIABLE_MASTERPIECE)
            .build(),
    RELIC_RESTORATION = ADVANCEMENT_FACTORY.builder("relic_restoration")
            .title("Relic Restoration")
            .description("Make brand new copy from a tattered book")
            .icon(Items.WRITABLE_BOOK)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(COPIABLE_MYSTERY)
            .build(),
     EMERGING_BRAND = ADVANCEMENT_FACTORY.builder("emerging_brand")
             .title("Emerging Brand")
             .description("Using the printer to name items")
             .icon(Items.NAME_TAG)
             .announce(true)
             .parent(COPIABLE_MASTERPIECE)
             .build(),
    BRAND_REGISTRY = ADVANCEMENT_FACTORY.builder("brand_registry")
            .title("Brand Registry")
            .description("Use a Printer to rename an item")
            .icon(Items.NAME_TAG)
            .parent(COPIABLE_MYSTERY)
            .build(),
    SUPPLY_CHAIN_REFACTOR = ADVANCEMENT_FACTORY.builder("supply_chain_refactor")
            .title("Supply Chain Refactor")
            .description("Use a Printer to change a package's address")
            .icon(Items.PAPER)
            .parent(BRAND_REGISTRY)
            .build(),
    ASSEMBLY_AESTHETICS = ADVANCEMENT_FACTORY.builder("assembly_aesthetics")
            .title("Assembly Aesthetics")
            .description("Use a Printer to change a package's pattern")
            .icon(Items.PAPER)
            .parent(SUPPLY_CHAIN_REFACTOR)
            .build(),
    GREAT_PUBLISHER = ADVANCEMENT_FACTORY.builder("great_publisher")
            .title("Great Publisher")
            .description("Copy 1000 books using Printer")
            .externalTrigger("book_copied", new AccumulativeTrigger.TriggerInstance(CeiTriggers.BOOK_PRINTED.getId(), ContextAwarePredicate.ANY, MinMaxBounds.Ints.atLeast(1000)))
            .icon(CeiBlocks.PRINTER)
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(RELIC_RESTORATION)
            .build(),
    EXPERIMENTAL = ADVANCEMENT_FACTORY.builder("experimental")
            .title("Experimental")
            .description("Get some Liquid Experience for your enchanting experiment!")
            .icon(Items.EXPERIENCE_BOTTLE)
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    GONE_WITH_THE_FOIL = ADVANCEMENT_FACTORY.builder("gone_with_the_foil")
            .title("Gone with the Foil")
            .description("Watch an enchanted item be disenchanted by a Disenchanter")
            .icon(CeiBlocks.DISENCHANTER)
            .parent(EXPERIMENTAL)
            .build(),
    SPIRIT_TAKING = ADVANCEMENT_FACTORY.builder("spirit_taking")
            .title("Spirit Taking")
            .description("Get your experience absorbed by a Disenchanter")
            .icon(AllBlocks.MECHANICAL_PUMP)
            .announce(true)
            .parent(GONE_WITH_THE_FOIL)
            .build(),
    SPIRITUAL_RETURN = ADVANCEMENT_FACTORY.builder("spiritual_return")
            .title("Spiritual Return")
            .description("Retrieve some experience using an Experience Hatch")
            .icon(AllItems.EXP_NUGGET)
            .announce(true)
            .parent(SPIRIT_TAKING)
            .build(),
    A_SHOWER_EXPERIENCE = ADVANCEMENT_FACTORY.builder("a_shower_experience")
            .title("A Shower \"Experience\"")
            .description("Break a Fluid Pipe and bathe in the leaked experience")
            .icon(AllBlocks.FLUID_PIPE)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(SPIRITUAL_RETURN)
            .build(),
    LUMEN_NEXUS = ADVANCEMENT_FACTORY.builder("lumen_nexus")
            .title("Lumen Nexus")
            .description("Obtain an Experience Lantern")
            .icon(CeiBlocks.EXPERIENCE_LANTERN)
            .externalTrigger("have_experience_lantern", InventoryChangeTrigger.TriggerInstance.hasItems(CeiBlocks.EXPERIENCE_LANTERN.get().asItem()))
            .parent(SPIRITUAL_RETURN)
            .build(),
    EXPERIENCED_RECYCLER = ADVANCEMENT_FACTORY.builder("experienced_recycler")
            .title("Experienced Recycler")
            .description("Recycle 1,000,000 mB of experience from Disenchanter")
            .icon(AllBlocks.COPPER_VALVE_HANDLE)
            .externalTrigger("experience_recycled", new AccumulativeTrigger.TriggerInstance(CeiTriggers.DISENCHANTED.getId(), ContextAwarePredicate.ANY, MinMaxBounds.Ints.atLeast(1000000)))
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(A_SHOWER_EXPERIENCE)
            .build(),
    // Blaze Enchanter Branch
    BLAZES_NEW_JOB = ADVANCEMENT_FACTORY.builder("blazes_new_job")
            .title("Blaze's New Job")
            .description("Give your Blaze Burner a Enchanting Guide and turn it into a Blaze Enchanter")
            .icon(CeiItems.ENCHANTING_GUIDE)
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    FIRST_ORDER = ADVANCEMENT_FACTORY.builder("first_order")
            .title("First Order")
            .description("Add a new enchantment to an unenchanted item using Blaze Enchanter")
            .icon(Items.GOLDEN_HELMET)
            .parent(BLAZES_NEW_JOB)
            .build(),
    ADDITIONAL_ORDER = ADVANCEMENT_FACTORY.builder("additional_order")
            .title("Additional Order")
            .description("Add a new enchantment to an enchanted item using Blaze Enchanter")
            .icon(Util.make(
                    new ItemStack(Items.GOLDEN_HELMET),
                    stack -> EnchantmentHelper.setEnchantments(Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5), stack)
            ))
            .parent(FIRST_ORDER)
            .build(),
    HYPOTHETICAL_EXTENSION = ADVANCEMENT_FACTORY.builder("hypothetical_extension")
            .title("Hypothetical Extension")
            .description("Add a new enchantment to an item using Blaze Enchanter's hyper-enchanting")
            .icon(Items.DIAMOND_HELMET)
            .parent(ADDITIONAL_ORDER)
            .build(),
    // Blaze Enchanter - additional advancements
    SIGIL_FORGING = ADVANCEMENT_FACTORY.builder("sigil_forging")
            .title("Sigil Forging")
            .description("Add a new enchantment to an Enchanting Template using a Blaze Enchanter")
            .icon(CeiItems.ENCHANTING_GUIDE)
            .parent(ADDITIONAL_ORDER)
            .build(),
    THOUSAND_RUNES = ADVANCEMENT_FACTORY.builder("thousand_runes")
            .title("Thousand Runes")
            .description("Use a Blaze Enchanter 1,000 times")
            .icon(CeiBlocks.BLAZE_ENCHANTER)
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(SIGIL_FORGING)
            .build(),
    // Blaze Forger Branch
    BORN_TALENT_OF_FIRE = ADVANCEMENT_FACTORY.builder("born_talent_of_fire")
            .title("Born Talent of Fire")
            .description("Blazes were born for this. Obtain a Blaze Forger")
            .icon(CeiBlocks.BLAZE_FORGER)
            .externalTrigger("have_blaze_forger", InventoryChangeTrigger.TriggerInstance.hasItems(CeiBlocks.BLAZE_FORGER.get().asItem()))
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    BLAZING_FUSION = ADVANCEMENT_FACTORY.builder("blazing_fusion")
            .title("Blazing Fusion")
            .description("Merge two enchanted items or books using the Blaze Forger")
            .icon(Items.ANVIL)
            .parent(BORN_TALENT_OF_FIRE)
            .build(),
    SIGIL_CASTING = ADVANCEMENT_FACTORY.builder("sigil_casting")
            .title("Sigil Casting")
            .description("Apply an Enchanting Template using a Blaze Forger")
            .icon(CeiItems.ENCHANTING_GUIDE)
            .parent(BLAZING_FUSION)
            .build(),
    MAGIC_UNBINDING = ADVANCEMENT_FACTORY.builder("magic_unbinding")
            .title("Magic Unbinding")
            .description("Strip an item's enchantment off using a Blaze Forger")
            .icon(Items.ENCHANTED_BOOK)
            .parent(SIGIL_CASTING)
            .build(),
    BLAZING_CENTURION = ADVANCEMENT_FACTORY.builder("blazing_centurion")
            .title("Blazing Centurion")
            .description("Use a Blaze Forger 1,000 times")
            .icon(Items.ANVIL)
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(MAGIC_UNBINDING)
            .build(),
    // Super Enchant Branch
    LIGHTNING_CATALYSIS = ADVANCEMENT_FACTORY.builder("lightning_catalysis")
            .title("Lightning Catalysis")
            .description("Obtain Super Experience")
            .icon(CeiBlocks.SUPER_EXPERIENCE_BLOCK)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    PROBABILITY_SPIKE = ADVANCEMENT_FACTORY.builder("probability_spike")
            .title("Probability Spike")
            .description("How did all these treasures get here?")
            .icon(Items.DIAMOND)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(LIGHTNING_CATALYSIS)
            .build(),
    TRANSCENDENT_OVERCLOCK = ADVANCEMENT_FACTORY.builder("transcendent_overclock")
            .title("Transcendent Overclock")
            .description("How is this possible? Enchantment level caps don't exist?")
            .icon(Items.EMERALD)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(PROBABILITY_SPIKE)
            .build(),
    PARADOX_FUSION = ADVANCEMENT_FACTORY.builder("paradox_fusion")
            .title("Paradox Fusion")
            .description("How is this possible? They shouldn't appear at the same time!")
            .icon(Items.REDSTONE)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(TRANSCENDENT_OVERCLOCK)
            .build(),
    OMNI_ENCHANTER = ADVANCEMENT_FACTORY.builder("omni_enchanter")
            .title("Omni-Enchanter")
            .description("Super Enchant 100 times")
            .icon(Items.NETHER_STAR)
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(PARADOX_FUSION)
            .build(),
    // OSHA Violation - Lightning destroys the machine
    OSHA_VIOLATION = ADVANCEMENT_FACTORY.builder("osha_violation")
            .title("OSHA Violation")
            .description("Watch your Blaze Enchanter or Forger get destroyed by lightning due to lack of a lightning rod")
            .icon(Items.BARRIER)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(LIGHTNING_CATALYSIS)
            .build(),
    // Mechanical Grindstone Branch
    GRIND_TO_POLISH = ADVANCEMENT_FACTORY.builder("grind_to_polish")
            .title("Grind to Polish")
            .description("Sandpaper? I've got a better one")
            .icon(AllBlocks.MECHANICAL_PUMP)
            .announce(true)
            .parent(GONE_WITH_THE_FOIL)
            .build(),
    END = null;
    
    public static void register() {
        if (!registered) {
            ADVANCEMENT_FACTORY.register();
        }
        registered = true;
    }
    
}
