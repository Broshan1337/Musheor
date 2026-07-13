# Deobfuscation map — musheor-1.6.1

Raw Vineflower output: `../decompiled-v2/`
Cleaned/deobfuscated source: this folder (`clean-v2/`)

In 1.6.1 the addon's commands and core utils were moved into a flat, class-name-obfuscated
`obf` package. This map records the recovered identity and target location of each `obf` class.
Members (methods/fields/locals) are renamed per-file during deobfuscation.

## obf/ → clean-v2 location

| obf class | Clean class | Target path |
|---|---|---|
| `aY0a71o` | `Execute` | `musheor/commands/Execute.java` |
| `D0Jn` | `FindItem` | `musheor/commands/FindItem.java` |
| `FDb5` | `CountItems` | `musheor/commands/CountItems.java` |
| `SDNZCBI` | `FolderCommand` | `musheor/commands/FolderCommand.java` |
| `w4hkT` | `XaeroUtilsCommand` | `musheor/commands/XaeroUtilsCommand.java` |
| `zQn6` | `PearlStoreCommand` | `musheor/commands/PearlStoreCommand.java` |
| `VGn8YrSOy` | `KitCommand` | `musheor/commands/KitCommand.java` |
| `UkMm7uisd` | `RestockConfig` | `musheor/commands/RestockConfig.java` |
| `JUKhlfIQiiGS` | `WhisperCommand` | `musheor/commands/WhisperCommand.java` |
| `e1lTf` | `InventoryManager` module (registered `"inventory-manager"`) | `musheor/modules/automation/InventoryManager.java` |
| `HFbqnT1FEh2q` | `WorldUtils` (+ `Direction8` enum) | `musheor/utils/WorldUtils.java` |
| `MFFPIcv139M` | `HighwayState` | `musheor/utils/internal/HighwayState.java` |
| `TKzj7u` | `BlockPositions` | `musheor/utils/BlockPositions.java` |
| `J0sjSCk` | `HighwayLocator` | `musheor/utils/internal/HighwayLocator.java` |
| `GZpL` | `Handlers` | `musheor/utils/Handlers.java` |
| `NKyC2E` | `PathingHelper` | `musheor/utils/internal/PathingHelper.java` |
| `Mj77A` | `PearlStore` | `musheor/utils/PearlStore.java` |
| `XfFrUB` | `DiscordRPC` | `musheor/utils/DiscordRPC.java` |
| `fVHOZ` | `StatsHandler` (stats math + number abbrev) | `musheor/utils/StatsHandler.java` |
| `x3lfFe8H5f` | `RenderUtils` | `musheor/utils/RenderUtils.java` |
| `CUfICea7s` | `PlayerUtils` | `musheor/utils/PlayerUtils.java` |
| `EzpHtsX2O` | `TagUtils` | `musheor/utils/TagUtils.java` |
| `QLktfq` | `PlacementEngine` | `musheor/utils/internal/PlacementEngine.java` |
| `SyqMxK` | `RateController` | `musheor/utils/internal/RateController.java` |
| `ttI5` | `StatsCollector` (reads MC stats → HighwayState) | `musheor/utils/StatsCollector.java` |

## Common obfuscated member names (context-dependent — same token reused across unrelated classes)
- `FvaNWO`, `Q90GLXQ0Pef`, `psJq59YIbp3Z`, `oZHMlTL`, `rKbT3Ifwo`, `SOYyh5IPg26f7F` … —
  these are per-class member slots; each is renamed from its behavior in-file.

## HighwayBuilder (musheor/modules/automation/HighwayBuilder.java) member map
Setting fields (recovered from their `.name()` values):
| obf field | setting name / clean name | type |
|---|---|---|
| sZkZ1izAy | buildMode ("build-mode") | BuildMode |
| FvaNWO | mode ("mode") | Mode |
| QYKUhjp | highwayType ("highway-type") | HighwayType |
| NIz4xic3Js9 | pavementWidth ("pavement-width") | int |
| u1WFwbQRSKa | autoBounce | bool |
| LGDfbZq | bounceDistanceCheck | int |
| to3T8DJCDVX8po | pavementBlock ("pavement-block") | Block |
| Sd3jEwKuGABy | scaffoldMode | ScaffoldMode |
| kJfFkD47Vh | scaffoldBlock | Block |
| ubHptFBRn5bO | scaffoldLeftRail | bool |
| apOpfoOHr3fJVwT | scaffoldRightRail | bool |
| hq1pN0qY | placeRails | bool |
| ptxWcpd1WV763T5 | placeLeftRail | bool |
| DnAk86nuI | placeRightRail | bool |
| LlN8EpIZKbk | replaceCryingObsidian | bool |
| pgjj9cLYUTE5g | fillCeiling | bool |
| IeStEJRJ9eb3l | echestFarming | bool |
| sFazojak6ig8QgGq | enableItemRestocking | bool |
| ewq603nIlCd9Gbu | swapBrokenPickaxes | bool |
| ExGM8SQ9Qni | shulkerRestocking | bool |
| yS4isXf3gAzs | echestShulkerRestocking | bool |
| eC9HV2bWGX | toolShulkerRestocking | bool |
| w9spWeVv3AvI | toggleAutoEat | bool |
| HvulV2j9tKjohNgh | toggleAutoGap | bool |
| Q90GLXQ0Pef | toggleKekNuker | bool |
| Qco5OF | mineAboveRails | bool |
| cgqo7J5iR6 | toggleKillAura | bool |
| u2kcN4vsQhS46w5s | disconnectIfNoMaterials | bool |
| Eos3LxdhEJt | toggleSourceFiller | bool |
| eB4Or3cBC2 | advancedSourceFiller | bool |
| ITVesx8a | toggleAutoReplenish | bool |
| ymaK1v | toggleInventoryCleaner | bool |
| WRxnOUhRut1YD0z | discordRpc | bool |
| jusZpYdy95sR | enableFreeLook | bool |
| psJq59YIbp3Z (static) | INSTANCE | HighwayBuilder |

Enums: BuildMode{FvaNWO=PAVE(THREE), Q90GLXQ0Pef=DIG(FOUR)}, Mode{FvaNWO=?, Q90GLXQ0Pef=BARITONE, psJq59YIbp3Z=?}, HighwayType{FvaNWO=CARDINAL, Q90GLXQ0Pef=DIAGONAL}, ScaffoldMode.
Accessors (used by Handlers/WorldUtils/BlockPositions): getWidth()=zu3a44xDeMFMCRwm, getDirection()=rKbT3Ifwo, getBuildMode()=oZHMlTL, placeRails()=sBBIyQG5NWq0K, getFillBlock()=amz3UB1vE, getScaffoldBlock()=NIz4xic3Js9, hasCeiling()=kJfFkD47Vh, hasWalls()=krxNb5lcQuWA, hasLeftRail()=sZkZ1izAy(), hasRightRail()=QYKUhjp().
