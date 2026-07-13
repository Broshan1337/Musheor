// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class/members readable; only Minecraft class refs were intermediary.
package musheor.compat;

import java.util.Comparator;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
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

/**
 * The active {@link XearoHelper} — bridges directly to Xaero's Minimap (and XaeroPlus for
 * map lines / ETA). Installed only when Xaero is present.
 */
public class XearoHelperImpl implements XearoHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private WaypointData lastEtaData = null;
    private Waypoint lastEtaWaypoint = null;

    private WaypointSet currentSet() {
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return null;
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        return world == null ? null : world.getCurrentWaypointSet();
    }

    private void refresh() {
        if (mc.player != null && mc.world != null) {
            SupportMods.xaeroMinimap.requestWaypointsRefresh();
        }
    }

    private static WaypointData wrap(Waypoint wp) {
        return new WaypointData(wp.getX(), wp.getY(), wp.getZ(), wp.getName(), wp.isTemporary(), wp.getCreatedAt());
    }

    private static WaypointColor xaeroColor(WaypointColorHint hint) {
        return switch (hint) {
            case WHITE -> WaypointColor.WHITE;
            case RED -> WaypointColor.RED;
            case GOLD -> WaypointColor.GOLD;
            case BLUE -> WaypointColor.BLUE;
        };
    }

    @Override
    public List<WaypointData> getWaypoints(boolean tempOnly) {
        WaypointSet set = this.currentSet();
        if (set == null) return List.of();
        List<Waypoint> list = (List<Waypoint>) set.getWaypoints();
        List<Waypoint> filtered = tempOnly ? list.stream().filter(Waypoint::isTemporary).toList() : list;
        return filtered.stream().map(XearoHelperImpl::wrap).toList();
    }

    @Override
    public WaypointData getOldestWaypoint(boolean tempOnly) {
        return this.getWaypoints(tempOnly).stream().min(Comparator.comparingLong(WaypointData::createdAt)).orElse(null);
    }

    @Override
    public Object getCurrentWaypointSetHandle() {
        return this.currentSet();
    }

    @Override
    public void setWaypointSet(String name) {
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return;
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) return;
        if (world.getWaypointSet(name) == null) world.addWaypointSet(name);
        world.setCurrentWaypointSetId(name);
    }

    @Override
    public void restoreWaypointSet(Object handle) {
        if (handle == null) return;
        WaypointSet set = (WaypointSet) handle;
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return;
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world != null) world.setCurrentWaypointSetId(set.getName());
    }

    @Override
    public void addWaypointToCurrent(String name, String initial, Vec3d pos, WaypointColorHint colorHint) {
        this.addWaypointToCurrent(name, initial, pos, colorHint, false);
    }

    @Override
    public void addWaypointToCurrent(String name, String initial, Vec3d pos, WaypointColorHint colorHint, boolean temporary) {
        WaypointSet set = this.currentSet();
        if (set == null) return;
        Waypoint waypoint = new Waypoint((int) pos.x, (int) pos.y, (int) pos.z, name, initial, xaeroColor(colorHint));
        waypoint.setTemporary(temporary);
        set.add(waypoint);
        this.refresh();
    }

    @Override
    public void deleteCurrentWaypoint(WaypointData data) {
        WaypointSet set = this.currentSet();
        if (set == null) return;
        List<Waypoint> list = (List<Waypoint>) set.getWaypoints();
        list.stream().filter(wp -> wp.getX() == data.x() && wp.getZ() == data.z() && wp.getCreatedAt() == data.createdAt()).findFirst().ifPresent(wp -> {
            set.remove(wp);
            this.refresh();
        });
    }

    @Override
    public void deleteAllTempWaypoints() {
        WaypointSet set = this.currentSet();
        if (set == null) return;
        ((List<Waypoint>) set.getWaypoints()).removeIf(Waypoint::isTemporary);
        this.refresh();
    }

    @Override
    public void updateWaypointSettings() {
        this.refresh();
    }

    @Override
    public void drawLinesOnMap(List<LineData> lines, int color) {
        this.drawLinesOnMap("Path", lines, color);
    }

    @Override
    public void drawLinesOnMap(String key, List<LineData> lines, int color) {
        if (!FabricLoader.getInstance().isModLoaded("xaeroplus")) return;
        List<Line> xaeroLines = lines.stream().map(l -> new Line(l.x1(), l.z1(), l.x2(), l.z2())).toList();
        Globals.drawManager.registry().unregister(key);
        Globals.drawManager.registry().register(DrawFeatureFactory.lines(key, (cx, cz, zoom, dim) -> xaeroLines, () -> color, () -> 1.0F, 50));
    }

    @Override
    public void clearLinesOnMap() {
        this.clearLinesOnMap("Path");
    }

    @Override
    public void clearLinesOnMap(String key) {
        if (FabricLoader.getInstance().isModLoaded("xaeroplus")) {
            Globals.drawManager.registry().unregister(key);
        }
    }

    @Override
    public double distanceToWaypoint(WaypointData waypoint) {
        if (mc.player == null) return Double.MAX_VALUE;
        double dx = mc.player.getX() - waypoint.x();
        double dz = mc.player.getZ() - waypoint.z();
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public String getEtaSuffix(WaypointData waypoint) {
        if (!FabricLoader.getInstance().isModLoaded("xaeroplus")) return null;
        if (!waypoint.equals(this.lastEtaData)) {
            this.lastEtaData = waypoint;
            this.lastEtaWaypoint = new Waypoint(waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), "W", WaypointColor.WHITE);
        }
        return WaypointEtaManager.INSTANCE.getEtaTextSuffix(this.lastEtaWaypoint);
    }
}
