// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.fVHOZ)
package musheor.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.InventoryManager;
import musheor.utils.internal.HighwayState;
import net.minecraft.item.Items;

/**
 * Highway statistics: distance travelled, placement/mining rates, ETAs, and the
 * human-readable number/clock formatters used by the HUD and Discord RPC.
 *
 * In 1.5 this class also persisted lifetime totals to {@code data.csv}; in 1.6.1
 * that CSV persistence was removed — lifetime totals now come from Minecraft's own
 * StatHandler via {@link StatsCollector}. This class holds only the live math.
 */
public class StatsHandler {
    /** Cached distance travelled, refreshed while HighwayBuilder is active. */
    public static int cachedDistance = 0; // was: FvaNWO (field)

    /** Percentage of the current section completed, formatted to 2 decimals. */
    public static String getPercentComplete(int sectionSize) { // was: FvaNWO(int)
        double distanceLeft = getBlocksLeftInSection(sectionSize);
        double percentage = (sectionSize - distanceLeft) / sectionSize * 100.0;
        return String.format("%.2f", percentage);
    }

    /** Distance travelled along the highway axis since the module was enabled. */
    public static int computeDistanceTravelled() { // was: FvaNWO()
        HighwayState.getInstance();
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        assert MeteorClient.mc.player != null;
        int distanceTravelled = 0;
        if (direction == WorldUtils.Direction8.NORTH || direction == WorldUtils.Direction8.SOUTH) {
            distanceTravelled = Math.abs(MeteorClient.mc.player.getBlockZ()) - Math.abs(HighwayState.getInstance().getStartZ());
        } else if (direction == WorldUtils.Direction8.EAST || direction == WorldUtils.Direction8.WEST) {
            distanceTravelled = Math.abs(MeteorClient.mc.player.getBlockX()) - Math.abs(HighwayState.getInstance().getStartX());
        }
        if (direction == WorldUtils.Direction8.NORTH_EAST || direction == WorldUtils.Direction8.NORTH_WEST
            || direction == WorldUtils.Direction8.SOUTH_EAST || direction == WorldUtils.Direction8.SOUTH_WEST) {
            distanceTravelled = Math.abs(Math.abs(MeteorClient.mc.player.getBlockX()) - Math.abs(HighwayState.getInstance().getStartX()));
        }
        return Math.abs(distanceTravelled);
    }

    /** Returns the cached distance travelled, refreshing it if the module is active. */
    public static int getDistanceTravelled() { // was: Q90GLXQ0Pef()
        if (((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) {
            cachedDistance = computeDistanceTravelled();
        }
        return cachedDistance;
    }

    /** Obsidian blocks placed per second this session (-1 = idle). */
    public static double getBlocksPlacedPerSecond() { // was: psJq59YIbp3Z()
        if (!((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) return -1.0;
        double obsidianPlaced = HighwayState.getInstance().getSessionObsidianPlaced();
        double ticksPassed = HighwayState.getInstance().getTicksActive();
        return obsidianPlaced != 0.0 && ticksPassed != 0.0 ? obsidianPlaced / (ticksPassed / 20.0) : 0.0;
    }

    /** Blocks broken per second this session (obsidian + lava + misc; -1 = idle). */
    public static double getBlocksMinedPerSecond() { // was: SOYyh5IPg26f7F()
        if (!((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) return -1.0;
        HighwayState state = HighwayState.getInstance();
        double blocksBroken = state.getSessionObsidianMined() + state.getSessionLavaBuckets() + state.getSessionMiscMined();
        double ticksPassed = state.getTicksActive();
        return blocksBroken != 0.0 && ticksPassed != 0.0 ? blocksBroken / (ticksPassed / 20.0) : 0.0;
    }

    /** Obsidian available in inventory (ender chests count as 8 obsidian each). */
    public static int getAvailableObsidian() { // was: rKbT3Ifwo()
        return InventoryManager.countItemIncludingShulkers(Items.ENDER_CHEST) * 8
             + InventoryManager.countItemIncludingShulkers(Items.OBSIDIAN);
    }

    /** Rate = distance / seconds (seconds = ticks / 20). */
    public static double ratePerSecond(double distanceTravelled, double ticksPassed) { // was: FvaNWO(double,double)
        double secondsPassed = ticksPassed / 20.0;
        return secondsPassed <= 0.0 ? 0.0 : distanceTravelled / secondsPassed;
    }

    /** ETA rate for reaching the next section boundary (-2/-1 = calculating/waiting). */
    public static double getSectionEtaRate(int sectionSize) { // was: Q90GLXQ0Pef(int)
        HighwayState state = HighwayState.getInstance();
        if (((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) {
            long remainingDistance = getBlocksLeftInSection(sectionSize);
            long distanceTravelled = getDistanceTravelled();
            if (distanceTravelled <= 1L) return -2.0;
            return state.getTicksActive() <= 200
                ? -1.0
                : remainingDistance / ratePerSecond(distanceTravelled, state.getTicksActive());
        }
        return 0.0;
    }

    /** Blocks left until the next section boundary along the travel axis. */
    public static int getBlocksLeftInSection(int sectionSize) { // was: psJq59YIbp3Z(int)
        assert MeteorClient.mc.player != null;
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        int blockCoordinate = switch (direction) {
            case NORTH, SOUTH -> MeteorClient.mc.player.getBlockZ();
            case EAST, WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST -> MeteorClient.mc.player.getBlockX();
            case null, default -> 0;
        };
        int sectionStart = Math.floorDiv(blockCoordinate, sectionSize) * sectionSize;
        int sectionEnd = sectionStart + sectionSize;
        if (direction == WorldUtils.Direction8.SOUTH || direction == WorldUtils.Direction8.WEST
            || direction == WorldUtils.Direction8.NORTH_EAST || direction == WorldUtils.Direction8.SOUTH_EAST) {
            sectionStart = sectionEnd;
        }
        return Math.abs(sectionStart - blockCoordinate);
    }

    // ---- Formatters -------------------------------------------------------

    /** Abbreviates large numbers: 1_500_000 → "1.50m", 2_500 → "2.50k". */
    public static String abbreviate(int value) { // was: SOYyh5IPg26f7F(int)
        if (value >= 1_000_000) return String.format("%.2fm", value / 1_000_000.0);
        return value >= 1_000 ? String.format("%.2fk", value / 1_000.0) : String.valueOf(value);
    }

    public static String formatPlacementsPerSecond(double placementsPerSecond) { // was: FvaNWO(double)
        return placementsPerSecond == -1.0 ? "Waiting..." : String.format("%.2f blocks / s", placementsPerSecond);
    }

    public static String formatPlacementsPerHour(double placementsPerSecond) { // was: Q90GLXQ0Pef(double)
        return placementsPerSecond == -1.0 ? "Waiting..." : String.format("%.2f blocks / h", placementsPerSecond * 3600.0);
    }

    public static String formatDistancePerSecond(int distanceTravelled) { // was: rKbT3Ifwo(int)
        return distanceTravelled < 1 ? "Waiting..."
            : String.format("%.2f blocks / s", (float) ratePerSecond(distanceTravelled, HighwayState.getInstance().getTicksActive()));
    }

    public static String formatDistancePerHour(int distanceTravelled) { // was: r7hOYIKN2(int)
        return distanceTravelled < 1 ? "Waiting..."
            : String.format("%.2f blocks / h", (float) ratePerSecond(distanceTravelled, HighwayState.getInstance().getTicksActive()) * 3600.0F);
    }

    public static String formatBreakingPerSecond(double breakingPerSecond) { // was: psJq59YIbp3Z(double)
        return breakingPerSecond == -1.0 ? "Waiting..." : String.format("%.2f blocks / s", breakingPerSecond);
    }

    /** Mining-based ETA as H:MM:SS. */
    public static String formatMiningEta() { // was: r7hOYIKN2()
        if (getBlocksPlacedPerSecond() == -1.0) return "Waiting...";
        long timeLeft = (long) (getAvailableObsidian() / getBlocksPlacedPerSecond());
        return clock(timeLeft);
    }

    /** Section-based ETA as H:MM:SS. */
    public static String formatSectionEta(int sectionSize) { // was: oZHMlTL(int)
        if (getBlocksPlacedPerSecond() == -1.0) return "Waiting...";
        long timeLeft = (long) (getBlocksLeftInSection(sectionSize)
            / ratePerSecond(getDistanceTravelled(), HighwayState.getInstance().getTicksActive()));
        return clock(timeLeft);
    }

    /** Formats a tick count as an H:MM:SS clock (-1 = calculating, -2 = waiting). */
    public static String formatTicksAsClock(long ticks) { // was: FvaNWO(long)
        if (ticks == -1L) return "Calculating...";
        if (ticks == -2L) return "Waiting...";
        return clock(ticks / 20L);
    }

    private static String clock(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = totalSeconds % 3600L / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
