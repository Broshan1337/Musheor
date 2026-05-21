// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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

public class HudInfoPlus
extends HudElement {
    public static final HudElementInfo<HudInfoPlus> INFO = new HudElementInfo(musheor.MUSHEOR_HUD, "packet-limits", "A hud module that displays how many packets you are sending to the server", HudInfoPlus::new);
    private final Setting<Double> scale;
    private static final Deque<Long> O3n0DkBJhp7 = new ArrayDeque<Long>();
    private static final Deque<Long> Gc5AFJxsc3y = new ArrayDeque<Long>();
    private static final Deque<Long> SMd0PdrY = new ArrayDeque<Long>();

    public HudInfoPlus() {
        super(INFO);
        this.scale = this.settings.getDefaultGroup().add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).defaultValue(1.0).build());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void yUTSjfYE2q2du() {
        Deque<Long> deque = O3n0DkBJhp7;
        synchronized (deque) {
            O3n0DkBJhp7.addLast(System.currentTimeMillis());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void Y036W9pcsZhAYFUl() {
        Deque<Long> deque = Gc5AFJxsc3y;
        synchronized (deque) {
            Gc5AFJxsc3y.addLast(System.currentTimeMillis());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void TfF42oD7() {
        Deque<Long> deque = SMd0PdrY;
        synchronized (deque) {
            SMd0PdrY.addLast(System.currentTimeMillis());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick() {
        Long l;
        long l2 = System.currentTimeMillis();
        Deque<Long> deque = O3n0DkBJhp7;
        synchronized (deque) {
            while ((l = O3n0DkBJhp7.peekFirst()) != null && l2 - l > 4000L) {
                O3n0DkBJhp7.pollFirst();
            }
        }
        deque = Gc5AFJxsc3y;
        synchronized (deque) {
            while ((l = Gc5AFJxsc3y.peekFirst()) != null && l2 - l > 4000L) {
                Gc5AFJxsc3y.pollFirst();
            }
        }
        deque = SMd0PdrY;
        synchronized (deque) {
            l = SMd0PdrY.peekFirst();
            if (l != null && l2 - l > 310L) {
                SMd0PdrY.clear();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int kBQdZKStLMVDV() {
        Deque<Long> deque = O3n0DkBJhp7;
        synchronized (deque) {
            return O3n0DkBJhp7.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int XuSVOP3J5xFv() {
        Deque<Long> deque = Gc5AFJxsc3y;
        synchronized (deque) {
            return Gc5AFJxsc3y.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int Y1fGfDLuV() {
        Deque<Long> deque = SMd0PdrY;
        synchronized (deque) {
            return SMd0PdrY.size();
        }
    }

    /*
     * WARNING - void declaration
     */
    public void render(HudRenderer hudRenderer) {
        void var13_17;
        int n = HudInfoPlus.kBQdZKStLMVDV();
        int n2 = HudInfoPlus.XuSVOP3J5xFv();
        int n3 = HudInfoPlus.Y1fGfDLuV();
        String[][] stringArrayArray = new String[][]{{"Inv", String.valueOf(n)}, {"Global", String.valueOf(n2)}, {"Int", String.valueOf(n3)}};
        double d = hudRenderer.textHeight(true, ((Double)this.scale.get()).doubleValue());
        double d2 = 0.0;
        for (String[] color2 : stringArrayArray) {
            double d3 = hudRenderer.textWidth(color2[0], true) + hudRenderer.textWidth(": ", true) + hudRenderer.textWidth(color2[1], true, ((Double)this.scale.get()).doubleValue());
            d2 = Math.max(d2, d3);
        }
        this.setSize(d2, d * (double)stringArrayArray.length);
        double d4 = this.y;
        double d5 = this.x;
        Color color3 = (double)n < (double)((Integer)MusheorSystem.Manager.invPacketLimit.get()).intValue() * 0.5 ? Color.GREEN : ((double)n < (double)((Integer)MusheorSystem.Manager.invPacketLimit.get()).intValue() * 0.8 ? Color.YELLOW : Color.RED);
        if ((double)n2 < (double)((Integer)MusheorSystem.Manager.globalPacketLimit.get()).intValue() * 0.5) {
            Color color = Color.GREEN;
        } else if ((double)n2 < (double)((Integer)MusheorSystem.Manager.globalPacketLimit.get()).intValue() * 0.8) {
            Color color = Color.YELLOW;
        } else {
            Color color = Color.RED;
        }
        Color color = (double)n3 < 4.5 ? Color.GREEN : ((double)n3 < 7.2 ? Color.YELLOW : Color.RED);
        String string = String.format("global: %d/%d", n2, MusheorSystem.Manager.globalPacketLimit.get());
        hudRenderer.text(string, d5, d4, (Color)var13_17, true, ((Double)this.scale.get()).doubleValue());
        String string2 = String.format("inv: %d/%d", n, MusheorSystem.Manager.invPacketLimit.get());
        hudRenderer.text(string2, d5, d4 += d, color3, true, ((Double)this.scale.get()).doubleValue());
        String string3 = String.format("int: %d/%d", n3, 9);
        hudRenderer.text(string3, d5, d4 += d, color, true, ((Double)this.scale.get()).doubleValue());
    }
}

