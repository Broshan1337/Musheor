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
import net.minecraft.InteractionHand;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.class_1826;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.class_239;
import net.minecraft.Vec3d;
import net.minecraft.MinecraftClient;
import net.minecraft.Screen;

public class AirPlace
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient lp0pdRphdk = MinecraftClient.getInstance();
    private final Setting<Boolean> render;
    private final Setting<Boolean> customRange;
    private final Setting<Double> range;
    private BlockPos MWtXKjmtUPMW8cZK;
    private boolean aZElPUDuPV3EqLnc;
    private int WvQP0Zr;

    public AirPlace() {
        super(musheor.MAIN, "kek-place", "Bypasses grim to place blocks mid-air");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.render = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where the block will be placed.")).defaultValue((Object)true)).build());
        this.customRange = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-range")).description("Use custom range for air place.")).defaultValue((Object)false)).build());
        this.range = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Custom range to place at.")).visible(() -> this.customRange.get())).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.MWtXKjmtUPMW8cZK = null;
        this.aZElPUDuPV3EqLnc = false;
        this.WvQP0Zr = 0;
    }

    public void onActivate() {
        this.aZElPUDuPV3EqLnc = false;
        this.WvQP0Zr = 0;
        this.MWtXKjmtUPMW8cZK = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (AirPlace.lp0pdRphdk.player == null || AirPlace.lp0pdRphdk.world == null) {
            return;
        }
        this.MWtXKjmtUPMW8cZK = this.RP30itVPat();
        boolean bl = AirPlace.lp0pdRphdk.options.gameRenderer.method_1434();
        boolean bl2 = bl && !this.aZElPUDuPV3EqLnc;
        this.aZElPUDuPV3EqLnc = bl;
        if (!bl) {
            this.WvQP0Zr = 0;
            return;
        }
        if (this.MWtXKjmtUPMW8cZK == null) {
            return;
        }
        if (!(AirPlace.lp0pdRphdk.player.method_6047().getStack() instanceof AbstractClientPlayerEntity) && !(AirPlace.lp0pdRphdk.player.method_6047().getStack() instanceof class_1826)) {
            return;
        }
        if (!BlockUtils.canPlace((BlockPos)this.MWtXKjmtUPMW8cZK, (boolean)true)) {
            return;
        }
        if (bl2) {
            this.WvQP0Zr = 0;
            this.KP44bk(this.MWtXKjmtUPMW8cZK);
        } else {
            ++this.WvQP0Zr;
            if (this.WvQP0Zr >= 4) {
                this.WvQP0Zr = 0;
                this.KP44bk(this.MWtXKjmtUPMW8cZK);
            }
        }
    }

    private BlockPos RP30itVPat() {
        double d;
        if (AirPlace.lp0pdRphdk.player == null || AirPlace.lp0pdRphdk.world == null) {
            return null;
        }
        double d2 = d = (Boolean)this.customRange.get() != false ? ((Double)this.range.get()).doubleValue() : AirPlace.lp0pdRphdk.player.method_55754();
        if (lp0pdRphdk.method_1560() == null) {
            return null;
        }
        class_239 class_2392 = lp0pdRphdk.method_1560().method_5745(d, 0.0f, false);
        if (!(class_2392 instanceof Screen)) {
            return null;
        }
        Screen Screen2 = (Screen)class_2392;
        BlockPos BlockPos2 = Screen2.method_17777();
        if (AirPlace.lp0pdRphdk.world.getBlockState(BlockPos2).method_45474()) {
            return BlockPos2;
        }
        BlockPos BlockPos3 = BlockPos2.offset(Screen2.method_17780());
        return AirPlace.lp0pdRphdk.world.getBlockState(BlockPos3).method_45474() ? BlockPos3 : null;
    }

    private void KP44bk(BlockPos BlockPos2) {
        Screen Screen2 = new Screen(Vec3d.method_24953((BlockPos)BlockPos2), Direction.field_11033, BlockPos2, false);
        WorldUtils.l3ot1CwoJ9CsS();
        WorldUtils.jOdDDFXSeWl4(InteractionHand.field_5810, Screen2);
        WorldUtils.l3ot1CwoJ9CsS();
        ItemStack ItemStack2 = AirPlace.lp0pdRphdk.player.method_6047().getStack();
        if (ItemStack2 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity AbstractClientPlayerEntity2 = (AbstractClientPlayerEntity)ItemStack2;
            AirPlace.lp0pdRphdk.world.method_8652(BlockPos2, AbstractClientPlayerEntity2.method_7711().method_9564(), 3);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (AirPlace.lp0pdRphdk.player == null || AirPlace.lp0pdRphdk.world == null || !((Boolean)this.render.get()).booleanValue() || this.MWtXKjmtUPMW8cZK == null) {
            return;
        }
        if (!(AirPlace.lp0pdRphdk.player.method_6047().getStack() instanceof AbstractClientPlayerEntity) && !(AirPlace.lp0pdRphdk.player.method_6047().getStack() instanceof class_1826)) {
            return;
        }
        if (!AirPlace.lp0pdRphdk.world.getBlockState(this.MWtXKjmtUPMW8cZK).method_45474()) {
            return;
        }
        RenderUtils.jOdDDFXSeWl4(render3DEvent, this.MWtXKjmtUPMW8cZK, Block.method_9503((ItemStack)AirPlace.lp0pdRphdk.player.method_6047().getStack()));
    }
}

