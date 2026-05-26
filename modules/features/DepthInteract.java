// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DepthInteract
extends Module {
    public static DepthInteract INSTANCE;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    public BlockPos targetPos = null;

    public DepthInteract() {
        super(musheor.MAIN, "depth-interact", "Interact with containers through blocks.");
        INSTANCE = this;
    }

    public void onDeactivate() {
        this.targetPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.targetPos = this.findInteractableContainer(this.mc.player.getBlockInteractionRange());
    }

    private BlockPos findInteractableContainer(double d) {
        Vec3d rotationVec = this.mc.player.getRotationVec(1.0f);
        Vec3d cameraPos = this.mc.player.getCameraPosVec(1.0f);
        for (double d2 = 0.0; d2 <= d; d2 += 0.1) {
            BlockEntity blockEntity;
            Vec3d pos = cameraPos.add(rotationVec.multiply(d2));
            BlockPos blockPos = BlockPos.ofFloored(pos);
            if (!this.mc.world.isChunkLoaded(blockPos) || !this.isInteractableContainer(blockEntity = this.mc.world.getBlockEntity(blockPos))) continue;
            return blockPos;
        }
        return null;
    }

    private boolean isInteractableContainer(BlockEntity blockEntity) {
        return blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity || blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ShulkerBoxBlockEntity || blockEntity instanceof EnderChestBlockEntity;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (this.targetPos == null || this.mc.world == null) {
            return;
        }
        render3DEvent.renderer.box(this.targetPos, Color.CYAN, Color.CYAN, ShapeMode.Lines, 0);
    }
}

