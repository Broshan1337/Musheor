// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

/**
 * "logout-goal" — disconnects (via a client-side fake disconnect packet) when the player
 * reaches a configured X/Z (within {@code range}) in the chosen dimension. Optionally turns
 * off Meteor's AutoReconnect, self-disables, and appends the arrival coordinates to the
 * disconnect message.
 */
public class LogoutGoal extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: Q90GLXQ0Pef

    private final Setting<Dimension> dimension = sgGeneral.add(new EnumSetting.Builder<Dimension>() // was: psJq59YIbp3Z
        .name("dimension").description("Dimension player is inside.").defaultValue(Dimension.Nether).build());
    private final Setting<Integer> xCoord = sgGeneral.add(new IntSetting.Builder() // was: SOYyh5IPg26f7F
        .name("x-coord").description("The X coordinate (world border is at ~ 29.999.983).").defaultValue(0).range(-29999949, 29999949).noSlider().build());
    private final Setting<Integer> zCoord = sgGeneral.add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("z-coord").description("The Z coordinate (world border is at ~ 29.999.983).").defaultValue(0).range(-29999949, 29999949).noSlider().build());
    private final Setting<Boolean> disableAutoReconnect = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("disable_auto-reconnect").description("Turns off AutoReconnect when logging out.").defaultValue(true).build());
    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("auto-disable").description("Automatically disables itself when reaching your destination.").defaultValue(true).build());
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("range").description("Distance from the goal at which the player will log out.").defaultValue(32).min(0).sliderRange(0, 200).build());
    private final Setting<String> disconnectText = sgGeneral.add(new StringSetting.Builder() // was: OMMZL1F3q
        .name("text").description("Text displayed on disconnect.").defaultValue("Arrived at destination.").build());
    private final Setting<Boolean> showCoords = sgGeneral.add(new BoolSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("Coords").description("Display coordinates on logout screen.").defaultValue(true).build());

    public LogoutGoal() {
        super(musheor.MAIN, "logout-goal", "Automatically disconnect when arriving at a specific location.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        int playerX = (int) Math.round(this.mc.player.getX());
        int playerZ = (int) Math.round(this.mc.player.getZ());
        if (this.isWithinX() && this.isWithinZ() && PlayerUtils.getDimension() == this.dimension.get()) {
            String message = this.disconnectText.get();
            if (this.disableAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class)) {
                ((AutoReconnect) Modules.get().get(AutoReconnect.class)).toggle();
            }
            if (this.autoDisable.get()) this.toggle();
            if (this.showCoords.get()) message = message + " X: " + playerX + " Z: " + playerZ;

            musheor.utils.PlayerUtils.sendChatMessage(message);
            this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal(message)));
        }
    }

    private boolean isWithinX() { // was: FvaNWO()
        assert this.mc.player != null;
        return this.mc.player.getX() <= this.xCoord.get() + this.range.get() && this.mc.player.getX() >= this.xCoord.get() - this.range.get();
    }

    private boolean isWithinZ() { // was: Q90GLXQ0Pef()
        assert this.mc.player != null;
        return this.mc.player.getZ() <= this.zCoord.get() + this.range.get() && this.mc.player.getZ() >= this.zCoord.get() - this.range.get();
    }
}
