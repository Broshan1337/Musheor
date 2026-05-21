// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.automation.HighwayBuilder;
import musheor.utils.InventoryManager;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import net.minecraft.item.Items; // Items

/**
 * Handles persistent highway statistics, stored locally in
 * MeteorClient.FOLDER/musheor/data.csv. Also provides helper methods
 * for formatting and computing rates/ETAs shown in the HUD.
 */
public class StatsHandler {
    /** Persistent CSV file for lifetime highway statistics. */
    private static final File DATA_FILE = new File(MeteorClient.FOLDER, "musheor/data.csv");

    /** Tracks distance traveled since the highway builder was enabled. */
    public static int distanceTraveled = 0;

    // -------------------------------------------------------------------------
    // File I/O
    // -------------------------------------------------------------------------

    /** Write all rows to the CSV file (overwrites existing content). */
    public static void writeData(List<String[]> rows) {
        try {
            FileWriter fw = new FileWriter(DATA_FILE);
            for (CharSequence[] row : rows) {
                fw.write(String.join(",", row));
                fw.write("\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Read all rows from the CSV file. */
    public static List<String[]> readData() {
        ArrayList<String[]> result = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(DATA_FILE));
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line.split(","));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Called once on addon init. Creates the data.csv file with zeroed rows
     * if it does not already exist.
     */
    public static void initDataFile() {
        try {
            if (!DATA_FILE.getParentFile().exists()) {
                DATA_FILE.getParentFile().mkdirs();
            }
            if (!DATA_FILE.exists() && DATA_FILE.createNewFile()) {
                try (FileWriter fw = new FileWriter(DATA_FILE)) {
                    for (int i = 0; i <= 4; i++) {
                        fw.write("0,\n");
                    }
                }
                System.out.println("Created data.csv with headers.");
            }
        } catch (IOException e) {
            System.err.println("Failed to create data.csv: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Distance / ETA calculations
    // -------------------------------------------------------------------------

    /**
     * Returns the percentage of the distance to the next checkpoint that has
     * been completed, as a formatted string.
     */
    public static String getPercentOffset(int checkpointInterval) {
        double distToNext = getDistanceToNextMultiple(checkpointInterval);
        double pct = ((double) checkpointInterval - distToNext) / (double) checkpointInterval * 100.0;
        return String.format("%.2f", pct);
    }

    /**
     * Calculates how many blocks remain until the next multiple of
     * {@code interval} along the current highway axis.
     */
    public static int calculateDistanceToNextCheckpoint() {
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        assert MeteorClient.mc.player != null;
        int pos;
        if (dir == WorldUtils.Direction8.NORTH || dir == WorldUtils.Direction8.SOUTH) {
            pos = Math.abs(MeteorClient.mc.player.getZ()) - Math.abs(HighwayState.getInstance().getStartZ());
        } else if (dir == WorldUtils.Direction8.EAST || dir == WorldUtils.Direction8.WEST) {
            pos = Math.abs(MeteorClient.mc.player.getX()) - Math.abs(HighwayState.getInstance().getStartX());
        } else {
            // Diagonal highways use X distance
            pos = Math.abs(Math.abs(MeteorClient.mc.player.getX()) - Math.abs(HighwayState.getInstance().getStartX()));
        }
        return Math.abs(pos);
    }

    /**
     * Returns the distance traveled stat (updates if HighwayBuilder is active).
     */
    public static int getDistanceToCheckpoint() {
        if (((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) {
            distanceTraveled = calculateDistanceToNextCheckpoint();
        }
        return distanceTraveled;
    }

    /** Returns current obsidian placement rate in blocks/second, or -1 if inactive. */
    public static double getBlocksPlacedPerSecond() {
        if (!((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) return -1.0;
        double placed = HighwayState.getInstance().getSessionObsidianPlacedCount();
        double ticks  = HighwayState.getInstance().getTicksActive();
        if (placed == 0.0 || ticks == 0.0) return 0.0;
        return placed / (ticks / 20.0);
    }

    /** Returns combined block mining rate (obsidian + other) in blocks/second, or -1 if inactive. */
    public static double getBlocksMinedPerSecond() {
        if (!((HighwayBuilder) Modules.get().get(HighwayBuilder.class)).isActive()) return -1.0;
        HighwayState state = HighwayState.getInstance();
        double mined = state.getSessionObsidianMinedCount() + state.getSessionLavaBucketCount() + state.getSessionMiscMinedCount();
        double ticks  = state.getTicksActive();
        if (mined == 0.0 || ticks == 0.0) return 0.0;
        return mined / (ticks / 20.0);
    }

    /** Count of total obsidian blocks in player inventory (echest stacks + loose obsidian). */
    public static int countObsidianBlocks() {
        return InventoryManager.countItem(Items.ENDER_CHEST) * 8 + InventoryManager.countItem(Items.OBSIDIAN);
    }

    /** Returns {@code value / (ticks / 20)}, i.e. a per-second rate. Returns 0 if ticks is 0. */
    public static double calcRate(double value, double ticks) {
        double seconds = ticks / 20.0;
        if (seconds <= 0.0) return 0.0;
        return value / seconds;
    }

    /**
     * Estimates ticks until {@code distance} is covered at current rate.
     * Returns -1 if waiting for data, -2 if distance < 2 blocks.
     */
    public static double calcETA(int distance) {
        HighwayState state = HighwayState.getInstance();
        if (getDistanceToCheckpoint() < 1 || state.getTicksActive() < 1) return 0.0;
        long traveled = getDistanceToCheckpoint();
        long ticks    = state.getTicksActive();
        long speed    = getDistanceToCheckpoint() / (ticks / 20);
        if (traveled <= 1L) return -2.0;
        if (ticks <= 200) return -1.0;
        return (double) getDistanceToNextMultiple(distance) / calcRate(traveled, ticks);
    }

    /**
     * Returns how many blocks remain until the player's coordinate is a multiple
     * of {@code interval} along the current highway axis.
     */
    public static int getDistanceToNextMultiple(int interval) {
        assert MeteorClient.mc.player != null;
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        int coord;
        switch (dir) {
            case NORTH: case SOUTH:
                coord = MeteorClient.mc.player.getZ(); break;
            case EAST: case WEST:
            case NORTH_EAST: case NORTH_WEST:
            case SOUTH_EAST: case SOUTH_WEST:
            default:
                coord = MeteorClient.mc.player.getX(); break;
        }
        int floor = Math.floorDiv(coord, interval) * interval;
        int next  = floor + interval;
        // For negative-direction travel, the "next" multiple is the floor, not floor+interval
        if (dir == WorldUtils.Direction8.SOUTH || dir == WorldUtils.Direction8.WEST
                || dir == WorldUtils.Direction8.SOUTH_EAST || dir == WorldUtils.Direction8.SOUTH_WEST) {
            next = floor;
        }
        return Math.abs(next - coord);
    }

    // -------------------------------------------------------------------------
    // Formatting helpers
    // -------------------------------------------------------------------------

    public static String formatBlocksPerSecond(double bps) {
        if (bps == -1.0) return "Waiting...";
        return String.format("%.2f blocks / s", bps);
    }

    public static String formatBlocksPerHour(double bps) {
        if (bps == -1.0) return "Waiting...";
        return String.format("%.2f blocks / h", bps * 3600.0);
    }

    public static String formatDistancePerSecond(int distance) {
        if (distance < 1) return "Waiting...";
        return String.format("%.2f blocks / s",
            (float) calcRate(distance, HighwayState.getInstance().getTicksActive()));
    }

    public static String formatDistancePerHour(int distance) {
        if (distance < 1) return "Waiting...";
        return String.format("%.2f blocks / h",
            (float) calcRate(distance, HighwayState.getInstance().getTicksActive()) * 3600.0f);
    }

    public static String formatETABlocksPerSecond(double bps) {
        if (bps == -1.0) return "Waiting...";
        return String.format("%.2f blocks / s", bps);
    }

    /** Format how long until current obsidian supply runs out, as HH:MM:SS. */
    public static String formatObsidianETA() {
        if (getBlocksPlacedPerSecond() == -1.0) return "Waiting...";
        long secs  = (long) ((double) countObsidianBlocks() / getBlocksPlacedPerSecond());
        return String.format("%d:%02d:%02d", secs / 3600, secs % 3600 / 60, secs % 60);
    }

    /** Format ETA until the next multiple of {@code interval} blocks, as HH:MM:SS. */
    public static String formatCheckpointETA(int interval) {
        if (getBlocksPlacedPerSecond() == -1.0) return "Waiting...";
        long secs = (long) ((double) getDistanceToNextMultiple(interval)
            / calcRate(getDistanceToCheckpoint(), HighwayState.getInstance().getTicksActive()));
        return String.format("%d:%02d:%02d", secs / 3600, secs % 3600 / 60, secs % 60);
    }

    /** Format a tick count as HH:MM:SS (20 ticks = 1 second). */
    public static String formatTicksAsTime(long ticks) {
        if (ticks == -1L) return "Calculating...";
        if (ticks == -2L) return "Waiting...";
        long secs = ticks / 20L;
        return String.format("%d:%02d:%02d", secs / 3600, secs % 3600 / 60, secs % 60);
    }
}
