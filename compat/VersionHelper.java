// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Interface and members were already readable.
package musheor.compat;

import com.google.gson.Gson;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.Method;
import java.util.UUID;
import meteordevelopment.meteorclient.mixin.ContainerComponentAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.collection.DefaultedList;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * Version-abstraction layer for Minecraft APIs whose signatures shift between game versions
 * (NBT access, sounds, skin textures, player pos, tab-list UUIDs, etc.). A single
 * {@code VersionHelperImpl} is installed at startup; all addon code calls {@link #get()}.
 */
public interface VersionHelper {
    Gson getGson();

    static VersionHelper get() {
        return VersionHelperHolder.INSTANCE;
    }

    Vec3d getPlayerPos();

    static void setInstance(VersionHelper instance) {
        VersionHelperHolder.INSTANCE = instance;
    }

    NbtCompound getCompound(NbtCompound tag, String key);

    NbtList getList(NbtCompound tag, String key, int type);

    String getString(NbtCompound tag, String key);

    int getInt(NbtCompound tag, String key, int fallback);

    boolean hasControlDown();

    default boolean supportsHudButtons() {
        return true;
    }

    void sendCommand(String command);

    boolean isPickaxe(ItemStack stack);

    boolean isTool(ItemStack stack);

    void playSoundPlayer(SoundEvent sound);

    DefaultedList<ItemStack> getStacks(ContainerComponentAccessor accessor);

    String getName(CommandContext<?> context);

    UUID getUUID(PlayerListEntry entry);

    boolean onSameServer(String playerName);

    void syncInventory();

    void interactEntityAt(Entity entity, Hand hand);

    void registerSkinTexture(NativeImage image, String name, Identifier id);

    void drawSkinTexture(Identifier id, float x, float y, float width, float height);

    void renderColorRenderer();

    /** Best-effort read of the render camera position (reflected across version accessor names), or the player pos. */
    default Vec3d getCameraPos() {
        try {
            Object cam = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
            if (cam == null) return Vec3d.ZERO;
            for (String name : new String[]{"getPos", "getPosition"}) {
                try {
                    Method m = cam.getClass().getMethod(name);
                    if (m.invoke(cam) instanceof Vec3d v) return v;
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player != null ? new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()) : Vec3d.ZERO;
    }

    class VersionHelperHolder {
        static VersionHelper INSTANCE;
    }
}
