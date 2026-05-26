// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.registry.Registries;

public class ReKit
extends Module {
    private final SettingGroup sgGeneral;
    private final MinecraftClient mc;
    public final Setting<RekitMode> rekitMode;
    public final Setting<Keybind> rekitKeybind;
    public final Setting<String> loadout;
    private final HashMap<String, HashMap<Integer, Item>> kitCache;
    private boolean keyWasPressed;
    private int containerDelay;
    private ScreenHandler pendingContainer;

    public ReKit() {
        super(musheor.MAIN, "rekit", "Sorts inventory to match a saved kit loadout when opening a container or pressing a keybind.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mc = MinecraftClient.getInstance();
        this.rekitMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Auto = triggers on container open. OnKey = triggers on keybind.")).defaultValue((Object)RekitMode.Auto)).build());
        this.rekitKeybind = this.sgGeneral.add((Setting)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("key")).description("Keybind to trigger rekit.")).defaultValue((Object)Keybind.none())).visible(() -> this.rekitMode.get() == RekitMode.OnKey)).build());
        this.loadout = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("kit-name")).description("Name of the active kit. Leave blank to disable.")).defaultValue((Object)"")).build());
        this.kitCache = new HashMap();
        this.keyWasPressed = false;
        this.containerDelay = -1;
        this.pendingContainer = null;
    }

    public WWidget getWidget(GuiTheme guiTheme) {
        WVerticalList wVerticalList = guiTheme.verticalList();
        WButton wButton = (WButton)wVerticalList.add((WWidget)guiTheme.button("Save current loadout")).widget();
        WButton wButton2 = (WButton)wVerticalList.add((WWidget)guiTheme.button("Clear kit cache")).widget();
        wButton.action = () -> this.saveCurrentLoadout((String)this.loadout.get());
        wButton2.action = this::clearKitCache;
        return wVerticalList;
    }

    public void onActivate() {
        String string = (String)this.loadout.get();
        if (string.isEmpty()) {
            this.info("No kit selected.", new Object[0]);
            return;
        }
        HashMap<Integer, Item> hashMap = this.loadKitFromFile(string);
        if (hashMap == null) {
            this.info("Kit '\u00a7b%s\u00a7r' not found. Use \u00a7b.kit create\u00a7r to save one.", new Object[]{string});
        } else {
            this.kitCache.put(string, hashMap);
            this.info("Loaded kit '\u00a7b%s\u00a7r' (\u00a7b%d\u00a7r slots).", new Object[]{string, hashMap.size()});
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent openScreenEvent) {
        if (this.rekitMode.get() != RekitMode.Auto) {
            return;
        }
        if (this.mc.player == null) {
            return;
        }
        if (!(openScreenEvent.screen instanceof HandledScreen)) {
            return;
        }
        if (openScreenEvent.screen instanceof CreativeInventoryScreen) {
            return;
        }
        ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
        if (screenHandler == null || screenHandler == this.mc.player.playerScreenHandler) {
            return;
        }
        this.pendingContainer = screenHandler;
        this.containerDelay = 4;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null) {
            return;
        }
        if (this.containerDelay > 0 && --this.containerDelay == 0) {
            ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
            if (screenHandler != null && screenHandler == this.pendingContainer) {
                this.applyKit(this.pendingContainer);
            }
            this.pendingContainer = null;
        }
        if (this.rekitMode.get() == RekitMode.OnKey) {
            boolean bl = ((Keybind)this.rekitKeybind.get()).isPressed();
            if (bl && !this.keyWasPressed) {
                this.keyWasPressed = true;
                ScreenHandler containerScreen = null;
                ScreenHandler sh = this.mc.player.currentScreenHandler;
                if (this.mc.currentScreen instanceof HandledScreen && sh != null && sh != this.mc.player.playerScreenHandler) {
                    containerScreen = sh;
                }
                this.applyKit(containerScreen);
            } else if (!bl) {
                this.keyWasPressed = false;
            }
        }
    }

    public void applyKit(ScreenHandler screenHandler) {
        if (this.mc.player == null) {
            return;
        }
        String string = (String)this.loadout.get();
        if (string.isEmpty()) {
            return;
        }
        HashMap<Integer, Item> hashMap = this.kitCache.computeIfAbsent(string, this::loadKitFromFile);
        if (hashMap == null) {
            this.info("Kit '\u00a7b%s\u00a7r' not found.", new Object[]{string});
            return;
        }
        Set<Integer> set = hashMap.keySet();
        for (Map.Entry<Integer, Item> entry : hashMap.entrySet()) {
            int n;
            int n2;
            boolean bl;
            if (!RateController.checkInventoryRate()) {
                return;
            }
            int n3 = (Integer)entry.getKey();
            Item targetItem = (Item)entry.getValue();
            ItemStack currentStack = this.mc.player.getInventory().getStack(n3);
            boolean bl2 = currentStack.getItem() == targetItem;
            boolean bl3 = bl2 && currentStack.getCount() >= currentStack.getMaxCount();
            boolean bl4 = bl = !currentStack.isEmpty() && !bl2;
            if (bl3) continue;
            int n4 = this.findSlotInInventory(targetItem, hashMap);
            if (n4 != -1) {
                if (bl) {
                    n2 = this.findEmptySlot(set, n3);
                    if (n2 != -1) {
                        if (!RateController.checkInventoryRate()) {
                            return;
                        }
                        this.swapInventorySlots(n3, n2, screenHandler);
                    } else {
                        if (screenHandler == null) continue;
                        if (!RateController.checkInventoryRate()) {
                            return;
                        }
                        this.throwFromInventorySlot(n3, screenHandler);
                    }
                }
                if (!RateController.checkInventoryRate()) {
                    return;
                }
                this.swapInventorySlots(n4, n3, screenHandler);
                continue;
            }
            if (screenHandler == null) {
                if (!bl || (n2 = this.findEmptySlot(set, n3)) == -1) continue;
                if (!RateController.checkInventoryRate()) {
                    return;
                }
                this.swapInventorySlots(n3, n2, screenHandler);
                continue;
            }
            n2 = this.findSlotInContainer(targetItem, screenHandler);
            if (n2 != -1) {
                if (bl) {
                    n = this.inventorySlotToContainerIndex(n3, screenHandler);
                    this.mc.interactionManager.clickSlot(screenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
                    this.mc.interactionManager.clickSlot(screenHandler.syncId, n2, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
                    this.mc.interactionManager.clickSlot(screenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
                    continue;
                }
                this.moveContainerSlotToInventory(n2, n3, screenHandler);
                continue;
            }
            if (!bl) continue;
            n = this.findEmptySlot(set, n3);
            if (n != -1) {
                if (!RateController.checkInventoryRate()) {
                    return;
                }
                this.swapInventorySlots(n3, n, screenHandler);
                continue;
            }
            if (!RateController.checkInventoryRate()) {
                return;
            }
            this.throwFromInventorySlot(n3, screenHandler);
        }
    }

    private void swapInventorySlots(int n, int n2, ScreenHandler screenHandler) {
        boolean bl = n <= 8;
        boolean bl2 = n2 <= 8;
        ItemStack ItemStack2 = this.mc.player.getInventory().getStack(n2);
        if (bl != bl2 && ItemStack2.isEmpty()) {
            int n3 = bl ? n : n2;
            int n4 = bl ? n2 : n;
            this.mc.interactionManager.clickSlot(this.getContainerSyncId(screenHandler), this.inventorySlotToContainerIndex(n4, screenHandler), n3, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
            return;
        }
        int n5 = this.getContainerSyncId(screenHandler);
        int n6 = this.inventorySlotToContainerIndex(n, screenHandler);
        int n7 = this.inventorySlotToContainerIndex(n2, screenHandler);
        ItemStack ItemStack3 = this.mc.player.getInventory().getStack(n);
        int n8 = 0;
        if (!ItemStack2.isEmpty()) {
            int n9 = ItemStack2.getMaxCount() - ItemStack2.getCount();
            n8 = Math.max(0, ItemStack3.getCount() - n9);
        }
        this.mc.interactionManager.clickSlot(n5, n6, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
        this.mc.interactionManager.clickSlot(n5, n7, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
        if (n8 > 0 && RateController.checkInventoryRate()) {
            this.mc.interactionManager.clickSlot(n5, n6, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
        }
    }

    private void moveContainerSlotToInventory(int n, int n2, ScreenHandler screenHandler) {
        ItemStack ItemStack2 = screenHandler.getSlot(n).getStack();
        ItemStack ItemStack3 = this.mc.player.getInventory().getStack(n2);
        int n3 = this.inventorySlotToContainerIndex(n2, screenHandler);
        if (ItemStack3.isEmpty()) {
            this.mc.interactionManager.clickSlot(screenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
            this.mc.interactionManager.clickSlot(screenHandler.syncId, n3, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
        } else {
            int n4 = ItemStack3.getMaxCount() - ItemStack3.getCount();
            int n5 = ItemStack2.getCount() - n4;
            if (n5 <= 0) {
                this.mc.interactionManager.clickSlot(screenHandler.syncId, n, 0, SlotActionType.THROW, (PlayerEntity)this.mc.player);
            } else {
                this.mc.interactionManager.clickSlot(screenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
                this.mc.interactionManager.clickSlot(screenHandler.syncId, n3, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
                if (RateController.checkInventoryRate()) {
                    this.mc.interactionManager.clickSlot(screenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
                }
            }
        }
    }

    private void throwFromInventorySlot(int n, ScreenHandler screenHandler) {
        this.mc.interactionManager.clickSlot(screenHandler.syncId, this.inventorySlotToContainerIndex(n, screenHandler), 0, SlotActionType.THROW, (PlayerEntity)this.mc.player);
    }

    private int inventorySlotToContainerIndex(int n, ScreenHandler screenHandler) {
        if (screenHandler != null && screenHandler != this.mc.player.playerScreenHandler) {
            int n2 = screenHandler.slots.size() - 36;
            return n <= 8 ? n2 + 27 + n : n2 + (n - 9);
        }
        return n <= 8 ? 36 + n : n;
    }

    private int getContainerSyncId(ScreenHandler screenHandler) {
        return screenHandler != null && screenHandler != this.mc.player.playerScreenHandler ? screenHandler.syncId : this.mc.player.playerScreenHandler.syncId;
    }

    private int findSlotInContainer(Item targetItem, ScreenHandler screenHandler) {
        int n = screenHandler.slots.size() - 36;
        for (int i = 0; i < n; ++i) {
            if (screenHandler.getSlot(i).getStack().getItem() != targetItem) continue;
            return i;
        }
        return -1;
    }

    private int findSlotInInventory(Item targetItem, Map<Integer, Item> map) {
        for (int i = 0; i < 36; ++i) {
            Item slotItem = this.mc.player.getInventory().getStack(i).getItem();
            Item mappedItem;
            if (slotItem != targetItem || (mappedItem = map.get(i)) != null && mappedItem == targetItem) continue;
            return i;
        }
        return -1;
    }

    private int findEmptySlot(Set<Integer> set, int n) {
        int n2;
        for (n2 = 35; n2 >= 9; --n2) {
            if (n2 == n || set.contains(n2) || !this.mc.player.getInventory().getStack(n2).isEmpty()) continue;
            return n2;
        }
        for (n2 = 8; n2 >= 0; --n2) {
            if (n2 == n || set.contains(n2) || !this.mc.player.getInventory().getStack(n2).isEmpty()) continue;
            return n2;
        }
        return -1;
    }

    public void saveCurrentLoadout(String string) {
        Object object;
        if (string == null || string.isEmpty()) {
            this.info("Set a kit name first.", new Object[0]);
            return;
        }
        if (this.mc.player == null) {
            return;
        }
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stackInSlot = this.mc.player.getInventory().getStack(i);
            if (stackInSlot.isEmpty()) continue;
            linkedHashMap.put(String.valueOf(i), Registries.ITEM.getId(stackInSlot.getItem()).toString());
        }
        File file = this.getKitFile(string);
        if (file == null) {
            return;
        }
        file.getParentFile().mkdirs();
        try {
            object = new FileWriter(file);
            try {
                new Gson().toJson(linkedHashMap, (Appendable)object);
            }
            finally {
                ((Writer)object).close();
            }
        }
        catch (IOException iOException) {
            this.error("Failed to save kit: %s", new Object[]{iOException.getMessage()});
            return;
        }
        object = this.parseKitMap(linkedHashMap);
        this.kitCache.put(string, (HashMap<Integer, Item>)object);
        this.info("Saved kit '\u00a7b%s\u00a7r' (\u00a7b%d\u00a7r slots).", new Object[]{string, ((HashMap)object).size()});
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public HashMap<Integer, Item> loadKitFromFile(String string) {
        File file = this.getKitFile(string);
        if (file == null) return null;
        if (!file.exists()) {
            return null;
        }
        try (FileReader fileReader = new FileReader(file);){
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap hashMap = (HashMap)new Gson().fromJson((Reader)fileReader, type);
            if (hashMap == null) {
                HashMap<Integer, Item> hashMap2 = null;
                return hashMap2;
            }
            HashMap<Integer, Item> hashMap3 = this.parseKitMap(hashMap);
            return hashMap3;
        }
        catch (Exception exception) {
            this.error("Failed to load kit '\u00a7b%s\u00a7r': %s", new Object[]{string, exception.getMessage()});
            return null;
        }
    }

    private HashMap<Integer, Item> parseKitMap(Map<String, String> map) {
        HashMap<Integer, Item> hashMap = new HashMap<Integer, Item>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                int n = Integer.parseInt(entry.getKey());
                Item item = (Item)Registries.ITEM.get(Identifier.of((String)entry.getValue()));
                if (n < 0 || n > 35 || item == Items.AIR) continue;
                hashMap.put(n, item);
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return hashMap;
    }

    public File getKitFile(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        try {
            return new File(new File(new File(MeteorClient.FOLDER, "musheor"), "kits"), string + ".json");
        }
        catch (NullPointerException nullPointerException) {
            return null;
        }
    }

    public void clearKitCache() {
        this.kitCache.clear();
        this.info("Cleared kit cache.", new Object[0]);
    }

    public void deleteKit(String string) {
        this.kitCache.remove(string);
        File file = this.getKitFile(string);
        if (file != null && file.exists() && file.delete()) {
            this.info("Deleted kit '\u00a7b%s\u00a7r'.", new Object[]{string});
        }
    }

    public boolean kitExists(String string) {
        if (this.kitCache.containsKey(string)) {
            return true;
        }
        File file = this.getKitFile(string);
        return file != null && file.exists();
    }

    public List<String> getKitNames() {
        File file2 = new File(new File(MeteorClient.FOLDER, "musheor"), "kits");
        if (!file2.exists()) {
            return Collections.emptyList();
        }
        File[] fileArray = file2.listFiles((file, string) -> string.endsWith(".json"));
        if (fileArray == null) {
            return Collections.emptyList();
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (File file3 : fileArray) {
            arrayList.add(file3.getName().replace(".json", ""));
        }
        return arrayList;
    }

    public static final class RekitMode
    extends Enum<RekitMode> {
        public static final /* enum */ RekitMode Auto = new RekitMode();
        public static final /* enum */ RekitMode OnKey = new RekitMode();
        private static final /* synthetic */ RekitMode[] $VALUES;

        public static RekitMode[] values() {
            return (RekitMode[])$VALUES.clone();
        }

        public static RekitMode valueOf(String string) {
            return Enum.valueOf(RekitMode.class, string);
        }

        private static /* synthetic */ RekitMode[] LtMW6lcKPv() {
            return new RekitMode[]{Auto, OnKey};
        }

        static {
            $VALUES = RekitMode.LtMW6lcKPv();
        }
    }
}

