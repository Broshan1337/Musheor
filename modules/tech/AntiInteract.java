// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.commands.RestockConfig;
import musheor.musheor;
import net.minecraft.class_2244;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;
import net.minecraft.MinecraftClient;
import net.minecraft.class_4969;

public class AntiInteract
extends Module {
    private final SettingGroup sgGeneral;
    private final MinecraftClient AL6Vddo9zXHx;
    private final Setting<Boolean> beds;
    private final Setting<Boolean> anchors;
    private final Setting<List<Block>> blockList;

    @EventHandler
    private void onInteract(InteractBlockEvent interactBlockEvent) {
        if (this.AL6Vddo9zXHx.player == null || this.AL6Vddo9zXHx.world == null) {
            return;
        }
        BlockPos BlockPos2 = interactBlockEvent.result.method_17777();
        BlockState BlockState2 = this.AL6Vddo9zXHx.world.getBlockState(BlockPos2);
        if (((Boolean)this.beds.get()).booleanValue() && BlockState2.getBlock() instanceof class_2244) {
            interactBlockEvent.cancel();
            ChatUtils.info((String)"\u00a7cPrevented interaction with \u00a7e%s", (Object[])new Object[]{RestockConfig.jOdDDFXSeWl4(BlockState2.getBlock().asItem())});
        } else if (((Boolean)this.anchors.get()).booleanValue() && BlockState2.getBlock() instanceof class_4969) {
            interactBlockEvent.cancel();
            ChatUtils.info((String)"\u00a7cPrevented interaction with \u00a7e%s", (Object[])new Object[]{RestockConfig.jOdDDFXSeWl4(BlockState2.getBlock().asItem())});
        } else if (((List)this.blockList.get()).contains(BlockState2.getBlock())) {
            interactBlockEvent.cancel();
            ChatUtils.info((String)"\u00a7cPrevented interaction with \u00a7e%s", (Object[])new Object[]{RestockConfig.jOdDDFXSeWl4(BlockState2.getBlock().asItem())});
        }
    }

    public AntiInteract() {
        super(musheor.MAIN, "anti-interact", "Prevents you from interacting with certain blocks e.g. beds and respawn anchors");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.AL6Vddo9zXHx = MinecraftClient.getInstance();
        this.beds = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("beds")).description("Stops you from interacting with beds")).defaultValue((Object)true)).build());
        this.anchors = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("respawn-anchors")).description("Stops you from interacting with respawn anchors")).defaultValue((Object)true)).build());
        this.blockList = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("block-list")).description("When enabled, stops you from interacting with beds")).defaultValue(new Block[]{Blocks.LAVA}).build());
    }
}

