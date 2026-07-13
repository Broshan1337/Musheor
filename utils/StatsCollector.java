// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.ttI5)
package musheor.utils;

import musheor.utils.internal.HighwayState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;

/**
 * Pulls the player's lifetime block statistics from Minecraft's own StatHandler
 * and feeds them into {@link HighwayState}. Replaces the v1.5 CSV-based lifetime
 * persistence (data.csv is gone in 1.6.1).
 */
public class StatsCollector {

    /** Asks the server to send the up-to-date stats snapshot. */
    public static void requestStats() { // was: FvaNWO()
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
        }
    }

    /** Reads mined/placed counters from StatHandler into HighwayState lifetime totals. */
    public static void collectStats() { // was: Q90GLXQ0Pef()
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        StatHandler stats = mc.player.getStatHandler();
        HighwayState state = HighwayState.getInstance();

        int mined    = stats.getStat(Stats.MINED, Blocks.OBSIDIAN);
        int placed   = stats.getStat(Stats.USED,  Items.OBSIDIAN);
        int echests  = stats.getStat(Stats.MINED, Blocks.ENDER_CHEST);
        int crying   = stats.getStat(Stats.MINED, Blocks.CRYING_OBSIDIAN); // was: class_2246.field_10515

        state.setLifetimeObsidianMined(mined);
        state.setLifetimeObsidianPlaced(placed);
        state.setLifetimeEchests(echests);
        state.setLifetimeMiscBlocks(crying);
        state.setLifetimeTotalMined(mined + echests + crying);
    }

    /** Requests a fresh snapshot then folds it into HighwayState. */
    public static void update() { // was: psJq59YIbp3Z()
        requestStats();
        collectStats();
    }
}
