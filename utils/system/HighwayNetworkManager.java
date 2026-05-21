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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Downloads highway configuration data from https://highways.musheck.dev.
 *
 * The remote JSON maps server names (e.g. "2b2t") to objects containing:
 *   - "ring_distances"    : double[] — distances where ring roads cross the highway
 *   - "diamond_distances" : double[] — distances for diamond-pattern waypoints
 *
 * Falls back to hardcoded 2b2t values if the fetch fails.
 * Only performs outbound GET requests — no data is ever uploaded.
 */
public class HighwayNetworkManager {
    public static final HighwayNetworkManager INSTANCE = new HighwayNetworkManager();

    private static final String HIGHWAY_URL = "https://highways.musheck.dev";

    /** Hardcoded fallback config for 2b2t (used if remote fetch fails). */
    private static final HighwayServerConfig FALLBACK_2B2T = new HighwayServerConfig(
        new double[]{500, 1000, 1500, 2000, 2500, 7500.5, 55000, 62500, 100000, 125000,
                     250000, 500000, 750000, 1000000, 1250000, 1875000, 2500000, 3750000},
        new double[]{2500, 5000, 25000, 50000, 125000, 250000, 500000, 3750000}
    );

    private static final Map<String, HighwayServerConfig> FALLBACK_CONFIGS;

    private final AtomicReference<Map<String, HighwayServerConfig>> configs =
        new AtomicReference<>(FALLBACK_CONFIGS);

    private final CopyOnWriteArrayList<Runnable> onLoadedCallbacks = new CopyOnWriteArrayList<>();
    private volatile boolean loaded = false;

    private HighwayNetworkManager() {
        fetchRemoteHighways();
    }

    /** Async fetch of highway configs from the remote server. */
    public void fetchRemoteHighways() {
        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(HIGHWAY_URL).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "MusheorAddon");

                if (conn.getResponseCode() != 200) {
                    System.err.println("[HighwayNetworkManager] HTTP " + conn.getResponseCode() + " — keeping fallback configs.");
                    return;
                }

                InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                reader.close();

                LinkedHashMap<String, HighwayServerConfig> parsed = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    String serverName = entry.getKey();
                    JsonObject serverJson = entry.getValue().getAsJsonObject();
                    double[] rings    = parseDoubleArray(serverJson, "ring_distances");
                    double[] diamonds = parseDoubleArray(serverJson, "diamond_distances");

                    // Fall back to existing config values if the remote doesn't provide them
                    HighwayServerConfig existing = configs.get().getOrDefault(serverName, HighwayServerConfig.EMPTY);
                    if (rings == null || rings.length == 0)    rings    = existing.ringDistances();
                    if (diamonds == null || diamonds.length == 0) diamonds = existing.diamondDistances();

                    parsed.put(serverName, new HighwayServerConfig(rings, diamonds));
                }

                LinkedHashMap<String, HighwayServerConfig> merged = new LinkedHashMap<>(configs.get());
                merged.putAll(parsed);
                configs.set(Collections.unmodifiableMap(merged));
                loaded = true;

                System.out.println("[HighwayNetworkManager] Loaded configs for servers: " + configs.get().keySet());

                for (Runnable cb : onLoadedCallbacks) {
                    try { cb.run(); }
                    catch (Exception e) {
                        System.err.println("[HighwayNetworkManager] Callback error: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("[HighwayNetworkManager] Error fetching remote data, using fallback.");
                e.printStackTrace();
            }
        });
    }

    private double[] parseDoubleArray(JsonObject json, String key) {
        if (!json.has(key)) return null;
        JsonArray arr = json.getAsJsonArray(key);
        double[] result = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            try {
                result[i] = arr.get(i).getAsDouble();
            } catch (NumberFormatException e) {
                System.err.println("[HighwayNetworkManager] Skipping invalid number in '" + key + "': " + arr.get(i));
            }
        }
        return result;
    }

    public static HighwayNetworkManager getInstance() { return INSTANCE; }

    public boolean isLoaded() { return loaded; }

    public HighwayServerConfig getConfig(String serverName) {
        Map<String, HighwayServerConfig> map = configs.get();
        if (map == null) return HighwayServerConfig.EMPTY;
        return map.getOrDefault(serverName, HighwayServerConfig.EMPTY);
    }

    public List<String> getServerNames() {
        ArrayList<String> names = new ArrayList<>(configs.get().keySet());
        Collections.sort(names);
        return names.isEmpty() ? Collections.emptyList() : names;
    }

    /** Register a callback to run once the remote config has loaded. Runs immediately if already loaded. */
    public void onLoaded(Runnable callback) {
        if (loaded) callback.run();
        else onLoadedCallbacks.add(callback);
    }

    public double[] getRingDistances(String serverName) {
        return getConfig(serverName).ringDistances();
    }

    public double[] getDiamondDistances(String serverName) {
        return getConfig(serverName).diamondDistances();
    }

    static {
        LinkedHashMap<String, HighwayServerConfig> fallback = new LinkedHashMap<>();
        fallback.put("2b2t", FALLBACK_2B2T);
        FALLBACK_CONFIGS = Collections.unmodifiableMap(fallback);
    }

    /**
     * Immutable config for one server: the distances at which ring roads
     * and diamond-pattern waypoints intersect the main highway.
     */
    public record HighwayServerConfig(double[] ringDistances, double[] diamondDistances) {
        public static final HighwayServerConfig EMPTY = new HighwayServerConfig(new double[0], new double[0]);
    }
}
