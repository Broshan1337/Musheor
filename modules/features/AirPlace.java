// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import net.minecraft.util.Hand;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;

public class AirPlace
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final Setting<Boolean> render;
    private final Setting<Boolean> customRange;
    private final Setting<Double> range;
    private BlockPos targetPos;
    private boolean wasUseHeld;
    private int ticksHeld;

    public AirPlace() {
        super(musheor.MAIN, "kek-place", "Bypasses grim to place blocks mid-air");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.render = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where the block will be placed.")).defaultValue((Object)true)).build());
        this.customRange = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-range")).description("Use custom range for air place.")).defaultValue((Object)false)).build());
        this.range = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Custom range to place at.")).visible(() -> this.customRange.get())).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.targetPos = null;
        this.wasUseHeld = false;
        this.ticksHeld = 0;
    }

    public void onActivate() {
        this.wasUseHeld = false;
        this.ticksHeld = 0;
        this.targetPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (AirPlace.mc.player == null || AirPlace.mc.world == null) {
            return;
        }
        this.targetPos = this.getAirPlaceTarget();
        boolean bl = AirPlace.mc.options.useKey.isPressed();
        boolean bl2 = bl && !this.wasUseHeld;
        this.wasUseHeld = bl;
        if (!bl) {
            this.ticksHeld = 0;
            return;
        }
        if (this.targetPos == null) {
            return;
        }
        if (!(AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem) && !(AirPlace.mc.player.getMainHandStack().getItem() instanceof SpawnEggItem)) {
            return;
        }
        if (!BlockUtils.canPlace((BlockPos)this.targetPos, (boolean)true)) {
            return;
        }
        if (bl2) {
            this.ticksHeld = 0;
            this.placeBlock(this.targetPos);
        } else {
            ++this.ticksHeld;
            if (this.ticksHeld >= 4) {
                this.ticksHeld = 0;
                this.placeBlock(this.targetPos);
            }
        }
    }

    private BlockPos getAirPlaceTarget() {
        double d;
        if (AirPlace.mc.player == null || AirPlace.mc.world == null) {
            return null;
        }
        double d2 = d = (Boolean)this.customRange.get() != false ? ((Double)this.range.get()).doubleValue() : AirPlace.mc.player.getBlockInteractionRange();
        if (mc.getCameraEntity() == null) {
            return null;
        }
        HitResult hitResult = mc.getCameraEntity().raycast(d, 0.0f, false);
        if (!(hitResult instanceof BlockHitResult)) {
            return null;
        }
        BlockHitResult blockHit = (BlockHitResult)hitResult;
        BlockPos BlockPos2 = blockHit.getBlockPos();
        if (AirPlace.mc.world.getBlockState(BlockPos2).isReplaceable()) {
            return BlockPos2;
        }
        BlockPos BlockPos3 = BlockPos2.offset(blockHit.getSide());
        return AirPlace.mc.world.getBlockState(BlockPos3).isReplaceable() ? BlockPos3 : null;
    }

    private void placeBlock(BlockPos BlockPos2) {
        BlockHitResult blockHit = new BlockHitResult(Vec3d.ofCenter(BlockPos2), Direction.DOWN, BlockPos2, false);
        WorldUtils.swapCarriedItems();
        WorldUtils.sendPlacePacket(Hand.OFF_HAND, blockHit);
        WorldUtils.swapCarriedItems();
        Item ItemStack2 = AirPlace.mc.player.getMainHandStack().getItem(); // was: ItemStack ItemStack2 (CFR type error)
        if (ItemStack2 instanceof BlockItem) {
            BlockItem blockItem2 = (BlockItem)ItemStack2;
            AirPlace.mc.world.setBlockState(BlockPos2, blockItem2.getBlock().getDefaultState(), 3);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (AirPlace.mc.player == null || AirPlace.mc.world == null || !((Boolean)this.render.get()).booleanValue() || this.targetPos == null) {
            return;
        }
        if (!(AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem) && !(AirPlace.mc.player.getMainHandStack().getItem() instanceof SpawnEggItem)) {
            return;
        }
        if (!AirPlace.mc.world.getBlockState(this.targetPos).isReplaceable()) {
            return;
        }
        RenderUtils.jOdDDFXSeWl4(render3DEvent, this.targetPos, Block.getBlockFromItem(AirPlace.mc.player.getMainHandStack().getItem())); // was: (ItemStack) cast (CFR error); RenderUtils.jOdDDFXSeWl4 pending deobfuscation
    }
}
