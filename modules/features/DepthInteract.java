// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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

/**
 * "depth-interact" — ray-marches the player's look vector and exposes the first container
 * block entity (chest/trapped chest/barrel/shulker/ender chest) it passes through, so it
 * can be interacted with even when occluded by other blocks. The found position is rendered
 * and read by the interaction mixin.
 */
public class DepthInteract extends Module {
    public static DepthInteract INSTANCE;
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: Q90GLXQ0Pef
    public BlockPos targetPos = null;                                 // was: FvaNWO

    public DepthInteract() {
        super(musheor.MAIN, "depth-interact", "Interact with containers through blocks.");
        INSTANCE = this;
    }

    @Override
    public void onDeactivate() {
        this.targetPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) { // was: FvaNWO(Post)
        if (this.mc.player != null && this.mc.world != null) {
            this.targetPos = this.raycastContainer(this.mc.player.getBlockInteractionRange());
        }
    }

    /** Steps along the look vector up to {@code maxDistance}, returning the first container-BE position. */
    private BlockPos raycastContainer(double maxDistance) { // was: FvaNWO(double)
        Vec3d start = this.mc.player.getCameraPosVec(1.0F);
        Vec3d look = this.mc.player.getRotationVec(1.0F);
        for (double d = 0.0; d <= maxDistance; d += 0.1) {
            Vec3d point = start.add(look.multiply(d));
            BlockPos pos = BlockPos.ofFloored(point);
            if (this.mc.world.isChunkLoaded(pos)) {
                BlockEntity be = this.mc.world.getBlockEntity(pos);
                if (this.isInteractableContainer(be)) return pos;
            }
        }
        return null;
    }

    /** True for container block entities that can be depth-interacted. */
    private boolean isInteractableContainer(BlockEntity be) { // was: FvaNWO(BlockEntity)
        return be instanceof ChestBlockEntity || be instanceof TrappedChestBlockEntity || be instanceof BarrelBlockEntity
            || be instanceof ShulkerBoxBlockEntity || be instanceof EnderChestBlockEntity;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.targetPos != null && this.mc.world != null) {
            event.renderer.box(this.targetPos, Color.CYAN, Color.CYAN, ShapeMode.Lines, 0);
        }
    }
}
