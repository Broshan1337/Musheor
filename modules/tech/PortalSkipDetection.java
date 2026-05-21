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
import net.minecraft.ChunkPos;
import net.minecraft.DimensionType;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;
import net.minecraft.class_638;

public class PortalSkipDetection
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> chunkRadius;
    private final CopyOnWriteArrayList<BlockPos> A1xp1DHrnISZwJb;
    private final Set<Long> YqVTXQj0rqGr;
    private final Set<Long> PIvYDB4epL00lo;
    private ExecutorService gFbILzs6j0GLw;
    private final AtomicBoolean GCKrteYx1UK4ofkK;

    public PortalSkipDetection() {
        super(musheor.AUTOMATION, "portal-skip-detection", "Detects portal skip patterns (AIR in CAVE_AIR regions)");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.chunkRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("chunk-radius")).description("Radius in chunks to scan (circular)")).defaultValue((Object)4)).sliderMin(1).sliderMax(8).build());
        this.A1xp1DHrnISZwJb = new CopyOnWriteArrayList();
        this.YqVTXQj0rqGr = ConcurrentHashMap.newKeySet();
        this.PIvYDB4epL00lo = ConcurrentHashMap.newKeySet();
        this.GCKrteYx1UK4ofkK = new AtomicBoolean(false);
    }

    public void onActivate() {
        this.A1xp1DHrnISZwJb.clear();
        this.YqVTXQj0rqGr.clear();
        this.PIvYDB4epL00lo.clear();
        this.GCKrteYx1UK4ofkK.set(false);
        this.gFbILzs6j0GLw = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "PortalSkipScanner");
            thread.setDaemon(true);
            thread.setPriority(1);
            return thread;
        });
        this.gFbILzs6j0GLw.submit(this::PvLNVHs2LlOde76);
        this.info("Portal scanner started.", new Object[0]);
    }

    public void onDeactivate() {
        this.GCKrteYx1UK4ofkK.set(true);
        if (this.gFbILzs6j0GLw != null) {
            this.gFbILzs6j0GLw.shutdownNow();
            this.gFbILzs6j0GLw = null;
        }
        this.A1xp1DHrnISZwJb.clear();
        this.YqVTXQj0rqGr.clear();
        this.PIvYDB4epL00lo.clear();
    }

    private void PvLNVHs2LlOde76() {
        while (!this.GCKrteYx1UK4ofkK.get()) {
            try {
                if (this.mc.player == null || this.mc.world == null) {
                    Thread.sleep(500L);
                    continue;
                }
                class_638 class_6382 = this.mc.world;
                ChunkPos ChunkPos2 = this.mc.player.method_31476();
                int n = (Integer)this.chunkRadius.get();
                ArrayList<ChunkPos> arrayList = new ArrayList<ChunkPos>();
                for (int i = -n; i <= n; ++i) {
                    for (int j = -n; j <= n; ++j) {
                        ChunkPos ChunkPos3;
                        long l;
                        if (i * i + j * j > n * n || this.PIvYDB4epL00lo.contains(l = (ChunkPos3 = new ChunkPos(ChunkPos2.x + i, ChunkPos2.z + j)).method_8324()) || !class_6382.method_8393(ChunkPos3.x, ChunkPos3.z)) continue;
                        arrayList.add(ChunkPos3);
                        this.PIvYDB4epL00lo.add(l);
                    }
                }
                if (!arrayList.isEmpty()) {
                    for (ChunkPos ChunkPos4 : arrayList) {
                        if (this.GCKrteYx1UK4ofkK.get()) break;
                        this.jOdDDFXSeWl4((DimensionType)class_6382, ChunkPos4);
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

    private void jOdDDFXSeWl4(DimensionType DimensionType2, ChunkPos ChunkPos2) {
        int n = ChunkPos2.getStartX();
        int n2 = ChunkPos2.getStartZ();
        int n3 = DimensionType2.getBottomY();
        int n4 = DimensionType2.getBottomY() + DimensionType2.method_31605();
        for (int i = n3; i < n4 && !this.GCKrteYx1UK4ofkK.get(); ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    BlockPos BlockPos2 = new BlockPos(n + j, i, n2 + k);
                    this.mp3zoXQFKUKYj5(DimensionType2, BlockPos2);
                }
            }
        }
    }

    private void mp3zoXQFKUKYj5(DimensionType DimensionType2, BlockPos BlockPos2) {
        try {
            if (DimensionType2.getBlockState(BlockPos2).getBlock() != Blocks.LAVA) {
                return;
            }
        }
        catch (Exception exception) {
            return;
        }
        this.jOdDDFXSeWl4(DimensionType2, BlockPos2, true);
        this.jOdDDFXSeWl4(DimensionType2, BlockPos2, false);
    }

    private void jOdDDFXSeWl4(DimensionType DimensionType2, BlockPos BlockPos2, boolean bl) {
        int n;
        int n2;
        long l = BlockPos2.method_10063() ^ (bl ? 1L : 0L);
        if (this.YqVTXQj0rqGr.contains(l)) {
            return;
        }
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        boolean bl2 = true;
        try {
            for (n2 = 0; n2 < 5 && bl2; ++n2) {
                for (n = 0; n < 4 && bl2; ++n) {
                    boolean bl3;
                    BlockPos BlockPos3 = bl ? BlockPos2.method_10069(n, n2, 0) : BlockPos2.method_10069(0, n2, n);
                    BlockState BlockState2 = DimensionType2.getBlockState(BlockPos3);
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
            this.YqVTXQj0rqGr.add(l);
            this.A1xp1DHrnISZwJb.addAll(arrayList);
            n2 = BlockPos2.getX();
            n = BlockPos2.getY();
            int n3 = BlockPos2.getZ();
            this.mc.execute(() -> this.info("Portal found at %d, %d, %d!", new Object[]{n2, n, n3}));
        }
    }

    private boolean mp3zoXQFKUKYj5(DimensionType DimensionType2, BlockPos BlockPos2, boolean bl) {
        try {
            int n;
            int n2;
            int n3;
            int n4 = 0;
            int n5 = 0;
            boolean bl2 = true;
            for (n3 = 0; n3 < 5; ++n3) {
                BlockPos BlockPos3;
                BlockPos BlockPos4 = BlockPos3 = bl ? BlockPos2.method_10069(-1, n3, 0) : BlockPos2.method_10069(0, n3, -1);
                if (DimensionType2.getBlockState(BlockPos3).getBlock() == Blocks.LAVA) {
                    bl2 = false;
                }
                if (DimensionType2.getBlockState(BlockPos3).getBlock() != Blocks.field_10543) continue;
                ++n4;
            }
            if (bl2) {
                ++n5;
            }
            n3 = 1;
            for (n2 = 0; n2 < 5; ++n2) {
                BlockPos BlockPos5;
                BlockPos BlockPos6 = BlockPos5 = bl ? BlockPos2.method_10069(4, n2, 0) : BlockPos2.method_10069(0, n2, 4);
                if (DimensionType2.getBlockState(BlockPos5).getBlock() == Blocks.LAVA) {
                    n3 = 0;
                }
                if (DimensionType2.getBlockState(BlockPos5).getBlock() != Blocks.field_10543) continue;
                ++n4;
            }
            if (n3 != 0) {
                ++n5;
            }
            n2 = 1;
            for (n = 0; n < 4; ++n) {
                BlockPos BlockPos7;
                BlockPos BlockPos8 = BlockPos7 = bl ? BlockPos2.method_10069(n, -1, 0) : BlockPos2.method_10069(0, -1, n);
                if (DimensionType2.getBlockState(BlockPos7).getBlock() == Blocks.LAVA) {
                    n2 = 0;
                }
                if (DimensionType2.getBlockState(BlockPos7).getBlock() != Blocks.field_10543) continue;
                ++n4;
            }
            if (n2 != 0) {
                ++n5;
            }
            n = 1;
            for (int i = 0; i < 4; ++i) {
                BlockPos BlockPos9;
                BlockPos BlockPos10 = BlockPos9 = bl ? BlockPos2.method_10069(i, 5, 0) : BlockPos2.method_10069(0, 5, i);
                if (DimensionType2.getBlockState(BlockPos9).getBlock() == Blocks.LAVA) {
                    n = 0;
                }
                if (DimensionType2.getBlockState(BlockPos9).getBlock() != Blocks.field_10543) continue;
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
        if (this.A1xp1DHrnISZwJb.isEmpty()) {
            return;
        }
        RenderUtils.mp3zoXQFKUKYj5(render3DEvent, new ArrayList<BlockPos>(this.A1xp1DHrnISZwJb));
    }
}

