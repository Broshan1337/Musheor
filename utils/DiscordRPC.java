// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.XfFrUB)
package musheor.utils;

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import musheor.utils.internal.HighwayState;
import musheor.utils.system.MusheorSystem;

/**
 * Discord Rich Presence integration (LOCAL Discord IPC socket only — no network
 * egress). Shows live/lifetime obsidian-placed/mined counts while building.
 */
public class DiscordRPC {
    private static final long APP_ID = 1346947991059300434L;              // was: FvaNWO (field)
    private static final RichPresence PRESENCE = new RichPresence();      // was: Q90GLXQ0Pef (field)

    /** Connects to Discord and initialises the presence. */
    public static void start() { // was: FvaNWO()
        DiscordIPC.start(APP_ID, null);
        PRESENCE.setStart(System.currentTimeMillis() / 1000L);
        PRESENCE.setLargeImage("musheor_logo", "Musheor Highway Builder");
        update();
    }

    /** Disconnects from Discord. */
    public static void stop() { // was: Q90GLXQ0Pef()
        DiscordIPC.stop();
    }

    /** Pushes current stats to Discord (only once the session has run >20 ticks). */
    public static void update() { // was: psJq59YIbp3Z()
        if (HighwayState.getInstance().getTicksActive() > 20) {
            PRESENCE.setDetails(buildDetails());
            PRESENCE.setState(buildState());
            DiscordIPC.setActivity(PRESENCE);
            MusheorSystem.debug("Updated RPC.");
        }
    }

    private static String buildDetails() { // was: SOYyh5IPg26f7F()
        return String.format("Obsidian placed | mined: %s | %s",
            StatsHandler.abbreviate(HighwayState.getInstance().getSessionObsidianPlaced()),
            StatsHandler.abbreviate(HighwayState.getInstance().getSessionObsidianMined()));
    }

    private static String buildState() { // was: rKbT3Ifwo()
        return String.format("Lifetime: %s placed | %s mined",
            StatsHandler.abbreviate(HighwayState.getInstance().getLifetimeObsidianPlaced()),
            StatsHandler.abbreviate(HighwayState.getInstance().getLifetimeObsidianMined()));
    }
}
