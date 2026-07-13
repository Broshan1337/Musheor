// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "kek-place" — a Grim-bypass air-placer. While the use key is held and a block/spawn-egg
 * item is in hand, it raycasts for a replaceable position (or the face adjacent to a hit
 * block) and places there via the off-hand swap trick, then sets the block locally.
 */
public class AirPlace extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();  // was: FvaNWO
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: Q90GLXQ0Pef

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder() // was: psJq59YIbp3Z
        .name("render").description("Renders an overlay where the block will be placed.").defaultValue(true).build());
    private final Setting<Boolean> customRange = sgGeneral.add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("custom-range").description("Use custom range for air place.").defaultValue(false).build());
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder() // was: rKbT3Ifwo
        .name("range").description("Custom range to place at.").visible(customRange::get).defaultValue(4.5).min(0.0).sliderMax(6.0).build());

    private BlockPos targetPos = null; // was: r7hOYIKN2
    private boolean wasPressed = false; // was: oZHMlTL
    private int holdTicks = 0;          // was: xQr5FhbwpQPWgIQ

    public AirPlace() {
        super(musheor.MAIN, "kek-place", "Bypasses grim to place blocks mid-air");
    }

    @Override
    public void onActivate() {
        this.wasPressed = false;
        this.holdTicks = 0;
        this.targetPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) { // was: FvaNWO(Post)
        if (mc.player == null || mc.world == null) return;
        this.targetPos = this.findAirPlaceTarget();
        boolean pressed = mc.options.useKey.isPressed();
        boolean justPressed = pressed && !this.wasPressed;
        this.wasPressed = pressed;
        if (!pressed) {
            this.holdTicks = 0;
        } else if (this.targetPos != null
            && (mc.player.getMainHandStack().getItem() instanceof BlockItem || mc.player.getMainHandStack().getItem() instanceof SpawnEggItem)
            && BlockUtils.canPlace(this.targetPos, true)) {
            if (justPressed) {
                this.holdTicks = 0;
                this.placeBlock(this.targetPos);
            } else if (++this.holdTicks >= 4) {
                this.holdTicks = 0;
                this.placeBlock(this.targetPos);
            }
        }
    }

    /** The replaceable position under the crosshair (or the face adjacent to a hit block), or null. */
    private BlockPos findAirPlaceTarget() { // was: FvaNWO()
        if (mc.player == null || mc.world == null) return null;
        double r = this.customRange.get() ? this.range.get() : mc.player.getBlockInteractionRange();
        if (mc.getCameraEntity() == null) return null;
        if (mc.getCameraEntity().raycast(r, 0.0F, false) instanceof BlockHitResult bhr) {
            BlockPos hitPos = bhr.getBlockPos();
            if (mc.world.getBlockState(hitPos).isReplaceable()) return hitPos;
            BlockPos adjacent = hitPos.offset(bhr.getSide());
            return mc.world.getBlockState(adjacent).isReplaceable() ? adjacent : null;
        }
        return null;
    }

    /** Places the held block at {@code pos} via the off-hand swap trick and mirrors it locally. */
    private void placeBlock(BlockPos pos) { // was: FvaNWO(BlockPos)
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.DOWN, pos, false);
        WorldUtils.swapHands();
        WorldUtils.sendInteract(Hand.OFF_HAND, hit);
        WorldUtils.swapHands();
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem bi) {
            mc.world.setBlockState(pos, bi.getBlock().getDefaultState(), 3);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (mc.player == null || mc.world == null || !this.render.get() || this.targetPos == null) return;
        if ((mc.player.getMainHandStack().getItem() instanceof BlockItem || mc.player.getMainHandStack().getItem() instanceof SpawnEggItem)
            && mc.world.getBlockState(this.targetPos).isReplaceable()) {
            RenderUtils.render(event, this.targetPos, Block.getBlockFromItem(mc.player.getMainHandStack().getItem()));
        }
    }
}
