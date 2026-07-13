// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ssl.HttpsURLConnection;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.compat.XearoHelper;
import musheor.modules.features.CoordHider;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

/**
 * "dispatcher" — passive base-finding / awareness scanner. Alerts (in chat, via the user's
 * own Discord webhook, and/or as Xaero waypoints) on: players entering/leaving visual
 * range, stashes (clusters of storage block entities), thrown ender pearls / stasis
 * chambers, and illegally-placed bedrock. Webhook posts run on a background daemon thread
 * with a configurable per-request delay. Only active beyond {@code minimum-distance} from
 * spawn. (Security-relevant: the webhook is user-supplied; audited benign.)
 */
public class Dispatcher extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();            // was: SOYyh5IPg26f7F
    private final SettingGroup sgVisualRange = this.settings.createGroup("Visual Range"); // was: rKbT3Ifwo
    private final SettingGroup sgStashes = this.settings.createGroup("Stashes");       // was: r7hOYIKN2
    private final SettingGroup sgPearls = this.settings.createGroup("Ender Pearls");   // was: oZHMlTL

    private final Setting<NotificationType> notificationType = sgGeneral.add(new EnumSetting.Builder<NotificationType>() // was: xQr5FhbwpQPWgIQ
        .name("notification-type").description("How notifications are being handled").defaultValue(NotificationType.BOTH).build());
    private final Setting<Integer> minimumDistance = sgGeneral.add(new meteordevelopment.meteorclient.settings.IntSetting.Builder() // was: OMMZL1F3q
        .name("minimum-distance").description("Minimum distance away from spawn before alerts are sent").defaultValue(1000).sliderRange(0, 10000).build());
    private final Setting<Boolean> webhook = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("webhook").description("Sends notifications over a discord webhook").defaultValue(true)
        .visible(() -> notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK).build());
    public final Setting<String> webhookLink = sgGeneral.add(new meteordevelopment.meteorclient.settings.StringSetting.Builder() // was: FvaNWO
        .name("webhook-link").defaultValue("")
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get()).build());
    public final Setting<Integer> webhookTimeoutMs = sgGeneral.add(new meteordevelopment.meteorclient.settings.IntSetting.Builder() // was: Q90GLXQ0Pef
        .name("webhook-timeout-MS").description("Timeout for webhook requests in ms, set to 0 to disable, increase when getting rate-limited").defaultValue(350).sliderRange(0, 5000)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get()).build());
    private final Setting<Boolean> webhookCoords = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: krxNb5lcQuWA
        .name("webhook-coords").description("Includes coordinates in webhook message").defaultValue(true)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get()).build());
    private final Setting<Boolean> allowPing = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: nt0HZnvBBp
        .name("allow-ping").description("Pings a role when a notification is sent").defaultValue(false)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get()).build());
    public final Setting<String> roleId = sgGeneral.add(new meteordevelopment.meteorclient.settings.StringSetting.Builder() // was: psJq59YIbp3Z
        .name("role-ID").defaultValue("")
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && allowPing.get()).build());
    private final Setting<Boolean> createWaypoints = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: amz3UB1vE
        .name("create-waypoints").description("Creates xaeros minimap waypoints").defaultValue(false)
        .onChanged(value -> { if (XearoHelper.isLoaded()) XearoHelper.get().updateWaypointSettings(); }).visible(XearoHelper::isLoaded).build());
    private final Setting<Boolean> recordSigns = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: sBBIyQG5NWq0K
        .name("record-signs").description("Alerts and records signs and their text").defaultValue(true).visible(() -> false).build());
    private final Setting<Boolean> playSound = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: sZkZ1izAy
        .name("play-sound").description("Plays a sound when dispatch event is triggered").defaultValue(true)
        .visible(() -> notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.CHAT).build());

    private final Setting<Boolean> visualRangeAlerts = sgVisualRange.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: QYKUhjp
        .name("visual-range-alerts").description("Sends an alert when a player enters or leaves visual range").defaultValue(true).build());
    private final Setting<VisualRangeType> visualRangeType = sgVisualRange.add(new EnumSetting.Builder<VisualRangeType>() // was: NIz4xic3Js9
        .name("visual-range-type").description("Choose what type of visual range alerts you want to receive").defaultValue(VisualRangeType.ENTER).visible(visualRangeAlerts::get).build());
    private final Setting<Boolean> webhookVisualRange = sgVisualRange.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: u1WFwbQRSKa
        .name("webhook-visual-range").description("Sends visual range alerts over discord webhook").defaultValue(true)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && visualRangeAlerts.get()).build());
    private final Setting<Boolean> waypointVisualRange = sgVisualRange.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: LGDfbZq
        .name("waypoint-visual-range").description("Creates a xaero waypoint when a player enters or leaves visual range").defaultValue(true)
        .visible(() -> visualRangeAlerts.get() && createWaypoints.get()).build());
    private final Setting<Boolean> pingVisualRange = sgVisualRange.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: to3T8DJCDVX8po
        .name("ping-visual-range").description("Pings a discord role when a player enters or leaves visual range").defaultValue(false)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && visualRangeAlerts.get() && allowPing.get()).build());

    private final Setting<Boolean> stashAlerts = sgStashes.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: Sd3jEwKuGABy
        .name("stash-alerts").description("Sends an alert when a stash or unnatural blocks are found").defaultValue(true).build());
    private final Setting<List<BlockEntityType<?>>> containerList = sgStashes.add(new StorageBlockListSetting.Builder() // was: kJfFkD47Vh
        .name("container-list").description("List of container blocks to track or search for").defaultValue(StorageBlockListSetting.STORAGE_BLOCKS).visible(stashAlerts::get).build());
    private final Setting<Integer> containerThreshold = sgStashes.add(new meteordevelopment.meteorclient.settings.IntSetting.Builder() // was: ubHptFBRn5bO
        .name("container-threshold").description("How many containers need to be in visual range before an alert is sent").defaultValue(8).sliderRange(1, 64).visible(stashAlerts::get).build());
    private final Setting<List<BlockEntityType<?>>> instantHitList = sgStashes.add(new StorageBlockListSetting.Builder() // was: apOpfoOHr3fJVwT
        .name("instant-hit-list").description("List of container blocks to track or search for, when a single one is found it will alert")
        .defaultValue(BlockEntityType.SHULKER_BOX).visible(stashAlerts::get).build());
    private final Setting<Boolean> webhookStashAlerts = sgStashes.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: hq1pN0qY
        .name("webhook-stash-alerts").description("Sends visual range alerts over discord webhook").defaultValue(true)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && stashAlerts.get()).build());
    private final Setting<Boolean> waypointStash = sgStashes.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: ptxWcpd1WV763T5
        .name("waypoint-stash").description("Creates a xaero waypoint when a stash is found").defaultValue(true)
        .visible(() -> stashAlerts.get() && createWaypoints.get()).build());
    private final Setting<Boolean> pingStashAlerts = sgStashes.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: DnAk86nuI
        .name("ping-stash-alerts").description("Pings a discord role when a player enters or leaves visual range").defaultValue(false)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && stashAlerts.get() && allowPing.get()).build());
    private final Setting<Boolean> illegalBedrock = sgStashes.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: LlN8EpIZKbk
        .name("illegal-bedrock").description("Sends an alert when illegal bedrock is found").defaultValue(false).build());
    private final Setting<Boolean> waypointIllegalBedrock = sgStashes.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: pgjj9cLYUTE5g
        .name("waypoint-illegal-bedrock").description("Creates a xaero waypoint when illegal bedrock is found").defaultValue(true)
        .visible(() -> illegalBedrock.get() && createWaypoints.get()).build());

    private final Setting<Boolean> pearlAlerts = sgPearls.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: IeStEJRJ9eb3l
        .name("pearl-alerts").description("Sends an alert when an enderpearl or stasis-chamber is found").defaultValue(true).build());
    private final Setting<Boolean> webhookPearlAlerts = sgPearls.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: sFazojak6ig8QgGq
        .name("webhook-pearl-alerts").description("Sends a pearl alerts over discord webhook").defaultValue(true)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && pearlAlerts.get()).build());
    private final Setting<Boolean> waypointPearl = sgPearls.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: ewq603nIlCd9Gbu
        .name("waypoint-pearl").description("Creates a xaero waypoint when an enderpearl is found").defaultValue(true)
        .visible(() -> pearlAlerts.get() && createWaypoints.get()).build());
    private final Setting<Boolean> pingPearlAlerts = sgPearls.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder() // was: ExGM8SQ9Qni
        .name("ping-pearl-alerts").description("Pings a discord role when a pearl or stasis chamber is found").defaultValue(false)
        .visible(() -> (notificationType.get() == NotificationType.BOTH || notificationType.get() == NotificationType.WEBHOOK) && webhook.get() && pearlAlerts.get() && allowPing.get()).build());

    private final Map<Integer, Vec3d> knownPearls = new HashMap<>();          // was: yS4isXf3gAzs (entityId -> pos)
    private final Map<Long, List<Vec3d>> pearlsByChunk = new HashMap<>();     // was: eC9HV2bWGX (pending pearl alerts by chunk)
    private final Set<Long> scannedChunks = new HashSet<>();                  // was: w9spWeVv3AvI
    private long pearlAlertTime = 0L;                                         // was: HvulV2j9tKjohNgh (debounce time to flush pearl alerts)
    private Object previousWaypointSet = null;                                // was: Qco5OF (Xaero handle)
    private final LinkedBlockingQueue<String> webhookQueue = new LinkedBlockingQueue<>(); // was: cgqo7J5iR6
    private Thread webhookThread;                                             // was: u2kcN4vsQhS46w5s

    public Dispatcher() {
        super(musheor.AUTOMATION, "dispatcher", "Records specific data and notifies the player in chat or through a discord webhook");
    }

    @Override
    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) return;
        if (XearoHelper.isLoaded()) {
            this.previousWaypointSet = XearoHelper.get().getCurrentWaypointSetHandle();
            if (this.previousWaypointSet != null) XearoHelper.get().setWaypointSet("Dispatcher");
        }

        this.webhookThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String json = this.webhookQueue.take();
                    sendWebhook(this.webhookLink.get(), json);
                    int timeout = this.webhookTimeoutMs.get();
                    if (timeout > 0) Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.webhookThread.setDaemon(true);
        this.webhookThread.start();
    }

    @Override
    public void onDeactivate() {
        this.scannedChunks.clear();
        this.pearlsByChunk.clear();
        this.knownPearls.clear();
        this.webhookQueue.clear();
        if (this.webhookThread != null) {
            this.webhookThread.interrupt();
            this.webhookThread = null;
        }
        if (XearoHelper.isLoaded()) XearoHelper.get().restoreWaypointSet(this.previousWaypointSet);
    }

    /** True if the player is within {@code minimum-distance} of spawn on either axis (alerts suppressed). */
    private boolean nearSpawn() {
        Vec3d playerPos = VersionHelper.get().getPlayerPos();
        return Math.abs(playerPos.x) < this.minimumDistance.get() || Math.abs(playerPos.z) < this.minimumDistance.get();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) { // was: FvaNWO(ChunkDataEvent)
        if (this.mc.player == null || this.mc.world == null || !this.stashAlerts.get() || this.nearSpawn()) return;
        WorldChunk chunk = event.chunk();
        long chunkKey = ChunkPos.toLong(chunk.getPos().x, chunk.getPos().z);
        if (this.scannedChunks.add(chunkKey)) {
            this.scanChunk(chunk.getPos());
        }
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) { // was: FvaNWO(EntityAddedEvent)
        if (this.mc.player == null || this.mc.world == null || this.nearSpawn()) return;

        if (this.visualRangeAlerts.get()
            && this.visualRangeType.get() != VisualRangeType.LEAVE
            && event.entity.getUuid() != this.mc.player.getUuid()
            && event.entity instanceof PlayerEntity player) {
            if (this.notificationType.get() == NotificationType.BOTH || this.notificationType.get() == NotificationType.CHAT) {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has entered visual range!", player.getName().getString());
                } else {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has entered visual range at %d, %d, %d!",
                        player.getName().getString(), player.getBlockX(), player.getBlockY(), player.getBlockZ());
                }
            }
            if (this.webhook.get() && this.webhookVisualRange.get() && !this.webhookLink.get().isEmpty()) {
                String pos = this.webhookCoords.get() ? String.format("||%d, %d, %d||", player.getBlockX(), player.getBlockY(), player.getBlockZ()) : "";
                this.queueWebhook("Visual Range", player.getName().getString() + " has entered visual range!\n" + pos, 16733525,
                    this.pingVisualRange.get() && this.allowPing.get());
            }
            if (this.createWaypoints.get() && this.waypointVisualRange.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("VisualRange E: " + player.getName().getString(), "P", VersionHelper.get().getPlayerPos(), XearoHelper.WaypointColorHint.WHITE);
            }
            if (this.playSound.get()) {
                this.mc.world.playSound(this.mc.player, this.mc.player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 3.0F, 1.0F);
            }
        }

        if (this.pearlAlerts.get() && event.entity instanceof EnderPearlEntity pearlEntity && !this.knownPearls.containsKey(pearlEntity.getId())) {
            Vec3d pos = new Vec3d(pearlEntity.getX(), pearlEntity.getY(), pearlEntity.getZ());
            this.knownPearls.put(pearlEntity.getId(), pos);
            long chunkKey = ((int) pearlEntity.getX() >> 4) * 1000000L + ((int) pearlEntity.getZ() >> 4);
            this.pearlsByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(pos);
            this.pearlAlertTime = System.currentTimeMillis() + 2000L;
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) { // was: FvaNWO(EntityRemovedEvent)
        if (this.mc.player == null || this.mc.world == null || this.nearSpawn()) return;
        if (this.visualRangeAlerts.get()
            && this.visualRangeType.get() != VisualRangeType.ENTER
            && event.entity.getUuid() != this.mc.player.getUuid()
            && event.entity instanceof PlayerEntity player) {
            if (this.notificationType.get() == NotificationType.BOTH || this.notificationType.get() == NotificationType.CHAT) {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has left visual range!", player.getName().getString());
                } else {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has left visual range at %d, %d, %d!",
                        player.getName().getString(), player.getBlockX(), player.getBlockY(), player.getBlockZ());
                }
            }
            if (this.webhook.get() && this.webhookVisualRange.get() && !this.webhookLink.get().isEmpty()) {
                String pos = this.webhookCoords.get() ? String.format("||%d, %d, %d||", player.getBlockX(), player.getBlockY(), player.getBlockZ()) : "";
                this.queueWebhook("Visual Range", player.getName().getString() + " has left visual range!\n" + pos, 8530731,
                    this.pingVisualRange.get() && this.allowPing.get());
            }
            if (this.createWaypoints.get() && this.waypointVisualRange.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("VisualRange L: " + player.getName().getString(), "P", VersionHelper.get().getPlayerPos(), XearoHelper.WaypointColorHint.WHITE);
            }
            if (this.playSound.get()) {
                this.mc.world.playSound(this.mc.player, this.mc.player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 3.0F, 1.0F);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.pearlsByChunk.isEmpty() || System.currentTimeMillis() < this.pearlAlertTime) return;
        for (Map.Entry<Long, List<Vec3d>> entry : this.pearlsByChunk.entrySet()) {
            List<Vec3d> pearls = entry.getValue();
            Vec3d first = pearls.getFirst();
            int count = pearls.size();
            if (count == 1) {
                if (this.notificationType.get() == NotificationType.BOTH || this.notificationType.get() == NotificationType.CHAT) {
                    if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                        ChatUtils.sendMsg(0, Formatting.BLUE, "Found an Enderpearl!");
                    } else {
                        ChatUtils.sendMsg(0, Formatting.BLUE, "Enderpearl %sfound at [%s%d, %d, %d%s]",
                            Formatting.WHITE, Formatting.GRAY, (int) first.x, (int) first.y, (int) first.z, Formatting.WHITE);
                    }
                }
            } else if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                ChatUtils.sendMsg(0, Formatting.BLUE, "Found %d Enderpearls!", count);
            } else {
                ChatUtils.sendMsg(0, Formatting.BLUE, "%d Enderpearls %sfound at [%s%d, %d, %d%s]",
                    count, Formatting.WHITE, Formatting.GRAY, (int) first.x, (int) first.y, (int) first.z, Formatting.WHITE);
            }

            if (this.webhook.get() && this.webhookPearlAlerts.get() && !this.webhookLink.get().isEmpty()) {
                String pos = this.webhookCoords.get() ? String.format("||%d, %d, %d||", (int) first.x, (int) first.y, (int) first.z) : "";
                this.queueWebhook("Pearl Detected", "Found " + count + " enderpearls!\n" + pos, 5592575, this.pingPearlAlerts.get() && this.allowPing.get());
            }
            if (this.createWaypoints.get() && this.waypointPearl.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("Pearls: " + count, "P", first, XearoHelper.WaypointColorHint.BLUE);
            }
        }
        this.pearlsByChunk.clear();
    }

    /** Scans a chunk for stashes (container clusters), instant-hit containers, and illegal bedrock. */
    private void scanChunk(ChunkPos chunkPos) { // was: FvaNWO(ChunkPos)
        if (this.mc.world == null || this.mc.player == null || this.nearSpawn()) return;
        WorldChunk chunk = this.mc.world.getChunk(chunkPos.x, chunkPos.z);
        MusheorSystem.debug("[DEBUG] Scanning chunk " + chunkPos.x + ", " + chunkPos.z + " - block entities: " + chunk.getBlockEntities().size());

        Map<BlockEntityType<?>, Integer> foundContainers = new HashMap<>();
        boolean foundSingleAlert = false;
        BlockPos firstContainerPos = null;

        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockEntity be = entry.getValue();
            BlockPos pos = entry.getKey();
            boolean isThresholdContainer = this.containerList.get().contains(be.getType());
            boolean isSingleContainer = this.instantHitList.get().contains(be.getType());
            if (isThresholdContainer || isSingleContainer) {
                foundContainers.merge(be.getType(), 1, Integer::sum);
                if (firstContainerPos == null) firstContainerPos = pos;
            }
            if (isSingleContainer) foundSingleAlert = true;
        }

        int totalThresholdContainers = this.containerList.get().isEmpty() ? 0
            : foundContainers.entrySet().stream().filter(e -> this.containerList.get().contains(e.getKey())).mapToInt(Map.Entry::getValue).sum();
        boolean thresholdMet = !this.containerList.get().isEmpty() && totalThresholdContainers >= this.containerThreshold.get();
        if ((thresholdMet || foundSingleAlert) && firstContainerPos != null) {
            StringBuilder summary = new StringBuilder();
            for (Map.Entry<BlockEntityType<?>, Integer> entry : foundContainers.entrySet()) {
                String name = Registries.BLOCK_ENTITY_TYPE.getId(entry.getKey()).toString().replace("minecraft:", "");
                summary.append("**[").append(entry.getValue()).append("x]** ").append(name).append("\n");
            }
            int totalContainers = foundContainers.values().stream().mapToInt(Integer::intValue).sum();
            String blockCoords = String.format("|%d, %d, %d|", firstContainerPos.getX(), firstContainerPos.getY(), firstContainerPos.getZ());
            String description = summary.toString().trim() + "\n||" + blockCoords + "||";
            if (this.notificationType.get() == NotificationType.BOTH || this.notificationType.get() == NotificationType.CHAT) {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(0, Formatting.WHITE, "Stash found! %s- %s%d containers", Formatting.WHITE, Formatting.AQUA, totalContainers);
                } else {
                    ChatUtils.sendMsg(0, Formatting.WHITE, "Stash found at %s%d, %d, %d %s- %s%d containers",
                        Formatting.GRAY, firstContainerPos.getX(), firstContainerPos.getY(), firstContainerPos.getZ(), Formatting.WHITE, Formatting.AQUA, totalContainers);
                }
            }
            if (this.webhook.get() && this.webhookStashAlerts.get() && !this.webhookLink.get().isEmpty()) {
                this.queueWebhook("GOLD GOLD GOLD", description, 16759603, this.pingStashAlerts.get() && this.allowPing.get());
            }
            if (this.createWaypoints.get() && this.waypointStash.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("Stash: " + totalContainers, "S", Vec3d.ofCenter(firstContainerPos), XearoHelper.WaypointColorHint.GOLD);
            }
        }

        if (this.illegalBedrock.get()) {
            boolean isOverworld = this.mc.world.getRegistryKey() == World.OVERWORLD;
            boolean isNether = this.mc.world.getRegistryKey() == World.NETHER;
            boolean isEnd = this.mc.world.getRegistryKey() == World.END;
            if (isEnd) {
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof EndGatewayBlockEntity) return; // natural end-gateway bedrock, skip
                }
            }

            BlockPos illegalPos = null;
            outer:
            for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
                for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                    int minY = this.mc.world.getBottomY();
                    int maxY = this.mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
                    for (int y = minY; y < maxY; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (this.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
                            boolean illegal = isOverworld && y > 5 || isNether && y > 5 && y < 122 || isEnd;
                            if (illegal) {
                                illegalPos = pos;
                                break outer;
                            }
                        }
                    }
                }
            }

            if (illegalPos != null) {
                String pos = this.webhookCoords.get() ? String.format("|%d, %d, %d|", illegalPos.getX(), illegalPos.getY(), illegalPos.getZ()) : "";
                if (this.notificationType.get() == NotificationType.BOTH || this.notificationType.get() == NotificationType.CHAT) {
                    if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                        ChatUtils.sendMsg(0, Formatting.RED, "Illegal bedrock found!");
                    } else {
                        ChatUtils.sendMsg(0, Formatting.RED, "Illegal bedrock found at %s%d, %d, %d",
                            Formatting.GRAY, illegalPos.getX(), illegalPos.getY(), illegalPos.getZ());
                    }
                }
                if (this.webhook.get() && this.webhookStashAlerts.get() && !this.webhookLink.get().isEmpty()) {
                    this.queueWebhook("Illegal Bedrock", "**Found illegal bedrock**!!\n" + pos, 16711935, this.pingStashAlerts.get() && this.allowPing.get());
                }
                if (this.createWaypoints.get() && this.waypointIllegalBedrock.get() && XearoHelper.isLoaded()) {
                    XearoHelper.get().addWaypointToCurrent("Illegal Bedrock", "B", Vec3d.ofCenter(illegalPos), XearoHelper.WaypointColorHint.RED);
                }
            }
        }
    }

    /** Builds the Discord embed JSON and enqueues it for the background sender thread. */
    private void queueWebhook(String title, String description, int color, boolean pingRole) { // was: FvaNWO(String,String,int,boolean)
        String content = pingRole && this.allowPing.get() && !this.roleId.get().isEmpty()
            ? "\"content\": \"<@&" + this.roleId.get() + ">\","
            : "";
        String json = "{" + content
            + "\"embeds\": [{\"title\": \"" + title.replace("\"", "\\\"")
            + "\",\"description\": \"" + description.replace("\"", "\\\"").replace("\n", "\\n")
            + "\",\"color\": " + color + "}]}";
        this.webhookQueue.offer(json);
    }

    /** POSTs the embed JSON to the configured Discord webhook URL. */
    private static void sendWebhook(String webhookURL, String json) { // was: FvaNWO(String,String)
        try {
            URL url = new URL(webhookURL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Mozilla");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream stream = connection.getOutputStream();
            stream.write(json.getBytes());
            stream.flush();
            stream.close();
            connection.getInputStream().close();
            connection.disconnect();
        } catch (MalformedURLException | UnknownServiceException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Which channels notifications are sent through. */ // was: enum NotificationType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum NotificationType { CHAT, WEBHOOK, BOTH }

    /** Which visual-range transitions trigger alerts. */ // was: enum VisualRangeType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum VisualRangeType { ENTER, LEAVE, BOTH }
}
