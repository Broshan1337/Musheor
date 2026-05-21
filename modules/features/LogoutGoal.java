// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.PlayerUtils;
import net.minecraft.ScreenHandler;
import net.minecraft.class_2661;

public class LogoutGoal
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Dimension> dimension;
    private final Setting<Integer> xCoords;
    private final Setting<Integer> zCoords;
    private final Setting<Boolean> disableAutoReconnect;
    private final Setting<Boolean> toggle;
    private final Setting<Integer> range;
    private final Setting<String> text;
    private final Setting<Boolean> coords;

    public LogoutGoal() {
        super(musheor.MAIN, "logout-goal", "Automatically disconnect when arriving at a specific location.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.dimension = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("dimension")).description("Dimension player is inside.")).defaultValue((Object)Dimension.Nether)).build());
        this.xCoords = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("x-coord")).description("The X coordinate (world border is at ~ 29.999.983).")).defaultValue((Object)0)).range(-29999949, 29999949).noSlider().build());
        this.zCoords = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("z-coord")).description("The Z coordinate (world border is at ~ 29.999.983).")).defaultValue((Object)0)).range(-29999949, 29999949).noSlider().build());
        this.disableAutoReconnect = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable_auto-reconnect")).description("Turns off AutoReconnect when logging out.")).defaultValue((Object)true)).build());
        this.toggle = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-disable")).description("Automatically disables itself when reaching your destination.")).defaultValue((Object)true)).build());
        this.range = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("range")).description("Distance from the goal at which the player will log out.")).defaultValue((Object)32)).min(0).sliderRange(0, 200).build());
        this.text = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("text")).description("Text displayed on disconnect.")).defaultValue((Object)"Arrived at destination.")).build());
        this.coords = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("Coords")).description("Display coordinates on logout screen.")).defaultValue((Object)true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        int n = (int)Math.round(this.mc.player.getX());
        int n2 = (int)Math.round(this.mc.player.getZ());
        if (this.FIc3lket() && this.NvJtheFonk() && meteordevelopment.meteorclient.utils.player.PlayerUtils.getDimension() == this.dimension.get()) {
            Object object = (String)this.text.get();
            if (((Boolean)this.disableAutoReconnect.get()).booleanValue() && Modules.get().isActive(AutoReconnect.class)) {
                ((AutoReconnect)Modules.get().get(AutoReconnect.class)).toggle();
            }
            if (((Boolean)this.toggle.get()).booleanValue()) {
                this.toggle();
            }
            if (((Boolean)this.coords.get()).booleanValue()) {
                object = (String)object + " X: " + n + " Z: " + n2;
            }
            PlayerUtils.J2pm2c07elEb5G((String)object);
            this.mc.player.field_3944.method_52781(new class_2661((ScreenHandler)ScreenHandler.method_43470((String)object)));
        }
    }

    private boolean FIc3lket() {
        assert (this.mc.player != null);
        return this.mc.player.getX() <= (double)((Integer)this.xCoords.get() + (Integer)this.range.get()) && this.mc.player.getX() >= (double)((Integer)this.xCoords.get() - (Integer)this.range.get());
    }

    private boolean NvJtheFonk() {
        assert (this.mc.player != null);
        return this.mc.player.getZ() <= (double)((Integer)this.zCoords.get() + (Integer)this.range.get()) && this.mc.player.getZ() >= (double)((Integer)this.zCoords.get() - (Integer)this.range.get());
    }
}

