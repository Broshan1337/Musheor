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
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.PlayerAbilities;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.class_2533;
import net.minecraft.MinecraftClient;
import net.minecraft.Screen;

public class MessageInteract
extends Module {
    private static final MinecraftClient jTgOjrDfWE = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral;
    private final Setting<Action> action;
    private final Setting<BlockPos> position;
    private final Setting<Boolean> dropPearl;
    private final Setting<Boolean> returnToStartPos;
    private final Setting<String> PlayerIGN;
    boolean JA34csMMAMKnI;
    int OyaWN2jsET;
    private BlockPos MWtXKjmtUPMW8cZK;
    private BlockPos IuR8CfqY;
    private static final Pattern k3N3kyK92V = Pattern.compile("([A-Za-z0-9_]{3,16}) whispers: ");
    private static final Random G0MS6o1MBu8X8QrN = new Random();

    public MessageInteract() {
        super(musheor.MAIN, "message-interact", "Performs a certain action upon receiving a message from a specific player.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.action = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("action")).description("What kind of action to perform.")).defaultValue((Object)Action.Mf6xpJ)).build());
        this.position = this.sgGeneral.add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("position")).description("What block position the action is supposed to be for")).defaultValue((Object)new BlockPos(0, 0, 0))).visible(() -> this.action.get() != Action.ARY91RcgOYBjLC)).build());
        this.dropPearl = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("drop-pearl")).description("Drops a pearl after performing the interaction")).defaultValue((Object)true)).visible(() -> this.action.get() == Action.ARY91RcgOYBjLC)).build());
        this.returnToStartPos = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("return-to-start-pos")).description("Pathfinds back to your starting position.")).defaultValue((Object)true)).visible(() -> this.action.get() == Action.ARY91RcgOYBjLC)).build());
        this.PlayerIGN = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("allowed-player")).description("If set, only reacts to !tp whispers from this player. Leave empty to allow anyone.")).defaultValue((Object)"")).build());
        this.JA34csMMAMKnI = false;
        this.OyaWN2jsET = 0;
        this.MWtXKjmtUPMW8cZK = null;
        this.IuR8CfqY = null;
    }

    public void onDeactivate() {
        PathingHelper.xRVyNRV3cB7();
        this.JA34csMMAMKnI = false;
        this.OyaWN2jsET = 0;
        this.MWtXKjmtUPMW8cZK = null;
        this.IuR8CfqY = null;
    }

    public void onActivate() {
        if (this.action.get() == Action.ARY91RcgOYBjLC && !((MoreTags)Modules.get().get(MoreTags.class)).isActive()) {
            ((MoreTags)Modules.get().get(MoreTags.class)).toggle();
        }
    }

    private BlockPos Y9BgxR(String string) {
        BlockPos BlockPos2;
        if (MessageInteract.jTgOjrDfWE.world != null && MessageInteract.jTgOjrDfWE.player != null) {
            BlockPos2 = null;
            double d = Double.MAX_VALUE;
            for (Map.Entry<Integer, String> entry : MoreTags.rYODaO.entrySet()) {
                Entity Entity2;
                if (!entry.getValue().equalsIgnoreCase(string) || !((Entity2 = MessageInteract.jTgOjrDfWE.world.method_8469(entry.getKey().intValue())) instanceof LivingEntity)) continue;
                LivingEntity LivingEntity2 = (LivingEntity)Entity2;
                for (BlockPos BlockPos3 : new BlockPos[]{LivingEntity2.getBlockPos(), LivingEntity2.getBlockPos().method_10074()}) {
                    double d2;
                    if (!(MessageInteract.jTgOjrDfWE.world.getBlockState(BlockPos3).getBlock() instanceof class_2533) || !((d2 = VersionHelper.get().getPlayerPos().method_1025(Vec3d.method_24953((BlockPos)BlockPos3))) < d)) continue;
                    d = d2;
                    BlockPos2 = BlockPos3;
                }
            }
            if (BlockPos2 != null) {
                return BlockPos2;
            }
        }
        if ((BlockPos2 = PearlStore.LoFK6z05DRRnOV(string)).isEmpty()) {
            return null;
        }
        if (MessageInteract.jTgOjrDfWE.player == null || BlockPos2.size() == 1) {
            return BlockPos2.getFirst().Iy17yV0;
        }
        return BlockPos2.stream().min(Comparator.comparingDouble(pearlRecord -> VersionHelper.get().getPlayerPos().method_1025(Vec3d.method_24953((BlockPos)pearlRecord.Iy17yV0)))).map(pearlRecord -> pearlRecord.Iy17yV0).orElse(null);
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent receiveMessageEvent) {
        String string = receiveMessageEvent.getMessage().getString();
        if (!string.contains("!tp")) {
            return;
        }
        Matcher matcher = k3N3kyK92V.matcher(string);
        if (!matcher.find()) {
            return;
        }
        String string2 = matcher.group(1);
        this.info("Received message from: " + string2, new Object[0]);
        if (!((String)this.PlayerIGN.get()).isEmpty() && !string2.equalsIgnoreCase((String)this.PlayerIGN.get())) {
            return;
        }
        if (this.action.get() == Action.ARY91RcgOYBjLC) {
            this.MWtXKjmtUPMW8cZK = this.Y9BgxR(string2);
            if (this.MWtXKjmtUPMW8cZK == null) {
                this.info("!tp from " + string2 + " but no tracked pearl with trapdoor found.", new Object[0]);
                this.jOdDDFXSeWl4(string2, "Could not find a tracked pearl or trapdoor for you.", MessageInteract.UgB10d(8));
                return;
            }
            if (MessageInteract.jTgOjrDfWE.player.method_5707(Vec3d.method_24953((BlockPos)this.MWtXKjmtUPMW8cZK)) > 256.0) {
                this.info("!tp from " + string2 + " but pearl is out of render distance.", new Object[0]);
                this.jOdDDFXSeWl4(string2, "Pearl is out of render distance.", MessageInteract.UgB10d(8));
                return;
            }
            this.info("Pearl found at " + String.valueOf(this.MWtXKjmtUPMW8cZK) + " \u2014 pathfinding to load for " + string2, new Object[0]);
            this.jOdDDFXSeWl4(string2, "Pearl found, loading...", MessageInteract.UgB10d(8));
            this.IuR8CfqY = BlockPos.method_49638((PlayerAbilities)VersionHelper.get().getPlayerPos());
        }
        this.JA34csMMAMKnI = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        BlockPos BlockPos2;
        if (MessageInteract.jTgOjrDfWE.player == null || MessageInteract.jTgOjrDfWE.world == null || !this.JA34csMMAMKnI) {
            return;
        }
        BlockPos BlockPos3 = BlockPos2 = this.action.get() == Action.ARY91RcgOYBjLC ? this.MWtXKjmtUPMW8cZK : (BlockPos)this.position.get();
        if (BlockPos2 == null) {
            this.JA34csMMAMKnI = false;
            return;
        }
        if (this.OyaWN2jsET > 0) {
            --this.OyaWN2jsET;
            if (this.OyaWN2jsET == 0) {
                int n;
                InventoryManager.jOdDDFXSeWl4(new Screen(Vec3d.method_24953((BlockPos)BlockPos2), Direction.field_11036, BlockPos2, false));
                if (this.action.get() == Action.ARY91RcgOYBjLC && ((Boolean)this.dropPearl.get()).booleanValue() && (n = InventoryManager.KP44bk(Items.field_8634)) != -1) {
                    InventoryManager.jOdDDFXSeWl4(n, false);
                }
                this.JA34csMMAMKnI = false;
                this.MWtXKjmtUPMW8cZK = null;
                if (((Boolean)this.returnToStartPos.get()).booleanValue() && this.IuR8CfqY != null) {
                    PathingHelper.jOdDDFXSeWl4((Goal)new GoalBlock(this.IuR8CfqY));
                }
            }
            return;
        }
        if (!WorldUtils.jOdDDFXSeWl4(BlockPos2, 4.0)) {
            PathingHelper.jOdDDFXSeWl4(new GoalNear(BlockPos2, 2));
            return;
        }
        PathingHelper.xRVyNRV3cB7();
        WorldUtils.MS1x7YGHjIg7eB(BlockPos2);
        if (this.action.get() == Action.zL8HcoH9O3) {
            Utils.leftClick();
            this.JA34csMMAMKnI = false;
        } else if (this.action.get() == Action.Mf6xpJ) {
            Utils.rightClick();
            this.JA34csMMAMKnI = false;
        } else if (MessageInteract.jTgOjrDfWE.world.getBlockState(BlockPos2).getBlock() instanceof class_2533) {
            InventoryManager.jOdDDFXSeWl4(new Screen(Vec3d.method_24953((BlockPos)BlockPos2), Direction.field_11036, BlockPos2, false));
            this.OyaWN2jsET = 10;
        } else {
            this.info("Trapdoor no longer present at " + String.valueOf(BlockPos2) + ", aborting.", new Object[0]);
            this.JA34csMMAMKnI = false;
            this.MWtXKjmtUPMW8cZK = null;
        }
    }

    private void jOdDDFXSeWl4(String string, String string2, String string3) {
        if (MessageInteract.jTgOjrDfWE.player == null) {
            return;
        }
        MessageInteract.jTgOjrDfWE.player.field_3944.method_45730("msg " + string + " " + string2 + " " + string3);
    }

    public static String UgB10d(int n) {
        StringBuilder stringBuilder = new StringBuilder(n);
        for (int i = 0; i < n; ++i) {
            stringBuilder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(G0MS6o1MBu8X8QrN.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
        }
        return "[" + String.valueOf(stringBuilder) + "]";
    }

    public static final class Action
    extends Enum<Action> {
        public static final /* enum */ Action Mf6xpJ = new Action();
        public static final /* enum */ Action zL8HcoH9O3 = new Action();
        public static final /* enum */ Action ARY91RcgOYBjLC = new Action();
        private static final /* synthetic */ Action[] y28KLJnZwrJGrg;

        public static Action[] values() {
            return (Action[])y28KLJnZwrJGrg.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private static /* synthetic */ Action[] LR7hJDRhkI() {
            return new Action[]{Mf6xpJ, zL8HcoH9O3, ARY91RcgOYBjLC};
        }

        static {
            y28KLJnZwrJGrg = Action.LR7hJDRhkI();
        }
    }
}

