package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.materials.ExperienceNuggetItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.Tags;
import net.minecraft.world.item.Item;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceRotorItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottleItem;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiItems {

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE = REGISTRATE.item("enchanting_guide", EnchantingGuideItem::new)
            .properties(prop -> prop.stacksTo(1))
            .register();

    public static final ItemEntry<EnchantingTemplateItem> ENCHANTING_TEMPLATE = REGISTRATE
            .item("enchanting_template", EnchantingTemplateItem::normal)
            .properties(prop -> prop.stacksTo(16))
            .lang("Enchanting Template")
            .register();

    public static final ItemEntry<EnchantingTemplateItem> SUPER_ENCHANTING_TEMPLATE = REGISTRATE
            .item("super_enchanting_template", EnchantingTemplateItem::special)
            .properties(prop -> prop.stacksTo(16).rarity(Rarity.RARE))
            .lang("Super Enchanting Template")
            .register();

    public static final ItemEntry<HyperExperienceBottleItem> HYPER_EXP_BOTTLE = REGISTRATE.item("hyper_experience_bottle", HyperExperienceBottleItem::new)
            .properties(prop -> prop.rarity(Rarity.RARE))
            .lang("Bottle O' Hyper Enchanting")
            .tag(CeiTags.ItemTag.UPRIGHT_ON_BELT.tag)
            .register();

    public static final ItemEntry<ExperienceRotorItem> EXPERIENCE_ROTOR = REGISTRATE.item("experience_rotor", ExperienceRotorItem::new)
            .register();

    public static final ItemEntry<ExperienceNuggetItem> SUPER_EXPERIENCE_NUGGET = REGISTRATE
            .item("super_experience_nugget", ExperienceNuggetItem::new)
            .tag(Tags.Items.NUGGETS)
            .properties(p -> p.rarity(Rarity.RARE))
            .lang("Nugget of Super Experience")
            .register();

    public static void register() {}

}