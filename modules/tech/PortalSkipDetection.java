// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.tech;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.RenderUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * "portal-skip-detection" — finds abandoned/unlit nether portal frames (a 4×5 opening of
 * AIR that would normally be CAVE_AIR underground) around the player. A background thread
 * scans loaded chunks in a circular radius, validating candidate frames by their bounded
 * sides and nearby cave-air, and renders any matches.
 */
public class PortalSkipDetection extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: FvaNWO
    private final Setting<Integer> chunkRadius = sgGeneral.add(new IntSetting.Builder() // was: Q90GLXQ0Pef
        .name("chunk-radius").description("Radius in chunks to scan (circular)").defaultValue(4).sliderMin(1).sliderMax(8).build());

    private final CopyOnWriteArrayList<BlockPos> detectedPortalBlocks = new CopyOnWriteArrayList<>(); // was: psJq59YIbp3Z
    private final Set<Long> foundPortalKeys = ConcurrentHashMap.newKeySet();  // was: SOYyh5IPg26f7F
    private final Set<Long> scannedChunks = ConcurrentHashMap.newKeySet();    // was: rKbT3Ifwo
    private ExecutorService scanExecutor;                                     // was: r7hOYIKN2
    private final AtomicBoolean stopped = new AtomicBoolean(false);           // was: oZHMlTL

    public PortalSkipDetection() {
        super(musheor.AUTOMATION, "portal-skip-detection", "Detects portal skip patterns (AIR in CAVE_AIR regions)");
    }

    @Override
    public void onActivate() {
        this.detectedPortalBlocks.clear();
        this.foundPortalKeys.clear();
        this.scannedChunks.clear();
        this.stopped.set(false);
        this.scanExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "PortalSkipScanner");
            t.setDaemon(true);
            t.setPriority(1);
            return t;
        });
        this.scanExecutor.submit(this::scanLoop);
        this.info("Portal scanner started.");
    }

    @Override
    public void onDeactivate() {
        this.stopped.set(true);
        if (this.scanExecutor != null) {
            this.scanExecutor.shutdownNow();
            this.scanExecutor = null;
        }
        this.detectedPortalBlocks.clear();
        this.foundPortalKeys.clear();
        this.scannedChunks.clear();
    }

    /** Background scan loop: scans newly-loaded chunks within the circular radius. */
    private void scanLoop() { // was: FvaNWO()
        while (!this.stopped.get()) {
            try {
                if (this.mc.player != null && this.mc.world != null) {
                    World world = this.mc.world;
                    ChunkPos center = this.mc.player.getChunkPos();
                    int r = this.chunkRadius.get();
                    List<ChunkPos> newChunks = new ArrayList<>();
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dz = -r; dz <= r; dz++) {
                            if (dx * dx + dz * dz <= r * r) {
                                ChunkPos chunk = new ChunkPos(center.x + dx, center.z + dz);
                                long chunkKey = chunk.toLong();
                                if (!this.scannedChunks.contains(chunkKey) && world.isChunkLoaded(chunk.x, chunk.z)) {
                                    newChunks.add(chunk);
                                    this.scannedChunks.add(chunkKey);
                                }
                            }
                        }
                    }
                    for (ChunkPos chunk : newChunks) {
                        if (this.stopped.get()) break;
                        this.scanChunk(world, chunk);
                    }
                    Thread.sleep(50L);
                } else {
                    Thread.sleep(500L);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception ignored) {
            }
        }
    }

    /** Scans every block column in a chunk for candidate portal openings. */
    private void scanChunk(World world, ChunkPos chunk) { // was: FvaNWO(World,ChunkPos)
        int startX = chunk.getStartX();
        int startZ = chunk.getStartZ();
        int minY = world.getBottomY();
        int maxY = world.getBottomY() + world.getHeight();
        for (int y = minY; y < maxY && !this.stopped.get(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    this.checkAirBlock(world, new BlockPos(startX + x, y, startZ + z));
                }
            }
        }
    }

    /** If {@code pos} is AIR, tests both frame orientations anchored there. */
    private void checkAirBlock(World world, BlockPos pos) { // was: FvaNWO(World,BlockPos)
        try {
            if (world.getBlockState(pos).getBlock() != Blocks.AIR) return;
        } catch (Exception e) {
            return;
        }
        this.checkPortalFrame(world, pos, true);
        this.checkPortalFrame(world, pos, false);
    }

    /** Validates a 4×5 AIR opening (allowing empty corners) anchored at {@code corner}. */
    private void checkPortalFrame(World world, BlockPos corner, boolean xAligned) { // was: FvaNWO(World,BlockPos,boolean)
        long key = corner.asLong() ^ (xAligned ? 1L : 0L);
        if (this.foundPortalKeys.contains(key)) return;
        List<BlockPos> portalBlocks = new ArrayList<>();
        boolean valid = true;
        try {
            for (int h = 0; h < 5 && valid; h++) {
                for (int w = 0; w < 4 && valid; w++) {
                    BlockPos checkPos = xAligned ? corner.add(w, h, 0) : corner.add(0, h, w);
                    BlockState state = world.getBlockState(checkPos);
                    boolean isAir = state.getBlock() == Blocks.AIR;
                    boolean isCorner = (h == 0 || h == 4) && (w == 0 || w == 3);
                    if (isAir) portalBlocks.add(checkPos);
                    else if (!isCorner) valid = false;
                }
            }
        } catch (Exception e) {
            return;
        }

        if (valid && portalBlocks.size() >= 14 && this.isPortalSkipContext(world, corner, xAligned)) {
            this.foundPortalKeys.add(key);
            this.detectedPortalBlocks.addAll(portalBlocks);
            int x = corner.getX();
            int y = corner.getY();
            int z = corner.getZ();
            this.mc.execute(() -> this.info("Portal found at %d, %d, %d!", x, y, z));
        }
    }

    /** True if the frame is sufficiently bounded and surrounded by cave-air (a portal-skip signature). */
    private boolean isPortalSkipContext(World world, BlockPos corner, boolean xAligned) { // was: Q90GLXQ0Pef(World,BlockPos,boolean)
        try {
            int caveAirNearby = 0;
            int boundedSides = 0;

            boolean leftBounded = true;
            for (int h = 0; h < 5; h++) {
                BlockPos pos = xAligned ? corner.add(-1, h, 0) : corner.add(0, h, -1);
                if (world.getBlockState(pos).getBlock() == Blocks.AIR) leftBounded = false;
                if (world.getBlockState(pos).getBlock() == Blocks.CAVE_AIR) caveAirNearby++;
            }
            if (leftBounded) boundedSides++;

            boolean rightBounded = true;
            for (int h = 0; h < 5; h++) {
                BlockPos pos = xAligned ? corner.add(4, h, 0) : corner.add(0, h, 4);
                if (world.getBlockState(pos).getBlock() == Blocks.AIR) rightBounded = false;
                if (world.getBlockState(pos).getBlock() == Blocks.CAVE_AIR) caveAirNearby++;
            }
            if (rightBounded) boundedSides++;

            boolean bottomBounded = true;
            for (int w = 0; w < 4; w++) {
                BlockPos pos = xAligned ? corner.add(w, -1, 0) : corner.add(0, -1, w);
                if (world.getBlockState(pos).getBlock() == Blocks.AIR) bottomBounded = false;
                if (world.getBlockState(pos).getBlock() == Blocks.CAVE_AIR) caveAirNearby++;
            }
            if (bottomBounded) boundedSides++;

            boolean topBounded = true;
            for (int w = 0; w < 4; w++) {
                BlockPos pos = xAligned ? corner.add(w, 5, 0) : corner.add(0, 5, w);
                if (world.getBlockState(pos).getBlock() == Blocks.AIR) topBounded = false;
                if (world.getBlockState(pos).getBlock() == Blocks.CAVE_AIR) caveAirNearby++;
            }
            if (topBounded) boundedSides++;

            return boundedSides >= 2 && caveAirNearby >= 10;
        } catch (Exception e) {
            return false;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.mc.player != null && this.mc.world != null && !this.detectedPortalBlocks.isEmpty()) {
            RenderUtils.render(event, new ArrayList<>(this.detectedPortalBlocks));
        }
    }
}
