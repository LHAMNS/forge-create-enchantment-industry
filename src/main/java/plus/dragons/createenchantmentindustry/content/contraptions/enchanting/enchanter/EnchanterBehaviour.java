package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.behaviour.EnchantingBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.behaviour.TemplateEnchantingBehaviour;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/**
 * ScrollValueBehaviour extension for the Blaze Enchanter.
 * Allows players to scroll-set the enchanting level (0 to maxLevel)
 * and manage the enchanting template item.
 * <p>
 * Equivalent to upstream's EnchanterBehaviour (1.21.1/6.0.0-dev).
 */
public class EnchanterBehaviour extends ScrollValueBehaviour implements IHaveGoggleInformation {
    public static final BehaviourType<EnchanterBehaviour> TYPE = new BehaviourType<>();
    public static final String LEVEL = "EnchantingLevel";
    public static final String TEMPLATE = "EnchantingTemplate";

    private final BlazeEnchanterBlockEntity enchanter;
    private ItemStack template = ItemStack.EMPTY;
    private EnchantingBehaviour enchanting = new EnchantingBehaviour();
    ValueBoxTransform.Sided templateItemTransform;

    public EnchanterBehaviour(BlazeEnchanterBlockEntity enchanter, ValueBoxTransform transform, ValueBoxTransform.Sided templateItemTransform) {
        super(LANG.translate("gui.blaze_enchanter.level").component(), enchanter, transform);
        this.enchanter = enchanter;
        this.templateItemTransform = templateItemTransform;
    }

    public ValueBoxTransform getTemplateItemSlotPositioning() {
        return templateItemTransform;
    }

    public boolean canProcess(ItemStack stack) {
        return enchanting.canProcess(stack, enchanter.targetItem, enchanter.hyper());
    }

    public void update(ItemStack stack) {
        // Update the enchanting behaviour state based on current value and enchanter state
    }

    public ItemStack getResult(ItemStack stack) {
        // Template-based path
        if (!template.isEmpty() && enchanting instanceof TemplateEnchantingBehaviour) {
            enchanting.applyEnchantment(stack, enchanter.targetItem, enchanter.hyper());
            return stack;
        }
        // Guide-based path
        var entry = Enchanting.getValidEnchantment(stack, enchanter.targetItem, enchanter.hyper());
        if (entry != null) {
            Enchanting.enchantItem(stack, entry);
        }
        return stack;
    }

    public int getExperienceCost() {
        return enchanting.getExperienceCost(enchanter.targetItem, enchanter.hyper());
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean setTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            template = ItemStack.EMPTY;
            enchanting = new EnchantingBehaviour();
        } else if (stack.isEnchantable()) {
            template = stack;
            enchanting = new TemplateEnchantingBehaviour(template);
        } else {
            return false;
        }
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, 0, enchanter.getMaxEnchantLevel());
        if (value == this.value)
            return;
        this.value = value;
        update(enchanter.getHeldItemStack());
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        int max = enchanter.getMaxEnchantLevel();
        return new ValueSettingsBoard(
                label,
                max,
                max / 6,
                ImmutableList.of(label),
                new ValueSettingsFormatter(ValueSettings::format));
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);
        if (AllItems.WRENCH.isIn(stack))
            return;
        if (AllBlocks.MECHANICAL_ARM.isIn(stack))
            return;
        var level = getWorld();
        var pos = getPos();
        if (stack.isEmpty()) {
            setTemplate(ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, .25f, .1f);
            return;
        }
        if (!setTemplate(stack.copyWithCount(1))) {
            player.displayClientMessage(LANG.translate("gui.blaze_enchanter.template.invalid").component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putInt(LEVEL, value);
        nbt.put(TEMPLATE, template.serializeNBT());
        super.write(nbt, clientPacket);
    }

    @Override
    public void writeSafe(CompoundTag nbt) {
        nbt.putInt(LEVEL, value);
        super.writeSafe(nbt);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        value = Math.min(Math.max(nbt.getInt(LEVEL), 0), enchanter.getMaxEnchantLevel());
        template = ItemStack.of(nbt.getCompound(TEMPLATE));
        var level = getWorld();
        if (level != null)
            setTemplate(template);
        super.read(nbt, clientPacket);
    }

    @Override
    public void initialize() {
        setTemplate(template);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = false;
        if (!template.isEmpty()) {
            LANG.translate("gui.goggles.enchanting.template").forGoggles(tooltip);
            LANG.builder().add(template.getHoverName().copy()).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            added = true;
        }
        var style = enchanter.hyper()
                ? (enchanter.cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        if (value > 0) {
            LANG.translate("gui.goggles.enchanting.level", value).style(style).forGoggles(tooltip);
            added = true;
        } else {
            LANG.translate("gui.goggles.enchanting.level.not_set").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        int cost = getExperienceCost();
        if (cost > 0) {
            LANG.translate("gui.goggles.enchanting.cost", cost).text(" mB").style(style).forGoggles(tooltip);
            added = true;
        }
        if (!enchanter.getHeldItemStack().isEmpty() && enchanter.processingTicks <= 0) {
            if (!EnchantmentHelper.getEnchantments(enchanter.getHeldItemStack()).isEmpty())
                LANG.translate("gui.goggles.enchanting.completed").style(ChatFormatting.GREEN).forGoggles(tooltip);
            else
                LANG.translate("gui.goggles.enchanting.invalid_item").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        return added;
    }
}
