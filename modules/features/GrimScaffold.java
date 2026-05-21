// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.RateController;
import net.minecraft.InteractionHand;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.ItemStack;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.MinecraftClient;
import net.minecraft.Screen;

public class GrimScaffold
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient ckqstPn4Gd = MinecraftClient.getInstance();
    private final Setting<Boolean> instantPlace;
    private final Setting<List<Block>> allowBlocklist;
    private final Setting<Boolean> expand;
    private final Setting<Double> expandRange;
    private final Setting<Boolean> expandOnlyForward;
    private final Setting<Boolean> render;
    private final List<BlockPos> LmpuWjra;
    private Block P7WK4vInkqbLg;

    public GrimScaffold() {
        super(musheor.MAIN, "grim-scaffold", "Place blocks under your feet, even if you are in the air");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.instantPlace = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instant-place")).description("Instantly place the block client-side before server confirms")).defaultValue((Object)true)).build());
        this.allowBlocklist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("allowed-blocks")).description("Which blocks scaffold is allowed to place")).defaultValue(new Block[]{Blocks.field_10540, Blocks.field_10515}).build());
        this.expand = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("expand")).description("Extend the scaffold area beyond directly under your feet")).defaultValue((Object)false)).build());
        this.expandRange = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("expand-range")).description("How many blocks to expand the scaffold area")).defaultValue(2.0).sliderRange(1.0, 5.0).decimalPlaces(1).visible(() -> this.expand.get())).build());
        this.expandOnlyForward = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("expand-forward-only")).description("Only expand in the direction you are currently facing")).defaultValue((Object)true)).visible(() -> this.expand.get())).build());
        this.render = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed")).defaultValue((Object)true)).build());
        this.LmpuWjra = new ArrayList<BlockPos>();
        this.P7WK4vInkqbLg = null;
    }

    public void onActivate() {
        this.LmpuWjra.clear();
        this.P7WK4vInkqbLg = null;
    }

    public void onDeactivate() {
        this.LmpuWjra.clear();
        this.P7WK4vInkqbLg = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        Object object;
        if (GrimScaffold.ckqstPn4Gd.player == null || GrimScaffold.ckqstPn4Gd.world == null) {
            return;
        }
        this.P7WK4vInkqbLg = this.BcrtDMyh();
        this.LmpuWjra.clear();
        List<BlockPos> list = this.Nx0JPvjp();
        list.removeIf(BlockPos2 -> !BlockUtils.canPlace((BlockPos)BlockPos2, (boolean)true));
        if (((Boolean)this.render.get()).booleanValue()) {
            this.LmpuWjra.addAll(list);
        }
        if (this.P7WK4vInkqbLg == null || list.isEmpty()) {
            return;
        }
        ItemStack ItemStack22 = GrimScaffold.ckqstPn4Gd.player.method_6047().getStack();
        if (!(ItemStack22 instanceof AbstractClientPlayerEntity) || (object = (AbstractClientPlayerEntity)ItemStack22).method_7711() != this.P7WK4vInkqbLg) {
            InventoryManager.L5CF0C6jx0T17H4I(this.P7WK4vInkqbLg.asItem());
        }
        WorldUtils.l3ot1CwoJ9CsS();
        for (BlockPos BlockPos3 : list) {
            if (!RateController.qy8UwM99rVr()) break;
            Screen Screen2 = new Screen(Vec3d.method_24953((BlockPos)BlockPos3), Direction.field_11033, BlockPos3, false);
            WorldUtils.jOdDDFXSeWl4(InteractionHand.field_5810, Screen2);
            if (!((Boolean)this.instantPlace.get()).booleanValue()) continue;
            GrimScaffold.ckqstPn4Gd.world.method_8652(BlockPos3, this.P7WK4vInkqbLg.method_9564(), 3);
        }
        WorldUtils.l3ot1CwoJ9CsS();
    }

    private Block BcrtDMyh() {
        ItemStack ItemStack2 = GrimScaffold.ckqstPn4Gd.player.method_6047();
        ItemStack ItemStack2 = ItemStack2.getStack();
        if (ItemStack2 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity AbstractClientPlayerEntity2 = (AbstractClientPlayerEntity)ItemStack2;
            if (((List)this.allowBlocklist.get()).contains(AbstractClientPlayerEntity2.method_7711()) && !ItemStack2.setStack()) {
                return AbstractClientPlayerEntity2.method_7711();
            }
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack2 = GrimScaffold.ckqstPn4Gd.player.getId().method_5438(i);
            ItemStack ItemStack3 = ItemStack2.getStack();
            if (!(ItemStack3 instanceof AbstractClientPlayerEntity)) continue;
            AbstractClientPlayerEntity AbstractClientPlayerEntity3 = (AbstractClientPlayerEntity)ItemStack3;
            if (!((List)this.allowBlocklist.get()).contains(AbstractClientPlayerEntity3.method_7711()) || ItemStack2.setStack()) continue;
            return AbstractClientPlayerEntity3.method_7711();
        }
        return null;
    }

    private List<BlockPos> Nx0JPvjp() {
        ArrayList<BlockPos> arrayList;
        block6: {
            arrayList = new ArrayList<BlockPos>();
            HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
            BlockPos BlockPos2 = GrimScaffold.ckqstPn4Gd.player.getBlockPos().method_10074();
            if (hashSet.add(BlockPos2)) {
                arrayList.add(BlockPos2);
            }
            if (!((Boolean)this.expand.get()).booleanValue()) break block6;
            int n = (int)Math.ceil((Double)this.expandRange.get());
            double d = (Double)this.expandRange.get() * (Double)this.expandRange.get();
            Direction Direction2 = GrimScaffold.ckqstPn4Gd.player.method_5735();
            if (((Boolean)this.expandOnlyForward.get()).booleanValue()) {
                for (int i = 1; i <= n; ++i) {
                    BlockPos BlockPos3 = new BlockPos(BlockPos2.getX() + Direction2.method_10148() * i, BlockPos2.getY(), BlockPos2.getZ() + Direction2.method_10165() * i);
                    if (!hashSet.add(BlockPos3)) continue;
                    arrayList.add(BlockPos3);
                }
            } else {
                for (int i = -n; i <= n; ++i) {
                    for (int j = -n; j <= n; ++j) {
                        BlockPos BlockPos4;
                        if (i == 0 && j == 0 || (double)(i * i + j * j) > d || !hashSet.add(BlockPos4 = BlockPos2.method_10069(i, 0, j))) continue;
                        arrayList.add(BlockPos4);
                    }
                }
            }
        }
        return arrayList;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (!((Boolean)this.render.get()).booleanValue() || this.LmpuWjra.isEmpty()) {
            return;
        }
        Block Block2 = this.P7WK4vInkqbLg != null ? this.P7WK4vInkqbLg : Blocks.field_10540;
        RenderUtils.jOdDDFXSeWl4(render3DEvent, this.LmpuWjra, Block2);
    }
}

