// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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
import musheor.musheor;
import musheor.commands.RestockConfig;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * "anti-interact" — cancels block interactions with beds, respawn anchors, and any
 * user-listed blocks (to avoid accidentally blowing yourself up on 2b2t), announcing each
 * prevented interaction in chat.
 */
public class AntiInteract extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();   // was: FvaNWO
    private final MinecraftClient mc = MinecraftClient.getInstance();         // was: Q90GLXQ0Pef

    private final Setting<Boolean> beds = sgGeneral.add(new BoolSetting.Builder() // was: psJq59YIbp3Z
        .name("beds").description("Stops you from interacting with beds").defaultValue(true).build());
    private final Setting<Boolean> respawnAnchors = sgGeneral.add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("respawn-anchors").description("Stops you from interacting with respawn anchors").defaultValue(true).build());
    private final Setting<List<Block>> blockList = sgGeneral.add(new BlockListSetting.Builder() // was: rKbT3Ifwo
        .name("block-list").description("When enabled, stops you from interacting with beds").defaultValue(Blocks.AIR).build());

    public AntiInteract() {
        super(musheor.MAIN, "anti-interact", "Prevents you from interacting with certain blocks e.g. beds and respawn anchors");
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) { // was: FvaNWO(InteractBlockEvent)
        if (this.mc.player == null || this.mc.world == null) return;
        BlockPos pos = event.result.getBlockPos();
        BlockState state = this.mc.world.getBlockState(pos);
        if (this.beds.get() && state.getBlock() instanceof BedBlock) {
            event.cancel();
            ChatUtils.info("§cPrevented interaction with §e%s", RestockConfig.getItemName(state.getBlock().asItem()));
        } else if (this.respawnAnchors.get() && state.getBlock() instanceof RespawnAnchorBlock) {
            event.cancel();
            ChatUtils.info("§cPrevented interaction with §e%s", RestockConfig.getItemName(state.getBlock().asItem()));
        } else if (this.blockList.get().contains(state.getBlock())) {
            event.cancel();
            ChatUtils.info("§cPrevented interaction with §e%s", RestockConfig.getItemName(state.getBlock().asItem()));
        }
    }
}
