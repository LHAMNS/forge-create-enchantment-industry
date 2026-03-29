package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import javax.annotation.Nonnull;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.behaviour.EnchantingBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.behaviour.TemplateEnchantingBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiDataMaps;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

public class BlazeEnchanterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, MenuProvider {

    public static final int ENCHANTING_TIME = 200;
    SmartFluidTankBehaviour internalTank;
    TransportedItemStack heldItem;
    ItemStack targetItem = new ItemStack(CeiItems.ENCHANTING_GUIDE.get());
    /** Template item placed on the enchanter for template-based enchanting. Empty = use EnchantingGuide mode. */
    ItemStack templateItem = ItemStack.EMPTY;
    /** Current enchanting behaviour - switches between guide-based and template-based. */
    EnchantingBehaviour enchantingBehaviour = new EnchantingBehaviour();
    int processingTicks;
    /** Whether this enchanter is in "cursed" mode (hyper mode with lightning rod deflecting lightning). */
    protected boolean cursed;
    /** Internal super experience counter - filled by special ExperienceFuel items, not piped in. */
    protected int superExperience;
    /** Whether this enchanter has infinite liquid (Creative Blaze Cake applied). */
    protected boolean isCreative;
    /** Seed for deterministic enchantment randomization. Updated after each enchanting operation. */
    protected long enchantmentSeed;
    /** Snapshotted cost coefficient at processing start, to prevent mid-processing config changes. */
    protected float snapshotCostCoefficient = 1.0f;
    /** ScrollValue-based enchant level (0 = not set, 1-maxLevel). Equivalent to upstream's EnchanterBehaviour value. */
    protected int enchantLevel;
    /** The EnchanterBehaviour (ScrollValueBehaviour) for scroll-wheel enchant level selection. */
    protected EnchanterBehaviour enchanterBehaviour;
    Map<Direction, LazyOptional<EnchantingItemHandler>> itemHandlers;
    boolean sendParticles;
    LerpedFloat headAnimation;
    LerpedFloat headAngle;
    Random random = new Random();
    float flip;
    float oFlip;
    float flipT;
    float flipA;
    public boolean goggles;

    public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        itemHandlers = new IdentityHashMap<>();
        for (Direction d : Iterate.horizontalDirections) {
            EnchantingItemHandler enchantingItemHandler = new EnchantingItemHandler(this, d);
            itemHandlers.put(d, LazyOptional.of(() -> enchantingItemHandler));
        }
        headAnimation = LerpedFloat.linear();
        headAngle = LerpedFloat.angular();
        headAngle.startWithValue((AngleHelper
                .horizontalAngle(state.getOptionalValue(BlazeEnchanterBlock.FACING)
                        .orElse(Direction.SOUTH)) + 180) % 360
        );
        goggles = false;
        enchantmentSeed = pos.asLong();
        superExperience = 0;
        isCreative = false;
    }

    /**
     * Get the maximum enchant level based on whether super experience is active.
     * Equivalent to upstream's getMaxEnchantLevel().
     */
    public int getMaxEnchantLevel() {
        return getMaxEnchantLevel(hyper());
    }

    /**
     * Get the maximum enchant level for the given mode.
     */
    public int getMaxEnchantLevel(boolean superMode) {
        int max = CeiConfigs.SERVER.blazeEnchanterMaxEnchantLevel.get();
        int maxSuper = CeiConfigs.SERVER.blazeEnchanterMaxSuperEnchantLevel.get();
        return superMode ? Math.max(max, maxSuper) : Math.min(max, maxSuper);
    }

    /**
     * Get the current scroll-value enchant level.
     */
    public int getEnchantLevel() {
        return enchanterBehaviour != null ? enchanterBehaviour.getValue() : enchantLevel;
    }

    @Override
    @SuppressWarnings("deprecation") //Fluid Tags are still useful for mod interaction
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels()
                .setInsertionHandler(this::tryInsertingFromSide));
        behaviours.add(internalTank = FilteringFluidTankBehaviour
                .single(fluidStack -> fluidStack.getFluid().is(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag),
                    this, CeiConfigs.SERVER.blazeEnchanterTankCapacity.get())
                .whenFluidUpdates(() -> {
                    var fluid = internalTank.getPrimaryHandler().getFluid().getFluid();
                    if (CeiFluids.EXPERIENCE.is(fluid))
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.KINDLED);
                    else if (CeiFluids.HYPER_EXPERIENCE.is(fluid))
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.SEETHING);
                    else
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.SMOULDERING);
                }));
        // Register the ScrollValue-based EnchanterBehaviour for enchant level selection
        enchanterBehaviour = new EnchanterBehaviour(this,
                new com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform(
                        (blockState, direction) -> direction.getAxis().isHorizontal()),
                new EnchanterTemplateItemTransform());
        enchanterBehaviour.between(0, getMaxEnchantLevel());
        enchanterBehaviour.withCallback(i -> {
            this.enchantLevel = i;
        });
        behaviours.add(enchanterBehaviour);
        registerAwardables(behaviours,
                CeiAdvancements.FIRST_ORDER.asCreateAdvancement(),
                CeiAdvancements.ADDITIONAL_ORDER.asCreateAdvancement(),
                CeiAdvancements.HYPOTHETICAL_EXTENSION.asCreateAdvancement(),
                CeiAdvancements.OSHA_VIOLATION.asCreateAdvancement(),
                CeiAdvancements.SIGIL_FORGING.asCreateAdvancement(),
                CeiAdvancements.THOUSAND_RUNES.asCreateAdvancement(),
                CeiAdvancements.PROBABILITY_SPIKE.asCreateAdvancement(),
                CeiAdvancements.TRANSCENDENT_OVERCLOCK.asCreateAdvancement(),
                CeiAdvancements.PARADOX_FUSION.asCreateAdvancement(),
                CeiAdvancements.OMNI_ENCHANTER.asCreateAdvancement());
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        boolean onClient = level.isClientSide && !isVirtual();

        if (onClient) {
            bookTick();
            blazeTick();
        }

        // Update cursed state: in hyper mode, if lightning rod is nearby, enchanting becomes cursed
        boolean isHyper = hyper();
        var strikePos = getStrikePos();
        boolean newCursed = isHyper && strikePos != null && !worldPosition.equals(strikePos);
        if (this.cursed != newCursed) {
            this.cursed = newCursed;
        }

        if (heldItem == null) {
            processingTicks = 0;
            return;
        }


        if (processingTicks > 0) {
            heldItem.prevBeltPosition = .5f;
            boolean wasAtBeginning = processingTicks == ENCHANTING_TIME;
            if (!onClient || processingTicks < ENCHANTING_TIME)
                processingTicks--;
            if (!continueProcessing()) {
                processingTicks = 0;
                notifyUpdate();
                return;
            }
            // Interesting Trigger Sync Design
            if (wasAtBeginning != (processingTicks == ENCHANTING_TIME))
                sendData();
            // A return here
            return;
        }

        heldItem.prevBeltPosition = heldItem.beltPosition;
        heldItem.prevSideOffset = heldItem.sideOffset;

        heldItem.beltPosition += itemMovementPerTick();
        if (heldItem.beltPosition > 1) {
            heldItem.beltPosition = 1;

            if (onClient)
                return;

            Direction side = heldItem.insertedFrom;

            /* DirectBeltInputBehaviour#tryExportingToBeltFunnel(ItemStack, Direction, boolean) return null to
             * represent insertion is invalid due to invalidity
             * of funnel (excludes funnel being powered) or something go wrong. */
            ItemStack tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE)
                    .tryExportingToBeltFunnel(heldItem.stack, side.getOpposite(), false);
            if (tryExportingToBeltFunnel != null) {
                if (tryExportingToBeltFunnel.getCount() != heldItem.stack.getCount()) {
                    if (tryExportingToBeltFunnel.isEmpty())
                        heldItem = null;
                    else
                        heldItem.stack = tryExportingToBeltFunnel;
                    notifyUpdate();
                    return;
                }
                if (!tryExportingToBeltFunnel.isEmpty())
                    return;
            }

            BlockPos nextPosition = worldPosition.relative(side);
            DirectBeltInputBehaviour directBeltInputBehaviour =
                    BlockEntityBehaviour.get(level, nextPosition, DirectBeltInputBehaviour.TYPE);
            if (directBeltInputBehaviour == null) {
                if (!BlockHelper.hasBlockSolidSide(level.getBlockState(nextPosition), level, nextPosition,
                        side.getOpposite())) {
                    ItemStack ejected = heldItem.stack;
                    // Following "Launching out" process can be used as standard.
                    Vec3 outPos = VecHelper.getCenterOf(worldPosition)
                            .add(Vec3.atLowerCornerOf(side.getNormal())
                                    .scale(.75));
                    float movementSpeed = itemMovementPerTick();
                    Vec3 outMotion = Vec3.atLowerCornerOf(side.getNormal())
                            .scale(movementSpeed)
                            .add(0, 1 / 8f, 0);
                    outPos = outPos.add(outMotion.normalize());
                    ItemEntity entity = new ItemEntity(level, outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
                    entity.setDeltaMovement(outMotion);
                    entity.setDefaultPickUpDelay();
                    entity.hurtMarked = true;
                    level.addFreshEntity(entity);

                    heldItem = null;
                    notifyUpdate();
                }
                return;
            }

            if (!directBeltInputBehaviour.canInsertFromSide(side))
                return;

            ItemStack returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);

            if (returned.isEmpty()) {
                heldItem = null;
                notifyUpdate();
                return;
            }

            if (returned.getCount() != heldItem.stack.getCount()) {
                heldItem.stack = returned;
                notifyUpdate();
                return;
            }

            return;
        }

        if (heldItem.prevBeltPosition < .5f && heldItem.beltPosition >= .5f) {
            // Use behaviour-based check if template is set, otherwise fall back to guide-based check
            boolean canProcess;
            if (!templateItem.isEmpty()) {
                canProcess = enchantingBehaviour.canProcess(heldItem.stack, targetItem, hyper());
            } else {
                canProcess = Enchanting.getValidEnchantment(heldItem.stack, targetItem, hyper()) != null;
            }
            if (!canProcess)
                return;
            heldItem.beltPosition = .5f;
            if (onClient)
                return;
            processingTicks = ENCHANTING_TIME;
            // Snapshot cost coefficient at processing start
            snapshotCostCoefficient = hyper()
                    ? CeiConfigs.SERVER.hyperEnchantByBlazeEnchanterCostCoefficient.getF()
                    : CeiConfigs.SERVER.enchantByBlazeEnchanterCostCoefficient.getF();
            sendData();
        }

    }

    protected void blazeTick() {
        boolean active = processingTicks > 0;

        if (!active) {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (getBlockPos().getX() + 0.5);
                double dz = z - (getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
            headAngle.chase(target, .25f, LerpedFloat.Chaser.exp(5));
            headAngle.tickChaser();
        } else {
            headAngle.chase((AngleHelper.horizontalAngle(getBlockState().getOptionalValue(BlazeEnchanterBlock.FACING)
                    .orElse(Direction.SOUTH)) + 180) % 360, .125f, LerpedFloat.Chaser.EXP);
            headAngle.tickChaser();
        }
        headAnimation.chase(1, .25f, LerpedFloat.Chaser.exp(.25f));
        headAnimation.tickChaser();

        spawnBlazeParticles();
    }

    protected void bookTick() {
        if (random.nextInt(40) == 0) {
            float oFlipT = flipT;
            while (oFlipT == flipT) {
                flipT += (random.nextInt(4) - random.nextInt(4));
            }
        }
        oFlip = flip;
        float flipDiff = (flipT - flip) * 0.4F;
        flipDiff = Mth.clamp(flipDiff, -0.2F, 0.2F);
        flipA += (flipDiff - flipA) * 0.9F;
        flip += flipA;
    }

    protected void spawnBlazeParticles() {
        if (level == null)
            return;
        BlazeEnchanterBlock.HeatLevel heatLevel = getBlockState().getValue(BlazeEnchanterBlock.HEAT_LEVEL);

        var r = level.random;

        Vec3 c = VecHelper.getCenterOf(worldPosition);
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
                .multiply(1, 0, 1));

        if (r.nextInt(3) == 0)
            level.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);
        if (r.nextInt(2) != 0)
            return;

        boolean empty = level.getBlockState(worldPosition.above())
                .getCollisionShape(level, worldPosition.above())
                .isEmpty();

        double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
        Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
                        .multiply(1, .25f, 1)
                        .normalize()
                        .scale((empty ? .25f : .5) + r.nextDouble() * .125f))
                .add(0, .5, 0);

        if (heatLevel.isAtLeast(BlazeEnchanterBlock.HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(BlazeEnchanterBlock.HeatLevel.KINDLED)) {
            level.addParticle(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        }
    }

    protected static int ENCHANT_PARTICLE_COUNT = 20;

    protected void spawnEnchantParticles() {
        if (isVirtual())
            return;
        Vec3 vec = VecHelper.getCenterOf(worldPosition);
        vec = vec.add(0, 1, 0);
        ParticleOptions particle = ParticleTypes.ENCHANT;
        for (int i = 0; i < ENCHANT_PARTICLE_COUNT; i++) {
            Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1f);
            m = new Vec3(m.x, Math.abs(m.y), m.z);
            level.addAlwaysVisibleParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
        }
        level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, level.random.nextFloat() * .1f + .9f, true);
    }

    protected boolean continueProcessing() {
        if (level.isClientSide && !isVirtual()) {
            if (processingTicks > 0 && processingTicks < 200 && level.getGameTime() % 80L == 0L)
                ((ClientLevel) level).playLocalSound(worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f, true);
            return true;
        }
        if (processingTicks < 5)
            return true;

        boolean hyper = hyper();

        // Template-based enchanting path
        if (!templateItem.isEmpty()) {
            if (!enchantingBehaviour.canProcess(heldItem.stack, targetItem, hyper))
                return false;

            int cost = (int) (enchantingBehaviour.getExperienceCost(targetItem, hyper) * snapshotCostCoefficient);
            FluidStack exp = new FluidStack(hyper
                    ? CeiFluids.HYPER_EXPERIENCE.get().getSource()
                    : CeiFluids.EXPERIENCE.get().getSource(), cost);

            if (processingTicks > 5) {
                if (!isCreative) {
                    boolean hasSuperExp = hyper && superExperience >= cost;
                    var tankFluid = internalTank.getPrimaryHandler().getFluid().getFluid();
                    if (!hasSuperExp && (!CeiFluids.EXPERIENCE.is(tankFluid) && !CeiFluids.HYPER_EXPERIENCE.is(tankFluid) ||
                            internalTank.getPrimaryHandler().getFluidAmount() < exp.getAmount())) {
                        processingTicks = ENCHANTING_TIME;
                    }
                }
                return true;
            }

            // Lightning strike check in hyper mode
            if (hyper && !cursed && level instanceof ServerLevel serverLevel) {
                var lightningStrikePos = getStrikePos();
                if (lightningStrikePos != null && strikeLightning(serverLevel, lightningStrikePos)) {
                    award(CeiAdvancements.OSHA_VIOLATION.asCreateAdvancement());
                    serverLevel.destroyBlock(worldPosition, false);
                    serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.BLAZE_BURNER.getDefaultState());
                    return false;
                }
            }
            // Process finished - apply template enchanting
            enchantingBehaviour.applyEnchantment(heldItem.stack, targetItem, hyper);
            // In cursed mode, also apply a random curse enchantment
            if (cursed) {
                Enchanting.applyCurseEnchantment(heldItem.stack, random);
            }
            consumeExperience(cost, hyper);
            advanceEnchantmentSeed();
            // Consume the template item to prevent infinite reuse (item duplication)
            templateItem.shrink(1);
            if (templateItem.isEmpty()) {
                templateItem = ItemStack.EMPTY;
                enchantingBehaviour = new EnchantingBehaviour();
            }
            sendParticles = true;
            notifyUpdate();
            return true;
        }

        // Standard guide-based enchanting path
        Pair<Enchantment, Integer> entry = Enchanting.getValidEnchantment(heldItem.stack, targetItem, hyper);
        if (entry == null)
            return false;

        FluidStack exp = new FluidStack(hyper
                ? CeiFluids.HYPER_EXPERIENCE.get().getSource()
                : CeiFluids.EXPERIENCE.get().getSource(),
                (int) (Enchanting.getExperienceConsumption(entry.getFirst(), entry.getSecond()) *
                        (hyper? CeiConfigs.SERVER.hyperEnchantByBlazeEnchanterCostCoefficient.get():
                                CeiConfigs.SERVER.enchantByBlazeEnchanterCostCoefficient.get()))
        );

        if (processingTicks > 5) {
            if (!isCreative) {
                boolean hasSuperExp = hyper && superExperience >= exp.getAmount();
                var tankFluid = internalTank.getPrimaryHandler().getFluid().getFluid();
                if (!hasSuperExp && (!CeiFluids.EXPERIENCE.is(tankFluid) && !CeiFluids.HYPER_EXPERIENCE.is(tankFluid) ||
                        internalTank.getPrimaryHandler().getFluidAmount() < exp.getAmount())) {
                    processingTicks = ENCHANTING_TIME;
                }
            }
            return true;
        }

        // Lightning strike check in hyper mode
        if (hyper && !cursed && level instanceof ServerLevel serverLevel) {
            var lightningStrikePos = getStrikePos();
            if (lightningStrikePos != null && strikeLightning(serverLevel, lightningStrikePos)) {
                award(CeiAdvancements.OSHA_VIOLATION.asCreateAdvancement());
                serverLevel.destroyBlock(worldPosition, false);
                serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.BLAZE_BURNER.getDefaultState());
                return false;
            }
        }
        // Advancement
        if (EnchantmentHelper.getEnchantments(heldItem.stack).isEmpty())
            award(CeiAdvancements.FIRST_ORDER.asCreateAdvancement());
        else
            award(CeiAdvancements.ADDITIONAL_ORDER.asCreateAdvancement());
        if (hyper)
            award(CeiAdvancements.HYPOTHETICAL_EXTENSION.asCreateAdvancement());
        // Process finished
        Enchanting.enchantItem(heldItem.stack, entry);
        // In cursed mode, also apply a random curse enchantment
        if (cursed) {
            Enchanting.applyCurseEnchantment(heldItem.stack, random);
        }
        consumeExperience(exp.getAmount(), hyper);
        advanceEnchantmentSeed();
        sendParticles = true;
        notifyUpdate();
        return true;
    }

    private float itemMovementPerTick() {
        return 1 / 8f;
    }

    public void setTargetItem(ItemStack itemStack) {
        targetItem = itemStack;
        setChanged();
    }

    /**
     * Get the template item currently placed on the enchanter.
     */
    public ItemStack getTemplateItem() {
        return templateItem;
    }

    /**
     * Set the template item on the enchanter.
     * If the item is enchantable, switches to TemplateEnchantingBehaviour.
     * If empty, switches back to default EnchantingBehaviour.
     * @return true if the template was accepted
     */
    public boolean setTemplateItem(ItemStack stack) {
        if (stack.isEmpty()) {
            templateItem = ItemStack.EMPTY;
            enchantingBehaviour = new EnchantingBehaviour();
        } else if (stack.isEnchantable()) {
            templateItem = stack;
            enchantingBehaviour = new TemplateEnchantingBehaviour(templateItem);
        } else {
            return false;
        }
        setChanged();
        sendData();
        return true;
    }

    /**
     * Check if the current enchanting behaviour can process the given item.
     */
    public boolean canProcessWithBehaviour(ItemStack stack) {
        return enchantingBehaviour.canProcess(stack, targetItem, hyper());
    }

    /**
     * Apply enchantment using the current behaviour.
     */
    public void applyEnchantmentWithBehaviour(ItemStack stack) {
        enchantingBehaviour.applyEnchantment(stack, targetItem, hyper());
    }

    /**
     * Get experience cost using the current behaviour.
     */
    public int getExperienceCostFromBehaviour() {
        return enchantingBehaviour.getExperienceCost(targetItem, hyper());
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        ItemStack inserted = transportedStack.stack;
        ItemStack returned = ItemStack.EMPTY;

        if (!getHeldItemStack().isEmpty())
            return inserted;

        // Check if item can be processed - support both template mode and guide mode
        boolean canProcess;
        if (!templateItem.isEmpty()) {
            canProcess = enchantingBehaviour.canProcess(inserted, targetItem, hyper());
        } else {
            canProcess = Enchanting.getValidEnchantment(inserted, targetItem, hyper()) != null;
        }

        if (inserted.getCount() > 1 && canProcess) {
            returned = ItemHandlerHelper.copyStackWithSize(inserted, inserted.getCount() - 1);
            inserted = ItemHandlerHelper.copyStackWithSize(inserted, 1);
        }

        if (simulate)
            return returned;

        transportedStack = transportedStack.copy();
        transportedStack.stack = inserted.copy();
        transportedStack.beltPosition = side.getAxis()
                .isVertical() ? .5f : 0;
        transportedStack.prevSideOffset = transportedStack.sideOffset;
        transportedStack.prevBeltPosition = transportedStack.beltPosition;
        setHeldItem(transportedStack, side);
        setChanged();
        sendData();

        return returned;
    }

    public ItemStack getHeldItemStack() {
        return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
    }

    public void setHeldItem(TransportedItemStack heldItem, Direction insertedFrom) {
        this.heldItem = heldItem;
        this.heldItem.insertedFrom = insertedFrom;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (LazyOptional<EnchantingItemHandler> lazyOptional : itemHandlers.values())
            lazyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandlers = new IdentityHashMap<>();
        for (Direction d : Iterate.horizontalDirections) {
            EnchantingItemHandler enchantingItemHandler = new EnchantingItemHandler(this, d);
            itemHandlers.put(d, LazyOptional.of(() -> enchantingItemHandler));
        }
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state){
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME,targetItem);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level instanceof ServerLevel serverLevel) {
            ItemStack heldItemStack = getHeldItemStack();
            var pos = getBlockPos();
            if (!heldItemStack.isEmpty())
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), targetItem);
            if (!templateItem.isEmpty())
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), templateItem);
            var tank = internalTank.getPrimaryHandler();
            var fluidStack = tank.getFluid();
            ExperienceFluid expFluid = CeiDataMaps.asExperienceFluid(fluidStack.getFluid());
            if(expFluid != null) {
                expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
            }
        }
    }

    public boolean hyper() {
        return superExperience > 0 || CeiFluids.HYPER_EXPERIENCE.is(internalTank.getPrimaryHandler().getFluid().getFluid());
    }

    /** Get the internal super experience counter value. */
    public int getSuperExperience() {
        return superExperience;
    }

    /** Add super experience from a special ExperienceFuel item. */
    public void addSuperExperience(int amount) {
        this.superExperience += amount;
        notifyUpdate();
    }

    /** Whether this enchanter has creative (infinite) mode enabled. */
    public boolean isCreative() {
        return isCreative;
    }

    /** Apply creative mode (from Creative Blaze Cake). */
    public void applyCreativeMode() {
        this.isCreative = true;
        notifyUpdate();
    }

    /** Get the enchantment seed for deterministic randomization. */
    public long getEnchantmentSeed() {
        return enchantmentSeed;
    }

    /** Advance the enchantment seed after each enchanting operation. */
    protected void advanceEnchantmentSeed() {
        enchantmentSeed = enchantmentSeed * 6364136223846793005L + 1442695040888963407L;
    }

    /**
     * Consume experience for enchanting. Prefers superExperience for hyper enchanting.
     * If creative mode, always succeeds without draining.
     * @return true if enough experience was available
     */
    protected boolean consumeExperience(int amount, boolean hyper) {
        if (isCreative) return true;
        if (hyper && superExperience >= amount) {
            superExperience -= amount;
            return true;
        }
        // Fall back to tank
        FluidStack exp = new FluidStack(hyper
                ? CeiFluids.HYPER_EXPERIENCE.get().getSource()
                : CeiFluids.EXPERIENCE.get().getSource(), amount);
        if (internalTank.getPrimaryHandler().getFluidAmount() >= amount) {
            internalTank.getPrimaryHandler().drain(exp, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    @Override
    public void write(CompoundTag compoundTag, boolean clientPacket) {
        super.write(compoundTag, clientPacket);
        compoundTag.putInt("ProcessingTicks", processingTicks);
        compoundTag.put("TargetItem", targetItem.serializeNBT());
        compoundTag.putBoolean("Goggles", goggles);
        compoundTag.putInt("SuperExperience", superExperience);
        compoundTag.putBoolean("IsCreative", isCreative);
        compoundTag.putLong("EnchantmentSeed", enchantmentSeed);
        compoundTag.putInt("EnchantLevel", enchantLevel);
        compoundTag.putFloat("SnapshotCostCoeff", snapshotCostCoefficient);
        if (!templateItem.isEmpty())
            compoundTag.put("TemplateItem", templateItem.serializeNBT());
        if (heldItem != null)
            compoundTag.put("HeldItem", heldItem.serializeNBT());
        if (sendParticles && clientPacket) {
            compoundTag.putBoolean("SpawnParticles", true);
            sendParticles = false;
        }
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        tag.put("TargetItem", new ItemStack(CeiItems.ENCHANTING_GUIDE.get()).serializeNBT());
        tag.putBoolean("Goggles", goggles);
    }

    @Override
    protected void read(CompoundTag compoundTag, boolean clientPacket) {
        super.read(compoundTag, clientPacket);
        heldItem = null;
        processingTicks = compoundTag.getInt("ProcessingTicks");
        targetItem = ItemStack.of(compoundTag.getCompound("TargetItem"));
        goggles = compoundTag.getBoolean("Goggles");
        superExperience = compoundTag.getInt("SuperExperience");
        isCreative = compoundTag.getBoolean("IsCreative");
        enchantmentSeed = compoundTag.contains("EnchantmentSeed") ? compoundTag.getLong("EnchantmentSeed") : worldPosition.asLong();
        enchantLevel = compoundTag.getInt("EnchantLevel");
        snapshotCostCoefficient = compoundTag.contains("SnapshotCostCoeff") ? compoundTag.getFloat("SnapshotCostCoeff") : 1.0f;
        if (compoundTag.contains("TemplateItem")) {
            templateItem = ItemStack.of(compoundTag.getCompound("TemplateItem"));
            if (!templateItem.isEmpty() && templateItem.isEnchantable()) {
                enchantingBehaviour = new TemplateEnchantingBehaviour(templateItem);
            } else {
                templateItem = ItemStack.EMPTY;
                enchantingBehaviour = new EnchantingBehaviour();
            }
        } else {
            templateItem = ItemStack.EMPTY;
            enchantingBehaviour = new EnchantingBehaviour();
        }
        if (compoundTag.contains("HeldItem"))
            heldItem = TransportedItemStack.read(compoundTag.getCompound("HeldItem"));
        if (!clientPacket)
            return;
        if (compoundTag.contains("SpawnParticles"))
            spawnEnchantParticles();
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (side != null && side.getAxis()
                .isHorizontal() && isItemHandlerCap(capability))
            return itemHandlers.get(side)
                    .cast();

        if ((side == Direction.DOWN || side == null) && isFluidHandlerCap(capability))
            return internalTank.getCapability()
                    .cast();
        return super.getCapability(capability, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LANG.translate("gui.goggles.blaze_enchanter").forGoggles(tooltip);
        if (!templateItem.isEmpty()) {
            LANG.translate("gui.goggles.blaze_enchanter.template").forGoggles(tooltip);
            tooltip.add(Component.literal("     ")
                    .append(templateItem.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (targetItem != null && targetItem.is(CeiItems.ENCHANTING_GUIDE.get())) {
            EnchantmentEntry entry = Enchanting.getTargetEnchantment(targetItem, hyper());
            if (entry != null) {
                tooltip.add(Component.literal("     ")
                        .append(entry.getFirst().getFullname(entry.getSecond())));
                if (!entry.valid())
                    tooltip.add(Component.literal("     ")
                            .append(LANG.translate("gui.goggles.blaze_enchanter.invalid_target").component())
                            .withStyle(ChatFormatting.RED));
                else {
                    int consumption = (int) (Enchanting.getExperienceConsumption(entry.getFirst(), entry.getSecond()) *
                            (hyper()? CeiConfigs.SERVER.hyperEnchantByBlazeEnchanterCostCoefficient.get():
                                    CeiConfigs.SERVER.enchantByBlazeEnchanterCostCoefficient.get()));
                    if (consumption > CeiConfigs.SERVER.blazeEnchanterTankCapacity.get())
                        tooltip.add(Component.literal("     ").append(LANG.translate("gui.goggles.too_expensive")
                                        .component())
                                .withStyle(ChatFormatting.RED));
                    else
                        tooltip.add(Component.literal("     ")
                                .append(LANG.translate("gui.goggles.xp_consumption", consumption).component())
                                .withStyle(ChatFormatting.GREEN));
                }
            }
        }
        containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
        return true;
    }

    // ── Lightning Strike Logic (matches upstream BlazeExperienceBlockEntity) ──

    /**
     * Get the position where lightning would strike above this block.
     * Returns null if the dimension has no sky light or has a ceiling.
     */
    @Nullable
    protected BlockPos getStrikePos() {
        if (level == null) return null;
        var dimension = level.dimensionType();
        if (!dimension.hasSkyLight()) return null;
        if (dimension.hasCeiling()) return null;
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, worldPosition).below();
    }

    /**
     * Attempt to strike lightning at the given position.
     * Returns true if the lightning hits directly (no lightning rod protection),
     * meaning the machine should self-destruct.
     */
    protected boolean strikeLightning(ServerLevel serverLevel, BlockPos strikePos) {
        var lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning == null) return false;
        // Check if there's a lightning rod that could deflect the strike
        // Look in a reasonable area above for a lightning rod
        BlockPos rodPos = findNearbyLightningRod(serverLevel, strikePos);
        if (rodPos != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(rodPos.above()));
        } else {
            lightning.moveTo(Vec3.atBottomCenterOf(strikePos.above()));
        }
        serverLevel.addFreshEntity(lightning);
        return rodPos == null; // Direct hit if no rod found
    }

    /**
     * Search for a lightning rod near the strike position.
     */
    @Nullable
    private BlockPos findNearbyLightningRod(ServerLevel level, BlockPos strikePos) {
        int searchRadius = 32;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                mutable.set(strikePos.getX() + dx, 0, strikePos.getZ() + dz);
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, mutable.getX(), mutable.getZ()) - 1;
                mutable.setY(surfaceY);
                BlockState state = level.getBlockState(mutable);
                if (state.is(CeiTags.LIGHTNING_RODS) || state.getBlock() instanceof LightningRodBlock) {
                    return mutable.immutable();
                }
            }
        }
        return null;
    }

    public void updateHeatLevel(BlazeEnchanterBlock.HeatLevel heatLevel) {
        if (level != null)
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlazeEnchanterBlock.HEAT_LEVEL, heatLevel));
    }

    @Override
    public Component getDisplayName() {
        return targetItem.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new EnchantingGuideMenu(CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, targetItem, getBlockPos());
    }

    /**
     * ValueBoxTransform for the template item slot on the Blaze Enchanter.
     * Positioned above the enchant level scroll value.
     */
    private static class EnchanterTemplateItemTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 14.5);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
