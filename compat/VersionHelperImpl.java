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
import net.minecraft.SkinTextures;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10938;
import net.minecraft.class_12137;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.ClientPlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.DefaultedList;
import net.minecraft.Vec3d;
import net.minecraft.NbtCompound;
import net.minecraft.NbtList;
import net.minecraft.Packet;
import net.minecraft.class_2813;
import net.minecraft.class_2824;
import net.minecraft.Identifier;
import net.minecraft.MinecraftClient;
import net.minecraft.SoundEvent;
import net.minecraft.class_3489;
import net.minecraft.PlayerListEntry;
import net.minecraft.MutableText;
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
        return VersionHelperImpl.mc.player.method_73189();
    }

    @Override
    public void playSoundPlayer(SoundEvent SoundEvent2) {
        VersionHelperImpl.mc.player.method_5783(SoundEvent2, 1.0f, 1.0f);
    }

    @Override
    public boolean isPickaxe(ItemStack ItemStack2) {
        return ItemStack2.method_31573(class_3489.field_42614);
    }

    @Override
    public boolean isTool(ItemStack ItemStack2) {
        return ItemStack2.method_57353().method_57832(MutableText.field_50077);
    }

    @Override
    public DefaultedList<ItemStack> getStacks(ContainerComponentAccessor containerComponentAccessor) {
        return containerComponentAccessor.meteor$getStacks();
    }

    @Override
    public NbtCompound getCompound(NbtCompound NbtCompound2, String string) {
        return NbtCompound2.method_68568(string);
    }

    @Override
    public NbtList getList(NbtCompound NbtCompound2, String string, int n) {
        return NbtCompound2.method_10554(string).orElse(new NbtList());
    }

    @Override
    public String getName(CommandContext<?> commandContext) {
        return PlayerListEntryArgumentType.get(commandContext).getProfile().name();
    }

    @Override
    public UUID getUUID(PlayerListEntry PlayerListEntry2) {
        return PlayerListEntry2.getProfile().id();
    }

    public LitematicaSchematic getSchematicFromFile(File file, String string) {
        return LitematicaSchematic.createFromFile((Path)file.getParentFile().toPath(), (String)string, (FileType)FileType.fromFile((File)file));
    }

    @Override
    public boolean onSameServer(String string) {
        return mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerList().stream().anyMatch(PlayerListEntry2 -> PlayerListEntry2.getProfile().name().equalsIgnoreCase(string));
    }

    @Override
    public void syncInventory() {
        if (VersionHelperImpl.mc.player == null || VersionHelperImpl.mc.field_1761 == null) {
            return;
        }
        mc.getNetworkHandler().method_52787((Packet)new class_2813(VersionHelperImpl.mc.player.field_7512.field_7763, Integer.MAX_VALUE, 0, 3, ClientPlayerEntity.field_7790, Int2ObjectMaps.emptyMap(), class_10938.field_58176));
    }

    @Override
    public void interactEntityAt(Entity Entity2, InteractionHand InteractionHand2) {
        VersionHelperImpl.mc.field_1761.method_41931(VersionHelperImpl.mc.world, n -> class_2824.method_34208((Entity)Entity2, (boolean)false, (InteractionHand)InteractionHand2, (Vec3d)Entity2.method_73189()));
    }

    @Override
    public String getString(NbtCompound NbtCompound2, String string) {
        return NbtCompound2.method_10558(string).orElse("");
    }

    @Override
    public int getInt(NbtCompound NbtCompound2, String string, int n) {
        return NbtCompound2.method_10550(string).orElse(n);
    }

    @Override
    public boolean hasControlDown() {
        long l = mc.method_22683().method_4490();
        return GLFW.glfwGetKey((long)l, (int)341) == 1 || GLFW.glfwGetKey((long)l, (int)345) == 1;
    }

    @Override
    public boolean supportsHudButtons() {
        return false;
    }

    @Override
    public void sendCommand(String string) {
        if (VersionHelperImpl.mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        mc.getNetworkHandler().method_45730(string);
    }

    @Override
    public void registerSkinTexture(SkinTextures SkinTextures2, String string, Identifier Identifier2) {
        class_1043 class_10432 = new class_1043(() -> string, SkinTextures2);
        mc.method_1531().method_4616(Identifier2, (class_1044)class_10432);
        class_10432.method_4524();
    }

    @Override
    public void drawSkinTexture(Identifier Identifier2, float f, float f2, float f3, float f4) {
        class_1044 class_10442 = mc.method_1531().method_4619(Identifier2);
        if (class_10442 == null) {
            return;
        }
        GpuTextureView gpuTextureView = class_10442.method_71659();
        if (gpuTextureView == null) {
            return;
        }
        class_12137 class_121372 = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad((double)f, (double)f2, (double)f3, (double)f4, Color.WHITE);
        Renderer2D.TEXTURE.render(gpuTextureView, class_121372);
    }

    @Override
    public void renderColorRenderer() {
        Renderer2D.COLOR.render();
    }
}

