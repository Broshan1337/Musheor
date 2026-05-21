// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.runtime.ObjectMethods;
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
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BlockSetting;
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
import musheor.commands.RestockConfig;
import musheor.compat.LitematicaHelper;
import musheor.compat.VersionHelper;
import musheor.modules.automation.Refill;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.InteractionHand;
import net.minecraft.PlayerEntity;
import net.minecraft.Text;
import net.minecraft.ClientPlayerEntity;
import net.minecraft.class_1750;
import net.minecraft.ItemStack;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.ChunkPos;
import net.minecraft.class_1935;
import net.minecraft.DimensionType;
import net.minecraft.FluidBlock;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.class_2346;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.class_2457;
import net.minecraft.BlockState;
import net.minecraft.class_2741;
import net.minecraft.class_2754;
import net.minecraft.class_2769;
import net.minecraft.MinecraftClient;
import net.minecraft.SoundEvents;
import net.minecraft.class_3749;
import net.minecraft.Screen;
import net.minecraft.Registries;

public class Printer
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlacement;
    private final SettingGroup sgRender;
    private final SettingGroup sgLitematica;
    private final MinecraftClient rBGedpmjQyZ;
    private final Long2ObjectMap<Map<BlockPos, BlockState>> mR2Jt8P;
    private final List<Pair<BlockPos, BlockState>> ISyBYC52zF;
    private final List<Pair<BlockPos, BlockState>> bhy0Ddon9H6;
    private int vV6cbpE7KWBI;
    private int OyaWN2jsET;
    private int Pg9t6rCsTkuc;
    private int YCvJj8imMAxxu;
    private int NwHqgBmOLP;
    private boolean aP5dDWz;
    private int Tr234Br;
    private final Set<BlockPos> m9RUHINs8;
    private ItemStack pyVwRgYkI;
    private final List<ItemStack> X6N4Qf2Uc;
    private boolean xs9d08DSpSt;
    private int JxUbzYJNdp9UvTN;
    private PlacementStrategy dzkD9N;
    private Map<Block, Integer> gJZa6Zx1Rzm;
    private BlockPos n70VJ4CE5nwNu;
    private int gfosOAUCOp8Yq;
    private boolean gKa5NJsmT;
    private static final Set<class_2769<?>> TF0ZUa0QN41EJWaC = new HashSet<class_2754>(Arrays.asList(class_2741.field_12525, class_2741.field_12481, class_2741.field_12518, class_2741.field_12485, class_2741.field_12496, class_2741.field_12545));
    private static final Direction[] hXpkL9u = new Direction[]{Direction.field_11043, Direction.field_11035, Direction.field_11034, Direction.field_11039};
    private static final float[] oosx8z2R = new float[]{-89.0f, 0.0f, 89.0f};
    private static final double[] r0hCSR0 = new double[]{0.25, 0.75};
    private final Map<BlockState, PlacementStrategy> Z8PfWilTZRV;
    private static Method cjuOUcp2TVL3 = null;
    private final Setting<SelectionType> selectionType;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Boolean> pauseOnAura;
    private final Setting<List<Block>> ignoredBlocks;
    private final Setting<LayerType> layerType;
    private final Setting<Boolean> onlyAir;
    private final Setting<Boolean> gravityCheck;
    private final Setting<Boolean> ignoreRotations;
    private final Setting<Boolean> forceRotate;
    private final Setting<Integer> rotationDelay;
    private final Setting<Block> block;
    private final Setting<Boolean> pathfind;
    private final Setting<RestockType> restockType;
    private final Setting<Boolean> finishedSound;
    private final Setting<Integer> pathfindRescanInterval;
    private final Setting<Integer> renderRadius;
    private final Setting<String> schematicName;
    private final Setting<BlockPos> schematicPos;

    private static BlockState jOdDDFXSeWl4(Block Block2, class_1750 class_17502) {
        try {
            if (cjuOUcp2TVL3 == null) {
                Object object = Block.class.getMethods();
                int n = ((Method[])object).length;
                for (int i = 0; i < n; ++i) {
                    Method method = object[i];
                    if (method.getParameterCount() != 1 || !BlockState.class.isAssignableFrom(method.getReturnType()) || !method.getParameterTypes()[0].isAssignableFrom(class_1750.class)) continue;
                    cjuOUcp2TVL3 = method;
                    break;
                }
                if (cjuOUcp2TVL3 == null) {
                    block3: for (object = Block.class; object != null && object != Object.class; object = ((Class)object).getSuperclass()) {
                        for (Method method : ((Class)object).getDeclaredMethods()) {
                            if (method.getParameterCount() != 1 || !BlockState.class.isAssignableFrom(method.getReturnType()) || !method.getParameterTypes()[0].isAssignableFrom(class_1750.class)) continue;
                            method.setAccessible(true);
                            cjuOUcp2TVL3 = method;
                            break block3;
                        }
                    }
                }
            }
            return cjuOUcp2TVL3 != null ? (BlockState)cjuOUcp2TVL3.invoke((Object)Block2, class_17502) : null;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Printer() {
        super(musheor.AUTOMATION, "kek-printer", "Places blocks from schematics or fills baritone selections pretty damn fast");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlacement = this.settings.createGroup("Placement");
        this.sgRender = this.settings.createGroup("Render");
        this.sgLitematica = this.settings.createGroup("Litematica");
        this.rBGedpmjQyZ = MinecraftClient.getInstance();
        this.mR2Jt8P = new Long2ObjectOpenHashMap();
        this.ISyBYC52zF = new ArrayList<Pair<BlockPos, BlockState>>();
        this.bhy0Ddon9H6 = new ArrayList<Pair<BlockPos, BlockState>>();
        this.OyaWN2jsET = 0;
        this.Pg9t6rCsTkuc = 0;
        this.YCvJj8imMAxxu = 0;
        this.NwHqgBmOLP = 0;
        this.m9RUHINs8 = new HashSet<BlockPos>();
        this.pyVwRgYkI = null;
        this.X6N4Qf2Uc = new ArrayList<ItemStack>();
        this.xs9d08DSpSt = false;
        this.JxUbzYJNdp9UvTN = 0;
        this.dzkD9N = null;
        this.gJZa6Zx1Rzm = new HashMap<Block, Integer>();
        this.n70VJ4CE5nwNu = null;
        this.gfosOAUCOp8Yq = 0;
        this.gKa5NJsmT = false;
        this.Z8PfWilTZRV = new HashMap<BlockState, PlacementStrategy>();
        this.selectionType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("selection-type")).description("Get selection from litematica or baritone")).defaultValue((Object)SelectionType.sdcDUaa)).build());
        this.pauseOnEat = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-when-eating")).description("Pauses the printing process when the player eats (only when using meteor's auto-eat)")).defaultValue((Object)true)).build());
        this.pauseOnAura = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-auta")).description("Pauses the printing process when the player is killing mobs using killaura")).defaultValue((Object)true)).build());
        this.ignoredBlocks = this.sgPlacement.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("ignored-block-list")).description("Avoids placing blocks configured in this list")).defaultValue(new Block[0]).build());
        this.layerType = this.sgPlacement.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("layer-type")).description("Choose how layering is handled when printing")).defaultValue((Object)LayerType.byVifkEYgkzY1E)).build());
        this.onlyAir = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-air")).description("Places blocks only when the desired location is an air block (meaning it doesn't replace non-solid blocks)")).defaultValue((Object)true)).build());
        this.gravityCheck = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gravity-check")).description("Skip gravity-affected blocks (sand, gravel, etc.) if there is no solid block below them to prevent unwanted falling.")).defaultValue((Object)true)).build());
        this.ignoreRotations = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-rotations")).description("Fully ignores the rotation of the player and blocks when placing them")).defaultValue((Object)false)).build());
        this.forceRotate = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("force-rotate")).description("Automatically rotate the camera to the correct direction when placing directional blocks.")).defaultValue((Object)false)).build());
        this.rotationDelay = this.sgPlacement.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rotation-delay")).description("Ticks to wait after rotating before attempting placement (allows rotation to settle server-side).")).defaultValue((Object)2)).min(0).sliderMax(10).visible(() -> this.forceRotate.get())).build());
        this.block = this.sgPlacement.add((Setting)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)new BlockSetting.Builder().name("block")).description("What block to place (used for Baritone mode).")).defaultValue((Object)Blocks.field_10540)).visible(() -> this.selectionType.get() == SelectionType.ni1UVTBDGbU3)).build());
        this.pathfind = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-pathfinding")).description("Basically baritone building")).defaultValue((Object)true)).build());
        this.restockType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("auto-restock")).description("Automatically restock more materials from either configured containers or from shulkers you have on you")).defaultValue((Object)RestockType.Rd1eOmBQPxISFki)).visible(() -> this.pathfind.get())).build());
        this.finishedSound = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("play-sound")).description("Plays a sound when its finished building or failed")).defaultValue((Object)true)).visible(() -> this.pathfind.get())).build());
        this.pathfindRescanInterval = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pathfind-rescan-interval")).description("How many ticks between pathfinding goal rescans (only in Litematica mode).")).defaultValue((Object)20)).min(1).sliderMax(100).visible(() -> (Boolean)this.pathfind.get() != false && this.selectionType.get() == SelectionType.sdcDUaa)).build());
        this.renderRadius = this.sgRender.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-radius")).description("Only renders placeable blocks in a specified radius (lower if you experience FPS drops)")).defaultValue((Object)16)).sliderMin(2).sliderMax(128).visible(() -> MusheorSystem.Manager.placeRender.get())).build());
        this.schematicName = this.sgLitematica.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("schematic-name")).description("The name of the schematic you want to place")).visible(() -> LitematicaHelper.isLoaded() && this.selectionType.get() == SelectionType.sdcDUaa)).defaultValue((Object)"")).build());
        this.schematicPos = this.sgLitematica.add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("schematic-pos")).description("The position of the schematic you want to place")).visible(() -> LitematicaHelper.isLoaded() && this.selectionType.get() == SelectionType.sdcDUaa && this.xs9d08DSpSt)).defaultValue((Object)new BlockPos(0, 0, 0))).build());
    }

    public WWidget getWidget(GuiTheme guiTheme) {
        WVerticalList wVerticalList = guiTheme.verticalList();
        WButton wButton = (WButton)wVerticalList.add((WWidget)guiTheme.button("Load and place schematic")).widget();
        wButton.action = () -> {
            if (this.rBGedpmjQyZ.player == null || this.rBGedpmjQyZ.world == null) {
                return;
            }
            if (!LitematicaHelper.isLoaded()) {
                this.error("Litematica is not installed", new Object[0]);
                return;
            }
            String string = (String)this.schematicName.get();
            BlockPos BlockPos2 = (BlockPos)this.schematicPos.get();
            if (string.isEmpty()) {
                this.error("No schematic name specified", new Object[0]);
                return;
            }
            if (LitematicaHelper.get().ensureSchematicAt(string, BlockPos2)) {
                this.info("Loaded schematic: \u00a7b%s", new Object[]{string});
            }
        };
        WButton wButton2 = (WButton)wVerticalList.add((WWidget)guiTheme.button("Remove placement")).widget();
        wButton2.action = () -> {
            if (LitematicaHelper.isLoaded()) {
                LitematicaHelper.get().clearAllPlacements();
            }
        };
        WButton wButton3 = (WButton)wVerticalList.add((WWidget)guiTheme.button("Toggle BlockPos visibility")).widget();
        wButton3.action = () -> {
            this.xs9d08DSpSt = !this.xs9d08DSpSt;
            wButton3.set(this.xs9d08DSpSt ? "Hide Coordinates" : "Show Coordinates");
            this.schematicPos.onChanged();
        };
        return wVerticalList;
    }

    private void jOdDDFXSeWl4(BlockPos BlockPos2, BlockState BlockState2) {
        long l2 = ChunkPos.toLong((int)(BlockPos2.getX() >> 4), (int)(BlockPos2.getZ() >> 4));
        ((Map)this.mR2Jt8P.computeIfAbsent(l2, l -> new HashMap())).put(BlockPos2, BlockState2);
    }

    private void jOdDDFXSeWl4(ISelection iSelection) {
        if (iSelection == null || this.rBGedpmjQyZ.world == null) {
            return;
        }
        BetterBlockPos betterBlockPos = iSelection.min();
        BetterBlockPos betterBlockPos2 = iSelection.max();
        for (int i = betterBlockPos.getX(); i <= betterBlockPos2.getX(); ++i) {
            for (int j = betterBlockPos.getY(); j <= betterBlockPos2.getY(); ++j) {
                for (int k = betterBlockPos.getZ(); k <= betterBlockPos2.getZ(); ++k) {
                    BlockPos BlockPos2 = new BlockPos(i, j, k);
                    if ((Boolean)this.onlyAir.get() != false ? !(this.rBGedpmjQyZ.world.getBlockState(BlockPos2).getBlock() instanceof FluidBlock) : !BlockUtils.canPlace((BlockPos)BlockPos2, (boolean)false)) continue;
                    this.jOdDDFXSeWl4(BlockPos2, ((Block)this.block.get()).method_9564());
                }
            }
        }
    }

    private int dOw8Pbbaj() {
        return (int)Math.ceil((double)((Integer)this.renderRadius.get()).intValue() / 16.0);
    }

    public void onActivate() {
        this.mR2Jt8P.clear();
        this.X6N4Qf2Uc.clear();
        HighwayState.LmpuWjra().Os3dd8a().clear();
        this.vV6cbpE7KWBI = 0;
        this.YCvJj8imMAxxu = 0;
        this.JxUbzYJNdp9UvTN = 0;
        this.dzkD9N = null;
        this.Z8PfWilTZRV.clear();
        this.NwHqgBmOLP = 0;
        this.Tr234Br = 0;
        this.aP5dDWz = false;
        this.gJZa6Zx1Rzm = new HashMap<Block, Integer>();
        this.n70VJ4CE5nwNu = null;
        this.gKa5NJsmT = false;
        this.gfosOAUCOp8Yq = 0;
        if (this.selectionType.get() == SelectionType.ni1UVTBDGbU3) {
            IBaritone iBaritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            ISelectionManager iSelectionManager = iBaritone.getSelectionManager();
            if (iSelectionManager.getSelections() == null) {
                this.error("No Baritone selection found.", new Object[0]);
                this.toggle();
                return;
            }
            for (ISelection iSelection : iSelectionManager.getSelections()) {
                this.jOdDDFXSeWl4(iSelection);
            }
        }
        if (this.selectionType.get() == SelectionType.sdcDUaa) {
            if (!LitematicaHelper.isLoaded()) {
                this.error("Litematica is not installed. Install Litematica or switch to Baritone mode.", new Object[0]);
                this.toggle();
                return;
            }
            if (!LitematicaHelper.get().verifySchematic()) {
                this.error("Invalid or missing schematic", new Object[0]);
                this.toggle();
                return;
            }
            this.gJZa6Zx1Rzm = LitematicaHelper.get().getMaterialCounts((List)this.ignoredBlocks.get());
            int n = this.gJZa6Zx1Rzm.values().stream().mapToInt(Integer::intValue).sum();
            this.info("Schematic ready \u2014 \u00a7b" + n + "\u00a7r blocks to place", new Object[0]);
        }
    }

    public void onDeactivate() {
        musheor.utils.PlayerUtils.JaevRTUQKWIx5LQ();
        this.bhy0Ddon9H6.clear();
        this.gJZa6Zx1Rzm.clear();
        this.n70VJ4CE5nwNu = null;
        this.gKa5NJsmT = false;
        this.OyaWN2jsET = 0;
        this.YCvJj8imMAxxu = 0;
        this.Pg9t6rCsTkuc = 0;
    }

    /*
     * WARNING - void declaration
     */
    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        BlockState BlockState2;
        Object object;
        ItemStack ItemStack2;
        Pair<BlockPos, BlockState> pair222;
        int n;
        BlockPos BlockPos2;
        if (this.rBGedpmjQyZ.player == null || this.rBGedpmjQyZ.world == null) {
            return;
        }
        HighwayState highwayState = HighwayState.LmpuWjra();
        ++this.vV6cbpE7KWBI;
        highwayState.Os3dd8a().entrySet().removeIf(entry -> {
            BlockState BlockState2;
            if (this.selectionType.get() == SelectionType.ni1UVTBDGbU3 && (BlockState2 = this.vgrtgn5((BlockPos)entry.getKey())) != null && this.rBGedpmjQyZ.world.getBlockState((BlockPos)entry.getKey()).getBlock() == BlockState2.getBlock()) {
                return true;
            }
            return this.vV6cbpE7KWBI - (Integer)entry.getValue() > (Integer)MusheorSystem.Manager.placementTimeout.get();
        });
        if (((Boolean)this.pauseOnEat.get()).booleanValue() && ((AutoEat)Modules.get().get(AutoEat.class)).eating) {
            return;
        }
        if (((Boolean)this.pauseOnAura.get()).booleanValue() && ((KillAura)Modules.get().get(KillAura.class)).attacking) {
            return;
        }
        if (this.aP5dDWz) {
            this.rBGedpmjQyZ.field_1761.method_2906(this.rBGedpmjQyZ.player.field_7512.field_7763, this.rBGedpmjQyZ.player.getId().method_7376(), 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.rBGedpmjQyZ.player);
            this.aP5dDWz = false;
            VersionHelper.get().syncInventory();
        } else if (!this.rBGedpmjQyZ.player.field_7512.method_34255().setStack() || this.rBGedpmjQyZ.player.method_6079().getStack() != Items.field_8288 && ((AutoTotem)Modules.get().get(AutoTotem.class)).isActive()) {
            ++this.Tr234Br;
            if (this.Tr234Br >= 20) {
                this.aP5dDWz = true;
                this.Tr234Br = 0;
            }
        }
        this.ISyBYC52zF.clear();
        if (this.selectionType.get() == SelectionType.sdcDUaa) {
            BlockPos2 = this.rBGedpmjQyZ.player.getBlockPos();
            n = (int)Math.ceil((Double)MusheorSystem.Manager.placementRange.get()) + 1;
            pair222 = LitematicaHelper.get().getBlocksInBox(BlockPos2.method_10069(-n, -n, -n), BlockPos2.method_10069(n, n, n), (Boolean)this.onlyAir.get(), (List)this.ignoredBlocks.get(), 500);
            for (Map.Entry<BlockPos, BlockState> object22 : pair222.entrySet()) {
                BlockPos n3 = object22.getKey();
                BlockState i = object22.getValue();
                if (InventoryManager.usJLOV0subXO3(i.getBlock().asItem()) <= 0 || !BlockUtils.canPlace((BlockPos)n3, (boolean)true) || this.layerType.get() == LayerType.ZuBA2SJemxMpFD1 && (double)n3.getY() >= Math.floor(this.rBGedpmjQyZ.player.getY()) || this.layerType.get() == LayerType.gvp3bKzV && !LitematicaHelper.get().isPositionInRenderLayer(n3) || highwayState.Os3dd8a().containsKey(n3) || !WorldUtils.KDNrzlU9qtrEv(n3)) continue;
                this.ISyBYC52zF.add((Pair<BlockPos, BlockState>)Pair.of((Object)n3, (Object)i));
            }
            var6_12 = (Integer)this.renderRadius.get();
            Map<BlockPos, BlockState> l = LitematicaHelper.get().getBlocksInBox(BlockPos2.method_10069(-var6_12, -var6_12, -var6_12), BlockPos2.method_10069(var6_12, var6_12, var6_12), (Boolean)this.onlyAir.get(), (List)this.ignoredBlocks.get(), 2000);
            this.bhy0Ddon9H6.clear();
            for (Map.Entry entry2 : l.entrySet()) {
                if (InventoryManager.usJLOV0subXO3(((BlockState)entry2.getValue()).getBlock().asItem()) <= 0 || !WorldUtils.jOdDDFXSeWl4((BlockPos)entry2.getKey(), var6_12)) continue;
                this.bhy0Ddon9H6.add((Pair<BlockPos, BlockState>)Pair.of((Object)((BlockPos)entry2.getKey()), (Object)((BlockState)entry2.getValue())));
            }
        } else {
            this.bhy0Ddon9H6.clear();
            BlockPos2 = this.rBGedpmjQyZ.player.method_31476();
            n = this.dOw8Pbbaj();
            for (int i = BlockPos2.x - n; i <= BlockPos2.x + n; ++i) {
                for (var6_12 = BlockPos2.z - n; var6_12 <= BlockPos2.z + n; ++var6_12) {
                    long AbstractClientPlayerEntity = ChunkPos.toLong((int)i, (int)var6_12);
                    Map map = (Map)this.mR2Jt8P.get(AbstractClientPlayerEntity);
                    if (map == null || map.isEmpty()) continue;
                    ItemStack2 = map.entrySet().iterator();
                    while (ItemStack2.hasNext()) {
                        Map.Entry entry3 = ItemStack2.next();
                        object = (BlockPos)entry3.getKey();
                        BlockState BlockState3 = this.rBGedpmjQyZ.world.getBlockState((BlockPos)object);
                        BlockState2 = (BlockState)entry3.getValue();
                        if (BlockState3.getBlock() == BlockState2.getBlock()) {
                            ItemStack2.remove();
                            continue;
                        }
                        if (InventoryManager.usJLOV0subXO3(BlockState2.getBlock().asItem()) <= 0 || !BlockUtils.canPlace((BlockPos)object, (boolean)true) || this.layerType.get() == LayerType.ZuBA2SJemxMpFD1 && (double)object.getY() >= Math.floor(this.rBGedpmjQyZ.player.getY()) || this.layerType.get() == LayerType.gvp3bKzV && (!LitematicaHelper.isLoaded() || !LitematicaHelper.get().isPositionInRenderLayer((BlockPos)object)) || !(BlockState3.getBlock() instanceof FluidBlock) && ((Boolean)this.onlyAir.get()).booleanValue() || highwayState.Os3dd8a().containsKey(object)) continue;
                        if (WorldUtils.jOdDDFXSeWl4((BlockPos)object, ((Integer)this.renderRadius.get()).intValue())) {
                            this.bhy0Ddon9H6.add((Pair<BlockPos, BlockState>)Pair.of((Object)object, (Object)BlockState2));
                        }
                        if (!WorldUtils.KDNrzlU9qtrEv((BlockPos)object) || !this.ISyBYC52zF.stream().noneMatch(arg_0 -> Printer.jOdDDFXSeWl4((BlockPos)object, arg_0))) continue;
                        this.ISyBYC52zF.add((Pair<BlockPos, BlockState>)Pair.of((Object)object, (Object)BlockState2));
                    }
                    if (!map.isEmpty()) continue;
                    this.mR2Jt8P.remove(AbstractClientPlayerEntity);
                }
            }
        }
        this.ISyBYC52zF.sort(Comparator.comparingDouble(pair -> this.rBGedpmjQyZ.player.method_5649((double)((BlockPos)pair.first()).getX(), (double)((BlockPos)pair.first()).getY(), (double)((BlockPos)pair.first()).getZ())));
        if (this.restockType.get() != RestockType.Rd1eOmBQPxISFki && ((Boolean)this.pathfind.get()).booleanValue()) {
            BlockPos2 = this.v1nokUkHXYjAGxn();
            if (BlockPos2 == null) {
                n = (this.selectionType.get() == SelectionType.sdcDUaa ? this.ISyBYC52zF.isEmpty() : this.ISyBYC52zF.isEmpty() && this.mR2Jt8P.isEmpty()) ? 1 : 0;
                if (n != 0) {
                    this.info("All blocks placed!", new Object[0]);
                    if (((Boolean)this.finishedSound.get()).booleanValue()) {
                        VersionHelper.get().playSoundPlayer(SoundEvents.field_15195);
                    }
                    this.toggle();
                    return;
                }
                if (this.selectionType.get() == SelectionType.ni1UVTBDGbU3) {
                    this.info("Cannot reach or find more blocks to place", new Object[0]);
                    if (((Boolean)this.finishedSound.get()).booleanValue()) {
                        VersionHelper.get().playSoundPlayer(SoundEvents.field_15008);
                    }
                    this.toggle();
                    return;
                }
            }
            if (InventoryManager.usJLOV0subXO3(BlockPos2.asItem()) <= 0 && this.X6N4Qf2Uc.isEmpty()) {
                this.jOdDDFXSeWl4((Block)BlockPos2);
            }
            if (this.restockType.get() == RestockType.yF2JzAqyBTfec) {
                if (!this.X6N4Qf2Uc.isEmpty()) {
                    Refill refill;
                    if (PathingHelper.LcPVM4w5KCoKSxGs()) {
                        PathingHelper.xRVyNRV3cB7();
                    }
                    if ((refill = (Refill)Modules.get().get(Refill.class)).isActive()) {
                        return;
                    }
                    if (this.gKa5NJsmT) {
                        this.gKa5NJsmT = false;
                        this.X6N4Qf2Uc.clear();
                        this.OyaWN2jsET = 0;
                        return;
                    }
                    while (!this.X6N4Qf2Uc.isEmpty() && InventoryManager.ZbTtF5KYyGL9YXed(this.X6N4Qf2Uc.getFirst()) <= 0) {
                        this.X6N4Qf2Uc.removeFirst();
                    }
                    if (this.X6N4Qf2Uc.isEmpty()) {
                        this.info("No shulkers found for needed blocks, disabling...", new Object[0]);
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.field_15008);
                        }
                        this.toggle();
                        return;
                    }
                    if (this.OyaWN2jsET <= (Integer)InventoryManager.BOhrdyrKEar.lCQE4G.get()) {
                        ++this.OyaWN2jsET;
                        return;
                    }
                    refill.item.set((Object)this.X6N4Qf2Uc.getFirst());
                    refill.toggle();
                    this.gKa5NJsmT = true;
                    return;
                }
            } else if (!this.X6N4Qf2Uc.isEmpty()) {
                void var9_31;
                ItemStack ItemStack2 = this.X6N4Qf2Uc.getFirst();
                int n2 = this.mp3zoXQFKUKYj5(Block.method_9503((ItemStack)ItemStack2));
                if (this.pyVwRgYkI != ItemStack2) {
                    this.m9RUHINs8.clear();
                    this.pyVwRgYkI = ItemStack2;
                    this.NwHqgBmOLP = 0;
                }
                if (!RestockConfig.Gt56Sj4a6BWhgB(ItemStack2)) {
                    if (ItemStack2 == BlockPos2.asItem()) {
                        this.info("Cannot find container for %s, disabling...", new Object[]{RestockConfig.jOdDDFXSeWl4(ItemStack2)});
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.field_15008);
                        }
                        this.X6N4Qf2Uc.clear();
                        this.toggle();
                        return;
                    }
                    this.X6N4Qf2Uc.removeFirst();
                    return;
                }
                BlockPos BlockPos4 = this.VYEwzRq(ItemStack2);
                if (BlockPos4 == null) {
                    if (ItemStack2 == BlockPos2.asItem()) {
                        this.info("All containers for %s are empty, disabling...", new Object[]{RestockConfig.jOdDDFXSeWl4(ItemStack2)});
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.field_15008);
                        }
                        this.m9RUHINs8.clear();
                        this.X6N4Qf2Uc.clear();
                        this.toggle();
                        return;
                    }
                    this.X6N4Qf2Uc.removeFirst();
                    return;
                }
                this.info("Restocking %s", new Object[]{RestockConfig.jOdDDFXSeWl4(ItemStack2)});
                if (!WorldUtils.KDNrzlU9qtrEv(BlockPos4)) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalNear(BlockPos4, 2));
                    return;
                }
                musheor.utils.PlayerUtils.JaevRTUQKWIx5LQ();
                if (!InventoryManager.FeGlqzs7Rjvi() && this.Pg9t6rCsTkuc == 0) {
                    WorldUtils.MS1x7YGHjIg7eB(BlockPos4);
                    InventoryManager.LoFK6z05DRRnOV(BlockPos4);
                    this.Pg9t6rCsTkuc = 3;
                    return;
                }
                if (this.Pg9t6rCsTkuc > 0) {
                    --this.Pg9t6rCsTkuc;
                    return;
                }
                if (this.OyaWN2jsET <= (Integer)InventoryManager.BOhrdyrKEar.nqXWHiZIUs11V.get()) {
                    ++this.OyaWN2jsET;
                    return;
                }
                if (!RateController.OwcAnTXUsd()) {
                    return;
                }
                Text f = this.rBGedpmjQyZ.player.field_7512;
                int f2 = f.field_7761.size() - 36;
                boolean bl = false;
                while (++var9_31 < f2) {
                    ItemStack2 = f.method_7611((int)var9_31).method_7677();
                    if (ItemStack2.setStack()) continue;
                    if (ItemStack2.getStack() == ItemStack2) {
                        this.NwHqgBmOLP += ItemStack2.method_7947();
                        this.rBGedpmjQyZ.field_1761.method_2906(f.field_7763, (int)var9_31, 0, ClientPlayerEntity.field_7794, (PlayerEntity)this.rBGedpmjQyZ.player);
                    }
                    if (this.NwHqgBmOLP >= n2 || InventoryManager.ZeOLrA() <= 0 || !RateController.OwcAnTXUsd()) break;
                }
                if (this.NwHqgBmOLP == 0) {
                    this.m9RUHINs8.add(BlockPos4);
                } else if (this.NwHqgBmOLP < n2 && InventoryManager.ZeOLrA() > 0) {
                    this.m9RUHINs8.add(BlockPos4);
                } else if (InventoryManager.ZeOLrA() > 0) {
                    this.m9RUHINs8.clear();
                    this.X6N4Qf2Uc.removeFirst();
                }
                this.OyaWN2jsET = 0;
                this.Pg9t6rCsTkuc = 0;
                this.rBGedpmjQyZ.player.method_3137();
                if (this.X6N4Qf2Uc.isEmpty() || InventoryManager.ZeOLrA() <= 0) {
                    this.X6N4Qf2Uc.clear();
                }
                return;
            }
        }
        if (((Boolean)this.pathfind.get()).booleanValue() && this.ISyBYC52zF.isEmpty()) {
            if (this.selectionType.get() == SelectionType.sdcDUaa) {
                ++this.gfosOAUCOp8Yq;
                if (this.n70VJ4CE5nwNu == null || this.gfosOAUCOp8Yq >= (Integer)this.pathfindRescanInterval.get() || WorldUtils.KDNrzlU9qtrEv(this.n70VJ4CE5nwNu)) {
                    this.gfosOAUCOp8Yq = 0;
                    this.n70VJ4CE5nwNu = LitematicaHelper.get().findClosestUnplacedBlock(this.rBGedpmjQyZ.player.getBlockPos(), 64, (Boolean)this.onlyAir.get(), (List)this.ignoredBlocks.get());
                    if (this.n70VJ4CE5nwNu == null) {
                        this.info("All blocks placed!", new Object[0]);
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.field_15195);
                        }
                        this.toggle();
                        return;
                    }
                }
                if (WorldUtils.Gt56Sj4a6BWhgB(this.rBGedpmjQyZ.player.getBlockPos(), this.n70VJ4CE5nwNu) > 2.5) {
                    PathingHelper.jOdDDFXSeWl4(new GoalNear(this.n70VJ4CE5nwNu, 2));
                    return;
                }
                musheor.utils.PlayerUtils.JaevRTUQKWIx5LQ();
            } else if (!this.mR2Jt8P.isEmpty() && (BlockPos2 = this.jOdDDFXSeWl4(this.mR2Jt8P)) != null) {
                if (WorldUtils.Gt56Sj4a6BWhgB(this.rBGedpmjQyZ.player.getBlockPos(), BlockPos2) > 2.5) {
                    PathingHelper.jOdDDFXSeWl4(new GoalNear(BlockPos2, 2));
                    return;
                }
                musheor.utils.PlayerUtils.JaevRTUQKWIx5LQ();
                if (this.rBGedpmjQyZ.player.getBlockPos() == BlockPos2) {
                    PathingHelper.jOdDDFXSeWl4((Goal)new GoalBlock(BlockPos2.method_10078()));
                }
            }
        }
        if (this.YCvJj8imMAxxu > 0) {
            --this.YCvJj8imMAxxu;
            return;
        }
        if (!this.ISyBYC52zF.isEmpty()) {
            BlockPos2 = null;
            for (Pair<BlockPos, BlockState> pair222 : this.ISyBYC52zF) {
                if (!this.mp3zoXQFKUKYj5((BlockPos)pair222.first(), (BlockState)pair222.second())) continue;
                BlockPos2 = pair222;
                break;
            }
            if (BlockPos2 != null) {
                boolean bl;
                BlockState BlockState4 = (BlockState)BlockPos2.second();
                pair222 = BlockState4.getBlock();
                PlacementStrategy placementStrategy = this.mp3zoXQFKUKYj5(BlockState4);
                if (this.JxUbzYJNdp9UvTN > 0) {
                    --this.JxUbzYJNdp9UvTN;
                    if (((Boolean)this.forceRotate.get()).booleanValue() && this.dzkD9N != null) {
                        float placementStrategy2 = this.dzkD9N.ThlHLXv3gtRxWy() != null ? this.jOdDDFXSeWl4(this.dzkD9N.ThlHLXv3gtRxWy()) : this.rBGedpmjQyZ.player.method_36454();
                        float bl2 = this.dzkD9N.gaDbi5D443T6vqgt() != null ? this.mp3zoXQFKUKYj5(this.dzkD9N.gaDbi5D443T6vqgt()) : this.rBGedpmjQyZ.player.method_36455();
                        Rotations.rotate((double)placementStrategy2, (double)bl2);
                    }
                    return;
                }
                PlacementStrategy placementStrategy2 = this.dzkD9N;
                this.dzkD9N = null;
                boolean bl2 = placementStrategy2 != null;
                boolean bl3 = bl = (Boolean)this.ignoreRotations.get() == false && (placementStrategy.ThlHLXv3gtRxWy() != null || placementStrategy.gaDbi5D443T6vqgt() != null);
                if (bl) {
                    if (((Boolean)this.forceRotate.get()).booleanValue()) {
                        if (!bl2) {
                            float f = placementStrategy.ThlHLXv3gtRxWy() != null ? this.jOdDDFXSeWl4(placementStrategy.ThlHLXv3gtRxWy()) : this.rBGedpmjQyZ.player.method_36454();
                            float f2 = placementStrategy.gaDbi5D443T6vqgt() != null ? this.mp3zoXQFKUKYj5(placementStrategy.gaDbi5D443T6vqgt()) : this.rBGedpmjQyZ.player.method_36455();
                            Rotations.rotate((double)f, (double)f2);
                            this.dzkD9N = placementStrategy;
                            this.JxUbzYJNdp9UvTN = (Integer)this.rotationDelay.get();
                            return;
                        }
                    } else if (!this.jOdDDFXSeWl4(placementStrategy)) {
                        this.YCvJj8imMAxxu = (Integer)MusheorSystem.Manager.swapDelay.get();
                        return;
                    }
                }
                if (this.rBGedpmjQyZ.player.method_6047().getStack() != pair222.asItem()) {
                    InventoryManager.L5CF0C6jx0T17H4I(pair222.asItem());
                    this.YCvJj8imMAxxu = (Integer)MusheorSystem.Manager.swapDelay.get();
                    return;
                }
                WorldUtils.l3ot1CwoJ9CsS();
                for (Pair pair2 : this.ISyBYC52zF) {
                    boolean bl4;
                    if (!BlockUtils.canPlace((BlockPos)((BlockPos)pair2.first()), (boolean)true) || !PlayerUtils.isWithin((BlockPos)((BlockPos)pair2.first()), (double)((Double)MusheorSystem.Manager.placementRange.get())) || ((BlockState)pair2.second()).getBlock() != pair222 || !this.mp3zoXQFKUKYj5((BlockPos)pair2.first(), (BlockState)pair2.second())) continue;
                    object = this.mp3zoXQFKUKYj5((BlockState)pair2.second());
                    boolean bl5 = bl4 = (Boolean)this.ignoreRotations.get() == false && (((PlacementStrategy)object).ThlHLXv3gtRxWy() != null || ((PlacementStrategy)object).gaDbi5D443T6vqgt() != null);
                    if (bl4) {
                        boolean bl6;
                        if (((Boolean)this.forceRotate.get()).booleanValue() && bl2) {
                            bl6 = ((PlacementStrategy)object).ThlHLXv3gtRxWy() != placementStrategy2.ThlHLXv3gtRxWy() || ((PlacementStrategy)object).gaDbi5D443T6vqgt() != placementStrategy2.gaDbi5D443T6vqgt();
                        } else {
                            boolean bl7 = bl6 = !this.jOdDDFXSeWl4((PlacementStrategy)object);
                        }
                        if (bl6) {
                            if (((Boolean)this.forceRotate.get()).booleanValue()) break;
                            this.YCvJj8imMAxxu = (Integer)MusheorSystem.Manager.swapDelay.get();
                            break;
                        }
                    }
                    if (!RateController.qy8UwM99rVr()) break;
                    BlockState2 = new Vec3d((double)((BlockPos)pair2.first()).getX() + 0.5 + (double)((PlacementStrategy)object).Lm4xX5QT0OxvyV().method_10148() * 0.5, (double)((BlockPos)pair2.first()).getY() + ((PlacementStrategy)object).lVls3aWwqcm7(), (double)((BlockPos)pair2.first()).getZ() + 0.5 + (double)((PlacementStrategy)object).Lm4xX5QT0OxvyV().method_10165() * 0.5);
                    Screen Screen2 = new Screen((Vec3d)BlockState2, ((PlacementStrategy)object).Lm4xX5QT0OxvyV(), (BlockPos)pair2.first(), false);
                    WorldUtils.jOdDDFXSeWl4(InteractionHand.field_5810, Screen2);
                    if (((BlockState)pair2.second()).getBlock() == Blocks.field_10540) {
                        HighwayState.LmpuWjra().s6I5Zvj();
                    }
                    highwayState.Os3dd8a().put((BlockPos)pair2.first(), this.vV6cbpE7KWBI);
                }
                WorldUtils.l3ot1CwoJ9CsS();
            }
        }
    }

    private void jOdDDFXSeWl4(Block Block2) {
        this.X6N4Qf2Uc.clear();
        this.X6N4Qf2Uc.add(Block2.asItem());
        HashSet<ItemStack> hashSet = new HashSet<ItemStack>();
        hashSet.add(Block2.asItem());
        ArrayList<Map.Entry<ItemStack, Integer>> arrayList = new ArrayList<Map.Entry<ItemStack, Integer>>();
        if (this.selectionType.get() == SelectionType.sdcDUaa) {
            for (Map.Entry object : this.gJZa6Zx1Rzm.entrySet()) {
                ItemStack ItemStack2 = ((Block)object.getKey()).asItem();
                if (hashSet.contains(ItemStack2)) continue;
                hashSet.add(ItemStack2);
                if (InventoryManager.usJLOV0subXO3(ItemStack2) > 0 || !RestockConfig.Gt56Sj4a6BWhgB(ItemStack2)) continue;
                arrayList.add(Map.entry(ItemStack2, (Integer)object.getValue()));
            }
        } else {
            for (Map map : this.mR2Jt8P.values()) {
                for (BlockState BlockState2 : map.values()) {
                    ItemStack ItemStack3 = BlockState2.getBlock().asItem();
                    if (hashSet.contains(ItemStack3)) continue;
                    hashSet.add(ItemStack3);
                    if (InventoryManager.usJLOV0subXO3(ItemStack3) > 0 || !RestockConfig.Gt56Sj4a6BWhgB(ItemStack3)) continue;
                    arrayList.add(Map.entry(ItemStack3, this.mp3zoXQFKUKYj5(BlockState2.getBlock())));
                }
            }
        }
        arrayList.sort((entry, entry2) -> Integer.compare((Integer)entry2.getValue(), (Integer)entry.getValue()));
        for (Map.Entry entry3 : arrayList) {
            this.X6N4Qf2Uc.add((ItemStack)entry3.getKey());
        }
    }

    private BlockPos VYEwzRq(ItemStack ItemStack2) {
        List<BlockPos> list = RestockConfig.mp3zoXQFKUKYj5(ItemStack2);
        BlockPos BlockPos2 = this.rBGedpmjQyZ.player.getBlockPos();
        BlockPos BlockPos3 = null;
        double d = Double.MAX_VALUE;
        for (BlockPos BlockPos4 : list) {
            double d2;
            if (this.m9RUHINs8.contains(BlockPos4) || !((d2 = BlockPos2.method_10262((BlockPos)BlockPos4)) < d)) continue;
            d = d2;
            BlockPos3 = BlockPos4;
        }
        return BlockPos3;
    }

    private int mp3zoXQFKUKYj5(Block Block2) {
        if (this.selectionType.get() == SelectionType.sdcDUaa) {
            return this.gJZa6Zx1Rzm.getOrDefault(Block2, 0);
        }
        int n = 0;
        for (Map map : this.mR2Jt8P.values()) {
            for (BlockState BlockState2 : map.values()) {
                if (BlockState2.getBlock() != Block2) continue;
                ++n;
            }
        }
        return n;
    }

    private Block v1nokUkHXYjAGxn() {
        Object object;
        Object object2;
        ObjectIterator objectIterator = null;
        Object object3 = null;
        ObjectIterator objectIterator2 = null;
        Object object4 = null;
        if (this.selectionType.get() == SelectionType.sdcDUaa) {
            object2 = this.gJZa6Zx1Rzm.keySet();
        } else {
            object = new HashSet();
            for (Object object5 : this.mR2Jt8P.values()) {
                for (BlockState BlockState2 : object5.values()) {
                    object.add(BlockState2.getBlock());
                }
            }
            object2 = object;
        }
        object = object2.iterator();
        while (object.hasNext()) {
            Object object5;
            ObjectIterator objectIterator3 = (Block)object.next();
            object5 = Registries.BLOCK.getId((Object)objectIterator3).toString();
            if (objectIterator2 == null || ((String)object5).compareTo((String)object4) < 0) {
                objectIterator2 = objectIterator3;
                object4 = object5;
            }
            if (InventoryManager.usJLOV0subXO3(objectIterator3.asItem()) <= 0 || objectIterator != null && ((String)object5).compareTo((String)object3) >= 0) continue;
            objectIterator = objectIterator3;
            object3 = object5;
        }
        if (objectIterator != null) {
            return objectIterator;
        }
        if (objectIterator2 != null) {
            return objectIterator2;
        }
        return this.selectionType.get() == SelectionType.ni1UVTBDGbU3 ? (Block)this.block.get() : null;
    }

    private BlockState vgrtgn5(BlockPos BlockPos2) {
        long l = ChunkPos.toLong((int)(BlockPos2.getX() >> 4), (int)(BlockPos2.getZ() >> 4));
        Map map = (Map)this.mR2Jt8P.get(l);
        if (map == null) {
            return null;
        }
        return (BlockState)map.get(BlockPos2);
    }

    private PlacementStrategy mp3zoXQFKUKYj5(BlockState BlockState2) {
        return this.Z8PfWilTZRV.computeIfAbsent(BlockState2, this::Gt56Sj4a6BWhgB);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PlacementStrategy Gt56Sj4a6BWhgB(BlockState BlockState2) {
        if (this.rBGedpmjQyZ.player == null || this.rBGedpmjQyZ.world == null) {
            return new PlacementStrategy(Direction.field_11033, 0.5, null, null);
        }
        boolean bl = TF0ZUa0QN41EJWaC.stream().anyMatch(arg_0 -> ((BlockState)BlockState2).method_28498(arg_0));
        if (!bl) {
            return new PlacementStrategy(Direction.field_11033, 0.5, null, null);
        }
        ItemStack ItemStack2 = BlockState2.getBlock().asItem();
        if (ItemStack2 == Items.field_8162) {
            return new PlacementStrategy(Direction.field_11033, 0.5, null, null);
        }
        ItemStack ItemStack2 = new ItemStack((class_1935)ItemStack2);
        BlockPos BlockPos2 = this.rBGedpmjQyZ.player.getBlockPos();
        float f = this.rBGedpmjQyZ.player.method_36454();
        float f2 = this.rBGedpmjQyZ.player.method_36455();
        try {
            Screen Screen2;
            Vec3d Vec3d2;
            double[] dArray;
            for (Direction Direction2 : Direction.values()) {
                double[] dArray2;
                if (Direction2.method_10166() == Direction.class_2351.field_11052) {
                    double[] dArray3 = new double[1];
                    dArray2 = dArray3;
                    dArray3[0] = Direction2 == Direction.field_11036 ? 1.0 : 0.0;
                } else {
                    dArray2 = r0hCSR0;
                }
                for (double d : dArray = dArray2) {
                    Vec3d2 = new Vec3d((double)BlockPos2.getX() + 0.5 + (double)Direction2.method_10148() * 0.5, (double)BlockPos2.getY() + d, (double)BlockPos2.getZ() + 0.5 + (double)Direction2.method_10165() * 0.5);
                    Screen2 = new Screen(Vec3d2, Direction2, BlockPos2, false);
                    boolean bl2 = false;
                    int n = 0;
                    block7: for (Direction Direction3 : hXpkL9u) {
                        this.rBGedpmjQyZ.player.method_36456(this.jOdDDFXSeWl4(Direction3));
                        for (float BlockState4 : oosx8z2R) {
                            this.rBGedpmjQyZ.player.method_36457(BlockState4);
                            class_1750 bl3 = new class_1750(this, (DimensionType)this.rBGedpmjQyZ.world, (PlayerEntity)this.rBGedpmjQyZ.player, InteractionHand.field_5808, ItemStack2, Screen2){};
                            BlockState bl4 = Printer.jOdDDFXSeWl4(BlockState2.getBlock(), bl3);
                            if (bl4 == null) continue;
                            if (this.jOdDDFXSeWl4(bl4, BlockState2)) {
                                bl2 = true;
                                continue;
                            }
                            n = 1;
                            break block7;
                        }
                    }
                    if (!bl2 || n != 0) continue;
                    PlacementStrategy placementStrategy = new PlacementStrategy(Direction2, d, null, null);
                    return placementStrategy;
                }
            }
            for (Direction Direction2 : Direction.values()) {
                double[] dArray4;
                if (Direction2.method_10166() == Direction.class_2351.field_11052) {
                    double[] dArray5 = new double[1];
                    dArray4 = dArray5;
                    dArray5[0] = Direction2 == Direction.field_11036 ? 1.0 : 0.0;
                } else {
                    dArray4 = r0hCSR0;
                }
                for (double d : dArray = dArray4) {
                    Vec3d2 = new Vec3d((double)BlockPos2.getX() + 0.5 + (double)Direction2.method_10148() * 0.5, (double)BlockPos2.getY() + d, (double)BlockPos2.getZ() + 0.5 + (double)Direction2.method_10165() * 0.5);
                    Screen2 = new Screen(Vec3d2, Direction2, BlockPos2, false);
                    for (Direction Direction4 : hXpkL9u) {
                        this.rBGedpmjQyZ.player.method_36456(this.jOdDDFXSeWl4(Direction4));
                        for (float f3 : oosx8z2R) {
                            Direction Direction5;
                            this.rBGedpmjQyZ.player.method_36457(f3);
                            class_1750 class_17502 = new class_1750(this, (DimensionType)this.rBGedpmjQyZ.world, (PlayerEntity)this.rBGedpmjQyZ.player, InteractionHand.field_5808, ItemStack2, Screen2){};
                            BlockState BlockState3 = Printer.jOdDDFXSeWl4(BlockState2.getBlock(), class_17502);
                            if (BlockState3 == null || !this.jOdDDFXSeWl4(BlockState3, BlockState2)) continue;
                            boolean bl2 = BlockState2.method_28498((class_2769)class_2741.field_12481) || BlockState2.method_28498((class_2769)class_2741.field_12525) && ((Direction)BlockState2.method_11654((class_2769)class_2741.field_12525)).method_10166().method_10179();
                            boolean bl3 = BlockState2.method_28498((class_2769)class_2741.field_12525) && ((Direction)BlockState2.method_11654((class_2769)class_2741.field_12525)).method_10166().method_10178();
                            Object object = Direction5 = bl2 ? Direction4 : null;
                            Direction Direction6 = bl3 ? (f3 < 0.0f ? Direction.field_11036 : Direction.field_11033) : null;
                            PlacementStrategy placementStrategy = new PlacementStrategy(Direction2, d, Direction5, Direction6);
                            return placementStrategy;
                        }
                    }
                }
            }
        }
        finally {
            this.rBGedpmjQyZ.player.method_36456(f);
            this.rBGedpmjQyZ.player.method_36457(f2);
        }
        return new PlacementStrategy(Direction.field_11033, 0.5, null, null);
    }

    private boolean jOdDDFXSeWl4(BlockState BlockState2, BlockState BlockState3) {
        if (BlockState2.getBlock() != BlockState3.getBlock()) {
            return false;
        }
        for (class_2769<?> class_27692 : TF0ZUa0QN41EJWaC) {
            if (!BlockState2.method_28498(class_27692) || !BlockState3.method_28498(class_27692) || BlockState2.method_11654(class_27692).equals(BlockState3.method_11654(class_27692))) continue;
            return false;
        }
        return true;
    }

    private boolean jOdDDFXSeWl4(PlacementStrategy placementStrategy) {
        if (this.rBGedpmjQyZ.player == null) {
            return false;
        }
        if (placementStrategy.ThlHLXv3gtRxWy() != null && this.rBGedpmjQyZ.player.method_58149() != placementStrategy.ThlHLXv3gtRxWy()) {
            return false;
        }
        if (placementStrategy.gaDbi5D443T6vqgt() != null) {
            float f = this.rBGedpmjQyZ.player.method_36455();
            if (placementStrategy.gaDbi5D443T6vqgt() == Direction.field_11036 && f >= -45.0f) {
                return false;
            }
            if (placementStrategy.gaDbi5D443T6vqgt() == Direction.field_11033 && f <= 45.0f) {
                return false;
            }
        }
        return true;
    }

    private float jOdDDFXSeWl4(Direction Direction2) {
        return switch (Direction2) {
            case Direction.field_11035 -> 0.0f;
            case Direction.field_11039 -> 90.0f;
            case Direction.field_11043 -> 180.0f;
            case Direction.field_11034 -> -90.0f;
            default -> this.rBGedpmjQyZ.player.method_36454();
        };
    }

    private float mp3zoXQFKUKYj5(Direction Direction2) {
        return switch (Direction2) {
            case Direction.field_11036 -> -90.0f;
            case Direction.field_11033 -> 90.0f;
            default -> this.rBGedpmjQyZ.player.method_36455();
        };
    }

    private boolean mp3zoXQFKUKYj5(BlockPos BlockPos2, BlockState BlockState2) {
        if (BlockState2.getBlock() instanceof class_3749) {
            boolean bl = (Boolean)BlockState2.method_11654((class_2769)class_2741.field_16561);
            BlockPos BlockPos3 = bl ? BlockPos2.method_10084() : BlockPos2.method_10074();
            return !this.rBGedpmjQyZ.world.getBlockState(BlockPos3).isAir();
        }
        if (BlockState2.getBlock() instanceof class_2457) {
            return !this.rBGedpmjQyZ.world.getBlockState(BlockPos2.method_10074()).isAir();
        }
        if (((Boolean)this.gravityCheck.get()).booleanValue() && BlockState2.getBlock() instanceof class_2346) {
            return !this.rBGedpmjQyZ.world.getBlockState(BlockPos2.method_10074()).isAir();
        }
        return true;
    }

    private BlockPos jOdDDFXSeWl4(Long2ObjectMap<Map<BlockPos, BlockState>> long2ObjectMap) {
        if (this.rBGedpmjQyZ.player == null || this.rBGedpmjQyZ.world == null) {
            return null;
        }
        double d = this.rBGedpmjQyZ.player.getX();
        double d2 = this.rBGedpmjQyZ.player.getY();
        double d3 = this.rBGedpmjQyZ.player.getZ();
        ArrayList<Map.Entry> arrayList = new ArrayList<Map.Entry>();
        for (Long2ObjectMap.Entry object : long2ObjectMap.long2ObjectEntrySet()) {
            arrayList.add(Map.entry(object.getLongKey(), (Map)object.getValue()));
        }
        arrayList.sort(Comparator.comparingDouble(entry -> {
            int n = ChunkPos.method_8325((long)((Long)entry.getKey()));
            int n2 = ChunkPos.method_8332((long)((Long)entry.getKey()));
            double d3 = (double)(n << 4) + 8.0;
            double d4 = (double)(n2 << 4) + 8.0;
            return (d3 - d) * (d3 - d) + (d4 - d3) * (d4 - d3);
        }));
        for (Map.Entry entry2 : arrayList) {
            Map map = (Map)entry2.getValue();
            if (map == null || map.isEmpty()) continue;
            BlockPos BlockPos2 = null;
            double d4 = Double.MAX_VALUE;
            for (Map.Entry entry3 : map.entrySet()) {
                double d5;
                double d6;
                double d7;
                double d8;
                BlockPos BlockPos3 = (BlockPos)entry3.getKey();
                BlockState BlockState2 = (BlockState)entry3.getValue();
                if (this.rBGedpmjQyZ.world.getBlockState(BlockPos3).getBlock() == BlockState2.getBlock() || !(this.rBGedpmjQyZ.world.getBlockState(BlockPos3).getBlock() instanceof FluidBlock) || !BlockUtils.canPlaceBlock((BlockPos)BlockPos3, (boolean)true, (Block)BlockState2.getBlock()) || InventoryManager.usJLOV0subXO3(BlockState2.getBlock().asItem()) <= 0 || !((d8 = (d7 = (double)BlockPos3.getX() + 0.5 - d) * d7 + (d6 = (double)BlockPos3.getY() + 0.5 - d2) * d6 + (d5 = (double)BlockPos3.getZ() + 0.5 - d3) * d5) < d4)) continue;
                d4 = d8;
                BlockPos2 = BlockPos3;
            }
            if (BlockPos2 == null) continue;
            return BlockPos2;
        }
        return null;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (this.rBGedpmjQyZ.player == null || this.rBGedpmjQyZ.world == null) {
            return;
        }
        if (!((Boolean)MusheorSystem.Manager.placeRender.get()).booleanValue()) {
            return;
        }
        if (this.bhy0Ddon9H6.isEmpty()) {
            return;
        }
        ArrayList<Pair<BlockPos, Block>> arrayList = new ArrayList<Pair<BlockPos, Block>>();
        for (Pair<BlockPos, BlockState> pair : this.bhy0Ddon9H6) {
            arrayList.add((Pair<BlockPos, Block>)Pair.of((Object)((BlockPos)pair.first()), (Object)((BlockState)pair.second()).getBlock()));
        }
        RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList);
    }

    private static /* synthetic */ boolean jOdDDFXSeWl4(BlockPos BlockPos2, Pair pair) {
        return ((BlockPos)pair.first()).equals((Object)BlockPos2);
    }

    static final class PlacementStrategy
    extends Record {
        private final Direction ISNvq0uvdjAugvE;
        private final double ZhoaRNJV1pNk;
        private final Direction VnBeu9FFeHHM;
        private final Direction t4IlnBm0D;

        PlacementStrategy(Direction Direction2, double d, Direction Direction3, Direction Direction4) {
            this.ISNvq0uvdjAugvE = Direction2;
            this.ZhoaRNJV1pNk = d;
            this.VnBeu9FFeHHM = Direction3;
            this.t4IlnBm0D = Direction4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlacementStrategy.class, "hitDir;hitFracY;yawRequired;pitchRequired", "ISNvq0uvdjAugvE", "ZhoaRNJV1pNk", "VnBeu9FFeHHM", "t4IlnBm0D"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlacementStrategy.class, "hitDir;hitFracY;yawRequired;pitchRequired", "ISNvq0uvdjAugvE", "ZhoaRNJV1pNk", "VnBeu9FFeHHM", "t4IlnBm0D"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlacementStrategy.class, "hitDir;hitFracY;yawRequired;pitchRequired", "ISNvq0uvdjAugvE", "ZhoaRNJV1pNk", "VnBeu9FFeHHM", "t4IlnBm0D"}, this, object);
        }

        public Direction Lm4xX5QT0OxvyV() {
            return this.ISNvq0uvdjAugvE;
        }

        public double lVls3aWwqcm7() {
            return this.ZhoaRNJV1pNk;
        }

        public Direction ThlHLXv3gtRxWy() {
            return this.VnBeu9FFeHHM;
        }

        public Direction gaDbi5D443T6vqgt() {
            return this.t4IlnBm0D;
        }
    }

    static final class SelectionType
    extends Enum<SelectionType> {
        public static final /* enum */ SelectionType ni1UVTBDGbU3 = new SelectionType();
        public static final /* enum */ SelectionType sdcDUaa = new SelectionType();
        private static final /* synthetic */ SelectionType[] keJiIXfi6Ivt7zUB;

        public static SelectionType[] values() {
            return (SelectionType[])keJiIXfi6Ivt7zUB.clone();
        }

        public static SelectionType valueOf(String string) {
            return Enum.valueOf(SelectionType.class, string);
        }

        private static /* synthetic */ SelectionType[] TZa5O0xAoIaC() {
            return new SelectionType[]{ni1UVTBDGbU3, sdcDUaa};
        }

        static {
            keJiIXfi6Ivt7zUB = SelectionType.TZa5O0xAoIaC();
        }
    }

    static final class LayerType
    extends Enum<LayerType> {
        public static final /* enum */ LayerType byVifkEYgkzY1E = new LayerType();
        public static final /* enum */ LayerType ZuBA2SJemxMpFD1 = new LayerType();
        public static final /* enum */ LayerType gvp3bKzV = new LayerType();
        private static final /* synthetic */ LayerType[] sVkASV;

        public static LayerType[] values() {
            return (LayerType[])sVkASV.clone();
        }

        public static LayerType valueOf(String string) {
            return Enum.valueOf(LayerType.class, string);
        }

        private static /* synthetic */ LayerType[] HWVVSgN() {
            return new LayerType[]{byVifkEYgkzY1E, ZuBA2SJemxMpFD1, gvp3bKzV};
        }

        static {
            sVkASV = LayerType.HWVVSgN();
        }
    }

    static final class RestockType
    extends Enum<RestockType> {
        public static final /* enum */ RestockType Rd1eOmBQPxISFki = new RestockType();
        public static final /* enum */ RestockType VcecHi2glQUu1VZB = new RestockType();
        public static final /* enum */ RestockType yF2JzAqyBTfec = new RestockType();
        private static final /* synthetic */ RestockType[] RP30itVPat;

        public static RestockType[] values() {
            return (RestockType[])RP30itVPat.clone();
        }

        public static RestockType valueOf(String string) {
            return Enum.valueOf(RestockType.class, string);
        }

        private static /* synthetic */ RestockType[] E4emISQ55E8d() {
            return new RestockType[]{Rd1eOmBQPxISFki, VcecHi2glQUu1VZB, yF2JzAqyBTfec};
        }

        static {
            RP30itVPat = RestockType.E4emISQ55E8d();
        }
    }
}

