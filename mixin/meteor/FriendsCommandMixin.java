// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable; Minecraft intermediary ids are annotated inline.
package musheor.mixin.meteor;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.FriendArgumentType;
import meteordevelopment.meteorclient.commands.commands.FriendsCommand;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.class_124;  // Formatting
import net.minecraft.class_2172; // CommandSource
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Meteor's {@code .friends} command implementation with Musheor's own add/remove/list
 * subcommands (the original build is cancelled). Purely a UX tweak to the friends command wording.
 */
@Mixin(value = FriendsCommand.class, remap = false)
public abstract class FriendsCommandMixin extends Command {
   protected FriendsCommandMixin(String name, String description, String... aliases) {
      super(name, description, aliases);
   }

   @Inject(method = "build", at = @At("HEAD"), cancellable = true)
   private void build(LiteralArgumentBuilder<class_2172> builder, CallbackInfo ci) {
      builder.then(literal("add").then(argument("player", StringArgumentType.word()).executes(context -> {
         String name = StringArgumentType.getString(context, "player");
         Friend friend = new Friend(name);
         if (Friends.get().add(friend)) {
            ChatUtils.sendMsg(friend.hashCode(), class_124.field_1080, "Added (highlight)%s (default)to friends.".formatted(name), new Object[0]); // Formatting.GRAY
         } else {
            this.error("Already friends with that player.", new Object[0]);
         }

         return 1;
      })));
      builder.then(
         literal("remove")
            .then(
               argument("friend", FriendArgumentType.create())
                  .executes(
                     context -> {
                        Friend friend = FriendArgumentType.get(context);
                        if (friend == null) {
                           this.error("Not friends with that player.", new Object[0]);
                           return 1;
                        }

                        if (Friends.get().remove(friend)) {
                           ChatUtils.sendMsg(
                              friend.hashCode(),
                              class_124.field_1080, // Formatting.GRAY
                              "Removed (highlight)%s (default)from friends.".formatted(friend.getName()),
                              new Object[0]
                           );
                        } else {
                           this.error("Failed to remove that friend.", new Object[0]);
                        }

                        return 1;
                     }
                  )
            )
      );
      builder.then(literal("list").executes(context -> {
         this.info("--- Friends ((highlight)%s(default)) ---", new Object[]{Friends.get().count()});
         Friends.get().forEach(f -> ChatUtils.info("(highlight)%s".formatted(f.getName()), new Object[0]));
         return 1;
      }));
      ci.cancel();
   }
}
