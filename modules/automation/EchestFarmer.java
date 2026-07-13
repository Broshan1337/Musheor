// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.modules.features.KekMine;
import musheor.utils.PlayerUtils;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "obi-farmer" — an ender chest farmer that mines echests for obsidian, considering
 * the player's inventory space, with an optional 20-block/s instant-rebreak mode.
 */
public class EchestFarmer extends Module {
    private final Setting<Boolean> selfToggle = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("self-toggle").description("Disables when you reach the desired amount of ender chests.").defaultValue(true).build());
    private final Setting<Integer> amount = this.settings.getDefaultGroup().add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("amount").description("The amount of ender chests to farm.").defaultValue(64).sliderRange(1, 64).visible(selfToggle::get).build());
    private final Setting<Boolean> superFarm = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("super-farm-20bps").description("Uses instant rebreak when mining enderchests to speed up the farming process up to 20 blocks per second.").defaultValue(true).build());

    public static BlockPos targetPos;          // was: FvaNWO (static)
    private int minedCount;                     // was: oZHMlTL
    BlockPos startPos = null;                   // was: Q90GLXQ0Pef
    private boolean initialized = false;        // was: xQr5FhbwpQPWgIQ
    private boolean placedEchest = false;       // was: OMMZL1F3q
    private int swapDelayTicks;                 // was: zu3a44xDeMFMCRwm
    public static EchestFarmer INSTANCE;        // was: psJq59YIbp3Z (static)

    public EchestFarmer() {
        super(musheor.AUTOMATION, "obi-farmer", "An enderchest farmer that considers the player's inventory space when mining for obsidian, its also really fast");
        INSTANCE = this;
    }

    @Override
    public void onDeactivate() {
        targetPos = null;
        PlayerUtils.cancelPathing();
        this.swapDelayTicks = 0;
    }

    @Override
    public void onActivate() {
        targetPos = null;
        this.minedCount = 0;
        this.initialized = false;
        this.startPos = null;
        this.placedEchest = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        if (HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking() || SourceRemover.isRemoving()) return;
        if (this.swapDelayTicks > 0) { this.swapDelayTicks--; return; }
        if (!this.initialized) {
            this.startPos = this.mc.player.getBlockPos();
            if (((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) {
                targetPos = WorldUtils.offsetTwoBlocks(HighwayBuilder.getDirection());
            } else {
                targetPos = WorldUtils.offsetTwoBlocks(WorldUtils.getMovementDirection().opposite());
            }
            this.initialized = true;
            return;
        }
        boolean isEchestHere = this.mc.world.getBlockState(targetPos).getBlock() == Blocks.ENDER_CHEST;
        boolean isEmpty = this.mc.world.getBlockState(targetPos).isReplaceable();
        if ((!this.selfToggle.get() || this.minedCount < this.amount.get()) && InventoryManager.countPickaxes(false) != 0) {
            FindItemResult echest = InvUtils.findInHotbar(Items.ENDER_CHEST);
            if (!echest.found()) {
                if (InventoryManager.countItemInInventory(Items.ENDER_CHEST) < 8) {
                    state.setFlag12(false);
                    this.toggle();
                } else {
                    InventoryManager.moveToHotbar(Items.ENDER_CHEST);
                }
            } else if (this.superFarm.get()) {
                if (this.minedCount < 4) {
                    if (isEmpty && !this.placedEchest) {
                        if (this.mc.player.getMainHandStack().getItem() != Items.ENDER_CHEST) {
                            InventoryManager.selectItem(Items.ENDER_CHEST);
                            this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
                            return;
                        }
                        this.placeEchest();
                        this.placedEchest = true;
                    } else if (isEchestHere) {
                        if (KekMine.INSTANCE.isActive()) KekMine.INSTANCE.mine(targetPos, this.mc.world.getBlockState(targetPos));
                        else BlockUtils.breakBlock(targetPos, true);
                        this.placedEchest = false;
                    }
                } else {
                    if (isEmpty) this.placeEchest();
                    InventoryManager.selectBestPickaxe(false);
                    KekMine.INSTANCE.sendAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos);
                }
            } else {
                if (isEchestHere) {
                    InventoryManager.selectBestPickaxe(false);
                    if (KekMine.INSTANCE.isActive()) KekMine.INSTANCE.mine(targetPos, this.mc.world.getBlockState(targetPos));
                    else BlockUtils.breakBlock(targetPos, true);
                    this.placedEchest = false;
                    return;
                }
                if (isEmpty && !this.placedEchest) {
                    if (this.mc.player.getMainHandStack().getItem() != Items.ENDER_CHEST) {
                        InventoryManager.selectItem(Items.ENDER_CHEST);
                        this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
                        return;
                    }
                    this.placeEchest();
                    this.placedEchest = true;
                }
            }
        } else {
            MusheorSystem.debug("Mining last enderchest...");
            if (isEchestHere) {
                InventoryManager.selectBestPickaxe(false);
                if (KekMine.INSTANCE.isActive()) KekMine.INSTANCE.mine(targetPos, this.mc.world.getBlockState(targetPos));
                else BlockUtils.breakBlock(targetPos, true);
            } else if (isEmpty) {
                state.setFlag12(false);
                this.toggle();
            }
        }
    }

    /** Places an ender chest at {@code targetPos} using the off-hand swap trick. */
    private void placeEchest() { // was: FvaNWO()
        InventoryManager.selectItem(Items.ENDER_CHEST);
        WorldUtils.lookAtBlock(targetPos);
        WorldUtils.swapHands();
        WorldUtils.sendInteract(Hand.OFF_HAND, new BlockHitResult(Vec3d.ofCenter(targetPos), Direction.DOWN, targetPos, false));
        WorldUtils.swapHands();
        this.minedCount++;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (targetPos != null) {
            RenderUtils.render(event, targetPos, Color.WHITE, Color.WHITE, ShapeMode.Lines);
        }
    }
}
