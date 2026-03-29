# Forge-Create: Enchantment Industry

A Forge 1.20.1 port of [Create: Enchantment Industry](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry), originally developed for NeoForge 1.21.1 by **DragonsPlusMinecraft**.

This mod extends [Create](https://www.curseforge.com/minecraft/mc-mods/create) with enchanting & experience automation, including a Blaze Enchanter, Blaze Forger, Mechanical Grindstone, Printer, and liquid experience system.

## Features

- **Blaze Enchanter** - Automated enchanting table powered by liquid experience, supports Enchanting Templates
- **Blaze Forger** - Automated anvil for merging/upgrading enchantments with experience fluid
- **Mechanical Grindstone + Grindstone Drain** - Automated disenchanting with experience fluid output
- **Printer** - Copies enchanted books, written books, name tags, banner patterns, package addresses, and more
- **Disenchanter** - Strips enchantments from items and converts them to liquid experience
- **Liquid Experience** - Pumpable, storable experience fluid for automation pipelines
- **Experience Hatch** - Player XP deposit/withdrawal into fluid tanks
- **Experience Lantern** - Auto-absorbs nearby player XP into fluid, supports contraption movement
- **Super Experience Block / Nugget** - Upgraded experience storage obtained via lightning strike
- **Enchanting Templates** - Normal & super variants for guiding the Blaze Enchanter
- **Deployer XP Extension** - Kill drops, mending, and experience nugget conversion
- **Mechanical Arm Integration** - For both Enchanter and Forger
- **JEI Integration** - Recipe display for disenchanting and grinding

## Requirements

| Dependency | Version |
|------------|---------|
| Minecraft  | 1.20.1  |
| Forge      | 47.2.6+ |
| Create     | 6.0.8+  |

## Optional Compatibility

- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) - Recipe viewing for all machines
- [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) - Equipped item support

## Installation

1. Install Forge 1.20.1 and Create 6.0.8+
2. Place the CEI jar in your `mods/` folder
3. Launch the game

## License

This project is licensed under **LGPL-3.0-or-later**.

- Original code: Copyright (C) 2022 MarbleGateKeeper & LimonBlaze
- Ported to Forge 1.20.1 under the same license
- Original project: https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry

## Bug Fixes Over Upstream

This port includes fixes for several bugs present in the original version:

- Fixed FurnaceExpExtractor experience duplication exploit (drain condition inversion)
- Fixed OpenEndedPipeMixin PonderLevel double processing (missing return)
- Fixed MendingByDeployer broken XP calculation logic
- Fixed BlazeEnchanterEditPacket remote exploitation (no distance check)
- Fixed FluidTankBlockMixin null pointer on missing controller
- Fixed PrinterBlockEntity ink check logic inverted
- Fixed BlazeEnchanterBlockEntity bypassing tank notification via direct shrink
- Fixed Vec3.add() return value discarded
- Fixed Disenchanting NBT tag leakage on book conversion
- Removed empty shell Mixins (OpenEndedPipeInvoker, OpenEndFluidHandlerMixin)
