// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PlacementEngine;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.InteractionHand;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.BlockState;
import net.minecraft.class_2885;
import net.minecraft.MinecraftClient;
import net.minecraft.Screen;

public class AutoPortal
extends Module {
    private static final MinecraftClient pYDFCzX50EA = MinecraftClient.getInstance();
    private static final SettingColor qTGt6ffFpdBAIaF = new SettingColor(255, 45, 45, 220);
    private static final SettingColor jiHRZgkisVF = new SettingColor(255, 45, 45, 60);
    private final Setting<Boolean> autoDisable;
    private final Setting<Boolean> pauseOnInput;
    private final Setting<Boolean> renderPos;
    private final Setting<SettingColor> renderColor;
    private boolean msmOdasZ;
    private Phase KWA2BzTciv4Q7Yt;
    private final List<BlockPos> yNMw0ToiAtkLQLLo;
    private final List<BlockPos> Op6JoJ6PKB;
    private BlockPos Ev08e0;
    private int x2JWSQiCNC;
    private int foyBzXEh1eRCTh4;

    public AutoPortal() {
        super(musheor.MAIN, "auto-portal", "Automatically builds and lights a nether portal");
        this.autoDisable = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-disable")).description("Automatically disables the module when a portal is built")).defaultValue((Object)true)).build());
        this.pauseOnInput = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-input")).description("Pauses the building process when you are moving, sneaking or jumping")).defaultValue((Object)true)).build());
        this.renderPos = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-portal")).description("Renders the portal's position")).defaultValue((Object)true)).build());
        this.renderColor = this.settings.getDefaultGroup().add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("render-color")).defaultValue(new SettingColor(0, 225, 255, 200)).description("Custom color for rendering (lines / wireframe)")).visible(() -> this.renderPos.get())).build());
        this.yNMw0ToiAtkLQLLo = new ArrayList<BlockPos>();
        this.Op6JoJ6PKB = new ArrayList<BlockPos>();
    }

    public void onActivate() {
        this.msmOdasZ = false;
        this.KWA2BzTciv4Q7Yt = Phase.eTZdz0xBm;
        this.yNMw0ToiAtkLQLLo.clear();
        this.Op6JoJ6PKB.clear();
        this.Ev08e0 = null;
        this.x2JWSQiCNC = 0;
        this.foyBzXEh1eRCTh4 = 0;
        HighwayState.LmpuWjra().Os3dd8a().clear();
    }

    public void onDeactivate() {
        this.yNMw0ToiAtkLQLLo.clear();
        this.Op6JoJ6PKB.clear();
        this.Ev08e0 = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (AutoPortal.pYDFCzX50EA.player == null || AutoPortal.pYDFCzX50EA.world == null || AutoPortal.pYDFCzX50EA.field_1761 == null) {
            return;
        }
        if (!this.msmOdasZ) {
            this.SMYZpUvCuykws2();
        }
        HighwayState.LmpuWjra().Os3dd8a().entrySet().removeIf(entry -> {
            BlockState BlockState2 = AutoPortal.pYDFCzX50EA.world.getBlockState((BlockPos)entry.getKey());
            if (BlockState2 != null && AutoPortal.pYDFCzX50EA.world.getBlockState((BlockPos)entry.getKey()).getBlock() == Blocks.field_10540) {
                return true;
            }
            return this.foyBzXEh1eRCTh4 - (Integer)entry.getValue() > (Integer)MusheorSystem.Manager.placementTimeout.get();
        });
        if (this.x2JWSQiCNC > 0) {
            --this.x2JWSQiCNC;
        }
        if (((Boolean)this.pauseOnInput.get()).booleanValue() && this.Tlldfou()) {
            return;
        }
        if (!this.msmOdasZ) {
            int n = InventoryManager.ZbTtF5KYyGL9YXed(Items.OBSIDIAN);
            if (n < 10) {
                this.error("Not enough obsidian (need %d, have %d), disabling.", new Object[]{10, n});
                this.toggle();
                return;
            }
            if (!InvUtils.find(ItemStack2 -> ItemStack2.getStack() == Items.field_8884).found()) {
                this.error("No flint and steel found in inventory, disabling.", new Object[0]);
                this.toggle();
                return;
            }
            this.msmOdasZ = true;
            this.KWA2BzTciv4Q7Yt = Phase.eTZdz0xBm;
            this.info("Position locked, building portal...", new Object[0]);
        }
        switch (this.KWA2BzTciv4Q7Yt.ordinal()) {
            case 0: {
                this.VYEwzRq(this.foyBzXEh1eRCTh4++);
                break;
            }
            case 1: {
                this.ZR5lph5QmbF4();
                break;
            }
            case 2: {
                this.rsx7hBWYw();
            }
        }
    }

    private void VYEwzRq(int n) {
        boolean bl = false;
        for (BlockPos object : this.yNMw0ToiAtkLQLLo) {
            if (AutoPortal.pYDFCzX50EA.world.getBlockState(object).getBlock() == Blocks.field_10540 || !AutoPortal.pYDFCzX50EA.world.getBlockState(object).method_45474() || !WorldUtils.KDNrzlU9qtrEv(object)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            if (this.BJiJWZC()) {
                this.KWA2BzTciv4Q7Yt = Phase.bNZTMRD;
                this.x2JWSQiCNC = (Integer)MusheorSystem.Manager.swapDelay.get();
            }
            return;
        }
        if (this.x2JWSQiCNC > 0) {
            return;
        }
        FindItemResult findItemResult = InvUtils.findInHotbar(ItemStack2 -> ItemStack2.getStack() == Items.OBSIDIAN);
        if (!findItemResult.found()) {
            InventoryManager.UgB10d(Items.OBSIDIAN);
            this.x2JWSQiCNC = (Integer)MusheorSystem.Manager.swapDelay.get();
            return;
        }
        InventoryManager.L5CF0C6jx0T17H4I(Items.OBSIDIAN);
        for (BlockPos BlockPos2 : this.yNMw0ToiAtkLQLLo) {
            if (!RateController.qy8UwM99rVr()) break;
            if (HighwayState.LmpuWjra().Os3dd8a().containsKey(BlockPos2) || AutoPortal.pYDFCzX50EA.world.getBlockState(BlockPos2).getBlock() == Blocks.field_10540 || !AutoPortal.pYDFCzX50EA.world.getBlockState(BlockPos2).method_45474() || !WorldUtils.KDNrzlU9qtrEv(BlockPos2)) continue;
            HighwayState.LmpuWjra().Os3dd8a().put(BlockPos2, n);
            PlacementEngine.jOdDDFXSeWl4(BlockPos2, Direction.field_11036);
        }
    }

    private void ZR5lph5QmbF4() {
        if (this.Ev08e0 == null || !WorldUtils.KDNrzlU9qtrEv(this.Ev08e0)) {
            return;
        }
        if (this.x2JWSQiCNC > 0 || !RateController.qy8UwM99rVr()) {
            return;
        }
        FindItemResult findItemResult = InvUtils.findInHotbar(ItemStack2 -> ItemStack2.getStack() == Items.field_8884);
        if (!findItemResult.found()) {
            if (!InvUtils.find(ItemStack2 -> ItemStack2.getStack() == Items.field_8884).found()) {
                this.error("Lost flint and steel during build, disabling.", new Object[0]);
                this.toggle();
                return;
            }
            InventoryManager.UgB10d(Items.field_8884);
            this.x2JWSQiCNC = (Integer)MusheorSystem.Manager.swapDelay.get();
            return;
        }
        InvUtils.swap((int)findItemResult.slot(), (boolean)false);
        Screen Screen2 = new Screen(Vec3d.method_24953((BlockPos)this.Ev08e0).method_1031(0.0, 0.5, 0.0), Direction.field_11036, this.Ev08e0, false);
        AutoPortal.pYDFCzX50EA.field_1761.method_41931(AutoPortal.pYDFCzX50EA.world, n -> new class_2885(InteractionHand.field_5808, Screen2, n));
        this.KWA2BzTciv4Q7Yt = Phase.PhVJOAQVInCST;
    }

    private void rsx7hBWYw() {
        if (this.zFc9E6nf()) {
            this.info("Portal built successfully!", new Object[0]);
            if (((Boolean)this.autoDisable.get()).booleanValue()) {
                this.toggle();
            }
        } else {
            this.KWA2BzTciv4Q7Yt = Phase.bNZTMRD;
        }
    }

    private boolean BJiJWZC() {
        for (BlockPos BlockPos2 : this.yNMw0ToiAtkLQLLo) {
            if (AutoPortal.pYDFCzX50EA.world.getBlockState(BlockPos2).getBlock() == Blocks.field_10540) continue;
            return false;
        }
        return true;
    }

    private boolean zFc9E6nf() {
        for (BlockPos BlockPos2 : this.Op6JoJ6PKB) {
            if (AutoPortal.pYDFCzX50EA.world.getBlockState(BlockPos2).getBlock() != Blocks.AIR) continue;
            return true;
        }
        return false;
    }

    private boolean Tlldfou() {
        return AutoPortal.pYDFCzX50EA.options.field_1894.method_1434() || AutoPortal.pYDFCzX50EA.options.field_1881.method_1434() || AutoPortal.pYDFCzX50EA.options.field_1913.method_1434() || AutoPortal.pYDFCzX50EA.options.field_1849.method_1434() || AutoPortal.pYDFCzX50EA.options.field_1903.method_1434() || AutoPortal.pYDFCzX50EA.options.field_1832.method_1434();
    }

    private Direction jOdDDFXSeWl4(float f) {
        float f2 = (f % 360.0f + 360.0f) % 360.0f;
        if (f2 >= 315.0f || f2 < 45.0f) {
            return Direction.field_11035;
        }
        if (f2 < 135.0f) {
            return Direction.field_11039;
        }
        if (f2 < 225.0f) {
            return Direction.field_11043;
        }
        return Direction.field_11034;
    }

    private int mp3zoXQFKUKYj5(float f) {
        float f2 = Math.max(-45.0f, Math.min(45.0f, f));
        int n = Math.round(-f2 / 9.0f);
        return Math.max(-5, Math.min(5, n));
    }

    private void SMYZpUvCuykws2() {
        this.yNMw0ToiAtkLQLLo.clear();
        this.Op6JoJ6PKB.clear();
        this.Ev08e0 = null;
        BlockPos BlockPos2 = AutoPortal.pYDFCzX50EA.player.getBlockPos();
        Direction Direction2 = this.jOdDDFXSeWl4(AutoPortal.pYDFCzX50EA.player.method_36454());
        int n = this.mp3zoXQFKUKYj5(AutoPortal.pYDFCzX50EA.player.method_36455());
        int n2 = Direction2.method_10148();
        int n3 = Direction2.method_10165();
        int n4 = -n3;
        int n5 = n2;
        int n6 = BlockPos2.getX() + n2 * 2;
        int n7 = BlockPos2.getZ() + n3 * 2;
        int n8 = BlockPos2.getY() + n;
        for (int i = 0; i < 5; ++i) {
            for (int j = -1; j <= 2; ++j) {
                boolean bl;
                BlockPos BlockPos3 = new BlockPos(n6 + n4 * j, n8 + i, n7 + n5 * j);
                boolean bl2 = !(i != 0 && i != 4 || j != -1 && j != 2);
                boolean bl3 = bl = i >= 1 && i <= 3 && j >= 0 && j <= 1;
                if (bl2) continue;
                if (bl) {
                    this.Op6JoJ6PKB.add(BlockPos3);
                    continue;
                }
                this.yNMw0ToiAtkLQLLo.add(BlockPos3);
            }
        }
        for (BlockPos BlockPos4 : this.yNMw0ToiAtkLQLLo) {
            if (BlockPos4.getY() != n8) continue;
            this.Ev08e0 = BlockPos4;
            break;
        }
    }

    private boolean Rvjkko3BhJ() {
        BlockState BlockState2;
        for (BlockPos BlockPos2 : this.yNMw0ToiAtkLQLLo) {
            BlockState2 = AutoPortal.pYDFCzX50EA.world.getBlockState(BlockPos2);
            if (BlockState2.getBlock() == Blocks.field_10540 || BlockState2.method_45474()) continue;
            return true;
        }
        for (BlockPos BlockPos2 : this.Op6JoJ6PKB) {
            BlockState2 = AutoPortal.pYDFCzX50EA.world.getBlockState(BlockPos2);
            if (BlockState2.getBlock() == Blocks.AIR || BlockState2.getBlock() == Blocks.field_10036 || BlockState2.method_45474()) continue;
            return true;
        }
        return false;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (AutoPortal.pYDFCzX50EA.player == null || AutoPortal.pYDFCzX50EA.world == null) {
            return;
        }
        if (!((Boolean)this.renderPos.get()).booleanValue()) {
            return;
        }
        if (this.yNMw0ToiAtkLQLLo.isEmpty() && this.Op6JoJ6PKB.isEmpty()) {
            return;
        }
        boolean bl = this.Rvjkko3BhJ();
        SettingColor settingColor = bl ? qTGt6ffFpdBAIaF : (SettingColor)this.renderColor.get();
        SettingColor settingColor2 = bl ? jiHRZgkisVF : (SettingColor)this.renderColor.get();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(this.yNMw0ToiAtkLQLLo.size() + this.Op6JoJ6PKB.size());
        arrayList.addAll(this.yNMw0ToiAtkLQLLo);
        arrayList.addAll(this.Op6JoJ6PKB);
        RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList, (Color)settingColor, (Color)settingColor2, ShapeMode.Lines);
    }

    static final class Phase
    extends Enum<Phase> {
        public static final /* enum */ Phase eTZdz0xBm = new Phase();
        public static final /* enum */ Phase bNZTMRD = new Phase();
        public static final /* enum */ Phase PhVJOAQVInCST = new Phase();
        private static final /* synthetic */ Phase[] uWBPgePFuwvwE3;

        public static Phase[] values() {
            return (Phase[])uWBPgePFuwvwE3.clone();
        }

        public static Phase valueOf(String string) {
            return Enum.valueOf(Phase.class, string);
        }

        private static /* synthetic */ Phase[] FERZ92ROAgUmnlta() {
            return new Phase[]{eTZdz0xBm, bNZTMRD, PhVJOAQVInCST};
        }

        static {
            uWBPgePFuwvwE3 = Phase.FERZ92ROAgUmnlta();
        }
    }
}

