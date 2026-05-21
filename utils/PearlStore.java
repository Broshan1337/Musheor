// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.BlockPos;  // BlockPos

/**
 * Persists a list of tracked ender pearl entities to {@code meteor-client/pearls.json}.
 *
 * Each pearl is stored as a {@link PearlRecord} containing:
 *   - owner: the player username whose pearl this is
 *   - pos:   the last known BlockPos of the pearl entity
 *   - entityId: the runtime entity ID (used to correlate entity move packets)
 *
 * The store is saved to disk after every mutation.
 */
public class PearlStore {
    private static final Path PEARLS_FILE = Path.of("meteor-client/pearls.json", new String[0]); // was: nahKImcZNC
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();              // was: HhqUkuyUmfI4QmDV
    private static final List<PearlRecord> pearls = new ArrayList<PearlRecord>();                 // was: ClRygFqRHY3

    /** Returns the live list of tracked pearl records. */
    public static List<PearlRecord> getPearls() { // was: WDd4dOS9
        return pearls;
    }

    /**
     * Adds or updates a pearl record for the given position.
     * If a record with the same {@code pos} already exists, updates its owner and entityId;
     * otherwise appends a new record. Saves to disk afterward.
     */
    public static void addOrUpdate(String owner, BlockPos pos, int entityId) { // was: jOdDDFXSeWl4(String,BlockPos,int)
        for (PearlRecord record : pearls) {
            if (!record.pos.equals(pos)) continue;
            record.owner    = owner;
            record.entityId = entityId;
            PearlStore.save();
            return;
        }
        pearls.add(new PearlRecord(owner, pos, entityId));
        PearlStore.save();
    }

    /**
     * Updates the entityId of the record matching {@code pos}.
     * Does not save to disk (entity IDs are transient session data).
     */
    public static void updateEntityId(BlockPos pos, int entityId) { // was: jOdDDFXSeWl4(BlockPos,int)
        for (PearlRecord record : pearls) {
            if (!record.pos.equals(pos)) continue;
            record.entityId = entityId;
            return;
        }
    }

    /** Returns the PearlRecord at the given position, or null if not tracked. */
    public static PearlRecord getByPos(BlockPos pos) { // was: J9PiTNS(BlockPos)
        for (PearlRecord record : pearls) {
            if (!record.pos.equals(pos)) continue;
            return record;
        }
        return null;
    }

    /** Removes all pearls belonging to the given owner (case-insensitive) and saves. */
    public static void removeByOwner(String owner) { // was: xG2PP8jo4RWLS(String)
        pearls.removeIf(record -> record.owner.equalsIgnoreCase(owner));
        PearlStore.save();
    }

    /** Clears all tracked pearls and saves the empty list to disk. */
    public static void clearAll() { // was: TMdT6kYQyv0It
        pearls.clear();
        PearlStore.save();
    }

    /** Returns a list of all pearls belonging to the given owner (case-insensitive). */
    public static List<PearlRecord> getByOwner(String owner) { // was: LoFK6z05DRRnOV(String)
        ArrayList<PearlRecord> result = new ArrayList<PearlRecord>();
        for (PearlRecord record : pearls) {
            if (!record.owner.equalsIgnoreCase(owner)) continue;
            result.add(record);
        }
        return result;
    }

    /** Serialises the pearl list to {@code meteor-client/pearls.json}. */
    public static void save() { // was: jOdDDFXSeWl4() (no-arg overload)
        try {
            Files.createDirectories(PEARLS_FILE.getParent(), new FileAttribute[0]);
            JsonArray array = new JsonArray();
            for (PearlRecord record : pearls) {
                JsonObject obj = new JsonObject();
                obj.addProperty("owner",    record.owner);
                obj.addProperty("entityId", (Number) record.entityId);
                obj.addProperty("x", (Number) record.pos.getX()); // getX
                obj.addProperty("y", (Number) record.pos.getY()); // getY
                obj.addProperty("z", (Number) record.pos.getZ()); // getZ
                array.add((JsonElement) obj);
            }
            Files.writeString(PEARLS_FILE, (CharSequence) gson.toJson((JsonElement) array), new OpenOption[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Loads pearl records from disk, replacing the current in-memory list. */
    public static void load() { // was: yVhVr2zkw
        pearls.clear();
        if (!Files.exists(PEARLS_FILE, new LinkOption[0])) return;
        try {
            String json = Files.readString(PEARLS_FILE);
            JsonArray array = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement element : array) {
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
    public static class PearlRecord {
        public String     owner;    // was: SuVwp6TMoBEuEP
        public BlockPos pos;      // was: Iy17yV0  (BlockPos)
        public int        entityId; // was: BLEzYuOlPg4yGLp

        public PearlRecord(String owner, BlockPos pos, int entityId) {
            this.owner    = owner;
            this.pos      = pos;
            this.entityId = entityId;
        }
    }
}
