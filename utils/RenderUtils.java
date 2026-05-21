// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import it.unimi.dsi.fastutil.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.color.Color;
import musheor.utils.system.MusheorSystem;
import net.minecraft.class_1922;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockState;
import net.minecraft.MinecraftClient;

public class RenderUtils {
    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, List<Pair<BlockPos, Block>> list) {
        boolean bl;
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) {
            return;
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean bl2 = (Boolean)MusheorSystem.Manager.renderLines.get() != false && (MusheorSystem.Manager.renderShape.get() == ShapeMode.Lines || MusheorSystem.Manager.renderShape.get() == ShapeMode.Both);
        boolean bl3 = bl = (Boolean)MusheorSystem.Manager.renderSides.get() != false && (MusheorSystem.Manager.renderShape.get() == ShapeMode.Sides || MusheorSystem.Manager.renderShape.get() == ShapeMode.Both);
        if (!bl2 && !bl) {
            return;
        }
        if (MusheorSystem.Manager.renderType.get() == MusheorSystem.RenderType.Mapped) {
            RenderUtils.jOdDDFXSeWl4(render3DEvent, list, bl2, bl);
        } else {
            RenderUtils.mp3zoXQFKUKYj5(render3DEvent, list, bl2, bl);
        }
    }

    public static void mp3zoXQFKUKYj5(Render3DEvent render3DEvent, List<BlockPos> list) {
        RenderUtils.jOdDDFXSeWl4(render3DEvent, list, null);
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, List<BlockPos> list, Block Block2) {
        MinecraftClient MinecraftClient2 = MinecraftClient.getInstance();
        if (MinecraftClient2.player == null || MinecraftClient2.world == null) {
            return;
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Object> list2 = Block2 != null ? list.stream().map(BlockPos2 -> Pair.of((Object)BlockPos2, (Object)Block2)).collect(Collectors.toList()) : list.stream().map(BlockPos2 -> {
            Block Block2 = MinecraftClient2.world.getBlockState(BlockPos2).getBlock();
            return Pair.of((Object)BlockPos2, (Object)Block2);
        }).collect(Collectors.toList());
        RenderUtils.jOdDDFXSeWl4(render3DEvent, list2);
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, List<BlockPos> list, Color color, Color color2, ShapeMode shapeMode) {
        boolean bl;
        MinecraftClient MinecraftClient2 = MinecraftClient.getInstance();
        if (MinecraftClient2.player == null || MinecraftClient2.world == null) {
            return;
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean bl2 = shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both;
        boolean bl3 = bl = shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both;
        if (!bl2 && !bl) {
            return;
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(list);
        RenderUtils.jOdDDFXSeWl4(render3DEvent, hashSet, bl2, bl, color, color2);
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, BlockPos BlockPos2) {
        RenderUtils.mp3zoXQFKUKYj5(render3DEvent, List.of(BlockPos2));
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, BlockPos BlockPos2, Block Block2) {
        RenderUtils.jOdDDFXSeWl4(render3DEvent, List.of(BlockPos2), Block2);
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, BlockPos BlockPos2, Color color, Color color2, ShapeMode shapeMode) {
        RenderUtils.jOdDDFXSeWl4(render3DEvent, List.of(BlockPos2), color, color2, shapeMode);
    }

    private static void jOdDDFXSeWl4(Render3DEvent render3DEvent, List<Pair<BlockPos, Block>> list, boolean bl, boolean bl2) {
        MinecraftClient MinecraftClient2 = MinecraftClient.getInstance();
        HashMap<Integer, Set> hashMap = new HashMap<Integer, Set>();
        for (Pair<BlockPos, Block> pair : list) {
            BlockPos BlockPos2 = (BlockPos)pair.first();
            Block object = (Block)pair.second();
            BlockState BlockState2 = object.method_9564();
            int n2 = BlockState2.method_26205((class_1922)MinecraftClient2.world, (BlockPos)BlockPos2).field_16011;
            hashMap.computeIfAbsent(n2, n -> new HashSet()).add(BlockPos2);
        }
        int n3 = (Integer)MusheorSystem.Manager.renderLineAlpha.get();
        int n4 = (Integer)MusheorSystem.Manager.renderSideAlpha.get();
        for (Map.Entry entry : hashMap.entrySet()) {
            int n5 = (Integer)entry.getKey();
            Set set = (Set)entry.getValue();
            int n6 = n5 >> 16 & 0xFF;
            int n7 = n5 >> 8 & 0xFF;
            int n8 = n5 & 0xFF;
            Color color = new Color(n6, n7, n8, n3);
            Color color2 = new Color(n6, n7, n8, n4);
            RenderUtils.jOdDDFXSeWl4(render3DEvent, set, bl, bl2, color, color2);
        }
    }

    private static void mp3zoXQFKUKYj5(Render3DEvent render3DEvent, List<Pair<BlockPos, Block>> list, boolean bl, boolean bl2) {
        Set<BlockPos> set = list.stream().map(Pair::first).collect(Collectors.toSet());
        RenderUtils.jOdDDFXSeWl4(render3DEvent, set, bl, bl2, (Color)MusheorSystem.Manager.renderLineColor.get(), (Color)MusheorSystem.Manager.renderSideColor.get());
    }

    private static void jOdDDFXSeWl4(Render3DEvent render3DEvent, Set<BlockPos> set, boolean bl, boolean bl2, Color color, Color color2) {
        MinecraftClient MinecraftClient2 = MinecraftClient.getInstance();
        for (BlockPos BlockPos2 : set) {
            for (Direction Direction2 : Direction.values()) {
                BlockPos BlockPos3 = BlockPos2.offset(Direction2);
                if (set.contains(BlockPos3)) continue;
                if (bl) {
                    RenderUtils.jOdDDFXSeWl4(render3DEvent, BlockPos2, Direction2, set, color);
                }
                if (!bl2) continue;
                RenderUtils.jOdDDFXSeWl4(render3DEvent, BlockPos2, Direction2, color2);
            }
        }
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, BlockPos BlockPos2, Direction Direction2, Set<BlockPos> set, Color color) {
        double d = BlockPos2.getX();
        double d2 = BlockPos2.getY();
        double d3 = BlockPos2.getZ();
        switch (Direction2) {
            case field_11036: 
            case field_11033: {
                double d4;
                double d5 = d4 = Direction2 == Direction.field_11036 ? d2 + 1.0 : d2;
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11043, set)) {
                    render3DEvent.renderer.line(d, d4, d3, d + 1.0, d4, d3, color);
                }
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11035, set)) {
                    render3DEvent.renderer.line(d, d4, d3 + 1.0, d + 1.0, d4, d3 + 1.0, color);
                }
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11039, set)) {
                    render3DEvent.renderer.line(d, d4, d3, d, d4, d3 + 1.0, color);
                }
                if (!RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11034, set)) break;
                render3DEvent.renderer.line(d + 1.0, d4, d3, d + 1.0, d4, d3 + 1.0, color);
                break;
            }
            case field_11043: 
            case field_11035: {
                double d6;
                double d7 = d6 = Direction2 == Direction.field_11035 ? d3 + 1.0 : d3;
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11036, set)) {
                    render3DEvent.renderer.line(d, d2 + 1.0, d6, d + 1.0, d2 + 1.0, d6, color);
                }
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11033, set)) {
                    render3DEvent.renderer.line(d, d2, d6, d + 1.0, d2, d6, color);
                }
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11039, set)) {
                    render3DEvent.renderer.line(d, d2, d6, d, d2 + 1.0, d6, color);
                }
                if (!RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11034, set)) break;
                render3DEvent.renderer.line(d + 1.0, d2, d6, d + 1.0, d2 + 1.0, d6, color);
                break;
            }
            case field_11039: 
            case field_11034: {
                double d8;
                double d9 = d8 = Direction2 == Direction.field_11034 ? d + 1.0 : d;
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11036, set)) {
                    render3DEvent.renderer.line(d8, d2 + 1.0, d3, d8, d2 + 1.0, d3 + 1.0, color);
                }
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11033, set)) {
                    render3DEvent.renderer.line(d8, d2, d3, d8, d2, d3 + 1.0, color);
                }
                if (RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11043, set)) {
                    render3DEvent.renderer.line(d8, d2, d3, d8, d2 + 1.0, d3, color);
                }
                if (!RenderUtils.jOdDDFXSeWl4(BlockPos2, Direction2, Direction.field_11035, set)) break;
                render3DEvent.renderer.line(d8, d2, d3 + 1.0, d8, d2 + 1.0, d3 + 1.0, color);
            }
        }
    }

    public static void jOdDDFXSeWl4(Render3DEvent render3DEvent, BlockPos BlockPos2, Direction Direction2, Color color) {
        double d = BlockPos2.getX();
        double d2 = BlockPos2.getY();
        double d3 = BlockPos2.getZ();
        switch (Direction2) {
            case field_11036: {
                render3DEvent.renderer.quad(d, d2 + 1.0, d3, d + 1.0, d2 + 1.0, d3, d + 1.0, d2 + 1.0, d3 + 1.0, d, d2 + 1.0, d3 + 1.0, color);
                break;
            }
            case field_11033: {
                render3DEvent.renderer.quad(d, d2, d3, d, d2, d3 + 1.0, d + 1.0, d2, d3 + 1.0, d + 1.0, d2, d3, color);
                break;
            }
            case field_11043: {
                render3DEvent.renderer.quad(d, d2, d3, d + 1.0, d2, d3, d + 1.0, d2 + 1.0, d3, d, d2 + 1.0, d3, color);
                break;
            }
            case field_11035: {
                render3DEvent.renderer.quad(d, d2, d3 + 1.0, d, d2 + 1.0, d3 + 1.0, d + 1.0, d2 + 1.0, d3 + 1.0, d + 1.0, d2, d3 + 1.0, color);
                break;
            }
            case field_11039: {
                render3DEvent.renderer.quad(d, d2, d3, d, d2 + 1.0, d3, d, d2 + 1.0, d3 + 1.0, d, d2, d3 + 1.0, color);
                break;
            }
            case field_11034: {
                render3DEvent.renderer.quad(d + 1.0, d2, d3, d + 1.0, d2, d3 + 1.0, d + 1.0, d2 + 1.0, d3 + 1.0, d + 1.0, d2 + 1.0, d3, color);
            }
        }
    }

    public static boolean jOdDDFXSeWl4(BlockPos BlockPos2, Direction Direction2, Direction Direction3, Set<BlockPos> set) {
        return !set.contains(BlockPos2.offset(Direction3)) || set.contains(BlockPos2.offset(Direction2).offset(Direction3));
    }
}

