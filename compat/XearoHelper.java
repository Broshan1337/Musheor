// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Interface and members were already readable.
package musheor.compat;

import java.util.List;
import net.minecraft.util.math.Vec3d;

/**
 * Optional-dependency bridge to Xaero's Minimap/World Map (and XaeroPlus). A
 * {@code XearoHelperImpl} is installed (via reflection) only when Xaero is present;
 * {@link #isLoaded()} gates every call. Exposes temp-waypoint management, map line
 * drawing, and ETA lookups used by the navigation modules.
 */
public interface XearoHelper {
    List<WaypointData> getWaypoints(boolean tempOnly);

    WaypointData getOldestWaypoint(boolean tempOnly);

    Object getCurrentWaypointSetHandle();

    void setWaypointSet(String name);

    void restoreWaypointSet(Object handle);

    void addWaypointToCurrent(String name, String symbol, Vec3d pos, WaypointColorHint color);

    void addWaypointToCurrent(String name, String symbol, Vec3d pos, WaypointColorHint color, boolean temporary);

    void deleteCurrentWaypoint(WaypointData waypoint);

    void deleteAllTempWaypoints();

    void updateWaypointSettings();

    void drawLinesOnMap(List<LineData> lines, int color);

    void drawLinesOnMap(String id, List<LineData> lines, int color);

    void clearLinesOnMap();

    void clearLinesOnMap(String id);

    double distanceToWaypoint(WaypointData waypoint);

    String getEtaSuffix(WaypointData waypoint);

    static boolean isLoaded() {
        return XearoHelperHolder.INSTANCE != null;
    }

    static XearoHelper get() {
        return XearoHelperHolder.INSTANCE;
    }

    static void setInstance(XearoHelper instance) {
        XearoHelperHolder.INSTANCE = instance;
    }

    /** A line segment (x1,z1)->(x2,z2) drawn on the map. */
    record LineData(int x1, int z1, int x2, int z2) { }

    /** Hint colour for a created waypoint. */
    enum WaypointColorHint { WHITE, RED, GOLD, BLUE }

    /** A Xaero waypoint's position, name, temp flag and creation time. */
    record WaypointData(int x, int y, int z, String name, boolean temporary, long createdAt) { }

    class XearoHelperHolder {
        static XearoHelper INSTANCE = null;
    }
}
