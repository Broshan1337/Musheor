// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable.
package musheor.utils.system;

import com.google.gson.Gson;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Tracks which players in the tab list are Musheor community members ("kek"/"post" tiers),
 * so their cosmetic capes and glow can be rendered. The UUID lists are GETed once from
 * {@code https://mutuals.musheck.dev} (a plain read-only fetch; audited benign) and the set
 * of currently-visible mutuals is refreshed each tick. Purely cosmetic — no gameplay effect.
 */
public class MutualManager {
    public static final MutualManager mutualManager = new MutualManager();
    public static final Identifier PLUS_CAPE = Identifier.of("template", "musheorpluscape.png");
    private static final String UUID_LIST_URL = "https://mutuals.musheck.dev";
    private static final Set<UUID> POST = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> KEK = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> mutual = Collections.synchronizedSet(new HashSet<>());
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private volatile boolean loaded = false;

    private MutualManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.fetchRemoteUUIDs();
    }

    /** Asynchronously fetches the kek/post UUID lists from the remote endpoint. */
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
                this.parseAndFill(json, "kek", KEK);
                this.parseAndFill(json, "post", POST);
                this.loaded = true;
                System.out.println("[MutualManager] Loaded " + KEK.size() + " KEK, " + POST.size() + " POST from remote.");
            } catch (Exception e) {
                System.err.println("[MutualManager] Error fetching remote UUID list, using fallback.");
                e.printStackTrace();
            }
        });
    }

    /** Fills {@code target} with the UUIDs under {@code key}, skipping malformed entries. */
    private void parseAndFill(JsonObject root, String key, Set<UUID> target) {
        if (!root.has(key)) return;
        target.clear();
        for (JsonElement element : root.getAsJsonArray(key)) {
            try {
                target.add(UUID.fromString(element.getAsString()));
            } catch (IllegalArgumentException e) {
                System.err.println("[MutualManager] Skipping invalid UUID: " + element.getAsString());
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (this.mc.player == null || this.mc.getNetworkHandler() == null) return;
        this.mutual.clear();
        for (PlayerListEntry entry : this.mc.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile() != null) {
                UUID uuid = VersionHelper.get().getUUID(entry);
                if (POST.contains(uuid) || KEK.contains(uuid)) this.mutual.add(uuid);
            }
        }
    }

    public static MutualManager getInstance() {
        return mutualManager;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean hasCape(UUID uuid) {
        return this.mutual.contains(uuid);
    }

    public boolean hasCape(PlayerEntity player) {
        return player != null && this.hasCape(player.getUuid());
    }

    public boolean isPost(PlayerEntity player) {
        return POST.contains(player.getUuid());
    }

    public boolean isKek(PlayerEntity player) {
        return KEK.contains(player.getUuid());
    }

    public boolean shouldGlow(UUID uuid) {
        return this.mutual.contains(uuid);
    }

    public boolean shouldGlow(PlayerEntity player) {
        return player != null && this.shouldGlow(player.getUuid());
    }
}
