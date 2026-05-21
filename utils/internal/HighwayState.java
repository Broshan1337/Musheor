// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import musheor.utils.StatsHandler;
import musheor.utils.WorldUtils;
import net.minecraft.util.math.BlockPos;   // BlockPos
import net.minecraft.util.math.Vec3d;      // class_9331 (approximate — used as goal ref)

/**
 * Singleton that holds ALL mutable runtime state for the HighwayBuilder module.
 * Reset via reset() when the module deactivates.
 *
 * Counters are split into:
 *   - session*  : counts since the current activation of HighwayBuilder
 *   - lifetime* : persistent totals loaded from / saved to data.csv
 */
public class HighwayState {
    private static final HighwayState INSTANCE = new HighwayState();

    // --- Direction & position anchors ---
    private WorldUtils.Direction8 direction;       // Which axis the highway runs along
    private BlockPos centerPos;                    // The player-set center block of the highway
    private int startX;                            // Player X coordinate when module was enabled
    private int startZ;                            // Player Z coordinate when module was enabled
    private Integer centerX;                       // X coordinate of the highway center line
    private Integer centerY;                       // Y coordinate (build height)
    private Integer centerZ;                       // Z coordinate of the highway center line
    private Double lastX;                          // Last recorded X (used for direction detection)
    private Double lastZ;                          // Last recorded Z

    // --- State flags ---
    private boolean isFullyFilled;                 // All required blocks in the current slice are placed
    private boolean flag2;                         // (context unclear)
    private boolean blocksFilled;                  // Current row of blocks is complete
    private boolean isPlacingBlocks;               // Currently in a placement cycle
    private boolean hasMissingBlock;               // A block is missing that needs to be placed
    private boolean allBlocksFilled;               // No blocks need placing right now
    private boolean flag7;                         // (context unclear)
    private boolean flag8;                         // (context unclear)
    private boolean flag9;                         // (context unclear)
    private boolean flag10;                        // (context unclear)
    private boolean flag11;                        // (context unclear)
    private boolean isResupplying;                 // EchestFarmer resupply triggered
    private boolean resupplyActive;                // EchestFarmer resupply is currently running

    // --- Baritone / pathing ---
    private Object baritoneGoal;                   // Current Baritone pathing goal (class_9331)

    // --- Block tracking positions ---
    private BlockPos lastPlayerBlockPos;           // Last player block position (updated each tick)
    private BlockPos pendingBreakPos;              // Block currently being broken
    private BlockPos returnGoalPos;                // Position to return to after a detour
    private BlockPos missingMaterialGoalPos;       // Position of missing material source

    // --- Tick / timing counters ---
    private int ticksActive;                       // Ticks since HighwayBuilder was last activated
    private int ticksSinceLastAction;              // Ticks since a placement/break action
    private int breakProgress;                     // Progress on current block break (ticks)
    private int swapDelayTicks;                    // Ticks remaining in item-swap delay

    // --- Session statistics (reset on deactivate) ---
    private int sessionObsidianPlaced;             // Obsidian blocks placed this session
    private int sessionObsidianMined;              // Obsidian blocks mined this session
    private int sessionLavaBuckets;                // Lava sources removed this session
    private int sessionMiscMined;                  // Other blocks mined this session
    private int sessionBlocksMined;                // Total blocks mined this session

    // --- Lifetime statistics (persisted to data.csv) ---
    private int lifetimeObsidianPlaced;
    private int lifetimeObsidianMined;
    private int lifetimeLavaBuckets;
    private int lifetimeMiscBlocks;
    private int lifetimeMiscMined;

    // --- Block position lists ---
    private final List<BlockPos> list1 = new ArrayList<>();           // (purpose unclear)
    private final List<BlockPos> blocksToBuild = new ArrayList<>();   // Blocks that need to be placed
    private final List<BlockPos> list3 = new ArrayList<>();           // (purpose unclear)
    private final List<BlockPos> list4 = new ArrayList<>();           // (purpose unclear)
    private final Map<BlockPos, Integer> blockBreakAttempts = new HashMap<>(); // Break retry counts

    // --- CSV data cache ---
    private List<String[]> csvData;

    private HighwayState() {}

    public static HighwayState getInstance() { // was: LmpuWjra()
        return INSTANCE;
    }

    // --- Direction ---
    public WorldUtils.Direction8 getDirection() { return direction; }                          // was: P7WK4vInkqbLg
    public void setDirection(WorldUtils.Direction8 dir) { this.direction = dir; }              // was: Gt56Sj4a6BWhgB(Direction8)

    // --- Center position ---
    public BlockPos getCenterPos() { return centerPos; }                                       // was: QTmNF6NCXs
    public void setCenterPos(BlockPos pos) { this.centerPos = pos; }                           // was: Tne1O2a8S2sVbX

    // --- Start coordinates ---
    public int getStartX() { return startX; }                                                  // was: lYl0U01zxBqO9u
    public void setStartX(int x) { this.startX = x; }                                         // was: J2pm2c07elEb5G(int)
    public int getStartZ() { return startZ; }                                                  // was: U6GoOdyLiE04
    public void setStartZ(int z) { this.startZ = z; }                                         // was: J9PiTNS(int)

    // --- Center coordinates ---
    public Integer getCenterX() { return centerX; }                                            // was: Icks58Pk4vQH3
    public void setCenterX(Integer x) { this.centerX = x; }                                   // was: Gt56Sj4a6BWhgB(Integer)
    public Integer getCenterY() { return centerY; }                                            // was: KaWPzeyl1xVKHWo
    public void setCenterY(Integer y) { this.centerY = y; }                                   // was: TAdu5cndwWu3A1(Integer)
    public Integer getCenterZ() { return centerZ; }                                            // was: A02ApsqZGj
    public void setCenterZ(Integer z) { this.centerZ = z; }                                   // was: vgrtgn5(Integer)

    // --- Last position ---
    public Double getLastX() { return lastX; }                                                 // was: JDwgf5
    public void setLastX(Double x) { this.lastX = x; }                                        // was: jOdDDFXSeWl4(Double)
    public Double getLastZ() { return lastZ; }                                                 // was: yyKeW1d7hG
    public void setLastZ(Double z) { this.lastZ = z; }                                        // was: mp3zoXQFKUKYj5(Double)

    // --- State flags ---
    public boolean isFullyFilled() { return isFullyFilled; }                                   // was: yjhDfCpm
    public void setFullyFilled(boolean v) { this.isFullyFilled = v; }                         // was: ZbTtF5KYyGL9YXed(bool)
    public boolean getFlag2() { return flag2; }                                                // was: XWpV9Q7
    public void setFlag2(boolean v) { this.flag2 = v; }                                       // was: e5oi2ZF(bool)
    public boolean isBlocksFilled() { return blocksFilled; }                                   // was: YvaEDE3IjU1
    public void setBlocksFilled(boolean v) { this.blocksFilled = v; }                         // was: BX92A0OIIvD9(bool)
    public boolean isPlacingBlocks() { return isPlacingBlocks; }                               // was: F41rraDXnaj
    public void setPlacingBlocks(boolean v) { this.isPlacingBlocks = v; }                     // was: L5CF0C6jx0T17H4I(bool)
    public boolean hasMissingBlock() { return hasMissingBlock; }                               // was: YnQ4ChsDR
    public void setHasMissingBlock(boolean v) { this.hasMissingBlock = v; }                   // was: Y9BgxR(bool)
    public boolean isAllBlocksFilled() { return allBlocksFilled; }                             // was: NZkZx8MJ67Zw
    public void setAllBlocksFilled(boolean v) { this.allBlocksFilled = v; }                   // was: xG2PP8jo4RWLS(bool)
    public boolean isResupplying() { return isResupplying; }                                   // was: rpvWtoVonf6GeT
    public void setResupplying(boolean v) { this.isResupplying = v; }                         // was: CEOjBr5G5R(bool)
    public boolean isResupplyActive() { return resupplyActive; }                               // was: Y775oeIufYz9
    public void setResupplyActive(boolean v) { this.resupplyActive = v; }                     // was: MS1x7YGHjIg7eB(bool)
    public boolean getFlag9() { return flag9; }                                                // was: jIXFBaSwUWYqAc9
    public boolean getFlag10() { return flag10; }                                              // was: gsu3U1
    public void setFlag10(boolean v) { this.flag10 = v; }                                     // was: LoFK6z05DRRnOV(bool)
    public boolean getFlag11() { return flag11; }                                              // was: OIExXGL6BNv
    public void setFlag11(boolean v) { this.flag11 = v; }                                     // was: J2pm2c07elEb5G(bool)

    // --- Baritone goal ---
    public Object getBaritoneGoal() { return baritoneGoal; }                                   // was: lzRYRnZcMXfWy6t
    public void setBaritoneGoal(Object goal) { this.baritoneGoal = goal; }                    // was: Gt56Sj4a6BWhgB(Goal)

    // --- Block positions ---
    public void setLastPlayerBlockPos(BlockPos pos) { this.lastPlayerBlockPos = pos; }        // was: bGqPXJzBtf
    public BlockPos getPendingBreakPos() { return pendingBreakPos; }                           // was: HBAiI3pyGGGxpI2b
    public void setPendingBreakPos(BlockPos pos) { this.pendingBreakPos = pos; }              // was: ULOAMKfWE3NZZTj8
    public BlockPos getReturnGoalPos() { return returnGoalPos; }                               // was: rUchPoPt
    public void setReturnGoalPos(BlockPos pos) { this.returnGoalPos = pos; }                  // was: gsYdyKVgv
    public BlockPos getMissingMaterialGoalPos() { return missingMaterialGoalPos; }             // was: E74ay1CfIa1C1X6
    public void setMissingMaterialGoalPos(BlockPos pos) { this.missingMaterialGoalPos = pos; }// was: V2mbWoNZftH0t

    // --- Tick counters ---
    public int getTicksActive() { return ticksActive; }                                        // was: yIXEDGFGtS9H
    public void setTicksActive(int n) { this.ticksActive = n; }                               // was: CEOjBr5G5R(int)
    public void incrementTicksActive() { ++ticksActive; }                                      // was: fjsJhTJB1Q6qDp4F
    public int getTicksSinceLastAction() { return ticksSinceLastAction; }                      // was: GjvUiGg0HmH6I
    public void setTicksSinceLastAction(int n) { this.ticksSinceLastAction = n; }             // was: MS1x7YGHjIg7eB(int)
    public void incrementTicksSinceLastAction() { ++ticksSinceLastAction; }                    // was: cIb0h21P81
    public int getBreakProgress() { return breakProgress; }                                    // was: hJTPuzeVhs9lAR
    public void setBreakProgress(int n) { this.breakProgress = n; }                           // was: KDNrzlU9qtrEv(int)
    public int getSwapDelayTicks() { return swapDelayTicks; }                                  // was: LTAva3M
    public void setSwapDelayTicks(int n) { this.swapDelayTicks = n; }                         // was: WOqvNwnejoKApoa(int)

    // --- Session stats ---
    public int getSessionObsidianPlacedCount() { return sessionObsidianPlaced; }               // was: y4KXVv64NUgBOpTQ
    public void incrementSessionObsidianPlaced() { ++sessionObsidianPlaced; }                  // was: s6I5Zvj
    public int getSessionObsidianMinedCount() { return sessionObsidianMined; }                 // was: rYODaO
    public void incrementSessionObsidianMined() { ++sessionObsidianMined; }                    // was: orXwdS7X2l
    public int getSessionLavaBucketCount() { return sessionLavaBuckets; }                      // was: ga2XNtjAtdTW
    public void incrementSessionLavaBuckets() { ++sessionLavaBuckets; }                        // was: qMP0ctta2esan3W
    public int getSessionMiscMinedCount() { return sessionMiscMined; }                         // was: ydrC4rD1c1Q8
    public void incrementSessionMiscMined() { ++sessionMiscMined; }                            // was: J9PESj

    // --- Lifetime stats (read from CSV on load) ---
    public int getLifetimeObsidianPlaced() { return lifetimeObsidianPlaced; }                  // was: IQLoNzzVjej0Fh
    public int getLifetimeObsidianMined() { return lifetimeObsidianMined; }                    // was: xf86w8EXQDMegty
    public int getLifetimeMiscMined() { return lifetimeMiscMined; }                            // was: JUMkrk2AfG
    public int getLifetimeLavaBuckets() { return lifetimeLavaBuckets; }                        // was: YqwfVX
    public int getLifetimeMiscBlocks() { return lifetimeMiscBlocks; }                          // was: TQkkPszTZ

    // --- Block lists ---
    public List<BlockPos> getBlocksToBuild() { return blocksToBuild; }                         // was: Mz2EP5
    public Map<BlockPos, Integer> getBlockBreakAttempts() { return blockBreakAttempts; }       // was: Os3dd8a

    // --- CSV persistence ---
    /** Store CSV rows in memory for reading lifetime stats. */
    public void setCsvData(List<String[]> data) { this.csvData = data; }                       // was: jWrhVf2psx

    /** Load lifetime stats from the cached CSV rows. */
    public void loadLifetimeStatsFromCsv() {  // was: Tz7qNAG6
        this.lifetimeObsidianPlaced = Integer.parseInt(csvData.get(0)[0]);
        this.lifetimeObsidianMined  = Integer.parseInt(csvData.get(1)[0]);
        this.lifetimeLavaBuckets    = Integer.parseInt(csvData.get(2)[0]);
        this.lifetimeMiscBlocks     = Integer.parseInt(csvData.get(3)[0]);
        this.lifetimeMiscMined      = Integer.parseInt(csvData.get(4)[0]);
    }

    /** Persist current session stats merged with lifetime stats to the CSV file. */
    public void saveLifetimeStatsToCsv() {  // was: yQzOzveN8BjKJN5
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(new String[]{ String.valueOf(lifetimeObsidianPlaced + sessionObsidianPlaced) });
        rows.add(new String[]{ String.valueOf(lifetimeObsidianMined  + sessionObsidianMined)  });
        rows.add(new String[]{ String.valueOf(lifetimeLavaBuckets    + sessionMiscMined)      });
        rows.add(new String[]{ String.valueOf(lifetimeMiscBlocks     + sessionLavaBuckets)    });
        rows.add(new String[]{ String.valueOf(lifetimeMiscMined      + sessionBlocksMined)    });
        StatsHandler.writeData(rows);
    }

    /** Reset all session state. Called when HighwayBuilder deactivates. */
    public void reset() {  // was: merMGMToO0ZYtkEn
        direction = null;
        centerPos = null;
        startX = 0; startZ = 0;
        centerX = null; centerY = null; centerZ = null;
        lastX = null; lastZ = null;
        isFullyFilled = false; flag2 = false; blocksFilled = false;
        isPlacingBlocks = false; hasMissingBlock = false; allBlocksFilled = false;
        flag7 = false; flag8 = false; flag9 = false; flag10 = false; flag11 = false;
        isResupplying = false; resupplyActive = false;
        baritoneGoal = null;
        lastPlayerBlockPos = null; pendingBreakPos = null;
        returnGoalPos = null; missingMaterialGoalPos = null;
        ticksActive = 0; ticksSinceLastAction = 0; breakProgress = 0; swapDelayTicks = 0;
        sessionObsidianPlaced = 0; sessionObsidianMined = 0;
        sessionLavaBuckets = 0; sessionMiscMined = 0;
        blocksToBuild.clear();
        list1.clear(); list3.clear(); list4.clear();
        blockBreakAttempts.clear();
    }

    // Aliases used in DiscordRPC to access session stats as formatted strings
    // (the RPC uses the raw int getters defined above)
    public int getSessionObsidianPlaced() { return sessionObsidianPlaced; }
    public int getSessionObsidianMined()  { return sessionObsidianMined;  }
}
