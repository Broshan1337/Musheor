The main developer of this goyim software uses the GPL-3.0 License, yet he obfuscates his releases.... code is also nowhere available so here we go!
I've decompiled and deobfuscated most stuff I could so you guys could understand what it does, keep it all open source mr musheck!

Requirement: be able to see and read.


# Musheor
> A Meteor Client addon for Minecraft 1.21+ focused on highway building automation, utility modules, and hunting tools. Supports multiple Minecraft versions (1.21.4, 1.21.5, 1.21.11).
## Modules
## Automation
### Better Highway Builder
Fully automated highway building for 2b2t nether highways. Handles paving, digging, scaffolding, restocking, eating, combat, and inventory management in one integrated module.
- **Build modes:** Pave or Dig
- **Highway types:** Cardinal or Diagonal, configurable width (3-9 blocks)
- **Scaffolding:** Simple or full scaffold with optional railings and ceiling fill
- **Restocking:** Ender chest farming, shulker restocking, tool restocking
- **Safety:** Auto-eat, auto-totem, auto-disconnect on material shortage, lava source removal
- **Integration:** KekNuker, Kill Aura, HotbarReplenish, InventoryCleaner, Discord RPC, Freelook
- **Lag handling:** Pauses operations when server lag is detected
### Printer
High-speed schematic block placement from Litematica or Baritone selections.
- **Selection types:** Litematica schematics or Baritone region selections
- **Auto-pathfinding:** Walks to out-of-range blocks using Baritone
- **Auto-restock:** Fetches blocks from configured containers (via RestockConfig), with multi-container fallback and batch restocking across all needed block types
- **Placement:** Handles directional blocks, slabs, stairs, trapdoors, and other stateful blocks
- **Filters:** Ignored block list, only-below-feet, only-air placement modes
### KekNuker
Selective block breaker with multiple operating modes.
- **Modes:** Highway mode (highway-specific blocks), Litematica mode (incorrect schematic blocks), or manual whitelist/blacklist
- **Shapes:** Cube or Sphere radius
- **Sorting:** Closest, topdown, or random mining order
- **Safety:** Packet kick prevention, configurable blocks-per-tick limit
### Ender Chest Farmer
Optimized ender chest mining for obsidian farming.
- Considers player inventory space before mining
- Optional super-farm mode (instant rebreak for ~20 blocks/second)
- Auto-disables at target amount
### Hotbar Replenish
Maintains specified items in each hotbar slot by restocking from inventory.
- Per-slot item configuration (slots 1-9 + offhand)
- Configurable restock threshold
- Highway builder integration mode
### Inventory Cleaner
Automatically disposes of unwanted items.
- Blacklist or whitelist filtering
- Drops empty shulker boxes
- Configurable drop delay with rotate-drop (tosses items behind you)
- One-click "blacklist current inventory" button
### Refill
Places a shulker box and restocks inventory from it.
- Configurable target item and slot count
- Option to place shulker below player (upside-down)
### Source Remover
Fills lava and water source blocks to remove them.
- Independent lava/water toggles
- Block whitelist/blacklist for fill material
- Visual rendering of detected sources
### Gather Item
Pathfinds to and collects nearby dropped items using Baritone.
- Configurable target item
- Optional Y-level filtering
### Ice Rail Builder
Semi-automated ice highway construction.
- Supports all four cardinal directions


## Features
### Air Place
Bypasses anti-cheat to place blocks in mid-air without support.
- Optional custom placement range
- Visual overlay for placement position
### Anti Cheat
Anti-cheat bypass utilities for 2b2t.
- Prevents server-forced screen closing
### Axis Viewer
Renders highway axes, ring roads, diamond highways, and grid highways in all dimensions.
- **Cardinals & Diagonals:** Main axis lines through origin
- **Ring Roads:** Square perimeter highways at configurable distances
- **Diamond Highways:** 45-degree highways connecting cardinal axes
- **Grid Highways:** Full grid pattern every 5000 blocks (from -50k to 50k)
- Per-dimension settings (Overworld, Nether, End) with configurable height and color
- Render range culling for performance
### Container Tweaks
Advanced container interaction with quick-move keybinds.
- Move all matching items between inventories
- Move all items at once
- Shulker-only filtering mode
### Depth Interact
Interact with containers through solid blocks via raytracing.
- Supports chests, barrels, shulker boxes, and ender chests
### KekMine
Grim-safe packet miner with queue system and double-break support.
- Auto-rebreak when blocks are replaced
- Silent swap (mines without holding pickaxe)
### Logout Goal
Auto-disconnects when reaching a target location.
- Per-dimension coordinate targeting
- Configurable arrival range
- Optional AutoReconnect disable
### Message Interact
Performs actions when receiving messages from specific players.
- Triggers left-click or right-click on message detection
### Portal Spawn ESP
Client-side scanner for valid nether portal spawn locations.
- Scans around a target position within configurable radius
- Renders valid portal positions
### Whisper
Sends randomized messages to players (for pearlbot TP requests).
- Configurable message length and target player
## Hunting
### Portal Skip Detection
Background thread scanner that detects portal skip patterns (AIR blocks in CAVE_AIR regions) in loaded chunks.
- Configurable scan radius in chunks
## HUD
### Packet Limits
Real-time display of inventory, global, and interaction packet rates with color-coded warnings.
### Stats Viewer
Comprehensive statistics overlay for highway building sessions.
- **Session:** Runtime, direction, distance traveled
- **Blocks:** Obsidian placed/mined, ender chests mined, netherrack mined
- **Performance:** Placements/sec, breaks/sec, distance/sec
- **Progress:** Distance to next section, percentage, ETA
- **Lifetime:** All-time cumulative statistics
## Commands
`| Command              | Description                                                                                            |`
`|----------------------|--------------------------------------------------------------------------------------------------------|`
`|.rc add <item>     | Enter config mode to register restock containers for an item (right-click containers to add, Esc to exit) |`
`|.rc list           | List all configured restock containers with distances                                                     |`
`|.rc remove <item>  | Remove all containers for an item                                                                         |`
`|.rc clear          | Clear all restock container configurations                                                                |`
`|.countItems <item> | Count items in inventory including inside shulker boxes                                                   |`
`|.find <item>       | Pathfind to and pick up a specific item using Baritone                                                    |`
`|.tp <player>       | Send a randomized TP request message                                                                      |`
## Global Settings
Accessible via the Musheor tab in Meteor Client's settings.
`|        Setting         | Description                        | Default  |`
`|:----------------------:|------------------------------------|:--------:|`
`|       Debug Mode       | Print debug messages to chat       |  false   |`
`|      Place Range       | Block placement range              |   4.5    |`
`|   Placement Timeout    | Ticks before retrying placement    |    5     |`
`|       Swap Delay       | Ticks to wait after item swap      |    3     |`
`|      Grim Bypass       | Enable grim anti-cheat bypass      |   true   |`
`|    Break Threshold     | Mining progress threshold          |   0.7    |`
`|      Double Break      | Enable double-break                |   true   |`
`|     Validate Break     | Wait for server break confirmation |   true   |`
`|     Break Timeout      | Ticks before re-attempting break   |    3     |`
`|  Global Packet Limit   | Max packets before kick            |   1350   |`
`| Inventory Packet Limit | Max inventory actions before kick  |    80    |`
`|  Placement Rendering   | Render block placement positions   |   true   |`
`|    Break Rendering     | Render block break positions       |   true   |`
`|      Render Type       | Static color or auto-mapped        |  Static  |`
`|      Render Shape      | Lines, Sides, or Both              |  Lines   |`
## Supported Versions
`| Minecraft Version |`
`|:-----------------:|`
`|      1.21.4       |`
`|      1.21.5       |`
`|      1.21.11      |`
## Dependencies
- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- [Baritone](https://github.com/cabaletta/baritone) (pathfinding & automation)
- [Litematica](https://github.com/maruohon/litematica) (schematic support)
- [MaLiLib](https://github.com/maruohon/malilib) (Litematica dependency)


