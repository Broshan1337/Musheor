// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import com.google.gson.Gson;
import com.mojang.brigadier.context.CommandContext;
import java.io.File;
import java.util.UUID;
import meteordevelopment.meteorclient.mixin.ContainerComponentAccessor;
import net.minecraft.SkinTextures;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.ItemStack;
import net.minecraft.DefaultedList;
import net.minecraft.Vec3d;
import net.minecraft.NbtCompound;
import net.minecraft.NbtList;
import net.minecraft.Identifier;
import net.minecraft.SoundEvent;
import net.minecraft.PlayerListEntry;

public interface VersionHelper {
    public Gson getGson();

    public static VersionHelper get() {
        return VersionHelperHolder.INSTANCE;
    }

    public Vec3d getPlayerPos();

    public static void setInstance(VersionHelper versionHelper) {
        VersionHelperHolder.INSTANCE = versionHelper;
    }

    public NbtCompound getCompound(NbtCompound var1, String var2);

    public NbtList getList(NbtCompound var1, String var2, int var3);

    public String getString(NbtCompound var1, String var2);

    public int getInt(NbtCompound var1, String var2, int var3);

    public boolean hasControlDown();

    default public boolean supportsHudButtons() {
        return true;
    }

    public void sendCommand(String var1);

    public boolean isPickaxe(ItemStack var1);

    public boolean isTool(ItemStack var1);

    public void playSoundPlayer(SoundEvent var1);

    public DefaultedList<ItemStack> getStacks(ContainerComponentAccessor var1);

    public String getName(CommandContext<?> var1);

    public UUID getUUID(PlayerListEntry var1);

    public Object getSchematicFromFile(File var1, String var2);

    public boolean onSameServer(String var1);

    public void syncInventory();

    public void interactEntityAt(Entity var1, InteractionHand var2);

    public void registerSkinTexture(SkinTextures var1, String var2, Identifier var3);

    public void drawSkinTexture(Identifier var1, float var2, float var3, float var4, float var5);

    public void renderColorRenderer();

    public static class VersionHelperHolder {
        static VersionHelper INSTANCE;
    }
}

