// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import java.util.Comparator;
import java.util.List;
import musheor.compat.XearoHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.feature.waypoint.eta.WaypointEtaManager;

public class XearoHelperImpl
implements XearoHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private XearoHelper.WaypointData lastEtaData = null;
    private Waypoint lastEtaWaypoint = null;

    private WaypointSet currentSet() {
        MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) {
            return null;
        }
        MinimapWorld minimapWorld = minimapSession.getWorldManager().getCurrentWorld();
        if (minimapWorld == null) {
            return null;
        }
        return minimapWorld.getCurrentWaypointSet();
    }

    private void refresh() {
        if (XearoHelperImpl.mc.player == null || XearoHelperImpl.mc.world == null) {
            return;
        }
        SupportMods.xaeroMinimap.requestWaypointsRefresh();
    }

    private static XearoHelper.WaypointData wrap(Waypoint waypoint) {
        return new XearoHelper.WaypointData(waypoint.getX(), waypoint.getY(), waypoint.getZ(), waypoint.getName(), waypoint.isTemporary(), waypoint.getCreatedAt());
    }

    private static WaypointColor xaeroColor(XearoHelper.WaypointColorHint waypointColorHint) {
        return switch (waypointColorHint) {
            default -> throw new MatchException(null, null);
            case XearoHelper.WaypointColorHint.WHITE -> WaypointColor.WHITE;
            case XearoHelper.WaypointColorHint.RED -> WaypointColor.RED;
            case XearoHelper.WaypointColorHint.GOLD -> WaypointColor.GOLD;
            case XearoHelper.WaypointColorHint.BLUE -> WaypointColor.BLUE;
        };
    }

    @Override
    public List<XearoHelper.WaypointData> getWaypoints(boolean bl) {
        WaypointSet waypointSet = this.currentSet();
        if (waypointSet == null) {
            return List.of();
        }
        List<Waypoint> list = (List<Waypoint>)waypointSet.getWaypoints();
        List<Waypoint> list2 = bl ? list.stream().filter(Waypoint::isTemporary).toList() : list;
        return list2.stream().map(XearoHelperImpl::wrap).toList();
    }

    @Override
    public XearoHelper.WaypointData getOldestWaypoint(boolean bl) {
        return this.getWaypoints(bl).stream().min(Comparator.comparingLong(XearoHelper.WaypointData::createdAt)).orElse(null);
    }

    @Override
    public Object getCurrentWaypointSetHandle() {
        return this.currentSet();
    }

    @Override
    public void setWaypointSet(String string) {
        MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) {
            return;
        }
        MinimapWorld minimapWorld = minimapSession.getWorldManager().getCurrentWorld();
        if (minimapWorld == null) {
            return;
        }
        if (minimapWorld.getWaypointSet(string) == null) {
            minimapWorld.addWaypointSet(string);
        }
        minimapWorld.setCurrentWaypointSetId(string);
    }

    @Override
    public void restoreWaypointSet(Object object) {
        if (object == null) {
            return;
        }
        WaypointSet waypointSet = (WaypointSet)object;
        MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) {
            return;
        }
        MinimapWorld minimapWorld = minimapSession.getWorldManager().getCurrentWorld();
        if (minimapWorld == null) {
            return;
        }
        minimapWorld.setCurrentWaypointSetId(waypointSet.getName());
    }

    @Override
    public void addWaypointToCurrent(String string, String string2, Vec3d Vec3d2, XearoHelper.WaypointColorHint waypointColorHint) {
        this.addWaypointToCurrent(string, string2, Vec3d2, waypointColorHint, false);
    }

    @Override
    public void addWaypointToCurrent(String string, String string2, Vec3d Vec3d2, XearoHelper.WaypointColorHint waypointColorHint, boolean bl) {
        WaypointSet waypointSet = this.currentSet();
        if (waypointSet == null) {
            return;
        }
        Waypoint waypoint = new Waypoint((int)Vec3d2.x, (int)Vec3d2.y, (int)Vec3d2.z, string, string2, XearoHelperImpl.xaeroColor(waypointColorHint));
        waypoint.setTemporary(bl);
        waypointSet.add(waypoint);
        this.refresh();
    }

    @Override
    public void deleteCurrentWaypoint(XearoHelper.WaypointData waypointData) {
        WaypointSet waypointSet = this.currentSet();
        if (waypointSet == null) {
            return;
        }
        List list = (List)waypointSet.getWaypoints();
        list.stream().filter(waypoint -> waypoint.getX() == waypointData.x() && waypoint.getZ() == waypointData.z() && waypoint.getCreatedAt() == waypointData.createdAt()).findFirst().ifPresent(waypoint -> {
            waypointSet.remove(waypoint);
            this.refresh();
        });
    }

    @Override
    public void deleteAllTempWaypoints() {
        WaypointSet waypointSet = this.currentSet();
        if (waypointSet == null) {
            return;
        }
        List list = (List)waypointSet.getWaypoints();
        list.removeIf(Waypoint::isTemporary);
        this.refresh();
    }

    @Override
    public void updateWaypointSettings() {
        this.refresh();
    }

    @Override
    public void drawLinesOnMap(List<XearoHelper.LineData> list, int n) {
        this.drawLinesOnMap("Path", list, n);
    }

    @Override
    public void drawLinesOnMap(String string, List<XearoHelper.LineData> list, int n4) {
        if (!FabricLoader.getInstance().isModLoaded("xaeroplus")) {
            return;
        }
        List<Line> list2 = list.stream().map(lineData -> new Line(lineData.x1(), lineData.z1(), lineData.x2(), lineData.z2())).toList();
        Globals.drawManager.registry().unregister(string);
        Globals.drawManager.registry().register(DrawFeatureFactory.lines((String)string, (n, n2, n3, ignored) -> list2, () -> n4, () -> 1.0f, (int)50));
    }

    @Override
    public void clearLinesOnMap() {
        this.clearLinesOnMap("Path");
    }

    @Override
    public void clearLinesOnMap(String string) {
        if (!FabricLoader.getInstance().isModLoaded("xaeroplus")) {
            return;
        }
        Globals.drawManager.registry().unregister(string);
    }

    @Override
    public double distanceToWaypoint(XearoHelper.WaypointData waypointData) {
        if (XearoHelperImpl.mc.player == null) {
            return Double.MAX_VALUE;
        }
        double d = XearoHelperImpl.mc.player.getX() - (double)waypointData.x();
        double d2 = XearoHelperImpl.mc.player.getZ() - (double)waypointData.z();
        return Math.sqrt(d * d + d2 * d2);
    }

    @Override
    public String getEtaSuffix(XearoHelper.WaypointData waypointData) {
        if (!FabricLoader.getInstance().isModLoaded("xaeroplus")) {
            return null;
        }
        if (!waypointData.equals(this.lastEtaData)) {
            this.lastEtaData = waypointData;
            this.lastEtaWaypoint = new Waypoint(waypointData.x(), waypointData.y(), waypointData.z(), waypointData.name(), "W", WaypointColor.WHITE);
        }
        return WaypointEtaManager.INSTANCE.getEtaTextSuffix(this.lastEtaWaypoint);
    }
}

