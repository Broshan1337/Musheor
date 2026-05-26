// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import java.util.Comparator;
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
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.modules.features.MoreTags;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.PearlStore;
import musheor.utils.WorldUtils;
import musheor.utils.internal.PathingHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;

public class MessageInteract
extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral;
    private final Setting<Action> action;
    private final Setting<BlockPos> position;
    private final Setting<Boolean> dropPearl;
    private final Setting<Boolean> returnToStartPos;
    private final Setting<String> PlayerIGN;
    boolean isTeleporting;
    int interactDelayTicks;
    private BlockPos pearlTrapdoorPos;
    private BlockPos startPos;
    private static final Pattern WHISPER_PATTERN = Pattern.compile("([A-Za-z0-9_]{3,16}) whispers: ");
    private static final Random RANDOM = new Random();

    public MessageInteract() {
        super(musheor.MAIN, "message-interact", "Performs a certain action upon receiving a message from a specific player.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.action = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("action")).description("What kind of action to perform.")).defaultValue((Object)Action.RightClick)).build());
        this.position = this.sgGeneral.add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("position")).description("What block position the action is supposed to be for")).defaultValue((Object)new BlockPos(0, 0, 0))).visible(() -> this.action.get() != Action.PearlTeleport)).build());
        this.dropPearl = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("drop-pearl")).description("Drops a pearl after performing the interaction")).defaultValue((Object)true)).visible(() -> this.action.get() == Action.PearlTeleport)).build());
        this.returnToStartPos = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("return-to-start-pos")).description("Pathfinds back to your starting position.")).defaultValue((Object)true)).visible(() -> this.action.get() == Action.PearlTeleport)).build());
        this.PlayerIGN = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("allowed-player")).description("If set, only reacts to !tp whispers from this player. Leave empty to allow anyone.")).defaultValue((Object)"")).build());
        this.isTeleporting = false;
        this.interactDelayTicks = 0;
        this.pearlTrapdoorPos = null;
        this.startPos = null;
    }

    public void onDeactivate() {
        PathingHelper.stopPathing();
        this.isTeleporting = false;
        this.interactDelayTicks = 0;
        this.pearlTrapdoorPos = null;
        this.startPos = null;
    }

    public void onActivate() {
        if (this.action.get() == Action.PearlTeleport && !((MoreTags)Modules.get().get(MoreTags.class)).isActive()) {
            ((MoreTags)Modules.get().get(MoreTags.class)).toggle();
        }
    }

    private BlockPos findNearbyPearlTrapdoor(String string) {
        BlockPos BlockPos2;
        if (MessageInteract.mc.world != null && MessageInteract.mc.player != null) {
            BlockPos2 = null;
            double d = Double.MAX_VALUE;
            for (Map.Entry<Integer, String> entry : MoreTags.pearlOwnerMap.entrySet()) {
                Entity Entity2;
                if (!entry.getValue().equalsIgnoreCase(string) || !((Entity2 = MessageInteract.mc.world.getEntityById(entry.getKey().intValue())) instanceof LivingEntity)) continue;
                LivingEntity LivingEntity2 = (LivingEntity)Entity2;
                for (BlockPos BlockPos3 : new BlockPos[]{LivingEntity2.getBlockPos(), LivingEntity2.getBlockPos().up()}) {
                    double d2;
                    if (!(MessageInteract.mc.world.getBlockState(BlockPos3).getBlock() instanceof EnderChestBlock) || !((d2 = VersionHelper.get().getPlayerPos().squaredDistanceTo(Vec3d.ofCenter(BlockPos3))) < d)) continue;
                    d = d2;
                    BlockPos2 = BlockPos3;
                }
            }
            if (BlockPos2 != null) {
                return BlockPos2;
            }
        }
        if ((BlockPos2 = PearlStore.getByOwner(string)).isEmpty()) {
            return null;
        }
        if (MessageInteract.mc.player == null || BlockPos2.size() == 1) {
            return BlockPos2.getFirst().pos;
        }
        return BlockPos2.stream().min(Comparator.comparingDouble(pearlRecord -> VersionHelper.get().getPlayerPos().squaredDistanceTo(Vec3d.ofCenter(pearlRecord.pos)))).map(pearlRecord -> pearlRecord.pos).orElse(null);
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent receiveMessageEvent) {
        String string = receiveMessageEvent.getMessage().getString();
        if (!string.contains("!tp")) {
            return;
        }
        Matcher matcher = WHISPER_PATTERN.matcher(string);
        if (!matcher.find()) {
            return;
        }
        String string2 = matcher.group(1);
        this.info("Received message from: " + string2, new Object[0]);
        if (!((String)this.PlayerIGN.get()).isEmpty() && !string2.equalsIgnoreCase((String)this.PlayerIGN.get())) {
            return;
        }
        if (this.action.get() == Action.PearlTeleport) {
            this.pearlTrapdoorPos = this.findNearbyPearlTrapdoor(string2);
            if (this.pearlTrapdoorPos == null) {
                this.info("!tp from " + string2 + " but no tracked pearl with trapdoor found.", new Object[0]);
                this.sendWhisper(string2, "Could not find a tracked pearl or trapdoor for you.", MessageInteract.generateRandomTag(8));
                return;
            }
            if (MessageInteract.mc.player.squaredDistanceTo(Vec3d.ofCenter(this.pearlTrapdoorPos)) > 256.0) {
                this.info("!tp from " + string2 + " but pearl is out of render distance.", new Object[0]);
                this.sendWhisper(string2, "Pearl is out of render distance.", MessageInteract.generateRandomTag(8));
                return;
            }
            this.info("Pearl found at " + String.valueOf(this.pearlTrapdoorPos) + " \u2014 pathfinding to load for " + string2, new Object[0]);
            this.sendWhisper(string2, "Pearl found, loading...", MessageInteract.generateRandomTag(8));
            this.startPos = BlockPos.ofFloored(VersionHelper.get().getPlayerPos());
        }
        this.isTeleporting = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        BlockPos BlockPos2;
        if (MessageInteract.mc.player == null || MessageInteract.mc.world == null || !this.isTeleporting) {
            return;
        }
        BlockPos BlockPos3 = BlockPos2 = this.action.get() == Action.PearlTeleport ? this.pearlTrapdoorPos : (BlockPos)this.position.get();
        if (BlockPos2 == null) {
            this.isTeleporting = false;
            return;
        }
        if (this.interactDelayTicks > 0) {
            --this.interactDelayTicks;
            if (this.interactDelayTicks == 0) {
                int n;
                InventoryManager.sendUsePacket(new BlockHitResult(Vec3d.ofCenter(BlockPos2), Direction.UP, BlockPos2, false));
                if (this.action.get() == Action.PearlTeleport && ((Boolean)this.dropPearl.get()).booleanValue() && (n = InventoryManager.findItemSlot(Items.ENDER_PEARL)) != -1) {
                    InventoryManager.dropSlot(n, false);
                }
                this.isTeleporting = false;
                this.pearlTrapdoorPos = null;
                if (((Boolean)this.returnToStartPos.get()).booleanValue() && this.startPos != null) {
                    PathingHelper.setBaritoneGoal((Goal)new GoalBlock(this.startPos));
                }
            }
            return;
        }
        if (!WorldUtils.isWithinDistance(BlockPos2, 4.0)) {
            PathingHelper.setGoalNear(new GoalNear(BlockPos2, 2));
            return;
        }
        PathingHelper.stopPathing();
        WorldUtils.lookAtBlock(BlockPos2);
        if (this.action.get() == Action.LeftClick) {
            Utils.leftClick();
            this.isTeleporting = false;
        } else if (this.action.get() == Action.RightClick) {
            Utils.rightClick();
            this.isTeleporting = false;
        } else if (MessageInteract.mc.world.getBlockState(BlockPos2).getBlock() instanceof EnderChestBlock) {
            InventoryManager.sendUsePacket(new BlockHitResult(Vec3d.ofCenter(BlockPos2), Direction.UP, BlockPos2, false));
            this.interactDelayTicks = 10;
        } else {
            this.info("Trapdoor no longer present at " + String.valueOf(BlockPos2) + ", aborting.", new Object[0]);
            this.isTeleporting = false;
            this.pearlTrapdoorPos = null;
        }
    }

    private void sendWhisper(String string, String string2, String string3) {
        if (MessageInteract.mc.player == null) {
            return;
        }
        MessageInteract.mc.player.networkHandler.sendChatCommand("msg " + string + " " + string2 + " " + string3);
    }

    public static String generateRandomTag(int n) {
        StringBuilder stringBuilder = new StringBuilder(n);
        for (int i = 0; i < n; ++i) {
            stringBuilder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(RANDOM.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
        }
        return "[" + String.valueOf(stringBuilder) + "]";
    }

    public static final class Action
    extends Enum<Action> {
        public static final /* enum */ Action RightClick = new Action();
        public static final /* enum */ Action LeftClick = new Action();
        public static final /* enum */ Action PearlTeleport = new Action();
        private static final /* synthetic */ Action[] $VALUES;

        public static Action[] values() {
            return (Action[])$VALUES.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private static /* synthetic */ Action[] LR7hJDRhkI() {
            return new Action[]{Mf6xpJ, zL8HcoH9O3, ARY91RcgOYBjLC};
        }

        static {
            $VALUES = Action.LR7hJDRhkI();
        }
    }
}

