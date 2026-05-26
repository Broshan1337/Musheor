// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PlacementEngine;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.util.Hand;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;

public class AutoPortal
extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final SettingColor OBSTRUCTION_LINE_COLOR = new SettingColor(255, 45, 45, 220);
    private static final SettingColor OBSTRUCTION_FILL_COLOR = new SettingColor(255, 45, 45, 60);
    private final Setting<Boolean> autoDisable;
    private final Setting<Boolean> pauseOnInput;
    private final Setting<Boolean> renderPos;
    private final Setting<SettingColor> renderColor;
    private boolean positionLocked;
    private Phase phase;
    private final List<BlockPos> frameBlocks;
    private final List<BlockPos> interiorBlocks;
    private BlockPos lightPos;
    private int swapDelayTicks;
    private int currentTick;

    public AutoPortal() {
        super(musheor.MAIN, "auto-portal", "Automatically builds and lights a nether portal");
        this.autoDisable = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-disable")).description("Automatically disables the module when a portal is built")).defaultValue((Object)true)).build());
        this.pauseOnInput = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-input")).description("Pauses the building process when you are moving, sneaking or jumping")).defaultValue((Object)true)).build());
        this.renderPos = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-portal")).description("Renders the portal's position")).defaultValue((Object)true)).build());
        this.renderColor = this.settings.getDefaultGroup().add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("render-color")).defaultValue(new SettingColor(0, 225, 255, 200)).description("Custom color for rendering (lines / wireframe)")).visible(() -> this.renderPos.get())).build());
        this.frameBlocks = new ArrayList<BlockPos>();
        this.interiorBlocks = new ArrayList<BlockPos>();
    }

    public void onActivate() {
        this.positionLocked = false;
        this.phase = Phase.PlaceFrame;
        this.frameBlocks.clear();
        this.interiorBlocks.clear();
        this.lightPos = null;
        this.swapDelayTicks = 0;
        this.currentTick = 0;
        HighwayState.getInstance().getBlockBreakAttempts().clear();
    }

    public void onDeactivate() {
        this.frameBlocks.clear();
        this.interiorBlocks.clear();
        this.lightPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (AutoPortal.mc.player == null || AutoPortal.mc.world == null || AutoPortal.mc.interactionManager == null) {
            return;
        }
        if (!this.positionLocked) {
            this.recalcPortalPosition();
        }
        HighwayState.getInstance().getBlockBreakAttempts().entrySet().removeIf(entry -> {
            BlockState BlockState2 = AutoPortal.mc.world.getBlockState((BlockPos)entry.getKey());
            if (BlockState2 != null && AutoPortal.mc.world.getBlockState((BlockPos)entry.getKey()).getBlock() == Blocks.OBSIDIAN) {
                return true;
            }
            return this.currentTick - (Integer)entry.getValue() > (Integer)MusheorSystem.Manager.placementTimeout.get();
        });
        if (this.swapDelayTicks > 0) {
            --this.swapDelayTicks;
        }
        if (((Boolean)this.pauseOnInput.get()).booleanValue() && this.isPlayerMoving()) {
            return;
        }
        if (!this.positionLocked) {
            int n = InventoryManager.countItemIncludingShulkers(Items.OBSIDIAN);
            if (n < 10) {
                this.error("Not enough obsidian (need %d, have %d), disabling.", new Object[]{10, n});
                this.toggle();
                return;
            }
            if (!InvUtils.find(itemStack -> itemStack.getItem() == Items.FLINT_AND_STEEL).found()) {
                this.error("No flint and steel found in inventory, disabling.", new Object[0]);
                this.toggle();
                return;
            }
            this.positionLocked = true;
            this.phase = Phase.PlaceFrame;
            this.info("Position locked, building portal...", new Object[0]);
        }
        switch (this.phase.ordinal()) {
            case 0: {
                this.tickPlaceFrame(this.currentTick++);
                break;
            }
            case 1: {
                this.tickLightPortal();
                break;
            }
            case 2: {
                this.tickVerify();
            }
        }
    }

    private void tickPlaceFrame(int n) {
        boolean bl = false;
        for (BlockPos object : this.frameBlocks) {
            if (AutoPortal.mc.world.getBlockState(object).getBlock() == Blocks.OBSIDIAN || !AutoPortal.mc.world.getBlockState(object).isReplaceable() || !WorldUtils.isInPlacementRange(object)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            if (this.isFrameComplete()) {
                this.phase = Phase.LightPortal;
                this.swapDelayTicks = (Integer)MusheorSystem.Manager.swapDelay.get();
            }
            return;
        }
        if (this.swapDelayTicks > 0) {
            return;
        }
        FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.OBSIDIAN);
        if (!findItemResult.found()) {
            InventoryManager.moveItemToHotbar(Items.OBSIDIAN);
            this.swapDelayTicks = (Integer)MusheorSystem.Manager.swapDelay.get();
            return;
        }
        InventoryManager.equipItem(Items.OBSIDIAN);
        for (BlockPos BlockPos2 : this.frameBlocks) {
            if (!RateController.checkPlaceRate()) break;
            if (HighwayState.getInstance().getBlockBreakAttempts().containsKey(BlockPos2) || AutoPortal.mc.world.getBlockState(BlockPos2).getBlock() == Blocks.OBSIDIAN || !AutoPortal.mc.world.getBlockState(BlockPos2).isReplaceable() || !WorldUtils.isInPlacementRange(BlockPos2)) continue;
            HighwayState.getInstance().getBlockBreakAttempts().put(BlockPos2, n);
            PlacementEngine.placeBlock(BlockPos2, Direction.UP);
        }
    }

    private void tickLightPortal() {
        if (this.lightPos == null || !WorldUtils.isInPlacementRange(this.lightPos)) {
            return;
        }
        if (this.swapDelayTicks > 0 || !RateController.checkPlaceRate()) {
            return;
        }
        FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.FLINT_AND_STEEL);
        if (!findItemResult.found()) {
            if (!InvUtils.find(itemStack -> itemStack.getItem() == Items.FLINT_AND_STEEL).found()) {
                this.error("Lost flint and steel during build, disabling.", new Object[0]);
                this.toggle();
                return;
            }
            InventoryManager.moveItemToHotbar(Items.FLINT_AND_STEEL);
            this.swapDelayTicks = (Integer)MusheorSystem.Manager.swapDelay.get();
            return;
        }
        InvUtils.swap((int)findItemResult.slot(), (boolean)false);
        BlockHitResult blockHit = new BlockHitResult(Vec3d.ofCenter(this.lightPos).add(0.0, 0.5, 0.0), Direction.UP, this.lightPos, false);
        AutoPortal.mc.interactionManager.sendSequencedPacket(AutoPortal.mc.world, n -> new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, blockHit, n));
        this.phase = Phase.Verify;
    }

    private void tickVerify() {
        if (this.isPortalLit()) {
            this.info("Portal built successfully!", new Object[0]);
            if (((Boolean)this.autoDisable.get()).booleanValue()) {
                this.toggle();
            }
        } else {
            this.phase = Phase.LightPortal;
        }
    }

    private boolean isFrameComplete() {
        for (BlockPos BlockPos2 : this.frameBlocks) {
            if (AutoPortal.mc.world.getBlockState(BlockPos2).getBlock() == Blocks.OBSIDIAN) continue;
            return false;
        }
        return true;
    }

    private boolean isPortalLit() {
        for (BlockPos BlockPos2 : this.interiorBlocks) {
            if (AutoPortal.mc.world.getBlockState(BlockPos2).getBlock() != Blocks.AIR) continue;
            return true;
        }
        return false;
    }

    private boolean isPlayerMoving() {
        return AutoPortal.mc.options.forwardKey.isPressed() || AutoPortal.mc.options.backKey.isPressed() || AutoPortal.mc.options.leftKey.isPressed() || AutoPortal.mc.options.rightKey.isPressed() || AutoPortal.mc.options.jumpKey.isPressed() || AutoPortal.mc.options.sneakKey.isPressed();
    }

    private Direction yawToFacingDirection(float f) {
        float f2 = (f % 360.0f + 360.0f) % 360.0f;
        if (f2 >= 315.0f || f2 < 45.0f) {
            return Direction.NORTH;
        }
        if (f2 < 135.0f) {
            return Direction.EAST;
        }
        if (f2 < 225.0f) {
            return Direction.SOUTH;
        }
        return Direction.WEST;
    }

    private int pitchToVerticalOffset(float f) {
        float f2 = Math.max(-45.0f, Math.min(45.0f, f));
        int n = Math.round(-f2 / 9.0f);
        return Math.max(-5, Math.min(5, n));
    }

    private void recalcPortalPosition() {
        this.frameBlocks.clear();
        this.interiorBlocks.clear();
        this.lightPos = null;
        BlockPos BlockPos2 = AutoPortal.mc.player.getBlockPos();
        Direction Direction2 = this.yawToFacingDirection(AutoPortal.mc.player.getYaw());
        int n = this.pitchToVerticalOffset(AutoPortal.mc.player.getPitch());
        int n2 = Direction2.getOffsetX();
        int n3 = Direction2.getOffsetZ();
        int n4 = -n3;
        int n5 = n2;
        int n6 = BlockPos2.getX() + n2 * 2;
        int n7 = BlockPos2.getZ() + n3 * 2;
        int n8 = BlockPos2.getY() + n;
        for (int i = 0; i < 5; ++i) {
            for (int j = -1; j <= 2; ++j) {
                boolean bl;
                BlockPos BlockPos3 = new BlockPos(n6 + n4 * j, n8 + i, n7 + n5 * j);
                boolean bl2 = !(i != 0 && i != 4 || j != -1 && j != 2);
                boolean bl3 = bl = i >= 1 && i <= 3 && j >= 0 && j <= 1;
                if (bl2) continue;
                if (bl) {
                    this.interiorBlocks.add(BlockPos3);
                    continue;
                }
                this.frameBlocks.add(BlockPos3);
            }
        }
        for (BlockPos BlockPos4 : this.frameBlocks) {
            if (BlockPos4.getY() != n8) continue;
            this.lightPos = BlockPos4;
            break;
        }
    }

    private boolean hasObstructingBlocks() {
        BlockState BlockState2;
        for (BlockPos BlockPos2 : this.frameBlocks) {
            BlockState2 = AutoPortal.mc.world.getBlockState(BlockPos2);
            if (BlockState2.getBlock() == Blocks.OBSIDIAN || BlockState2.isReplaceable()) continue;
            return true;
        }
        for (BlockPos BlockPos2 : this.interiorBlocks) {
            BlockState2 = AutoPortal.mc.world.getBlockState(BlockPos2);
            if (BlockState2.getBlock() == Blocks.AIR || BlockState2.getBlock() == Blocks.NETHER_PORTAL || BlockState2.isReplaceable()) continue;
            return true;
        }
        return false;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (AutoPortal.mc.player == null || AutoPortal.mc.world == null) {
            return;
        }
        if (!((Boolean)this.renderPos.get()).booleanValue()) {
            return;
        }
        if (this.frameBlocks.isEmpty() && this.interiorBlocks.isEmpty()) {
            return;
        }
        boolean bl = this.hasObstructingBlocks();
        SettingColor settingColor = bl ? OBSTRUCTION_LINE_COLOR : (SettingColor)this.renderColor.get();
        SettingColor settingColor2 = bl ? OBSTRUCTION_FILL_COLOR : (SettingColor)this.renderColor.get();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(this.frameBlocks.size() + this.interiorBlocks.size());
        arrayList.addAll(this.frameBlocks);
        arrayList.addAll(this.interiorBlocks);
        RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList, (Color)settingColor, (Color)settingColor2, ShapeMode.Lines);
    }

    static final class Phase
    extends Enum<Phase> {
        public static final /* enum */ Phase PlaceFrame = new Phase();
        public static final /* enum */ Phase LightPortal = new Phase();
        public static final /* enum */ Phase Verify = new Phase();
        private static final /* synthetic */ Phase[] $VALUES;

        public static Phase[] values() {
            return (Phase[])$VALUES.clone();
        }

        public static Phase valueOf(String string) {
            return Enum.valueOf(Phase.class, string);
        }

        private static /* synthetic */ Phase[] $values() {
            return new Phase[]{PlaceFrame, LightPortal, Verify};
        }

        static {
            $VALUES = Phase.$values();
        }
    }
}

