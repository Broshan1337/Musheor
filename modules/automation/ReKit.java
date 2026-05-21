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
import net.minecraft.PlayerEntity;
import net.minecraft.Text;
import net.minecraft.ClientPlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Identifier;
import net.minecraft.MinecraftClient;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.Registries;

public class ReKit
extends Module {
    private final SettingGroup sgGeneral;
    private final MinecraftClient BcrtDMyh;
    public final Setting<RekitMode> rekitMode;
    public final Setting<Keybind> rekitKeybind;
    public final Setting<String> loadout;
    private final HashMap<String, HashMap<Integer, ItemStack>> Nx0JPvjp;
    private boolean xkVv33gtDkfg4;
    private int sE4h5W;
    private Text icVv2m6;

    public ReKit() {
        super(musheor.MAIN, "rekit", "Sorts inventory to match a saved kit loadout when opening a container or pressing a keybind.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.BcrtDMyh = MinecraftClient.getInstance();
        this.rekitMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Auto = triggers on container open. OnKey = triggers on keybind.")).defaultValue((Object)RekitMode.df1mByH0u)).build());
        this.rekitKeybind = this.sgGeneral.add((Setting)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("key")).description("Keybind to trigger rekit.")).defaultValue((Object)Keybind.none())).visible(() -> this.rekitMode.get() == RekitMode.jM7Ku65I6T)).build());
        this.loadout = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("kit-name")).description("Name of the active kit. Leave blank to disable.")).defaultValue((Object)"")).build());
        this.Nx0JPvjp = new HashMap();
        this.xkVv33gtDkfg4 = false;
        this.sE4h5W = -1;
        this.icVv2m6 = null;
    }

    public WWidget getWidget(GuiTheme guiTheme) {
        WVerticalList wVerticalList = guiTheme.verticalList();
        WButton wButton = (WButton)wVerticalList.add((WWidget)guiTheme.button("Save current loadout")).widget();
        WButton wButton2 = (WButton)wVerticalList.add((WWidget)guiTheme.button("Clear kit cache")).widget();
        wButton.action = () -> this.TAdu5cndwWu3A1((String)this.loadout.get());
        wButton2.action = this::L8HTTT;
        return wVerticalList;
    }

    public void onActivate() {
        String string = (String)this.loadout.get();
        if (string.isEmpty()) {
            this.info("No kit selected.", new Object[0]);
            return;
        }
        HashMap<Integer, ItemStack> hashMap = this.vgrtgn5(string);
        if (hashMap == null) {
            this.info("Kit '\u00a7b%s\u00a7r' not found. Use \u00a7b.kit create\u00a7r to save one.", new Object[]{string});
        } else {
            this.Nx0JPvjp.put(string, hashMap);
            this.info("Loaded kit '\u00a7b%s\u00a7r' (\u00a7b%d\u00a7r slots).", new Object[]{string, hashMap.size()});
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent openScreenEvent) {
        if (this.rekitMode.get() != RekitMode.df1mByH0u) {
            return;
        }
        if (this.BcrtDMyh.player == null) {
            return;
        }
        if (!(openScreenEvent.screen instanceof class_465)) {
            return;
        }
        if (openScreenEvent.screen instanceof class_490) {
            return;
        }
        Text AbstractClientPlayerEntity = this.BcrtDMyh.player.field_7512;
        if (AbstractClientPlayerEntity == null || AbstractClientPlayerEntity == this.BcrtDMyh.player.field_7498) {
            return;
        }
        this.icVv2m6 = AbstractClientPlayerEntity;
        this.sE4h5W = 4;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.BcrtDMyh.player == null) {
            return;
        }
        if (this.sE4h5W > 0 && --this.sE4h5W == 0) {
            Text AbstractClientPlayerEntity = this.BcrtDMyh.player.field_7512;
            if (AbstractClientPlayerEntity != null && AbstractClientPlayerEntity == this.icVv2m6) {
                this.jOdDDFXSeWl4(this.icVv2m6);
            }
            this.icVv2m6 = null;
        }
        if (this.rekitMode.get() == RekitMode.jM7Ku65I6T) {
            boolean bl = ((Keybind)this.rekitKeybind.get()).isPressed();
            if (bl && !this.xkVv33gtDkfg4) {
                Text Text3;
                this.xkVv33gtDkfg4 = true;
                Text Text4 = null;
                if (this.BcrtDMyh.field_1755 instanceof class_465 && (Text3 = this.BcrtDMyh.player.field_7512) != null && Text3 != this.BcrtDMyh.player.field_7498) {
                    Text4 = Text3;
                }
                this.jOdDDFXSeWl4(Text4);
            } else if (!bl) {
                this.xkVv33gtDkfg4 = false;
            }
        }
    }

    public void jOdDDFXSeWl4(Text AbstractClientPlayerEntity) {
        if (this.BcrtDMyh.player == null) {
            return;
        }
        String string = (String)this.loadout.get();
        if (string.isEmpty()) {
            return;
        }
        HashMap hashMap = this.Nx0JPvjp.computeIfAbsent(string, this::vgrtgn5);
        if (hashMap == null) {
            this.info("Kit '\u00a7b%s\u00a7r' not found.", new Object[]{string});
            return;
        }
        Set<Integer> set = hashMap.keySet();
        for (Map.Entry entry : hashMap.entrySet()) {
            int n;
            int n2;
            boolean bl;
            if (!RateController.OwcAnTXUsd()) {
                return;
            }
            int n3 = (Integer)entry.getKey();
            ItemStack ItemStack2 = (ItemStack)entry.getValue();
            ItemStack ItemStack2 = this.BcrtDMyh.player.getId().method_5438(n3);
            boolean bl2 = ItemStack2.getStack() == ItemStack2;
            boolean bl3 = bl2 && ItemStack2.method_7947() >= ItemStack2.method_7914();
            boolean bl4 = bl = !ItemStack2.setStack() && !bl2;
            if (bl3) continue;
            int n4 = this.jOdDDFXSeWl4(ItemStack2, hashMap);
            if (n4 != -1) {
                if (bl) {
                    n2 = this.jOdDDFXSeWl4(set, n3);
                    if (n2 != -1) {
                        if (!RateController.OwcAnTXUsd()) {
                            return;
                        }
                        this.jOdDDFXSeWl4(n3, n2, AbstractClientPlayerEntity);
                    } else {
                        if (AbstractClientPlayerEntity == null) continue;
                        if (!RateController.OwcAnTXUsd()) {
                            return;
                        }
                        this.jOdDDFXSeWl4(n3, AbstractClientPlayerEntity);
                    }
                }
                if (!RateController.OwcAnTXUsd()) {
                    return;
                }
                this.jOdDDFXSeWl4(n4, n3, AbstractClientPlayerEntity);
                continue;
            }
            if (AbstractClientPlayerEntity == null) {
                if (!bl || (n2 = this.jOdDDFXSeWl4(set, n3)) == -1) continue;
                if (!RateController.OwcAnTXUsd()) {
                    return;
                }
                this.jOdDDFXSeWl4(n3, n2, AbstractClientPlayerEntity);
                continue;
            }
            n2 = this.jOdDDFXSeWl4(ItemStack2, AbstractClientPlayerEntity);
            if (n2 != -1) {
                if (bl) {
                    n = this.mp3zoXQFKUKYj5(n3, AbstractClientPlayerEntity);
                    this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
                    this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n2, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
                    this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
                    continue;
                }
                this.mp3zoXQFKUKYj5(n2, n3, AbstractClientPlayerEntity);
                continue;
            }
            if (!bl) continue;
            n = this.jOdDDFXSeWl4(set, n3);
            if (n != -1) {
                if (!RateController.OwcAnTXUsd()) {
                    return;
                }
                this.jOdDDFXSeWl4(n3, n, AbstractClientPlayerEntity);
                continue;
            }
            if (!RateController.OwcAnTXUsd()) {
                return;
            }
            this.jOdDDFXSeWl4(n3, AbstractClientPlayerEntity);
        }
    }

    private void jOdDDFXSeWl4(int n, int n2, Text AbstractClientPlayerEntity) {
        boolean bl = n <= 8;
        boolean bl2 = n2 <= 8;
        ItemStack ItemStack2 = this.BcrtDMyh.player.getId().method_5438(n2);
        if (bl != bl2 && ItemStack2.setStack()) {
            int n3 = bl ? n : n2;
            int n4 = bl ? n2 : n;
            this.BcrtDMyh.field_1761.method_2906(this.mp3zoXQFKUKYj5(AbstractClientPlayerEntity), this.mp3zoXQFKUKYj5(n4, AbstractClientPlayerEntity), n3, ClientPlayerEntity.field_7791, (PlayerEntity)this.BcrtDMyh.player);
            return;
        }
        int n5 = this.mp3zoXQFKUKYj5(AbstractClientPlayerEntity);
        int n6 = this.mp3zoXQFKUKYj5(n, AbstractClientPlayerEntity);
        int n7 = this.mp3zoXQFKUKYj5(n2, AbstractClientPlayerEntity);
        ItemStack ItemStack3 = this.BcrtDMyh.player.getId().method_5438(n);
        int n8 = 0;
        if (!ItemStack2.setStack()) {
            int n9 = ItemStack2.method_7914() - ItemStack2.method_7947();
            n8 = Math.max(0, ItemStack3.method_7947() - n9);
        }
        this.BcrtDMyh.field_1761.method_2906(n5, n6, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
        this.BcrtDMyh.field_1761.method_2906(n5, n7, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
        if (n8 > 0 && RateController.OwcAnTXUsd()) {
            this.BcrtDMyh.field_1761.method_2906(n5, n6, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
        }
    }

    private void mp3zoXQFKUKYj5(int n, int n2, Text AbstractClientPlayerEntity) {
        ItemStack ItemStack2 = AbstractClientPlayerEntity.method_7611(n).method_7677();
        ItemStack ItemStack3 = this.BcrtDMyh.player.getId().method_5438(n2);
        int n3 = this.mp3zoXQFKUKYj5(n2, AbstractClientPlayerEntity);
        if (ItemStack3.setStack()) {
            this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
            this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n3, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
        } else {
            int n4 = ItemStack3.method_7914() - ItemStack3.method_7947();
            int n5 = ItemStack2.method_7947() - n4;
            if (n5 <= 0) {
                this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n, 0, ClientPlayerEntity.field_7794, (PlayerEntity)this.BcrtDMyh.player);
            } else {
                this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
                this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n3, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
                if (RateController.OwcAnTXUsd()) {
                    this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, n, 0, ClientPlayerEntity.field_7790, (PlayerEntity)this.BcrtDMyh.player);
                }
            }
        }
    }

    private void jOdDDFXSeWl4(int n, Text AbstractClientPlayerEntity) {
        this.BcrtDMyh.field_1761.method_2906(AbstractClientPlayerEntity.field_7763, this.mp3zoXQFKUKYj5(n, AbstractClientPlayerEntity), 0, ClientPlayerEntity.field_7794, (PlayerEntity)this.BcrtDMyh.player);
    }

    private int mp3zoXQFKUKYj5(int n, Text AbstractClientPlayerEntity) {
        if (AbstractClientPlayerEntity != null && AbstractClientPlayerEntity != this.BcrtDMyh.player.field_7498) {
            int n2 = AbstractClientPlayerEntity.field_7761.size() - 36;
            return n <= 8 ? n2 + 27 + n : n2 + (n - 9);
        }
        return n <= 8 ? 36 + n : n;
    }

    private int mp3zoXQFKUKYj5(Text AbstractClientPlayerEntity) {
        return AbstractClientPlayerEntity != null && AbstractClientPlayerEntity != this.BcrtDMyh.player.field_7498 ? AbstractClientPlayerEntity.field_7763 : this.BcrtDMyh.player.field_7498.field_7763;
    }

    private int jOdDDFXSeWl4(ItemStack ItemStack2, Text AbstractClientPlayerEntity) {
        int n = AbstractClientPlayerEntity.field_7761.size() - 36;
        for (int i = 0; i < n; ++i) {
            if (AbstractClientPlayerEntity.method_7611(i).method_7677().getStack() != ItemStack2) continue;
            return i;
        }
        return -1;
    }

    private int jOdDDFXSeWl4(ItemStack ItemStack2, Map<Integer, ItemStack> map) {
        for (int i = 0; i < 36; ++i) {
            ItemStack ItemStack3;
            if (this.BcrtDMyh.player.getId().method_5438(i).getStack() != ItemStack2 || (ItemStack3 = map.get(i)) != null && ItemStack3 == ItemStack2) continue;
            return i;
        }
        return -1;
    }

    private int jOdDDFXSeWl4(Set<Integer> set, int n) {
        int n2;
        for (n2 = 35; n2 >= 9; --n2) {
            if (n2 == n || set.contains(n2) || !this.BcrtDMyh.player.getId().method_5438(n2).setStack()) continue;
            return n2;
        }
        for (n2 = 8; n2 >= 0; --n2) {
            if (n2 == n || set.contains(n2) || !this.BcrtDMyh.player.getId().method_5438(n2).setStack()) continue;
            return n2;
        }
        return -1;
    }

    public void TAdu5cndwWu3A1(String string) {
        Object object;
        if (string == null || string.isEmpty()) {
            this.info("Set a kit name first.", new Object[0]);
            return;
        }
        if (this.BcrtDMyh.player == null) {
            return;
        }
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < 36; ++i) {
            object = this.BcrtDMyh.player.getId().method_5438(i);
            if (object.setStack()) continue;
            linkedHashMap.put(String.valueOf(i), Registries.field_41178.getId((Object)object.getStack()).toString());
        }
        File file = this.VYEwzRq(string);
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
        object = this.jOdDDFXSeWl4(linkedHashMap);
        this.Nx0JPvjp.put(string, (HashMap<Integer, ItemStack>)object);
        this.info("Saved kit '\u00a7b%s\u00a7r' (\u00a7b%d\u00a7r slots).", new Object[]{string, ((HashMap)object).size()});
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public HashMap<Integer, ItemStack> vgrtgn5(String string) {
        File file = this.VYEwzRq(string);
        if (file == null) return null;
        if (!file.exists()) {
            return null;
        }
        try (FileReader fileReader = new FileReader(file);){
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap hashMap = (HashMap)new Gson().fromJson((Reader)fileReader, type);
            if (hashMap == null) {
                HashMap<Integer, ItemStack> hashMap2 = null;
                return hashMap2;
            }
            HashMap<Integer, ItemStack> hashMap3 = this.jOdDDFXSeWl4(hashMap);
            return hashMap3;
        }
        catch (Exception exception) {
            this.error("Failed to load kit '\u00a7b%s\u00a7r': %s", new Object[]{string, exception.getMessage()});
            return null;
        }
    }

    private HashMap<Integer, ItemStack> jOdDDFXSeWl4(Map<String, String> map) {
        HashMap<Integer, ItemStack> hashMap = new HashMap<Integer, ItemStack>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                ItemStack ItemStack2;
                int n = Integer.parseInt(entry.getKey());
                if (n < 0 || n > 35 || (ItemStack2 = (ItemStack)Registries.field_41178.method_63535(Identifier.method_60654((String)entry.getValue()))) == Items.field_8162) continue;
                hashMap.put(n, ItemStack2);
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return hashMap;
    }

    public File VYEwzRq(String string) {
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

    public void L8HTTT() {
        this.Nx0JPvjp.clear();
        this.info("Cleared kit cache.", new Object[0]);
    }

    public void UgB10d(String string) {
        this.Nx0JPvjp.remove(string);
        File file = this.VYEwzRq(string);
        if (file != null && file.exists() && file.delete()) {
            this.info("Deleted kit '\u00a7b%s\u00a7r'.", new Object[]{string});
        }
    }

    public boolean KP44bk(String string) {
        if (this.Nx0JPvjp.containsKey(string)) {
            return true;
        }
        File file = this.VYEwzRq(string);
        return file != null && file.exists();
    }

    public List<String> vV6cbpE7KWBI() {
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
        public static final /* enum */ RekitMode df1mByH0u = new RekitMode();
        public static final /* enum */ RekitMode jM7Ku65I6T = new RekitMode();
        private static final /* synthetic */ RekitMode[] SNCRr7EZFUj;

        public static RekitMode[] values() {
            return (RekitMode[])SNCRr7EZFUj.clone();
        }

        public static RekitMode valueOf(String string) {
            return Enum.valueOf(RekitMode.class, string);
        }

        private static /* synthetic */ RekitMode[] LtMW6lcKPv() {
            return new RekitMode[]{df1mByH0u, jM7Ku65I6T};
        }

        static {
            SNCRr7EZFUj = RekitMode.LtMW6lcKPv();
        }
    }
}

