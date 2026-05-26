// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import java.util.ArrayList;
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
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;

public class PortalSkipDetection
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> chunkRadius;
    private final CopyOnWriteArrayList<BlockPos> portalBlocks;
    private final Set<Long> processedPortalKeys;
    private final Set<Long> processedChunks;
    private ExecutorService scannerExecutor;
    private final AtomicBoolean stopFlag;

    public PortalSkipDetection() {
        super(musheor.AUTOMATION, "portal-skip-detection", "Detects portal skip patterns (AIR in CAVE_AIR regions)");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.chunkRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("chunk-radius")).description("Radius in chunks to scan (circular)")).defaultValue((Object)4)).sliderMin(1).sliderMax(8).build());
        this.portalBlocks = new CopyOnWriteArrayList();
        this.processedPortalKeys = ConcurrentHashMap.newKeySet();
        this.processedChunks = ConcurrentHashMap.newKeySet();
        this.stopFlag = new AtomicBoolean(false);
    }

    public void onActivate() {
        this.portalBlocks.clear();
        this.processedPortalKeys.clear();
        this.processedChunks.clear();
        this.stopFlag.set(false);
        this.scannerExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "PortalSkipScanner");
            thread.setDaemon(true);
            thread.setPriority(1);
            return thread;
        });
        this.scannerExecutor.submit(this::runScanLoop);
        this.info("Portal scanner started.", new Object[0]);
    }

    public void onDeactivate() {
        this.stopFlag.set(true);
        if (this.scannerExecutor != null) {
            this.scannerExecutor.shutdownNow();
            this.scannerExecutor = null;
        }
        this.portalBlocks.clear();
        this.processedPortalKeys.clear();
        this.processedChunks.clear();
    }

    private void runScanLoop() {
        while (!this.stopFlag.get()) {
            try {
                if (this.mc.player == null || this.mc.world == null) {
                    Thread.sleep(500L);
                    continue;
                }
                ClientWorld world = this.mc.world;
                ChunkPos ChunkPos2 = this.mc.player.getChunkPos();
                int n = (Integer)this.chunkRadius.get();
                ArrayList<ChunkPos> arrayList = new ArrayList<ChunkPos>();
                for (int i = -n; i <= n; ++i) {
                    for (int j = -n; j <= n; ++j) {
                        ChunkPos ChunkPos3;
                        long l;
                        if (i * i + j * j > n * n || this.processedChunks.contains(l = (ChunkPos3 = new ChunkPos(ChunkPos2.x + i, ChunkPos2.z + j)).toLong()) || !world.isChunkLoaded(ChunkPos3.x, ChunkPos3.z)) continue;
                        arrayList.add(ChunkPos3);
                        this.processedChunks.add(l);
                    }
                }
                if (!arrayList.isEmpty()) {
                    for (ChunkPos ChunkPos4 : arrayList) {
                        if (this.stopFlag.get()) break;
                        this.scanChunk(world, ChunkPos4);
                    }
                }
                Thread.sleep(50L);
            }
            catch (InterruptedException interruptedException) {
                break;
            }
            catch (Exception exception) {
            }
        }
    }

    private void scanChunk(World world, ChunkPos ChunkPos2) {
        int n = ChunkPos2.getStartX();
        int n2 = ChunkPos2.getStartZ();
        int n3 = world.getBottomY();
        int n4 = world.getBottomY() + world.getHeight();
        for (int i = n3; i < n4 && !this.stopFlag.get(); ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    BlockPos BlockPos2 = new BlockPos(n + j, i, n2 + k);
                    this.mp3zoXQFKUKYj5(DimensionType2, BlockPos2);
                }
            }
        }
    }

    private void checkBlockForPortal(World world, BlockPos BlockPos2) {
        try {
            if (world.getBlockState(BlockPos2).getBlock() != Blocks.LAVA) {
                return;
            }
        }
        catch (Exception exception) {
            return;
        }
        this.jOdDDFXSeWl4(DimensionType2, BlockPos2, true);
        this.jOdDDFXSeWl4(DimensionType2, BlockPos2, false);
    }

    private void checkPortalOrientation(World world, BlockPos BlockPos2, boolean bl) {
        int n;
        int n2;
        long l = BlockPos2.asLong() ^ (bl ? 1L : 0L);
        if (this.processedPortalKeys.contains(l)) {
            return;
        }
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        boolean bl2 = true;
        try {
            for (n2 = 0; n2 < 5 && bl2; ++n2) {
                for (n = 0; n < 4 && bl2; ++n) {
                    boolean bl3;
                    BlockPos BlockPos3 = bl ? BlockPos2.add(n, n2, 0) : BlockPos2.add(0, n2, n);
                    BlockState BlockState2 = world.getBlockState(BlockPos3);
                    boolean bl4 = BlockState2.getBlock() == Blocks.LAVA;
                    boolean bl5 = bl3 = !(n2 != 0 && n2 != 4 || n != 0 && n != 3);
                    if (bl4) {
                        arrayList.add(BlockPos3);
                        continue;
                    }
                    if (bl3) continue;
                    bl2 = false;
                }
            }
        }
        catch (Exception exception) {
            return;
        }
        if (bl2 && arrayList.size() >= 14 && this.mp3zoXQFKUKYj5(DimensionType2, BlockPos2, bl)) {
            this.processedPortalKeys.add(l);
            this.portalBlocks.addAll(arrayList);
            n2 = BlockPos2.getX();
            n = BlockPos2.getY();
            int n3 = BlockPos2.getZ();
            this.mc.execute(() -> this.info("Portal found at %d, %d, %d!", new Object[]{n2, n, n3}));
        }
    }

    private boolean verifyPortalFrame(World world, BlockPos BlockPos2, boolean bl) {
        try {
            int n;
            int n2;
            int n3;
            int n4 = 0;
            int n5 = 0;
            boolean bl2 = true;
            for (n3 = 0; n3 < 5; ++n3) {
                BlockPos BlockPos3;
                BlockPos BlockPos4 = BlockPos3 = bl ? BlockPos2.add(-1, n3, 0) : BlockPos2.add(0, n3, -1);
                if (world.getBlockState(BlockPos3).getBlock() == Blocks.LAVA) {
                    bl2 = false;
                }
                if (world.getBlockState(BlockPos3).getBlock() != Blocks.OBSIDIAN) continue;
                ++n4;
            }
            if (bl2) {
                ++n5;
            }
            n3 = 1;
            for (n2 = 0; n2 < 5; ++n2) {
                BlockPos BlockPos5;
                BlockPos BlockPos6 = BlockPos5 = bl ? BlockPos2.add(4, n2, 0) : BlockPos2.add(0, n2, 4);
                if (world.getBlockState(BlockPos5).getBlock() == Blocks.LAVA) {
                    n3 = 0;
                }
                if (world.getBlockState(BlockPos5).getBlock() != Blocks.OBSIDIAN) continue;
                ++n4;
            }
            if (n3 != 0) {
                ++n5;
            }
            n2 = 1;
            for (n = 0; n < 4; ++n) {
                BlockPos BlockPos7;
                BlockPos BlockPos8 = BlockPos7 = bl ? BlockPos2.add(n, -1, 0) : BlockPos2.add(0, -1, n);
                if (world.getBlockState(BlockPos7).getBlock() == Blocks.LAVA) {
                    n2 = 0;
                }
                if (world.getBlockState(BlockPos7).getBlock() != Blocks.OBSIDIAN) continue;
                ++n4;
            }
            if (n2 != 0) {
                ++n5;
            }
            n = 1;
            for (int i = 0; i < 4; ++i) {
                BlockPos BlockPos9;
                BlockPos BlockPos10 = BlockPos9 = bl ? BlockPos2.add(i, 5, 0) : BlockPos2.add(0, 5, i);
                if (world.getBlockState(BlockPos9).getBlock() == Blocks.LAVA) {
                    n = 0;
                }
                if (world.getBlockState(BlockPos9).getBlock() != Blocks.OBSIDIAN) continue;
                ++n4;
            }
            if (n != 0) {
                ++n5;
            }
            return n5 >= 2 && n4 >= 10;
        }
        catch (Exception exception) {
            return false;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.portalBlocks.isEmpty()) {
            return;
        }
        RenderUtils.mp3zoXQFKUKYj5(render3DEvent, new ArrayList<BlockPos>(this.portalBlocks));
    }
}

