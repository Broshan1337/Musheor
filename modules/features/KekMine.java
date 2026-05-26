// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import java.awt.Color;
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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.KekNuker;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class KekMine
extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final SettingGroup sgRender;
    public final Setting<Boolean> autoRebreak;
    private final Setting<Boolean> silentSwap;
    private final Setting<Boolean> globalRendering;
    private final Setting<SettingColor> renderColor;
    public static KekMine INSTANCE;
    private MineContext primaryMine;
    private MineContext secondaryMine;
    public BlockPos lastBreakPos;
    public final Deque<BlockPos> miningQueue;

    public KekMine() {
        super(musheor.MAIN, "KekMine", "Grim-safe packet miner with queue and double break.");
        this.sgRender = this.settings.createGroup("Render");
        this.autoRebreak = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-rebreak")).description("Automatically rebreak the last block in case it gets replaced")).defaultValue((Object)false)).build());
        this.silentSwap = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silent-swap")).description("Breaks the block without holding the pickaxe")).defaultValue((Object)false)).build());
        this.globalRendering = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("global-rendering")).defaultValue((Object)true)).description("Synchronize rendering with Musheor-Tab")).build());
        this.renderColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).defaultValue(new SettingColor(Color.cyan)).description("Custom color for rendering (lines / wireframe)")).visible(() -> (Boolean)this.globalRendering.get() == false)).build());
        this.miningQueue = new ArrayDeque<BlockPos>();
        INSTANCE = this;
    }

    public void tryMineBlock(BlockPos pos) {
        if (KekMine.mc.world == null) {
            return;
        }
        if (!BlockUtils.canBreak((BlockPos)pos, (BlockState)KekMine.mc.world.getBlockState(pos))) {
            return;
        }
        if (this.isOutOfRange(pos)) {
            return;
        }
        if (this.isAlreadyMining(pos)) {
            return;
        }
        this.queueBlock(pos, KekMine.mc.world.getBlockState(pos));
    }

    public boolean isAlreadyMining(BlockPos pos) {
        if (this.primaryMine != null && this.primaryMine.pos.equals((Object)pos)) {
            return true;
        }
        if (this.secondaryMine != null && this.secondaryMine.pos.equals((Object)pos)) {
            return true;
        }
        return this.miningQueue.contains(pos);
    }

    public boolean isQueueActive() {
        return this.primaryMine == null && this.secondaryMine == null && !this.miningQueue.isEmpty();
    }

    public static void startMining(BlockPos pos) {
        if (INSTANCE.isAlreadyMining(pos)) {
            return;
        }
        if (pos != null) {
            MineContext mineContext = new MineContext(pos, KekMine.mc.world.getBlockState(pos), true);
            KekMine.swapToTool(pos, KekMine.mc.world.getBlockState(pos));
            INSTANCE.sendBreakPacket(pos);
            INSTANCE.completeMining(mineContext, (Boolean)KekMine.INSTANCE.silentSwap.get());
        }
    }

    public void onDeactivate() {
        this.primaryMine = null;
        this.secondaryMine = null;
        this.miningQueue.clear();
        this.lastBreakPos = null;
    }

    private static void swapToTool(BlockPos pos, BlockState state) {
        if (!InventoryManager.getBestToolForBlock(state).isEmpty() && !((Boolean)KekMine.INSTANCE.silentSwap.get()).booleanValue()) {
            InventoryManager.equipBestToolForBlock(pos);
        }
    }

    public void queueBlock(BlockPos pos, BlockState state) {
        if (this.isAlreadyMining(pos)) {
            return;
        }
        if (HighwayBuilder.isEating()) {
            return;
        }
        if (!(this.primaryMine == null || this.secondaryMine == null && ((Boolean)MusheorSystem.Manager.doubleBreak.get()).booleanValue())) {
            if (!this.miningQueue.contains(pos)) {
                this.miningQueue.addLast(pos);
            }
            return;
        }
        if (this.primaryMine == null) {
            KekMine.swapToTool(pos, state);
            this.primaryMine = new MineContext(pos, state, true);
            this.sendBreakPacket(pos);
        } else if (((Boolean)MusheorSystem.Manager.doubleBreak.get()).booleanValue() && this.secondaryMine == null) {
            this.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.primaryMine.pos);
            this.secondaryMine = new MineContext(this.primaryMine.pos, this.primaryMine.state, false);
            this.primaryMine = new MineContext(pos, state, true);
            this.sendBreakPacket(this.primaryMine.pos);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (KekMine.mc.player == null || KekMine.mc.world == null) {
            return;
        }
        if (HighwayBuilder.isEating()) {
            this.primaryMine = null;
            this.secondaryMine = null;
            return;
        }
        if (this.lastBreakPos != null && ((Boolean)this.autoRebreak.get()).booleanValue() && this.primaryMine == null && this.secondaryMine == null && !KekMine.mc.world.getBlockState(this.lastBreakPos).isAir()) {
            this.sendBreakWithSwap(new MineContext(this.lastBreakPos, KekMine.mc.world.getBlockState(this.lastBreakPos), false), (Boolean)this.silentSwap.get());
            return;
        }
        this.cleanupStaleEntries();
        if (this.secondaryMine != null && this.secondaryMine.getBreakProgress() >= 1.0) {
            this.completeMining(this.secondaryMine, (Boolean)KekMine.INSTANCE.silentSwap.get());
        }
        if (this.primaryMine != null && this.primaryMine.getBreakProgress() >= 1.0) {
            this.completeMining(this.primaryMine, (Boolean)KekMine.INSTANCE.silentSwap.get());
        }
        this.processQueue();
    }

    private void cleanupStaleEntries() {
        if (this.primaryMine != null && this.shouldCancelMining(this.primaryMine.pos)) {
            this.primaryMine = null;
        }
        if (this.secondaryMine != null && this.shouldCancelMining(this.secondaryMine.pos)) {
            this.secondaryMine = null;
        }
        this.miningQueue.removeIf(this::shouldCancelMining);
    }

    private boolean shouldCancelMining(BlockPos pos) {
        BlockState blockState = KekMine.mc.world.getBlockState(pos);
        return blockState.isAir() || this.isOutOfRange(pos);
    }

    private void processQueue() {
        if (this.miningQueue.isEmpty()) {
            return;
        }
        if (this.primaryMine == null) {
            BlockPos pos = this.miningQueue.pollFirst();
            BlockState state = KekMine.mc.world.getBlockState(pos);
            KekMine.swapToTool(pos, state);
            this.primaryMine = new MineContext(pos, state, true);
            this.sendBreakPacket(this.primaryMine.pos);
        } else if (((Boolean)MusheorSystem.Manager.doubleBreak.get()).booleanValue() && this.secondaryMine == null) {
            this.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.primaryMine.pos);
            BlockPos pos2 = this.miningQueue.pollFirst();
            BlockState state2 = KekMine.mc.world.getBlockState(pos2);
            this.secondaryMine = new MineContext(this.primaryMine.pos, this.primaryMine.state, false);
            this.primaryMine = new MineContext(pos2, state2, true);
            this.sendBreakPacket(this.primaryMine.pos);
        }
    }

    private void sendBreakPacket(BlockPos pos) {
        if (((Boolean)MusheorSystem.Manager.grimBypass.get()).booleanValue()) {
            this.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos);
        }
        this.sendBlockAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos);
    }

    private void sendBreakWithSwap(MineContext mineContext, boolean bl) {
        int n;
        boolean bl2;
        if (KekMine.mc.world == null || KekMine.mc.player == null) {
            return;
        }
        int n2 = KekMine.mc.player.getInventory().getSlotWithStack(InventoryManager.getBestToolForBlock(mineContext.state));
        boolean bl3 = bl2 = n2 != (n = KekMine.mc.player.getInventory().selectedSlot);
        if (bl && bl2) {
            this.sendSlotPacket(n2);
        }
        this.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mineContext.pos);
        if (bl && bl2) {
            this.sendSlotPacket(n);
        }
    }

    private void completeMining(MineContext mineContext, boolean bl) {
        int n;
        if (KekMine.mc.world == null || KekMine.mc.player == null) {
            return;
        }
        if (this.primaryMine != null) {
            HighwayBuilder.onBlockMined(this.primaryMine.state);
        }
        if (this.secondaryMine != null) {
            HighwayBuilder.onBlockMined(this.secondaryMine.state);
        }
        int n2 = KekMine.mc.player.getInventory().getSlotWithStack(InventoryManager.getBestToolForBlock(mineContext.state));
        if (mineContext == this.secondaryMine && this.primaryMine != null && (n = KekMine.mc.player.getInventory().getSlotWithStack(InventoryManager.getBestToolForBlock(this.primaryMine.state))) != n2) {
            n2 = n;
        }
        if (!mineContext.canInstaBreak) {
            if (bl) {
                InventoryManager.withHotbarSlot(n2, () -> this.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mineContext.pos));
            } else {
                this.sendBlockAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mineContext.pos);
            }
        } else if (bl) {
            InventoryManager.withHotbarSlot(n2, null);
        }
        if ((mineContext.canInstaBreak || mineContext.isPrimary) && !((Boolean)MusheorSystem.Manager.validateBreak.get()).booleanValue()) {
            KekMine.mc.world.syncWorldEvent(2001, mineContext.pos, Block.getRawIdFromState((BlockState)mineContext.state));
            KekMine.mc.world.setBlockState(mineContext.pos, Blocks.AIR.getDefaultState(), 3);
        }
        this.lastBreakPos = mineContext.pos;
        mineContext.active = false;
        if (mineContext == this.primaryMine) {
            this.primaryMine = null;
        } else if (mineContext == this.secondaryMine) {
            this.secondaryMine = null;
        }
    }

    public void sendBlockAction(PlayerActionC2SPacket.Action actionType, BlockPos pos) {
        if (KekMine.mc.interactionManager == null || KekMine.mc.world == null) {
            return;
        }
        KekMine.mc.interactionManager.sendSequencedPacket(KekMine.mc.world, n -> new PlayerActionC2SPacket(actionType, pos, Direction.UP, n));
    }

    public void sendSlotPacket(int n) {
        if (KekMine.mc.interactionManager == null || KekMine.mc.world == null || n < 0) {
            return;
        }
        KekMine.mc.interactionManager.sendSequencedPacket(KekMine.mc.world, n2 -> new UpdateSelectedSlotC2SPacket(n));
    }

    public boolean isOutOfRange(BlockPos pos) {
        return !(KekMine.mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= (Double)((KekNuker)Modules.get().get(KekNuker.class)).range.get() + 0.5);
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (KekMine.mc.player == null || KekMine.mc.world == null) {
            return;
        }
        if (!((KekNuker)Modules.get().get(KekNuker.class)).isActive()) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                RenderUtils.mp3zoXQFKUKYj5(render3DEvent, this.miningQueue.stream().toList());
            } else {
                RenderUtils.jOdDDFXSeWl4(render3DEvent, this.miningQueue.stream().toList(), meteordevelopment.meteorclient.utils.render.color.Color.WHITE, meteordevelopment.meteorclient.utils.render.color.Color.WHITE, ShapeMode.Lines);
            }
        }
        if (this.secondaryMine != null) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                this.renderMineContext(render3DEvent, this.secondaryMine, (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderSideColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderLineColor.get(), (ShapeMode)MusheorSystem.Manager.renderShape.get());
            } else {
                this.renderMineContext(render3DEvent, this.secondaryMine, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
            }
        }
        if (this.primaryMine != null) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                this.renderMineContext(render3DEvent, this.primaryMine, (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderSideColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderLineColor.get(), (ShapeMode)MusheorSystem.Manager.renderShape.get());
            } else {
                this.renderMineContext(render3DEvent, this.primaryMine, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
            }
        }
        if (this.lastBreakPos != null && ((Boolean)this.autoRebreak.get()).booleanValue() && !KekMine.mc.world.getBlockState(this.lastBreakPos).isAir()) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                RenderUtils.jOdDDFXSeWl4(render3DEvent, this.lastBreakPos, KekMine.mc.world.getBlockState(this.lastBreakPos).getBlock());
            } else {
                RenderUtils.jOdDDFXSeWl4(render3DEvent, this.lastBreakPos, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
            }
        }
    }

    private void renderMineContext(Render3DEvent render3DEvent, MineContext mineContext, meteordevelopment.meteorclient.utils.render.color.Color color, meteordevelopment.meteorclient.utils.render.color.Color color2, ShapeMode shapeMode) {
        double d = (1.0 - mineContext.getBreakProgress()) / 2.0;
        Box box = new Box((double)mineContext.pos.getX() + d, (double)mineContext.pos.getY() + d, (double)mineContext.pos.getZ() + d, (double)mineContext.pos.getX() + 1.0 - d, (double)mineContext.pos.getY() + 1.0 - d, (double)mineContext.pos.getZ() + 1.0 - d);
        render3DEvent.renderer.box(box, color, color2, shapeMode, 0);
    }

    public static class MineContext {
        public final BlockPos pos;
        public final BlockState state;
        public long breakStartTime;
        public final float hardness;
        public boolean active = true;
        public final boolean isAttack;
        public final boolean canInstaBreak;
        public final boolean isPrimary;
        public final MinecraftClient mc = MinecraftClient.getInstance();

        public MineContext(BlockPos pos, BlockState state, boolean bl) {
            this.pos = pos.toImmutable();
            this.state = state;
            this.hardness = state.getHardness((BlockView)this.mc.world, pos);
            this.isAttack = bl;
            this.breakStartTime = System.currentTimeMillis();
            this.canInstaBreak = BlockUtils.canInstaBreak((BlockPos)pos);
            this.isPrimary = (double)this.getBreakSpeed() / (Double)MusheorSystem.Manager.breakThreshold.get() >= 1.0;
        }

        private float getBreakSpeed() {
            float f;
            float f2 = this.state.getHardness((BlockView)this.mc.world, this.pos);
            ItemStack ItemStack2 = InventoryManager.getBestToolForBlock(this.state);
            int n = !this.state.isToolRequired() || ItemStack2.isSuitableFor(this.state) ? 30 : 100;
            float f3 = this.mc.player.getBlockBreakingSpeed(this.state);
            if (ItemStack2 != null && !ItemStack2.isEmpty() && (f = ItemStack2.getMiningSpeedMultiplier(this.state)) > 1.0f) {
                f3 = f;
                int n2 = Utils.getEnchantmentLevel((ItemStack)ItemStack2, (RegistryKey)Enchantments.EFFICIENCY);
                if (n2 > 0 && !ItemStack2.isEmpty()) {
                    f3 += (float)(n2 * n2 + 1);
                }
            }
            if (StatusEffectUtil.hasHaste((LivingEntity)this.mc.player)) {
                f3 *= 1.0f + (float)(StatusEffectUtil.getHasteAmplifier((LivingEntity)this.mc.player) + 1) * 0.2f;
            }
            if (this.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                f = switch (this.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                    case 0 -> 0.3f;
                    case 1 -> 0.09f;
                    case 2 -> 0.0027f;
                    default -> 8.1E-4f;
                };
                f3 *= f;
            }
            if (this.mc.player.isSubmergedIn(FluidTags.WATER)) {
                f3 *= (float)this.mc.player.getAttributeValue(EntityAttributes.SUBMERGED_MINING_SPEED);
            }
            if (!this.mc.player.isOnGround()) {
                f3 /= 5.0f;
            }
            return f3 / f2 / (float)n;
        }

        double getBreakProgress() {
            if (this.mc.player == null || this.mc.world == null || this.hardness < 0.0f) {
                return 0.0;
            }
            float f = this.getBreakSpeed();
            if (f <= 0.0f) {
                return 2.147483647E9;
            }
            float f2 = Math.max((float)(System.currentTimeMillis() - this.breakStartTime) / 50.0f + 1.0f, 1.0f);
            float f3 = f * f2;
            float f4 = this.isAttack ? ((Double)MusheorSystem.Manager.breakThreshold.get()).floatValue() : 1.0f;
            return Math.min((double)(f3 / f4), 1.0);
        }
    }
}