// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable; Minecraft intermediary ids are annotated inline.
package musheor.mixin.meteor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import musheor.accessor.BetterTabAccessor;
import musheor.utils.system.MusheorSystem;
import net.minecraft.class_310; // MinecraftClient
import net.minecraft.class_355; // PlayerListHud
import net.minecraft.class_640; // PlayerListEntry
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When BetterTab's Musheor display-mode is not "All", filters the tab-list entries: currently only
 * the "Friends" mode is honoured (shows only Meteor friends), sorted in vanilla order and clamped to
 * BetterTab's configured tab size.
 */
@Mixin(class_355.class)
public abstract class PlayerListHudMixin implements BetterTabAccessor {
   @Shadow
   @Final
   private class_310 field_2155; // client

   @Inject(method = "method_48213", at = @At("RETURN"), cancellable = true) // collectPlayerEntries
   private void meteor$filterTabList(CallbackInfoReturnable<List<class_640>> cir) {
      BetterTab betterTab = (BetterTab) Modules.get().get(BetterTab.class);
      if (betterTab.isActive()) {
         MusheorSystem.TabListMode mode = ((BetterTabAccessor) betterTab).musheor$getDisplayMode().get();
         if (mode != MusheorSystem.TabListMode.All) {
            List<class_640> full = new ArrayList<>(Objects.requireNonNull(this.field_2155.method_1562()).method_2880()); // getNetworkHandler().getPlayerList()
            List<class_640> filtered = new ArrayList<>();

            for (class_640 entry : full) {
               if (mode == MusheorSystem.TabListMode.Friends && Friends.get().isFriend(entry)) {
                  filtered.add(entry);
               }
            }

            filtered.sort(class_355.field_2156); // PlayerListHud.ENTRY_ORDERING comparator
            int limit = betterTab.tabSize.get();
            if (filtered.size() > limit) {
               filtered = filtered.subList(0, limit);
            }

            cir.setReturnValue(filtered);
         }
      }
   }
}
