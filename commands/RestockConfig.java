// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.UkMm7uisd; nested listeners were obf.UkMm7uisd$FDb5 / $aY0a71o)
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.utils.WorldUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * .rc [auto | add <item> | remove <item> | list | clear]
 *
 * Configures which containers (chests, shulkers, etc.) each item should be
 * restocked from. Used by the InventoryManager restocking logic.
 *
 * Modes:
 *   auto          — "auto config" mode: open containers to auto-register contents
 *   add <item>    — manually enter "add container" mode for a specific item
 *   remove <item> — removes all configured containers for the given item
 *   list          — lists all configured item→container mappings with distances
 *   clear         — wipes the entire configuration
 *
 * Persisted to the Meteor NBT save file via MusheorSystem.save().
 */
public class RestockConfig extends Command {
    /** item → list of container BlockPos mappings */
    private static final Map<Item, List<BlockPos>> containerMap = new HashMap<Item, List<BlockPos>>(); // was: FvaNWO (Map field)

    // --- State for "add" mode ---
    private static boolean addModeActive = false;  // was: Q90GLXQ0Pef
    private static Item addModeItem      = null;   // was: psJq59YIbp3Z

    // --- State for "auto" mode ---
    private static boolean autoModeActive = false; // was: SOYyh5IPg26f7F
    private static BlockPos lastClickedPos = null;  // was: rKbT3Ifwo
    private static int autoScanDelay       = -1;   // was: r7hOYIKN2

    public RestockConfig() {
        super("rc", "Configure restocking container locations for items.", new String[]{"rc"});
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.executes(this::handleNoArgs);
        builder.then(RestockConfig.literal("auto").executes(this::handleAuto));
        builder.then(RestockConfig.literal("add")
            .then(RestockConfig.argument("item", (ArgumentType) ItemStackArgumentType.itemStack((CommandRegistryAccess) REGISTRY_ACCESS))
                .executes(this::handleAdd)));
        builder.then(RestockConfig.literal("clear").executes(this::handleClear));
        builder.then(RestockConfig.literal("list").executes(this::handleList));
        builder.then(RestockConfig.literal("remove")
            .then(RestockConfig.argument("item", (ArgumentType) ItemStackArgumentType.itemStack((CommandRegistryAccess) REGISTRY_ACCESS))
                .executes(this::handleRemove)));
    }

    /** If in auto mode, exits it; otherwise shows usage. */
    private int handleNoArgs(CommandContext<ServerCommandSource> ctx) { // was: FvaNWO(CommandContext)
        if (!autoModeActive) {
            this.info("Usage: .rc auto | add <item> | remove <item> | list | clear", new Object[0]);
            return 1;
        }
        autoModeActive = false;
        lastClickedPos = null;
        autoScanDelay  = -1;
        if (MeteorClient.EVENT_BUS.isListening(AutoConfigListener.class)) {
            MeteorClient.EVENT_BUS.unsubscribe((Object) AutoConfigListener.INSTANCE);
        }
        this.info("Exited auto config mode", new Object[0]);
        return 1;
    }

    /** Enters auto-config mode: open containers to register them automatically. */
    private int handleAuto(CommandContext<ServerCommandSource> ctx) { // was: Q90GLXQ0Pef(CommandContext)
        autoModeActive = true;
        lastClickedPos = null;
        autoScanDelay  = -1;
        this.info("Auto config mode enabled", new Object[0]);
        this.info("§7Open containers to automatically register their contents", new Object[0]);
        this.info("§7Type .rc to exit", new Object[0]);
        if (!MeteorClient.EVENT_BUS.isListening(AutoConfigListener.class)) {
            MeteorClient.EVENT_BUS.subscribe((Object) AutoConfigListener.INSTANCE);
        }
        return 1;
    }

    /** Enters item-specific "add container" mode for the given item. */
    private int handleAdd(CommandContext<ServerCommandSource> ctx) { // was: psJq59YIbp3Z(CommandContext)
        Item item = ItemStackArgumentType.getItemStackArgument(ctx, "item").getItem();
        if (item == null || item == Items.AIR) {
            this.error("Invalid item: %s", new Object[]{item});
            return 1;
        }
        addModeActive = true;
        addModeItem   = item;
        this.info("Restocking config mode enabled for §b%s", new Object[]{getItemName(item)});
        this.info("§7Right-click containers to add them", new Object[0]);
        this.info("§7Esc to exit config mode", new Object[0]);
        if (!MeteorClient.EVENT_BUS.isListening(RestockingConfigListener.class)) {
            MeteorClient.EVENT_BUS.subscribe((Object) RestockingConfigListener.INSTANCE);
        }
        return 1;
    }

    /** Clears all configured containers for all items. */
    private int handleClear(CommandContext<ServerCommandSource> ctx) { // was: SOYyh5IPg26f7F(CommandContext)
        int total = containerMap.values().stream().mapToInt(List::size).sum();
        containerMap.clear();
        saveConfig();
        this.info("Cleared all restocking containers (%d total)", new Object[]{total});
        return 1;
    }

    /** Lists all item→container mappings with block distances. */
    private int handleList(CommandContext<ServerCommandSource> ctx) { // was: rKbT3Ifwo(CommandContext)
        if (containerMap.isEmpty()) {
            this.info("No restocking containers configured", new Object[0]);
            return 1;
        }
        this.info("Restocking containers:", new Object[0]);
        for (Map.Entry<Item, List<BlockPos>> entry : containerMap.entrySet()) {
            Item item = entry.getKey();
            List<BlockPos> positions = entry.getValue();
            this.info("§b%s§r (%d containers):", new Object[]{getItemName(item), positions.size()});
            for (BlockPos pos : positions) {
                this.info("  - %s, blocks away",
                    new Object[]{Math.ceil(WorldUtils.horizontalDistance(
                        RestockConfig.mc.player.getBlockPos(), pos))}); // was: HFbqnT1FEh2q.FvaNWO
            }
        }
        return 1;
    }

    /** Removes all containers registered for the given item. */
    private int handleRemove(CommandContext<ServerCommandSource> ctx) { // was: r7hOYIKN2(CommandContext)
        Item item = ItemStackArgumentType.getItemStackArgument(ctx, "item").getItem();
        if (item == null || item == Items.AIR) {
            this.error("Invalid item: %s", new Object[]{item});
            return 1;
        }
        List<BlockPos> removed = containerMap.remove(item);
        if (removed == null || removed.isEmpty()) {
            this.warning("No containers configured for %s", new Object[]{getItemName(item)});
        } else {
            this.info("Removed %d containers for %s", new Object[]{removed.size(), getItemName(item)});
            saveConfig();
        }
        return 1;
    }

    // -------------------------------------------------------------------------
    // Static API used by InventoryManager
    // -------------------------------------------------------------------------

    /** Returns the display name of an item (strips "minecraft:" prefix). */
    public static String getItemName(Item item) { // was: FvaNWO(Item)
        return Registries.ITEM.getId(item).toString().replace("minecraft:", "");
    }

    /** Returns the list of container positions for the given item (empty list if none). */
    public static List<BlockPos> getContainersFor(Item item) { // was: Q90GLXQ0Pef(Item)
        return containerMap.getOrDefault(item, new ArrayList<BlockPos>());
    }

    /** Returns true if the given item has at least one configured container. */
    public static boolean hasContainers(Item item) { // was: psJq59YIbp3Z(Item)
        return containerMap.containsKey(item) && !containerMap.get(item).isEmpty();
    }

    /** Triggers a Meteor NBT save so the config is persisted. */
    private static void saveConfig() { // was: rKbT3Ifwo() (no-arg)
        ((MusheorSystem) Systems.get(MusheorSystem.class)).save();
    }

    // -------------------------------------------------------------------------
    // NBT serialisation
    // -------------------------------------------------------------------------

    /** Serialises the container map to an NbtCompound (item ID → list of packed BlockPos longs). */
    public static NbtCompound toTag() { // was: FvaNWO() (NbtCompound)
        NbtCompound tag = new NbtCompound();
        for (Map.Entry<Item, List<BlockPos>> entry : containerMap.entrySet()) {
            String id = Registries.ITEM.getId(entry.getKey()).toString();
            NbtList list = new NbtList();
            for (BlockPos pos : entry.getValue()) {
                list.add(NbtLong.of((long) pos.asLong()));
            }
            tag.put(id, (NbtElement) list);
        }
        return tag;
    }

    /** Deserialises the container map from an NbtCompound. */
    public static void fromTag(NbtCompound tag) { // was: FvaNWO(NbtCompound)
        containerMap.clear();
        for (String key : tag.getKeys()) {
            Identifier id = Identifier.tryParse(key);
            if (id == null || !Registries.ITEM.containsId(id)) continue;
            Item item = (Item) Registries.ITEM.get(id);
            NbtList list = VersionHelper.get().getList(tag, key, 4); // TAG_Long = 4
            ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
            for (int i = 0; i < list.size(); ++i) {
                positions.add(BlockPos.fromLong((long) ((NbtLong) list.get(i)).longValue()));
            }
            containerMap.put(item, positions);
        }
    }

    // -------------------------------------------------------------------------
    // Event listener: auto-config mode  (was: nested class UkMm7uisd$FDb5)
    // -------------------------------------------------------------------------

    /**
     * Listens for block interactions and screen opens while in auto-config mode.
     * After a delay of 4 ticks (to let the server send the container contents),
     * scans the open screen handler for item types and registers the container.
     */
    private static class AutoConfigListener {
        private static final AutoConfigListener INSTANCE = new AutoConfigListener(); // was: FvaNWO

        private AutoConfigListener() {}

        @EventHandler(priority = 200)
        private void onInteractBlock(InteractBlockEvent event) {
            if (!autoModeActive) return;
            lastClickedPos = event.result.getBlockPos();
            autoScanDelay  = -1;
        }

        @EventHandler(priority = 200)
        private void onOpenScreen(OpenScreenEvent event) {
            if (!autoModeActive || lastClickedPos == null) return;
            if (event.screen == null) return;
            autoScanDelay = 4; // wait 4 ticks for server to populate container
        }

        @EventHandler
        private void onTick(TickEvent.Post post) {
            if (!autoModeActive || autoScanDelay < 0) return;
            if (autoScanDelay > 0) {
                --autoScanDelay;
                return;
            }
            autoScanDelay = -1;
            if (mc.player == null || mc.player.currentScreenHandler == null || lastClickedPos == null) return;

            ScreenHandler screenHandler = mc.player.currentScreenHandler;
            HashSet<Item> items = new HashSet<Item>();

            // Only scan the container slots (skip the 36 player inventory slots at the end)
            int containerSlots = screenHandler.slots.size() - 36;
            if (containerSlots <= 0) return;

            for (int i = 0; i < containerSlots; ++i) {
                Slot slot  = (Slot) screenHandler.slots.get(i);
                ItemStack stack = slot.getStack();
                if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;
                items.add(stack.getItem());
            }

            if (items.isEmpty()) {
                ChatUtils.info("Container at §b%d %d %d§r is empty, skipping",
                    new Object[]{lastClickedPos.getX(), lastClickedPos.getY(), lastClickedPos.getZ()});
            } else {
                BlockPos pos = lastClickedPos;
                for (Item item : items) {
                    List<BlockPos> list = containerMap.computeIfAbsent(item, k -> new ArrayList<BlockPos>());
                    if (!list.contains(pos)) list.add(pos);
                }
                saveConfig();
                ChatUtils.info("Registered §b%d§r item(s) from container",
                    new Object[]{items.size(), pos.getX(), pos.getY(), pos.getZ()});
                for (Item item : items) {
                    ChatUtils.info("  - §b%s", new Object[]{getItemName(item)});
                }
            }
            lastClickedPos = null;
        }
    }

    // -------------------------------------------------------------------------
    // Event listener: add-container mode  (was: nested class UkMm7uisd$aY0a71o)
    // -------------------------------------------------------------------------

    /**
     * Listens while in add-mode (triggered by ".rc add <item>").
     * Right-clicking a container registers it for the configured item.
     * Pressing Esc (opening a null screen) exits the mode.
     */
    private static class RestockingConfigListener {
        private static final RestockingConfigListener INSTANCE = new RestockingConfigListener(); // was: FvaNWO

        private RestockingConfigListener() {}

        @EventHandler(priority = 200)
        private void onInteractBlock(InteractBlockEvent event) {
            if (!addModeActive || addModeItem == null) return;
            BlockPos pos = event.result.getBlockPos();
            containerMap.computeIfAbsent(addModeItem, k -> new ArrayList<BlockPos>()).add(pos);
            saveConfig();
            ChatUtils.info("Added container for §b%s",
                new Object[]{Registries.ITEM.getId(addModeItem).toString()});
            event.cancel();
        }

        @EventHandler(priority = 200)
        private void onOpenScreen(OpenScreenEvent event) {
            if (!addModeActive) return;
            if (event.screen == null) return;
            exitAddMode();
            event.cancel();
        }

        private void exitAddMode() { // was: FvaNWO() (no-arg)
            addModeActive = false;
            Item item = addModeItem;
            addModeItem = null;
            int count = containerMap.getOrDefault(item, new ArrayList<BlockPos>()).size();
            ChatUtils.info("Exited restocking config mode. Added §b%d§r containers for §b%s",
                new Object[]{count, getItemName(item)});
        }
    }
}
