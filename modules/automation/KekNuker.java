// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.LitematicaHelper;
import musheor.modules.features.KekMine;
import musheor.modules.hud.HudInfoPlus;
import musheor.utils.BlockPositions;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayLocator;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * "kek-nuker" — mines every eligible block around the player. Supports four target
 * sources ({@link NukerMode}): a radius scan, a queue driven externally by
 * {@link HighwayBuilder} ({@code SMART}), the wrong blocks of a loaded Litematica
 * schematic, or a Baritone selection. Breaking is delegated to {@link KekMine} when
 * that module is active (packet mining), otherwise to {@link BlockUtils#breakBlock}.
 * Optionally auto-pathfinds (via Baritone) to the nearest breakable block when nothing
 * is in range.
 */
public class KekNuker extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    // was: xQr5FhbwpQPWgIQ
    private final SettingGroup sgPathfind = this.settings.createGroup("Pathfind"); // was: OMMZL1F3q
    private final SettingGroup sgRender = this.settings.createGroup("Render");  // was: zu3a44xDeMFMCRwm
    private static final MinecraftClient mc = MinecraftClient.getInstance();   // was: krxNb5lcQuWA
    public static KekNuker INSTANCE;                                            // was: FvaNWO (static)

    public final Setting<NukerMode> nukerMode = sgGeneral.add(new EnumSetting.Builder<NukerMode>() // was: Q90GLXQ0Pef
        .name("nuker-mode").defaultValue(NukerMode.Normal).build());
    private final Setting<LayerType> layerType = sgGeneral.add(new EnumSetting.Builder<LayerType>() // was: nt0HZnvBBp
        .name("layer-type").description("Controls where blocks are allowed to be broken (layer handling)")
        .defaultValue(LayerType.AboveFeet).visible(() -> nukerMode.get() != NukerMode.Smart).build());
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>() // was: amz3UB1vE
        .name("sort-mode").description("Choose what order to mine the blocks in").defaultValue(SortMode.Closest).build());
    public final Setting<Shape> shape = sgGeneral.add(new EnumSetting.Builder<Shape>() // was: psJq59YIbp3Z
        .name("shape").defaultValue(Shape.Sphere).visible(() -> nukerMode.get() != NukerMode.Smart).build());
    public final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder() // was: SOYyh5IPg26f7F
        .name("range").defaultValue(5.5).min(2.0).max(8.0).decimalPlaces(1).build());
    private final Setting<Boolean> belowFeetLast = sgGeneral.add(new BoolSetting.Builder() // was: sBBIyQG5NWq0K
        .name("below-feet-last").description("Makes it so the block you are standing on is only mined when all other blocks in range are broken")
        .defaultValue(false).visible(() -> nukerMode.get() != NukerMode.Smart).build());
    private final Setting<Boolean> ignoreAir = sgGeneral.add(new BoolSetting.Builder() // was: sZkZ1izAy
        .name("ignore-air").description("Ignores air blocks in the schematic.")
        .visible(() -> LitematicaHelper.isLoaded() && nukerMode.get() == NukerMode.Litematica).defaultValue(false).build());
    private final Setting<Boolean> avoidSpillingLiquids = sgGeneral.add(new BoolSetting.Builder() // was: QYKUhjp
        .name("avoid-spilling-liquids").description("Prevents you from mining blocks that will cause liquid to flow everywhere")
        .defaultValue(true).build());
    private final Setting<Boolean> preventPacketKick = sgGeneral.add(new BoolSetting.Builder() // was: NIz4xic3Js9
        .name("prevent-packet-kick").description("+100000 aura setting").defaultValue(true).build());
    public final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("blocks-per-tick").description("Maximum blocks to try to break per tick. (Only for instant-breakable-blocks)")
        .defaultValue(35).min(1).sliderRange(1, 100).build());
    private final Setting<ListMode> listMode = sgGeneral.add(new EnumSetting.Builder<ListMode>() // was: u1WFwbQRSKa
        .name("list-mode").description("Selection mode").defaultValue(ListMode.None).build());
    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder() // was: LGDfbZq
        .name("whitelist").description("The blocks you want to mine.").defaultValue()
        .visible(() -> listMode.get() == ListMode.Whitelist).build());
    private final Setting<List<Block>> blacklist = sgGeneral.add(new BlockListSetting.Builder() // was: to3T8DJCDVX8po
        .name("blacklist").description("The blocks you don't want to mine.").defaultValue()
        .visible(() -> listMode.get() == ListMode.Blacklist).build());

    private final Setting<Boolean> autoPathfind = sgPathfind.add(new BoolSetting.Builder() // was: Sd3jEwKuGABy
        .name("auto-pathfind").description("Automatically pathfinds when no blocks are in range")
        .defaultValue(false).visible(() -> nukerMode.get() != NukerMode.Smart).build());
    private final Setting<Integer> pathfindChunkRange = sgPathfind.add(new IntSetting.Builder() // was: kJfFkD47Vh
        .name("pathfind-chunk-range").description("Maximum range in chunks the pathfinder can scan for more blocks to break")
        .defaultValue(4).sliderMax(8).visible(() -> nukerMode.get() != NukerMode.Smart).build());
    private final Setting<Integer> chunksPerTick = sgPathfind.add(new IntSetting.Builder() // was: ubHptFBRn5bO
        .name("chunks-per-tick").description("How many chunks to scan per tick when searching for more blocks. Higher values find targets faster at the cost of CPU per tick.")
        .defaultValue(2).min(1).sliderMax(8).visible(() -> nukerMode.get() != NukerMode.Smart).build());

    private final Setting<Boolean> globalRendering = sgRender.add(new BoolSetting.Builder() // was: apOpfoOHr3fJVwT
        .name("global-rendering").defaultValue(true).description("Synchronize rendering with Musheor-Tab").build());
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder() // was: hq1pN0qY
        .name("color").defaultValue(new SettingColor(Color.cyan)).description("Custom color for rendering (lines / wireframe)")
        .visible(() -> !globalRendering.get()).build());

    /** Extra positions pushed in by {@link HighwayBuilder}/Handlers for SMART mode to break. */
    public static List<BlockPos> extraBreakQueue = new ArrayList<>();          // was: r7hOYIKN2 (public static)
    /** Filtered, sorted positions actually mined and rendered this tick. */
    private static List<BlockPos> mineTargets = new ArrayList<>();             // was: ptxWcpd1WV763T5 (private static)
    /** pos -> tick it was last dispatched, used to throttle rebreak attempts. */
    public static final Map<BlockPos, Integer> breakTimeouts = new HashMap<>(); // was: oZHMlTL (public static final)

    private int tickCounter;                 // was: DnAk86nuI
    private int scanRing = -1;               // was: LlN8EpIZKbk  (current ring radius in the spiral chunk scan)
    private int scanIndex = 0;               // was: pgjj9cLYUTE5g (index within the current ring)
    private BlockPos pathTarget = null;      // was: IeStEJRJ9eb3l (block we are pathing toward)
    private BlockPos bestScanCandidate = null; // was: sFazojak6ig8QgGq (closest candidate found so far while scanning)
    private boolean wasMining = false;       // was: ewq603nIlCd9Gbu

    public KekNuker() {
        super(musheor.AUTOMATION, "kek-nuker", "Mines blocks in radius of the player");
        INSTANCE = this;
    }

    /** The configured block blacklist (used by Handlers to avoid breaking protected blocks). */
    public static List<Block> getBlacklist() { // was: FvaNWO()
        return INSTANCE.blacklist.get();
    }

    @Override
    public void onActivate() {
        breakTimeouts.clear();
        this.tickCounter = 0;
        this.scanRing = -1;
        this.scanIndex = 0;
        this.pathTarget = null;
        this.bestScanCandidate = null;
        this.wasMining = false;
    }

    @Override
    public void onDeactivate() {
        extraBreakQueue.clear();
        mineTargets.clear();
        KekMine.INSTANCE.mineQueue.clear();
        this.pathTarget = null;
        this.bestScanCandidate = null;
        this.scanRing = -1;
        if (this.autoPathfind.get() && (PathingHelper.isPathing() || PathingHelper.hasPath())) {
            PathingHelper.cancelEverything();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (mc.player == null || mc.world == null) return;
        if (HighwayBuilder.isEating()) return;

        this.tickCounter++;
        breakTimeouts.entrySet().removeIf(entry ->
            mc.world.getBlockState(entry.getKey()).isAir()
                || this.tickCounter - entry.getValue() > MusheorSystem.Manager.rebreakTimeout.get());
        Module kekMine = Modules.get().get(KekMine.class);

        // 1) Collect candidate positions for the active mode.
        if (this.nukerMode.get() == NukerMode.Normal) {
            extraBreakQueue.clear();
            extraBreakQueue = this.getBlocksInShape(this.shape.get(), this.range.get());
        } else if (this.nukerMode.get() == NukerMode.Litematica) {
            if (!LitematicaHelper.isLoaded()) {
                this.warning("Litematica is not installed. Install Litematica or switch to another mode.");
                this.toggle();
                return;
            }
            extraBreakQueue.clear();
            extraBreakQueue.addAll(LitematicaHelper.get().getWrongSchematicBlocks(this.range.get(), this.ignoreAir.get()));
        } else if (this.nukerMode.get() == NukerMode.Selection) {
            extraBreakQueue.clear();
            extraBreakQueue.addAll(this.getSelectionBlocks());
        }
        // NukerMode.Smart intentionally leaves extraBreakQueue as-is (it is filled externally).

        // 2) Filter candidates into the mine list.
        mineTargets.clear();
        for (BlockPos pos : extraBreakQueue) {
            BlockState state = mc.world.getBlockState(pos);
            if ((this.nukerMode.get() == NukerMode.Smart
                    || (this.layerType.get() != LayerType.AboveFeet || pos.getY() >= mc.player.getBlockPos().getY())
                       && (this.layerType.get() != LayerType.RenderLayer || LitematicaHelper.get().isPositionInRenderLayer(pos)))
                && !state.isLiquid()
                && !state.isAir()
                && BlockUtils.canBreak(pos)
                && (!this.avoidSpillingLiquids.get() || !WorldUtils.hasAdjacentLiquid(pos))
                && !(squaredDistanceToBox(mc.player.getEyePos(), pos) > this.range.get() * this.range.get())
                && (this.listMode.get() == ListMode.None
                    || (this.listMode.get() != ListMode.Whitelist || this.whitelist.get().contains(state.getBlock()))
                       && (this.listMode.get() != ListMode.Blacklist || !this.blacklist.get().contains(state.getBlock())))) {
                mineTargets.add(pos);
            }
        }

        // 3) Sort the mine list.
        sortPositions(mineTargets);
        if (this.belowFeetLast.get()) moveBelowFeetToEnd(mineTargets);

        // 4) When nothing is in range, optionally pathfind toward the nearest breakable block.
        if (mineTargets.isEmpty() && this.autoPathfind.get() && this.nukerMode.get() != NukerMode.Smart) {
            if (this.wasMining) {
                this.wasMining = false;
                this.scanRing = -1;
                this.scanIndex = 0;
                this.pathTarget = null;
                this.bestScanCandidate = null;
            }

            if (this.pathTarget != null) {
                if (WorldUtils.isWithinRange(this.pathTarget, this.range.get())) {
                    PathingHelper.cancelEverything();
                    this.pathTarget = null;
                    this.bestScanCandidate = null;
                    this.scanRing = 0;
                    this.scanIndex = 0;
                } else if (!PathingHelper.isPathing()) {
                    BlockPos standPos = this.findStandPosition(this.pathTarget);
                    if (standPos == null) {
                        this.pathTarget = null;
                        this.bestScanCandidate = null;
                    } else {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(standPos, 1));
                    }
                }
            } else {
                if (this.scanRing == -1) {
                    this.scanRing = 0;
                    this.scanIndex = 0;
                }
                int maxChunkRadius = this.pathfindChunkRange.get();
                ChunkPos playerChunk = mc.player.getChunkPos();

                for (int i = 0; i < this.chunksPerTick.get() && this.scanRing <= maxChunkRadius && this.pathTarget == null; i++) {
                    ChunkPos toScan = this.getRingChunk(playerChunk, this.scanRing, this.scanIndex);
                    BlockPos candidate = this.findClosestBlockInChunk(toScan);
                    if (candidate != null
                        && (this.bestScanCandidate == null
                            || mc.player.squaredDistanceTo(candidate.getX() + 0.5, candidate.getY() + 0.5, candidate.getZ() + 0.5)
                               < mc.player.squaredDistanceTo(this.bestScanCandidate.getX() + 0.5, this.bestScanCandidate.getY() + 0.5, this.bestScanCandidate.getZ() + 0.5))) {
                        this.bestScanCandidate = candidate;
                    }

                    int ringSize = this.scanRing == 0 ? 1 : 8 * this.scanRing;
                    this.scanIndex++;
                    if (this.scanIndex >= ringSize) {
                        this.scanIndex = 0;
                        this.scanRing++;
                        if (this.bestScanCandidate != null) {
                            BlockPos standPos = this.findStandPosition(this.bestScanCandidate);
                            if (standPos != null) {
                                this.pathTarget = this.bestScanCandidate;
                                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(standPos, 1));
                            }
                            this.bestScanCandidate = null;
                        }
                    }
                }
            }
        }

        // 5) Dispatch breaks.
        if (!mineTargets.isEmpty()) {
            this.wasMining = true;
            int count = 0;

            for (BlockPos pos : mineTargets) {
                BlockState state = mc.world.getBlockState(pos);
                if (HudInfoPlus.getPacketCount() >= MusheorSystem.Manager.globalPacketLimit.get() * 0.9 && this.preventPacketKick.get()) {
                    break;
                }

                if (!breakTimeouts.containsKey(pos)) {
                    KekMine.MineContext ctx = new KekMine.MineContext(pos, mc.world.getBlockState(pos), true);
                    if (kekMine.isActive()) {
                        if (!KekMine.INSTANCE.isReadyToDequeue()) {
                            Direction face = getClosestFace(mc.player.getEyePos(), pos);
                            if (!ctx.instaBreak && !ctx.reachedThreshold) {
                                KekMine.INSTANCE.mine(pos, state, face);
                            } else {
                                KekMine.breakBlock(pos, face);
                                count++;
                            }
                            breakTimeouts.put(pos, this.tickCounter);
                        }
                    } else {
                        BlockUtils.breakBlock(pos, true);
                        count++;
                        if (!BlockUtils.canInstaBreak(pos)) break;
                    }

                    if (count >= this.blocksPerTick.get()) break;
                }
            }

            // Re-sort KekMine's own pending queue to match our sort order.
            if (kekMine.isActive() && !KekMine.INSTANCE.mineQueue.isEmpty()) {
                List<BlockPos> queueList = new ArrayList<>(KekMine.INSTANCE.mineQueue);
                sortPositions(queueList);
                if (this.belowFeetLast.get()) moveBelowFeetToEnd(queueList);
                KekMine.INSTANCE.mineQueue.clear();
                KekMine.INSTANCE.mineQueue.addAll(queueList);
            }
        }
    }

    /** Sorts {@code list} in place according to the current {@link SortMode}. */
    private void sortPositions(List<BlockPos> list) {
        switch (this.sortMode.get()) {
            case Closest -> list.sort(Comparator.comparingDouble(pos -> mc.player.squaredDistanceTo(Vec3d.ofCenter(pos))));
            case Furthest -> list.sort(Comparator.comparingDouble(pos -> -mc.player.squaredDistanceTo(Vec3d.ofCenter(pos))));
            case TopDown -> list.sort(Comparator.comparingDouble(pos -> -pos.getY()));
            case BottomUp -> list.sort(Comparator.comparingDouble(Vec3i::getY));
            case None -> { }
        }
    }

    /** Moves the column of blocks directly beneath the player to the tail of {@code list}. */
    private static void moveBelowFeetToEnd(List<BlockPos> list) {
        BlockPos p = mc.player.getBlockPos();
        List<BlockPos> below = list.stream()
            .filter(pos -> pos.getX() == p.getX() && pos.getZ() == p.getZ() && pos.getY() < p.getY())
            .sorted(Comparator.comparingInt(Vec3i::getY).reversed())
            .toList();
        list.removeAll(below);
        list.addAll(below);
    }

    /**
     * True while KekMine is actively mining (or has queued) a block that lies inside the
     * highway build footprint. HighwayBuilder/Handlers use this to avoid placing over a
     * block the nuker is still clearing. (Was obfuscated {@code Q90GLXQ0Pef()}; note the
     * sense is "busy", not "finished".)
     */
    public static boolean isBusyInBuildZone() { // was: Q90GLXQ0Pef()
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        if (dir == null) return false;

        boolean isCardinal = dir == WorldUtils.Direction8.NORTH || dir == WorldUtils.Direction8.SOUTH
            || dir == WorldUtils.Direction8.EAST || dir == WorldUtils.Direction8.WEST;
        BlockPos[] zone = isCardinal ? BlockPositions.cardinalTunnel(0, 2) : BlockPositions.diagonalTunnel(0, 2);
        Set<BlockPos> zoneSet = new HashSet<>(Arrays.asList(zone));

        HighwayState state = HighwayState.getInstance();
        HighwayLocator.Checkpoint detected = state.getCurrentCheckpoint();
        if (detected != null && state.getCenterY() != null) {
            int railY = state.getCenterY();
            BlockPos[] railZone = isCardinal ? BlockPositions.cardinalFloor(0, 2, true, true)
                                             : BlockPositions.diagonalFloor(0, 2, true, true);
            for (BlockPos pos : railZone) {
                if (pos.getY() == railY && HighwayLocator.isRailOnAnyHighway(pos, detected)) zoneSet.add(pos);
            }
        }

        BlockPos p = KekMine.INSTANCE.getCurrentTarget();
        if (p != null && zoneSet.contains(p)) return true;
        p = KekMine.INSTANCE.getPendingTarget();
        if (p != null && zoneSet.contains(p)) return true;
        for (BlockPos pos : mineTargets) {
            if (zoneSet.contains(pos)) return true;
        }
        return false;
    }

    /** Collects breakable positions within {@code radius}, either as a cube or a sphere. */
    private List<BlockPos> getBlocksInShape(Shape shape, double radius) { // was: FvaNWO(Shape,double)
        List<BlockPos> positions = new ArrayList<>();
        Vec3d eyePos = mc.player.getEyePos();
        int blockRadius = (int) Math.ceil(radius);
        for (int x = -blockRadius; x < blockRadius; x++) {
            for (int y = -blockRadius; y < blockRadius; y++) {
                for (int z = -blockRadius; z < blockRadius; z++) {
                    BlockPos pos = BlockPos.ofFloored(eyePos.x + x, eyePos.y + y, eyePos.z + z);
                    if (BlockUtils.canBreak(pos)) {
                        if (shape == Shape.Sphere) {
                            if (squaredDistanceToBox(eyePos, pos) <= radius * radius) positions.add(pos);
                        } else if (shape == Shape.Cube) {
                            positions.add(pos);
                        }
                    }
                }
            }
        }
        return positions;
    }

    /** Squared distance from {@code eye} to the nearest point of the block's AABB. */
    static double squaredDistanceToBox(Vec3d eye, BlockPos pos) { // was: FvaNWO(Vec3d,BlockPos)
        double cx = Math.max(pos.getX(), Math.min(eye.x, pos.getX() + 1.0));
        double cy = Math.max(pos.getY(), Math.min(eye.y, pos.getY() + 1.0));
        double cz = Math.max(pos.getZ(), Math.min(eye.z, pos.getZ() + 1.0));
        double dx = eye.x - cx;
        double dy = eye.y - cy;
        double dz = eye.z - cz;
        return dx * dx + dy * dy + dz * dz;
    }

    /** The block face nearest to the player's eye (used as the interaction side). */
    public static Direction getClosestFace(Vec3d eye, BlockPos pos) { // was: Q90GLXQ0Pef(Vec3d,BlockPos)
        double dx = eye.x - (pos.getX() + 0.5);
        double dy = eye.y - (pos.getY() + 0.5);
        double dz = eye.z - (pos.getZ() + 0.5);
        double adx = Math.abs(dx);
        double ady = Math.abs(dy);
        double adz = Math.abs(dz);
        if (adx >= ady && adx >= adz) {
            return dx > 0.0 ? Direction.EAST : Direction.WEST;
        } else if (ady >= adz) {
            return dy > 0.0 ? Direction.UP : Direction.DOWN;
        } else {
            return dz > 0.0 ? Direction.SOUTH : Direction.NORTH;
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

    /** Finds the closest eligible breakable block within the given chunk, or null. */
    private BlockPos findClosestBlockInChunk(ChunkPos chunk) { // was: FvaNWO(ChunkPos)
        if (mc.world == null || mc.player == null) return null;
        if (!mc.world.isChunkLoaded(chunk.x, chunk.z)) return null;

        int minX = chunk.getStartX();
        int maxX = chunk.getEndX();
        int minZ = chunk.getStartZ();
        int maxZ = chunk.getEndZ();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        if (this.nukerMode.get() == NukerMode.Litematica) {
            if (!LitematicaHelper.isLoaded()) return null;
            int minY = mc.world.getBottomY();
            int maxY = mc.world.getBottomY() + mc.world.getHeight() - 1;
            for (BlockPos pos : LitematicaHelper.get()
                .getWrongBlocksInBox(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), !this.ignoreAir.get(), 64)) {
                BlockState state = mc.world.getBlockState(pos);
                if (this.isInLayer(pos)
                    && BlockUtils.canBreak(pos)
                    && (!this.avoidSpillingLiquids.get() || !WorldUtils.hasAdjacentLiquid(pos))
                    && this.isAllowedByList(state)
                    && this.hasSolidBelow(pos, 8)) {
                    double dist = pos.getSquaredDistance(px, py, pz);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = pos;
                    }
                }
            }
        } else {
            ISelection[] selections = null;
            if (this.nukerMode.get() == NukerMode.Selection) {
                selections = BaritoneAPI.getProvider().getPrimaryBaritone().getSelectionManager().getSelections();
                if (selections == null || selections.length == 0) return null;
            }

            int feetY = mc.player.getBlockPos().getY();
            int r = (int) Math.ceil(this.range.get());
            int minY = this.layerType.get() == LayerType.AboveFeet ? feetY : Math.max(mc.world.getBottomY(), feetY - r);
            int maxY = Math.min(mc.world.getBottomY() + mc.world.getHeight() - 1, feetY + r);
            BlockPos.Mutable mpos = new BlockPos.Mutable();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        mpos.set(x, y, z);
                        BlockState state = mc.world.getBlockState(mpos);
                        if (!state.isAir()
                            && !state.isLiquid()
                            && BlockUtils.canBreak(mpos)
                            && (!this.avoidSpillingLiquids.get() || !WorldUtils.hasAdjacentLiquid(mpos))
                            && this.isInLayer(mpos)
                            && this.isAllowedByList(state)
                            && (selections == null || isInSelection(mpos, selections))
                            && this.hasSolidBelow(mpos, 8)) {
                            double dist = mpos.getSquaredDistance(px, py, pz);
                            if (dist < closestDist) {
                                closestDist = dist;
                                closest = mpos.toImmutable();
                            }
                        }
                    }
                }
            }
        }
        return closest;
    }

    /** Finds the nearest standable position (solid ground, within range) next to {@code candidate}. */
    private BlockPos findStandPosition(BlockPos candidate) { // was: FvaNWO(BlockPos)
        int playerY = mc.player.getBlockY();
        int r = (int) Math.ceil(this.range.get());
        double rangeSq = this.range.get() * this.range.get();
        int dy = playerY - candidate.getY();
        if (dy * dy > rangeSq) return null;

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos.Mutable standMut = new BlockPos.Mutable();
        BlockPos.Mutable groundMut = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!(dx * dx + dy * dy + dz * dz > rangeSq)) {
                    int sx = candidate.getX() + dx;
                    int sz = candidate.getZ() + dz;
                    groundMut.set(sx, playerY - 1, sz);
                    BlockState ground = mc.world.getBlockState(groundMut);
                    if (!ground.isAir() && !ground.isLiquid()) {
                        standMut.set(sx, playerY, sz);
                        double dist = standMut.getSquaredDistance(candidate);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = standMut.toImmutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    /** True if there is a solid (non-air, non-liquid) block within {@code depth} blocks below {@code pos}. */
    private boolean hasSolidBelow(BlockPos pos, int depth) { // was: FvaNWO(BlockPos,int)
        int bottom = mc.world.getBottomY();
        int x = pos.getX();
        int z = pos.getZ();
        BlockPos.Mutable check = new BlockPos.Mutable(x, 0, z);
        for (int dy = 1; dy <= depth; dy++) {
            int y = pos.getY() - dy;
            if (y <= bottom) break;
            check.set(x, y, z);
            BlockState s = mc.world.getBlockState(check);
            if (!s.isAir() && !s.isLiquid()) return true;
        }
        return false;
    }

    /** True if {@code pos} is allowed by the current {@link LayerType} restriction. */
    private boolean isInLayer(BlockPos pos) { // was: Q90GLXQ0Pef(BlockPos)
        if (this.layerType.get() == LayerType.AboveFeet && pos.getY() < mc.player.getBlockPos().getY()) return false;
        return this.layerType.get() != LayerType.RenderLayer || !LitematicaHelper.isLoaded()
            || LitematicaHelper.get().isPositionInRenderLayer(pos);
    }

    /** True if {@code state}'s block is allowed by the current white/blacklist. */
    private boolean isAllowedByList(BlockState state) { // was: FvaNWO(BlockState)
        if (this.listMode.get() == ListMode.Whitelist) return this.whitelist.get().contains(state.getBlock());
        if (this.listMode.get() == ListMode.Blacklist) return !this.blacklist.get().contains(state.getBlock());
        return true;
    }

    /** True if {@code pos} falls inside any Baritone selection box. */
    private static boolean isInSelection(BlockPos pos, ISelection[] selections) { // was: FvaNWO(BlockPos,ISelection[])
        for (ISelection sel : selections) {
            BlockPos min = sel.min();
            BlockPos max = sel.max();
            if (pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ()) {
                return true;
            }
        }
        return false;
    }

    /** Collects non-empty blocks within 12 blocks of the player that fall in any Baritone selection. */
    private List<BlockPos> getSelectionBlocks() { // was: psJq59YIbp3Z()
        List<BlockPos> list = new ArrayList<>();
        if (mc.player == null || mc.world == null) return list;
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        ISelectionManager selectionManager = baritone.getSelectionManager();
        if (selectionManager.getSelections() == null) return list;

        for (ISelection selection : selectionManager.getSelections()) {
            BlockPos min = selection.min();
            BlockPos max = selection.max();
            int regionMinX = Math.min(min.getX(), max.getX());
            int regionMinY = Math.min(min.getY(), max.getY());
            int regionMinZ = Math.min(min.getZ(), max.getZ());
            int regionMaxX = Math.max(min.getX(), max.getX());
            int regionMaxY = Math.max(min.getY(), max.getY());
            int regionMaxZ = Math.max(min.getZ(), max.getZ());
            int px = mc.player.getBlockX();
            int py = mc.player.getBlockY();
            int pz = mc.player.getBlockZ();
            int minX = Math.max(regionMinX, px - 12);
            int minY = Math.max(regionMinY, py - 12);
            int minZ = Math.max(regionMinZ, pz - 12);
            int maxX = Math.min(regionMaxX, px + 12);
            int maxY = Math.min(regionMaxY, py + 12);
            int maxZ = Math.min(regionMaxZ, pz + 12);
            if (minX <= maxX && minY <= maxY && minZ <= maxZ) {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (!mc.world.getBlockState(pos).isAir()
                                && !mc.world.getBlockState(pos).isLiquid()
                                && !list.contains(pos)
                                && WorldUtils.isWithinRange(pos, this.range.get())) {
                                list.add(pos);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (mc.player == null || mc.world == null) return;
        if (this.globalRendering.get()) {
            RenderUtils.render(event, mineTargets);
        } else {
            RenderUtils.render(event, mineTargets, this.color.get(), this.color.get(), ShapeMode.Lines);
        }
    }

    /** Layer restriction on where blocks may be broken. */ // was: enum LayerType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum LayerType { All, AboveFeet, RenderLayer }

    /** White/blacklist selection mode. */ // was: enum ListMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum ListMode { None, Whitelist, Blacklist }

    /** Source of the blocks to mine. */ // was: enum NukerMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F}
    public enum NukerMode { Normal, Smart, Litematica, Selection }

    /** Radius scan shape (Normal mode). */ // was: enum Shape {FvaNWO, Q90GLXQ0Pef}
    public enum Shape { Cube, Sphere }

    /** Order in which queued blocks are mined. */ // was: enum SortMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F, rKbT3Ifwo}
    public enum SortMode { None, Closest, Furthest, TopDown, BottomUp }
}
