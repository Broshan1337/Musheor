// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.ChunkPos;
import net.minecraft.class_2806;
import net.minecraft.class_631;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Timer.class}, remap=false)
public abstract class TimerMixin
extends Module {
    @Shadow
    @Final
    private Setting<Double> multiplier;
    @Unique
    private Setting<Boolean> autoTimer;
    @Unique
    private Setting<Boolean> onlyWhenFlying;
    @Unique
    private Setting<Double> minThreshold;
    @Unique
    private Setting<Double> maxThreshold;
    @Unique
    private Setting<Integer> scanRadius;
    @Unique
    private Setting<Integer> unloadedThreshold;
    @Unique
    private Setting<Double> adjustmentFactor;
    @Unique
    private double currentThreshold;

    public TimerMixin() {
        super(musheor.AUTOMATION, "auto-timer", "Automatically changes the speed of everything in your client");
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo callbackInfo) {
        this.autoTimer = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-timer")).description("Automatically adjust timer speed based on chunk loading")).defaultValue((Object)false)).build());
        this.onlyWhenFlying = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-when-flying")).description("Only activates and adjusts itself when the player is flying with an elytra")).visible(() -> this.autoTimer.get())).defaultValue((Object)true)).build());
        this.minThreshold = this.settings.getDefaultGroup().add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-threshold")).description("Minimum value timer is allowed to set itself to")).defaultValue(0.4).sliderRange(0.1, 1.0).decimalPlaces(2).visible(() -> this.autoTimer.get())).build());
        this.maxThreshold = this.settings.getDefaultGroup().add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-threshold")).description("Maximum value timer is allowed to set itself to [default = 1.0]")).defaultValue(1.0).sliderRange(0.1, 1.0).decimalPlaces(2).visible(() -> this.autoTimer.get())).build());
        this.scanRadius = this.settings.getDefaultGroup().add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-radius")).description("Radius in chunks to scan")).defaultValue((Object)4)).sliderRange(1, 10).visible(() -> this.autoTimer.get())).build());
        this.unloadedThreshold = this.settings.getDefaultGroup().add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("unloaded-threshold")).description("Amount of unloaded chunks before timer is allowed to adjust itself")).defaultValue((Object)5)).sliderRange(1, 20).visible(() -> this.autoTimer.get())).build());
        this.adjustmentFactor = this.settings.getDefaultGroup().add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("adjustment-factor")).description("The factor in which timer is allowed to adjust itself")).defaultValue(0.1).sliderRange(0.1, 1.0).decimalPlaces(2).visible(() -> this.autoTimer.get())).build());
    }

    public void onActivate() {
        this.currentThreshold = (Double)this.multiplier.get();
    }

    @Unique
    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null || !((Boolean)this.autoTimer.get()).booleanValue()) {
            return;
        }
        if (!this.mc.player.isFallFlying() && ((Boolean)this.onlyWhenFlying.get()).booleanValue()) {
            return;
        }
        int n = this.countUnloadedChunks();
        double d = Math.min(1.0, (double)n / ((double)((Integer)this.unloadedThreshold.get()).intValue() * 2.0));
        double d2 = n > (Integer)this.unloadedThreshold.get() ? (Double)this.minThreshold.get() + ((Double)this.maxThreshold.get() - (Double)this.minThreshold.get()) * (1.0 - d) : (Double)this.maxThreshold.get();
        double d3 = d2 - this.currentThreshold;
        if (Math.abs(d3) > 0.01) {
            this.currentThreshold += d3 * (Double)this.adjustmentFactor.get();
            this.multiplier.set((Object)this.currentThreshold);
        }
    }

    @Unique
    private int countUnloadedChunks() {
        class_631 class_6312 = this.mc.world.method_2935();
        ChunkPos ChunkPos2 = this.mc.player.method_31476();
        int n = (Integer)this.scanRadius.get();
        int n2 = 0;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                if (class_6312.method_2857(ChunkPos2.x + i, ChunkPos2.z + j, class_2806.field_12803, false) != null || ++n2 <= (Integer)this.unloadedThreshold.get()) continue;
                return n2;
            }
        }
        return n2;
    }
}

