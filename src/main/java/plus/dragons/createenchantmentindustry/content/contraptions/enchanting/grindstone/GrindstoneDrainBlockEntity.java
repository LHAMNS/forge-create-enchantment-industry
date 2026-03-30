package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.grindstone;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;
import plus.dragons.createenchantmentindustry.entry.CeiStats;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy.AdvancementBehaviourAccessor;

import java.util.List;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class GrindstoneDrainBlockEntity extends KineticBlockEntity {
    public static final int GRINDING_TIME = 20;
    public ProcessingInventory inventory;
    private ItemStack processedItem = ItemStack.EMPTY;
    protected SmartFluidTankBehaviour tank;
    private DirectBeltInputBehaviour beltInput;

    private LazyOptional<IItemHandler> itemCapability;

    public GrindstoneDrainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ProcessingInventory(this::start) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                // Force single-item processing to prevent stacked input issues
                if (stack.getCount() > 1) {
                    ItemStack single = stack.copyWithCount(1);
                    var space = tank.getPrimaryHandler().getSpace();
                    int a = GrindstoneHelper.getExperienceFromItem(single);
                    int b = level != null ? GrindstoneHelper.getExperienceFromGrindingRecipe(level, single) : 0;
                    if (a > space || b > space) return stack;
                    ItemStack result = super.insertItem(slot, single, simulate);
                    if (result.isEmpty()) {
                        // Successfully inserted 1, return the rest
                        return stack.copyWithCount(stack.getCount() - 1);
                    }
                    return stack;
                }
                var space = tank.getPrimaryHandler().getSpace();
                int a = GrindstoneHelper.getExperienceFromItem(stack);
                int b = level != null ? GrindstoneHelper.getExperienceFromGrindingRecipe(level, stack) : 0;
                if (a > space || b > space) return stack;
                return super.insertItem(slot, stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 3000) {
                    if (amount <= 0)
                        return ItemStack.EMPTY;
                    int[] preferredSlots = {1, 2, 3, 0};
                    for (int actualSlot : preferredSlots) {
                        ItemStack stack = getStackInSlot(actualSlot);
                        if (stack.isEmpty())
                            continue;
                        ItemStack result = stack.copy();
                        result.setCount(Math.min(amount, stack.getCount()));
                        if (!simulate) {
                            ItemStack remainder = stack.copy();
                            remainder.shrink(result.getCount());
                            setStackInSlot(actualSlot, remainder);
                            boolean anyItemsLeft = false;
                            for (int i = 0; i < getSlots(); i++) {
                                if (!getStackInSlot(i).isEmpty()) {
                                    anyItemsLeft = true;
                                    break;
                                }
                            }
                            if (!anyItemsLeft) {
                                remainingTime = -1;
                                appliedRecipe = false;
                            }
                            GrindstoneDrainBlockEntity.this.setChanged();
                            GrindstoneDrainBlockEntity.this.sendData();
                        }
                        return result;
                    }
                    return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
        }.withSlotLimit(true);
        itemCapability = LazyOptional.of(() -> inventory);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        tank = SmartFluidTankBehaviour.single(this, CeiConfigs.SERVER.mechanicalGrindstoneTankCapacity.get());
        beltInput = new DirectBeltInputBehaviour(this).allowingBeltFunnels();
        var advancement = new AdvancementBehaviour(this);
        behaviours.add(tank);
        behaviours.add(beltInput);
        behaviours.add(advancement);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != Direction.DOWN) {
            return itemCapability.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            Direction facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
            if (side == facing.getOpposite() || side == null) {
                LazyOptional<?> fluidCap = tank.getCapability();
                return fluidCap.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> inventory);
    }

    private Direction getOutputSide() {
        var facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        var speed = facing == Direction.WEST || facing == Direction.NORTH ? getSpeed() * -1 : getSpeed();
        return speed > 0 ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public float getRelativeSpeed() {
        if (level == null) return 0f;
        float speed = getSpeed();
        if (speed == 0f)
            return 0f;
        var above = worldPosition.above();
        var aboveState = level.getBlockState(above);
        if (!(aboveState.getBlock() instanceof MechanicalGrindstoneBlock grinderWheel))
            return 0f;
        var facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (grinderWheel.getRotationAxis(aboveState) != facing.getAxis())
            return 0f;
        float aboveSpeed = grinderWheel.getBlockEntityOptional(level, above)
                .map(KineticBlockEntity::getSpeed).orElse(0f);
        if (speed > 0f) {
            return aboveSpeed < 0f ? Math.min(speed, -aboveSpeed) : 0f;
        } else {
            return aboveSpeed > 0f ? Math.min(-speed, aboveSpeed) : 0f;
        }
    }

    private int getProcessDuration(ItemStack inputStack) {
        if (level == null) return 10;
        var recipeManager = level.getRecipeManager();
        RecipeWrapper wrapper = createWrapper(inputStack);
        var sizeModifier = Math.max(1, Mth.ceil(inputStack.getCount() / 5.0f));
        var grinding = recipeManager.getRecipeFor(CeiRecipeTypes.GRINDING.getType(), wrapper, level);
        if (grinding.isPresent() && grinding.get() instanceof GrindingRecipe grindingRecipe) {
            return grindingRecipe.getProcessingDuration() * sizeModifier;
        }
        if (recipeManager.getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), wrapper, level).isPresent()) {
            return 50 * sizeModifier;
        }
        if (GrindstoneHelper.canItemBeGrinded(inputStack, ItemStack.EMPTY)) {
            return 50 * sizeModifier;
        }
        return 10;
    }

    private static RecipeWrapper createWrapper(ItemStack stack) {
        return new RecipeWrapper(new net.minecraftforge.items.ItemStackHandler(1) {{
            setStackInSlot(0, stack);
        }});
    }

    private boolean fill(FluidStack fluid) {
        if (tank.getPrimaryHandler().fill(fluid, FluidAction.SIMULATE) == fluid.getAmount()) {
            tank.getPrimaryHandler().fill(fluid, FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    private boolean drain(FluidIngredient fluidIngredient) {
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        int required = fluidIngredient.getRequiredAmount();
        if (fluidIngredient.test(fluid) && fluid.getAmount() >= required) {
            fluid.shrink(required);
            tank.getPrimaryHandler().setFluid(fluid);
            return true;
        }
        return false;
    }

    private void start(ItemStack inputStack) {
        if (level == null) return;
        if (inventory.isEmpty())
            return;
        if (level.isClientSide && !isVirtual())
            return;
        inventory.remainingTime = inventory.recipeDuration = getProcessDuration(inputStack);
        inventory.appliedRecipe = false;
        sendData();
    }

    private void applyRecipe() {
        if (level == null) return;
        var recipeManager = level.getRecipeManager();
        var inputStack = inventory.getStackInSlot(0);
        RecipeWrapper wrapper = createWrapper(inputStack);
        // Grinding
        var grinding = SequencedAssemblyRecipe.getRecipe(level, wrapper, CeiRecipeTypes.GRINDING.getType(), GrindingRecipe.class);
        if (grinding.isEmpty())
            grinding = recipeManager.getRecipeFor(CeiRecipeTypes.GRINDING.getType(), wrapper, level);
        if (grinding.isPresent()) {
            var recipe = grinding.get();
            var fluidIngredients = recipe.getFluidIngredients();
            var fluidResults = recipe.getFluidResults();
            boolean applicable = fluidIngredients.isEmpty() && fluidResults.isEmpty();
            if (!fluidIngredients.isEmpty())
                applicable = drain(fluidIngredients.get(0));
            else if (!fluidResults.isEmpty())
                applicable = fill(fluidResults.get(0));
            if (applicable) {
                if (!fluidResults.isEmpty() && fluidResults.get(0).getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())) {
                    // Track grinding stats using CEI custom stats
                    int xpAmount = fluidResults.get(0).getAmount();
                    awardGrindingStat(xpAmount);
                }
                inventory.clear();
                var grinded = recipe.rollResults();
                for (int i = 0; i < grinded.size(); i++)
                    inventory.setStackInSlot(i + 1, grinded.get(i));
                return;
            }
            // GrindingRecipe matched but fluid check failed — do not fall through
            // to sandpaper polishing or vanilla grindstone logic
            return;
        }
        // Sand Paper Polishing
        var polishing = recipeManager.getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), wrapper, level);
        if (polishing.isPresent() && AllRecipeTypes.CAN_BE_AUTOMATED.test(polishing.get())) {
            var polished = polishing.get().getResultItem(level.registryAccess());
            inventory.clear();
            inventory.setStackInSlot(1, polished);
            return;
        }
        // Grind Stone
        var grindstone = GrindstoneHelper.grindItem(level, inputStack, ItemStack.EMPTY);
        if (grindstone.isPresent()) {
            var result = grindstone.get();
            var fluid = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), result.experience());
            if (fill(fluid)) {
                // Track grinding stats
                awardGrindingStat(result.experience());
                inventory.clear();
                inventory.setStackInSlot(1, result.top());
                inventory.setStackInSlot(2, result.bottom());
                inventory.setStackInSlot(3, result.output());
            }
        }
    }

    /**
     * Award the MECHANICAL_GRINDSTONE_EXPERIENCE custom stat to the player who placed this block.
     * Uses the AdvancementBehaviour to find the owning player.
     */
    private void awardGrindingStat(int xpAmount) {
        if (level == null || level.isClientSide || xpAmount <= 0) return;
        var advBehaviour = getBehaviour(AdvancementBehaviour.TYPE);
        if (advBehaviour == null) return;
        // Use the accessor to get the player ID from advancement behaviour
        var accessor = (plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy.AdvancementBehaviourAccessor) advBehaviour;
        var playerId = accessor.getPlayerId();
        if (playerId == null) return;
        var player = level.getPlayerByUUID(playerId);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.CUSTOM.get(CeiStats.MECHANICAL_GRINDSTONE_EXPERIENCE.get()), xpAmount);
        }
    }

    private void spawnProcessedParticles(ItemStack stack) {
        if (level == null) return;
        if (stack.isEmpty())
            return;

        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem blockItem)
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState());
        else
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);

        Vec3 pos = Vec3.atBottomCenterOf(this.worldPosition).add(0, 1, 0);
        for (int i = 0; i < 10; i++) {
            Vec3 motion = VecHelper.offsetRandomly(new Vec3(0, 0.25f, 0), level.random, .125f);
            level.addParticle(particleData, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }

    private void spawnProcessingParticles(ItemStack stack) {
        if (level == null) return;
        if (stack.isEmpty())
            return;

        float speed;
        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem blockItem) {
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState());
            speed = 1f;
        } else {
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);
            speed = .125f;
        }

        Vec3 pos = Vec3.atBottomCenterOf(worldPosition).add(0, 1, 0);
        Direction inputSide = getOutputSide().getOpposite();
        float offset = inventory.recipeDuration != 0 ? (float) inventory.remainingTime / inventory.recipeDuration : 0;
        offset /= 2;
        if (inventory.appliedRecipe)
            offset -= .5f;
        level.addParticle(particleData,
                pos.x + inputSide.getStepX() * offset,
                pos.y,
                pos.z + inputSide.getStepZ() * offset,
                inputSide.getStepX() * speed,
                level.random.nextFloat() * speed,
                inputSide.getStepZ() * speed);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("Inventory", inventory.serializeNBT());
        if (clientPacket && !processedItem.isEmpty()) {
            compound.put("ProcessedItem", processedItem.serializeNBT());
            processedItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        if (compound.contains("ProcessedItem"))
            processedItem = ItemStack.of(compound.getCompound("ProcessedItem"));
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null && !level.isClientSide) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), processedItem);
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inventory.extractItem(3000, 64, false));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        if (level == null) return;
        super.tickAudio();
        if (getSpeed() == 0)
            return;

        if (!processedItem.isEmpty()) {
            spawnProcessedParticles(processedItem);
            processedItem = ItemStack.EMPTY;
            level.levelEvent(1042, worldPosition, 0);
        }
    }

    @Override
    public void tick() {
        if (level == null) return;
        super.tick();

        float processingSpeed = getRelativeSpeed();
        if (processingSpeed == 0)
            return;
        if (inventory.remainingTime == -1) {
            if (!inventory.isEmpty() && !inventory.appliedRecipe)
                start(inventory.getStackInSlot(0));
            return;
        }

        processingSpeed = Mth.clamp(processingSpeed / 24, 1, 128);
        inventory.remainingTime -= processingSpeed;

        if (inventory.remainingTime > 0)
            spawnProcessingParticles(inventory.getStackInSlot(0));

        if (inventory.remainingTime < 5 && !inventory.appliedRecipe) {
            if (level.isClientSide && !isVirtual())
                return;
            processedItem = inventory.getStackInSlot(0);
            applyRecipe();
            inventory.appliedRecipe = true;
            inventory.recipeDuration = 20;
            inventory.remainingTime = 20;
            sendData();
            return;
        }

        Direction outputSide = getOutputSide();
        if (inventory.remainingTime > 0)
            return;
        inventory.remainingTime = 0;

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            ItemStack toFunnel = beltInput.tryExportingToBeltFunnel(stack, outputSide.getOpposite(), false);
            if (toFunnel != null) {
                if (toFunnel.getCount() != stack.getCount()) {
                    inventory.setStackInSlot(slot, toFunnel);
                    notifyUpdate();
                    return;
                }
                if (!toFunnel.isEmpty())
                    return;
            }
        }

        BlockPos outputPos = worldPosition.relative(outputSide);
        DirectBeltInputBehaviour outputTarget = BlockEntityBehaviour.get(level, outputPos, DirectBeltInputBehaviour.TYPE);
        if (outputTarget != null) {
            boolean changed = false;
            if (!outputTarget.canInsertFromSide(outputSide))
                return;
            if (level.isClientSide && !isVirtual())
                return;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty())
                    continue;
                ItemStack remainder = outputTarget.handleInsertion(stack, outputSide, false);
                if (ItemStack.matches(remainder, stack))
                    continue;
                inventory.setStackInSlot(slot, remainder);
                changed = true;
            }
            if (changed) {
                // Check if all items have been output
                boolean allEmpty = true;
                for (int slot2 = 0; slot2 < inventory.getSlots(); slot2++) {
                    if (!inventory.getStackInSlot(slot2).isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                }
                if (allEmpty) {
                    inventory.remainingTime = -1;
                    inventory.appliedRecipe = false;
                }
                setChanged();
                sendData();
            }
            return;
        }

        Vec3 itemMovement = Vec3.atLowerCornerOf(outputSide.getNormal());
        Vec3 outPos = VecHelper.getCenterOf(worldPosition).add(itemMovement.scale(.5f).add(0, .5, 0));
        Vec3 outMotion = itemMovement.scale(.0625).add(0, .125, 0);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, stack);
            entityIn.setDeltaMovement(outMotion);
            level.addFreshEntity(entityIn);
        }
        inventory.clear();
        inventory.appliedRecipe = false;
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        inventory.remainingTime = -1;
        sendData();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        added |= this.containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
        return added;
    }
}
