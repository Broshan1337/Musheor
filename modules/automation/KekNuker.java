// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import java.awt.Color;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import musheor.compat.LitematicaHelper;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.features.KekMine;
import musheor.modules.hud.HudInfoPlus;
import musheor.musheor;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.BlockState;
import net.minecraft.MinecraftClient;

public class KekNuker
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private static final MinecraftClient lVls3aWwqcm7 = MinecraftClient.getInstance();
    public static KekNuker ThlHLXv3gtRxWy;
    public final Setting<NukerMode> nukerMode;
    private final Setting<Boolean> ignoreAir;
    private final Setting<Boolean> pathfind;
    private final Setting<Integer> pathfindChunkRange;
    public final Setting<Shape> shape;
    private final Setting<SortMode> sortMode;
    private final Setting<Boolean> flatten;
    public final Setting<Double> range;
    private final Setting<Boolean> avoidSpillingLiquid;
    private final Setting<Boolean> noPacketKick;
    public final Setting<Integer> bpt;
    private final Setting<ListMode> listMode;
    private final Setting<List<Block>> whitelist;
    private final Setting<List<Block>> blacklist;
    private final Setting<Boolean> globalRendering;
    private final Setting<SettingColor> renderColor;
    public static List<BlockPos> gaDbi5D443T6vqgt;
    private List<BlockPos> E4emISQ55E8d;
    public static final Map<BlockPos, Integer> TZa5O0xAoIaC;
    private BlockPos L8HTTT;
    private int vV6cbpE7KWBI;

    public KekNuker() {
        super(musheor.AUTOMATION, "kek-nuker", "Mines blocks in radius of the player");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.nukerMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("nuker-mode")).defaultValue((Object)NukerMode.KrscIKJwTdc)).build());
        this.ignoreAir = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-air")).description("Ignores air blocks in the schematic.")).visible(() -> LitematicaHelper.isLoaded() && this.nukerMode.get() == NukerMode.kAT8CfWuaGb)).defaultValue((Object)false)).build());
        this.pathfind = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-pathfind")).description("Automatically pathfinds when no blocks are in range")).defaultValue((Object)true)).visible(() -> this.nukerMode.get() != NukerMode.h9MViB)).build());
        this.pathfindChunkRange = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pathfind-chunk-range")).description("Maximum range in chunks the pathfinder can scan for more blocks to break")).defaultValue((Object)2)).sliderMax(8).visible(() -> this.nukerMode.get() != NukerMode.h9MViB)).build());
        this.shape = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape")).defaultValue((Object)Shape.eXDG63e)).visible(() -> this.nukerMode.get() != NukerMode.h9MViB)).build());
        this.sortMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("sort-mode")).description("Choose what order to mine the blocks in")).defaultValue((Object)SortMode.QhaZQGwqdeFQp)).build());
        this.flatten = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("flatten")).description("Flattens the area by not mining below the player's feet")).defaultValue((Object)true)).visible(() -> this.nukerMode.get() != NukerMode.h9MViB)).build());
        this.range = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).defaultValue(1.0).min(2.0).max(8.0).decimalPlaces(1).visible(() -> this.nukerMode.get() != NukerMode.h9MViB)).build());
        this.avoidSpillingLiquid = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("avoid-spilling-liquids")).description("Prevents you from mining blocks that will cause liquid to flow everywhere")).defaultValue((Object)true)).build());
        this.noPacketKick = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("prevent-packet-kick")).description("+100000 aura setting")).defaultValue((Object)true)).build());
        this.bpt = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("Maximum blocks to try to break per tick. (Only for instant-breakable-blocks)")).defaultValue((Object)1)).min(1).sliderRange(1, 50).build());
        this.listMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue((Object)ListMode.m9okHiGFumA)).build());
        this.whitelist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("The blocks you want to mine.")).defaultValue(new Block[0]).visible(() -> this.listMode.get() == ListMode.LtMW6lcKPv)).build());
        this.blacklist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blacklist")).description("The blocks you don't want to mine.")).defaultValue(new Block[0]).visible(() -> this.listMode.get() == ListMode.fXEQFU)).build());
        this.globalRendering = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("global-rendering")).defaultValue((Object)true)).description("Synchronize rendering with Musheor-Tab")).build());
        this.renderColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).defaultValue(new SettingColor(Color.cyan)).description("Custom color for rendering (lines / wireframe)")).visible(() -> (Boolean)this.globalRendering.get() == false)).build());
        this.E4emISQ55E8d = new ArrayList<BlockPos>();
        this.L8HTTT = null;
        ThlHLXv3gtRxWy = this;
    }

    public static boolean Pa3aVwRtUo45jMG() {
        return !gaDbi5D443T6vqgt.isEmpty() && ThlHLXv3gtRxWy.isActive() || KekMine.YnQ4ChsDR.og2KVvNzA();
    }

    public static List<Block> t018N0() {
        return (List)KekNuker.ThlHLXv3gtRxWy.blacklist.get();
    }

    public void onActivate() {
        TZa5O0xAoIaC.clear();
        this.L8HTTT = null;
        this.vV6cbpE7KWBI = 0;
    }

    public void onDeactivate() {
        gaDbi5D443T6vqgt.clear();
        this.E4emISQ55E8d.clear();
        KekMine.YnQ4ChsDR.OIExXGL6BNv.clear();
        this.L8HTTT = null;
        if (PathingHelper.LcPVM4w5KCoKSxGs() && ((Boolean)this.pathfind.get()).booleanValue()) {
            PathingHelper.xRVyNRV3cB7();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre pre) {
        if (KekNuker.lVls3aWwqcm7.player == null || KekNuker.lVls3aWwqcm7.world == null) {
            return;
        }
        if (HighwayBuilder.S7TLszvzENsW7()) {
            return;
        }
        ++this.vV6cbpE7KWBI;
        TZa5O0xAoIaC.entrySet().removeIf(entry -> {
            if (KekNuker.lVls3aWwqcm7.world.getBlockState((BlockPos)entry.getKey()).isAir()) {
                return true;
            }
            return this.vV6cbpE7KWBI - (Integer)entry.getValue() > (Integer)MusheorSystem.Manager.rebreakTimeout.get();
        });
        Module module = Modules.get().get(KekMine.class);
        if (this.nukerMode.get() == NukerMode.KrscIKJwTdc) {
            gaDbi5D443T6vqgt.clear();
            gaDbi5D443T6vqgt = this.jOdDDFXSeWl4((Shape)((Object)this.shape.get()), (Double)this.range.get());
        } else if (this.nukerMode.get() == NukerMode.kAT8CfWuaGb) {
            if (!LitematicaHelper.isLoaded()) {
                this.warning("Litematica is not installed. Install Litematica or switch to another mode.", new Object[0]);
                this.toggle();
                return;
            }
            gaDbi5D443T6vqgt.clear();
            gaDbi5D443T6vqgt.addAll(LitematicaHelper.get().getWrongSchematicBlocks((Double)this.range.get(), (Boolean)this.ignoreAir.get()));
        } else if (this.nukerMode.get() == NukerMode.uUngom5N) {
            gaDbi5D443T6vqgt.clear();
            gaDbi5D443T6vqgt.addAll(this.UJeG0VvjslB());
        }
        this.E4emISQ55E8d.clear();
        for (BlockPos BlockPos3 : gaDbi5D443T6vqgt) {
            BlockState BlockState2 = KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos3);
            if (this.nukerMode.get() != NukerMode.h9MViB && ((Boolean)this.flatten.get()).booleanValue() && BlockPos3.getY() < KekNuker.lVls3aWwqcm7.player.getBlockPos().getY() || BlockState2.method_51176() || BlockState2.isAir() || !BlockUtils.canBreak((BlockPos)BlockPos3) || ((Boolean)this.avoidSpillingLiquid.get()).booleanValue() && WorldUtils.CEOjBr5G5R(BlockPos3) || !WorldUtils.jOdDDFXSeWl4(BlockPos3, (Double)this.range.get()) || this.listMode.get() != ListMode.m9okHiGFumA && (this.listMode.get() == ListMode.LtMW6lcKPv && !((List)this.whitelist.get()).contains(BlockState2.getBlock()) || this.listMode.get() == ListMode.fXEQFU && ((List)this.blacklist.get()).contains(BlockState2.getBlock()))) continue;
            this.E4emISQ55E8d.add(BlockPos3);
        }
        BlockPos BlockPos4 = (SortMode)((Object)this.sortMode.get());
        int n = 0;
        switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"Closest", "Furthest", "TopDown", "BottomUp"}, BlockPos4, n)) {
            case 0: {
                this.E4emISQ55E8d.sort(Comparator.comparingDouble(BlockPos2 -> KekNuker.lVls3aWwqcm7.player.method_5707(Vec3d.ofCenter((BlockPos)BlockPos2))));
                break;
            }
            case 1: {
                this.E4emISQ55E8d.sort(Comparator.comparingDouble(BlockPos2 -> -KekNuker.lVls3aWwqcm7.player.method_5707(Vec3d.ofCenter((BlockPos)BlockPos2))));
                break;
            }
            case 2: {
                this.E4emISQ55E8d.sort(Comparator.comparingDouble(BlockPos2 -> -BlockPos2.getY()));
                break;
            }
            case 3: {
                this.E4emISQ55E8d.sort(Comparator.comparingDouble(BlockPos::method_10264));
                break;
            }
        }
        if (this.E4emISQ55E8d.isEmpty() && ((Boolean)this.pathfind.get()).booleanValue() && this.nukerMode.get() != NukerMode.h9MViB && (BlockPos4 = this.fBdtt0EoATtS()) != null && !BlockPos4.equals((Object)this.L8HTTT)) {
            this.L8HTTT = BlockPos4;
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalNear(new BlockPos(BlockPos4.getX(), KekNuker.lVls3aWwqcm7.player.getY(), BlockPos4.getZ()), 1));
        }
        if (!this.E4emISQ55E8d.isEmpty()) {
            int n2 = 0;
            for (BlockPos BlockPos3 : this.E4emISQ55E8d) {
                BlockState BlockState2 = KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos3);
                if ((double)HudInfoPlus.XuSVOP3J5xFv() >= (double)((Integer)MusheorSystem.Manager.globalPacketLimit.get()).intValue() * 0.9 && ((Boolean)this.noPacketKick.get()).booleanValue()) break;
                if (TZa5O0xAoIaC.containsKey(BlockPos3)) continue;
                KekMine.MineContext mineContext = new KekMine.MineContext(BlockPos3, KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos3), true);
                if (module.isActive()) {
                    if (!KekMine.YnQ4ChsDR.og2KVvNzA()) {
                        if (mineContext.E74ay1CfIa1C1X6 || mineContext.yIXEDGFGtS9H) {
                            KekMine.e5oi2ZF(BlockPos3);
                            ++n2;
                        } else {
                            KekMine.YnQ4ChsDR.TAdu5cndwWu3A1(BlockPos3, BlockState2);
                        }
                        TZa5O0xAoIaC.put(BlockPos3, this.vV6cbpE7KWBI);
                    }
                } else {
                    BlockUtils.breakBlock((BlockPos)BlockPos3, (boolean)true);
                    ++n2;
                    if (!BlockUtils.canInstaBreak((BlockPos)BlockPos3)) break;
                }
                if (n2 < (Integer)this.bpt.get()) continue;
                break;
            }
        }
    }

    private List<BlockPos> jOdDDFXSeWl4(Shape shape, double d) {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        Vec3d Vec3d2 = KekNuker.lVls3aWwqcm7.player.method_33571();
        int n = (int)Math.ceil(d);
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos BlockPos2 = BlockPos.method_49637((double)(Vec3d2.x + (double)i), (double)(Vec3d2.y + (double)j), (double)(Vec3d2.z + (double)k));
                    if (!BlockUtils.canBreak((BlockPos)BlockPos2)) continue;
                    if (shape == Shape.eXDG63e) {
                        if (!(Vec3d2.method_1022(Vec3d.method_24953((BlockPos)BlockPos2)) <= d)) continue;
                        arrayList.add(BlockPos2);
                        continue;
                    }
                    if (shape != Shape.zJG6cArZPynMblA) continue;
                    arrayList.add(BlockPos2);
                }
            }
        }
        return arrayList;
    }

    private BlockPos fBdtt0EoATtS() {
        if (KekNuker.lVls3aWwqcm7.player == null || KekNuker.lVls3aWwqcm7.world == null) {
            return null;
        }
        double d = KekNuker.lVls3aWwqcm7.player.getX();
        double d2 = KekNuker.lVls3aWwqcm7.player.getZ();
        int n = KekNuker.lVls3aWwqcm7.player.getBlockPos().getY();
        int n2 = n + (int)Math.floor((Double)this.range.get());
        int n3 = KekNuker.lVls3aWwqcm7.player.method_31476().x;
        int n4 = KekNuker.lVls3aWwqcm7.player.method_31476().z;
        int n5 = (Integer)this.pathfindChunkRange.get();
        List<BlockPos[]> list = null;
        ISelection[] iSelectionArray = null;
        if (this.nukerMode.get() == NukerMode.kAT8CfWuaGb && LitematicaHelper.isLoaded()) {
            list = LitematicaHelper.get().getSchematicRegionBounds();
        } else if (this.nukerMode.get() == NukerMode.uUngom5N) {
            iSelectionArray = BaritoneAPI.getProvider().getPrimaryBaritone().getSelectionManager().getSelections();
        }
        ArrayList<int[]> arrayList = new ArrayList<int[]>();
        for (int i = -n5; i <= n5; ++i) {
            int n6 = -n5;
            while (n6 <= n5) {
                arrayList.add(new int[]{i, n6++});
            }
        }
        arrayList.sort(Comparator.comparingDouble(nArray -> {
            double d3 = (double)(n3 + nArray[0] << 4) + 8.0;
            double d4 = (double)(n4 + nArray[1] << 4) + 8.0;
            return (d3 - d) * (d3 - d) + (d4 - d2) * (d4 - d2);
        }));
        for (int[] nArray2 : arrayList) {
            int n7 = n3 + nArray2[0] << 4;
            int n8 = n4 + nArray2[1] << 4;
            BlockPos BlockPos2 = null;
            double d3 = Double.MAX_VALUE;
            for (int i = n7; i < n7 + 16; ++i) {
                for (int j = n8; j < n8 + 16; ++j) {
                    for (int k = n; k < n2; ++k) {
                        double d4;
                        double d5;
                        double d6;
                        BlockPos BlockPos3 = new BlockPos(i, k, j);
                        BlockPos BlockPos4 = new BlockPos(i, KekNuker.lVls3aWwqcm7.player.getBlockPos().getY() - 1, j);
                        BlockState BlockState2 = KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos3);
                        if (KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos4).isAir() || BlockState2.method_51176() || BlockState2.isAir() || ((Boolean)this.avoidSpillingLiquid.get()).booleanValue() && WorldUtils.CEOjBr5G5R(BlockPos3) || !BlockUtils.canBreak((BlockPos)BlockPos3) || this.listMode.get() == ListMode.LtMW6lcKPv && !((List)this.whitelist.get()).contains(BlockState2.getBlock()) || this.listMode.get() == ListMode.fXEQFU && ((List)this.blacklist.get()).contains(BlockState2.getBlock()) || list != null && !KekNuker.jOdDDFXSeWl4(BlockPos3, list) || iSelectionArray != null && !KekNuker.jOdDDFXSeWl4(BlockPos3, iSelectionArray) || !((d6 = (d5 = (double)i + 0.5 - d) * d5 + (d4 = (double)j + 0.5 - d2) * d4) < d3)) continue;
                        d3 = d6;
                        BlockPos2 = BlockPos3;
                    }
                }
            }
            if (BlockPos2 == null) continue;
            return BlockPos2;
        }
        return null;
    }

    private static boolean jOdDDFXSeWl4(BlockPos BlockPos2, List<BlockPos[]> list) {
        for (BlockPos[] BlockPosArray : list) {
            if (BlockPos2.getX() < BlockPosArray[0].getX() || BlockPos2.getX() > BlockPosArray[1].getX() || BlockPos2.getY() < BlockPosArray[0].getY() || BlockPos2.getY() > BlockPosArray[1].getY() || BlockPos2.getZ() < BlockPosArray[0].getZ() || BlockPos2.getZ() > BlockPosArray[1].getZ()) continue;
            return true;
        }
        return false;
    }

    private static boolean jOdDDFXSeWl4(BlockPos BlockPos2, ISelection[] iSelectionArray) {
        for (ISelection iSelection : iSelectionArray) {
            BetterBlockPos betterBlockPos = iSelection.min();
            BetterBlockPos betterBlockPos2 = iSelection.max();
            if (BlockPos2.getX() < betterBlockPos.getX() || BlockPos2.getX() > betterBlockPos2.getX() || BlockPos2.getY() < betterBlockPos.getY() || BlockPos2.getY() > betterBlockPos2.getY() || BlockPos2.getZ() < betterBlockPos.getZ() || BlockPos2.getZ() > betterBlockPos2.getZ()) continue;
            return true;
        }
        return false;
    }

    private List<BlockPos> UJeG0VvjslB() {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        if (KekNuker.lVls3aWwqcm7.player == null || KekNuker.lVls3aWwqcm7.world == null) {
            return arrayList;
        }
        IBaritone iBaritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        ISelectionManager iSelectionManager = iBaritone.getSelectionManager();
        if (iSelectionManager.getSelections() == null) {
            return arrayList;
        }
        for (ISelection iSelection : iSelectionManager.getSelections()) {
            BetterBlockPos betterBlockPos = iSelection.min();
            BetterBlockPos betterBlockPos2 = iSelection.max();
            int n = Math.min(betterBlockPos.getX(), betterBlockPos2.getX());
            int n2 = Math.min(betterBlockPos.getY(), betterBlockPos2.getY());
            int n3 = Math.min(betterBlockPos.getZ(), betterBlockPos2.getZ());
            int n4 = Math.max(betterBlockPos.getX(), betterBlockPos2.getX());
            int n5 = Math.max(betterBlockPos.getY(), betterBlockPos2.getY());
            int n6 = Math.max(betterBlockPos.getZ(), betterBlockPos2.getZ());
            int n7 = KekNuker.lVls3aWwqcm7.player.getX();
            int n8 = KekNuker.lVls3aWwqcm7.player.getY();
            int n9 = KekNuker.lVls3aWwqcm7.player.getZ();
            int n10 = Math.max(n, n7 - 12);
            int n11 = Math.max(n2, n8 - 12);
            int n12 = Math.max(n3, n9 - 12);
            int n13 = Math.min(n4, n7 + 12);
            int n14 = Math.min(n5, n8 + 12);
            int n15 = Math.min(n6, n9 + 12);
            if (n10 > n13 || n11 > n14 || n12 > n15) continue;
            for (int i = n10; i <= n13; ++i) {
                for (int j = n11; j <= n14; ++j) {
                    for (int k = n12; k <= n15; ++k) {
                        BlockPos BlockPos2 = new BlockPos(i, j, k);
                        if (KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos2).isAir() || KekNuker.lVls3aWwqcm7.world.getBlockState(BlockPos2).method_51176() || arrayList.contains(BlockPos2) || !WorldUtils.jOdDDFXSeWl4(BlockPos2, (Double)this.range.get())) continue;
                        arrayList.add(BlockPos2);
                    }
                }
            }
        }
        return arrayList;
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        if (KekNuker.lVls3aWwqcm7.player == null || KekNuker.lVls3aWwqcm7.world == null) {
            return;
        }
        if (((Boolean)this.globalRendering.get()).booleanValue()) {
            RenderUtils.mp3zoXQFKUKYj5(render3DEvent, this.E4emISQ55E8d);
        } else {
            RenderUtils.jOdDDFXSeWl4(render3DEvent, this.E4emISQ55E8d, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
        }
    }

    static {
        gaDbi5D443T6vqgt = new ArrayList<BlockPos>();
        TZa5O0xAoIaC = new HashMap<BlockPos, Integer>();
    }

    public static final class NukerMode
    extends Enum<NukerMode> {
        public static final /* enum */ NukerMode KrscIKJwTdc = new NukerMode();
        public static final /* enum */ NukerMode h9MViB = new NukerMode();
        public static final /* enum */ NukerMode kAT8CfWuaGb = new NukerMode();
        public static final /* enum */ NukerMode uUngom5N = new NukerMode();
        private static final /* synthetic */ NukerMode[] V9ZG3sNvd2tk4bPq;

        public static NukerMode[] values() {
            return (NukerMode[])V9ZG3sNvd2tk4bPq.clone();
        }

        public static NukerMode valueOf(String string) {
            return Enum.valueOf(NukerMode.class, string);
        }

        private static /* synthetic */ NukerMode[] bjcXSkBmj0OjqAf() {
            return new NukerMode[]{KrscIKJwTdc, h9MViB, kAT8CfWuaGb, uUngom5N};
        }

        static {
            V9ZG3sNvd2tk4bPq = NukerMode.bjcXSkBmj0OjqAf();
        }
    }

    public static final class Shape
    extends Enum<Shape> {
        public static final /* enum */ Shape zJG6cArZPynMblA = new Shape();
        public static final /* enum */ Shape eXDG63e = new Shape();
        private static final /* synthetic */ Shape[] NUDC7Q4AxeSjEhHt;

        public static Shape[] values() {
            return (Shape[])NUDC7Q4AxeSjEhHt.clone();
        }

        public static Shape valueOf(String string) {
            return Enum.valueOf(Shape.class, string);
        }

        private static /* synthetic */ Shape[] EXmTbeRB() {
            return new Shape[]{zJG6cArZPynMblA, eXDG63e};
        }

        static {
            NUDC7Q4AxeSjEhHt = Shape.EXmTbeRB();
        }
    }

    public static final class SortMode
    extends Enum<SortMode> {
        public static final /* enum */ SortMode zSBNh5WZFCR = new SortMode();
        public static final /* enum */ SortMode QhaZQGwqdeFQp = new SortMode();
        public static final /* enum */ SortMode xRVyNRV3cB7 = new SortMode();
        public static final /* enum */ SortMode wyf4MqVc0fll = new SortMode();
        public static final /* enum */ SortMode zOg3JjefgZ = new SortMode();
        private static final /* synthetic */ SortMode[] JZDYaLhvUPKPZH;

        public static SortMode[] values() {
            return (SortMode[])JZDYaLhvUPKPZH.clone();
        }

        public static SortMode valueOf(String string) {
            return Enum.valueOf(SortMode.class, string);
        }

        private static /* synthetic */ SortMode[] i09vexi4j0xa7Kn() {
            return new SortMode[]{zSBNh5WZFCR, QhaZQGwqdeFQp, xRVyNRV3cB7, wyf4MqVc0fll, zOg3JjefgZ};
        }

        static {
            JZDYaLhvUPKPZH = SortMode.i09vexi4j0xa7Kn();
        }
    }

    public static final class ListMode
    extends Enum<ListMode> {
        public static final /* enum */ ListMode m9okHiGFumA = new ListMode();
        public static final /* enum */ ListMode LtMW6lcKPv = new ListMode();
        public static final /* enum */ ListMode fXEQFU = new ListMode();
        private static final /* synthetic */ ListMode[] XydsV5qWOWTk7gA;

        public static ListMode[] values() {
            return (ListMode[])XydsV5qWOWTk7gA.clone();
        }

        public static ListMode valueOf(String string) {
            return Enum.valueOf(ListMode.class, string);
        }

        private static /* synthetic */ ListMode[] KMpX0B() {
            return new ListMode[]{m9okHiGFumA, LtMW6lcKPv, fXEQFU};
        }

        static {
            XydsV5qWOWTk7gA = ListMode.KMpX0B();
        }
    }
}

