// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import java.util.List;
import net.minecraft.Vec3d;

public interface XearoHelper {
    public List<WaypointData> getWaypoints(boolean var1);

    public WaypointData getOldestWaypoint(boolean var1);

    public Object getCurrentWaypointSetHandle();

    public void setWaypointSet(String var1);

    public void restoreWaypointSet(Object var1);

    public void addWaypointToCurrent(String var1, String var2, Vec3d var3, WaypointColorHint var4);

    public void addWaypointToCurrent(String var1, String var2, Vec3d var3, WaypointColorHint var4, boolean var5);

    public void deleteCurrentWaypoint(WaypointData var1);

    public void deleteAllTempWaypoints();

    public void updateWaypointSettings();

    public void drawLinesOnMap(List<LineData> var1, int var2);

    public void drawLinesOnMap(String var1, List<LineData> var2, int var3);

    public void clearLinesOnMap();

    public void clearLinesOnMap(String var1);

    public double distanceToWaypoint(WaypointData var1);

    public String getEtaSuffix(WaypointData var1);

    public static boolean isLoaded() {
        return XearoHelperHolder.INSTANCE != null;
    }

    public static XearoHelper get() {
        return XearoHelperHolder.INSTANCE;
    }

    public static void setInstance(XearoHelper xearoHelper) {
        XearoHelperHolder.INSTANCE = xearoHelper;
    }

    public static class XearoHelperHolder {
        static XearoHelper INSTANCE = null;
    }

    public static final class WaypointColorHint
    extends Enum<WaypointColorHint> {
        public static final /* enum */ WaypointColorHint WHITE = new WaypointColorHint();
        public static final /* enum */ WaypointColorHint RED = new WaypointColorHint();
        public static final /* enum */ WaypointColorHint GOLD = new WaypointColorHint();
        public static final /* enum */ WaypointColorHint BLUE = new WaypointColorHint();
        private static final /* synthetic */ WaypointColorHint[] $VALUES;

        public static WaypointColorHint[] values() {
            return (WaypointColorHint[])$VALUES.clone();
        }

        public static WaypointColorHint valueOf(String string) {
            return Enum.valueOf(WaypointColorHint.class, string);
        }

        private static /* synthetic */ WaypointColorHint[] $values() {
            return new WaypointColorHint[]{WHITE, RED, GOLD, BLUE};
        }

        static {
            $VALUES = WaypointColorHint.$values();
        }
    }

    public record LineData(int x1, int z1, int x2, int z2) {
    }

    public record WaypointData(int x, int y, int z, String name, boolean temporary, long createdAt) {
    }
}

