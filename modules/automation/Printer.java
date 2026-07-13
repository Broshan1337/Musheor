// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTotem;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.commands.RestockConfig;
import musheor.compat.LitematicaHelper;
import musheor.compat.VersionHelper;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "kek-printer" — places blocks from a Litematica schematic or fills a Baritone selection.
 * Scans nearby target positions, computes a per-block-state {@link PlacementStrategy}
 * (hit face + hit vector + required yaw/pitch, derived by simulating {@code getPlacementState}
 * for directional blocks via reflection), optionally auto-pathfinds to more work and
 * auto-restocks materials from shulkers (via {@link Refill}) or configured containers
 * (via {@link RestockConfig}). Rotations, layer limits, gravity-block safety and an
 * offhand-desync fix are all configurable.
 */
public class Printer extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();                       // was: FvaNWO
    private final SettingGroup sgPlacement = this.settings.createGroup("Placement");              // was: Q90GLXQ0Pef
    private final SettingGroup sgRotation = this.settings.createGroup("Rotation");                // was: psJq59YIbp3Z
    private final SettingGroup sgPathfinding = this.settings.createGroup("Pathfinding/Restocking"); // was: SOYyh5IPg26f7F
    private final SettingGroup sgRender = this.settings.createGroup("Render");                    // was: rKbT3Ifwo
    private final SettingGroup sgLitematica = this.settings.createGroup("Litematica");            // was: r7hOYIKN2
    private final MinecraftClient mc = MinecraftClient.getInstance();                             // was: oZHMlTL

    private final Long2ObjectMap<Map<BlockPos, BlockState>> chunkedTargets = new Long2ObjectOpenHashMap<>(); // was: xQr5FhbwpQPWgIQ (Baritone targets by chunk)
    private final List<Pair<BlockPos, BlockState>> placeQueue = new ArrayList<>();  // was: OMMZL1F3q (this tick's placeable candidates)
    private final List<Pair<BlockPos, BlockState>> renderQueue = new ArrayList<>(); // was: zu3a44xDeMFMCRwm
    private int tickCounter;              // was: krxNb5lcQuWA
    private int restockTimer = 0;         // was: nt0HZnvBBp
    private int openContainerTimer = 0;   // was: amz3UB1vE
    private int swapDelayTicks = 0;       // was: sBBIyQG5NWq0K
    private int stolenCount = 0;          // was: sZkZ1izAy
    private boolean pendingDesyncFix;     // was: QYKUhjp
    private int desyncTicks;              // was: NIz4xic3Js9
    private final Set<BlockPos> emptyContainers = new HashSet<>(); // was: u1WFwbQRSKa
    private Item restockingItem = null;   // was: LGDfbZq
    private final List<Item> restockQueue = new ArrayList<>();     // was: to3T8DJCDVX8po
    private boolean showCoordinates = false; // was: Sd3jEwKuGABy
    private int rotationTimer = 0;        // was: kJfFkD47Vh
    private PlacementStrategy pendingStrategy = null; // was: ubHptFBRn5bO
    private Map<Block, Integer> remainingMaterials = new HashMap<>(); // was: apOpfoOHr3fJVwT
    private int rescanTimer = 0;          // was: hq1pN0qY
    private int scanRing = -1;            // was: ptxWcpd1WV763T5
    private int scanIndex = 0;            // was: DnAk86nuI
    private BlockPos pathGoal = null;     // was: LlN8EpIZKbk
    private BlockPos bestScanCandidate = null; // was: pgjj9cLYUTE5g
    private boolean wasPlacing = false;   // was: IeStEJRJ9eb3l
    private boolean refillPending = false; // was: sFazojak6ig8QgGq
    private int debugTimer = 0;           // was: m9a8V6yT8klJSt44

    /** Block-state properties treated as "orientation" (require simulation to match). */ // was: ewq603nIlCd9Gbu
    private static final Set<Property<?>> ORIENTATION_PROPERTIES = new HashSet<>(Arrays.asList(
        Properties.FACING, Properties.HORIZONTAL_FACING, Properties.HALF, Properties.SLAB_TYPE, Properties.AXIS, Properties.HOPPER_FACING));
    private static final Direction[] YAW_DIRECTIONS = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST }; // was: ExGM8SQ9Qni
    private static final float[] PITCH_SAMPLES = { -89.0F, 0.0F, 89.0F };  // was: yS4isXf3gAzs
    private static final double[] HIT_Y_FRACTIONS = { 0.25, 0.75 };       // was: eC9HV2bWGX
    private final Map<BlockState, PlacementStrategy> strategyCache = new HashMap<>(); // was: w9spWeVv3AvI
    private static Method getPlacementStateMethod = null;                  // was: HvulV2j9tKjohNgh

    private final Setting<SelectionType> selectionType = sgGeneral.add(new EnumSetting.Builder<SelectionType>() // was: Qco5OF
        .name("selection-type").description("Get selection from litematica or baritone").defaultValue(SelectionType.LITEMATICA).build());
    private final Setting<Boolean> pauseWhenEating = sgGeneral.add(new BoolSetting.Builder() // was: cgqo7J5iR6
        .name("pause-when-eating").description("Pauses the printing process when the player eats (only when using meteor's auto-eat)").defaultValue(true).build());
    private final Setting<Boolean> pauseOnAura = sgGeneral.add(new BoolSetting.Builder() // was: u2kcN4vsQhS46w5s
        .name("pause-on-aura").description("Pauses the printing process when the player is killing mobs using killaura").defaultValue(true).build());
    private final Setting<Boolean> fixDesync = sgGeneral.add(new BoolSetting.Builder() // was: Eos3LxdhEJt
        .name("fix-desync").description("Attempts to fix your offhand / inventory desyncs from getting you stuck").defaultValue(false).build());

    private final Setting<LayerType> layerType = sgPlacement.add(new EnumSetting.Builder<LayerType>() // was: eB4Or3cBC2
        .name("layer-type").description("Choose how layering is handled when printing").defaultValue(LayerType.ALL).build());
    private final Setting<List<Block>> ignoredBlockList = sgPlacement.add(new BlockListSetting.Builder() // was: ITVesx8a
        .name("ignored-block-list").description("Avoids placing blocks configured in this list").defaultValue().build());
    private final Setting<Block> block = sgPlacement.add(new BlockSetting.Builder() // was: ymaK1v
        .name("block").description("What block to place (used for Baritone mode).").defaultValue(Blocks.OBSIDIAN)
        .visible(() -> selectionType.get() == SelectionType.BARITONE).build());
    private final Setting<Boolean> onlyAir = sgPlacement.add(new BoolSetting.Builder() // was: WRxnOUhRut1YD0z
        .name("only-air").description("Places blocks only when the desired location is an air block (meaning it doesn't replace non-solid blocks)").defaultValue(true).build());
    private final Setting<Boolean> gravityCheck = sgPlacement.add(new BoolSetting.Builder() // was: jusZpYdy95sR
        .name("gravity-check").description("Skip gravity-affected blocks (sand, gravel, etc.) if there is no solid block below them to prevent unwanted falling.").defaultValue(true).build());

    private final Setting<Boolean> ignoreRotations = sgRotation.add(new BoolSetting.Builder() // was: yNlQL5pBA2em
        .name("ignore-rotations").description("Fully ignores the rotation of the player and blocks when placing them").defaultValue(false).build());
    private final Setting<Boolean> forceRotate = sgRotation.add(new BoolSetting.Builder() // was: WUNsqX
        .name("force-rotate").description("Automatically rotate the camera to the correct direction when placing directional blocks.").defaultValue(false).build());
    private final Setting<Integer> rotationDelay = sgRotation.add(new IntSetting.Builder() // was: kmdOvDCSE7YfGRoG
        .name("rotation-delay").description("Ticks to wait after rotating before attempting placement (allows rotation to settle server-side).").defaultValue(2).min(0).sliderMax(10).visible(forceRotate::get).build());

    private final Setting<Boolean> autoPathfinding = sgPathfinding.add(new BoolSetting.Builder() // was: QW8HEHt74XOBmFi
        .name("auto-pathfinding").description("Basically baritone building").defaultValue(true).build());
    private final Setting<Integer> pathfindChunkRange = sgPathfinding.add(new IntSetting.Builder() // was: sgRet3EDuPy0
        .name("pathfind-chunk-range").description("The maximum distance in chunks to search for a block to pathfind to and place").defaultValue(8).visible(autoPathfinding::get).build());
    private final Setting<RestockType> autoRestock = sgPathfinding.add(new EnumSetting.Builder<RestockType>() // was: fFkn1eaeMzO
        .name("auto-restock").description("Automatically restock more materials from either configured containers or from shulkers you have on you").defaultValue(RestockType.NONE).visible(autoPathfinding::get).build());
    private final Setting<Boolean> playSound = sgPathfinding.add(new BoolSetting.Builder() // was: IBRKDF4jpTSEIY
        .name("play-sound").description("Plays a sound when its finished building or failed").defaultValue(true).visible(autoPathfinding::get).build());
    private final Setting<Boolean> debugMode = sgPathfinding.add(new BoolSetting.Builder() // was: waitO5uJ1bQzbe
        .name("debug-mode").description("Logs why each nearby candidate is rejected from placement, once per second").defaultValue(false).build());

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder() // was: vpx3Kb
        .name("render").description("Renders placement positions").defaultValue(true).build());
    private final Setting<Integer> renderRadius = sgRender.add(new IntSetting.Builder() // was: sK2VnB
        .name("render-radius").description("Only renders placeable blocks in a specified radius (lower if you experience FPS drops)").defaultValue(16).sliderMin(2).sliderMax(128).visible(MusheorSystem.Manager.placeRender::get).build());
    private final Setting<Integer> renderMaxResults = sgRender.add(new IntSetting.Builder() // was: FSlTD3
        .name("render-max-results").description("How many blocks are allowed to be rendered at once (increasing can cause lag)").defaultValue(2000).sliderRange(50, 10000).build());

    private final Setting<String> schematicName = sgLitematica.add(new StringSetting.Builder() // was: Ds6hqJEhQfd5P
        .name("schematic-name").description("The name of the schematic you want to place")
        .visible(() -> LitematicaHelper.isLoaded() && selectionType.get() == SelectionType.LITEMATICA).defaultValue("").build());
    private final Setting<BlockPos> schematicPos = sgLitematica.add(new BlockPosSetting.Builder() // was: AeWzUUcnnsf
        .name("schematic-pos").description("The position of the schematic you want to place")
        .visible(() -> LitematicaHelper.isLoaded() && selectionType.get() == SelectionType.LITEMATICA && this.showCoordinates).defaultValue(new BlockPos(0, 0, 0)).build());

    /** Reflectively invokes {@code Block#getPlacementState(ItemPlacementContext)} (name is obfuscated at runtime). */
    private static BlockState getPlacementState(Block block, ItemPlacementContext ctx) { // was: FvaNWO(Block,ItemPlacementContext)
        try {
            if (getPlacementStateMethod == null) {
                for (Method m : Block.class.getMethods()) {
                    if (m.getParameterCount() == 1 && BlockState.class.isAssignableFrom(m.getReturnType())
                        && m.getParameterTypes()[0].isAssignableFrom(ItemPlacementContext.class)) {
                        getPlacementStateMethod = m;
                        break;
                    }
                }
                if (getPlacementStateMethod == null) {
                    for (Class<?> clazz = Block.class; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                        for (Method m : clazz.getDeclaredMethods()) {
                            if (m.getParameterCount() == 1 && BlockState.class.isAssignableFrom(m.getReturnType())
                                && m.getParameterTypes()[0].isAssignableFrom(ItemPlacementContext.class)) {
                                m.setAccessible(true);
                                getPlacementStateMethod = m;
                                return getPlacementStateMethod != null ? (BlockState) getPlacementStateMethod.invoke(block, ctx) : null;
                            }
                        }
                    }
                }
            }
            return getPlacementStateMethod != null ? (BlockState) getPlacementStateMethod.invoke(block, ctx) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Printer() {
        super(musheor.AUTOMATION, "kek-printer", "Places blocks from schematics or fills baritone selections pretty damn fast");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton loadAndPlaceSchematic = list.add(theme.button("Load and place schematic")).widget();
        loadAndPlaceSchematic.action = () -> {
            if (this.mc.player == null || this.mc.world == null) return;
            if (!LitematicaHelper.isLoaded()) {
                this.error("Litematica is not installed");
                return;
            }
            String name = this.schematicName.get();
            BlockPos pos = this.schematicPos.get();
            if (name.isEmpty()) {
                this.error("No schematic name specified");
            } else if (LitematicaHelper.get().ensureSchematicAt(name, pos)) {
                this.info("Loaded schematic: §b%s", name);
            }
        };
        WButton clearPlacement = list.add(theme.button("Remove placement")).widget();
        clearPlacement.action = () -> { if (LitematicaHelper.isLoaded()) LitematicaHelper.get().clearAllPlacements(); };
        WButton toggleSettingVisibility = list.add(theme.button("Toggle BlockPos visibility")).widget();
        toggleSettingVisibility.action = () -> {
            this.showCoordinates = !this.showCoordinates;
            toggleSettingVisibility.set(this.showCoordinates ? "Hide Coordinates" : "Show Coordinates");
            this.schematicPos.onChanged();
        };
        return list;
    }

    /** Adds a Baritone-mode placement target, bucketed by chunk. */
    private void addTarget(BlockPos pos, BlockState targetState) { // was: FvaNWO(BlockPos,BlockState)
        long chunkKey = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
        this.chunkedTargets.computeIfAbsent(chunkKey, k -> new HashMap<>()).put(pos, targetState);
    }

    /** Fills a Baritone selection box with the configured block as placement targets. */
    private void addSelectionTargets(ISelection selection) { // was: FvaNWO(ISelection)
        if (selection == null || this.mc.world == null) return;
        BlockPos min = selection.min();
        BlockPos max = selection.max();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    boolean ok = this.onlyAir.get() ? this.mc.world.getBlockState(pos).getBlock() instanceof AirBlock : BlockUtils.canPlace(pos, false);
                    if (ok) this.addTarget(pos, this.block.get().getDefaultState());
                }
            }
        }
    }

    @Override
    public void onActivate() {
        this.chunkedTargets.clear();
        this.restockQueue.clear();
        HighwayState.getInstance().getBlockBreakAttempts().clear();
        this.tickCounter = 0;
        this.swapDelayTicks = 0;
        this.rotationTimer = 0;
        this.pendingStrategy = null;
        this.strategyCache.clear();
        this.stolenCount = 0;
        this.desyncTicks = 0;
        this.pendingDesyncFix = false;
        this.remainingMaterials = new HashMap<>();
        this.rescanTimer = 0;
        this.scanRing = -1;
        this.scanIndex = 0;
        this.pathGoal = null;
        this.bestScanCandidate = null;
        this.wasPlacing = false;
        this.refillPending = false;

        if (this.selectionType.get() == SelectionType.BARITONE) {
            IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            ISelectionManager selectionManager = baritone.getSelectionManager();
            if (selectionManager.getSelections() == null) {
                this.error("No Baritone selection found.");
                this.toggle();
                return;
            }
            for (ISelection selection : selectionManager.getSelections()) this.addSelectionTargets(selection);
        }

        if (this.selectionType.get() == SelectionType.LITEMATICA) {
            if (!LitematicaHelper.isLoaded()) {
                this.error("Litematica is not installed. Install Litematica or switch to Baritone mode.");
                this.toggle();
                return;
            }
            if (!LitematicaHelper.get().verifySchematic()) {
                this.error("Invalid or missing schematic");
                this.toggle();
                return;
            }
            this.remainingMaterials = LitematicaHelper.get().getRemainingMaterialCounts(this.ignoredBlockList.get(), this.onlyAir.get());
            int total = this.remainingMaterials.values().stream().mapToInt(Integer::intValue).sum();
            this.info("Schematic ready — §b" + total + "§r blocks remaining to place");
        }
    }

    @Override
    public void onDeactivate() {
        PlayerUtils.cancelPathing(); // musheor PlayerUtils (was CUfICea7s)
        this.renderQueue.clear();
        this.remainingMaterials.clear();
        this.rescanTimer = 0;
        this.scanRing = -1;
        this.scanIndex = 0;
        this.pathGoal = null;
        this.bestScanCandidate = null;
        this.wasPlacing = false;
        this.refillPending = false;
        this.restockTimer = 0;
        this.swapDelayTicks = 0;
        this.openContainerTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        this.tickCounter++;

        if (this.selectionType.get() == SelectionType.LITEMATICA && this.autoPathfinding.get() && ++this.rescanTimer >= 200) {
            this.rescanTimer = 0;
            this.refreshMaterials();
        }

        HighwayState.getInstance().getBlockBreakAttempts().entrySet()
            .removeIf(entry -> this.tickCounter - entry.getValue() > MusheorSystem.Manager.placementTimeout.get());

        if (this.pauseWhenEating.get() && ((AutoEat) Modules.get().get(AutoEat.class)).eating) return;
        if (this.pauseOnAura.get() && ((KillAura) Modules.get().get(KillAura.class)).attacking) return;

        // Offhand-desync fix: periodically re-sync inventory unless a totem is offhand-slotted.
        if (this.fixDesync.get()) {
            if (this.pendingDesyncFix) {
                this.pendingDesyncFix = false;
                VersionHelper.get().syncInventory();
            } else if (this.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
                && ((AutoTotem) Modules.get().get(AutoTotem.class)).isActive()) {
                this.desyncTicks++;
                if (this.desyncTicks >= 20) {
                    this.pendingDesyncFix = true;
                    this.desyncTicks = 0;
                }
            }
        }

        this.placeQueue.clear();
        if (this.selectionType.get() == SelectionType.LITEMATICA) {
            this.collectLitematicaCandidates(state);
        } else {
            this.collectBaritoneCandidates(state);
        }

        this.placeQueue.sort(Comparator.comparingDouble(p -> this.mc.player.squaredDistanceTo(
            p.first().getX(), p.first().getY(), p.first().getZ())));

        if (this.autoRestock.get() != RestockType.NONE && this.autoPathfinding.get()) {
            if (this.handleRestock(state)) return;
        }

        if (this.autoPathfinding.get()) {
            if (this.handlePathfinding(state)) return;
        }

        this.placeBlocks(state);
    }

    /** Populates {@link #placeQueue} (and the render queue) from the Litematica schematic near the player. */
    private void collectLitematicaCandidates(HighwayState state) {
        BlockPos playerPos = this.mc.player.getBlockPos();
        int pRange = (int) Math.ceil(MusheorSystem.Manager.placementRange.get()) + 1;
        Map<BlockPos, BlockState> placementCandidates = LitematicaHelper.get().getBlocksInBox(
            playerPos.add(-pRange, -pRange, -pRange), playerPos.add(pRange, pRange, pRange), this.onlyAir.get(), this.ignoredBlockList.get(), 750);

        boolean doDebug = this.debugMode.get() && ++this.debugTimer >= 20;
        if (doDebug) this.debugTimer = 0;
        if (doDebug && placementCandidates.isEmpty()) this.info("[DBG] No schematic candidates in box (pRange=%d)", pRange);

        BlockPos playerFeet = this.mc.player.getBlockPos();
        for (Map.Entry<BlockPos, BlockState> entry : placementCandidates.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState targetState = entry.getValue();
            String blockName = Registries.BLOCK.getId(targetState.getBlock()).getPath();
            if (InventoryManager.countItemInInventory(targetState.getBlock().asItem()) <= 0) {
                if (doDebug) this.info("[DBG] %s @ %s — skip: no items in inventory", blockName, pos.toShortString());
            } else if (!BlockUtils.canPlace(pos, true)) {
                if (doDebug) this.info("[DBG] %s @ %s — skip: BlockUtils.canPlace=false", blockName, pos.toShortString());
            } else if (this.layerType.get() == LayerType.BELOW_FEET && pos.getY() >= Math.floor(this.mc.player.getY())) {
                if (doDebug) this.info("[DBG] %s @ %s — skip: above feet layer", blockName, pos.toShortString());
            } else if (this.layerType.get() == LayerType.RENDER_LAYER && !LitematicaHelper.get().isPositionInRenderLayer(pos)) {
                if (doDebug) this.info("[DBG] %s @ %s — skip: not in render layer", blockName, pos.toShortString());
            } else if (state.getBlockBreakAttempts().containsKey(pos)) {
                if (doDebug) this.info("[DBG] %s @ %s — skip: recent attempt (tick %d, now %d)", blockName, pos.toShortString(), state.getBlockBreakAttempts().get(pos), this.tickCounter);
            } else if (!pos.equals(playerFeet) && !pos.equals(playerFeet.up())) {
                if (!PlayerUtils.isWithin(pos, MusheorSystem.Manager.placementRange.get())) {
                    if (doDebug) this.info("[DBG] %s @ %s — skip: not in placement range (dist=%.2f, range=%.1f)", blockName, pos.toShortString(),
                        Math.sqrt(this.mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)), MusheorSystem.Manager.placementRange.get());
                } else {
                    if (doDebug) this.info("[DBG] %s @ %s — ADDED to posAndBlocks", blockName, pos.toShortString());
                    this.placeQueue.add(Pair.of(pos, targetState));
                }
            } else if (doDebug) {
                this.info("[DBG] %s @ %s — skip: player standing here", blockName, pos.toShortString());
            }
        }

        if (this.render.get()) {
            int rRange = this.renderRadius.get();
            Map<BlockPos, BlockState> renderCandidates = LitematicaHelper.get().getBlocksInBox(
                playerPos.add(-rRange, -rRange, -rRange), playerPos.add(rRange, rRange, rRange), this.onlyAir.get(), this.ignoredBlockList.get(), this.renderMaxResults.get());
            this.renderQueue.clear();
            for (Map.Entry<BlockPos, BlockState> entry : renderCandidates.entrySet()) {
                if (InventoryManager.countItemInInventory(entry.getValue().getBlock().asItem()) > 0
                    && (this.layerType.get() != LayerType.BELOW_FEET || !(entry.getKey().getY() >= Math.floor(this.mc.player.getY())))
                    && (this.layerType.get() != LayerType.RENDER_LAYER || LitematicaHelper.get().isPositionInRenderLayer(entry.getKey()))
                    && WorldUtils.isWithinRange(entry.getKey(), rRange)) {
                    this.renderQueue.add(Pair.of(entry.getKey(), entry.getValue()));
                }
            }
        }
    }

    /** Populates {@link #placeQueue} (and the render queue) from the Baritone-mode chunked targets. */
    private void collectBaritoneCandidates(HighwayState state) {
        this.renderQueue.clear();
        ChunkPos playerChunk = this.mc.player.getChunkPos();
        int chunkRange = this.pathfindChunkRange.get();
        for (int cx = playerChunk.x - chunkRange; cx <= playerChunk.x + chunkRange; cx++) {
            for (int cz = playerChunk.z - chunkRange; cz <= playerChunk.z + chunkRange; cz++) {
                long key = ChunkPos.toLong(cx, cz);
                Map<BlockPos, BlockState> posMap = this.chunkedTargets.get(key);
                if (posMap == null || posMap.isEmpty()) continue;
                for (Map.Entry<BlockPos, BlockState> entry : posMap.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockState currentState = this.mc.world.getBlockState(pos);
                    BlockState targetState = entry.getValue();
                    if (InventoryManager.countItemInInventory(targetState.getBlock().asItem()) > 0
                        && BlockUtils.canPlace(pos, true)
                        && (this.layerType.get() != LayerType.BELOW_FEET || !(pos.getY() >= Math.floor(this.mc.player.getY())))
                        && (this.layerType.get() != LayerType.RENDER_LAYER || LitematicaHelper.isLoaded() && LitematicaHelper.get().isPositionInRenderLayer(pos))
                        && (currentState.getBlock() instanceof AirBlock || !this.onlyAir.get())) {
                        if (this.render.get() && WorldUtils.isWithinRange(pos, this.renderRadius.get())) {
                            this.renderQueue.add(Pair.of(pos, targetState));
                        }
                        if (!state.getBlockBreakAttempts().containsKey(pos) && WorldUtils.isWithinPlacementRange(pos)
                            && this.placeQueue.stream().noneMatch(p -> p.first().equals(pos))) {
                            this.placeQueue.add(Pair.of(pos, targetState));
                        }
                    }
                }
                if (posMap.isEmpty()) this.chunkedTargets.remove(key);
            }
        }
    }

    /**
     * Runs the restock state machine. Returns true if the caller should return early this tick.
     * Shulker restock defers to {@link Refill}; container restock walks to and empties configured containers.
     */
    private boolean handleRestock(HighwayState state) {
        Block neededBlock = this.pickNeededBlock();
        if (neededBlock == null) {
            boolean nothingLeft = this.selectionType.get() == SelectionType.LITEMATICA
                ? this.placeQueue.isEmpty() : this.placeQueue.isEmpty() && this.chunkedTargets.isEmpty();
            if (nothingLeft) {
                this.info("All blocks placed!");
                if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                this.toggle();
                return true;
            }
            if (this.selectionType.get() == SelectionType.BARITONE) {
                this.info("Cannot reach or find more blocks to place");
                if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                this.toggle();
                return true;
            }
        }

        if (InventoryManager.countItemInInventory(neededBlock.asItem()) <= 0 && this.restockQueue.isEmpty()) {
            if (this.selectionType.get() == SelectionType.LITEMATICA && this.remainingMaterials.getOrDefault(neededBlock, 0) == 0) {
                this.remainingMaterials.remove(neededBlock);
                return true;
            }
            this.buildRestockQueue(neededBlock);
        }

        if (this.autoRestock.get() == RestockType.SHULKERS) {
            return this.handleShulkerRestock();
        } else if (!this.restockQueue.isEmpty() && this.placeQueue.isEmpty()) {
            return this.handleContainerRestock(neededBlock);
        }
        return false;
    }

    /** Shulker restock path — sets Refill's item and toggles it, one item at a time. */
    private boolean handleShulkerRestock() {
        if (this.restockQueue.isEmpty()) return false;
        if (PathingHelper.isPathing()) PathingHelper.cancelEverything();
        Refill refill = (Refill) Modules.get().get(Refill.class);
        if (refill.isActive()) return true;

        if (this.refillPending) {
            this.refillPending = false;
            this.restockQueue.clear();
            this.restockTimer = 0;
            return true;
        }
        while (!this.restockQueue.isEmpty() && InventoryManager.countItemIncludingShulkers(this.restockQueue.getFirst()) <= 0) {
            this.restockQueue.removeFirst();
        }
        if (this.restockQueue.isEmpty()) {
            this.info("No shulkers found for needed blocks, disabling...");
            if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
            this.toggle();
            return true;
        }
        if (this.restockTimer <= InventoryManager.INSTANCE.delayAfterPlacingContainer.get()) {
            this.restockTimer++;
            return true;
        }
        refill.item.set(this.restockQueue.getFirst());
        refill.toggle();
        this.refillPending = true;
        return true;
    }

    /** Container restock path — walk to the nearest configured container for the item and shift-throw it out. */
    private boolean handleContainerRestock(Block neededBlock) {
        Item neededItem = this.restockQueue.getFirst();
        int amount = this.countRemaining(Block.getBlockFromItem(neededItem));
        if (this.restockingItem != neededItem) {
            this.emptyContainers.clear();
            this.restockingItem = neededItem;
            this.stolenCount = 0;
            this.info("Restocking %s", RestockConfig.getItemName(neededItem));
        }

        if (!RestockConfig.hasContainers(neededItem)) {
            if (neededItem == neededBlock.asItem()) {
                this.info("Cannot find container for %s, disabling...", RestockConfig.getItemName(neededItem));
                if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                this.restockQueue.clear();
                this.toggle();
                return true;
            }
            this.restockQueue.removeFirst();
            return true;
        }

        BlockPos container = this.findClosestContainer(neededItem);
        if (container == null) {
            if (neededItem == neededBlock.asItem()) {
                this.info("All containers for %s are empty, disabling...", RestockConfig.getItemName(neededItem));
                if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                this.emptyContainers.clear();
                this.restockQueue.clear();
                this.toggle();
                return true;
            }
            this.restockQueue.removeFirst();
            return true;
        }

        if (!WorldUtils.isWithinPlacementRange(container)) {
            if (!PathingHelper.isPathing()) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(container, 2));
            }
            return true;
        }

        PlayerUtils.cancelPathing();
        if (!InventoryManager.isContainerOpen() && this.openContainerTimer == 0) {
            WorldUtils.lookAtBlock(container);
            InventoryManager.openContainerAt(container);
            this.openContainerTimer = 3;
            return true;
        }
        if (this.openContainerTimer > 0) {
            this.openContainerTimer--;
            return true;
        }
        if (this.restockTimer <= InventoryManager.INSTANCE.delayAfterOpeningContainer.get()) {
            this.restockTimer++;
            return true;
        }
        if (!RateController.canSendInventoryPacket()) return true;

        ScreenHandler handler = this.mc.player.currentScreenHandler;
        int containerSize = handler.slots.size() - 36;
        for (int i = 0; i < containerSize; i++) {
            ItemStack currentStack = handler.getSlot(i).getStack();
            if (!currentStack.isEmpty()) {
                if (currentStack.getItem() == neededItem) {
                    this.stolenCount += currentStack.getCount();
                    this.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                }
                if (this.stolenCount >= amount || InventoryManager.countEmptyInventorySlots() <= 0 || !RateController.canSendInventoryPacket()) break;
            }
        }

        if (this.stolenCount == 0) {
            this.emptyContainers.add(container);
        } else if (this.stolenCount < amount && InventoryManager.countEmptyInventorySlots() > 0) {
            this.emptyContainers.add(container);
        } else if (InventoryManager.countEmptyInventorySlots() > 0) {
            this.emptyContainers.clear();
            this.restockQueue.removeFirst();
        }

        this.restockTimer = 0;
        this.openContainerTimer = 0;
        this.mc.player.closeScreen();
        if (this.restockQueue.isEmpty() || InventoryManager.countEmptyInventorySlots() <= 0) this.restockQueue.clear();
        return true;
    }

    /** Auto-pathfinding: rings-scan for more work and walk toward it. Returns true to return early. */
    private boolean handlePathfinding(HighwayState state) {
        if (!this.placeQueue.isEmpty()) {
            this.wasPlacing = true;
            return false;
        }

        if (this.wasPlacing) {
            this.scanRing = -1;
            this.scanIndex = 0;
            this.pathGoal = null;
            this.bestScanCandidate = null;
            this.wasPlacing = false;
        }

        if (this.selectionType.get() == SelectionType.LITEMATICA) {
            if (this.pathGoal != null) {
                double distToGoal = this.mc.player.squaredDistanceTo(this.pathGoal.getX() + 0.5, this.pathGoal.getY() + 0.5, this.pathGoal.getZ() + 0.5);
                if (this.debugMode.get()) this.info("[DBG-PF] goal=%s dist=%.1f pathing=%b radius=%d ringIdx=%d", this.pathGoal.toShortString(), Math.sqrt(distToGoal), PathingHelper.isPathing(), this.scanRing, this.scanIndex);
                if (!PathingHelper.isPathing()) {
                    if (distToGoal <= 16.0) {
                        if (this.debugMode.get()) this.info("[DBG-PF] arrived at goal, clearing and re-scanning");
                        this.pathGoal = null;
                        this.bestScanCandidate = null;
                        this.scanRing = -1;
                        this.scanIndex = 0;
                    } else {
                        PathingHelper.setGoal(new GoalNear(this.pathGoal, 2));
                    }
                }
                return true;
            }

            if (this.scanRing == -1) {
                Block priorityBlock = this.pickNeededBlock();
                if (priorityBlock != null && InventoryManager.countItemInInventory(priorityBlock.asItem()) > 0 && this.remainingMaterials.getOrDefault(priorityBlock, 0) == 0) {
                    this.remainingMaterials.remove(priorityBlock);
                    return true;
                }
                this.scanRing = 0;
                this.scanIndex = 0;
            }

            int maxChunkRadius = this.pathfindChunkRange.get();
            if (this.scanRing > maxChunkRadius) {
                this.refreshMaterials();
                Block exhaustedBlock = this.pickNeededBlock();
                if (exhaustedBlock != null && this.remainingMaterials.getOrDefault(exhaustedBlock, 0) == 0) {
                    this.remainingMaterials.remove(exhaustedBlock);
                    this.scanRing = -1;
                    this.scanIndex = 0;
                    this.pathGoal = null;
                    this.bestScanCandidate = null;
                    return true;
                }
                if (exhaustedBlock == null) {
                    this.info("All blocks placed!");
                    if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                    this.toggle();
                } else {
                    this.info("Cannot reach any blocks to place, disabling...");
                    if (this.playSound.get()) VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                    this.toggle();
                }
                return true;
            }

            ChunkPos playerChunk = this.mc.player.getChunkPos();
            int currentRingSize = this.scanRing == 0 ? 1 : 8 * this.scanRing;
            int chunksThisTick = Math.max(1, (currentRingSize + 3) / 4);
            for (int bi = 0; bi < chunksThisTick && this.pathGoal == null && this.scanRing <= maxChunkRadius; bi++) {
                ChunkPos toScan = this.getRingChunk(playerChunk, this.scanRing, this.scanIndex);
                BlockPos candidate = this.findClosestInChunk(toScan);
                if (candidate != null && (this.bestScanCandidate == null
                    || this.mc.player.squaredDistanceTo(candidate.getX() + 0.5, candidate.getY() + 0.5, candidate.getZ() + 0.5)
                       < this.mc.player.squaredDistanceTo(this.bestScanCandidate.getX() + 0.5, this.bestScanCandidate.getY() + 0.5, this.bestScanCandidate.getZ() + 0.5))) {
                    this.bestScanCandidate = candidate;
                }
                int ringSize = this.scanRing == 0 ? 1 : 8 * this.scanRing;
                this.scanIndex++;
                if (this.scanIndex >= ringSize) {
                    this.scanIndex = 0;
                    this.scanRing++;
                    if (this.bestScanCandidate != null) {
                        this.pathGoal = this.bestScanCandidate;
                        this.bestScanCandidate = null;
                        if (this.debugMode.get()) this.info("[DBG-PF] new goal from ring %d: %s (dist=%.1f)", this.scanRing - 1, this.pathGoal.toShortString(),
                            Math.sqrt(this.mc.player.squaredDistanceTo(this.pathGoal.getX() + 0.5, this.pathGoal.getY() + 0.5, this.pathGoal.getZ() + 0.5)));
                        PathingHelper.setGoal(new GoalNear(this.pathGoal, 2));
                        return true;
                    }
                    currentRingSize = this.scanRing == 0 ? 1 : 8 * this.scanRing;
                    chunksThisTick = Math.max(1, (currentRingSize + 3) / 4);
                }
            }
        } else if (!this.chunkedTargets.isEmpty()) {
            BlockPos closest = this.findClosestBaritoneTarget(this.chunkedTargets);
            if (closest != null) {
                if (WorldUtils.horizontalDistance(this.mc.player.getBlockPos(), closest) > 2.5) {
                    PathingHelper.setGoal(new GoalNear(closest, 2));
                    return true;
                }
                PlayerUtils.cancelPathing();
                if (this.mc.player.getBlockPos() == closest) PathingHelper.setGoal(new GoalBlock(closest.east()));
            }
        }
        return false;
    }

    /** Places blocks from {@link #placeQueue}, handling rotation timing, item selection and directional matching. */
    private void placeBlocks(HighwayState state) {
        if (this.swapDelayTicks > 0) {
            this.swapDelayTicks--;
            return;
        }
        if (this.placeQueue.isEmpty()) return;

        Pair<BlockPos, BlockState> firstPair = null;
        for (Pair<BlockPos, BlockState> candidate : this.placeQueue) {
            if (this.canPlaceSafely(candidate.first(), candidate.second())) {
                firstPair = candidate;
                break;
            }
        }

        if (this.debugMode.get()) {
            if (firstPair == null) this.info("[DBG] posAndBlocks has %d entries but all failed canPlaceSafely", this.placeQueue.size());
            else this.info("[DBG] firstPair=%s, swap=%d, rotationTimer=%d, hand=%s", firstPair.first().toShortString(), this.swapDelayTicks, this.rotationTimer, this.mc.player.getMainHandStack().getItem());
        }
        if (firstPair == null) return;

        BlockState firstState = firstPair.second();
        Block firstBlock = firstState.getBlock();
        PlacementStrategy firstStrat = this.getStrategy(firstState);

        if (this.rotationTimer > 0) {
            this.rotationTimer--;
            if (this.forceRotate.get() && this.pendingStrategy != null) {
                float y = this.pendingStrategy.yawRequired() != null ? this.yawForFacing(this.pendingStrategy.yawRequired()) : this.mc.player.getYaw();
                float p = this.pendingStrategy.pitchRequired() != null ? this.pendingStrategy.pitchRequired() : this.mc.player.getPitch();
                Rotations.rotate(y, p);
            }
            return;
        }

        PlacementStrategy appliedStrategy = this.pendingStrategy;
        this.pendingStrategy = null;
        boolean rotationApplied = appliedStrategy != null;
        boolean needsRotation = !this.ignoreRotations.get() && (firstStrat.yawRequired() != null || firstStrat.pitchRequired() != null);
        if (needsRotation) {
            if (this.forceRotate.get()) {
                if (!rotationApplied) {
                    float y = firstStrat.yawRequired() != null ? this.yawForFacing(firstStrat.yawRequired()) : this.mc.player.getYaw();
                    float p = firstStrat.pitchRequired() != null ? firstStrat.pitchRequired() : this.mc.player.getPitch();
                    Rotations.rotate(y, p);
                    this.pendingStrategy = firstStrat;
                    this.rotationTimer = this.rotationDelay.get();
                    if (this.debugMode.get()) this.info("[DBG] Waiting for rotation: yaw=%s pitch=%s", firstStrat.yawRequired(), firstStrat.pitchRequired());
                    return;
                }
            } else if (!this.isFacingCorrect(firstStrat)) {
                if (this.debugMode.get()) this.info("[DBG] Wrong facing for %s — waiting (yaw=%s pitch=%s)", Registries.BLOCK.getId(firstBlock).getPath(), firstStrat.yawRequired(), firstStrat.pitchRequired());
                this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
                return;
            }
        }

        if (this.mc.player.getMainHandStack().getItem() != firstBlock.asItem()) {
            if (this.debugMode.get()) this.info("[DBG] Wrong item in hand (%s), switching to %s", this.mc.player.getMainHandStack().getItem(), firstBlock.asItem());
            InventoryManager.selectItem(firstBlock.asItem());
            this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
            return;
        }

        WorldUtils.swapHands();
        for (Pair<BlockPos, BlockState> pair : this.placeQueue) {
            if (!BlockUtils.canPlace(pair.first(), true)) {
                if (this.debugMode.get()) this.info("[DBG] %s @ %s — inner skip: canPlace=false", Registries.BLOCK.getId(pair.second().getBlock()).getPath(), pair.first().toShortString());
            } else if (!PlayerUtils.isWithin(pair.first(), MusheorSystem.Manager.placementRange.get())) {
                if (this.debugMode.get()) this.info("[DBG] %s @ %s — inner skip: isWithin=false (dist=%.2f)", Registries.BLOCK.getId(pair.second().getBlock()).getPath(), pair.first().toShortString(),
                    Math.sqrt(this.mc.player.squaredDistanceTo(pair.first().getX() + 0.5, pair.first().getY() + 0.5, pair.first().getZ() + 0.5)));
            } else if (pair.second().getBlock() != firstBlock) {
                if (this.debugMode.get()) this.info("[DBG] %s @ %s — inner skip: wrong block type (want %s)", Registries.BLOCK.getId(pair.second().getBlock()).getPath(), pair.first().toShortString(), Registries.BLOCK.getId(firstBlock).getPath());
            } else if (!this.canPlaceSafely(pair.first(), pair.second())) {
                if (this.debugMode.get()) this.info("[DBG] %s @ %s — inner skip: canPlaceSafely=false", Registries.BLOCK.getId(firstBlock).getPath(), pair.first().toShortString());
            } else {
                PlacementStrategy pairStrat = this.getStrategy(pair.second());
                boolean pairNeedsRotation = !this.ignoreRotations.get() && (pairStrat.yawRequired() != null || pairStrat.pitchRequired() != null);
                if (pairNeedsRotation) {
                    boolean mismatch;
                    if (this.forceRotate.get() && rotationApplied) {
                        mismatch = pairStrat.yawRequired() != appliedStrategy.yawRequired() || !Objects.equals(pairStrat.pitchRequired(), appliedStrategy.pitchRequired());
                    } else {
                        mismatch = !this.isFacingCorrect(pairStrat);
                    }
                    if (mismatch) {
                        if (!this.forceRotate.get()) this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
                        break;
                    }
                }

                if (!RateController.canSendActionPacket()) {
                    if (this.debugMode.get()) this.info("[DBG] canInteract()=false, skipping placement this tick");
                    break;
                }

                Vec3d hitVec = new Vec3d(
                    pair.first().getX() + 0.5 + pairStrat.hitDir().getOffsetX() * 0.5,
                    pair.first().getY() + pairStrat.hitFracY(),
                    pair.first().getZ() + 0.5 + pairStrat.hitDir().getOffsetZ() * 0.5);
                BlockHitResult hit = new BlockHitResult(hitVec, pairStrat.hitDir(), pair.first(), false);
                WorldUtils.sendInteract(Hand.OFF_HAND, hit);
                if (pair.second().getBlock() == Blocks.OBSIDIAN) HighwayState.getInstance().incrementSessionObsidianPlaced();

                state.getBlockBreakAttempts().put(pair.first(), this.tickCounter);
                if (this.selectionType.get() == SelectionType.LITEMATICA) {
                    this.remainingMaterials.computeIfPresent(pair.second().getBlock(), (k, v) -> v <= 1 ? null : v - 1);
                }
            }
        }
        WorldUtils.swapHands();
    }

    /** Builds the restock queue: the primary block first, then other needed items ordered by remaining count. */
    private void buildRestockQueue(Block primaryBlock) { // was: FvaNWO(Block)
        this.restockQueue.clear();
        this.restockQueue.add(primaryBlock.asItem());
        Set<Item> seen = new HashSet<>();
        seen.add(primaryBlock.asItem());
        List<Map.Entry<Item, Integer>> others = new ArrayList<>();
        if (this.selectionType.get() == SelectionType.LITEMATICA) {
            for (Map.Entry<Block, Integer> entry : this.remainingMaterials.entrySet()) {
                if (entry.getValue() > 0) {
                    Item item = entry.getKey().asItem();
                    if (seen.add(item) && InventoryManager.countItemInInventory(item) <= 0 && RestockConfig.hasContainers(item)) {
                        others.add(Map.entry(item, entry.getValue()));
                    }
                }
            }
        } else {
            ObjectIterator<Map<BlockPos, BlockState>> it = this.chunkedTargets.values().iterator();
            while (it.hasNext()) {
                for (BlockState blockState : it.next().values()) {
                    Item item = blockState.getBlock().asItem();
                    if (seen.add(item) && InventoryManager.countItemInInventory(item) <= 0 && RestockConfig.hasContainers(item)) {
                        others.add(Map.entry(item, this.countRemaining(blockState.getBlock())));
                    }
                }
            }
        }
        others.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (Map.Entry<Item, Integer> entry : others) this.restockQueue.add(entry.getKey());
    }

    /** The closest configured container holding {@code item} that isn't marked empty. */
    private BlockPos findClosestContainer(Item item) { // was: FvaNWO(Item)
        List<BlockPos> containers = RestockConfig.getContainersFor(item);
        BlockPos playerPos = this.mc.player.getBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (BlockPos pos : containers) {
            if (!this.emptyContainers.contains(pos)) {
                double dist = playerPos.getSquaredDistance(pos);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = pos;
                }
            }
        }
        return closest;
    }

    /** How many of {@code block} remain to be placed (from the schematic count or the target map). */
    private int countRemaining(Block block) { // was: Q90GLXQ0Pef(Block)
        if (this.selectionType.get() == SelectionType.LITEMATICA) return this.remainingMaterials.getOrDefault(block, 0);
        int count = 0;
        ObjectIterator<Map<BlockPos, BlockState>> it = this.chunkedTargets.values().iterator();
        while (it.hasNext()) {
            for (BlockState blockState : it.next().values()) {
                if (blockState.getBlock() == block) count++;
            }
        }
        return count;
    }

    /** Picks the block to work on next: the alphabetically-first block that is in inventory, else first overall. */
    private Block pickNeededBlock() { // was: FvaNWO()
        Block firstInInventory = null;
        String firstInInventoryName = null;
        Block firstOverall = null;
        String firstOverallName = null;
        Iterable<Block> blockSource;
        if (this.selectionType.get() == SelectionType.LITEMATICA) {
            blockSource = this.remainingMaterials.keySet();
        } else {
            Set<Block> fromMap = new HashSet<>();
            ObjectIterator<Map<BlockPos, BlockState>> it = this.chunkedTargets.values().iterator();
            while (it.hasNext()) {
                for (BlockState s : it.next().values()) fromMap.add(s.getBlock());
            }
            blockSource = fromMap;
        }
        for (Block b : blockSource) {
            String name = Registries.BLOCK.getId(b).getPath();
            if (firstOverall == null || name.compareTo(firstOverallName) < 0) {
                firstOverall = b;
                firstOverallName = name;
            }
            if (InventoryManager.countItemInInventory(b.asItem()) > 0 && (firstInInventory == null || name.compareTo(firstInInventoryName) < 0)) {
                firstInInventory = b;
                firstInInventoryName = name;
            }
        }
        if (firstInInventory != null) return firstInInventory;
        if (firstOverall != null) return firstOverall;
        return this.selectionType.get() == SelectionType.BARITONE ? this.block.get() : null;
    }

    /** Re-reads the remaining material counts from the schematic. */
    private void refreshMaterials() { // was: Q90GLXQ0Pef()
        if (LitematicaHelper.isLoaded() && this.selectionType.get() == SelectionType.LITEMATICA) {
            Map<Block, Integer> fresh = LitematicaHelper.get().getRemainingMaterialCounts(this.ignoredBlockList.get(), this.onlyAir.get());
            fresh.entrySet().removeIf(e -> e.getValue() <= 0);
            this.remainingMaterials = fresh;
        }
    }

    private PlacementStrategy getStrategy(BlockState targetState) { // was: FvaNWO(BlockState)
        return this.strategyCache.computeIfAbsent(targetState, this::computeStrategy);
    }

    /**
     * Determines how to place {@code targetState}: the hit face + hit-vec Y fraction, and any required
     * yaw/pitch. For directional blocks it brute-forces facing/pitch combinations, simulating
     * {@code getPlacementState} to find one that yields the target orientation.
     */
    private PlacementStrategy computeStrategy(BlockState targetState) { // was: Q90GLXQ0Pef(BlockState)
        if (this.mc.player == null || this.mc.world == null) return new PlacementStrategy(Direction.DOWN, 0.5, null, null);
        boolean hasOrientation = ORIENTATION_PROPERTIES.stream().anyMatch(targetState::contains);
        if (!hasOrientation) return new PlacementStrategy(Direction.DOWN, 0.5, null, null);

        Item item = targetState.getBlock().asItem();
        if (item == Items.AIR) return new PlacementStrategy(Direction.DOWN, 0.5, null, null);

        ItemStack stack = new ItemStack(item);
        BlockPos simPos = this.mc.player.getBlockPos();
        float savedYaw = this.mc.player.getYaw();
        float savedPitch = this.mc.player.getPitch();
        try {
            // Pass 1: a hit face + Y fraction that matches for all sampled yaws/pitches (no rotation needed).
            for (Direction hitDir : Direction.values()) {
                double[] yFracs = hitDir.getAxis() == Direction.Axis.Y ? new double[]{hitDir == Direction.UP ? 1.0 : 0.0} : HIT_Y_FRACTIONS;
                for (double hitFracY : yFracs) {
                    Vec3d hitVec = new Vec3d(simPos.getX() + 0.5 + hitDir.getOffsetX() * 0.5, simPos.getY() + hitFracY, simPos.getZ() + 0.5 + hitDir.getOffsetZ() * 0.5);
                    BlockHitResult hit = new BlockHitResult(hitVec, hitDir, simPos, false);
                    boolean anyMatch = false;
                    boolean mismatchFound = false;
                    outer:
                    for (Direction yawDir : YAW_DIRECTIONS) {
                        this.mc.player.setYaw(this.yawForFacing(yawDir));
                        for (float pitch : PITCH_SAMPLES) {
                            this.mc.player.setPitch(pitch);
                            ItemPlacementContext ctx = new ItemPlacementContext(this.mc.world, this.mc.player, Hand.MAIN_HAND, stack, hit) {};
                            BlockState placed = getPlacementState(targetState.getBlock(), ctx);
                            if (placed != null) {
                                if (!this.statesMatch(placed, targetState)) {
                                    mismatchFound = true;
                                    break outer;
                                }
                                anyMatch = true;
                            }
                        }
                    }
                    if (anyMatch && !mismatchFound) return new PlacementStrategy(hitDir, hitFracY, null, null);
                }
            }

            // Pass 2: find any matching combination and record which of yaw/pitch actually matter.
            for (Direction hitDir : Direction.values()) {
                double[] yFracs = hitDir.getAxis() == Direction.Axis.Y ? new double[]{hitDir == Direction.UP ? 1.0 : 0.0} : HIT_Y_FRACTIONS;
                for (double hitFracY : yFracs) {
                    Vec3d hitVec = new Vec3d(simPos.getX() + 0.5 + hitDir.getOffsetX() * 0.5, simPos.getY() + hitFracY, simPos.getZ() + 0.5 + hitDir.getOffsetZ() * 0.5);
                    BlockHitResult hit = new BlockHitResult(hitVec, hitDir, simPos, false);
                    for (Direction yawDir : YAW_DIRECTIONS) {
                        this.mc.player.setYaw(this.yawForFacing(yawDir));
                        for (float pitch : PITCH_SAMPLES) {
                            this.mc.player.setPitch(pitch);
                            ItemPlacementContext ctx = new ItemPlacementContext(this.mc.world, this.mc.player, Hand.MAIN_HAND, stack, hit) {};
                            BlockState placed = getPlacementState(targetState.getBlock(), ctx);
                            if (placed != null && this.statesMatch(placed, targetState)) {
                                boolean needsH = targetState.contains(Properties.HORIZONTAL_FACING)
                                    || targetState.contains(Properties.FACING) && targetState.get(Properties.FACING).getAxis().isHorizontal();
                                boolean pitchMatters = false;
                                for (float otherPitch : PITCH_SAMPLES) {
                                    if (otherPitch != pitch) {
                                        this.mc.player.setPitch(otherPitch);
                                        ItemPlacementContext ctx2 = new ItemPlacementContext(this.mc.world, this.mc.player, Hand.MAIN_HAND, stack, hit) {};
                                        BlockState placed2 = getPlacementState(targetState.getBlock(), ctx2);
                                        if (placed2 == null || !this.statesMatch(placed2, targetState)) {
                                            pitchMatters = true;
                                            break;
                                        }
                                    }
                                }
                                this.mc.player.setPitch(pitch);
                                Direction reqYaw = needsH ? yawDir : null;
                                Float reqPitch = pitchMatters ? pitch : null;
                                return new PlacementStrategy(hitDir, hitFracY, reqYaw, reqPitch);
                            }
                        }
                    }
                }
            }
        } finally {
            this.mc.player.setYaw(savedYaw);
            this.mc.player.setPitch(savedPitch);
        }
        return new PlacementStrategy(Direction.DOWN, 0.5, null, null);
    }

    /** True if {@code placed} matches {@code target} on block and all orientation properties. */
    private boolean statesMatch(BlockState placed, BlockState target) { // was: FvaNWO(BlockState,BlockState)
        if (placed.getBlock() != target.getBlock()) return false;
        for (Property<?> prop : ORIENTATION_PROPERTIES) {
            if (placed.contains(prop) && target.contains(prop) && !placed.get(prop).equals(target.get(prop))) return false;
        }
        return true;
    }

    /** True if the player's current facing/pitch satisfies the strategy's requirements. */
    private boolean isFacingCorrect(PlacementStrategy strat) { // was: FvaNWO(PlacementStrategy)
        if (this.mc.player == null) return false;
        if (strat.yawRequired() != null && this.mc.player.getHorizontalFacing() != strat.yawRequired()) return false; // method_58149
        if (strat.pitchRequired() != null) {
            float actual = this.mc.player.getPitch();
            float required = strat.pitchRequired();
            boolean reqUp = required < -45.0F;
            boolean reqDown = required > 45.0F;
            boolean isUp = actual < -45.0F;
            boolean isDown = actual > 45.0F;
            if (reqUp && !isUp) return false;
            if (reqDown && !isDown) return false;
            if (!reqUp && !reqDown && (isUp || isDown)) return false;
        }
        return true;
    }

    /** The player yaw that faces {@code facing} (horizontal), or the current yaw. */
    private float yawForFacing(Direction facing) { // was: FvaNWO(Direction)
        return switch (facing) {
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> -90.0F;
            default -> this.mc.player.getYaw();
        };
    }

    /** Safety check for gravity/attachment-dependent blocks: don't place them without proper support. */
    private boolean canPlaceSafely(BlockPos pos, BlockState targetState) { // was: Q90GLXQ0Pef(BlockPos,BlockState)
        if (targetState.getBlock() instanceof LanternBlock) {
            boolean hanging = targetState.get(Properties.HANGING);
            BlockPos attach = hanging ? pos.up() : pos.down();
            return !this.mc.world.getBlockState(attach).isAir();
        } else if (targetState.getBlock() instanceof RedstoneWireBlock) {
            return !this.mc.world.getBlockState(pos.down()).isAir();
        } else {
            return !(this.gravityCheck.get() && targetState.getBlock() instanceof FallingBlock) || !this.mc.world.getBlockState(pos.down()).isAir();
        }
    }

    /** Returns the {@code index}-th chunk on the square ring of the given {@code radius} around {@code center}. */
    private ChunkPos getRingChunk(ChunkPos center, int radius, int index) { // was: FvaNWO(ChunkPos,int,int)
        if (radius == 0) return center;
        int cx = center.x;
        int cz = center.z;
        if (index < 2 * radius + 1) return new ChunkPos(cx - radius + index, cz - radius);
        index -= 2 * radius + 1;
        if (index < 2 * radius) return new ChunkPos(cx + radius, cz - radius + 1 + index);
        index -= 2 * radius;
        if (index < 2 * radius) return new ChunkPos(cx + radius - 1 - index, cz + radius);
        index -= 2 * radius;
        return new ChunkPos(cx - radius, cz + radius - 1 - index);
    }

    /** The closest placeable schematic block within a loaded chunk, or null. */
    private BlockPos findClosestInChunk(ChunkPos chunk) { // was: FvaNWO(ChunkPos)
        if (this.mc.world == null || this.mc.player == null) return null;
        if (!this.mc.world.isChunkLoaded(chunk.x, chunk.z)) return null;
        int minX = chunk.getStartX();
        int minZ = chunk.getStartZ();
        int maxX = chunk.getEndX();
        int maxZ = chunk.getEndZ();
        int minY = this.mc.world.getBottomY();
        int maxY = this.mc.world.getBottomY() + this.mc.world.getHeight() - 1;
        Map<BlockPos, BlockState> candidates = LitematicaHelper.get().getBlocksInBox(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), this.onlyAir.get(), this.ignoredBlockList.get(), 512);
        if (candidates.isEmpty()) return null;

        BlockPos playerFeet = this.mc.player.getBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Map.Entry<BlockPos, BlockState> entry : candidates.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState targetState = entry.getValue();
            if (InventoryManager.countItemInInventory(targetState.getBlock().asItem()) > 0
                && (this.layerType.get() != LayerType.BELOW_FEET || !(pos.getY() >= Math.floor(this.mc.player.getY())))
                && (this.layerType.get() != LayerType.RENDER_LAYER || LitematicaHelper.get().isPositionInRenderLayer(pos))
                && !pos.equals(playerFeet) && !pos.equals(playerFeet.up())) {
                double dist = this.mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = pos;
                }
            }
        }
        return closest;
    }

    /** The closest placeable Baritone-mode target across all chunks (chunks nearest-first). */
    private BlockPos findClosestBaritoneTarget(Long2ObjectMap<Map<BlockPos, BlockState>> chunkPositions) { // was: FvaNWO(Long2ObjectMap)
        if (this.mc.player == null || this.mc.world == null) return null;
        double px = this.mc.player.getX();
        double py = this.mc.player.getY();
        double pz = this.mc.player.getZ();
        List<Map.Entry<Long, Map<BlockPos, BlockState>>> chunks = new ArrayList<>();
        ObjectIterator<Long2ObjectMap.Entry<Map<BlockPos, BlockState>>> it = chunkPositions.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<Map<BlockPos, BlockState>> entry = it.next();
            chunks.add(Map.entry(entry.getLongKey(), entry.getValue()));
        }
        chunks.sort(Comparator.comparingDouble(e -> {
            int cx = ChunkPos.getPackedX(e.getKey());
            int cz = ChunkPos.getPackedZ(e.getKey());
            double centerX = (cx << 4) + 8.0;
            double centerZ = (cz << 4) + 8.0;
            return (centerX - px) * (centerX - px) + (centerZ - pz) * (centerZ - pz);
        }));

        for (Map.Entry<Long, Map<BlockPos, BlockState>> chunk : chunks) {
            Map<BlockPos, BlockState> posMap = chunk.getValue();
            if (posMap == null || posMap.isEmpty()) continue;
            BlockPos bestPos = null;
            double bestDist = Double.MAX_VALUE;
            for (Map.Entry<BlockPos, BlockState> entry : posMap.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState targetState = entry.getValue();
                if (this.mc.world.getBlockState(pos).getBlock() != targetState.getBlock()
                    && this.mc.world.getBlockState(pos).getBlock() instanceof AirBlock
                    && BlockUtils.canPlaceBlock(pos, true, targetState.getBlock())
                    && InventoryManager.countItemInInventory(targetState.getBlock().asItem()) > 0) {
                    double dx = pos.getX() + 0.5 - px;
                    double dy = pos.getY() + 0.5 - py;
                    double dz = pos.getZ() + 0.5 - pz;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < bestDist) {
                        bestDist = distSq;
                        bestPos = pos;
                    }
                }
            }
            if (bestPos != null) return bestPos;
        }
        return null;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.mc.player == null || this.mc.world == null) return;
        if (MusheorSystem.Manager.placeRender.get() && this.render.get() && !this.renderQueue.isEmpty()) {
            List<Pair<BlockPos, Block>> renderBlocks = new ArrayList<>();
            for (Pair<BlockPos, BlockState> pair : this.renderQueue) {
                renderBlocks.add(Pair.of(pair.first(), pair.second().getBlock()));
            }
            musheor.utils.RenderUtils.render(event, renderBlocks);
        }
    }

    /** Layer restriction. */ // was: enum LayerType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum LayerType { ALL, BELOW_FEET, RENDER_LAYER }

    /** How to place a block: which face to hit, the hit-vector Y fraction, and any required yaw/pitch. */
    private record PlacementStrategy(Direction hitDir, double hitFracY, Direction yawRequired, Float pitchRequired) { } // was: FvaNWO/Q90GLXQ0Pef/psJq59YIbp3Z/SOYyh5IPg26f7F

    /** Auto-restock source. */ // was: enum RestockType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum RestockType { NONE, CONTAINERS, SHULKERS }

    /** Where placement targets come from. */ // was: enum SelectionType {FvaNWO, Q90GLXQ0Pef}
    private enum SelectionType { BARITONE, LITEMATICA }
}
