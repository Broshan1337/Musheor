// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.features;

import java.util.ArrayDeque;
import java.util.Deque;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.InventoryManager;
import musheor.modules.automation.KekNuker;
import musheor.utils.RenderUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

/**
 * "KekMine" — a Grim-safe packet miner. Breaks blocks by sending
 * START/STOP_DESTROY_BLOCK packets directly (rather than holding the pickaxe), with:
 * an optional queue for extra targets, "double break" (mine two blocks at once by
 * keeping a primary + secondary context), silent tool swaps, and auto-rebreak of the
 * last block if it gets replaced. Timing/thresholds come from {@link MusheorSystem}.
 */
public class KekMine extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: rKbT3Ifwo
    private final SettingGroup sgRender = this.settings.createGroup("Render"); // was: r7hOYIKN2

    public final Setting<Boolean> autoRebreak = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: FvaNWO
        .name("auto-rebreak").description("Automatically rebreak the last block in case it gets replaced").defaultValue(false).build());
    private final Setting<Boolean> silentSwap = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: oZHMlTL
        .name("silent-swap").description("Breaks the block without holding the pickaxe").defaultValue(false).build());
    private final Setting<Boolean> globalRendering = sgRender.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("global-rendering").defaultValue(true).description("Synchronize rendering with Musheor-Tab").build());
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder() // was: OMMZL1F3q
        .name("color").defaultValue(new SettingColor(Color.cyan)).description("Custom color for rendering (lines / wireframe)")
        .visible(() -> !globalRendering.get()).build());

    public static KekMine INSTANCE;                     // was: Q90GLXQ0Pef (static)
    private MineContext primary;                        // was: zu3a44xDeMFMCRwm (block currently being mined)
    private MineContext secondary;                      // was: krxNb5lcQuWA (second block, when double-break is on)
    public BlockPos lastBrokenPos;                      // was: psJq59YIbp3Z (last completed break, for auto-rebreak)
    public final Deque<BlockPos> mineQueue = new ArrayDeque<>(); // was: SOYyh5IPg26f7F
    private int savedSlot = -1;                         // was: nt0HZnvBBp  (hotbar slot to restore after a silent swap)
    private int slotRestoreTick = -1;                   // was: amz3UB1vE   (tick at which to restore savedSlot)
    private int tickCounter = 0;                        // was: sBBIyQG5NWq0K

    public KekMine() {
        super(musheor.MAIN, "KekMine", "Grim-safe packet miner with queue and double break.");
        INSTANCE = this;
    }

    /** Begins mining {@code pos} (fetching its state) if it is reachable and not already targeted. */
    public void mine(BlockPos pos) { // was: FvaNWO(BlockPos)
        if (mc.world != null && BlockUtils.canBreak(pos, mc.world.getBlockState(pos))
            && !this.isOutOfRange(pos) && !this.isTargeting(pos)) {
            this.mine(pos, mc.world.getBlockState(pos));
        }
    }

    /** True if {@code pos} is the primary, secondary, or a queued target. */
    public boolean isTargeting(BlockPos pos) { // was: Q90GLXQ0Pef(BlockPos)
        if (this.primary != null && this.primary.pos.equals(pos)) return true;
        if (this.secondary != null && this.secondary.pos.equals(pos)) return true;
        return this.mineQueue.contains(pos);
    }

    /** True when no block is currently being mined but the queue still has work to pull. */
    public boolean isReadyToDequeue() { // was: FvaNWO()
        return this.primary == null && this.secondary == null && !this.mineQueue.isEmpty();
    }

    /** Statically begins mining {@code pos} from above. */
    public static void breakBlock(BlockPos pos) { // was: psJq59YIbp3Z(BlockPos)
        breakBlock(pos, Direction.UP);
    }

    /** Statically mines {@code pos} in a single call (start + immediate completion). */
    public static void breakBlock(BlockPos pos, Direction dir) { // was: FvaNWO(BlockPos,Direction)
        if (!INSTANCE.isTargeting(pos) && pos != null) {
            MineContext ctx = new MineContext(pos, mc.world.getBlockState(pos), true);
            if (!INSTANCE.silentSwap.get()) ensureBestTool(pos, mc.world.getBlockState(pos));
            INSTANCE.sendStartMining(pos, dir);
            INSTANCE.completeBreak(ctx, INSTANCE.silentSwap.get());
        }
    }

    @Override
    public void onDeactivate() {
        if (this.savedSlot >= 0) this.sendSlotUpdate(this.savedSlot);
        this.primary = null;
        this.secondary = null;
        this.mineQueue.clear();
        this.lastBrokenPos = null;
        this.savedSlot = -1;
        this.slotRestoreTick = -1;
        this.tickCounter = 0;
    }

    /** Selects the best tool for {@code state} if it is not already in the main hand. */
    private static void ensureBestTool(BlockPos pos, BlockState state) { // was: Q90GLXQ0Pef(BlockPos,BlockState)
        if (mc.player.getMainHandStack() != InventoryManager.findBestTool(state)) {
            InventoryManager.selectBestToolFor(pos);
        }
    }

    /** Returns the hotbar slot (0-8) whose item mines {@code state} fastest. */
    private int getBestToolSlot(BlockState state) { // was: FvaNWO(BlockState)
        if (mc.player == null) return 0;
        float bestSpeed = -1.0F;
        int bestSlot = mc.player.getInventory().selectedSlot;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    /** Begins mining {@code pos}/{@code state} from above. */
    public void mine(BlockPos pos, BlockState state) { // was: FvaNWO(BlockPos,BlockState)
        this.mine(pos, state, Direction.UP);
    }

    /**
     * Begins mining {@code pos}. If a block is already being mined and double-break is on,
     * the current primary is promoted to secondary and this becomes the new primary;
     * otherwise the position is queued.
     */
    public void mine(BlockPos pos, BlockState state, Direction dir) { // was: FvaNWO(BlockPos,BlockState,Direction)
        if (this.isTargeting(pos) || HighwayBuilder.isEating()) return;
        if (this.primary == null || this.secondary == null && MusheorSystem.Manager.doubleBreak.get()) {
            if (this.primary == null) {
                if (!this.silentSwap.get()) ensureBestTool(pos, state);
                this.primary = new MineContext(pos, state, true);
                this.sendStartMining(pos, dir);
            } else if (MusheorSystem.Manager.doubleBreak.get() && this.secondary == null) {
                this.sendAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.primary.pos,
                    KekNuker.getClosestFace(mc.player.getEyePos(), this.primary.pos));
                long savedStartTime = this.primary.startTime;
                this.secondary = new MineContext(this.primary.pos, this.primary.state, false);
                this.secondary.startTime = savedStartTime;
                this.primary = new MineContext(pos, state, true);
                this.sendStartMining(this.primary.pos, dir);
            }
        } else if (!this.mineQueue.contains(pos)) {
            this.mineQueue.addLast(pos);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (mc.player == null || mc.world == null) return;
        this.tickCounter++;
        if (this.savedSlot >= 0 && this.tickCounter >= this.slotRestoreTick) {
            this.sendSlotUpdate(this.savedSlot);
            this.savedSlot = -1;
        }

        if (HighwayBuilder.isEating() || mc.player.isUsingItem()) return;

        if (this.lastBrokenPos != null && this.autoRebreak.get()
            && this.primary == null && this.secondary == null
            && !mc.world.getBlockState(this.lastBrokenPos).isAir()) {
            this.finishBreak(new MineContext(this.lastBrokenPos, mc.world.getBlockState(this.lastBrokenPos), false), this.silentSwap.get());
        } else {
            this.pumpQueue();
            if (this.secondary != null && this.secondary.getProgress() >= 1.0) this.completeBreak(this.secondary, this.silentSwap.get());
            if (this.primary != null && this.primary.getProgress() >= 1.0) this.completeBreak(this.primary, this.silentSwap.get());
            this.pruneBrokenTargets();
        }
    }

    /** Drops finished/replaced positions from the primary, secondary, and queue. */
    private void pruneBrokenTargets() { // was: SOYyh5IPg26f7F()
        if (this.primary != null && this.isBrokenOrUnreachable(this.primary.pos)) this.primary = null;
        if (this.secondary != null && this.isBrokenOrUnreachable(this.secondary.pos)) this.secondary = null;
        this.mineQueue.removeIf(this::isBrokenOrUnreachable);
    }

    /** True if {@code pos} is now air or out of reach. */
    private boolean isBrokenOrUnreachable(BlockPos pos) { // was: rKbT3Ifwo(BlockPos)
        BlockState state = mc.world.getBlockState(pos);
        return state.isAir() || this.isOutOfRange(pos);
    }

    /** Pulls the next position(s) from the queue into the primary/secondary contexts. */
    private void pumpQueue() { // was: rKbT3Ifwo()
        if (this.mineQueue.isEmpty()) return;
        if (this.primary == null) {
            BlockPos pos = this.mineQueue.pollFirst();
            BlockState state = mc.world.getBlockState(pos);
            if (!this.silentSwap.get()) ensureBestTool(pos, state);
            this.primary = new MineContext(pos, state, true);
            this.sendStartMining(this.primary.pos, KekNuker.getClosestFace(mc.player.getEyePos(), this.primary.pos));
        } else if (MusheorSystem.Manager.doubleBreak.get() && this.secondary == null) {
            this.sendAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.primary.pos,
                KekNuker.getClosestFace(mc.player.getEyePos(), this.primary.pos));
            BlockPos nextPos = this.mineQueue.pollFirst();
            BlockState nextState = mc.world.getBlockState(nextPos);
            long savedStartTime = this.primary.startTime;
            this.secondary = new MineContext(this.primary.pos, this.primary.state, false);
            this.secondary.startTime = savedStartTime;
            this.primary = new MineContext(nextPos, nextState, true);
            this.sendStartMining(this.primary.pos, KekNuker.getClosestFace(mc.player.getEyePos(), this.primary.pos));
        }
    }

    /** Sends the START_DESTROY_BLOCK packet (preceded by a STOP for Grim's bypass). */
    private void sendStartMining(BlockPos pos, Direction dir) { // was: Q90GLXQ0Pef(BlockPos,Direction)
        if (MusheorSystem.Manager.grimBypass.get()) {
            this.sendAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, dir);
        }
        this.sendAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, dir);
    }

    /** Sends a finishing STOP_DESTROY_BLOCK for {@code ctx} (used by auto-rebreak), swapping tools if silent. */
    private void finishBreak(MineContext ctx, boolean silent) { // was: FvaNWO(MineContext,boolean)
        if (mc.world == null || mc.player == null) return;
        Direction dir = KekNuker.getClosestFace(mc.player.getEyePos(), ctx.pos);
        int prevSlot = this.savedSlot >= 0 ? this.savedSlot : mc.player.getInventory().selectedSlot;
        int bestSlot = silent ? this.getBestToolSlot(ctx.state) : prevSlot;
        boolean needSwap = silent && bestSlot != prevSlot;
        if (needSwap) this.sendSlotUpdate(bestSlot);
        this.sendAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, ctx.pos, dir);
        if (needSwap) this.scheduleSlotRestore(prevSlot);
    }

    /** Completes a break: notifies stats, sends STOP, optionally clears the block locally, and frees the context. */
    private void completeBreak(MineContext ctx, boolean silent) { // was: Q90GLXQ0Pef(MineContext,boolean)
        if (mc.world == null || mc.player == null) return;
        if (this.primary != null) HighwayBuilder.countBrokenBlock(this.primary.state);
        if (this.secondary != null) HighwayBuilder.countBrokenBlock(this.secondary.state);

        Direction dir = KekNuker.getClosestFace(mc.player.getEyePos(), ctx.pos);
        int prevSlot = this.savedSlot >= 0 ? this.savedSlot : mc.player.getInventory().selectedSlot;
        int bestSlot = silent ? this.getBestToolSlot(ctx.state) : prevSlot;
        // When finishing the secondary while a primary is still active, keep the primary's tool selected.
        if (silent && ctx == this.secondary && this.primary != null) {
            int primaryBest = this.getBestToolSlot(this.primary.state);
            if (primaryBest != bestSlot) bestSlot = primaryBest;
        }

        boolean needSwap = silent && bestSlot != prevSlot;
        if (!ctx.instaBreak) {
            if (needSwap) this.sendSlotUpdate(bestSlot);
            this.sendAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, ctx.pos, dir);
        } else if (needSwap) {
            this.sendSlotUpdate(bestSlot);
        }

        if (needSwap) this.scheduleSlotRestore(prevSlot);

        // Instant / threshold-reached blocks: clear locally without waiting for the server ack.
        if ((ctx.instaBreak || ctx.reachedThreshold) && !MusheorSystem.Manager.validateBreak.get()) {
            mc.world.syncWorldEvent(2001, ctx.pos, Block.getRawIdFromState(ctx.state));
            mc.world.setBlockState(ctx.pos, Blocks.AIR.getDefaultState(), 3);
        }

        this.lastBrokenPos = ctx.pos;
        ctx.active = false;
        if (ctx == this.primary) this.primary = null;
        else if (ctx == this.secondary) this.secondary = null;
    }

    /** Restores {@code prevSlot} now, or schedules it {@code holdTicks} ticks out. */
    private void scheduleSlotRestore(int prevSlot) {
        int hold = MusheorSystem.Manager.holdTicks.get();
        if (hold > 0) {
            this.savedSlot = prevSlot;
            this.slotRestoreTick = this.tickCounter + hold;
        } else {
            this.sendSlotUpdate(prevSlot);
            this.savedSlot = -1;
        }
    }

    /** Sends an UpdateSelectedSlot packet without changing the visible client-side slot. */
    private void sendSlotUpdate(int slot) { // was: FvaNWO(int)
        if (mc.getNetworkHandler() != null) mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    /** Sends a player action packet targeting {@code pos} from above. */
    public void sendAction(PlayerActionC2SPacket.Action action, BlockPos pos) { // was: FvaNWO(Action,BlockPos)
        this.sendAction(action, pos, Direction.UP);
    }

    /** Sends a sequenced player action packet targeting {@code pos} on face {@code dir}. */
    public void sendAction(PlayerActionC2SPacket.Action action, BlockPos pos, Direction dir) { // was: FvaNWO(Action,BlockPos,Direction)
        if (mc.interactionManager != null && mc.world != null) {
            mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerActionC2SPacket(action, pos, dir, sequence));
        }
    }

    /** True if {@code pos} lies beyond the KekNuker range (+1) from the player's eyes. */
    public boolean isOutOfRange(BlockPos pos) { // was: SOYyh5IPg26f7F(BlockPos)
        return mc.player.getEyePos().distanceTo(pos.toCenterPos())
            > ((KekNuker) Modules.get().get(KekNuker.class)).range.get() + 1.0;
    }

    /** The block currently being mined (primary), or null. */
    public BlockPos getCurrentTarget() { // was: Q90GLXQ0Pef()
        return this.primary != null ? this.primary.pos : null;
    }

    /** The second block being mined (double-break), or null. */
    public BlockPos getPendingTarget() { // was: psJq59YIbp3Z()
        return this.secondary != null ? this.secondary.pos : null;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (mc.player == null || mc.world == null) return;

        if (!((KekNuker) Modules.get().get(KekNuker.class)).isActive()) {
            if (this.globalRendering.get()) {
                RenderUtils.render(event, this.mineQueue.stream().toList());
            } else {
                RenderUtils.render(event, this.mineQueue.stream().toList(), Color.WHITE, Color.WHITE, ShapeMode.Lines);
            }
        }

        if (this.secondary != null) renderContext(event, this.secondary);
        if (this.primary != null) renderContext(event, this.primary);

        if (this.lastBrokenPos != null && this.autoRebreak.get() && !mc.world.getBlockState(this.lastBrokenPos).isAir()) {
            if (this.globalRendering.get()) {
                RenderUtils.render(event, this.lastBrokenPos, mc.world.getBlockState(this.lastBrokenPos).getBlock());
            } else {
                RenderUtils.render(event, this.lastBrokenPos, this.color.get(), this.color.get(), ShapeMode.Lines);
            }
        }
    }

    /** Renders a single mining context, honouring the global-rendering toggle. */
    private void renderContext(Render3DEvent event, MineContext ctx) {
        if (this.globalRendering.get()) {
            this.renderProgress(event, ctx, MusheorSystem.Manager.renderSideColor.get(),
                MusheorSystem.Manager.renderLineColor.get(), MusheorSystem.Manager.renderShape.get());
        } else {
            this.renderProgress(event, ctx, this.color.get(), this.color.get(), ShapeMode.Lines);
        }
    }

    /** Draws a break box for {@code ctx} that shrinks toward its centre as progress approaches 1. */
    private void renderProgress(Render3DEvent event, MineContext ctx, Color side, Color line, ShapeMode shape) { // was: FvaNWO(event,MineContext,Color,Color,ShapeMode)
        double offset = (1.0 - ctx.getProgress()) / 2.0;
        Box box = new Box(
            ctx.pos.getX() + offset, ctx.pos.getY() + offset, ctx.pos.getZ() + offset,
            ctx.pos.getX() + 1.0 - offset, ctx.pos.getY() + 1.0 - offset, ctx.pos.getZ() + 1.0 - offset);
        event.renderer.box(box, side, line, shape, 0);
    }

    /**
     * A single block being mined. Tracks the block, its start time, and precomputed flags
     * ({@code instaBreak}, {@code reachedThreshold}) so progress can be estimated client-side.
     */
    public static class MineContext {
        public final BlockPos pos;               // was: FvaNWO
        public final BlockState state;           // was: Q90GLXQ0Pef
        public long startTime;                   // was: psJq59YIbp3Z
        public final float hardness;             // was: SOYyh5IPg26f7F
        public boolean active = true;            // was: rKbT3Ifwo
        public final boolean isPrimary;          // was: r7hOYIKN2
        public final boolean instaBreak;         // was: oZHMlTL       (can be broken instantly)
        public final boolean reachedThreshold;   // was: xQr5FhbwpQPWgIQ (estimated progress already >= break threshold)
        public final MinecraftClient mc = MinecraftClient.getInstance(); // was: OMMZL1F3q

        public MineContext(BlockPos pos, BlockState state, boolean isPrimary) {
            this.pos = pos.toImmutable();
            this.state = state;
            this.hardness = state.getHardness(this.mc.world, pos);
            this.isPrimary = isPrimary;
            this.startTime = System.currentTimeMillis();
            this.instaBreak = BlockUtils.canInstaBreak(pos);
            this.reachedThreshold = this.miningSpeedPerTick() / MusheorSystem.Manager.breakThreshold.get() >= 1.0;
        }

        /** Fraction of the block broken per tick, replicating vanilla mining-speed maths. */
        private float miningSpeedPerTick() { // was: FvaNWO()
            float hardness = this.state.getHardness(this.mc.world, this.pos);
            ItemStack bestTool = InventoryManager.findBestTool(this.state);
            int divisor = this.state.isToolRequired() && !bestTool.isSuitableFor(this.state) ? 100 : 30;
            float speed = this.mc.player.getBlockBreakingSpeed(this.state);
            if (bestTool != null && !bestTool.isEmpty()) {
                float multiplier = bestTool.getMiningSpeedMultiplier(this.state);
                if (multiplier > 1.0F) {
                    speed = multiplier;
                    int efficiency = Utils.getEnchantmentLevel(bestTool, Enchantments.EFFICIENCY);
                    if (efficiency > 0 && !bestTool.isEmpty()) speed += efficiency * efficiency + 1;
                }
            }

            if (StatusEffectUtil.hasHaste(this.mc.player)) {
                speed *= 1.0F + (StatusEffectUtil.getHasteAmplifier(this.mc.player) + 1) * 0.2F;
            }

            if (this.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                float f = switch (this.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                    case 0 -> 0.3F;
                    case 1 -> 0.09F;
                    case 2 -> 0.0027F;
                    default -> 8.1E-4F;
                };
                speed *= f;
            }

            if (this.mc.player.isSubmergedIn(FluidTags.WATER)) {
                speed *= (float) this.mc.player.getAttributeValue(EntityAttributes.SUBMERGED_MINING_SPEED);
            }

            if (!this.mc.player.isOnGround()) speed /= 5.0F;

            return speed / hardness / divisor;
        }

        /** Estimated break progress in [0, 1], scaled by the configured break threshold for the primary. */
        private double getProgress() { // was: Q90GLXQ0Pef()
            if (this.mc.player == null || this.mc.world == null || this.hardness < 0.0F) return 0.0;
            float perTick = this.miningSpeedPerTick();
            if (perTick <= 0.0F) return 2.147483647E9;
            float elapsedTicks = Math.max((float) (System.currentTimeMillis() - this.startTime) / 50.0F + 1.0F, 1.0F);
            float currentProgress = perTick * elapsedTicks;
            float targetProgress = this.isPrimary ? MusheorSystem.Manager.breakThreshold.get().floatValue() : 1.0F;
            return Math.min(currentProgress / targetProgress, 1.0);
        }
    }
}
