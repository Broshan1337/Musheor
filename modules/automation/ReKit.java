// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.internal.RateController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

/**
 * "rekit" — sorts the inventory to match a saved kit loadout when a container opens or a
 * keybind is pressed. Kits map inventory slot (0-35) -> item and are stored as JSON under
 * {@code <meteor>/musheor/kits/<name>.json}. Rearrangement is planned as slot moves and
 * executed with rate-limited slot clicks, pulling extra items from an open container.
 */
public class ReKit extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: SOYyh5IPg26f7F
    private final MinecraftClient mc = MinecraftClient.getInstance();       // was: rKbT3Ifwo

    public final Setting<RekitMode> mode = sgGeneral.add(new EnumSetting.Builder<RekitMode>() // was: FvaNWO
        .name("mode").description("Auto = triggers on container open. OnKey = triggers on keybind.").defaultValue(RekitMode.AUTO).build());
    public final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder() // was: Q90GLXQ0Pef
        .name("key").description("Keybind to trigger rekit.").defaultValue(Keybind.none()).visible(() -> mode.get() == RekitMode.ON_KEY).build());
    public final Setting<String> loadout = sgGeneral.add(new StringSetting.Builder() // was: psJq59YIbp3Z
        .name("kit-name").description("Name of the active kit. Leave blank to disable.").defaultValue("").build());

    private final HashMap<String, HashMap<Integer, Item>> kitCache = new HashMap<>(); // was: r7hOYIKN2
    private boolean keyWasPressed = false;   // was: oZHMlTL
    private int openDelayTicks = -1;         // was: xQr5FhbwpQPWgIQ
    private ScreenHandler pendingHandler = null; // was: OMMZL1F3q

    public ReKit() {
        super(musheor.MAIN, "rekit", "Sorts inventory to match a saved kit loadout when opening a container or pressing a keybind.");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton saveBtn = list.add(theme.button("Save current loadout")).widget();
        WButton clearBtn = list.add(theme.button("Clear kit cache")).widget();
        saveBtn.action = () -> this.saveKit(this.loadout.get());
        clearBtn.action = this::clearCache;
        return list;
    }

    @Override
    public void onActivate() {
        String name = this.loadout.get();
        if (name.isEmpty()) {
            this.info("No kit selected.");
            return;
        }
        HashMap<Integer, Item> kit = this.loadKit(name);
        if (kit == null) {
            this.info("Kit '§b%s§r' not found. Use §b.kit create§r to save one.", name);
        } else {
            this.kitCache.put(name, kit);
            this.info("Loaded kit '§b%s§r' (§b%d§r slots).", name, kit.size());
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) { // was: FvaNWO(OpenScreenEvent)
        if (this.mode.get() != RekitMode.AUTO || this.mc.player == null) return;
        if (event.screen instanceof HandledScreen && !(event.screen instanceof InventoryScreen)) {
            ScreenHandler handler = this.mc.player.currentScreenHandler;
            if (handler != null && handler != this.mc.player.playerScreenHandler) {
                this.pendingHandler = handler;
                this.openDelayTicks = 1;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null) return;

        if (this.openDelayTicks > 0 && --this.openDelayTicks == 0) {
            ScreenHandler current = this.mc.player.currentScreenHandler;
            if (current != null && current == this.pendingHandler) {
                this.applyKit(this.pendingHandler);
            }
            this.pendingHandler = null;
        }

        if (this.mode.get() == RekitMode.ON_KEY) {
            boolean pressed = this.key.get().isPressed();
            if (pressed && !this.keyWasPressed) {
                this.keyWasPressed = true;
                ScreenHandler handler = null;
                if (this.mc.currentScreen instanceof HandledScreen) {
                    ScreenHandler current = this.mc.player.currentScreenHandler;
                    if (current != null && current != this.mc.player.playerScreenHandler) handler = current;
                }
                this.applyKit(handler);
            } else if (!pressed) {
                this.keyWasPressed = false;
            }
        }
    }

    /** Plans and executes the slot moves needed to satisfy the active kit within {@code containerHandler}. */
    public void applyKit(ScreenHandler containerHandler) { // was: FvaNWO(ScreenHandler)
        if (this.mc.player == null) return;
        String name = this.loadout.get();
        if (name.isEmpty()) return;

        HashMap<Integer, Item> kit = this.kitCache.computeIfAbsent(name, this::loadKit);
        if (kit == null) {
            this.info("Kit '§b%s§r' not found.", name);
            return;
        }

        Set<Integer> kitSlots = kit.keySet();
        int syncId = this.getSyncId(containerHandler);
        boolean hasContainer = containerHandler != null && containerHandler != this.mc.player.playerScreenHandler;
        int containerSize = hasContainer ? containerHandler.slots.size() - 36 : 0;

        Item[] invItem = new Item[36];
        int[] invCount = new int[36];
        for (int i = 0; i < 36; i++) {
            ItemStack s = this.mc.player.getInventory().getStack(i);
            invItem[i] = s.getItem();
            invCount[i] = s.getCount();
        }

        Item[] conItem = new Item[containerSize];
        int[] conCount = new int[containerSize];
        for (int i = 0; i < containerSize; i++) {
            ItemStack s = containerHandler.getSlot(i).getStack();
            conItem[i] = s.getItem();
            conCount[i] = s.getCount();
        }

        List<MoveOp> plan = new ArrayList<>();
        for (Map.Entry<Integer, Item> entry : kit.entrySet()) {
            int targetInv = entry.getKey();
            Item targetItem = entry.getValue();
            int maxCount = targetItem.getMaxCount();
            int targetScreen = this.toScreenSlot(targetInv, containerHandler);
            int current = invItem[targetInv] == targetItem ? invCount[targetInv] : 0;
            if (current >= maxCount) continue;

            // Evict a wrong item occupying the target slot.
            if (!(invCount[targetInv] <= 0 || invItem[targetInv] == targetItem)) {
                int freeSlot = this.findFreeSlot(invCount, kitSlots, targetInv);
                if (freeSlot != -1) {
                    plan.add(new MoveOp(targetScreen, this.toScreenSlot(freeSlot, containerHandler), -1));
                    invItem[freeSlot] = invItem[targetInv];
                    invCount[freeSlot] = invCount[targetInv];
                } else {
                    if (containerSize <= 0) continue;
                    plan.add(new MoveOp(targetScreen, -1, -1));
                }
                invItem[targetInv] = Items.AIR;
                invCount[targetInv] = 0;
                current = 0;
            }

            int remaining = maxCount - current;

            // Consolidate matching stacks from the inventory into the target slot.
            for (int i = 0; i < 36 && remaining > 0; i++) {
                if (invItem[i] == targetItem && i != targetInv && invCount[i] != 0) {
                    Item kitAtI = kit.get(i);
                    if (kitAtI == null || kitAtI != targetItem) {
                        int fromScreen = this.toScreenSlot(i, containerHandler);
                        int moved = Math.min(invCount[i], remaining);
                        plan.add(new MoveOp(fromScreen, targetScreen, invCount[i] > remaining ? fromScreen : -1));
                        invCount[i] -= moved;
                        if (invCount[i] == 0) invItem[i] = Items.AIR;
                        invCount[targetInv] += moved;
                        invItem[targetInv] = targetItem;
                        remaining -= moved;
                    }
                }
            }

            // Pull the remainder from the open container.
            for (int i = 0; i < containerSize && remaining > 0; i++) {
                if (conItem[i] == targetItem && conCount[i] != 0) {
                    int moved = Math.min(conCount[i], remaining);
                    plan.add(new MoveOp(i, targetScreen, conCount[i] > remaining ? i : -1));
                    conCount[i] -= moved;
                    if (conCount[i] == 0) conItem[i] = Items.AIR;
                    invCount[targetInv] += moved;
                    invItem[targetInv] = targetItem;
                    remaining -= moved;
                }
            }
        }

        // Execute the plan (rate-limited).
        for (MoveOp op : plan) {
            if (!RateController.canSendInventoryPacket()) return;
            if (op.to() == -1) {
                this.mc.interactionManager.clickSlot(syncId, op.from(), 0, SlotActionType.QUICK_MOVE, this.mc.player);
            } else {
                this.mc.interactionManager.clickSlot(syncId, op.from(), 0, SlotActionType.PICKUP, this.mc.player);
                this.mc.interactionManager.clickSlot(syncId, op.to(), 0, SlotActionType.PICKUP, this.mc.player);
                if (op.returnTo() != -1) {
                    this.mc.interactionManager.clickSlot(syncId, op.returnTo(), 0, SlotActionType.PICKUP, this.mc.player);
                }
            }
        }
    }

    /** Maps an inventory slot index (0-35) to the screen-handler slot index for the current screen. */
    private int toScreenSlot(int invSlot, ScreenHandler containerHandler) { // was: FvaNWO(int,ScreenHandler)
        if (containerHandler != null && containerHandler != this.mc.player.playerScreenHandler) {
            int containerSize = containerHandler.slots.size() - 36;
            return invSlot <= 8 ? containerSize + 27 + invSlot : containerSize + (invSlot - 9);
        }
        return invSlot <= 8 ? 36 + invSlot : invSlot;
    }

    private int getSyncId(ScreenHandler containerHandler) { // was: Q90GLXQ0Pef(ScreenHandler)
        return containerHandler != null && containerHandler != this.mc.player.playerScreenHandler
            ? containerHandler.syncId
            : this.mc.player.playerScreenHandler.syncId;
    }

    /** Finds an empty, non-kit inventory slot (hotbar last), or -1. */
    private int findFreeSlot(int[] invCount, Set<Integer> kitSlots, int exclude) { // was: FvaNWO(int[],Set,int)
        for (int i = 35; i >= 9; i--) {
            if (i != exclude && !kitSlots.contains(i) && invCount[i] == 0) return i;
        }
        for (int i = 8; i >= 0; i--) {
            if (i != exclude && !kitSlots.contains(i) && invCount[i] == 0) return i;
        }
        return -1;
    }

    /** Saves the current inventory (slots 0-35) as a kit JSON file and caches it. */
    public void saveKit(String kitName) { // was: FvaNWO(String)
        if (kitName == null || kitName.isEmpty()) {
            this.info("Set a kit name first.");
            return;
        }
        if (this.mc.player == null) return;

        Map<String, String> kitData = new LinkedHashMap<>();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                kitData.put(String.valueOf(i), Registries.ITEM.getId(stack.getItem()).toString());
            }
        }

        File file = this.kitFile(kitName);
        if (file == null) return;
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            new Gson().toJson(kitData, writer);
        } catch (IOException e) {
            this.error("Failed to save kit: %s", e.getMessage());
            return;
        }
        HashMap<Integer, Item> itemMap = this.parseKit(kitData);
        this.kitCache.put(kitName, itemMap);
        this.info("Saved kit '§b%s§r' (§b%d§r slots).", kitName, itemMap.size());
    }

    /** Loads a kit from disk (slot -> item), or null if missing/unreadable. */
    public HashMap<Integer, Item> loadKit(String kitName) { // was: Q90GLXQ0Pef(String)
        File file = this.kitFile(kitName);
        if (file == null || !file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
            HashMap<String, String> raw = new Gson().fromJson(reader, type);
            return raw == null ? null : this.parseKit(raw);
        } catch (Exception e) {
            this.error("Failed to load kit '§b%s§r': %s", kitName, e.getMessage());
            return null;
        }
    }

    /** Parses a raw slot->item-id map into a slot->Item map (slots 0-35 only). */
    private HashMap<Integer, Item> parseKit(Map<String, String> raw) { // was: FvaNWO(Map)
        HashMap<Integer, Item> map = new HashMap<>();
        for (Map.Entry<String, String> e : raw.entrySet()) {
            try {
                int slot = Integer.parseInt(e.getKey());
                if (slot >= 0 && slot <= 35) {
                    Item item = Registries.ITEM.get(Identifier.of(e.getValue()));
                    if (item != Items.AIR) map.put(slot, item);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    /** The JSON file backing a kit name, or null. */
    public File kitFile(String kitName) { // was: psJq59YIbp3Z(String)
        if (kitName == null || kitName.isEmpty()) return null;
        try {
            return new File(new File(new File(MeteorClient.FOLDER, "musheor"), "kits"), kitName + ".json");
        } catch (NullPointerException e) {
            return null;
        }
    }

    /** Clears the in-memory kit cache. */
    public void clearCache() { // was: FvaNWO()
        this.kitCache.clear();
        this.info("Cleared kit cache.");
    }

    /** Deletes a kit from the cache and disk. */
    public void deleteKit(String name) { // was: SOYyh5IPg26f7F(String)
        this.kitCache.remove(name);
        File file = this.kitFile(name);
        if (file != null && file.exists() && file.delete()) {
            this.info("Deleted kit '§b%s§r'.", name);
        }
    }

    /** True if a kit with the given name is cached or exists on disk. */
    public boolean hasKit(String name) { // was: rKbT3Ifwo(String)
        if (this.kitCache.containsKey(name)) return true;
        File file = this.kitFile(name);
        return file != null && file.exists();
    }

    /** Names of all saved kits on disk. */
    public List<String> listKits() { // was: Q90GLXQ0Pef()
        File kitsDir = new File(new File(MeteorClient.FOLDER, "musheor"), "kits");
        if (!kitsDir.exists()) return Collections.emptyList();
        File[] files = kitsDir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return Collections.emptyList();
        List<String> names = new ArrayList<>();
        for (File f : files) names.add(f.getName().replace(".json", ""));
        return names;
    }

    /** A single planned slot move: pick up from {@code from}, place at {@code to} (-1 = quick-move/throw), optionally return leftovers to {@code returnTo}. */
    private record MoveOp(int from, int to, int returnTo) { } // was: local record 1MoveOp (FvaNWO/Q90GLXQ0Pef/psJq59YIbp3Z)

    /** Trigger mode. */ // was: enum RekitMode {FvaNWO, Q90GLXQ0Pef}
    public enum RekitMode { AUTO, ON_KEY }
}
