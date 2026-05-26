// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import musheor.modules.features.KekMine;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Farms ender chests by placing them and immediately mining them for obsidian.
 *
 * "Super farm" mode (default): uses instant re-break (KekMine) to achieve
 * up to 20 ender chests mined per second by alternating place/break without
 * waiting for server confirmation.
 *
 * Normal mode: waits for the block to break (air) before placing the next one.
 *
 * Stops automatically when:
 *   - The configured `amount` of ender chests has been farmed
 *   - No obsidian is left in hotbar/inventory (tries to equip from inventory first)
 *   - There are no free inventory slots (zero durability tools / full inventory)
 */
public class EchestFarmer extends Module {
    private final Setting<Boolean> selfToggle;
    private final Setting<Integer> amount;
    private final Setting<Boolean> superFarm;

    /** The BlockPos where the ender chest is placed each cycle. */
    public static BlockPos echestPos; // was: zl2vxyh

    /** Number of ender chests successfully placed this session. */
    private int farmedCount; // was: kLIvClyeu

    /** Starting position of the player when this session began. */
    BlockPos sessionStartPos; // was: IuR8CfqY

    private boolean posInitialised;         // was: cghz8iox35K
    private boolean lastBreakWasSuccessful; // was: CcyVC0KkRVrmqA

    /** Singleton reference for external access. */
    public static EchestFarmer INSTANCE; // was: Nr0B0YDZRAaA

    public EchestFarmer() {
        super(musheor.AUTOMATION, "obi-farmer",
            "An enderchest farmer that considers the player's inventory space when mining for obsidian, its also really fast");
        this.selfToggle = this.settings.getDefaultGroup().add(new BoolSetting.Builder()
            .name("self-toggle")
            .description("Disables when you reach the desired amount of ender chests.")
            .defaultValue(true).build());
        this.amount = this.settings.getDefaultGroup().add(new IntSetting.Builder()
            .name("amount")
            .description("The amount of ender chests to farm.")
            .defaultValue(64).sliderRange(1, 64)
            .visible(() -> this.selfToggle.get())
            .build());
        this.superFarm = this.settings.getDefaultGroup().add(new BoolSetting.Builder()
            .name("super-farm-20bps")
            .description("Uses instant rebreak when mining enderchests to speed up the farming process up to 20 blocks per second.")
            .defaultValue(true).build());
        this.sessionStartPos = null;
        this.posInitialised  = false;
        this.lastBreakWasSuccessful = false;
        INSTANCE = this;
    }

    @Override
    public void onDeactivate() {
        echestPos = null;
        PlayerUtils.stopBaritone(); // was: JaevRTUQKWIx5LQ
    }

    @Override
    public void onActivate() {
        echestPos       = null;
        this.farmedCount = 0;
        this.posInitialised  = false;
        this.sessionStartPos = null;
        this.lastBreakWasSuccessful = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) return;
        HighwayState state = HighwayState.getInstance();

        WorldUtils.checkForLag();

        if (HighwayBuilder.isEating() || HighwayBuilder.isAttacking() || SourceRemover.fXEQFU()) return;

        // --- First tick: determine the placement position ---
        if (!this.posInitialised) {
            this.sessionStartPos = this.mc.player.getBlockPos();
            if (((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) {
                echestPos = WorldUtils.getOffset2AheadPos(HighwayBuilder.getDirection());
            } else {
                echestPos = WorldUtils.getOffset2AheadPos(WorldUtils.getPlayerFacing().opposite());
            }
            this.posInitialised = true;
            return;
        }

        boolean isEnderChest = this.mc.world.getBlockState(echestPos).getBlock() == Blocks.ENDER_CHEST;
        boolean isAir        = this.mc.world.getBlockState(echestPos).isAir();

        // --- Stop condition: farmed enough or no pickaxes left ---
        if (((Boolean) this.selfToggle.get()) && this.farmedCount >= (Integer) this.amount.get()
                || InventoryManager.countPickaxes(false) == 0) {
            MusheorSystem.debug("Mining last enderchest...", new Object[0]);
            if (isEnderChest) {
                InventoryManager.equipBestPickaxe(false);
                if (KekMine.INSTANCE.isActive()) {
                    KekMine.INSTANCE.queueBlock(echestPos, this.mc.world.getBlockState(echestPos));
                } else {
                    BlockUtils.breakBlock((BlockPos) echestPos, true);
                }
                return;
            }
            if (isAir) {
                state.setResupplyActive(false);
                this.toggle();
            }
            return;
        }

        // --- Ensure ender chest is in hotbar ---
        FindItemResult echestResult = InvUtils.findInHotbar(new Item[]{Items.ENDER_CHEST});
        if (!echestResult.found()) {
            if (InventoryManager.countItemInInventory(Items.ENDER_CHEST) < 8) {
                state.setResupplyActive(false);
                this.toggle();
                return;
            }
            InventoryManager.moveItemToHotbar(Items.ENDER_CHEST);
            return;
        }

        // --- Super-farm mode ---
        if (((Boolean) this.superFarm.get())) {
            if (this.farmedCount < 4) {
                if (isAir && !this.lastBreakWasSuccessful) {
                    placeEnderChest();
                    this.lastBreakWasSuccessful = true;
                } else if (isEnderChest) {
                    if (KekMine.INSTANCE.isActive()) {
                        KekMine.INSTANCE.queueBlock(echestPos, this.mc.world.getBlockState(echestPos));
                    } else {
                        BlockUtils.breakBlock((BlockPos) echestPos, true);
                    }
                    this.lastBreakWasSuccessful = false;
                }
            } else {
                if (isAir) placeEnderChest();
                InventoryManager.equipBestPickaxe(false);
                KekMine.INSTANCE.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, echestPos);
            }
        } else {
            // Normal mode: break then place
            if (isEnderChest) {
                InventoryManager.equipBestPickaxe(false);
                if (KekMine.INSTANCE.isActive()) {
                    KekMine.INSTANCE.queueBlock(echestPos, this.mc.world.getBlockState(echestPos));
                } else {
                    BlockUtils.breakBlock((BlockPos) echestPos, true);
                }
                this.lastBreakWasSuccessful = false;
                return;
            }
            if (isAir && !this.lastBreakWasSuccessful) {
                placeEnderChest();
                this.lastBreakWasSuccessful = true;
            }
        }
    }

    /**
     * Places an ender chest at {@code echestPos} using the main hand + offhand swap,
     * then increments the counter.
     */
    private void placeEnderChest() { // was: CEOjBr5G5R
        InventoryManager.equipItem(Items.ENDER_CHEST);
        WorldUtils.lookAtBlock(echestPos);
        WorldUtils.swapCarriedItems();
        WorldUtils.sendPlacePacket(Hand.OFF_HAND,
            new BlockHitResult(Vec3d.ofCenter(echestPos), Direction.DOWN, echestPos, false));
        WorldUtils.swapCarriedItems();
        ++this.farmedCount;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (echestPos == null) return;
        RenderUtils.jOdDDFXSeWl4(event, echestPos, Color.WHITE, Color.WHITE, ShapeMode.Lines);
    }
}