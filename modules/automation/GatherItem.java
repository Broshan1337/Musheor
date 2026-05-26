// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.WorldUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;

/**
 * Helper module: finds nearby item entities of the configured type and
 * paths to them one-by-one using Baritone, collecting them in sorted order
 * (accessible blocks first, closest first).
 *
 * Used by HighwayBuilder to collect dropped obsidian/cobblestone.
 */
public class GatherItem extends Module {
    private int tickCounter = 0; // was: oq3TU4VRVWuh

    /** The player entity snapshot used for position queries. */
    private static LivingEntity playerRef; // was: CduCWLxmO

    /** Scheduled executor that drives the async path-then-collect loop. */
    private static ScheduledExecutorService scheduler; // was: aYWh0ZNA4Rd

    /**
     * Whether GatherItem was invoked from a context where Baritone should NOT
     * be stopped when the module toggles off (e.g. during restocking).
     */
    private static boolean keepPathing; // was: oknfyMh

    static {
        keepPathing = true;
    }

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Item> item = this.sgGeneral.add(
        new ItemSetting.Builder()
            .name("item")
            .description("Item to gather")
            .defaultValue(Items.COBBLESTONE)
            .build());

    private final Setting<Boolean> ignoreBelowPavement = this.sgGeneral.add(
        new BoolSetting.Builder()
            .name("ignore-below-y120")
            .description("Ignores item sitting below Y-level 120 to prevent the paver from digging itself underground")
            .defaultValue(true)
            .build());

    public GatherItem() {
        super(musheor.AUTOMATION, "pickup-item", "A helper module that find and picks up nearby items.");
    }

    @Override
    public void onActivate() {
        playerRef = this.mc.player;
        resetScheduler();
    }

    @Override
    public void onDeactivate() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        PlayerUtils.stopBaritone(); // was: JaevRTUQKWIx5LQ
    }

    private static void resetScheduler() { // was: MS1x7YGHjIg7eB
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    // -------------------------------------------------------------------------
    // Item scanning
    // -------------------------------------------------------------------------

    /**
     * Scans a 10-block radius around the player for ItemEntities of the
     * configured type. Returns a list of BlockPos sorted by accessibility
     * (accessible first) then by distance.
     */
    private List<BlockPos> findItemPositions() { // was: KDNrzlU9qtrEv
        if (playerRef == null || this.mc.world == null) return new ArrayList<BlockPos>();

        Item targetItem = (Item) this.item.get();
        Box searchBox = new Box( // AABB
            playerRef.getX() - 10, playerRef.getY() - 10, playerRef.getZ() - 10,
            playerRef.getX() + 10, 320.0, playerRef.getZ() + 10);

        ArrayList<ItemLocation> locations = new ArrayList<ItemLocation>();
        this.mc.world.getEntitiesByClass(ItemEntity.class, searchBox, entity -> entity.getStack().getItem() == targetItem)
            .forEach(entity -> {
                BlockPos pos = new BlockPos(
                    (int) Math.floor(entity.getX()),
                    (int) Math.floor(entity.getY()),
                    (int) Math.floor(entity.getZ()));
                if (((Boolean) this.ignoreBelowPavement.get()) && pos.getY() < 120) return; // getY()
                double dist = Math.sqrt(
                    Math.pow(playerRef.getX() - entity.getX(), 2) +
                    Math.pow(playerRef.getY() - entity.getY(), 2) +
                    Math.pow(playerRef.getZ() - entity.getZ(), 2));
                boolean accessible = ItemLocation.isAccessible((World) this.mc.world, pos);
                locations.add(new ItemLocation(pos, accessible, dist));
            });

        return locations.stream()
            .sorted(Comparator.comparing((ItemLocation l) -> !l.isAccessible)
                .thenComparing(l -> l.distanceToPlayer))
            .<BlockPos>map(l -> l.pos)
            .toList();
    }

    private boolean inventoryFull() { // was: WOqvNwnejoKApoa
        return InventoryManager.countShulkerBoxes() == 0; // was: ZeOLrA
    }

    // -------------------------------------------------------------------------
    // Collect loop
    // -------------------------------------------------------------------------

    /**
     * Recursive async collect loop.
     * Paths toward items[index], waits 50 ms, then either advances to the
     * next item (if collected) or retries the same one.
     */
    private void collectNext(List<BlockPos> items, int index) { // was: jOdDDFXSeWl4(List,int)
        if (scheduler == null || scheduler.isShutdown()) return;
        if (index >= items.size()) {
            startCollection();
            return;
        }
        if (this.inventoryFull()) {
            PlayerUtils.noOp(keepPathing); // was: usJLOV0subXO3(boolean)
            scheduler.shutdownNow();
            return;
        }
        BlockPos target = items.get(index);
        if (target != null && !(Modules.get().get(EchestFarmer.class).isActive() && HighwayBuilder.isRestocking())) {
            BaritoneAPI.getProvider().getPrimaryBaritone()
                .getCustomGoalProcess().setGoalAndPath((Goal) new GoalBlock(target));
            PlayerUtils.resumeBaritone(); // was: ZjVmRLiAeys38
        }
        scheduler.schedule(() -> {
            if (HighwayBuilder.isRestocking()) return;
            if (isAtPos(playerRef, target)) {
                scheduler.schedule(this::startCollection, 50L, TimeUnit.MILLISECONDS);
            } else {
                scheduler.schedule(() -> this.collectNext(items, index), 50L, TimeUnit.MILLISECONDS);
            }
        }, 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAtPos(LivingEntity player, BlockPos pos) {
        return player != null && player.getBlockPos().isWithinDistance((Vec3i) pos, 1.0);
    }

    /** Restarts the scheduler and resumes Baritone, then calls startCollection. */
    public void restart() { // was: Tne1O2a8S2sVbX
        resetScheduler();
        PlayerUtils.resumeBaritone();
        this.startCollection();
    }

    private void startCollection() { // was: bGqPXJzBtf
        List<BlockPos> items = this.findItemPositions();
        if (items.isEmpty() || this.inventoryFull()) {
            if (items.isEmpty()) {
                ChatUtils.info("No more " + ((Item) this.item.get()).getName().getString() + " found nearby.", new Object[0]);
            } else {
                ChatUtils.info("No more space in inventory.", new Object[0]);
            }
            PlayerUtils.noOp(keepPathing);
            this.toggle();
            return;
        }
        this.collectNext(items, 0);
    }

    // -------------------------------------------------------------------------
    // Tick event
    // -------------------------------------------------------------------------

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (WorldUtils.checkForLag()) return; // was: btLCQHvKVR
        Module echestFarmer = Modules.get().get(EchestFarmer.class);
        if (echestFarmer != null && echestFarmer.isActive()) return;

        List<BlockPos> items = this.findItemPositions();
        if (items.isEmpty() || this.inventoryFull()) {
            PlayerUtils.noOp(keepPathing);
            MusheorSystem.debug("Toggling off gather item...", new Object[0]);
            this.toggle();
            return;
        }
        if (this.mc.player != null) {
            ++this.tickCounter;
            if (this.tickCounter % 2 == 0 && WorldUtils.allHaveSupport(this.findItemPositions())) { // was: UgB10d(List)
                this.restart();
            }
        }
    }

    // -------------------------------------------------------------------------
    // ItemLocation record
    // -------------------------------------------------------------------------

    /**
     * Internal record: holds a BlockPos, whether it is accessible (not covered
     * by a solid block), and the distance to the player.
     */
    public static final class ItemLocation extends Record {
        final BlockPos pos;             // was: J6PuzyzqvmhV
        final boolean    isAccessible;    // was: yaVvWAqooeFn
        final double     distanceToPlayer; // was: fGLoB1zTvFf

        public ItemLocation(BlockPos pos, boolean isAccessible, double distanceToPlayer) {
            this.pos             = pos;
            this.isAccessible    = isAccessible;
            this.distanceToPlayer = distanceToPlayer;
        }

        /**
         * Returns true if the block at {@code pos} in {@code world} is passable
         * (solid shape is empty, or the block is air).
         */
        public static boolean isAccessible(World world, BlockPos pos) {
            BlockState state = world.getBlockState(pos);
            return state.isAir()      // isLiquid
                || state.isReplaceable()       // replaceable
                || !state.getCollisionShape(null, null).isEmpty();
        }

        @Override public final String  toString() { return ObjectMethods.bootstrap("toString",  new MethodHandle[]{ItemLocation.class, "pos;isAccessible;distanceToPlayer", "pos", "isAccessible", "distanceToPlayer"}, this); }
        @Override public final int     hashCode() { return (int) ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ItemLocation.class, "pos;isAccessible;distanceToPlayer", "pos", "isAccessible", "distanceToPlayer"}, this); }
        @Override public final boolean equals(Object o) { return (boolean) ObjectMethods.bootstrap("equals", new MethodHandle[]{ItemLocation.class, "pos;isAccessible;distanceToPlayer", "pos", "isAccessible", "distanceToPlayer"}, this, o); }
    }
}
