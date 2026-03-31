# Changelog — Create: Enchantment Industry (Forge 1.20.1 Port)

> Audit period: 2026-03-29 to 2026-03-31
> Scope: 16 commits, ~70 files modified, 4 files deleted, ~45 files created
> Audited by: Gemini 2.5, 6x Claude Opus agents, ChatGPT o4 Pro, manual review

---

## Summary

Comprehensive quality audit and upstream parity pass for the Forge 1.20.1 port of Create: Enchantment Industry. Covers bug fixes (exploits, crashes, data loss), architecture refactoring, full Chinese localization, resource completeness, and upstream feature parity.

**By the numbers:**
- 100+ bugs identified, verified, and resolved
- 1 major architecture refactor (BlazeEnchanter dual-state unification)
- 1 new renderer ported (BlazeForgerRenderer)
- 22 advancements ported from upstream
- 3 missing recipes ported from upstream
- Full zh_cn.json localization (180+ keys, all polished)
- All resources cross-referenced against registrations (zero gaps)
- Final automated sweep: **CLEAN — zero remaining issues**

---

## Critical Fixes (crashes, compilation failures)

- **ExperienceOrb API**: Replaced non-existent `tryMergeToExisting()` with `ExperienceOrb.award()` for MC 1.20.1
- **FluidStack.CODEC**: Replaced NeoForge-only `FluidStack.CODEC` with manual `CompoundTag.CODEC.xmap()` serialization
- **PrintEntries catch scope**: `catch (ClassNotFoundException)` expanded to `catch (ClassNotFoundException | NoClassDefFoundError)`
- **ANTLR4 @NotNull**: Global replacement of `org.antlr.v4.runtime.misc.NotNull` with `javax.annotation.Nonnull` (10 files)
- **DisenchanterBlock.getDrops()**: Fixed method signature `LootContext.Builder` to `LootParams.Builder` (block was dropping nothing)
- **FurnaceExpExtractor NPE**: Null guards on all `BE.getLevel()` call sites
- **PrinterBlockEntity NPE**: `level` null check in `spawnParticles()` for NBT read path

## High-Priority Fixes (exploits, data loss)

- **BlazeEnchanterBlock template duplication**: Added `heldItem.shrink(1)` for non-creative players
- **GrindstoneDrain simulate bypass**: `clear()` now guarded by `if (!simulate)`
- **Untrusted packet validation**: Both `BlazeEnchanterEditPacket` and `EnchantingGuideEditPacket` now validate item type (ENCHANTED_BOOK), enchantment presence, and index bounds server-side
- **OpenEndedPipeMixin simulate fallthrough**: `cir.setReturnValue(true)` moved outside `!simulate` block; client-side early return now cancels CIR
- **DeployerExtension XP dupe**: Consumed XP calculated before `mendItem()` mutates the item; `event.setAmount(0)` prevents sub-3 XP accumulation on fake player
- **PrinterBlockEntity NPE**: `printEntry` null guard in goggle tooltip
- **DisenchanterBlockEntity Vec3.add()**: Return value now assigned (Vec3 is immutable)
- **ExperienceHatch XP truncation**: `Math.min()` cap prevents over-deduction from integer rounding
- **GrindstoneDrain recipe fallback**: Early return when GrindingRecipe matches but fluid check fails (no more fallthrough to vanilla)
- **Wrench conversion item loss**: `BlazeEnchanterBlock` and `BlazeForgerBlock` `onSneakWrenched()` now call `destroy()` before block replacement; BlazeForger returns anvil on both break and wrench
- **DisenchanterRenderer Vec3.add()**: Two discarded return values now assigned — fluid effect positioning corrected
- **DisenchanterBlockEntity phantom fluid**: Changed to SIMULATE-then-distribute-then-EXECUTE pattern
- **PrinterDisplaySource NPE**: Null guard on `printEntry` before dereference

## StoredEnchantments Fix (enchanted book NBT)

`EnchantmentHelper.getEnchantments()` in Forge 1.20.1 reads the `"Enchantments"` tag, but enchanted books store enchantments under `"StoredEnchantments"`. This caused silent failures across multiple systems.

**New utility:** `Enchanting.getAllEnchantments(ItemStack)` — checks for ENCHANTED_BOOK and reads StoredEnchantments with fallback.

**Fixed call sites (15+ total):**
- `BlazeForgerInventory` — all enchantment read/write paths (non-template combine, apply, split) + new `setEnchantmentsCorrectly()` helper
- `Disenchanting` — `disenchantResult()`, `disenchant()`, `getDisenchantExperience()`
- `PrintEntries` — `EnchantedBook` inner class (5 methods)
- `EnchantingGuideMenu` — `EnchantedBookSlot.mayPlace()`
- `EnchantingGuideItem` — `getSortedEnchantments()`
- `GrindstoneHelper` — `getExperienceFromItem()`

## Architecture Refactor: BlazeEnchanter Dual-State Unification

**Problem:** `BlazeEnchanterBlockEntity` and `EnchanterBehaviour` both independently maintained `templateItem` + `enchantingBehaviour` fields under different NBT keys. Processing used the BE's copy; UI used the behaviour's copy. They could diverge.

**Solution:** `EnchanterBehaviour` is now the single owner of template state. The BE delegates via thin wrappers (`getTemplateItem()`, `setTemplateItem()`, `isUsingTemplateMode()`).

**Changes:**
- Deleted 3 duplicate fields from BE: `templateItem`, `enchantingBehaviour`, `enchantLevel`
- Added 5 delegation methods to `EnchanterBehaviour`: `isUsingTemplateMode()`, `getTemplateEnchantmentCount()`, `applyEnchantment()`, `applyEnchantmentWithRandom()`, `consumeTemplate()`
- Removed `"TemplateItem"` serialization from BE; behaviour handles it under `"EnchantingTemplate"`
- Backward compatibility: `EnchanterBehaviour.read()` falls back to `"TemplateItem"` for old saves
- Removed dead code: `update()`, `getResult()` from EnchanterBehaviour

## Medium-Priority Fixes

- **BlazeForger notifyUpdate**: Only called at processing state transitions, not every tick
- **Static shared RecipeWrapper**: Create local instance per call in `Disenchanting`
- **FurnaceExpExtractor O(N)**: Mathematical calculation instead of ArrayList expansion
- **Missing reviveCaps()**: Added to all 6 block entities (or verified Create handles it)
- **SpoutBlockMixin**: Proper `@Overwrite` with `@author`/`@reason`
- **setTargetItem no setChanged()**: Added
- **absorbedXp flag**: Set true in both `>= ABSORB_AMOUNT` and `< ABSORB_AMOUNT` paths
- **GrindstoneDrain particle**: `motion.y` corrected to `motion.z`
- **Integer division truncation**: Cast to `(float)` before division in processing offset
- **PrinterBlock isClientSide guard**: Mutations wrapped in server-side check
- **ExperienceLantern division by zero**: `Math.max(0.05, distanceTo(...))` replaced with `distance < 0.01` guard
- **Disenchanting simulate side effects**: `allowInsertion()`/`forbidInsertion()` wrapped in try-finally (8 total sites across 2 files)
- **GrindstoneDrain assert as null check**: Replaced with `if (level == null) return;`
- **GrindstoneDrain destroy server guard**: Added `instanceof ServerLevel` check
- **PrinterTargetItemHandler**: `extractItem()` now respects `amount` parameter
- **BlazeForgerBlock.use()**: Server-side guards on all item insert/extract paths
- **CeiRecipeTypes namespace**: Uses mod's own namespace, not `create:`
- **GrindstoneDrain wrong output slot**: Result placed in slot 1+, not slot 0
- **FurnaceExpExtractor recipe tracking**: Explicit tracking without mid-iteration `clear()`
- **ApotheosisCompat**: `ForgeRegistries.ITEMS.getKey()` instead of `Item.toString()`
- **ExperienceLanternBlock shape**: Rotates based on FACING direction
- **ExperienceHelper XP formulas**: Verified correct against vanilla (false positive in original report)
- **GrindstoneDrainBlock.use()**: Passes `pos.above()` to grindstone (correct block position)
- **EnchantingItemHandler**: Template mode branch added for hopper/pipe automation
- **TemplateEnchantingBehaviour**: Uses `EnchantmentLevelUtil.getMaxLevel()` for Apotheosis compat; max cost instead of average cost
- **BlazeEnchanterBlockEntity writeSafe()**: Stripped transient state; only structural config persisted for schematics
- **superExperience cap**: Limited to 64 (prevents unbounded accumulation)
- **findNearbyLightningRod cache**: Cached for 200 ticks (4225-column scan → 1 validation call)
- **QuarkCompat**: `Optional.get()` replaced with `.map().orElse(false)`
- **Enchanting.getValidEnchantment()**: `ignoreEnchantmentCompatibility` config now actually consulted
- **Enchanting.UNENCHANTABLE_CONDITIONS**: `reduce().get()` replaced with `anyMatch()` (safe + short-circuits)
- **BlazeForgerBlockEntity.destroy()**: Iterates all 6 internal slots (was 4)
- **BlazeForgerArmInteractionPoint**: `notifyUpdate()` after extraction
- **BlazeForger fluid mode**: `inventory.updateResult()` called in `whenFluidUpdates` callback
- **SmartBlockEntityMixin**: `popExperience()` replaced with `ExperienceHelper.dropExperienceFluid()` (hyper XP now drops as HyperExperienceOrb)

## Low-Priority Fixes

- Removed unused `fluidCapability` and `advancement` fields from GrindstoneDrainBlockEntity
- Removed empty `invalidate()`/`reviveCaps()` overrides from BlazeForgerBlockEntity, ExperienceLanternBlockEntity
- Removed unused `getEnchantLevel()` method from BlazeEnchanterBlockEntity
- `CopyOnWriteArrayList` for `Enchanting.UNENCHANTABLE_CONDITIONS`; `ENTRIES` field `final`
- `instanceof ServerPlayer` pattern matching in DisenchanterBlockEntity (replaced raw casts)
- `Printing.print()` mutation contract documented via Javadoc
- `ComponentLabel` guard for `maxWidthPx <= 0`
- Removed dead `NameTag` PrintEntry (unreachable, replaced by `CustomNamePrintEntry`)
- Template/guide tooltip exclusive `else if` branching
- `AccumulativeTrigger.trigger()`: `instanceof ServerPlayer` guard
- Deleted dead `FluidTankBlockMixin` and `BasinBlockEntityMixin`
- `ClipBoard.match()` added `isEnabled()` check
- Removed dead AT entry for `ExperienceOrb.tryMergeToExisting`
- Removed 3 unused imports from `BlazeForgerInventory`
- `PackagePatternPrintEntry` and `BannerPatternPrintEntry`: `Component.translatable()` fixed to `LANG.translate()` with mod ID prefix

## Mixin System

- **9 Mixin files**: Added `remap = false` on `@Mixin` annotation for Create-targeting mixins (consistency)
- **CrushingWheelControllerBEMixin**: `remap = false` on `@Inject` + `remap = true` on `@At` (correct mixed targeting)
- **ItemDrainBlockMixin, SpoutBlockMixin**: Added explicit `remap = true` on vanilla method overrides
- **LightningBoltMixin**: Added `ExperienceConverted` persistent data flag for one-shot protection
- **ConnectivityHandlerMixin**: Documented RETURN ordinal targeting (3 return statements mapped)
- **PlayerMixin**: Documented boolean ordinal=3 targeting (4 locals mapped)
- **Deleted**: `BasinBlockEntityMixin` (empty no-op), `FluidTankBlockMixin` (dead code, not in mixin JSON)
- Mixin JSON: 19 common + 1 client = 20 entries, matches 20 .java files exactly

## New Features Ported from Upstream

### BlazeForgerRenderer (new file)
- Renders 4-6 floating, rotating items inside the Blaze Forger block
- Sine-wave vertical bobbing with per-slot offset
- Registered in CeiBlockEntities via `.renderer()`

### 22 Advancements (ported from NeoForge 1.21.1)
- Blaze Enchanter branch: first_order, additional_order, hypothetical_extension, sigil_forging, thousand_runes
- Blaze Forger branch: born_talent_of_fire, blazing_fusion, sigil_casting, magic_unbinding, blazing_centurion
- Experience branch: spiritual_return, lumen_nexus
- Printer branch: brand_registry, supply_chain_refactor, assembly_aesthetics
- Super Enchant: lightning_catalysis, probability_spike, transcendent_overclock, paradox_fusion, omni_enchanter, osha_violation
- Grindstone: grind_to_polish
- Recipe unlocks: experience_lantern, mechanical_grindstone, super_experience_block, super_experience_nugget
- Fixed: `a_shower_experience.json` parent chain corrected

### 3 Missing Cake Recipes (ported from upstream)
- `compacting/experience_cake_base.json` (egg + sugar + lapis lazuli)
- `filling/experience_cake.json` (cake base + 1000mB experience fluid)
- `cutting/experience_cake_slice.json` (cake → 4 slices via mechanical saw)

### Create Tags
- `create:fan_processing_catalysts/smoking` — blaze_enchanter + blaze_forger as fan smoking catalysts
- `create:fan_transparent` — fan effects visible through these blocks
- `create:bottomless/deny` — experience fluid denied from bottomless supply (balance)
- `minecraft:beacon_base_blocks` — super_experience_block as beacon base
- `forge:storage_blocks` (block + item) — super_experience_block
- `forge:nuggets` — super_experience_nugget
- `minecraft:mineable/pickaxe` — added 6 missing blocks

## Resource Fixes

- **4 item model JSONs**: `blaze_enchanter` (block parent), `experience_cake`/`experience_cake_base`/`experience_cake_slice` (item/generated)
- **experience_lantern block model**: Removed `neoforge_data` blocks (NeoForge 1.21+ feature)
- **Locale rename**: `es.mx.json` → `es_mx.json` (3 locations — Minecraft uses underscores)
- **pack.mcmeta**: `pack_format` 9 → 15 (correct for MC 1.20.1)
- **Cake lang key prefix**: `block.` → `item.` (these are items, not blocks)
- **Deleted dead texture**: `printing_template.png` (zero references in upstream and port)
- **Kept**: `experience_bucket.png` (upstream has bucket item — preserved for future porting)

## Localization (zh_cn.json)

- Added ~80 new translation keys covering all registered blocks, items, UI elements, goggles overlay, stats, death messages, recipe types, creative tab, and advancements
- Polished all ~180 existing translations for natural Chinese phrasing
- Key improvements: fixed redundant 的, replaced doubled verbs, added commas for readability, unified 超级经验 terminology, corrected sentence structure for death messages and stats
- Full parity: every key in en_us.json has a corresponding zh_cn.json entry

## Confirmed Not Needed (upstream analysis)

- **DeployerHandlerMixin**: Port uses `DeployerExtension` event handler (Forge 1.20.1 equivalent)
- **EnchantmentHelperMixin**: Patches 1.21+ DataComponent API, not applicable to 1.20.1
- **BEWLR item renderers**: JSON model parent chain already includes hat geometry via OBJ block model
- **Config file split**: Port's 52 options in CeiServerConfig cover all upstream functionality
- **Enchantment tags** (`cei:tags/enchantment/`): 1.21+ data-driven enchantment feature, not applicable
