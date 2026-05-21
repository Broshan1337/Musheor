// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import baritone.api.BaritoneAPI;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import meteordevelopment.meteorclient.pathing.BaritonePathManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={BaritonePathManager.class}, remap=false)
public class BaritonePathManagerMixin {
    @Redirect(method={"<init>"}, require=0, at=@At(value="INVOKE", target="Ljava/lang/invoke/MethodHandles$Lookup;unreflectVarHandle(Ljava/lang/reflect/Field;)Ljava/lang/invoke/VarHandle;"))
    private VarHandle redirectUnreflectVarHandle(MethodHandles.Lookup lookup, Field field) {
        return null;
    }

    @Overwrite
    public float getTargetYaw() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().playerRotations().getYaw();
    }

    @Overwrite
    public float getTargetPitch() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().playerRotations().getPitch();
    }
}

