// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.x3lfFe8H5f)
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Block-group ESP renderer. Draws outlines (lines) and/or filled faces (sides) for
 * sets of block positions, only rendering the outer edges/faces of each group.
 * Colours can be taken from settings, per-block map colour ("Mapped"), or supplied.
 */
public class RenderUtils {

    /** Entry point: render a list of (pos, block) pairs using the configured settings. */
    public static void render(Render3DEvent event, List<Pair<BlockPos, Block>> blocks) { // was: FvaNWO(event, List<Pair>)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || blocks == null || blocks.isEmpty()) return;

        boolean renderLines = (Boolean) MusheorSystem.Manager.renderLines.get()
            && (MusheorSystem.Manager.renderShape.get() == ShapeMode.Lines || MusheorSystem.Manager.renderShape.get() == ShapeMode.Both);
        boolean renderSides = (Boolean) MusheorSystem.Manager.renderSides.get()
            && (MusheorSystem.Manager.renderShape.get() == ShapeMode.Sides || MusheorSystem.Manager.renderShape.get() == ShapeMode.Both);
        if (!renderLines && !renderSides) return;

        if (MusheorSystem.Manager.renderType.get() == MusheorSystem.RenderType.Mapped) {
            renderMapped(event, blocks, renderLines, renderSides);
        } else {
            renderFlat(event, blocks, renderLines, renderSides);
        }
    }

    /** Render positions, sampling each block's actual state for its colour. */
    public static void render(Render3DEvent event, List<BlockPos> positions) { // was: Q90GLXQ0Pef(event, List<BlockPos>)
        render(event, positions, null);
    }

    /** Render positions using {@code defaultBlock} (or the world block if null). */
    public static void render(Render3DEvent event, List<BlockPos> positions, Block defaultBlock) { // was: FvaNWO(event, List<BlockPos>, Block)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || positions == null || positions.isEmpty()) return;
        List<Pair<BlockPos, Block>> blocks;
        if (defaultBlock != null) {
            blocks = positions.stream().map(pos -> Pair.of(pos, defaultBlock)).collect(Collectors.toList());
        } else {
            blocks = positions.stream().map(pos -> Pair.of(pos, mc.world.getBlockState(pos).getBlock())).collect(Collectors.toList());
        }
        render(event, blocks);
    }

    /** Render positions with explicit colours and shape mode. */
    public static void render(Render3DEvent event, List<BlockPos> positions, Color lineColor, Color sideColor, ShapeMode shapeMode) { // was: FvaNWO(event, List<BlockPos>, Color, Color, ShapeMode)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || positions == null || positions.isEmpty()) return;
        boolean renderLines = shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both;
        boolean renderSides = shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both;
        if (renderLines || renderSides) {
            renderFaces(event, new HashSet<>(positions), renderLines, renderSides, lineColor, sideColor);
        }
    }

    // Single-position convenience overloads
    public static void render(Render3DEvent event, BlockPos position) { render(event, List.of(position)); } // was: FvaNWO(event, BlockPos)
    public static void render(Render3DEvent event, BlockPos position, Block defaultBlock) { render(event, List.of(position), defaultBlock); } // was: FvaNWO(event, BlockPos, Block)
    public static void render(Render3DEvent event, BlockPos position, Color lineColor, Color sideColor, ShapeMode shapeMode) { render(event, List.of(position), lineColor, sideColor, shapeMode); } // was: FvaNWO(event, BlockPos, Color, Color, ShapeMode)

    /** Groups blocks by their map colour and renders each group in that colour. */
    private static void renderMapped(Render3DEvent event, List<Pair<BlockPos, Block>> blocks, boolean renderLines, boolean renderSides) { // was: FvaNWO(event, List<Pair>, boolean, boolean)
        MinecraftClient mc = MinecraftClient.getInstance();
        Map<Integer, Set<BlockPos>> colorGroups = new HashMap<>();
        for (Pair<BlockPos, Block> pair : blocks) {
            BlockPos pos = pair.first();
            BlockState state = pair.second().getDefaultState();
            int mapColorId = state.getMapColor(mc.world, pos).color;
            colorGroups.computeIfAbsent(mapColorId, k -> new HashSet<>()).add(pos);
        }
        int lineAlpha = (Integer) MusheorSystem.Manager.renderLineAlpha.get();
        int sideAlpha = (Integer) MusheorSystem.Manager.renderSideAlpha.get();
        for (Map.Entry<Integer, Set<BlockPos>> entry : colorGroups.entrySet()) {
            int rgb = entry.getKey();
            int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
            renderFaces(event, entry.getValue(), renderLines, renderSides,
                new Color(r, g, b, lineAlpha), new Color(r, g, b, sideAlpha));
        }
    }

    /** Renders all blocks in a single configured colour. */
    private static void renderFlat(Render3DEvent event, List<Pair<BlockPos, Block>> blocks, boolean renderLines, boolean renderSides) { // was: Q90GLXQ0Pef(event, List<Pair>, boolean, boolean)
        Set<BlockPos> blockSet = blocks.stream().map(Pair::first).collect(Collectors.toSet());
        renderFaces(event, blockSet, renderLines, renderSides,
            (Color) MusheorSystem.Manager.renderLineColor.get(), (Color) MusheorSystem.Manager.renderSideColor.get());
    }

    /** For each block, renders only the faces/edges exposed to a non-member neighbour. */
    private static void renderFaces(Render3DEvent event, Set<BlockPos> blockSet, boolean renderLines, boolean renderSides, Color lineColor, Color sideColor) { // was: FvaNWO(event, Set, boolean, boolean, Color, Color)
        for (BlockPos pos : blockSet) {
            for (Direction side : Direction.values()) {
                if (!blockSet.contains(pos.offset(side))) {
                    if (renderLines) renderEdges(event, pos, side, blockSet, lineColor);
                    if (renderSides) renderQuad(event, pos, side, sideColor);
                }
            }
        }
    }

    /** Draws the 4 edge lines of an exposed face (skipping interior shared edges). */
    public static void renderEdges(Render3DEvent event, BlockPos pos, Direction side, Set<BlockPos> blockSet, Color lineColor) { // was: FvaNWO(event, BlockPos, Direction, Set, Color)
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        switch (side) {
            case UP, DOWN -> {
                double fy = side == Direction.UP ? y + 1.0 : y;
                if (shouldRenderEdge(pos, side, Direction.NORTH, blockSet)) event.renderer.line(x, fy, z, x + 1.0, fy, z, lineColor);
                if (shouldRenderEdge(pos, side, Direction.SOUTH, blockSet)) event.renderer.line(x, fy, z + 1.0, x + 1.0, fy, z + 1.0, lineColor);
                if (shouldRenderEdge(pos, side, Direction.WEST, blockSet))  event.renderer.line(x, fy, z, x, fy, z + 1.0, lineColor);
                if (shouldRenderEdge(pos, side, Direction.EAST, blockSet))  event.renderer.line(x + 1.0, fy, z, x + 1.0, fy, z + 1.0, lineColor);
            }
            case NORTH, SOUTH -> {
                double fz = side == Direction.SOUTH ? z + 1.0 : z;
                if (shouldRenderEdge(pos, side, Direction.UP, blockSet))    event.renderer.line(x, y + 1.0, fz, x + 1.0, y + 1.0, fz, lineColor);
                if (shouldRenderEdge(pos, side, Direction.DOWN, blockSet))  event.renderer.line(x, y, fz, x + 1.0, y, fz, lineColor);
                if (shouldRenderEdge(pos, side, Direction.WEST, blockSet))  event.renderer.line(x, y, fz, x, y + 1.0, fz, lineColor);
                if (shouldRenderEdge(pos, side, Direction.EAST, blockSet))  event.renderer.line(x + 1.0, y, fz, x + 1.0, y + 1.0, fz, lineColor);
            }
            case WEST, EAST -> {
                double fx = side == Direction.EAST ? x + 1.0 : x;
                if (shouldRenderEdge(pos, side, Direction.UP, blockSet))    event.renderer.line(fx, y + 1.0, z, fx, y + 1.0, z + 1.0, lineColor);
                if (shouldRenderEdge(pos, side, Direction.DOWN, blockSet))  event.renderer.line(fx, y, z, fx, y, z + 1.0, lineColor);
                if (shouldRenderEdge(pos, side, Direction.NORTH, blockSet)) event.renderer.line(fx, y, z, fx, y + 1.0, z, lineColor);
                if (shouldRenderEdge(pos, side, Direction.SOUTH, blockSet)) event.renderer.line(fx, y, z + 1.0, fx, y + 1.0, z + 1.0, lineColor);
            }
        }
    }

    /** Draws a single filled face quad. */
    public static void renderQuad(Render3DEvent event, BlockPos pos, Direction side, Color sideColor) { // was: FvaNWO(event, BlockPos, Direction, Color)
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        switch (side) {
            case UP    -> event.renderer.quad(x, y + 1.0, z, x + 1.0, y + 1.0, z, x + 1.0, y + 1.0, z + 1.0, x, y + 1.0, z + 1.0, sideColor);
            case DOWN  -> event.renderer.quad(x, y, z, x, y, z + 1.0, x + 1.0, y, z + 1.0, x + 1.0, y, z, sideColor);
            case NORTH -> event.renderer.quad(x, y, z, x + 1.0, y, z, x + 1.0, y + 1.0, z, x, y + 1.0, z, sideColor);
            case SOUTH -> event.renderer.quad(x, y, z + 1.0, x, y + 1.0, z + 1.0, x + 1.0, y + 1.0, z + 1.0, x + 1.0, y, z + 1.0, sideColor);
            case WEST  -> event.renderer.quad(x, y, z, x, y + 1.0, z, x, y + 1.0, z + 1.0, x, y, z + 1.0, sideColor);
            case EAST  -> event.renderer.quad(x + 1.0, y, z, x + 1.0, y, z + 1.0, x + 1.0, y + 1.0, z + 1.0, x + 1.0, y + 1.0, z, sideColor);
        }
    }

    /** True if the edge between {@code face} and {@code edgeDir} is on the group boundary. */
    public static boolean shouldRenderEdge(BlockPos pos, Direction face, Direction edgeDir, Set<BlockPos> blockSet) { // was: FvaNWO(BlockPos, Direction, Direction, Set)
        return !blockSet.contains(pos.offset(edgeDir)) || blockSet.contains(pos.offset(face).offset(edgeDir));
    }
}
