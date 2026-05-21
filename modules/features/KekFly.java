// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.system.MusheorSystem;
import net.minecraft.Formatting;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.DamageSource;
import net.minecraft.class_1671;
import net.minecraft.Items;
import net.minecraft.Vec3d;
import net.minecraft.Packet;
import net.minecraft.EntityTrackerEntry;
import net.minecraft.class_2886;

public class KekFly
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<FlyMode> flyMode;
    private final Setting<Boolean> autoRocket;
    private final Setting<Double> rocketDelay;
    private final Setting<Boolean> hoverMidAir;
    private final Setting<Boolean> autoSwap;
    private final Setting<Boolean> autoSwapBack;
    private final Setting<Integer> launchDelay;
    public final Setting<Integer> rocketTimeout;
    public final Setting<Boolean> lockY;
    public final Setting<Integer> targetY;
    public static KekFly fly;
    private int QTmNF6NCXs;
    private static int lYl0U01zxBqO9u;
    private float U6GoOdyLiE04;
    private float Icks58Pk4vQH3;
    private boolean KaWPzeyl1xVKHWo;
    private boolean A02ApsqZGj;
    private Vec3d JDwgf5;
    private long yyKeW1d7hG;

    public KekFly() {
        super(musheor.MAIN, "kek-fly", "ZOOM thru the air, or even on ground...");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.flyMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("fly-mode")).defaultValue((Object)FlyMode.yjhDfCpm)).build());
        this.autoRocket = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-rocket")).description("Fires a rocket automatically whenever the previous one despawns.")).defaultValue((Object)true)).build());
        this.rocketDelay = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rocket-delay")).description("Seconds between rockets when auto-rocket is off.")).defaultValue(3.5).sliderRange(0.5, 15.0).decimalPlaces(1).visible(() -> (Boolean)this.autoRocket.get() == false)).build());
        this.hoverMidAir = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hover")).defaultValue((Object)true)).visible(() -> this.flyMode.get() == FlyMode.XWpV9Q7)).build());
        this.autoSwap = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-swap")).description("Automatically swaps to your elytra when activating")).defaultValue((Object)true)).build());
        this.autoSwapBack = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Automatically swaps back to your chestplate when deactivating")).defaultValue((Object)true)).visible(() -> this.autoSwap.get())).build());
        this.launchDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("launch-delay")).description("Delay in ticks before launching the player from the ground")).defaultValue((Object)0)).sliderRange(0, 5).build());
        this.rocketTimeout = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rocket-timeout")).description("Timeout in ticks before assuming a rocket has been fired, increase this if you are on higher ping")).defaultValue((Object)3)).sliderRange(0, 10).build());
        this.lockY = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lock-y")).description("Steer the player's pitch each tick to maintain a target Y level while gliding.")).defaultValue((Object)false)).visible(() -> this.flyMode.get() == FlyMode.yjhDfCpm)).build());
        this.targetY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-level")).description("The Y level to maintain when lock-y is enabled.")).defaultValue((Object)350)).sliderRange(64, 512).visible(() -> this.flyMode.get() == FlyMode.yjhDfCpm && (Boolean)this.lockY.get() != false)).build());
        fly = this;
    }

    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.QTmNF6NCXs = 0;
        this.KaWPzeyl1xVKHWo = false;
        if (((Boolean)this.autoSwap.get()).booleanValue() && !this.mc.player.method_6118(DamageSource.field_6174).getStack().equals(Items.field_8833)) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        }
        if (this.mc.player.method_24828()) {
            this.mc.player.method_6043();
        }
        lYl0U01zxBqO9u = -1;
        this.A02ApsqZGj = false;
        this.JDwgf5 = null;
        this.yyKeW1d7hG = 0L;
    }

    public void onDeactivate() {
        if (this.mc.player != null) {
            this.mc.player.method_5875(false);
        }
        this.JDwgf5 = null;
        if (((Boolean)this.autoSwap.get()).booleanValue() && ((Boolean)this.autoSwapBack.get()).booleanValue()) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        } else {
            this.icVv2m6();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null) {
            return;
        }
        this.KaWPzeyl1xVKHWo = false;
        if (lYl0U01zxBqO9u++ < (Integer)this.launchDelay.get()) {
            return;
        }
        this.icVv2m6();
        if (this.QTmNF6NCXs > 0) {
            --this.QTmNF6NCXs;
            if (this.QTmNF6NCXs == 0) {
                this.jM7Ku65I6T();
            }
            return;
        }
        if (this.flyMode.get() == FlyMode.yjhDfCpm) {
            this.qKoJQYof();
        } else {
            boolean bl = this.mc.options.field_1903.method_1434();
            boolean bl2 = this.mc.options.field_1832.method_1434();
            boolean bl3 = this.mc.options.field_1894.method_1434();
            boolean bl4 = this.mc.options.field_1913.method_1434();
            boolean bl5 = this.mc.options.field_1881.method_1434();
            boolean bl6 = this.mc.options.field_1849.method_1434();
            boolean bl7 = bl || bl2 || bl3 || bl4 || bl5 || bl6;
            this.U6GoOdyLiE04 = this.mc.player.method_36454();
            this.Icks58Pk4vQH3 = this.mc.player.method_36455();
            float f = this.U6GoOdyLiE04;
            float f2 = -1.0f;
            if (!bl7) {
                if (((Boolean)this.hoverMidAir.get()).booleanValue()) {
                    if (this.df1mByH0u()) {
                        if (this.A02ApsqZGj) {
                            f += 180.0f;
                        }
                        this.A02ApsqZGj = !this.A02ApsqZGj;
                    } else {
                        this.sE4h5W();
                    }
                }
            } else {
                if (this.JDwgf5 != null) {
                    this.mc.player.method_5875(false);
                    this.mc.player.method_22862();
                    this.JDwgf5 = null;
                }
                if (bl3 && bl4) {
                    f -= 45.0f;
                } else if (bl3 && bl6) {
                    f += 45.0f;
                } else if (bl5 && bl4) {
                    f -= 135.0f;
                } else if (bl5 && bl6) {
                    f += 135.0f;
                } else if (bl4) {
                    f -= 90.0f;
                } else if (bl6) {
                    f += 90.0f;
                } else if (bl5) {
                    f += 180.0f;
                }
                if (bl && (bl3 || bl4 || bl5 || bl6)) {
                    f2 = -45.0f;
                } else if (bl) {
                    f2 = -90.0f;
                } else if (bl2 && (bl3 || bl4 || bl5 || bl6)) {
                    f2 = 45.0f;
                } else if (bl2) {
                    f2 = 90.0f;
                }
                this.jM7Ku65I6T();
            }
            this.mc.player.method_36456(f);
            this.mc.player.method_36457(f2);
            this.KaWPzeyl1xVKHWo = true;
        }
        if (this.flyMode.get() == FlyMode.yjhDfCpm) {
            if (((Boolean)this.lockY.get()).booleanValue()) {
                this.xkVv33gtDkfg4();
            }
            this.jM7Ku65I6T();
        }
    }

    private void xkVv33gtDkfg4() {
        if (this.mc.player.method_24828()) {
            return;
        }
        if (PlayerUtils.getDimension() == Dimension.Nether) {
            return;
        }
        double d = (double)((Integer)this.targetY.get()).intValue() - this.mc.player.getY();
        float f = (float)Math.toDegrees(Math.atan2(-d, 16.0));
        f = Math.max(-45.0f, Math.min(45.0f, f));
        this.mc.player.method_36457(f);
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.player == null || !this.KaWPzeyl1xVKHWo) {
            return;
        }
        this.mc.player.method_36456(this.U6GoOdyLiE04);
        this.mc.player.method_36457(this.Icks58Pk4vQH3);
        this.KaWPzeyl1xVKHWo = false;
    }

    private void sE4h5W() {
        if (this.JDwgf5 == null) {
            this.JDwgf5 = VersionHelper.get().getPlayerPos();
        }
        this.mc.player.method_18799(Vec3d.field_1353);
        this.mc.player.method_5875(true);
        this.mc.player.method_5814(this.JDwgf5.x, this.JDwgf5.y, this.JDwgf5.z);
    }

    private void icVv2m6() {
        if (this.mc.player == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        if (this.mc.player.method_5869()) {
            return;
        }
        if (!this.mc.player.method_6118(DamageSource.field_6174).method_31574(Items.field_8833)) {
            return;
        }
        this.mc.getNetworkHandler().method_52787((Packet)new EntityTrackerEntry((Entity)this.mc.player, EntityTrackerEntry.class_2849.field_12982));
        this.mc.player.method_23669();
    }

    private void qKoJQYof() {
        if (this.mc.player.method_24828()) {
            if (!this.df1mByH0u()) {
                this.mc.player.method_6043();
                this.QTmNF6NCXs = (Integer)this.rocketTimeout.get();
            }
            return;
        }
    }

    public boolean df1mByH0u() {
        if (((Boolean)this.autoRocket.get()).booleanValue()) {
            for (Entity Entity2 : this.mc.world.method_18112()) {
                class_1671 class_16712;
                if (!(Entity2 instanceof class_1671) || (class_16712 = (class_1671)Entity2).method_24921() != this.mc.player) continue;
                return true;
            }
        }
        return (Boolean)this.autoRocket.get() == false && (double)(System.currentTimeMillis() - this.yyKeW1d7hG) < (Double)this.rocketDelay.get() * 1000.0;
    }

    public void jM7Ku65I6T() {
        if (this.df1mByH0u()) {
            return;
        }
        int n = InventoryManager.KP44bk(Items.field_8639);
        if (n == -1) {
            return;
        }
        InventoryManager.jOdDDFXSeWl4(n, () -> this.mc.field_1761.method_41931(this.mc.world, n -> new class_2886(InteractionHand.field_5808, n, this.mc.player.method_36454(), this.mc.player.method_36455())));
        MusheorSystem.debug("%sAttempted to fire a rocket!", Formatting.YELLOW);
        this.QTmNF6NCXs = (Integer)this.rocketTimeout.get();
        this.yyKeW1d7hG = System.currentTimeMillis();
    }

    public static boolean SNCRr7EZFUj() {
        return lYl0U01zxBqO9u > (Integer)KekFly.fly.launchDelay.get();
    }

    public static final class FlyMode
    extends Enum<FlyMode> {
        public static final /* enum */ FlyMode yjhDfCpm = new FlyMode();
        public static final /* enum */ FlyMode XWpV9Q7 = new FlyMode();
        private static final /* synthetic */ FlyMode[] YvaEDE3IjU1;

        public static FlyMode[] values() {
            return (FlyMode[])YvaEDE3IjU1.clone();
        }

        public static FlyMode valueOf(String string) {
            return Enum.valueOf(FlyMode.class, string);
        }

        private static /* synthetic */ FlyMode[] EZKBvX() {
            return new FlyMode[]{yjhDfCpm, XWpV9Q7};
        }

        static {
            YvaEDE3IjU1 = FlyMode.EZKBvX();
        }
    }
}

