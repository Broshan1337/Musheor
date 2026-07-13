// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.tech;

import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.modules.automation.InventoryManager;
import musheor.modules.features.MoreTags;
import musheor.utils.PearlStore;
import musheor.utils.WorldUtils;
import musheor.utils.internal.PathingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.projectile.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "message-interact" — reacts to "!tp" whispers from a (optionally restricted) player by
 * interacting with, attacking, or loading a stasis pearl at a resolved position. For pearl
 * loading it finds the sender's tracked pearl (from {@link MoreTags} live tags or the
 * persisted {@link PearlStore}), Baritone-paths to it, opens the trapdoor, optionally drops
 * a replacement pearl, and paths back to the start. Replies over /msg with a random token.
 */
public class MessageInteract extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: psJq59YIbp3Z
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();   // was: SOYyh5IPg26f7F

    private final Setting<Action> action = sgGeneral.add(new EnumSetting.Builder<Action>() // was: rKbT3Ifwo
        .name("action").description("What kind of action to perform.").defaultValue(Action.INTERACT).build());
    private final Setting<BlockPos> position = sgGeneral.add(new BlockPosSetting.Builder() // was: r7hOYIKN2
        .name("position").description("What block position the action is supposed to be for").defaultValue(new BlockPos(0, 0, 0))
        .visible(() -> action.get() != Action.LOAD_PEARL).build());
    private final Setting<Boolean> dropPearl = sgGeneral.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("drop-pearl").description("Drops a pearl after performing the interaction").defaultValue(true).visible(() -> action.get() == Action.LOAD_PEARL).build());
    private final Setting<Boolean> returnToStart = sgGeneral.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("return-to-start-pos").description("Pathfinds back to your starting position.").defaultValue(true).visible(() -> action.get() == Action.LOAD_PEARL).build());
    private final Setting<String> allowedPlayer = sgGeneral.add(new StringSetting.Builder() // was: OMMZL1F3q
        .name("allowed-player").description("If set, only reacts to !tp whispers from this player. Leave empty to allow anyone.").defaultValue("").build());

    boolean active = false;          // was: FvaNWO (an action is in progress)
    int interactDelay = 0;           // was: Q90GLXQ0Pef (post-open trapdoor delay)
    private int rotationTicks = 0;   // was: zu3a44xDeMFMCRwm
    private BlockPos pearlPos = null; // was: krxNb5lcQuWA (resolved trapdoor for LOAD_PEARL)
    private BlockPos startPos = null; // was: nt0HZnvBBp
    private static final Pattern WHISPER_PATTERN = Pattern.compile("([A-Za-z0-9_]{3,16}) whispers: "); // was: amz3UB1vE
    private static final Random RANDOM = new Random(); // was: sBBIyQG5NWq0K

    public MessageInteract() {
        super(musheor.MAIN, "message-interact", "Performs a certain action upon receiving a message from a specific player.");
    }

    @Override
    public void onDeactivate() {
        PathingHelper.cancelEverything();
        this.active = false;
        this.interactDelay = 0;
        this.rotationTicks = 0;
        this.pearlPos = null;
        this.startPos = null;
    }

    @Override
    public void onActivate() {
        if (this.action.get() == Action.LOAD_PEARL && !((MoreTags) Modules.get().get(MoreTags.class)).isActive()) {
            ((MoreTags) Modules.get().get(MoreTags.class)).toggle();
        }
    }

    /** Finds the trapdoor position of {@code ownerName}'s nearest tracked pearl (live, else persisted), or null. */
    private BlockPos findPearlTrapdoor(String ownerName) { // was: FvaNWO(String)
        if (mc.world != null && mc.player != null) {
            BlockPos closest = null;
            double closestDist = Double.MAX_VALUE;
            for (Map.Entry<Integer, String> entry : MoreTags.pearlOwners.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(ownerName) && mc.world.getEntityById(entry.getKey()) instanceof EnderPearlEntity pearl) {
                    for (BlockPos candidate : new BlockPos[]{pearl.getBlockPos(), pearl.getBlockPos().down()}) {
                        if (mc.world.getBlockState(candidate).getBlock() instanceof TrapdoorBlock) {
                            double dist = VersionHelper.get().getPlayerPos().squaredDistanceTo(Vec3d.ofCenter(candidate));
                            if (dist < closestDist) {
                                closestDist = dist;
                                closest = candidate;
                            }
                        }
                    }
                }
            }
            if (closest != null) return closest;
        }

        List<PearlStore.PearlRecord> saved = PearlStore.getByOwner(ownerName);
        if (saved.isEmpty()) return null;
        return mc.player != null && saved.size() != 1
            ? saved.stream().min(Comparator.comparingDouble(r -> VersionHelper.get().getPlayerPos().squaredDistanceTo(Vec3d.ofCenter(r.pos)))).map(r -> r.pos).orElse(null)
            : saved.getFirst().pos;
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) { // was: FvaNWO(ReceiveMessageEvent)
        String msg = event.getMessage().getString();
        if (!msg.contains("!tp")) return;
        Matcher matcher = WHISPER_PATTERN.matcher(msg);
        if (!matcher.find()) return;
        String senderName = matcher.group(1);
        this.info("Received message from: " + senderName);
        if (!this.allowedPlayer.get().isEmpty() && !senderName.equalsIgnoreCase(this.allowedPlayer.get())) return;

        if (this.action.get() == Action.LOAD_PEARL) {
            this.pearlPos = this.findPearlTrapdoor(senderName);
            if (this.pearlPos == null) {
                this.info("!tp from " + senderName + " but no tracked pearl with trapdoor found.");
                this.sendWhisper(senderName, "Could not find a tracked pearl or trapdoor for you.", randomToken(8));
                return;
            }
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(this.pearlPos)) > 256.0) {
                this.info("!tp from " + senderName + " but pearl is out of render distance.");
                this.sendWhisper(senderName, "Pearl is out of render distance.", randomToken(8));
                return;
            }
            this.info("Pearl found at " + this.pearlPos + " — pathfinding to load for " + senderName);
            this.sendWhisper(senderName, "Pearl found, loading...", randomToken(8));
            this.startPos = BlockPos.ofFloored(VersionHelper.get().getPlayerPos());
        }
        this.rotationTicks = 0;
        this.active = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (mc.player == null || mc.world == null || !this.active) return;
        BlockPos resolvedPos = this.action.get() == Action.LOAD_PEARL ? this.pearlPos : this.position.get();
        if (resolvedPos == null) {
            this.active = false;
        } else if (this.interactDelay > 0) {
            this.interactDelay--;
            if (this.interactDelay == 0) {
                InventoryManager.interactWith(new BlockHitResult(Vec3d.ofCenter(resolvedPos), Direction.UP, resolvedPos, false));
                if (this.action.get() == Action.LOAD_PEARL && this.dropPearl.get()) {
                    int pearlSlot = InventoryManager.findItemSlotIndex(Items.ENDER_PEARL);
                    if (pearlSlot != -1) InventoryManager.throwSlot(pearlSlot, false);
                }
                this.active = false;
                this.pearlPos = null;
                if (this.returnToStart.get() && this.startPos != null) PathingHelper.setGoal(new GoalBlock(this.startPos));
            }
        } else if (!WorldUtils.isWithinRange(resolvedPos, 4.0)) {
            this.rotationTicks = 0;
            PathingHelper.setGoal(new GoalNear(resolvedPos, 2));
        } else {
            PathingHelper.cancelEverything();
            double yaw = Rotations.getYaw(resolvedPos);
            double pitch = Rotations.getPitch(resolvedPos);
            Rotations.rotate(yaw, pitch);
            this.rotationTicks++;
            if (this.rotationTicks >= 3) {
                if (this.action.get() == Action.ATTACK) {
                    mc.interactionManager.attackBlock(resolvedPos, Direction.UP);
                    this.rotationTicks = 0;
                    this.active = false;
                } else if (this.action.get() == Action.INTERACT) {
                    InventoryManager.interactWith(new BlockHitResult(Vec3d.ofCenter(resolvedPos), Direction.UP, resolvedPos, false));
                    this.rotationTicks = 0;
                    this.active = false;
                } else if (mc.world.getBlockState(resolvedPos).getBlock() instanceof TrapdoorBlock) {
                    InventoryManager.interactWith(new BlockHitResult(Vec3d.ofCenter(resolvedPos), Direction.UP, resolvedPos, false));
                    this.rotationTicks = 0;
                    this.interactDelay = 10;
                } else {
                    this.info("Trapdoor no longer present at " + resolvedPos + ", aborting.");
                    this.rotationTicks = 0;
                    this.active = false;
                    this.pearlPos = null;
                }
            }
        }
    }

    /** Whispers {@code message} to {@code playerName} via /msg with an anti-spam suffix. */
    private void sendWhisper(String playerName, String message, String antiSpam) { // was: FvaNWO(String,String,String)
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand("msg " + playerName + " " + message + " " + antiSpam);
        }
    }

    /** Generates a random alphanumeric anti-spam token like {@code [aZ9kQ...]}. */
    public static String randomToken(int length) { // was: FvaNWO(int)
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return "[" + sb + "]";
    }

    /** What to do at the resolved position. */ // was: enum Action {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum Action { INTERACT, ATTACK, LOAD_PEARL }
}
