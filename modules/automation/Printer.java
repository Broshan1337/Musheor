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
import java.util.Iterator;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Printer
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlacement;
    private final SettingGroup sgRender;
    private final SettingGroup sgLitematica;
    private final MinecraftClient mc;
    private final Long2ObjectMap<Map<BlockPos, BlockState>> chunkBlockMap;
    private final List<Pair<BlockPos, BlockState>> placementQueue;
    private final List<Pair<BlockPos, BlockState>> renderList;
    private int tickCount;
    private int waitTicks;
    private int openContainerDelay;
    private int swapDelayTicks;
    private int itemsCollected;
    private boolean needsInventorySync;
    private int totemCheckTicks;
    private final Set<BlockPos> exhaustedContainers;
    private ItemStack currentRestockItem;
    private final List<ItemStack> restockQueue;
    private boolean showSchematicPos;
    private int rotationDelayTicks;
    private PlacementStrategy lastStrategy;
    private Map<Block, Integer> materialCounts;
    private BlockPos pathfindTarget;
    private int rescanTimer;
    private boolean restockFromShulkerDone;
    private static final Set<Property<?>> DIRECTIONAL_PROPERTIES = new HashSet<EnumProperty>(Arrays.asList(Properties.FACING, Properties.HORIZONTAL_FACING, Properties.BLOCK_HALF, Properties.SLAB_TYPE, Properties.AXIS, Properties.HOPPER_FACING));
    private static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
    private static final float[] PITCH_VALUES = new float[]{-89.0f, 0.0f, 89.0f};
    private static final double[] HIT_FRACTIONS = new double[]{0.25, 0.75};
    private final Map<BlockState, PlacementStrategy> strategyCache;
    private static Method getPlacementStateMethod = null;
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

    private static BlockState getPlacementStateReflected(Block Block2, ItemPlacementContext ctx) { // was: jOdDDFXSeWl4
        try {
            if (getPlacementStateMethod == null) {
                Object object = Block.class.getMethods();
                int n = ((Method[])object).length;
                for (int i = 0; i < n; ++i) {
                    Method method = object[i];
                    if (method.getParameterCount() != 1 || !BlockState.class.isAssignableFrom(method.getReturnType()) || !method.getParameterTypes()[0].isAssignableFrom(ItemPlacementContext.class)) continue;
                    getPlacementStateMethod = method;
                    break;
                }
                if (getPlacementStateMethod == null) {
                    block3: for (object = Block.class; object != null && object != Object.class; object = ((Class)object).getSuperclass()) {
                        for (Method method : ((Class)object).getDeclaredMethods()) {
                            if (method.getParameterCount() != 1 || !BlockState.class.isAssignableFrom(method.getReturnType()) || !method.getParameterTypes()[0].isAssignableFrom(ItemPlacementContext.class)) continue;
                            method.setAccessible(true);
                            getPlacementStateMethod = method;
                            break block3;
                        }
                    }
                }
            }
            return getPlacementStateMethod != null ? (BlockState)getPlacementStateMethod.invoke((Object)Block2, ctx) : null;
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
        this.mc = MinecraftClient.getInstance();
        this.chunkBlockMap = new Long2ObjectOpenHashMap();
        this.placementQueue = new ArrayList<Pair<BlockPos, BlockState>>();
        this.renderList = new ArrayList<Pair<BlockPos, BlockState>>();
        this.waitTicks = 0;
        this.openContainerDelay = 0;
        this.swapDelayTicks = 0;
        this.itemsCollected = 0;
        this.exhaustedContainers = new HashSet<BlockPos>();
        this.currentRestockItem = null;
        this.restockQueue = new ArrayList<ItemStack>();
        this.showSchematicPos = false;
        this.rotationDelayTicks = 0;
        this.lastStrategy = null;
        this.materialCounts = new HashMap<Block, Integer>();
        this.pathfindTarget = null;
        this.rescanTimer = 0;
        this.restockFromShulkerDone = false;
        this.strategyCache = new HashMap<BlockState, PlacementStrategy>();
        this.selectionType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("selection-type")).description("Get selection from litematica or baritone")).defaultValue((Object)SelectionType.Litematica)).build());
        this.pauseOnEat = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-when-eating")).description("Pauses the printing process when the player eats (only when using meteor's auto-eat)")).defaultValue((Object)true)).build());
        this.pauseOnAura = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-auta")).description("Pauses the printing process when the player is killing mobs using killaura")).defaultValue((Object)true)).build());
        this.ignoredBlocks = this.sgPlacement.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("ignored-block-list")).description("Avoids placing blocks configured in this list")).defaultValue(new Block[0]).build());
        this.layerType = this.sgPlacement.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("layer-type")).description("Choose how layering is handled when printing")).defaultValue((Object)LayerType.All)).build());
        this.onlyAir = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-air")).description("Places blocks only when the desired location is an air block (meaning it doesn't replace non-solid blocks)")).defaultValue((Object)true)).build());
        this.gravityCheck = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gravity-check")).description("Skip gravity-affected blocks (sand, gravel, etc.) if there is no solid block below them to prevent unwanted falling.")).defaultValue((Object)true)).build());
        this.ignoreRotations = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-rotations")).description("Fully ignores the rotation of the player and blocks when placing them")).defaultValue((Object)false)).build());
        this.forceRotate = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("force-rotate")).description("Automatically rotate the camera to the correct direction when placing directional blocks.")).defaultValue((Object)false)).build());
        this.rotationDelay = this.sgPlacement.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rotation-delay")).description("Ticks to wait after rotating before attempting placement (allows rotation to settle server-side).")).defaultValue((Object)2)).min(0).sliderMax(10).visible(() -> this.forceRotate.get())).build());
        this.block = this.sgPlacement.add((Setting)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)new BlockSetting.Builder().name("block")).description("What block to place (used for Baritone mode).")).defaultValue((Object)Blocks.OBSIDIAN)).visible(() -> this.selectionType.get() == SelectionType.Baritone)).build());
        this.pathfind = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-pathfinding")).description("Basically baritone building")).defaultValue((Object)true)).build());
        this.restockType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("auto-restock")).description("Automatically restock more materials from either configured containers or from shulkers you have on you")).defaultValue((Object)RestockType.Disabled)).visible(() -> this.pathfind.get())).build());
        this.finishedSound = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("play-sound")).description("Plays a sound when its finished building or failed")).defaultValue((Object)true)).visible(() -> this.pathfind.get())).build());
        this.pathfindRescanInterval = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pathfind-rescan-interval")).description("How many ticks between pathfinding goal rescans (only in Litematica mode).")).defaultValue((Object)20)).min(1).sliderMax(100).visible(() -> (Boolean)this.pathfind.get() != false && this.selectionType.get() == SelectionType.Litematica)).build());
        this.renderRadius = this.sgRender.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-radius")).description("Only renders placeable blocks in a specified radius (lower if you experience FPS drops)")).defaultValue((Object)16)).sliderMin(2).sliderMax(128).visible(() -> MusheorSystem.Manager.placeRender.get())).build());
        this.schematicName = this.sgLitematica.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("schematic-name")).description("The name of the schematic you want to place")).visible(() -> LitematicaHelper.isLoaded() && this.selectionType.get() == SelectionType.Litematica)).defaultValue((Object)"")).build());
        this.schematicPos = this.sgLitematica.add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("schematic-pos")).description("The position of the schematic you want to place")).visible(() -> LitematicaHelper.isLoaded() && this.selectionType.get() == SelectionType.Litematica && this.showSchematicPos)).defaultValue((Object)new BlockPos(0, 0, 0))).build());
    }

    public WWidget getWidget(GuiTheme guiTheme) {
        WVerticalList wVerticalList = guiTheme.verticalList();
        WButton wButton = (WButton)wVerticalList.add((WWidget)guiTheme.button("Load and place schematic")).widget();
        wButton.action = () -> {
            if (this.mc.player == null || this.mc.world == null) {
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
            this.showSchematicPos = !this.showSchematicPos;
            wButton3.set(this.showSchematicPos ? "Hide Coordinates" : "Show Coordinates");
            this.schematicPos.onChanged();
        };
        return wVerticalList;
    }

    private void addToChunkMap(BlockPos BlockPos2, BlockState BlockState2) { // was: jOdDDFXSeWl4
        long l2 = ChunkPos.toLong((int)(BlockPos2.getX() >> 4), (int)(BlockPos2.getZ() >> 4));
        ((Map)this.chunkBlockMap.computeIfAbsent(l2, l -> new HashMap())).put(BlockPos2, BlockState2);
    }

    private void loadSelectionIntoMap(ISelection iSelection) { // was: jOdDDFXSeWl4
        if (iSelection == null || this.mc.world == null) {
            return;
        }
        BetterBlockPos betterBlockPos = iSelection.min();
        BetterBlockPos betterBlockPos2 = iSelection.max();
        for (int i = betterBlockPos.getX(); i <= betterBlockPos2.getX(); ++i) {
            for (int j = betterBlockPos.getY(); j <= betterBlockPos2.getY(); ++j) {
                for (int k = betterBlockPos.getZ(); k <= betterBlockPos2.getZ(); ++k) {
                    BlockPos BlockPos2 = new BlockPos(i, j, k);
                    if ((Boolean)this.onlyAir.get() != false ? !(this.mc.world.getBlockState(BlockPos2).getBlock() instanceof FluidBlock) : !BlockUtils.canPlace((BlockPos)BlockPos2, (boolean)false)) continue;
                    this.addToChunkMap(BlockPos2, ((Block)this.block.get()).getDefaultState());
                }
            }
        }
    }

    private int getRenderChunkRadius() {
        return (int)Math.ceil((double)((Integer)this.renderRadius.get()).intValue() / 16.0);
    }

    public void onActivate() {
        this.chunkBlockMap.clear();
        this.restockQueue.clear();
        HighwayState.getInstance().getBlockBreakAttempts().clear();
        this.tickCount = 0;
        this.swapDelayTicks = 0;
        this.rotationDelayTicks = 0;
        this.lastStrategy = null;
        this.strategyCache.clear();
        this.itemsCollected = 0;
        this.totemCheckTicks = 0;
        this.needsInventorySync = false;
        this.materialCounts = new HashMap<Block, Integer>();
        this.pathfindTarget = null;
        this.restockFromShulkerDone = false;
        this.rescanTimer = 0;
        if (this.selectionType.get() == SelectionType.Baritone) {
            IBaritone iBaritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            ISelectionManager iSelectionManager = iBaritone.getSelectionManager();
            if (iSelectionManager.getSelections() == null) {
                this.error("No Baritone selection found.", new Object[0]);
                this.toggle();
                return;
            }
            for (ISelection iSelection : iSelectionManager.getSelections()) {
                this.loadSelectionIntoMap(iSelection);
            }
        }
        if (this.selectionType.get() == SelectionType.Litematica) {
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
            this.materialCounts = LitematicaHelper.get().getMaterialCounts((List)this.ignoredBlocks.get());
            int n = this.materialCounts.values().stream().mapToInt(Integer::intValue).sum();
            this.info("Schematic ready \u2014 \u00a7b" + n + "\u00a7r blocks to place", new Object[0]);
        }
    }

    public void onDeactivate() {
        musheor.utils.PlayerUtils.stopBaritone();
        this.renderList.clear();
        this.materialCounts.clear();
        this.pathfindTarget = null;
        this.restockFromShulkerDone = false;
        this.waitTicks = 0;
        this.swapDelayTicks = 0;
        this.openContainerDelay = 0;
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
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        HighwayState highwayState = HighwayState.getInstance();
        ++this.tickCount;
        highwayState.getBlockBreakAttempts().entrySet().removeIf(entry -> {
            BlockState BlockState2;
            if (this.selectionType.get() == SelectionType.Baritone && (BlockState2 = this.getBlockStateAt((BlockPos)entry.getKey())) != null && this.mc.world.getBlockState((BlockPos)entry.getKey()).getBlock() == BlockState2.getBlock()) {
                return true;
            }
            return this.tickCount - (Integer)entry.getValue() > (Integer)MusheorSystem.Manager.placementTimeout.get();
        });
        if (((Boolean)this.pauseOnEat.get()).booleanValue() && ((AutoEat)Modules.get().get(AutoEat.class)).eating) {
            return;
        }
        if (((Boolean)this.pauseOnAura.get()).booleanValue() && ((KillAura)Modules.get().get(KillAura.class)).attacking) {
            return;
        }
        if (this.needsInventorySync) {
            // CFR NOTE: mc.player.getId().method_7376() is a decompiler artifact; likely mc.player.currentScreenHandler.getEmptySlot()
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, this.mc.player.currentScreenHandler.getEmptySlot(), 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
            this.needsInventorySync = false;
            VersionHelper.get().syncInventory();
        } else if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty() || this.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING && ((AutoTotem)Modules.get().get(AutoTotem.class)).isActive()) {
            ++this.totemCheckTicks;
            if (this.totemCheckTicks >= 20) {
                this.needsInventorySync = true;
                this.totemCheckTicks = 0;
            }
        }
        this.placementQueue.clear();
        if (this.selectionType.get() == SelectionType.Litematica) {
            BlockPos2 = this.mc.player.getBlockPos();
            n = (int)Math.ceil((Double)MusheorSystem.Manager.placementRange.get()) + 1;
            pair222 = LitematicaHelper.get().getBlocksInBox(BlockPos2.add(-n, -n, -n), BlockPos2.add(n, n, n), (Boolean)this.onlyAir.get(), (List)this.ignoredBlocks.get(), 500);
            for (Map.Entry<BlockPos, BlockState> object22 : pair222.entrySet()) {
                BlockPos n3 = object22.getKey();
                BlockState i = object22.getValue();
                if (InventoryManager.countItemInInventory(i.getBlock().asItem()) <= 0 || !BlockUtils.canPlace((BlockPos)n3, (boolean)true) || this.layerType.get() == LayerType.BelowPlayer && (double)n3.getY() >= Math.floor(this.mc.player.getY()) || this.layerType.get() == LayerType.Schematic && !LitematicaHelper.get().isPositionInRenderLayer(n3) || highwayState.getBlockBreakAttempts().containsKey(n3) || !WorldUtils.isInPlacementRange(n3)) continue;
                this.placementQueue.add((Pair<BlockPos, BlockState>)Pair.of((Object)n3, (Object)i));
            }
            int renderRad = (Integer)this.renderRadius.get();
            Map<BlockPos, BlockState> l = LitematicaHelper.get().getBlocksInBox(BlockPos2.add(-renderRad, -renderRad, -renderRad), BlockPos2.add(renderRad, renderRad, renderRad), (Boolean)this.onlyAir.get(), (List)this.ignoredBlocks.get(), 2000);
            this.renderList.clear();
            for (Map.Entry entry2 : l.entrySet()) {
                if (InventoryManager.countItemInInventory(((BlockState)entry2.getValue()).getBlock().asItem()) <= 0 || !WorldUtils.isWithinDistance((BlockPos)entry2.getKey(), renderRad)) continue;
                this.renderList.add((Pair<BlockPos, BlockState>)Pair.of((Object)((BlockPos)entry2.getKey()), (Object)((BlockState)entry2.getValue())));
            }
        } else {
            this.renderList.clear();
            BlockPos2 = this.mc.player.getChunkPos();
            n = this.getRenderChunkRadius();
            for (int i = BlockPos2.x - n; i <= BlockPos2.x + n; ++i) {
                for (int chunkZ = BlockPos2.z - n; chunkZ <= BlockPos2.z + n; ++chunkZ) {
                    long chunkKey = ChunkPos.toLong((int)i, (int)chunkZ);
                    Map map = (Map)this.chunkBlockMap.get(chunkKey);
                    if (map == null || map.isEmpty()) continue;
                    Iterator<Map.Entry> chunkEntryIter = map.entrySet().iterator();
                    while (chunkEntryIter.hasNext()) {
                        Map.Entry entry3 = chunkEntryIter.next();
                        object = (BlockPos)entry3.getKey();
                        BlockState BlockState3 = this.mc.world.getBlockState((BlockPos)object);
                        BlockState2 = (BlockState)entry3.getValue();
                        if (BlockState3.getBlock() == BlockState2.getBlock()) {
                            chunkEntryIter.remove();
                            continue;
                        }
                        if (InventoryManager.countItemInInventory(BlockState2.getBlock().asItem()) <= 0 || !BlockUtils.canPlace((BlockPos)object, (boolean)true) || this.layerType.get() == LayerType.BelowPlayer && (double)object.getY() >= Math.floor(this.mc.player.getY()) || this.layerType.get() == LayerType.Schematic && (!LitematicaHelper.isLoaded() || !LitematicaHelper.get().isPositionInRenderLayer((BlockPos)object)) || !(BlockState3.getBlock() instanceof FluidBlock) && ((Boolean)this.onlyAir.get()).booleanValue() || highwayState.getBlockBreakAttempts().containsKey(object)) continue;
                        if (WorldUtils.isWithinDistance((BlockPos)object, ((Integer)this.renderRadius.get()).intValue())) {
                            this.renderList.add((Pair<BlockPos, BlockState>)Pair.of((Object)object, (Object)BlockState2));
                        }
                        if (!WorldUtils.isInPlacementRange((BlockPos)object) || !this.placementQueue.stream().noneMatch(arg_0 -> Printer.pairMatchesPos((BlockPos)object, arg_0))) continue;
                        this.placementQueue.add((Pair<BlockPos, BlockState>)Pair.of((Object)object, (Object)BlockState2));
                    }
                    if (!map.isEmpty()) continue;
                    this.chunkBlockMap.remove(chunkKey);
                }
            }
        }
        this.placementQueue.sort(Comparator.comparingDouble(pair -> this.mc.player.squaredDistanceTo((double)((BlockPos)pair.first()).getX(), (double)((BlockPos)pair.first()).getY(), (double)((BlockPos)pair.first()).getZ())));
        if (this.restockType.get() != RestockType.Disabled && ((Boolean)this.pathfind.get()).booleanValue()) {
            // CFR NOTE: original used BlockPos2 for Block (variable reuse); split into mostNeededBlock
            Block mostNeededBlock = this.getMostNeededBlock();
            if (mostNeededBlock == null) {
                n = (this.selectionType.get() == SelectionType.Litematica ? this.placementQueue.isEmpty() : this.placementQueue.isEmpty() && this.chunkBlockMap.isEmpty()) ? 1 : 0;
                if (n != 0) {
                    this.info("All blocks placed!", new Object[0]);
                    if (((Boolean)this.finishedSound.get()).booleanValue()) {
                        VersionHelper.get().playSoundPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                    }
                    this.toggle();
                    return;
                }
                if (this.selectionType.get() == SelectionType.Baritone) {
                    this.info("Cannot reach or find more blocks to place", new Object[0]);
                    if (((Boolean)this.finishedSound.get()).booleanValue()) {
                        VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                    }
                    this.toggle();
                    return;
                }
            }
            if (InventoryManager.countItemInInventory(mostNeededBlock.asItem()) <= 0 && this.restockQueue.isEmpty()) {
                this.buildRestockQueue(mostNeededBlock);
            }
            if (this.restockType.get() == RestockType.Shulker) {
                if (!this.restockQueue.isEmpty()) {
                    Refill refill;
                    if (PathingHelper.isAlreadyPathing()) {
                        PathingHelper.stopPathing();
                    }
                    if ((refill = (Refill)Modules.get().get(Refill.class)).isActive()) {
                        return;
                    }
                    if (this.restockFromShulkerDone) {
                        this.restockFromShulkerDone = false;
                        this.restockQueue.clear();
                        this.waitTicks = 0;
                        return;
                    }
                    while (!this.restockQueue.isEmpty() && InventoryManager.countItemIncludingShulkers(this.restockQueue.getFirst()) <= 0) {
                        this.restockQueue.removeFirst();
                    }
                    if (this.restockQueue.isEmpty()) {
                        this.info("No shulkers found for needed blocks, disabling...", new Object[0]);
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                        }
                        this.toggle();
                        return;
                    }
                    if (this.waitTicks <= (Integer)InventoryManager.INSTANCE.delayBeforePlacingSetting.get()) {
                        ++this.waitTicks;
                        return;
                    }
                    refill.item.set((Object)this.restockQueue.getFirst());
                    refill.toggle();
                    this.restockFromShulkerDone = true;
                    return;
                }
            } else if (!this.restockQueue.isEmpty()) {
                int containerSlot = 0; // was: void var9_31 (CFR artifact)
                ItemStack ItemStack2 = this.restockQueue.getFirst();
                int n2 = this.countBlocksNeeded(Block.getBlockFromItem((ItemStack)ItemStack2));
                if (this.currentRestockItem != ItemStack2) {
                    this.exhaustedContainers.clear();
                    this.currentRestockItem = ItemStack2;
                    this.itemsCollected = 0;
                }
                if (!RestockConfig.Gt56Sj4a6BWhgB(ItemStack2)) {
                    if (ItemStack2 == mostNeededBlock.asItem()) {
                        this.info("Cannot find container for %s, disabling...", new Object[]{RestockConfig.jOdDDFXSeWl4(ItemStack2)});
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                        }
                        this.restockQueue.clear();
                        this.toggle();
                        return;
                    }
                    this.restockQueue.removeFirst();
                    return;
                }
                BlockPos BlockPos4 = this.findContainerFor(ItemStack2);
                if (BlockPos4 == null) {
                    if (ItemStack2 == mostNeededBlock.asItem()) {
                        this.info("All containers for %s are empty, disabling...", new Object[]{RestockConfig.jOdDDFXSeWl4(ItemStack2)});
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.ENTITY_VILLAGER_NO);
                        }
                        this.exhaustedContainers.clear();
                        this.restockQueue.clear();
                        this.toggle();
                        return;
                    }
                    this.restockQueue.removeFirst();
                    return;
                }
                this.info("Restocking %s", new Object[]{RestockConfig.jOdDDFXSeWl4(ItemStack2)});
                if (!WorldUtils.isInPlacementRange(BlockPos4)) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalNear(BlockPos4, 2));
                    return;
                }
                musheor.utils.PlayerUtils.stopBaritone();
                if (!InventoryManager.isContainerOpen() && this.openContainerDelay == 0) {
                    WorldUtils.lookAtBlock(BlockPos4);
                    InventoryManager.openContainerAt(BlockPos4);
                    this.openContainerDelay = 3;
                    return;
                }
                if (this.openContainerDelay > 0) {
                    --this.openContainerDelay;
                    return;
                }
                if (this.waitTicks <= (Integer)InventoryManager.INSTANCE.delayAfterOpeningSetting.get()) {
                    ++this.waitTicks;
                    return;
                }
                if (!RateController.checkPlaceRate()) {
                    return;
                }
                // CFR NOTE: 'Text f' is a decompiler type error; should be ScreenHandler
                ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
                int containerSlotCount = screenHandler.slots.size() - 36;
                boolean bl = false;
                while (++containerSlot < containerSlotCount) {
                    // CFR NOTE: variable reuse — inner ItemStack2 shadows outer; comparing slot item to restock item
                    ItemStack slotStack = screenHandler.getSlot((int)containerSlot).getStack();
                    if (slotStack.isEmpty()) continue;
                    if (slotStack.getItem() == ItemStack2.getItem()) {
                        this.itemsCollected += slotStack.getCount();
                        this.mc.interactionManager.clickSlot(screenHandler.syncId, (int)containerSlot, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)this.mc.player);
                    }
                    if (this.itemsCollected >= n2 || InventoryManager.countEmptySlots() <= 0 || !RateController.checkPlaceRate()) break;
                }
                if (this.itemsCollected == 0) {
                    this.exhaustedContainers.add(BlockPos4);
                } else if (this.itemsCollected < n2 && InventoryManager.countEmptySlots() > 0) {
                    this.exhaustedContainers.add(BlockPos4);
                } else if (InventoryManager.countEmptySlots() > 0) {
                    this.exhaustedContainers.clear();
                    this.restockQueue.removeFirst();
                }
                this.waitTicks = 0;
                this.openContainerDelay = 0;
                this.mc.player.closeScreen();
                if (this.restockQueue.isEmpty() || InventoryManager.countEmptySlots() <= 0) {
                    this.restockQueue.clear();
                }
                return;
            }
        }
        if (((Boolean)this.pathfind.get()).booleanValue() && this.placementQueue.isEmpty()) {
            if (this.selectionType.get() == SelectionType.Litematica) {
                ++this.rescanTimer;
                if (this.pathfindTarget == null || this.rescanTimer >= (Integer)this.pathfindRescanInterval.get() || WorldUtils.isInPlacementRange(this.pathfindTarget)) {
                    this.rescanTimer = 0;
                    this.pathfindTarget = LitematicaHelper.get().findClosestUnplacedBlock(this.mc.player.getBlockPos(), 64, (Boolean)this.onlyAir.get(), (List)this.ignoredBlocks.get());
                    if (this.pathfindTarget == null) {
                        this.info("All blocks placed!", new Object[0]);
                        if (((Boolean)this.finishedSound.get()).booleanValue()) {
                            VersionHelper.get().playSoundPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                        }
                        this.toggle();
                        return;
                    }
                }
                if (WorldUtils.horizontalDistance(this.mc.player.getBlockPos(), this.pathfindTarget) > 2.5) {
                    PathingHelper.setGoalNear(new GoalNear(this.pathfindTarget, 2));
                    return;
                }
                musheor.utils.PlayerUtils.stopBaritone();
            } else if (!this.chunkBlockMap.isEmpty() && (BlockPos2 = this.findBestPathfindTarget(this.chunkBlockMap)) != null) {
                if (WorldUtils.horizontalDistance(this.mc.player.getBlockPos(), BlockPos2) > 2.5) {
                    PathingHelper.setGoalNear(new GoalNear(BlockPos2, 2));
                    return;
                }
                musheor.utils.PlayerUtils.stopBaritone();
                if (this.mc.player.getBlockPos() == BlockPos2) {
                    PathingHelper.setBaritoneGoal((Goal)new GoalBlock(BlockPos2.east()));
                }
            }
        }
        if (this.swapDelayTicks > 0) {
            --this.swapDelayTicks;
            return;
        }
        if (!this.placementQueue.isEmpty()) {
            BlockPos2 = null;
            for (Pair<BlockPos, BlockState> pair222 : this.placementQueue) {
                if (!this.canPlaceBlock((BlockPos)pair222.first(), (BlockState)pair222.second())) continue; // was: mp3zoXQFKUKYj5
                BlockPos2 = pair222;
                break;
            }
            if (BlockPos2 != null) {
                boolean bl;
                BlockState BlockState4 = (BlockState)BlockPos2.second();
                Block targetBlock = BlockState4.getBlock(); // was: pair222 (CFR variable reuse)
                PlacementStrategy placementStrategy = this.getPlacementStrategy(BlockState4);
                if (this.rotationDelayTicks > 0) {
                    --this.rotationDelayTicks;
                    if (((Boolean)this.forceRotate.get()).booleanValue() && this.lastStrategy != null) {
                        float yaw = this.lastStrategy.getYawRequired() != null ? this.directionToYaw(this.lastStrategy.getYawRequired()) : this.mc.player.getYaw();
                        float pitch = this.lastStrategy.getPitchRequired() != null ? this.directionToPitch(this.lastStrategy.getPitchRequired()) : this.mc.player.getPitch();
                        Rotations.rotate((double)yaw, (double)pitch);
                    }
                    return;
                }
                PlacementStrategy placementStrategy2 = this.lastStrategy;
                this.lastStrategy = null;
                boolean bl2 = placementStrategy2 != null;
                boolean bl3 = bl = (Boolean)this.ignoreRotations.get() == false && (placementStrategy.getYawRequired() != null || placementStrategy.getPitchRequired() != null);
                if (bl) {
                    if (((Boolean)this.forceRotate.get()).booleanValue()) {
                        if (!bl2) {
                            float f = placementStrategy.getYawRequired() != null ? this.directionToYaw(placementStrategy.getYawRequired()) : this.mc.player.getYaw();
                            float f2 = placementStrategy.getPitchRequired() != null ? this.directionToPitch(placementStrategy.getPitchRequired()) : this.mc.player.getPitch();
                            Rotations.rotate((double)f, (double)f2);
                            this.lastStrategy = placementStrategy;
                            this.rotationDelayTicks = (Integer)this.rotationDelay.get();
                            return;
                        }
                    } else if (!this.isCorrectRotation(placementStrategy)) {
                        this.swapDelayTicks = (Integer)MusheorSystem.Manager.swapDelay.get();
                        return;
                    }
                }
                if (this.mc.player.getMainHandStack().getItem() != targetBlock.asItem()) {
                    InventoryManager.equipItem(targetBlock.asItem());
                    this.swapDelayTicks = (Integer)MusheorSystem.Manager.swapDelay.get();
                    return;
                }
                WorldUtils.swapCarriedItems();
                for (Pair pair2 : this.placementQueue) {
                    boolean bl4;
                    if (!BlockUtils.canPlace((BlockPos)((BlockPos)pair2.first()), (boolean)true) || !PlayerUtils.isWithin((BlockPos)((BlockPos)pair2.first()), (double)((Double)MusheorSystem.Manager.placementRange.get())) || ((BlockState)pair2.second()).getBlock() != targetBlock || !this.canPlaceBlock((BlockPos)pair2.first(), (BlockState)pair2.second())) continue;
                    PlacementStrategy pairStrategy = this.getPlacementStrategy((BlockState)pair2.second());
                    boolean bl5 = bl4 = (Boolean)this.ignoreRotations.get() == false && (pairStrategy.getYawRequired() != null || pairStrategy.getPitchRequired() != null);
                    if (bl4) {
                        boolean bl6;
                        if (((Boolean)this.forceRotate.get()).booleanValue() && bl2) {
                            bl6 = pairStrategy.getYawRequired() != placementStrategy2.getYawRequired() || pairStrategy.getPitchRequired() != placementStrategy2.getPitchRequired();
                        } else {
                            boolean bl7 = bl6 = !this.isCorrectRotation(pairStrategy);
                        }
                        if (bl6) {
                            if (((Boolean)this.forceRotate.get()).booleanValue()) break;
                            this.swapDelayTicks = (Integer)MusheorSystem.Manager.swapDelay.get();
                            break;
                        }
                    }
                    if (!RateController.checkPlaceRate()) break;
                    Vec3d hitVec = new Vec3d((double)((BlockPos)pair2.first()).getX() + 0.5 + (double)pairStrategy.getHitDir().getOffsetX() * 0.5, (double)((BlockPos)pair2.first()).getY() + pairStrategy.getHitFracY(), (double)((BlockPos)pair2.first()).getZ() + 0.5 + (double)pairStrategy.getHitDir().getOffsetZ() * 0.5);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, pairStrategy.getHitDir(), (BlockPos)pair2.first(), false);
                    WorldUtils.sendPlacePacket(Hand.OFF_HAND, hitResult);
                    if (((BlockState)pair2.second()).getBlock() == Blocks.OBSIDIAN) {
                        HighwayState.getInstance().incrementSessionObsidianPlaced();
                    }
                    highwayState.getBlockBreakAttempts().put((BlockPos)pair2.first(), this.tickCount);
                }
                WorldUtils.swapCarriedItems();
            }
        }
    }

    private void buildRestockQueue(Block Block2) { // was: jOdDDFXSeWl4
        this.restockQueue.clear();
        this.restockQueue.add(Block2.asItem());
        HashSet<ItemStack> hashSet = new HashSet<ItemStack>();
        hashSet.add(Block2.asItem());
        ArrayList<Map.Entry<ItemStack, Integer>> arrayList = new ArrayList<Map.Entry<ItemStack, Integer>>();
        if (this.selectionType.get() == SelectionType.Litematica) {
            for (Map.Entry object : this.materialCounts.entrySet()) {
                ItemStack ItemStack2 = ((Block)object.getKey()).asItem();
                if (hashSet.contains(ItemStack2)) continue;
                hashSet.add(ItemStack2);
                if (InventoryManager.countItemInInventory(ItemStack2) > 0 || !RestockConfig.Gt56Sj4a6BWhgB(ItemStack2)) continue;
                arrayList.add(Map.entry(ItemStack2, (Integer)object.getValue()));
            }
        } else {
            for (Map map : this.chunkBlockMap.values()) {
                for (BlockState BlockState2 : map.values()) {
                    ItemStack ItemStack3 = BlockState2.getBlock().asItem();
                    if (hashSet.contains(ItemStack3)) continue;
                    hashSet.add(ItemStack3);
                    if (InventoryManager.countItemInInventory(ItemStack3) > 0 || !RestockConfig.Gt56Sj4a6BWhgB(ItemStack3)) continue;
                    arrayList.add(Map.entry(ItemStack3, this.countBlocksNeeded(BlockState2.getBlock())));
                }
            }
        }
        arrayList.sort((entry, entry2) -> Integer.compare((Integer)entry2.getValue(), (Integer)entry.getValue()));
        for (Map.Entry entry3 : arrayList) {
            this.restockQueue.add((ItemStack)entry3.getKey());
        }
    }

    private BlockPos findContainerFor(ItemStack ItemStack2) { // was: VYEwzRq
        List<BlockPos> list = RestockConfig.mp3zoXQFKUKYj5(ItemStack2);
        BlockPos BlockPos2 = this.mc.player.getBlockPos();
        BlockPos BlockPos3 = null;
        double d = Double.MAX_VALUE;
        for (BlockPos BlockPos4 : list) {
            double d2;
            if (this.exhaustedContainers.contains(BlockPos4) || !((d2 = BlockPos2.getSquaredDistance((BlockPos)BlockPos4)) < d)) continue;
            d = d2;
            BlockPos3 = BlockPos4;
        }
        return BlockPos3;
    }

    private int countBlocksNeeded(Block Block2) { // was: mp3zoXQFKUKYj5
        if (this.selectionType.get() == SelectionType.Litematica) {
            return this.materialCounts.getOrDefault(Block2, 0);
        }
        int n = 0;
        for (Map map : this.chunkBlockMap.values()) {
            for (BlockState BlockState2 : map.values()) {
                if (BlockState2.getBlock() != Block2) continue;
                ++n;
            }
        }
        return n;
    }

    private Block getMostNeededBlock() { // was: v1nokUkHXYjAGxn
        Object object;
        Object object2;
        ObjectIterator objectIterator = null;
        Object object3 = null;
        ObjectIterator objectIterator2 = null;
        Object object4 = null;
        if (this.selectionType.get() == SelectionType.Litematica) {
            object2 = this.materialCounts.keySet();
        } else {
            object = new HashSet();
            for (Object object5 : this.chunkBlockMap.values()) {
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
            if (InventoryManager.countItemInInventory(objectIterator3.asItem()) <= 0 || objectIterator != null && ((String)object5).compareTo((String)object3) >= 0) continue;
            objectIterator = objectIterator3;
            object3 = object5;
        }
        if (objectIterator != null) {
            return objectIterator;
        }
        if (objectIterator2 != null) {
            return objectIterator2;
        }
        return this.selectionType.get() == SelectionType.Baritone ? (Block)this.block.get() : null;
    }

    private BlockState getBlockStateAt(BlockPos BlockPos2) { // was: vgrtgn5
        long l = ChunkPos.toLong((int)(BlockPos2.getX() >> 4), (int)(BlockPos2.getZ() >> 4));
        Map map = (Map)this.chunkBlockMap.get(l);
        if (map == null) {
            return null;
        }
        return (BlockState)map.get(BlockPos2);
    }

    private PlacementStrategy getPlacementStrategy(BlockState BlockState2) { // was: mp3zoXQFKUKYj5
        return this.strategyCache.computeIfAbsent(BlockState2, this::computePlacementStrategy);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PlacementStrategy computePlacementStrategy(BlockState BlockState2) {
        if (this.mc.player == null || this.mc.world == null) {
            return new PlacementStrategy(Direction.DOWN, 0.5, null, null);
        }
        boolean bl = DIRECTIONAL_PROPERTIES.stream().anyMatch(arg_0 -> ((BlockState)BlockState2).contains(arg_0));
        if (!bl) {
            return new PlacementStrategy(Direction.DOWN, 0.5, null, null);
        }
        ItemStack ItemStack2 = BlockState2.getBlock().asItem();
        if (ItemStack2 == Items.AIR) {
            return new PlacementStrategy(Direction.DOWN, 0.5, null, null);
        }
        ItemStack placementStack = new ItemStack((ItemConvertible)ItemStack2);
        BlockPos BlockPos2 = this.mc.player.getBlockPos();
        float f = this.mc.player.getYaw();
        float f2 = this.mc.player.getPitch();
        try {
            BlockHitResult hitResult2;
            Vec3d Vec3d2;
            double[] dArray;
            for (Direction Direction2 : Direction.values()) {
                double[] dArray2;
                if (Direction2.getAxis() == Direction.Axis.Y) {
                    double[] dArray3 = new double[1];
                    dArray2 = dArray3;
                    dArray3[0] = Direction2 == Direction.UP ? 1.0 : 0.0;
                } else {
                    dArray2 = HIT_FRACTIONS;
                }
                for (double d : dArray = dArray2) {
                    Vec3d2 = new Vec3d((double)BlockPos2.getX() + 0.5 + (double)Direction2.getOffsetX() * 0.5, (double)BlockPos2.getY() + d, (double)BlockPos2.getZ() + 0.5 + (double)Direction2.getOffsetZ() * 0.5);
                    hitResult2 = new BlockHitResult(Vec3d2, Direction2, BlockPos2, false);
                    boolean bl2 = false;
                    int n = 0;
                    block7: for (Direction Direction3 : HORIZONTAL_DIRECTIONS) {
                        this.mc.player.setYaw(this.directionToYaw(Direction3));
                        for (float pitch3 : PITCH_VALUES) {
                            this.mc.player.setPitch(pitch3);
                            ItemPlacementContext ctx = new ItemPlacementContext(this.mc.world, (PlayerEntity)this.mc.player, Hand.MAIN_HAND, placementStack, hitResult2){};
                            BlockState placedState = Printer.getPlacementStateReflected(BlockState2.getBlock(), ctx);
                            if (placedState == null) continue;
                            if (this.blockStatesMatch(placedState, BlockState2)) {
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
                if (Direction2.getAxis() == Direction.Axis.Y) {
                    double[] dArray5 = new double[1];
                    dArray4 = dArray5;
                    dArray5[0] = Direction2 == Direction.UP ? 1.0 : 0.0;
                } else {
                    dArray4 = HIT_FRACTIONS;
                }
                for (double d : dArray = dArray4) {
                    Vec3d2 = new Vec3d((double)BlockPos2.getX() + 0.5 + (double)Direction2.getOffsetX() * 0.5, (double)BlockPos2.getY() + d, (double)BlockPos2.getZ() + 0.5 + (double)Direction2.getOffsetZ() * 0.5);
                    hitResult2 = new BlockHitResult(Vec3d2, Direction2, BlockPos2, false);
                    for (Direction Direction4 : HORIZONTAL_DIRECTIONS) {
                        this.mc.player.setYaw(this.directionToYaw(Direction4));
                        for (float f3 : PITCH_VALUES) {
                            Direction Direction5;
                            this.mc.player.setPitch(f3);
                            ItemPlacementContext ctx2 = new ItemPlacementContext(this.mc.world, (PlayerEntity)this.mc.player, Hand.MAIN_HAND, placementStack, hitResult2){};
                            BlockState BlockState3 = Printer.getPlacementStateReflected(BlockState2.getBlock(), ctx2);
                            if (BlockState3 == null || !this.blockStatesMatch(BlockState3, BlockState2)) continue;
                            boolean bl2 = BlockState2.contains(Properties.HORIZONTAL_FACING) || BlockState2.contains(Properties.FACING) && ((Direction)BlockState2.get(Properties.FACING)).getAxis().isVertical();
                            boolean bl3 = BlockState2.contains(Properties.FACING) && ((Direction)BlockState2.get(Properties.FACING)).getAxis().isHorizontal();
                            Object object = Direction5 = bl2 ? Direction4 : null;
                            Direction Direction6 = bl3 ? (f3 < 0.0f ? Direction.UP : Direction.DOWN) : null;
                            PlacementStrategy placementStrategy = new PlacementStrategy(Direction2, d, Direction5, Direction6);
                            return placementStrategy;
                        }
                    }
                }
            }
        }
        finally {
            this.mc.player.setYaw(f);
            this.mc.player.setPitch(f2);
        }
        return new PlacementStrategy(Direction.DOWN, 0.5, null, null);
    }

    private boolean blockStatesMatch(BlockState BlockState2, BlockState BlockState3) { // was: jOdDDFXSeWl4
        if (BlockState2.getBlock() != BlockState3.getBlock()) {
            return false;
        }
        for (Property<?> prop : DIRECTIONAL_PROPERTIES) {
            if (!BlockState2.contains(prop) || !BlockState3.contains(prop) || BlockState2.get(prop).equals(BlockState3.get(prop))) continue;
            return false;
        }
        return true;
    }

    private boolean isCorrectRotation(PlacementStrategy placementStrategy) { // was: jOdDDFXSeWl4
        if (this.mc.player == null) {
            return false;
        }
        if (placementStrategy.getYawRequired() != null && this.mc.player.getFacing() != placementStrategy.getYawRequired()) {
            return false;
        }
        if (placementStrategy.getPitchRequired() != null) {
            float f = this.mc.player.getPitch();
            if (placementStrategy.getPitchRequired() == Direction.UP && f >= -45.0f) {
                return false;
            }
            if (placementStrategy.getPitchRequired() == Direction.DOWN && f <= 45.0f) {
                return false;
            }
        }
        return true;
    }

    private float directionToYaw(Direction Direction2) { // was: jOdDDFXSeWl4
        return switch (Direction2) {
            case Direction.SOUTH -> 0.0f;
            case Direction.NORTH -> 90.0f;
            case Direction.EAST -> 180.0f;
            case Direction.WEST -> -90.0f;
            default -> this.mc.player.getYaw();
        };
    }

    private float directionToPitch(Direction Direction2) { // was: mp3zoXQFKUKYj5
        return switch (Direction2) {
            case Direction.UP -> -90.0f;
            case Direction.DOWN -> 90.0f;
            default -> this.mc.player.getPitch();
        };
    }

    private boolean canPlaceBlock(BlockPos BlockPos2, BlockState BlockState2) { // was: mp3zoXQFKUKYj5
        if (BlockState2.getBlock() instanceof LanternBlock) {
            boolean bl = (Boolean)BlockState2.get(Properties.HANGING);
            BlockPos BlockPos3 = bl ? BlockPos2.up() : BlockPos2.down();
            return !this.mc.world.getBlockState(BlockPos3).isAir();
        }
        if (BlockState2.getBlock() instanceof RedstoneWireBlock) {
            return !this.mc.world.getBlockState(BlockPos2.down()).isAir();
        }
        if (((Boolean)this.gravityCheck.get()).booleanValue() && BlockState2.getBlock() instanceof FallingBlock) {
            return !this.mc.world.getBlockState(BlockPos2.down()).isAir();
        }
        return true;
    }

    private BlockPos findBestPathfindTarget(Long2ObjectMap<Map<BlockPos, BlockState>> long2ObjectMap) { // was: jOdDDFXSeWl4
        if (this.mc.player == null || this.mc.world == null) {
            return null;
        }
        double d = this.mc.player.getX();
        double d2 = this.mc.player.getY();
        double d3 = this.mc.player.getZ();
        ArrayList<Map.Entry> arrayList = new ArrayList<Map.Entry>();
        for (Long2ObjectMap.Entry object : long2ObjectMap.long2ObjectEntrySet()) {
            arrayList.add(Map.entry(object.getLongKey(), (Map)object.getValue()));
        }
        arrayList.sort(Comparator.comparingDouble(entry -> {
            int n = ChunkPos.getPackedX((long)((Long)entry.getKey()));
            int n2 = ChunkPos.getPackedZ((long)((Long)entry.getKey()));
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
                if (this.mc.world.getBlockState(BlockPos3).getBlock() == BlockState2.getBlock() || !(this.mc.world.getBlockState(BlockPos3).getBlock() instanceof FluidBlock) || !BlockUtils.canPlaceBlock((BlockPos)BlockPos3, (boolean)true, (Block)BlockState2.getBlock()) || InventoryManager.countItemInInventory(BlockState2.getBlock().asItem()) <= 0 || !((d8 = (d7 = (double)BlockPos3.getX() + 0.5 - d) * d7 + (d6 = (double)BlockPos3.getY() + 0.5 - d2) * d6 + (d5 = (double)BlockPos3.getZ() + 0.5 - d3) * d5) < d4)) continue;
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
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (!((Boolean)MusheorSystem.Manager.placeRender.get()).booleanValue()) {
            return;
        }
        if (this.renderList.isEmpty()) {
            return;
        }
        ArrayList<Pair<BlockPos, Block>> arrayList = new ArrayList<Pair<BlockPos, Block>>();
        for (Pair<BlockPos, BlockState> pair : this.renderList) {
            arrayList.add((Pair<BlockPos, Block>)Pair.of((Object)((BlockPos)pair.first()), (Object)((BlockState)pair.second()).getBlock()));
        }
        RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList);
    }

    private static /* synthetic */ boolean pairMatchesPos(BlockPos BlockPos2, Pair pair) { // was: jOdDDFXSeWl4
        return ((BlockPos)pair.first()).equals((Object)BlockPos2);
    }

    static final class PlacementStrategy
    extends Record {
        private final Direction hitDir;
        private final double hitFracY;
        private final Direction yawRequired;
        private final Direction pitchRequired;

        PlacementStrategy(Direction Direction2, double d, Direction Direction3, Direction Direction4) {
            this.hitDir = Direction2;
            this.hitFracY = d;
            this.yawRequired = Direction3;
            this.pitchRequired = Direction4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlacementStrategy.class, "hitDir;hitFracY;yawRequired;pitchRequired", "hitDir", "hitFracY", "yawRequired", "pitchRequired"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlacementStrategy.class, "hitDir;hitFracY;yawRequired;pitchRequired", "hitDir", "hitFracY", "yawRequired", "pitchRequired"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlacementStrategy.class, "hitDir;hitFracY;yawRequired;pitchRequired", "hitDir", "hitFracY", "yawRequired", "pitchRequired"}, this, object);
        }

        public Direction getHitDir() {
            return this.hitDir;
        }

        public double getHitFracY() {
            return this.hitFracY;
        }

        public Direction getYawRequired() {
            return this.yawRequired;
        }

        public Direction getPitchRequired() {
            return this.pitchRequired;
        }
    }

    static final class SelectionType
    extends Enum<SelectionType> {
        public static final /* enum */ SelectionType Baritone = new SelectionType();
        public static final /* enum */ SelectionType Litematica = new SelectionType();
        private static final /* synthetic */ SelectionType[] $VALUES;

        public static SelectionType[] values() {
            return (SelectionType[])$VALUES.clone();
        }

        public static SelectionType valueOf(String string) {
            return Enum.valueOf(SelectionType.class, string);
        }

        private static /* synthetic */ SelectionType[] $init() {
            return new SelectionType[]{Baritone, Litematica};
        }

        static {
            $VALUES = SelectionType.$init();
        }
    }

    static final class LayerType
    extends Enum<LayerType> {
        public static final /* enum */ LayerType All = new LayerType();
        public static final /* enum */ LayerType BelowPlayer = new LayerType();
        public static final /* enum */ LayerType Schematic = new LayerType();
        private static final /* synthetic */ LayerType[] $VALUES;

        public static LayerType[] values() {
            return (LayerType[])$VALUES.clone();
        }

        public static LayerType valueOf(String string) {
            return Enum.valueOf(LayerType.class, string);
        }

        private static /* synthetic */ LayerType[] $init() {
            return new LayerType[]{All, BelowPlayer, Schematic};
        }

        static {
            $VALUES = LayerType.$init();
        }
    }

    static final class RestockType
    extends Enum<RestockType> {
        public static final /* enum */ RestockType Disabled = new RestockType();
        public static final /* enum */ RestockType Container = new RestockType();
        public static final /* enum */ RestockType Shulker = new RestockType();
        private static final /* synthetic */ RestockType[] $VALUES;

        public static RestockType[] values() {
            return (RestockType[])$VALUES.clone();
        }

        public static RestockType valueOf(String string) {
            return Enum.valueOf(RestockType.class, string);
        }

        private static /* synthetic */ RestockType[] $init() {
            return new RestockType[]{Disabled, Container, Shulker};
        }

        static {
            $VALUES = RestockType.$init();
        }
    }
}

