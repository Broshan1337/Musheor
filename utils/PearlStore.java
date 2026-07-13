// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.Mj77A; inner record was obf.Mj77A$FDb5)
package musheor.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;

/**
 * Persists a list of tracked ender pearl entities to {@code meteor-client/pearls.json}.
 *
 * Each pearl is stored as a {@link PearlRecord} containing:
 *   - owner: the player username whose pearl this is
 *   - pos:   the trapdoor/block BlockPos the pearl is associated with
 *   - entityId: the runtime entity ID (used to correlate entity move packets)
 *
 * The store is saved to disk after every mutation.
 */
public class PearlStore {
    private static final Path PEARLS_FILE = Path.of("meteor-client/pearls.json"); // was: FvaNWO (Path)
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // was: Q90GLXQ0Pef (Gson)
    private static final List<PearlRecord> pearls = new ArrayList<PearlRecord>();   // was: psJq59YIbp3Z (List)

    /** Returns the live list of tracked pearl records. */
    public static List<PearlRecord> getPearls() { // was: FvaNWO()
        return pearls;
    }

    /**
     * Adds or updates a pearl record for the given trapdoor position.
     * If a record with the same {@code pos} already exists, updates its owner and entityId;
     * otherwise appends a new record. Saves to disk afterward.
     */
    public static void addOrUpdate(String owner, BlockPos trapdoorPos, int entityId) { // was: FvaNWO(String,BlockPos,int)
        for (PearlRecord record : pearls) {
            if (record.pos.equals(trapdoorPos)) {
                record.owner    = owner;
                record.entityId = entityId;
                save();
                return;
            }
        }
        pearls.add(new PearlRecord(owner, trapdoorPos, entityId));
        save();
    }

    /**
     * Updates the entityId of the record matching {@code pos}.
     * Does not save to disk (entity IDs are transient session data).
     */
    public static void updateEntityId(BlockPos trapdoorPos, int entityId) { // was: FvaNWO(BlockPos,int)
        for (PearlRecord record : pearls) {
            if (record.pos.equals(trapdoorPos)) {
                record.entityId = entityId;
                return;
            }
        }
    }

    /** Returns the PearlRecord at the given position, or null if not tracked. */
    public static PearlRecord getByPos(BlockPos trapdoorPos) { // was: FvaNWO(BlockPos)
        for (PearlRecord record : pearls) {
            if (record.pos.equals(trapdoorPos)) {
                return record;
            }
        }
        return null;
    }

    /** Removes all pearls belonging to the given player (case-insensitive) and saves. */
    public static void removeByOwner(String playerName) { // was: FvaNWO(String)
        pearls.removeIf(record -> record.owner.equalsIgnoreCase(playerName));
        save();
    }

    /** Clears all tracked pearls and saves the empty list to disk. */
    public static void clearAll() { // was: Q90GLXQ0Pef()
        pearls.clear();
        save();
    }

    /** Returns a list of all pearls belonging to the given owner (case-insensitive). */
    public static List<PearlRecord> getByOwner(String ownerName) { // was: Q90GLXQ0Pef(String)
        ArrayList<PearlRecord> result = new ArrayList<PearlRecord>();
        for (PearlRecord record : pearls) {
            if (record.owner.equalsIgnoreCase(ownerName)) {
                result.add(record);
            }
        }
        return result;
    }

    /** Serialises the pearl list to {@code meteor-client/pearls.json}. */
    public static void save() { // was: psJq59YIbp3Z()
        try {
            Files.createDirectories(PEARLS_FILE.getParent());
            JsonArray array = new JsonArray();
            for (PearlRecord record : pearls) {
                JsonObject obj = new JsonObject();
                obj.addProperty("owner",    record.owner);
                obj.addProperty("entityId", record.entityId);
                obj.addProperty("x", record.pos.getX());
                obj.addProperty("y", record.pos.getY());
                obj.addProperty("z", record.pos.getZ());
                array.add(obj);
            }
            Files.writeString(PEARLS_FILE, gson.toJson(array));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Loads pearl records from disk, replacing the current in-memory list. */
    public static void load() { // was: SOYyh5IPg26f7F()
        pearls.clear();
        if (!Files.exists(PEARLS_FILE)) return;
        try {
            String json = Files.readString(PEARLS_FILE);
            for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                String owner    = obj.get("owner").getAsString();
                int    entityId = obj.get("entityId").getAsInt();
                int    x        = obj.get("x").getAsInt();
                int    y        = obj.get("y").getAsInt();
                int    z        = obj.get("z").getAsInt();
                pearls.add(new PearlRecord(owner, new BlockPos(x, y, z), entityId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Holds the tracked state for a single ender pearl entity. */
    public static class PearlRecord { // was: obf.Mj77A$FDb5
        public String   owner;    // was: FvaNWO
        public BlockPos pos;      // was: Q90GLXQ0Pef
        public int      entityId; // was: psJq59YIbp3Z

        public PearlRecord(String owner, BlockPos pos, int entityId) {
            this.owner    = owner;
            this.pos      = pos;
            this.entityId = entityId;
        }
    }
}
