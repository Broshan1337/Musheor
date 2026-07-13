// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.MFFPIcv139M)
package musheor.utils.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import musheor.utils.WorldUtils;
import net.minecraft.component.DataComponentType;   // class_9331
import net.minecraft.util.math.BlockPos;

/**
 * Singleton holding ALL mutable runtime state for the HighwayBuilder module.
 * Reset via reset() when the module deactivates.
 *
 * NOTE ON OBFUSCATION: in 1.6.1 the getter/setter method names were scrambled
 * so that a method's name no longer matches the field it reads/writes (e.g. the
 * former {@code krxNb5lcQuWA()} actually returned field {@code nt0HZnvBBp}). The
 * exact field↔method wiring below is preserved 1:1 from the bytecode; names were
 * assigned from how the stats consumers (StatsHandler, StatsCollector, DiscordRPC)
 * actually use each value.
 *
 * Counters split into:
 *   session*  — since the current activation of HighwayBuilder (reset on disable)
 *   lifetime* — persistent totals fed in from Minecraft's own StatHandler
 */
public class HighwayState {
    private static final HighwayState INSTANCE = new HighwayState(); // was: FvaNWO

    // --- Direction & position anchors ---
    private WorldUtils.Direction8 direction; // was: Q90GLXQ0Pef
    private BlockPos centerPos;              // was: psJq59YIbp3Z
    private int startX;                      // was: SOYyh5IPg26f7F
    private int startZ;                      // was: rKbT3Ifwo
    private Integer centerX;                 // was: r7hOYIKN2
    private Integer centerY;                 // was: oZHMlTL
    private Integer centerZ;                 // was: xQr5FhbwpQPWgIQ
    private Double lastX;                    // was: OMMZL1F3q
    private Double lastZ;                    // was: zu3a44xDeMFMCRwm

    // --- State flags (semantic meaning per-flag resolved at each call site) ---
    private boolean flag1;   // was: krxNb5lcQuWA
    private boolean flag2;   // was: nt0HZnvBBp
    private boolean flag3;   // was: amz3UB1vE
    private boolean flag4;   // was: sBBIyQG5NWq0K
    private boolean flag5;   // was: sZkZ1izAy
    private boolean flag6;   // was: QYKUhjp
    private boolean flag7;   // was: NIz4xic3Js9
    private boolean flag8;   // was: u1WFwbQRSKa
    private boolean flag9;   // was: LGDfbZq
    private boolean flag10;  // was: to3T8DJCDVX8po
    private boolean flag11;  // was: Sd3jEwKuGABy
    private boolean flag12;  // was: kJfFkD47Vh
    private boolean flag13;  // was: ubHptFBRn5bO

    // --- Baritone / pathing ---
    private DataComponentType<?> baritoneGoalType; // was: apOpfoOHr3fJVwT

    // --- Block tracking positions ---
    private BlockPos lastPlayerBlockPos;      // was: hq1pN0qY
    private BlockPos pendingBreakPos;         // was: ptxWcpd1WV763T5
    private BlockPos returnGoalPos;           // was: DnAk86nuI
    private BlockPos missingMaterialGoalPos;  // was: LlN8EpIZKbk

    // --- Tick / timing counters ---
    private int ticksActive;           // was: pgjj9cLYUTE5g
    private int ticksSinceLastAction;  // was: IeStEJRJ9eb3l
    private int breakProgress;         // was: sFazojak6ig8QgGq
    private int swapDelayTicks;        // was: ewq603nIlCd9Gbu

    // --- Session statistics (reset on deactivate) ---
    private int sessionObsidianPlaced; // was: ExGM8SQ9Qni
    private int sessionObsidianMined;  // was: yS4isXf3gAzs
    private int sessionBlocksMined;    // was: eC9HV2bWGX  (aggregate; reset only)
    private int sessionLavaBuckets;    // was: w9spWeVv3AvI
    private int sessionMiscMined;      // was: HvulV2j9tKjohNgh

    // --- Lifetime statistics (fed from Minecraft StatHandler by StatsCollector) ---
    private int lifetimeObsidianPlaced; // was: Qco5OF
    private int lifetimeObsidianMined;  // was: cgqo7J5iR6
    private int lifetimeTotalMined;     // was: u2kcN4vsQhS46w5s
    private int lifetimeEchests;        // was: Eos3LxdhEJt
    private int lifetimeMiscBlocks;     // was: eB4Or3cBC2

    // --- Block position lists ---
    private final List<BlockPos> list1 = new ArrayList<>();          // was: ITVesx8a
    private final List<BlockPos> blocksToBuild = new ArrayList<>();  // was: ymaK1v
    private final List<BlockPos> list3 = new ArrayList<>();          // was: WRxnOUhRut1YD0z
    private final List<BlockPos> list4 = new ArrayList<>();          // was: jusZpYdy95sR
    private final Map<BlockPos, Integer> blockBreakAttempts = new HashMap<>(); // was: yNlQL5pBA2em

    private HighwayLocator.Checkpoint currentCheckpoint; // was: WUNsqX (J0sjSCk.FDb5)

    private HighwayState() {}

    public static HighwayState getInstance() { return INSTANCE; } // was: FvaNWO()

    // --- Direction ---
    public WorldUtils.Direction8 getDirection() { return direction; }        // was: Q90GLXQ0Pef()
    public void setDirection(WorldUtils.Direction8 dir) { this.direction = dir; } // was: FvaNWO(Direction8)

    // --- Center position ---
    public BlockPos getCenterPos() { return centerPos; }                     // was: psJq59YIbp3Z()
    public void setCenterPos(BlockPos pos) { this.centerPos = pos; }         // was: FvaNWO(BlockPos)

    // --- Start coordinates (player pos when enabled) ---
    public int getStartX() { return startX; }                                // was: SOYyh5IPg26f7F()
    public void setStartX(int x) { this.startX = x; }                        // was: FvaNWO(int)
    public int getStartZ() { return startZ; }                                // was: rKbT3Ifwo()
    public void setStartZ(int z) { this.startZ = z; }                        // was: Q90GLXQ0Pef(int)

    // --- Center coordinates ---
    public Integer getCenterX() { return centerX; }                          // was: r7hOYIKN2()
    public void setCenterX(Integer x) { this.centerX = x; }                  // was: FvaNWO(Integer)
    public Integer getCenterY() { return centerY; }                          // was: oZHMlTL()
    public void setCenterY(Integer y) { this.centerY = y; }                  // was: Q90GLXQ0Pef(Integer)
    public Integer getCenterZ() { return centerZ; }                          // was: xQr5FhbwpQPWgIQ()
    public void setCenterZ(Integer z) { this.centerZ = z; }                  // was: psJq59YIbp3Z(Integer)

    // --- Last position (direction detection) ---
    public Double getLastX() { return lastX; }                               // was: OMMZL1F3q()
    public void setLastX(Double x) { this.lastX = x; }                       // was: FvaNWO(Double)
    public Double getLastZ() { return lastZ; }                               // was: zu3a44xDeMFMCRwm()
    public void setLastZ(Double z) { this.lastZ = z; }                       // was: Q90GLXQ0Pef(Double)

    // --- State flags (wiring preserved exactly; note scrambled getter/field offset) ---
    public void setFlag1(boolean v) { this.flag1 = v; }                      // was: FvaNWO(boolean)  → field krxNb5lcQuWA
    public boolean isFlag2() { return flag2; }                               // was: krxNb5lcQuWA() → field nt0HZnvBBp
    public void setFlag2(boolean v) { this.flag2 = v; }                      // was: Q90GLXQ0Pef(boolean)
    public boolean isFlag3() { return flag3; }                               // was: nt0HZnvBBp()
    public void setFlag3(boolean v) { this.flag3 = v; }                      // was: psJq59YIbp3Z(boolean)
    public boolean isFlag4() { return flag4; }                               // was: amz3UB1vE()
    public void setFlag4(boolean v) { this.flag4 = v; }                      // was: SOYyh5IPg26f7F(boolean)
    public boolean isFlag5() { return flag5; }                               // was: sBBIyQG5NWq0K()
    public void setFlag5(boolean v) { this.flag5 = v; }                      // was: rKbT3Ifwo(boolean)
    public boolean isFlag6() { return flag6; }                               // was: sZkZ1izAy()
    public void setFlag6(boolean v) { this.flag6 = v; }                      // was: r7hOYIKN2(boolean)
    public boolean isFlag7() { return flag7; }                               // was: QYKUhjp()  → field u1WFwbQRSKa
    public boolean isFlag8() { return flag8; }                               // was: NIz4xic3Js9() → field LGDfbZq
    public void setFlag8(boolean v) { this.flag8 = v; }                      // was: oZHMlTL(boolean)
    public boolean isFlag9() { return flag9; }                               // was: u1WFwbQRSKa() → field to3T8DJCDVX8po
    public void setFlag9(boolean v) { this.flag9 = v; }                      // was: xQr5FhbwpQPWgIQ(boolean)
    public boolean isFlag10() { return flag10; }                             // was: LGDfbZq() → field Sd3jEwKuGABy
    public void setFlag10(boolean v) { this.flag10 = v; }                    // was: OMMZL1F3q(boolean)
    public boolean isFlag11() { return flag11; }                             // was: to3T8DJCDVX8po() → field kJfFkD47Vh
    public void setFlag11(boolean v) { this.flag11 = v; }                    // was: zu3a44xDeMFMCRwm(boolean)
    public boolean isFlag12() { return flag12; }                             // was: Sd3jEwKuGABy() → field ubHptFBRn5bO
    public void setFlag12(boolean v) { this.flag12 = v; }                    // was: krxNb5lcQuWA(boolean)

    // --- Baritone goal component type ---
    public DataComponentType<?> getBaritoneGoalType() { return baritoneGoalType; } // was: kJfFkD47Vh()
    public void setBaritoneGoalType(DataComponentType<?> t) { this.baritoneGoalType = t; } // was: FvaNWO(class_9331)

    // --- Block positions ---
    public void setLastPlayerBlockPos(BlockPos pos) { this.lastPlayerBlockPos = pos; } // was: Q90GLXQ0Pef(BlockPos)
    public BlockPos getPendingBreakPos() { return pendingBreakPos; }         // was: ubHptFBRn5bO()
    public void setPendingBreakPos(BlockPos pos) { this.pendingBreakPos = pos; } // was: psJq59YIbp3Z(BlockPos)
    public BlockPos getReturnGoalPos() { return returnGoalPos; }             // was: apOpfoOHr3fJVwT()
    public void setReturnGoalPos(BlockPos pos) { this.returnGoalPos = pos; } // was: SOYyh5IPg26f7F(BlockPos)
    public BlockPos getMissingMaterialGoalPos() { return missingMaterialGoalPos; } // was: hq1pN0qY()
    public void setMissingMaterialGoalPos(BlockPos pos) { this.missingMaterialGoalPos = pos; } // was: rKbT3Ifwo(BlockPos)

    // --- Tick counters ---
    public int getTicksActive() { return ticksActive; }                      // was: ptxWcpd1WV763T5()
    public void setTicksActive(int n) { this.ticksActive = n; }              // was: psJq59YIbp3Z(int)
    public void incrementTicksActive() { this.ticksActive++; }               // was: DnAk86nuI()
    public int getTicksSinceLastAction() { return ticksSinceLastAction; }    // was: LlN8EpIZKbk()
    public void setTicksSinceLastAction(int n) { this.ticksSinceLastAction = n; } // was: SOYyh5IPg26f7F(int)
    public void incrementTicksSinceLastAction() { this.ticksSinceLastAction++; }  // was: pgjj9cLYUTE5g()
    public int getBreakProgress() { return breakProgress; }                  // was: IeStEJRJ9eb3l()
    public void setBreakProgress(int n) { this.breakProgress = n; }          // was: rKbT3Ifwo(int)
    public int getSwapDelayTicks() { return swapDelayTicks; }                // was: sFazojak6ig8QgGq()
    public void setSwapDelayTicks(int n) { this.swapDelayTicks = n; }        // was: r7hOYIKN2(int)

    // --- Session stats ---
    public int getSessionObsidianPlaced() { return sessionObsidianPlaced; }  // was: ewq603nIlCd9Gbu()
    public void incrementSessionObsidianPlaced() { this.sessionObsidianPlaced++; } // was: ExGM8SQ9Qni()
    public int getSessionObsidianMined() { return sessionObsidianMined; }    // was: yS4isXf3gAzs()
    public void incrementSessionObsidianMined() { this.sessionObsidianMined++; }   // was: eC9HV2bWGX()
    public int getSessionLavaBuckets() { return sessionLavaBuckets; }        // was: w9spWeVv3AvI()
    public void incrementSessionLavaBuckets() { this.sessionLavaBuckets++; } // was: HvulV2j9tKjohNgh()
    public int getSessionMiscMined() { return sessionMiscMined; }            // was: Qco5OF()
    public void incrementSessionMiscMined() { this.sessionMiscMined++; }     // was: cgqo7J5iR6()

    // --- Lifetime stats (populated from Minecraft StatHandler) ---
    public int getLifetimeObsidianPlaced() { return lifetimeObsidianPlaced; } // was: u2kcN4vsQhS46w5s()
    public void setLifetimeObsidianPlaced(int n) { this.lifetimeObsidianPlaced = n; } // was: oZHMlTL(int)
    public int getLifetimeObsidianMined() { return lifetimeObsidianMined; }   // was: Eos3LxdhEJt()
    public void setLifetimeObsidianMined(int n) { this.lifetimeObsidianMined = n; }   // was: xQr5FhbwpQPWgIQ(int)
    public int getLifetimeTotalMined() { return lifetimeTotalMined; }         // was: eB4Or3cBC2()
    public void setLifetimeTotalMined(int n) { this.lifetimeTotalMined = n; } // was: OMMZL1F3q(int)
    public int getLifetimeEchests() { return lifetimeEchests; }              // was: ITVesx8a()
    public void setLifetimeEchests(int n) { this.lifetimeEchests = n; }      // was: zu3a44xDeMFMCRwm(int)
    public int getLifetimeMiscBlocks() { return lifetimeMiscBlocks; }        // was: ymaK1v()
    public void setLifetimeMiscBlocks(int n) { this.lifetimeMiscBlocks = n; } // was: krxNb5lcQuWA(int)

    // --- Block lists / checkpoint ---
    public List<BlockPos> getBlocksToBuild() { return blocksToBuild; }       // was: WRxnOUhRut1YD0z()
    public Map<BlockPos, Integer> getBlockBreakAttempts() { return blockBreakAttempts; } // was: jusZpYdy95sR()
    public HighwayLocator.Checkpoint getCurrentCheckpoint() { return currentCheckpoint; } // was: yNlQL5pBA2em()
    public void setCurrentCheckpoint(HighwayLocator.Checkpoint c) { this.currentCheckpoint = c; } // was: FvaNWO(J0sjSCk.FDb5)

    /** Reset all session state. Called when HighwayBuilder deactivates. */
    public void reset() { // was: WUNsqX()
        direction = null;
        centerPos = null;
        startX = 0; startZ = 0;
        centerX = null; centerY = null; centerZ = null;
        lastX = null; lastZ = null;
        flag1 = false; flag2 = false; flag3 = false; flag4 = false; flag5 = false;
        flag6 = false; flag7 = false; flag8 = false; flag9 = false; flag10 = false;
        flag11 = false; flag12 = false; flag13 = false;
        baritoneGoalType = null;
        lastPlayerBlockPos = null; pendingBreakPos = null;
        returnGoalPos = null; missingMaterialGoalPos = null;
        ticksActive = 0; ticksSinceLastAction = 0; breakProgress = 0; swapDelayTicks = 0;
        sessionObsidianPlaced = 0; sessionObsidianMined = 0; sessionBlocksMined = 0;
        sessionLavaBuckets = 0; sessionMiscMined = 0;
        list1.clear(); blocksToBuild.clear(); list3.clear(); list4.clear();
        blockBreakAttempts.clear();
        currentCheckpoint = null;
    }
}
