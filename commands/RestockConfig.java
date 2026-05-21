// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import net.minecraft.Text;   // ScreenHandler
import net.minecraft.class_1735;   // Slot
import net.minecraft.ItemStack;   // Item
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items
import net.minecraft.GuiGraphics;   // CommandSource
import net.minecraft.class_2287;   // ItemStackArgument
import net.minecraft.BlockPos;   // BlockPos
import net.minecraft.class_2487;   // NbtCompound
import net.minecraft.class_2499;   // NbtList
import net.minecraft.class_2503;   // NbtLong
import net.minecraft.class_2520;   // NbtElement
import net.minecraft.class_2960;   // Identifier
import net.minecraft.class_7157;   // RegistryWrapper
import net.minecraft.class_7923;   // Registries

/**
 * .rc [auto | add <item> | remove <item> | list | clear]
 *
 * Configures which containers (chests, shulkers, etc.) each item should be
 * restocked from. Used by InventoryManager's restocking logic.
 *
 * Modes:
 *   auto          — opens "auto config" mode: walk up to containers and open
 *                   them to automatically register their contents
 *   add <item>    — manually enter "add container" mode for a specific item
 *   remove <item> — removes all configured containers for the given item
 *   list          — lists all configured item→container mappings with distances
 *   clear         — wipes the entire configuration
 *
 * Persisted to the Meteor NBT save file via MusheorSystem.save().
 */
public class RestockConfig extends Command {
    /** item → list of container BlockPos mappings */
    static final Map<ItemStack, List<BlockPos>> containerMap = new HashMap<ItemStack, List<BlockPos>>(); // was: Gt56Sj4a6BWhgB

    // --- State for "add" mode ---
    static boolean addModeActive     = false;    // was: TAdu5cndwWu3A1
    static ItemStack addModeItem    = null;      // was: vgrtgn5

    // --- State for "auto" mode ---
    static boolean autoModeActive    = false;    // was: VYEwzRq
    static BlockPos lastClickedPos = null;     // was: UgB10d
    static int autoScanDelay         = -1;       // was: KP44bk

    public RestockConfig() {
        super("rc", "Configure restocking container locations for items.", new String[]{"rc"});
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.executes(this::handleNoArgs);
        builder.then(RestockConfig.literal("auto").executes(this::handleAuto));
        builder.then(RestockConfig.literal("add")
            .then(RestockConfig.argument("item", (ArgumentType) class_2287.method_9776((class_7157) REGISTRY_ACCESS))
                .executes(this::handleAdd)));
        builder.then(RestockConfig.literal("clear").executes(this::handleClear));
        builder.then(RestockConfig.literal("list").executes(this::handleList));
        builder.then(RestockConfig.literal("remove")
            .then(RestockConfig.argument("item", (ArgumentType) class_2287.method_9776((class_7157) REGISTRY_ACCESS))
                .executes(this::handleRemove)));
    }

    /** If in auto mode, exits it; otherwise shows usage. */
    private int handleNoArgs(CommandContext<GuiGraphics> ctx) { // was: VYEwzRq(CommandContext)
        if (!autoModeActive) {
            this.info("Usage: .rc auto | add <item> | remove <item> | list | clear", new Object[0]);
            return 1;
        }
        autoModeActive    = false;
        lastClickedPos    = null;
        autoScanDelay     = -1;
        if (MeteorClient.EVENT_BUS.isListening(AutoConfigListener.class)) {
            MeteorClient.EVENT_BUS.unsubscribe((Object) AutoConfigListener.INSTANCE);
        }
        this.info("Exited auto config mode", new Object[0]);
        return 1;
    }

    /** Enters auto-config mode: open containers to register them automatically. */
    private int handleAuto(CommandContext<GuiGraphics> ctx) { // was: UgB10d(CommandContext)
        autoModeActive = true;
        lastClickedPos = null;
        autoScanDelay  = -1;
        this.info("Auto config mode enabled", new Object[0]);
        this.info("\u00a77Open containers to automatically register their contents", new Object[0]);
        this.info("\u00a77Type .rc to exit", new Object[0]);
        if (!MeteorClient.EVENT_BUS.isListening(AutoConfigListener.class)) {
            MeteorClient.EVENT_BUS.subscribe((Object) AutoConfigListener.INSTANCE);
        }
        return 1;
    }

    /** Enters item-specific "add container" mode for the given item. */
    private int handleAdd(CommandContext<GuiGraphics> ctx) { // was: KP44bk(CommandContext)
        ItemStack item = class_2287.method_9777(ctx, "item").method_9785();
        if (item == null || item == Items.field_8162) {
            this.error("Invalid item: %s", new Object[]{item});
            return 1;
        }
        addModeActive = true;
        addModeItem   = item;
        this.info("Restocking config mode enabled for \u00a7b%s", new Object[]{getItemName(item)});
        this.info("\u00a77Right-click containers to add them", new Object[0]);
        this.info("\u00a77Esc to exit config mode", new Object[0]);
        if (!MeteorClient.EVENT_BUS.isListening(RestockingConfigListener.class)) {
            MeteorClient.EVENT_BUS.subscribe((Object) RestockingConfigListener.INSTANCE);
        }
        return 1;
    }

    /** Clears all configured containers for all items. */
    private int handleClear(CommandContext<GuiGraphics> ctx) { // was: jWrhVf2psx(CommandContext)
        int total = containerMap.values().stream().mapToInt(List::size).sum();
        containerMap.clear();
        saveConfig();
        this.info("Cleared all restocking containers (%d total)", new Object[]{total});
        return 1;
    }

    /** Lists all item→container mappings with block distances. */
    private int handleList(CommandContext<GuiGraphics> ctx) { // was: usJLOV0subXO3(CommandContext)
        if (containerMap.isEmpty()) {
            this.info("No restocking containers configured", new Object[0]);
            return 1;
        }
        this.info("Restocking containers:", new Object[0]);
        for (Map.Entry<ItemStack, List<BlockPos>> entry : containerMap.entrySet()) {
            ItemStack item = entry.getKey();
            List<BlockPos> positions = entry.getValue();
            this.info("\u00a7b%s\u00a7r (%d containers):", new Object[]{getItemName(item), positions.size()});
            for (BlockPos pos : positions) {
                this.info("  - %s, blocks away",
                    new Object[]{Math.ceil(WorldUtils.distanceTo( // was: Gt56Sj4a6BWhgB(BlockPos,BlockPos)
                        RestockConfig.mc.player.getBlockPos(), pos))});
            }
        }
        return 1;
    }

    /** Removes all containers registered for the given item. */
    private int handleRemove(CommandContext<GuiGraphics> ctx) { // was: ZbTtF5KYyGL9YXed(CommandContext)
        ItemStack item = class_2287.method_9777(ctx, "item").method_9785();
        if (item == null || item == Items.field_8162) {
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
    public static String getItemName(ItemStack item) { // was: jOdDDFXSeWl4(Item)
        return class_7923.field_41178.method_10221(item).toString().replace("minecraft:", "");
    }

    /** Returns the list of container positions for the given item (empty list if none). */
    public static List<BlockPos> getContainersFor(ItemStack item) { // was: mp3zoXQFKUKYj5(Item)
        return containerMap.getOrDefault(item, new ArrayList());
    }

    /** Returns true if the given item has at least one configured container. */
    public static boolean hasContainers(ItemStack item) { // was: Gt56Sj4a6BWhgB(Item)
        return containerMap.containsKey(item) && !containerMap.get(item).isEmpty();
    }

    /** Triggers a Meteor NBT save so the config is persisted. */
    static void saveConfig() { // was: jOdDDFXSeWl4() (no-arg)
        ((MusheorSystem) Systems.get(MusheorSystem.class)).save();
    }

    // -------------------------------------------------------------------------
    // NBT serialisation
    // -------------------------------------------------------------------------

    /** Serialises the container map to an NbtCompound (item ID → list of packed BlockPos longs). */
    public static class_2487 toTag() {
        class_2487 tag = new class_2487();
        for (Map.Entry<ItemStack, List<BlockPos>> entry : containerMap.entrySet()) {
            String id = class_7923.field_41178.method_10221(entry.getKey()).toString();
            class_2499 list = new class_2499();
            for (BlockPos pos : entry.getValue()) {
                list.add(class_2503.method_23251((long) pos.method_10063())); // asLong()
            }
            tag.method_10566(id, (class_2520) list);
        }
        return tag;
    }

    /** Deserialises the container map from an NbtCompound. */
    public static void fromTag(class_2487 tag) { // was: jOdDDFXSeWl4(NbtCompound)
        containerMap.clear();
        for (String key : tag.method_10541()) {
            class_2960 id = class_2960.method_12829(key); // Identifier.tryParse
            if (id == null || !class_7923.field_41178.method_10250(id)) continue;
            ItemStack item = (ItemStack) class_7923.field_41178.method_63535(id);
            class_2499 list = VersionHelper.get().getList(tag, key, 4); // TAG_Long = 4
            ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
            for (int i = 0; i < list.size(); ++i) {
                positions.add(BlockPos.method_10092((long) ((class_2503) list.get(i)).method_10699())); // fromLong
            }
            containerMap.put(item, positions);
        }
    }

    // -------------------------------------------------------------------------
    // Event listener: auto-config mode
    // -------------------------------------------------------------------------

    /**
     * Listens for block interactions and screen opens while in auto-config mode.
     * After a delay of 4 ticks (to let the server send the container contents),
     * scans the open screen handler for item types and registers the container.
     */
    static class AutoConfigListener {
        static final AutoConfigListener INSTANCE = new AutoConfigListener(); // was: jWrhVf2psx

        private AutoConfigListener() {}

        @EventHandler(priority = 200)
        private void onInteractBlock(InteractBlockEvent event) {
            if (!autoModeActive) return;
            lastClickedPos = event.result.method_17777(); // getBlockPos()
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
            if (mc.field_1724 == null || mc.player.field_7512 == null || lastClickedPos == null) return;

            Text screenHandler = mc.player.field_7512; // currentScreenHandler
            HashSet<ItemStack> items = new HashSet<ItemStack>();

            // Only scan the container slots (skip the 36 player inventory slots at the end)
            int containerSlots = screenHandler.field_7761.size() - 36;
            if (containerSlots <= 0) return;

            for (int i = 0; i < containerSlots; ++i) {
                class_1735 slot  = (class_1735) screenHandler.field_7761.get(i);
                ItemStack stack = slot.method_7677(); // getStack()
                if (stack.setStack() || stack.getStack() == Items.field_8162) continue;
                items.add(stack.getStack());
            }

            if (items.isEmpty()) {
                ChatUtils.info("Container at \u00a7b%d %d %d\u00a7r is empty, skipping",
                    new Object[]{lastClickedPos.getX(), lastClickedPos.getY(), lastClickedPos.getZ()});
            } else {
                BlockPos pos = lastClickedPos;
                for (ItemStack item : items) {
                    List<BlockPos> list = containerMap.computeIfAbsent(item, k -> new ArrayList());
                    if (!list.contains(pos)) list.add(pos);
                }
                saveConfig();
                ChatUtils.info("Registered \u00a7b%d\u00a7r item(s) from container",
                    new Object[]{items.size(), pos.getX(), pos.getY(), pos.getZ()});
                for (ItemStack item : items) {
                    ChatUtils.info("  - \u00a7b%s", new Object[]{getItemName(item)});
                }
            }
            lastClickedPos = null;
        }
    }

    // -------------------------------------------------------------------------
    // Event listener: add-container mode (specific item)
    // -------------------------------------------------------------------------

    /**
     * Listens while in add-mode (triggered by ".rc add <item>").
     * Right-clicking a container registers it for the configured item.
     * Pressing Esc (opening a null screen) exits the mode.
     */
    static class RestockingConfigListener {
        static final RestockingConfigListener INSTANCE = new RestockingConfigListener(); // was: usJLOV0subXO3

        private RestockingConfigListener() {}

        @EventHandler(priority = 200)
        private void onInteractBlock(InteractBlockEvent event) {
            if (!addModeActive || addModeItem == null) return;
            BlockPos pos = event.result.method_17777();
            containerMap.computeIfAbsent(addModeItem, k -> new ArrayList()).add(pos);
            saveConfig();
            ChatUtils.info("Added container for \u00a7b%s",
                new Object[]{class_7923.field_41178.method_10221(addModeItem).toString()});
            event.cancel();
        }

        @EventHandler(priority = 200)
        private void onOpenScreen(OpenScreenEvent event) {
            if (!addModeActive) return;
            if (event.screen == null) return;
            exitAddMode();
            event.cancel();
        }

        private void exitAddMode() { // was: vgrtgn5()
            addModeActive = false;
            ItemStack item = addModeItem;
            addModeItem = null;
            int count = ((List<?>) containerMap.getOrDefault(item, new ArrayList())).size();
            ChatUtils.info("Exited restocking config mode. Added \u00a7b%d\u00a7r containers for \u00a7b%s",
                new Object[]{count, getItemName(item)});
        }
    }
}
