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
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.MinecraftClient;

/**
 * Renders highlighted block outlines and fills for various rendering modes.
 *
 * Public API (all overloads named renderBlocks, renderFaceLines, renderFaceQuad):
 *   renderBlocks(event, List<Pair<BlockPos,Block>>)       — managed colors from MusheorSystem config
 *   renderBlocks(event, List<BlockPos>)                   — simple positions, uses current world block types
 *   renderBlocks(event, List<BlockPos>, Block)            — treats every position as the given block type
 *   renderBlocks(event, List<BlockPos>, Color, Color, ShapeMode) — explicit colors
 *   renderBlocks(event, BlockPos)                         — single-block shorthand
 *   renderBlocks(event, BlockPos, Block)                  — single-block + explicit type
 *   renderBlocks(event, BlockPos, Color, Color, ShapeMode)— single-block + explicit colors
 *
 * Internal helpers:
 *   renderMapped  — groups a block list by MapColor, renders each colour group separately
 *   renderUniform — renders a block list using the configured uniform colour
 *   renderSet     — lowest-level; renders a Set<BlockPos> with explicit colours
 *   renderFaceLines / renderFaceQuad — render a single block face (lines / filled quad)
 *   shouldRenderEdge — returns true when a given edge of a face should be drawn
 */
public class RenderUtils {

    // -------------------------------------------------------------------------
    // Public entry points
    // -------------------------------------------------------------------------

    /** Renders a list of (position, block-type) pairs using colours from MusheorSystem config. */
    public static void renderBlocks(Render3DEvent event, List<Pair<BlockPos, Block>> list) { // was: jOdDDFXSeWl4
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) return;
        if (list == null || list.isEmpty()) return;

        boolean renderLines = (Boolean) MusheorSystem.Manager.renderLines.get() != false
            && (MusheorSystem.Manager.renderShape.get() == ShapeMode.Lines
                || MusheorSystem.Manager.renderShape.get() == ShapeMode.Both);
        boolean renderSides = (Boolean) MusheorSystem.Manager.renderSides.get() != false
            && (MusheorSystem.Manager.renderShape.get() == ShapeMode.Sides
                || MusheorSystem.Manager.renderShape.get() == ShapeMode.Both);
        if (!renderLines && !renderSides) return;

        if (MusheorSystem.Manager.renderType.get() == MusheorSystem.RenderType.Mapped) {
            RenderUtils.renderMapped(event, list, renderLines, renderSides);
        } else {
            RenderUtils.renderUniform(event, list, renderLines, renderSides);
        }
    }

    /** Renders a list of positions, inferring each block type from the current world. */
    public static void renderBlocks(Render3DEvent event, List<BlockPos> list) { // was: mp3zoXQFKUKYj5
        RenderUtils.renderBlocks(event, list, (Block) null);
    }

    /**
     * Renders a list of positions.
     * If {@code block} is non-null, all positions are treated as that block type;
     * otherwise each position's actual world block is used.
     */
    public static void renderBlocks(Render3DEvent event, List<BlockPos> list, Block block) { // was: jOdDDFXSeWl4
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (list == null || list.isEmpty()) return;

        List<Object> pairs = block != null
            ? list.stream().map(pos -> Pair.of((Object) pos, (Object) block)).collect(Collectors.toList())
            : list.stream().map(pos -> {
                Block b = mc.world.getBlockState(pos).getBlock();
                return Pair.of((Object) pos, (Object) b);
              }).collect(Collectors.toList());
        RenderUtils.renderBlocks(event, pairs);
    }

    /** Renders a list of positions with explicit line/side colours and shape mode. */
    public static void renderBlocks(Render3DEvent event, List<BlockPos> list, Color lineColor, Color sideColor, ShapeMode shapeMode) { // was: jOdDDFXSeWl4
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (list == null || list.isEmpty()) return;

        boolean renderLines = shapeMode == ShapeMode.Lines || shapeMode == ShapeMode.Both;
        boolean renderSides = shapeMode == ShapeMode.Sides || shapeMode == ShapeMode.Both;
        if (!renderLines && !renderSides) return;

        HashSet<BlockPos> set = new HashSet<BlockPos>(list);
        RenderUtils.renderSet(event, set, renderLines, renderSides, lineColor, sideColor);
    }

    /** Single-block shorthand — infers block type from world. */
    public static void renderBlocks(Render3DEvent event, BlockPos pos) { // was: jOdDDFXSeWl4
        RenderUtils.renderBlocks(event, List.of(pos));
    }

    /** Single-block shorthand — treats the position as the given block type. */
    public static void renderBlocks(Render3DEvent event, BlockPos pos, Block block) { // was: jOdDDFXSeWl4
        RenderUtils.renderBlocks(event, List.of(pos), block);
    }

    /** Single-block shorthand — explicit colours. */
    public static void renderBlocks(Render3DEvent event, BlockPos pos, Color lineColor, Color sideColor, ShapeMode shapeMode) { // was: jOdDDFXSeWl4
        RenderUtils.renderBlocks(event, List.of(pos), lineColor, sideColor, shapeMode);
    }

    // -------------------------------------------------------------------------
    // Private dispatch helpers
    // -------------------------------------------------------------------------

    /**
     * Groups blocks by their MapColor value and renders each group in that colour,
     * using alpha values from MusheorSystem config.
     */
    private static void renderMapped(Render3DEvent event, List<Pair<BlockPos, Block>> list, boolean renderLines, boolean renderSides) { // was: jOdDDFXSeWl4
        MinecraftClient mc = MinecraftClient.getInstance();
        HashMap<Integer, Set> byColor = new HashMap<Integer, Set>();
        for (Pair<BlockPos, Block> pair : list) {
            BlockPos pos = (BlockPos) pair.first();
            Block block = (Block) pair.second();
            BlockState state = block.getDefaultState();
            int colorInt = state.getMapColor((BlockView) mc.world, pos).color;
            byColor.computeIfAbsent(colorInt, k -> new HashSet()).add(pos);
        }
        int lineAlpha = (Integer) MusheorSystem.Manager.renderLineAlpha.get();
        int sideAlpha = (Integer) MusheorSystem.Manager.renderSideAlpha.get();
        for (Map.Entry entry : byColor.entrySet()) {
            int colorInt = (Integer) entry.getKey();
            Set set = (Set) entry.getValue();
            int r = colorInt >> 16 & 0xFF;
            int g = colorInt >> 8  & 0xFF;
            int b = colorInt       & 0xFF;
            Color lineColor = new Color(r, g, b, lineAlpha);
            Color sideColor = new Color(r, g, b, sideAlpha);
            RenderUtils.renderSet(event, set, renderLines, renderSides, lineColor, sideColor);
        }
    }

    /**
     * Renders all blocks using the configured uniform line/side colours from MusheorSystem.
     */
    private static void renderUniform(Render3DEvent event, List<Pair<BlockPos, Block>> list, boolean renderLines, boolean renderSides) { // was: mp3zoXQFKUKYj5
        Set<BlockPos> set = list.stream().map(Pair::first).collect(Collectors.toSet());
        RenderUtils.renderSet(event, set, renderLines, renderSides,
            (Color) MusheorSystem.Manager.renderLineColor.get(),
            (Color) MusheorSystem.Manager.renderSideColor.get());
    }

    /**
     * Core renderer: iterates the set of positions and, for each exposed face,
     * draws line edges and/or a filled quad depending on the flags.
     */
    private static void renderSet(Render3DEvent event, Set<BlockPos> set, boolean renderLines, boolean renderSides, Color lineColor, Color sideColor) { // was: jOdDDFXSeWl4
        for (BlockPos pos : set) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (set.contains(neighbor)) continue;
                if (renderLines) {
                    RenderUtils.renderFaceLines(event, pos, dir, set, lineColor);
                }
                if (!renderSides) continue;
                RenderUtils.renderFaceQuad(event, pos, dir, sideColor);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Face-level renderers
    // -------------------------------------------------------------------------

    /**
     * Draws the visible edge lines of a single block face.
     * An edge is skipped if the adjacent block in that direction is also in the set
     * (unless the corner block is also present, which re-enables the edge).
     */
    public static void renderFaceLines(Render3DEvent event, BlockPos pos, Direction dir, Set<BlockPos> set, Color color) { // was: jOdDDFXSeWl4
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (dir) {
            case UP:
            case DOWN: {
                double faceY = dir == Direction.UP ? y + 1.0 : y;
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.NORTH, set))
                    event.renderer.line(x, faceY, z, x + 1.0, faceY, z, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.SOUTH, set))
                    event.renderer.line(x, faceY, z + 1.0, x + 1.0, faceY, z + 1.0, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.WEST, set))
                    event.renderer.line(x, faceY, z, x, faceY, z + 1.0, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.EAST, set))
                    event.renderer.line(x + 1.0, faceY, z, x + 1.0, faceY, z + 1.0, color);
                break;
            }
            case NORTH:
            case SOUTH: {
                double faceZ = dir == Direction.SOUTH ? z + 1.0 : z;
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.UP, set))
                    event.renderer.line(x, y + 1.0, faceZ, x + 1.0, y + 1.0, faceZ, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.DOWN, set))
                    event.renderer.line(x, y, faceZ, x + 1.0, y, faceZ, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.WEST, set))
                    event.renderer.line(x, y, faceZ, x, y + 1.0, faceZ, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.EAST, set))
                    event.renderer.line(x + 1.0, y, faceZ, x + 1.0, y + 1.0, faceZ, color);
                break;
            }
            case WEST:
            case EAST: {
                double faceX = dir == Direction.EAST ? x + 1.0 : x;
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.UP, set))
                    event.renderer.line(faceX, y + 1.0, z, faceX, y + 1.0, z + 1.0, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.DOWN, set))
                    event.renderer.line(faceX, y, z, faceX, y, z + 1.0, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.NORTH, set))
                    event.renderer.line(faceX, y, z, faceX, y + 1.0, z, color);
                if (RenderUtils.shouldRenderEdge(pos, dir, Direction.SOUTH, set))
                    event.renderer.line(faceX, y, z + 1.0, faceX, y + 1.0, z + 1.0, color);
            }
        }
    }

    /** Draws a filled quad for a single block face. */
    public static void renderFaceQuad(Render3DEvent event, BlockPos pos, Direction dir, Color color) { // was: jOdDDFXSeWl4
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (dir) {
            case UP: {
                event.renderer.quad(x, y + 1.0, z,  x + 1.0, y + 1.0, z,  x + 1.0, y + 1.0, z + 1.0,  x, y + 1.0, z + 1.0, color);
                break;
            }
            case DOWN: {
                event.renderer.quad(x, y, z,  x, y, z + 1.0,  x + 1.0, y, z + 1.0,  x + 1.0, y, z, color);
                break;
            }
            case NORTH: {
                event.renderer.quad(x, y, z,  x + 1.0, y, z,  x + 1.0, y + 1.0, z,  x, y + 1.0, z, color);
                break;
            }
            case SOUTH: {
                event.renderer.quad(x, y, z + 1.0,  x, y + 1.0, z + 1.0,  x + 1.0, y + 1.0, z + 1.0,  x + 1.0, y, z + 1.0, color);
                break;
            }
            case WEST: {
                event.renderer.quad(x, y, z,  x, y + 1.0, z,  x, y + 1.0, z + 1.0,  x, y, z + 1.0, color);
                break;
            }
            case EAST: {
                event.renderer.quad(x + 1.0, y, z,  x + 1.0, y, z + 1.0,  x + 1.0, y + 1.0, z + 1.0,  x + 1.0, y + 1.0, z, color);
            }
        }
    }

    /**
     * Returns true if the given edge of a face should be rendered.
     *
     * The edge runs along {@code edgeDir} on the face {@code faceDir} of {@code pos}.
     * The edge is hidden when the neighbor block (in {@code edgeDir}) is also in the set
     * AND the diagonal corner (faceDir + edgeDir) is NOT in the set.
     */
    public static boolean shouldRenderEdge(BlockPos pos, Direction faceDir, Direction edgeDir, Set<BlockPos> set) { // was: jOdDDFXSeWl4
        return !set.contains(pos.offset(edgeDir)) || set.contains(pos.offset(faceDir).offset(edgeDir));
    }
}