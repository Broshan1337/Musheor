// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.system;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import net.minecraft.client.MinecraftClient;            // MinecraftClient
import net.minecraft.client.network.PlayerListEntry;   // class_640
import net.minecraft.entity.player.PlayerEntity;       // class_1657
import net.minecraft.util.Identifier;                  // class_2960

/**
 * Fetches a remote list of player UUIDs from https://mutuals.musheck.dev.
 * Players on the list get a cosmetic cape ("musheorpluscape.png") and a glow
 * effect rendered client-side. No data is ever sent to the server — this is
 * purely a cosmetic/social feature.
 *
 * The remote JSON has the shape:
 *   { "kek": ["uuid", ...], "post": ["uuid", ...] }
 *
 * "post" and "kek" are two tiers of recognized players.
 */
public class MutualManager {
    public static final MutualManager mutualManager = new MutualManager();

    /** Identifier for the plus-tier cosmetic cape texture. */
    public static final Identifier PLUS_CAPE = Identifier.of("template", "musheorpluscape.png");

    private static final String UUID_LIST_URL = "https://mutuals.musheck.dev";

    /** UUIDs of "post"-tier players (fetched remotely). */
    private static final Set<UUID> POST_UUIDS = Collections.synchronizedSet(new HashSet<>());
    /** UUIDs of "kek"-tier players (fetched remotely). */
    private static final Set<UUID> KEK_UUIDS = Collections.synchronizedSet(new HashSet<>());

    /** UUIDs of players currently visible in the server tab-list who are on either list. */
    private final Set<UUID> mutual = Collections.synchronizedSet(new HashSet<>());

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private volatile boolean loaded = false;

    private MutualManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.fetchRemoteUUIDs();
    }

    /** Async fetch of the UUID lists from the remote server. */
    public void fetchRemoteUUIDs() {
        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(UUID_LIST_URL).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "MusheorAddon");

                if (conn.getResponseCode() != 200) {
                    System.err.println("[MutualManager] Failed to fetch UUID list: HTTP " + conn.getResponseCode());
                    return;
                }

                InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                reader.close();

                parseAndFill(json, "kek", KEK_UUIDS);
                parseAndFill(json, "post", POST_UUIDS);
                this.loaded = true;

                System.out.println("[MutualManager] Loaded " + KEK_UUIDS.size() + " KEK, " + POST_UUIDS.size() + " POST from remote.");
            } catch (Exception e) {
                System.err.println("[MutualManager] Error fetching remote UUID list, using fallback.");
                e.printStackTrace();
            }
        });
    }

    /** Parse a named UUID array from JSON into the given set. */
    private void parseAndFill(JsonObject json, String key, Set<UUID> target) {
        if (!json.has(key)) return;
        target.clear();
        for (JsonElement el : json.getAsJsonArray(key)) {
            try {
                target.add(UUID.fromString(el.getAsString()));
            } catch (IllegalArgumentException e) {
                System.err.println("[MutualManager] Skipping invalid UUID: " + el.getAsString());
            }
        }
    }

    /**
     * Every tick: rebuild the set of currently-visible mutual players by cross-
     * referencing the server tab-list against the remotely-fetched UUID sets.
     */
    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        mutual.clear();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile() == null) continue;
            UUID uuid = VersionHelper.get().getUUID(entry);
            if (POST_UUIDS.contains(uuid) || KEK_UUIDS.contains(uuid)) {
                mutual.add(uuid);
            }
        }
    }

    public static MutualManager getInstance() {
        return mutualManager;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /** Returns true if the given UUID has a cape (is on either list and currently online). */
    public boolean hasCape(UUID uuid) {
        return mutual.contains(uuid);
    }

    public boolean hasCape(PlayerEntity player) {
        return player != null && hasCape(player.getUuid());
    }

    /** Returns true if the player is on the "post" tier. */
    public boolean isPost(PlayerEntity player) {
        return POST_UUIDS.contains(player.getUuid());
    }

    /** Returns true if the player is on the "kek" tier. */
    public boolean isKek(PlayerEntity player) {
        return KEK_UUIDS.contains(player.getUuid());
    }

    /** Returns true if the given UUID should have a glow effect rendered. */
    public boolean shouldGlow(UUID uuid) {
        return mutual.contains(uuid);
    }

    public boolean shouldGlow(PlayerEntity player) {
        return player != null && shouldGlow(player.getUuid());
    }
}
