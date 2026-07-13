// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.hud;

import java.util.ArrayDeque;
import java.util.Deque;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import musheor.musheor;
import musheor.utils.system.MusheorSystem;

/**
 * "packet-limits" HUD — tracks how many packets have been sent in sliding time windows and
 * shows them against the configured limits: inventory and global packets over the last 4s,
 * and interaction packets over the last ~310ms (limit 9). Colours each line green/yellow/red
 * as it approaches its limit. The counters are fed by the packet-sending code (mixins).
 */
public class HudInfoPlus extends HudElement {
    public static final HudElementInfo<HudInfoPlus> INFO = new HudElementInfo<>(
        musheor.MUSHEOR_HUD, "packet-limits", "A hud module that displays how many packets you are sending to the server", HudInfoPlus::new);

    private final Setting<Double> scale = this.settings.getDefaultGroup().add(new DoubleSetting.Builder().name("scale").defaultValue(1.0).build()); // was: FvaNWO

    private static final Deque<Long> invPackets = new ArrayDeque<>();  // was: Q90GLXQ0Pef (inventory packet timestamps, 4s window)
    private static final Deque<Long> packets = new ArrayDeque<>();     // was: psJq59YIbp3Z (global packet timestamps, 4s window)
    private static final Deque<Long> sentPackets = new ArrayDeque<>(); // was: SOYyh5IPg26f7F (interaction packet timestamps, 310ms window)

    public HudInfoPlus() {
        super(INFO);
    }

    /** Records an inventory packet. */
    public static void recordInvPacket() { // was: FvaNWO()
        synchronized (invPackets) { invPackets.addLast(System.currentTimeMillis()); }
    }

    /** Records a global packet. */
    public static void recordPacket() { // was: Q90GLXQ0Pef()
        synchronized (packets) { packets.addLast(System.currentTimeMillis()); }
    }

    /** Records an interaction packet. */
    public static void recordSentPacket() { // was: psJq59YIbp3Z()
        synchronized (sentPackets) { sentPackets.addLast(System.currentTimeMillis()); }
    }

    /** Drops timestamps that have fallen outside their window (inv/global: 4s, interaction: 310ms). */
    public static void pruneExpired() { // was: SOYyh5IPg26f7F()
        long now = System.currentTimeMillis();
        Long first;
        synchronized (invPackets) {
            while ((first = invPackets.peekFirst()) != null && now - first > 4000L) invPackets.pollFirst();
        }
        synchronized (packets) {
            while ((first = packets.peekFirst()) != null && now - first > 4000L) packets.pollFirst();
        }
        synchronized (sentPackets) {
            first = sentPackets.peekFirst();
            if (first != null && now - first > 310L) sentPackets.clear();
        }
    }

    /** Inventory packets in the last 4 seconds. */
    public static int getInvPacketCount() { // was: rKbT3Ifwo()
        synchronized (invPackets) { return invPackets.size(); }
    }

    /** Global packets in the last 4 seconds. */
    public static int getPacketCount() { // was: r7hOYIKN2()
        synchronized (packets) { return packets.size(); }
    }

    /** Interaction packets in the last ~310ms. */
    public static int getSentPacketCount() { // was: oZHMlTL()
        synchronized (sentPackets) { return sentPackets.size(); }
    }

    @Override
    public void render(HudRenderer renderer) {
        int inventoryCount = getInvPacketCount();
        int generalCount = getPacketCount();
        int interactionCount = getSentPacketCount();
        String[][] lines = {{"Inv", String.valueOf(inventoryCount)}, {"Global", String.valueOf(generalCount)}, {"Int", String.valueOf(interactionCount)}};
        double lineHeight = renderer.textHeight(true, this.scale.get());
        double width = 0.0;
        for (String[] line : lines) {
            double lineWidth = renderer.textWidth(line[0], true) + renderer.textWidth(": ", true) + renderer.textWidth(line[1], true, this.scale.get());
            width = Math.max(width, lineWidth);
        }
        this.setSize(width, lineHeight * lines.length);

        double y = this.y;
        double x = this.x;
        Color invColor = colorFor(inventoryCount, MusheorSystem.Manager.invPacketLimit.get());
        Color globalColor = colorFor(generalCount, MusheorSystem.Manager.globalPacketLimit.get());
        Color intColor;
        if (interactionCount < 4.5) intColor = Color.GREEN;
        else if (interactionCount < 7.2) intColor = Color.YELLOW;
        else intColor = Color.RED;

        renderer.text(String.format("global: %d/%d", generalCount, MusheorSystem.Manager.globalPacketLimit.get()), x, y, globalColor, true, this.scale.get());
        y += lineHeight;
        renderer.text(String.format("inv: %d/%d", inventoryCount, MusheorSystem.Manager.invPacketLimit.get()), x, y, invColor, true, this.scale.get());
        y += lineHeight;
        renderer.text(String.format("int: %d/%d", interactionCount, 9), x, y, intColor, true, this.scale.get());
    }

    /** Green below 50% of {@code limit}, yellow below 80%, red otherwise. */
    private static Color colorFor(int count, int limit) {
        if (count < limit * 0.5) return Color.GREEN;
        if (count < limit * 0.8) return Color.YELLOW;
        return Color.RED;
    }
}
