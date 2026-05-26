// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.FileType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.mixin.ContainerComponentAccessor;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.utils.render.color.Color;
import musheor.compat.VersionHelper;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class VersionHelperImpl
implements VersionHelper {
    private static final Gson GSON_INSTANCE = new GsonBuilder().setPrettyPrinting().create();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public Gson getGson() {
        return GSON_INSTANCE;
    }

    public static void init() {
        VersionHelper.setInstance(new VersionHelperImpl());
    }

    @Override
    public Vec3d getPlayerPos() {
        return VersionHelperImpl.mc.player.getEntityPos();
    }

    @Override
    public void playSoundPlayer(SoundEvent soundEvent) {
        VersionHelperImpl.mc.player.playSound(soundEvent, 1.0f, 1.0f);
    }

    @Override
    public boolean isPickaxe(ItemStack stack) {
        return stack.isIn(ItemTags.PICKAXES);
    }

    @Override
    public boolean isTool(ItemStack stack) {
        return stack.getComponents().contains(DataComponentTypes.TOOL);
    }

    @Override
    public DefaultedList<ItemStack> getStacks(ContainerComponentAccessor containerComponentAccessor) {
        return containerComponentAccessor.meteor$getStacks();
    }

    @Override
    public NbtCompound getCompound(NbtCompound nbt, String key) {
        return nbt.getCompoundOrEmpty(key);
    }

    @Override
    public NbtList getList(NbtCompound nbt, String key, int type) {
        return nbt.getList(key).orElse(new NbtList());
    }

    @Override
    public String getName(CommandContext<?> commandContext) {
        return PlayerListEntryArgumentType.get(commandContext).getProfile().name();
    }

    @Override
    public UUID getUUID(PlayerListEntry entry) {
        return entry.getProfile().id();
    }

    public LitematicaSchematic getSchematicFromFile(File file, String name) {
        return LitematicaSchematic.createFromFile((Path) file.getParentFile().toPath(), (String) name, (FileType) FileType.fromFile((File) file));
    }

    @Override
    public boolean onSameServer(String playerName) {
        return mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerList().stream()
            .anyMatch(e -> e.getProfile().name().equalsIgnoreCase(playerName));
    }

    @Override
    public void syncInventory() {
        if (VersionHelperImpl.mc.player == null || VersionHelperImpl.mc.interactionManager == null) {
            return;
        }
        mc.getNetworkHandler().sendPacket((Packet) new ClickSlotC2SPacket(
            VersionHelperImpl.mc.player.currentScreenHandler.syncId,
            Integer.MAX_VALUE, 0, 3,
            SlotActionType.PICKUP,
            Int2ObjectMaps.emptyMap(),
            ItemStackHash.EMPTY));
    }

    @Override
    public void interactEntityAt(Entity entity, Hand hand) {
        VersionHelperImpl.mc.interactionManager.sendSequencedPacket(VersionHelperImpl.mc.world,
            n -> PlayerInteractEntityC2SPacket.interactAt((Entity) entity, (boolean) false, (Hand) hand, (Vec3d) entity.getEntityPos()));
    }

    @Override
    public String getString(NbtCompound nbt, String key) {
        return nbt.getString(key).orElse("");
    }

    @Override
    public int getInt(NbtCompound nbt, String key, int defaultValue) {
        return nbt.getInt(key).orElse(defaultValue);
    }

    @Override
    public boolean hasControlDown() {
        long handle = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long) handle, (int) 341) == 1 || GLFW.glfwGetKey((long) handle, (int) 345) == 1;
    }

    @Override
    public boolean supportsHudButtons() {
        return false;
    }

    @Override
    public void sendCommand(String command) {
        if (VersionHelperImpl.mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        mc.getNetworkHandler().sendChatCommand(command);
    }

    @Override
    public void registerSkinTexture(SkinTextures skinTextures, String data, Identifier id) {
        NativeImageBackedTexture nativeTexture = new NativeImageBackedTexture(() -> data, skinTextures);
        mc.getTextureManager().registerTexture(id, (AbstractTexture) nativeTexture);
        nativeTexture.upload();
    }

    @Override
    public void drawSkinTexture(Identifier id, float x, float y, float width, float height) {
        AbstractTexture texture = mc.getTextureManager().getTexture(id);
        if (texture == null) {
            return;
        }
        GpuTextureView gpuTextureView = texture.getGlTextureView();
        if (gpuTextureView == null) {
            return;
        }
        GpuSampler sampler = RenderSystem.getSamplerCache().get(FilterMode.NEAREST);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad((double) x, (double) y, (double) width, (double) height, Color.WHITE);
        Renderer2D.TEXTURE.render(gpuTextureView, sampler);
    }

    @Override
    public void renderColorRenderer() {
        Renderer2D.COLOR.render();
    }
}