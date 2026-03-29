package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import net.minecraft.world.SimpleContainer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone.GrindstoneHelper;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiItemAttributes {
    private static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTES = DeferredRegister
            .create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, EnchantmentIndustry.ID);

    public static final RegistryObject<ItemAttributeType> PROCESSABLE_BY_MECHANICAL_GRINDSTONE = attribute(
            "processable_by_mechanical_grindstone",
            "can be processed by Mechanical Grindstone",
            "cannot be processed by Mechanical Grindstone");

    private static RegistryObject<ItemAttributeType> attribute(String name, String description, String invertedDescription) {
        String descriptionKey = "create.item_attributes." + EnchantmentIndustry.ID + "." + name;
        String invertedDescriptionKey = descriptionKey + ".inverted";
        REGISTRATE.addRawLang(descriptionKey, description);
        REGISTRATE.addRawLang(invertedDescriptionKey, invertedDescription);
        return ITEM_ATTRIBUTES.register(name, () -> new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type,
                (itemStack, level) -> {
                    var container = new SimpleContainer(itemStack);
                    var recipeManager = level.getRecipeManager();
                    var grinding = recipeManager.getRecipeFor(CeiRecipeTypes.GRINDING.getType(), container, level);
                    if (grinding.isPresent())
                        return true;
                    if (recipeManager.getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), container, level).isPresent())
                        return true;
                    return GrindstoneHelper.canItemBeGrinded(itemStack, net.minecraft.world.item.ItemStack.EMPTY);
                },
                EnchantmentIndustry.ID + "." + name)));
    }

    public static void register(IEventBus modBus) {
        ITEM_ATTRIBUTES.register(modBus);
    }
}
