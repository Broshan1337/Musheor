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
import net.minecraft.class_1292;
import net.minecraft.Hand;
import net.minecraft.LivingEntity;
import net.minecraft.ItemStack;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.class_238;
import net.minecraft.BlockState;
import net.minecraft.class_2846;
import net.minecraft.class_2868;
import net.minecraft.MinecraftClient;
import net.minecraft.class_3486;
import net.minecraft.class_5134;
import net.minecraft.class_5321;

public class KekMine
extends Module {
    private static final MinecraftClient F41rraDXnaj = MinecraftClient.getInstance();
    private final SettingGroup sgRender;
    public final Setting<Boolean> autoRebreak;
    private final Setting<Boolean> silentSwap;
    private final Setting<Boolean> globalRendering;
    private final Setting<SettingColor> renderColor;
    public static KekMine YnQ4ChsDR;
    private MineContext NZkZx8MJ67Zw;
    private MineContext jIXFBaSwUWYqAc9;
    public BlockPos gsu3U1;
    public final Deque<BlockPos> OIExXGL6BNv;

    public KekMine() {
        super(musheor.MAIN, "KekMine", "Grim-safe packet miner with queue and double break.");
        this.sgRender = this.settings.createGroup("Render");
        this.autoRebreak = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-rebreak")).description("Automatically rebreak the last block in case it gets replaced")).defaultValue((Object)false)).build());
        this.silentSwap = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silent-swap")).description("Breaks the block without holding the pickaxe")).defaultValue((Object)false)).build());
        this.globalRendering = this.sgRender.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("global-rendering")).defaultValue((Object)true)).description("Synchronize rendering with Musheor-Tab")).build());
        this.renderColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).defaultValue(new SettingColor(Color.cyan)).description("Custom color for rendering (lines / wireframe)")).visible(() -> (Boolean)this.globalRendering.get() == false)).build());
        this.OIExXGL6BNv = new ArrayDeque<BlockPos>();
        YnQ4ChsDR = this;
    }

    public void usJLOV0subXO3(BlockPos BlockPos2) {
        if (KekMine.F41rraDXnaj.world == null) {
            return;
        }
        if (!BlockUtils.canBreak((BlockPos)BlockPos2, (BlockState)KekMine.F41rraDXnaj.world.getBlockState(BlockPos2))) {
            return;
        }
        if (this.Y9BgxR(BlockPos2)) {
            return;
        }
        if (this.ZbTtF5KYyGL9YXed(BlockPos2)) {
            return;
        }
        this.TAdu5cndwWu3A1(BlockPos2, KekMine.F41rraDXnaj.world.getBlockState(BlockPos2));
    }

    public boolean ZbTtF5KYyGL9YXed(BlockPos BlockPos2) {
        if (this.NZkZx8MJ67Zw != null && this.NZkZx8MJ67Zw.XMj1R1A1.equals((Object)BlockPos2)) {
            return true;
        }
        if (this.jIXFBaSwUWYqAc9 != null && this.jIXFBaSwUWYqAc9.XMj1R1A1.equals((Object)BlockPos2)) {
            return true;
        }
        return this.OIExXGL6BNv.contains(BlockPos2);
    }

    public boolean og2KVvNzA() {
        return this.NZkZx8MJ67Zw == null && this.jIXFBaSwUWYqAc9 == null && !this.OIExXGL6BNv.isEmpty();
    }

    public static void e5oi2ZF(BlockPos BlockPos2) {
        if (YnQ4ChsDR.ZbTtF5KYyGL9YXed(BlockPos2)) {
            return;
        }
        if (BlockPos2 != null) {
            MineContext mineContext = new MineContext(BlockPos2, KekMine.F41rraDXnaj.world.getBlockState(BlockPos2), true);
            KekMine.Gt56Sj4a6BWhgB(BlockPos2, KekMine.F41rraDXnaj.world.getBlockState(BlockPos2));
            YnQ4ChsDR.L5CF0C6jx0T17H4I(BlockPos2);
            YnQ4ChsDR.mp3zoXQFKUKYj5(mineContext, (Boolean)KekMine.YnQ4ChsDR.silentSwap.get());
        }
    }

    public void onDeactivate() {
        this.NZkZx8MJ67Zw = null;
        this.jIXFBaSwUWYqAc9 = null;
        this.OIExXGL6BNv.clear();
        this.gsu3U1 = null;
    }

    private static void Gt56Sj4a6BWhgB(BlockPos BlockPos2, BlockState BlockState2) {
        if (!InventoryManager.TAdu5cndwWu3A1(BlockState2).setStack() && !((Boolean)KekMine.YnQ4ChsDR.silentSwap.get()).booleanValue()) {
            InventoryManager.J2pm2c07elEb5G(BlockPos2);
        }
    }

    public void TAdu5cndwWu3A1(BlockPos BlockPos2, BlockState BlockState2) {
        if (this.ZbTtF5KYyGL9YXed(BlockPos2)) {
            return;
        }
        if (HighwayBuilder.S7TLszvzENsW7()) {
            return;
        }
        if (!(this.NZkZx8MJ67Zw == null || this.jIXFBaSwUWYqAc9 == null && ((Boolean)MusheorSystem.Manager.doubleBreak.get()).booleanValue())) {
            if (!this.OIExXGL6BNv.contains(BlockPos2)) {
                this.OIExXGL6BNv.addLast(BlockPos2);
            }
            return;
        }
        if (this.NZkZx8MJ67Zw == null) {
            KekMine.Gt56Sj4a6BWhgB(BlockPos2, BlockState2);
            this.NZkZx8MJ67Zw = new MineContext(BlockPos2, BlockState2, true);
            this.L5CF0C6jx0T17H4I(BlockPos2);
        } else if (((Boolean)MusheorSystem.Manager.doubleBreak.get()).booleanValue() && this.jIXFBaSwUWYqAc9 == null) {
            this.jOdDDFXSeWl4(class_2846.class_2847.field_12973, this.NZkZx8MJ67Zw.XMj1R1A1);
            this.jIXFBaSwUWYqAc9 = new MineContext(this.NZkZx8MJ67Zw.XMj1R1A1, this.NZkZx8MJ67Zw.rpvWtoVonf6GeT, false);
            this.NZkZx8MJ67Zw = new MineContext(BlockPos2, BlockState2, true);
            this.L5CF0C6jx0T17H4I(this.NZkZx8MJ67Zw.XMj1R1A1);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (KekMine.F41rraDXnaj.player == null || KekMine.F41rraDXnaj.world == null) {
            return;
        }
        if (HighwayBuilder.S7TLszvzENsW7()) {
            this.NZkZx8MJ67Zw = null;
            this.jIXFBaSwUWYqAc9 = null;
            return;
        }
        if (this.gsu3U1 != null && ((Boolean)this.autoRebreak.get()).booleanValue() && this.NZkZx8MJ67Zw == null && this.jIXFBaSwUWYqAc9 == null && !KekMine.F41rraDXnaj.world.getBlockState(this.gsu3U1).isAir()) {
            this.jOdDDFXSeWl4(new MineContext(this.gsu3U1, KekMine.F41rraDXnaj.world.getBlockState(this.gsu3U1), false), (Boolean)this.silentSwap.get());
            return;
        }
        this.IErgCCM();
        if (this.jIXFBaSwUWYqAc9 != null && this.jIXFBaSwUWYqAc9.xZ3kyYFbKEKAvqOe() >= 1.0) {
            this.mp3zoXQFKUKYj5(this.jIXFBaSwUWYqAc9, (Boolean)KekMine.YnQ4ChsDR.silentSwap.get());
        }
        if (this.NZkZx8MJ67Zw != null && this.NZkZx8MJ67Zw.xZ3kyYFbKEKAvqOe() >= 1.0) {
            this.mp3zoXQFKUKYj5(this.NZkZx8MJ67Zw, (Boolean)KekMine.YnQ4ChsDR.silentSwap.get());
        }
        this.oQw0r3Nc();
    }

    private void IErgCCM() {
        if (this.NZkZx8MJ67Zw != null && this.BX92A0OIIvD9(this.NZkZx8MJ67Zw.XMj1R1A1)) {
            this.NZkZx8MJ67Zw = null;
        }
        if (this.jIXFBaSwUWYqAc9 != null && this.BX92A0OIIvD9(this.jIXFBaSwUWYqAc9.XMj1R1A1)) {
            this.jIXFBaSwUWYqAc9 = null;
        }
        this.OIExXGL6BNv.removeIf(this::BX92A0OIIvD9);
    }

    private boolean BX92A0OIIvD9(BlockPos BlockPos2) {
        BlockState BlockState2 = KekMine.F41rraDXnaj.world.getBlockState(BlockPos2);
        return BlockState2.isAir() || this.Y9BgxR(BlockPos2);
    }

    private void oQw0r3Nc() {
        if (this.OIExXGL6BNv.isEmpty()) {
            return;
        }
        if (this.NZkZx8MJ67Zw == null) {
            BlockPos BlockPos2 = this.OIExXGL6BNv.pollFirst();
            BlockState BlockState2 = KekMine.F41rraDXnaj.world.getBlockState(BlockPos2);
            KekMine.Gt56Sj4a6BWhgB(BlockPos2, BlockState2);
            this.NZkZx8MJ67Zw = new MineContext(BlockPos2, BlockState2, true);
            this.L5CF0C6jx0T17H4I(this.NZkZx8MJ67Zw.XMj1R1A1);
        } else if (((Boolean)MusheorSystem.Manager.doubleBreak.get()).booleanValue() && this.jIXFBaSwUWYqAc9 == null) {
            this.jOdDDFXSeWl4(class_2846.class_2847.field_12973, this.NZkZx8MJ67Zw.XMj1R1A1);
            BlockPos BlockPos3 = this.OIExXGL6BNv.pollFirst();
            BlockState BlockState3 = KekMine.F41rraDXnaj.world.getBlockState(BlockPos3);
            this.jIXFBaSwUWYqAc9 = new MineContext(this.NZkZx8MJ67Zw.XMj1R1A1, this.NZkZx8MJ67Zw.rpvWtoVonf6GeT, false);
            this.NZkZx8MJ67Zw = new MineContext(BlockPos3, BlockState3, true);
            this.L5CF0C6jx0T17H4I(this.NZkZx8MJ67Zw.XMj1R1A1);
        }
    }

    private void L5CF0C6jx0T17H4I(BlockPos BlockPos2) {
        if (((Boolean)MusheorSystem.Manager.grimBypass.get()).booleanValue()) {
            this.jOdDDFXSeWl4(class_2846.class_2847.field_12973, BlockPos2);
        }
        this.jOdDDFXSeWl4(class_2846.class_2847.field_12968, BlockPos2);
    }

    private void jOdDDFXSeWl4(MineContext mineContext, boolean bl) {
        int n;
        boolean bl2;
        if (KekMine.F41rraDXnaj.world == null || KekMine.F41rraDXnaj.player == null) {
            return;
        }
        int n2 = KekMine.F41rraDXnaj.player.getId().method_7395(InventoryManager.TAdu5cndwWu3A1(mineContext.rpvWtoVonf6GeT));
        boolean bl3 = bl2 = n2 != (n = KekMine.F41rraDXnaj.player.getId().field_7545);
        if (bl && bl2) {
            this.vgrtgn5(n2);
        }
        this.jOdDDFXSeWl4(class_2846.class_2847.field_12973, mineContext.XMj1R1A1);
        if (bl && bl2) {
            this.vgrtgn5(n);
        }
    }

    private void mp3zoXQFKUKYj5(MineContext mineContext, boolean bl) {
        int n;
        if (KekMine.F41rraDXnaj.world == null || KekMine.F41rraDXnaj.player == null) {
            return;
        }
        if (this.NZkZx8MJ67Zw != null) {
            HighwayBuilder.jOdDDFXSeWl4(this.NZkZx8MJ67Zw.rpvWtoVonf6GeT);
        }
        if (this.jIXFBaSwUWYqAc9 != null) {
            HighwayBuilder.jOdDDFXSeWl4(this.jIXFBaSwUWYqAc9.rpvWtoVonf6GeT);
        }
        int n2 = KekMine.F41rraDXnaj.player.getId().method_7395(InventoryManager.TAdu5cndwWu3A1(mineContext.rpvWtoVonf6GeT));
        if (mineContext == this.jIXFBaSwUWYqAc9 && this.NZkZx8MJ67Zw != null && (n = KekMine.F41rraDXnaj.player.getId().method_7395(InventoryManager.TAdu5cndwWu3A1(this.NZkZx8MJ67Zw.rpvWtoVonf6GeT))) != n2) {
            n2 = n;
        }
        if (!mineContext.E74ay1CfIa1C1X6) {
            if (bl) {
                InventoryManager.jOdDDFXSeWl4(n2, () -> this.jOdDDFXSeWl4(class_2846.class_2847.field_12973, mineContext.XMj1R1A1));
            } else {
                this.jOdDDFXSeWl4(class_2846.class_2847.field_12973, mineContext.XMj1R1A1);
            }
        } else if (bl) {
            InventoryManager.jOdDDFXSeWl4(n2, null);
        }
        if ((mineContext.E74ay1CfIa1C1X6 || mineContext.yIXEDGFGtS9H) && !((Boolean)MusheorSystem.Manager.validateBreak.get()).booleanValue()) {
            KekMine.F41rraDXnaj.world.method_20290(2001, mineContext.XMj1R1A1, Block.method_9507((BlockState)mineContext.rpvWtoVonf6GeT));
            KekMine.F41rraDXnaj.world.method_8652(mineContext.XMj1R1A1, Blocks.LAVA.method_9564(), 3);
        }
        this.gsu3U1 = mineContext.XMj1R1A1;
        mineContext.HBAiI3pyGGGxpI2b = false;
        if (mineContext == this.NZkZx8MJ67Zw) {
            this.NZkZx8MJ67Zw = null;
        } else if (mineContext == this.jIXFBaSwUWYqAc9) {
            this.jIXFBaSwUWYqAc9 = null;
        }
    }

    public void jOdDDFXSeWl4(class_2846.class_2847 class_28472, BlockPos BlockPos2) {
        if (KekMine.F41rraDXnaj.field_1761 == null || KekMine.F41rraDXnaj.world == null) {
            return;
        }
        KekMine.F41rraDXnaj.field_1761.method_41931(KekMine.F41rraDXnaj.world, n -> new class_2846(class_28472, BlockPos2, Direction.field_11036, n));
    }

    public void vgrtgn5(int n) {
        if (KekMine.F41rraDXnaj.field_1761 == null || KekMine.F41rraDXnaj.world == null || n < 0) {
            return;
        }
        KekMine.F41rraDXnaj.field_1761.method_41931(KekMine.F41rraDXnaj.world, n2 -> new class_2868(n));
    }

    public boolean Y9BgxR(BlockPos BlockPos2) {
        return !(KekMine.F41rraDXnaj.player.method_33571().method_1022(BlockPos2.method_46558()) <= (Double)((KekNuker)Modules.get().get(KekNuker.class)).range.get() + 0.5);
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (KekMine.F41rraDXnaj.player == null || KekMine.F41rraDXnaj.world == null) {
            return;
        }
        if (!((KekNuker)Modules.get().get(KekNuker.class)).isActive()) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                RenderUtils.mp3zoXQFKUKYj5(render3DEvent, this.OIExXGL6BNv.stream().toList());
            } else {
                RenderUtils.jOdDDFXSeWl4(render3DEvent, this.OIExXGL6BNv.stream().toList(), meteordevelopment.meteorclient.utils.render.color.Color.WHITE, meteordevelopment.meteorclient.utils.render.color.Color.WHITE, ShapeMode.Lines);
            }
        }
        if (this.jIXFBaSwUWYqAc9 != null) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                this.jOdDDFXSeWl4(render3DEvent, this.jIXFBaSwUWYqAc9, (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderSideColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderLineColor.get(), (ShapeMode)MusheorSystem.Manager.renderShape.get());
            } else {
                this.jOdDDFXSeWl4(render3DEvent, this.jIXFBaSwUWYqAc9, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
            }
        }
        if (this.NZkZx8MJ67Zw != null) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                this.jOdDDFXSeWl4(render3DEvent, this.NZkZx8MJ67Zw, (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderSideColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)MusheorSystem.Manager.renderLineColor.get(), (ShapeMode)MusheorSystem.Manager.renderShape.get());
            } else {
                this.jOdDDFXSeWl4(render3DEvent, this.NZkZx8MJ67Zw, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
            }
        }
        if (this.gsu3U1 != null && ((Boolean)this.autoRebreak.get()).booleanValue() && !KekMine.F41rraDXnaj.world.getBlockState(this.gsu3U1).isAir()) {
            if (((Boolean)this.globalRendering.get()).booleanValue()) {
                RenderUtils.jOdDDFXSeWl4(render3DEvent, this.gsu3U1, KekMine.F41rraDXnaj.world.getBlockState(this.gsu3U1).getBlock());
            } else {
                RenderUtils.jOdDDFXSeWl4(render3DEvent, this.gsu3U1, (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), (meteordevelopment.meteorclient.utils.render.color.Color)this.renderColor.get(), ShapeMode.Lines);
            }
        }
    }

    private void jOdDDFXSeWl4(Render3DEvent render3DEvent, MineContext mineContext, meteordevelopment.meteorclient.utils.render.color.Color color, meteordevelopment.meteorclient.utils.render.color.Color color2, ShapeMode shapeMode) {
        double d = (1.0 - mineContext.xZ3kyYFbKEKAvqOe()) / 2.0;
        class_238 class_2383 = new class_238((double)mineContext.XMj1R1A1.getX() + d, (double)mineContext.XMj1R1A1.getY() + d, (double)mineContext.XMj1R1A1.getZ() + d, (double)mineContext.XMj1R1A1.getX() + 1.0 - d, (double)mineContext.XMj1R1A1.getY() + 1.0 - d, (double)mineContext.XMj1R1A1.getZ() + 1.0 - d);
        render3DEvent.renderer.box(class_2383, color, color2, shapeMode, 0);
    }

    public static class MineContext {
        public final BlockPos XMj1R1A1;
        public final BlockState rpvWtoVonf6GeT;
        public long Y775oeIufYz9;
        public final float lzRYRnZcMXfWy6t;
        public boolean HBAiI3pyGGGxpI2b = true;
        public final boolean rUchPoPt;
        public final boolean E74ay1CfIa1C1X6;
        public final boolean yIXEDGFGtS9H;
        public final MinecraftClient fjsJhTJB1Q6qDp4F = MinecraftClient.getInstance();

        public MineContext(BlockPos BlockPos2, BlockState BlockState2, boolean bl) {
            this.XMj1R1A1 = BlockPos2.mutableCopy();
            this.rpvWtoVonf6GeT = BlockState2;
            this.lzRYRnZcMXfWy6t = BlockState2.method_26214((class_1922)this.fjsJhTJB1Q6qDp4F.world, BlockPos2);
            this.rUchPoPt = bl;
            this.Y775oeIufYz9 = System.currentTimeMillis();
            this.E74ay1CfIa1C1X6 = BlockUtils.canInstaBreak((BlockPos)BlockPos2);
            this.yIXEDGFGtS9H = (double)this.w6yjUYq() / (Double)MusheorSystem.Manager.breakThreshold.get() >= 1.0;
        }

        private float w6yjUYq() {
            float f;
            float f2 = this.rpvWtoVonf6GeT.method_26214((class_1922)this.fjsJhTJB1Q6qDp4F.world, this.XMj1R1A1);
            ItemStack ItemStack2 = InventoryManager.TAdu5cndwWu3A1(this.rpvWtoVonf6GeT);
            int n = !this.rpvWtoVonf6GeT.method_29291() || ItemStack2.method_7951(this.rpvWtoVonf6GeT) ? 30 : 100;
            float f3 = this.fjsJhTJB1Q6qDp4F.player.method_7351(this.rpvWtoVonf6GeT);
            if (ItemStack2 != null && !ItemStack2.setStack() && (f = ItemStack2.method_7924(this.rpvWtoVonf6GeT)) > 1.0f) {
                f3 = f;
                int n2 = Utils.getEnchantmentLevel((ItemStack)ItemStack2, (class_5321)class_1893.field_9131);
                if (n2 > 0 && !ItemStack2.setStack()) {
                    f3 += (float)(n2 * n2 + 1);
                }
            }
            if (class_1292.method_5576((LivingEntity)this.fjsJhTJB1Q6qDp4F.player)) {
                f3 *= 1.0f + (float)(class_1292.method_5575((LivingEntity)this.fjsJhTJB1Q6qDp4F.player) + 1) * 0.2f;
            }
            if (this.fjsJhTJB1Q6qDp4F.player.method_6059(Hand.field_5901)) {
                f = switch (this.fjsJhTJB1Q6qDp4F.player.method_6112(Hand.field_5901).method_5578()) {
                    case 0 -> 0.3f;
                    case 1 -> 0.09f;
                    case 2 -> 0.0027f;
                    default -> 8.1E-4f;
                };
                f3 *= f;
            }
            if (this.fjsJhTJB1Q6qDp4F.player.method_5777(class_3486.field_15517)) {
                f3 *= (float)this.fjsJhTJB1Q6qDp4F.player.method_45325(class_5134.field_51576);
            }
            if (!this.fjsJhTJB1Q6qDp4F.player.method_24828()) {
                f3 /= 5.0f;
            }
            return f3 / f2 / (float)n;
        }

        double xZ3kyYFbKEKAvqOe() {
            if (this.fjsJhTJB1Q6qDp4F.player == null || this.fjsJhTJB1Q6qDp4F.world == null || this.lzRYRnZcMXfWy6t < 0.0f) {
                return 0.0;
            }
            float f = this.w6yjUYq();
            if (f <= 0.0f) {
                return 2.147483647E9;
            }
            float f2 = Math.max((float)(System.currentTimeMillis() - this.Y775oeIufYz9) / 50.0f + 1.0f, 1.0f);
            float f3 = f * f2;
            float f4 = this.rUchPoPt ? ((Double)MusheorSystem.Manager.breakThreshold.get()).floatValue() : 1.0f;
            return Math.min((double)(f3 / f4), 1.0);
        }
    }
}

