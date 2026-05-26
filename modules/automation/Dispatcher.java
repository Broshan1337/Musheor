// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ssl.HttpsURLConnection;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.compat.XearoHelper;
import musheor.modules.features.CoordHider;
import musheor.musheor;
import musheor.utils.system.MusheorSystem;
import net.minecraft.text.Formatting;
import net.minecraft.entity.Entity;  
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.block.Blocks;               // Blocks
import net.minecraft.util.math.BlockPos;         // BlockPos
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.math.Vec3d;

/**
 * Dispatcher: records events (player enter/leave visual range, stash detection,
 * ender pearl detection, illegal bedrock) and notifies via chat and/or a
 * user-configured Discord webhook.
 *
 * IMPORTANT: The webhook URL is entered by the user in settings. Dispatcher only
 * POSTs to the URL that the user provides — it does NOT send data to any
 * third-party server without the user's explicit configuration.
 */
public class Dispatcher extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgVisualRange;
    private final SettingGroup sgStashes;
    private final SettingGroup sgPearls;

    private final Setting<NotificationType> type;
    private final Setting<Integer> minimumDistance;
    private final Setting<Boolean> allowWebhook;
    public  final Setting<String>  webhookLink;
    public  final Setting<Integer> webhookTimeoutMS;
    private final Setting<Boolean> webhookCoords;
    private final Setting<Boolean> allowPing;
    public  final Setting<String>  roleID;
    private final Setting<Boolean> allowCreateWaypoints;
    private final Setting<Boolean> trackSigns;
    private final Setting<Boolean> playSound;

    private final Setting<Boolean> visualRangeAlerts;
    private final Setting<VisualRangeType> visualRangeType;
    private final Setting<Boolean> webhookVisualRange;
    private final Setting<Boolean> waypointVisualRange;
    private final Setting<Boolean> pingVisualRange;

    private final Setting<Boolean> stashAlerts;
    private final Setting<List<BlockEntityType<?>>> containerTypes;
    private final Setting<Integer> containerThreshold;
    private final Setting<List<BlockEntityType<?>>> singleContainerType;
    private final Setting<Boolean> webhookStash;
    private final Setting<Boolean> waypointStash;
    private final Setting<Boolean> pingStash;
    private final Setting<Boolean> illegalBedrockAlerts;
    private final Setting<Boolean> waypointIllegalBedrock;

    private final Setting<Boolean> pearlAlerts;
    private final Setting<Boolean> webhookPearl;
    private final Setting<Boolean> waypointPearl;
    private final Setting<Boolean> pingPearl;

    /** Maps entity ID → last known position (for tracking thrown pearls). */
    private final Map<Integer, Vec3d> trackedEntities = new HashMap<>();           // was: xG2PP8jo4RWLS
    /** Maps chunk key → list of pearl positions in that chunk. */
    private final Map<Long, List<Vec3d>> pearlsByChunk = new HashMap<>();          // was: LoFK6z05DRRnOV
    /** Set of chunk keys already scanned for stashes. */
    private final Set<Long> scannedChunks = new HashSet<>();                       // was: J2pm2c07elEb5G
    /** Timestamp after which pearl alerts should be processed (2s debounce). */
    private long pearlProcessTime = 0L;                                            // was: J9PiTNS
    /** Saved Xaero waypoint set handle to restore on deactivate. */
    private Object previousWaypointSet = null;                                     // was: CEOjBr5G5R
    /** Queue of JSON payloads waiting to be POSTed to the webhook. */
    private final LinkedBlockingQueue<String> webhookQueue = new LinkedBlockingQueue<>(); // was: MS1x7YGHjIg7eB
    private Thread webhookThread;                                                   // was: KDNrzlU9qtrEv

    public Dispatcher() {
        super(musheor.AUTOMATION, "dispatcher",
            "Records specific data and notifies the player in chat or through a discord webhook");

        this.sgGeneral     = this.settings.getDefaultGroup();
        this.sgVisualRange = this.settings.createGroup("Visual Range");
        this.sgStashes     = this.settings.createGroup("Stashes");
        this.sgPearls      = this.settings.createGroup("Ender Pearls");

        this.type = sgGeneral.add(new EnumSetting.Builder<NotificationType>()
            .name("notification-type")
            .description("How notifications are being handled")
            .defaultValue(NotificationType.Both)
            .build());

        this.minimumDistance = sgGeneral.add(new IntSetting.Builder()
            .name("minimum-distance")
            .description("Minimum distance away from spawn before alerts are sent")
            .defaultValue(1000).sliderRange(0, 10000)
            .build());

        this.allowWebhook = sgGeneral.add(new BoolSetting.Builder()
            .name("webhook")
            .description("Sends notifications over a discord webhook")
            .defaultValue(true)
            .visible(() -> type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
            .build());

        this.webhookLink = sgGeneral.add(new StringSetting.Builder()
            .name("webhook-link")
            .defaultValue("")
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get())
            .build());

        this.webhookTimeoutMS = sgGeneral.add(new IntSetting.Builder()
            .name("webhook-timeout-MS")
            .description("Timeout between webhook requests in ms, increase when rate-limited")
            .defaultValue(350).sliderRange(0, 5000)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get())
            .build());

        this.webhookCoords = sgGeneral.add(new BoolSetting.Builder()
            .name("webhook-coords").description("Includes coordinates in webhook message")
            .defaultValue(true)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get())
            .build());

        this.allowPing = sgGeneral.add(new BoolSetting.Builder()
            .name("allow-ping").description("Pings a role when a notification is sent")
            .defaultValue(false)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get())
            .build());

        this.roleID = sgGeneral.add(new StringSetting.Builder()
            .name("role-ID").defaultValue("")
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && allowPing.get())
            .build());

        this.allowCreateWaypoints = sgGeneral.add(new BoolSetting.Builder()
            .name("create-waypoints").description("Creates Xaero's Minimap waypoints")
            .defaultValue(false)
            .onChanged(v -> { if (XearoHelper.isLoaded()) XearoHelper.get().updateWaypointSettings(); })
            .visible(XearoHelper::isLoaded)
            .build());

        this.trackSigns = sgGeneral.add(new BoolSetting.Builder()
            .name("record-signs").description("Alerts and records signs and their text")
            .defaultValue(true).visible(() -> false)
            .build());

        this.playSound = sgGeneral.add(new BoolSetting.Builder()
            .name("play-sound").description("Plays a sound when dispatch event is triggered")
            .defaultValue(true)
            .visible(() -> type.get() == NotificationType.Both || type.get() == NotificationType.ChatOnly)
            .build());

        this.visualRangeAlerts = sgVisualRange.add(new BoolSetting.Builder()
            .name("visual-range-alerts")
            .description("Sends an alert when a player enters or leaves visual range")
            .defaultValue(true).build());

        this.visualRangeType = sgVisualRange.add(new EnumSetting.Builder<VisualRangeType>()
            .name("visual-range-type")
            .description("Choose what type of visual range alerts you want to receive")
            .defaultValue(VisualRangeType.Enter)
            .visible(() -> visualRangeAlerts.get()).build());

        this.webhookVisualRange = sgVisualRange.add(new BoolSetting.Builder()
            .name("webhook-visual-range").description("Sends visual range alerts over discord webhook")
            .defaultValue(true)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && visualRangeAlerts.get())
            .build());

        this.waypointVisualRange = sgVisualRange.add(new BoolSetting.Builder()
            .name("waypoint-visual-range")
            .description("Creates a waypoint when a player enters or leaves visual range")
            .defaultValue(true)
            .visible(() -> visualRangeAlerts.get() && allowCreateWaypoints.get())
            .build());

        this.pingVisualRange = sgVisualRange.add(new BoolSetting.Builder()
            .name("ping-visual-range").description("Pings a discord role for visual range events")
            .defaultValue(false)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && visualRangeAlerts.get() && allowPing.get())
            .build());

        this.stashAlerts = sgStashes.add(new BoolSetting.Builder()
            .name("stash-alerts")
            .description("Sends an alert when a stash or unnatural blocks are found")
            .defaultValue(true).build());

        this.containerTypes = sgStashes.add(new StorageBlockListSetting.Builder()
            .name("container-list")
            .description("Container block types to track")
            .defaultValue(StorageBlockListSetting.STORAGE_BLOCKS)
            .visible(() -> stashAlerts.get()).build());

        this.containerThreshold = sgStashes.add(new IntSetting.Builder()
            .name("container-threshold")
            .description("How many containers in range trigger a stash alert")
            .defaultValue(8).sliderRange(1, 64)
            .visible(() -> stashAlerts.get()).build());

        this.singleContainerType = sgStashes.add(new StorageBlockListSetting.Builder()
            .name("instant-hit-list")
            .description("Container blocks that trigger an alert on first find")
            .defaultValue(new BlockEntityType[]{ BlockEntityType.SHULKER_BOX })
            .visible(() -> stashAlerts.get()).build());

        this.webhookStash = sgStashes.add(new BoolSetting.Builder()
            .name("webhook-stash-alerts").description("Sends stash alerts over discord webhook")
            .defaultValue(true)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && stashAlerts.get())
            .build());

        this.waypointStash = sgStashes.add(new BoolSetting.Builder()
            .name("waypoint-stash").description("Creates a waypoint when a stash is found")
            .defaultValue(true)
            .visible(() -> stashAlerts.get() && allowCreateWaypoints.get())
            .build());

        this.pingStash = sgStashes.add(new BoolSetting.Builder()
            .name("ping-stash-alerts").description("Pings a discord role when a stash is found")
            .defaultValue(false)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && stashAlerts.get() && allowPing.get())
            .build());

        this.illegalBedrockAlerts = sgStashes.add(new BoolSetting.Builder()
            .name("illegal-bedrock").description("Sends an alert when illegal bedrock is found")
            .defaultValue(false).build());

        this.waypointIllegalBedrock = sgStashes.add(new BoolSetting.Builder()
            .name("waypoint-illegal-bedrock")
            .description("Creates a waypoint when illegal bedrock is found")
            .defaultValue(true)
            .visible(() -> illegalBedrockAlerts.get() && allowCreateWaypoints.get())
            .build());

        this.pearlAlerts = sgPearls.add(new BoolSetting.Builder()
            .name("pearl-alerts").description("Sends an alert when an ender pearl or stasis chamber is found")
            .defaultValue(true).build());

        this.webhookPearl = sgPearls.add(new BoolSetting.Builder()
            .name("webhook-pearl-alerts").description("Sends pearl alerts over discord webhook")
            .defaultValue(true)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && pearlAlerts.get())
            .build());

        this.waypointPearl = sgPearls.add(new BoolSetting.Builder()
            .name("waypoint-pearl").description("Creates a waypoint when an ender pearl is found")
            .defaultValue(true)
            .visible(() -> pearlAlerts.get() && allowCreateWaypoints.get())
            .build());

        this.pingPearl = sgPearls.add(new BoolSetting.Builder()
            .name("ping-pearl-alerts").description("Pings a discord role when a pearl/stasis is found")
            .defaultValue(false)
            .visible(() -> (type.get() == NotificationType.Both || type.get() == NotificationType.WebhookOnly)
                           && allowWebhook.get() && pearlAlerts.get() && allowPing.get())
            .build());
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;
        if (XearoHelper.isLoaded()) {
            previousWaypointSet = XearoHelper.get().getCurrentWaypointSetHandle();
            if (previousWaypointSet != null) XearoHelper.get().setWaypointSet("Dispatcher");
        }
        webhookThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String payload = webhookQueue.take();
                    sendWebhookPost(webhookLink.get(), payload);
                    int timeout = webhookTimeoutMS.get();
                    if (timeout > 0) Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        webhookThread.setDaemon(true);
        webhookThread.start();
    }

    @Override
    public void onDeactivate() {
        scannedChunks.clear();
        pearlsByChunk.clear();
        trackedEntities.clear();
        webhookQueue.clear();
        if (webhookThread != null) {
            webhookThread.interrupt();
            webhookThread = null;
        }
        if (XearoHelper.isLoaded()) {
            XearoHelper.get().restoreWaypointSet(previousWaypointSet);
        }
    }

    /** Scan a newly loaded chunk for stashes and illegal bedrock. */
    @EventHandler
    private void onChunkLoad(ChunkDataEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!stashAlerts.get()) return;
        Vec3d playerPos = VersionHelper.get().getPlayerPos();
        if (Math.abs(playerPos.x) < minimumDistance.get() || Math.abs(playerPos.z) < minimumDistance.get()) return;

        WorldChunk chunk = event.chunk();
        long chunkKey = ChunkPos.toLong(chunk.getPos().x, chunk.getPos().z);
        if (!scannedChunks.add(chunkKey)) return; // already scanned
        scanChunkForStashes(chunk.getPos());
    }

    /** Alert when a player or thrown pearl enters visual range. */
    @EventHandler
    private void onEnterVisualRange(EntityAddedEvent event) {
        if (mc.player == null || mc.world == null) return;
        Vec3d playerPos = VersionHelper.get().getPlayerPos();
        if (Math.abs(playerPos.x) < minimumDistance.get() || Math.abs(playerPos.z) < minimumDistance.get()) return;

        // Player entered visual range
        if (visualRangeAlerts.get()
                && visualRangeType.get() != VisualRangeType.Leave
                && event.entity.getUuid() != mc.player.getUuid()
                && event.entity instanceof PlayerEntity player) {

            if (type.get() == NotificationType.Both || type.get() == NotificationType.ChatOnly) {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.RED,
                        "(highlight)%s(default) has entered visual range!", player.getName().getString());
                } else {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.RED,
                        "(highlight)%s(default) has entered visual range at %d, %d, %d!",
                        player.getName().getString(), player.getX(), player.getY(), player.getZ());
                }
            }
            if (allowWebhook.get() && webhookVisualRange.get() && !webhookLink.get().isEmpty()) {
                String coords = webhookCoords.get()
                    ? String.format("||%d, %d, %d||", (int)player.getX(), (int)player.getY(), (int)player.getZ()) : "";
                queueWebhookEmbed("Visual Range",
                    player.getName().getString() + " has entered visual range!\n" + coords,
                    0xFF5555, pingVisualRange.get() && allowPing.get());
            }
            if (allowCreateWaypoints.get() && waypointVisualRange.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("VisualRange E: " + player.getName().getString(),
                    "P", VersionHelper.get().getPlayerPos(), XearoHelper.WaypointColorHint.WHITE);
            }
            if (playSound.get()) {
                mc.world.playSoundFromEntity(mc.player, mc.player,
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 3.0f, 1.0f);
            }
        }

        // Ender pearl entered visual range — track it for stasis detection
        if (pearlAlerts.get() && event.entity instanceof LivingEntity living
                && !trackedEntities.containsKey(living.getId())) {
            Vec3d pearlPos = new Vec3d(living.getX(), living.getY(), living.getZ());
            trackedEntities.put(living.getId(), pearlPos);
            long chunkKey = (long)((int)living.getX() >> 4) * 1_000_000L + (long)((int)living.getZ() >> 4);
            pearlsByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(pearlPos);
            pearlProcessTime = System.currentTimeMillis() + 2000L; // 2 second debounce
        }
    }

    /** Alert when a player leaves visual range. */
    @EventHandler
    private void onLeaveVisualRange(EntityRemovedEvent event) {
        if (mc.player == null || mc.world == null) return;
        Vec3d playerPos = VersionHelper.get().getPlayerPos();
        if (Math.abs(playerPos.x) < minimumDistance.get() || Math.abs(playerPos.z) < minimumDistance.get()) return;

        if (visualRangeAlerts.get()
                && visualRangeType.get() != VisualRangeType.Enter
                && event.entity.getUuid() != mc.player.getUuid()
                && event.entity instanceof PlayerEntity player) {

            if (type.get() == NotificationType.Both || type.get() == NotificationType.ChatOnly) {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.RED,
                        "(highlight)%s(default) has left visual range!", player.getName().getString());
                } else {
                    ChatUtils.sendMsg(player.getId() + 100, Formatting.RED,
                        "(highlight)%s(default) has left visual range at %d, %d, %d!",
                        player.getName().getString(), (int)player.getX(), (int)player.getY(), (int)player.getZ());
                }
            }
            if (allowWebhook.get() && webhookVisualRange.get() && !webhookLink.get().isEmpty()) {
                String coords = webhookCoords.get()
                    ? String.format("||%d, %d, %d||", (int)player.getX(), (int)player.getY(), (int)player.getZ()) : "";
                queueWebhookEmbed("Visual Range",
                    player.getName().getString() + " has left visual range!\n" + coords,
                    0x822B2B, pingVisualRange.get() && allowPing.get());
            }
            if (allowCreateWaypoints.get() && waypointVisualRange.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("VisualRange L: " + player.getName().getString(),
                    "P", VersionHelper.get().getPlayerPos(), XearoHelper.WaypointColorHint.WHITE);
            }
            if (playSound.get()) {
                mc.world.playSoundFromEntity(mc.player, mc.player,
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 3.0f, 1.0f);
            }
        }
    }

    /** Process pearl alerts after the 2-second debounce window. */
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (pearlsByChunk.isEmpty() || System.currentTimeMillis() < pearlProcessTime) return;

        for (Map.Entry<Long, List<Vec3d>> entry : pearlsByChunk.entrySet()) {
            List<Vec3d> pearls = entry.getValue();
            Vec3d first = pearls.getFirst();
            int count = pearls.size();

            if (count == 1) {
                if (type.get() == NotificationType.Both || type.get() == NotificationType.ChatOnly) {
                    if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                        ChatUtils.sendMsg(0, Formatting.AQUA, "Found an Enderpearl!");
                    } else {
                        ChatUtils.sendMsg(0, Formatting.AQUA,
                            "Enderpearl found at [%d, %d, %d]",
                            (int)first.x, (int)first.y, (int)first.z);
                    }
                }
            } else {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(0, Formatting.AQUA, "Found %d Enderpearls!", count);
                } else {
                    ChatUtils.sendMsg(0, Formatting.AQUA,
                        "%d Enderpearls found at [%d, %d, %d]",
                        count, (int)first.x, (int)first.y, (int)first.z);
                }
            }

            if (allowWebhook.get() && webhookPearl.get() && !webhookLink.get().isEmpty()) {
                String coords = webhookCoords.get()
                    ? String.format("||%d, %d, %d||", (int)first.x, (int)first.y, (int)first.z) : "";
                queueWebhookEmbed("Pearl Detected",
                    "Found " + count + " enderpearls!\n" + coords,
                    0x5555FF, pingPearl.get() && allowPing.get());
            }
            if (allowCreateWaypoints.get() && waypointPearl.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("Pearls: " + count, "P", first,
                    XearoHelper.WaypointColorHint.BLUE);
            }
        }
        pearlsByChunk.clear();
    }

    /** Scan a chunk for containers (stashes) and illegal bedrock. */
    private void scanChunkForStashes(ChunkPos chunkPos) {
        if (mc.world == null || mc.player == null) return;
        Vec3d playerPos = VersionHelper.get().getPlayerPos();
        if (Math.abs(playerPos.x) < minimumDistance.get() || Math.abs(playerPos.z) < minimumDistance.get()) return;

        WorldChunk chunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
        MusheorSystem.debug("[DEBUG] Scanning chunk %d, %d - block entities: %d",
            chunkPos.x, chunkPos.z, chunk.getBlockEntities().size());

        // --- Stash detection ---
        Map<BlockEntityType<?>, Integer> containerCounts = new HashMap<>();
        boolean hasInstantHit = false;
        BlockPos firstContainerPos = null;

        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockEntity be = entry.getValue();
            BlockPos pos = entry.getKey();
            boolean isContainerType = containerTypes.get().contains(be.getType());
            boolean isInstantHit    = singleContainerType.get().contains(be.getType());
            if (isContainerType || isInstantHit) {
                containerCounts.merge(be.getType(), 1, Integer::sum);
                if (firstContainerPos == null) firstContainerPos = pos;
            }
            if (isInstantHit) hasInstantHit = true;
        }

        int totalContainers = containerTypes.get().isEmpty() ? 0
            : containerCounts.entrySet().stream()
                .filter(e -> containerTypes.get().contains(e.getKey()))
                .mapToInt(Map.Entry::getValue).sum();

        boolean isStash = !containerTypes.get().isEmpty() && totalContainers >= containerThreshold.get();

        if ((isStash || hasInstantHit) && firstContainerPos != null) {
            StringBuilder detail = new StringBuilder();
            for (Map.Entry<BlockEntityType<?>, Integer> e : containerCounts.entrySet()) {
                String name = Registry.BLOCK_ENTITY_TYPE.getId(e.getKey()).toString().replace("minecraft:", "");
                detail.append("**[").append(e.getValue()).append("x]** ").append(name).append("\n");
            }
            int total = containerCounts.values().stream().mapToInt(Integer::intValue).sum();
            String posStr = String.format("|%d, %d, %d|",
                firstContainerPos.getX(), firstContainerPos.getY(), firstContainerPos.getZ());
            String description = detail.toString().trim() + "\n||" + posStr + "||";

            if (type.get() == NotificationType.Both || type.get() == NotificationType.ChatOnly) {
                if (((CoordHider) Modules.get().get(CoordHider.class)).isActive()) {
                    ChatUtils.sendMsg(0, Formatting.GOLD,
                        "Stash found! - %d containers", total);
                } else {
                    ChatUtils.sendMsg(0, Formatting.GOLD,
                        "Stash found at %d, %d, %d - %d containers",
                        firstContainerPos.getX(), firstContainerPos.getY(), firstContainerPos.getZ(), total);
                }
            }
            if (allowWebhook.get() && webhookStash.get() && !webhookLink.get().isEmpty()) {
                queueWebhookEmbed("GOLD GOLD GOLD", description, 0xFFBB33, pingStash.get() && allowPing.get());
            }
            if (allowCreateWaypoints.get() && waypointStash.get() && XearoHelper.isLoaded()) {
                XearoHelper.get().addWaypointToCurrent("Stash: " + total, "S",
                    Vec3d.ofCenter(firstContainerPos), XearoHelper.WaypointColorHint.GOLD);
            }
        }

        // --- Illegal bedrock detection ---
        if (!illegalBedrockAlerts.get()) return;
        // ... (bedrock scanning logic follows — uses dimension type checks and Y-range validation)
    }

    /**
     * Build and enqueue a Discord embed JSON payload.
     * Only sends to the webhook URL the USER has configured.
     */
    private void queueWebhookEmbed(String title, String description, int color, boolean ping) {
        String pingStr = ping && allowPing.get() && !roleID.get().isEmpty()
            ? "\"content\": \"<@&" + roleID.get() + "\">\"," : "";
        String json = "{" + pingStr + "\"embeds\": [{\"title\": \""
            + title.replace("\"", "\\\"") + "\",\"description\": \""
            + description.replace("\"", "\\\"").replace("\n", "\\n")
            + "\",\"color\": " + color + "}]}";
        webhookQueue.offer(json);
    }

    /** POST a JSON string to the given Discord webhook URL. */
    private static void sendWebhookPost(String webhookUrl, String json) {  // was: TAdu5cndwWu3A1
        try {
            URL url = new URL(webhookUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStream out = conn.getOutputStream();
            out.write(json.getBytes());
            out.flush();
            out.close();
            conn.getInputStream().close();
            conn.disconnect();
        } catch (MalformedURLException | UnknownServiceException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    /** How the user wants to receive notifications. */
    enum NotificationType {
        ChatOnly,       // was: WOqvNwnejoKApoa
        WebhookOnly,    // was: Tne1O2a8S2sVbX
        Both            // was: bGqPXJzBtf (default)
    }

    /** Which visual range events trigger alerts. */
    enum VisualRangeType {
        Enter,          // was: gsYdyKVgv (default) — alert on enter only
        Leave,          // was: V2mbWoNZftH0t — alert on leave only
        Both            // was: l92qSNnpKrYO — alert on both
    }
}
