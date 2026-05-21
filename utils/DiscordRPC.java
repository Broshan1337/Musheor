// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import musheor.utils.internal.HighwayState;
import musheor.utils.system.MusheorSystem;

/**
 * Manages Discord Rich Presence showing highway building stats.
 * Connects to Discord via local IPC (not network). Reads-only local highway state.
 */
public class DiscordRPC {
    // Discord Application ID for the Musheor app
    private static final long APP_ID = 1346947991059300434L;
    private static final RichPresence presence = new RichPresence();

    /** Start Discord RPC and show initial activity. */
    public static void start() {
        DiscordIPC.start(APP_ID, null);
        presence.setStart(System.currentTimeMillis() / 1000L);
        presence.setLargeImage(
            "https://cdn.discordapp.com/app-icons/1346947991059300434/488ce08bbd35ade7c2d6ae1e552ec8e2.png?size=512",
            "Musheor Highway Builder"
        );
        updateActivity();
    }

    /** Stop Discord RPC. */
    public static void stop() {
        DiscordIPC.stop();
    }

    /** Push updated highway stats to Discord Rich Presence. Only updates after 20 ticks. */
    public static void updateActivity() {
        if (HighwayState.getInstance().getTicksActive() <= 20) {
            return;
        }
        presence.setDetails(getDetailsString());
        presence.setState(getStateString());
        DiscordIPC.setActivity(presence);
        MusheorSystem.debug("Updated RPC.");
    }

    /** Returns the RPC details line: current session obsidian placed/mined. */
    private static String getDetailsString() {
        return String.format(
            "Obsidian placed | mined: %s | %s",
            HighwayState.getInstance().getSessionObsidianPlaced(),
            HighwayState.getInstance().getSessionObsidianMined()
        );
    }

    /** Returns the RPC state line: lifetime obsidian placed/mined. */
    private static String getStateString() {
        return String.format(
            "Lifetime: %s placed | %s mined",
            HighwayState.getInstance().getLifetimeObsidianPlaced(),
            HighwayState.getInstance().getLifetimeObsidianMined()
        );
    }
}
