// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import musheor.accessor.BetterTabAccessor;
import musheor.utils.system.MusheorSystem;
import net.minecraft.MinecraftClient;
import net.minecraft.class_355;
import net.minecraft.PlayerListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_355.class})
public abstract class PlayerListHudMixin
implements BetterTabAccessor {
    @Shadow
    @Final
    private MinecraftClient field_2155;

    @Inject(method={"method_48213"}, at={@At(value="RETURN")}, cancellable=true)
    private void meteor$filterTabList(CallbackInfoReturnable<List<PlayerListEntry>> callbackInfoReturnable) {
        BetterTab betterTab = (BetterTab)Modules.get().get(BetterTab.class);
        if (!betterTab.isActive()) {
            return;
        }
        MusheorSystem.TabListMode tabListMode = (MusheorSystem.TabListMode)((Object)((BetterTabAccessor)betterTab).musheor$getDisplayMode().get());
        if (tabListMode == MusheorSystem.TabListMode.All) {
            return;
        }
        ArrayList arrayList = new ArrayList(Objects.requireNonNull(this.field_2155.getNetworkHandler()).getPlayerList());
        List<Object> list = new ArrayList<PlayerListEntry>();
        for (PlayerListEntry PlayerListEntry2 : arrayList) {
            if (tabListMode != MusheorSystem.TabListMode.Friends || !Friends.get().isFriend(PlayerListEntry2)) continue;
            list.add(PlayerListEntry2);
        }
        list.sort(class_355.field_2156);
        int n = (Integer)betterTab.tabSize.get();
        if (list.size() > n) {
            list = list.subList(0, n);
        }
        callbackInfoReturnable.setReturnValue(list);
    }
}

