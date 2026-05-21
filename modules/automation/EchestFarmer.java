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
import net.minecraft.class_1268;   // Hand
import net.minecraft.ItemStack;   // Item
import net.minecraft.Items;   // Items
import net.minecraft.Blocks;   // Blocks
import net.minecraft.BlockPos;   // BlockPos
import net.minecraft.Direction;   // Direction
import net.minecraft.class_2382;   // Vec3i
import net.minecraft.class_243;    // Vec3d
import net.minecraft.class_2846;   // PlayerActionC2SPacket
import net.minecraft.Screen;   // BlockHitResult

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
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) return;
        HighwayState state = HighwayState.getInstance(); // was: LmpuWjra

        WorldUtils.isScreenOpen(); // was: btLCQHvKVR (side effect: checks screen state)

        if (HighwayBuilder.isEating() || HighwayBuilder.isWaiting() || SourceRemover.isActive()) return;

        // --- First tick: determine the placement position ---
        if (!this.posInitialised) {
            this.sessionStartPos = this.mc.player.getBlockPos(); // getBlockPos()
            // Place relative to direction if in HighwayBuilder, otherwise look direction
            if (((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) {
                echestPos = WorldUtils.getBlockInFront(HighwayBuilder.getDirection()); // was: jOdDDFXSeWl4(Direction8)
            } else {
                echestPos = WorldUtils.getBlockInFront(WorldUtils.getPlayerFacing().toOppositeDirection()); // was: eQlnaotm4pUDUmJT().gkoa4kDDOuwRuB64()
            }
            this.posInitialised = true;
            return;
        }

        boolean isAir    = this.mc.world.getBlockState(echestPos).getBlock() == Blocks.field_10443; // Blocks.AIR
        boolean isLiquid = this.mc.world.getBlockState(echestPos).method_45474(); // isAir (double-check)

        // --- Stop condition: farmed enough or inventory full ---
        if (((Boolean) this.selfToggle.get()) && this.farmedCount >= (Integer) this.amount.get()
                || InventoryManager.countPickaxes(false) == 0) { // was: VYEwzRq(false)
            MusheorSystem.debug("Mining last enderchest...", new Object[0]);
            if (isAir) {
                InventoryManager.equipBestTool(false); // was: vgrtgn5(false)
                if (KekMine.INSTANCE.isActive()) {
                    KekMine.INSTANCE.startBreak(echestPos, this.mc.world.getBlockState(echestPos)); // was: TAdu5cndwWu3A1
                } else {
                    BlockUtils.breakBlock((BlockPos) echestPos, true);
                }
                return;
            }
            if (isLiquid) {
                state.setAutoWalkEnabled(false); // was: MS1x7YGHjIg7eB(false)
                this.toggle();
            }
            return;
        }

        // Ensure obsidian is in hotbar
        FindItemResult obsidian = InvUtils.findInHotbar(new ItemStack[]{Items.field_8466}); // OBSIDIAN
        if (!obsidian.found()) {
            if (InventoryManager.countItem(Items.field_8466) < 8) { // was: usJLOV0subXO3
                state.setAutoWalkEnabled(false);
                this.toggle();
                return;
            }
            InventoryManager.equipItem(Items.field_8466); // was: UgB10d
            return;
        }

        // --- Super-farm mode ---
        if (((Boolean) this.superFarm.get())) {
            if (this.farmedCount < 4) {
                if (isLiquid && !this.lastBreakWasSuccessful) {
                    placeEnderChest();
                    this.lastBreakWasSuccessful = true;
                } else if (isAir) {
                    if (KekMine.INSTANCE.isActive()) {
                        KekMine.INSTANCE.startBreak(echestPos, this.mc.world.getBlockState(echestPos));
                    } else {
                        BlockUtils.breakBlock((BlockPos) echestPos, true);
                    }
                    this.lastBreakWasSuccessful = false;
                }
            } else {
                if (isLiquid) placeEnderChest();
                InventoryManager.equipBestTool(false);
                KekMine.INSTANCE.startBreakAction(class_2846.class_2847.field_12973, echestPos); // was: jOdDDFXSeWl4(PlayerAction,BlockPos)
            }
        } else {
            // Normal mode: break then place
            if (isAir) {
                InventoryManager.equipBestTool(false);
                if (KekMine.INSTANCE.isActive()) {
                    KekMine.INSTANCE.startBreak(echestPos, this.mc.world.getBlockState(echestPos));
                } else {
                    BlockUtils.breakBlock((BlockPos) echestPos, true);
                }
                this.lastBreakWasSuccessful = false;
                return;
            }
            if (isLiquid && !this.lastBreakWasSuccessful) {
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
        InventoryManager.equipItem(Items.field_8466); // obsidian to main hand
        WorldUtils.highlightBlock(echestPos); // was: MS1x7YGHjIg7eB (render)
        WorldUtils.swapCarriedItems(); // was: l3ot1CwoJ9CsS
        WorldUtils.placeBlock(class_1268.field_5810,
            new Screen(class_243.method_24953((class_2382) echestPos), Direction.field_11033, echestPos, false)); // DOWN face
        WorldUtils.swapCarriedItems();
        ++this.farmedCount;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (echestPos == null) return;
        RenderUtils.renderBlock(event, echestPos, Color.WHITE, Color.WHITE, ShapeMode.Lines); // was: jOdDDFXSeWl4
    }
}
