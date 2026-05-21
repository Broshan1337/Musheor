// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.BlockPos;
import net.minecraft.PlayerAbilities;
import net.minecraft.Vec3d;
import net.minecraft.BlockEntity;
import net.minecraft.class_2595;
import net.minecraft.class_2611;
import net.minecraft.class_2627;
import net.minecraft.class_2646;
import net.minecraft.MinecraftClient;
import net.minecraft.class_3719;

public class DepthInteract
extends Module {
    public static DepthInteract INSTANCE;
    private final MinecraftClient RqrnBAk2kmqEg = MinecraftClient.getInstance();
    public BlockPos mcAmeo = null;

    public DepthInteract() {
        super(musheor.MAIN, "depth-interact", "Interact with containers through blocks.");
        INSTANCE = this;
    }

    public void onDeactivate() {
        this.mcAmeo = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.RqrnBAk2kmqEg.player == null || this.RqrnBAk2kmqEg.world == null) {
            return;
        }
        this.mcAmeo = this.UgB10d(this.RqrnBAk2kmqEg.player.method_55754());
    }

    private BlockPos UgB10d(double d) {
        Vec3d Vec3d2 = this.RqrnBAk2kmqEg.player.method_5836(1.0f);
        Vec3d Vec3d3 = this.RqrnBAk2kmqEg.player.method_5828(1.0f);
        for (double d2 = 0.0; d2 <= d; d2 += 0.1) {
            BlockEntity BlockEntity2;
            Vec3d Vec3d4 = Vec3d2.method_1019(Vec3d3.method_1021(d2));
            BlockPos BlockPos2 = BlockPos.method_49638((PlayerAbilities)Vec3d4);
            if (!this.RqrnBAk2kmqEg.world.method_22340(BlockPos2) || !this.jOdDDFXSeWl4(BlockEntity2 = this.RqrnBAk2kmqEg.world.method_8321(BlockPos2))) continue;
            return BlockPos2;
        }
        return null;
    }

    private boolean jOdDDFXSeWl4(BlockEntity BlockEntity2) {
        return BlockEntity2 instanceof class_2595 || BlockEntity2 instanceof class_2646 || BlockEntity2 instanceof class_3719 || BlockEntity2 instanceof class_2627 || BlockEntity2 instanceof class_2611;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (this.mcAmeo == null || this.RqrnBAk2kmqEg.world == null) {
            return;
        }
        render3DEvent.renderer.box(this.mcAmeo, Color.CYAN, Color.CYAN, ShapeMode.Lines, 0);
    }
}

