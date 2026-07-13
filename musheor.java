// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; command/module class refs were obfuscated.
package musheor;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import musheor.commands.CountItems;
import musheor.commands.Execute;
import musheor.commands.FindItem;
import musheor.commands.FolderCommand;
import musheor.commands.KitCommand;
import musheor.commands.PearlStoreCommand;
import musheor.commands.RestockConfig;
import musheor.commands.WhisperCommand;
import musheor.commands.XaeroUtilsCommand;
import musheor.compat.LitematicaHelper;
import musheor.compat.VersionHelper;
import musheor.compat.XearoHelper;
import musheor.modules.automation.AutoBreeder;
import musheor.modules.automation.Dispatcher;
import musheor.modules.automation.EchestFarmer;
import musheor.modules.automation.ElytraTweakz;
import musheor.modules.automation.GatherItem;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.HotbarReplenish;
import musheor.modules.automation.IceRailBuilder;
import musheor.modules.automation.InventoryCleaner;
import musheor.modules.automation.InventoryManager;
import musheor.modules.automation.KekBounce;
import musheor.modules.automation.KekNuker;
import musheor.modules.automation.Printer;
import musheor.modules.automation.ReKit;
import musheor.modules.automation.Refill;
import musheor.modules.automation.SourceRemover;
import musheor.modules.automation.WaypointFollower;
import musheor.modules.features.AirPlace;
import musheor.modules.features.AntiCheat;
import musheor.modules.features.AxisViewer;
import musheor.modules.features.CoordHider;
import musheor.modules.features.GrimScaffold;
import musheor.modules.features.KekFly;
import musheor.modules.features.KekMine;
import musheor.modules.features.LogoutGoal;
import musheor.modules.features.MoreTags;
import musheor.modules.features.NoJumpDelay;
import musheor.modules.features.PortalSpawnESP;
import musheor.modules.hud.DimensionalPosition;
import musheor.modules.hud.HudInfoPlus;
import musheor.modules.hud.StatsViewer;
import musheor.modules.tech.AntiInteract;
import musheor.modules.tech.AutoPortal;
import musheor.modules.tech.ContainerTweaks;
import musheor.modules.tech.MessageInteract;
import musheor.modules.tech.PortalSkipDetection;
import musheor.modules.tech.Whisper;
import musheor.utils.hud.HudEditorKeybind;
import musheor.utils.system.MusheorSystem;
import musheor.utils.system.MusheorTab;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Meteor addon entrypoint for Musheor. Registers the {@link MusheorSystem} config, the
 * Musheor GUI tab, all modules/commands/HUD elements and categories, and reflectively
 * installs the version/Litematica/Xaero compat shims (each gated on the relevant mod being
 * present) plus an optional "Plus" module pack.
 */
public class musheor extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category MAIN = new Category("Musheor");
    public static final Category AUTOMATION = new Category("Automation");
    public static final Category PLUS = new Category("Musheor+");
    public static final HudGroup MUSHEOR_HUD = new HudGroup("Musheor");

    @Override
    public void onInitialize() {
        LOG.info("Musheor initializing");

        try {
            Class.forName("musheor.compat.VersionHelperImpl").getMethod("init").invoke(null);
            LOG.info("VersionHelper initialized");
        } catch (Exception e) {
            LOG.error("Failed to initialize VersionHelper", e);
        }

        Systems.add(new MusheorSystem());
        Tabs.add(new MusheorTab());

        try {
            Class.forName("musheor.plus.PlusInitializer").getMethod("register").invoke(null);
            LOG.info("Plus modules registered");
        } catch (ClassNotFoundException noPlus) {
            // Plus module pack not installed.
        } catch (Exception e) {
            LOG.error("Failed to register Plus modules", e);
        }

        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            try {
                XearoHelper.setInstance((XearoHelper) Class.forName("musheor.compat.XearoHelperImpl").getDeclaredConstructor().newInstance());
                LOG.info("XearoHelper initialized");
            } catch (Exception e) {
                LOG.error("Failed to initialize XearoHelper — is XaeroPlus also installed?", e);
            }
        } else {
            LOG.warn("Xaero's Minimap not found — waypoint and navigation features will be unavailable.");
        }

        if (FabricLoader.getInstance().isModLoaded("litematica")) {
            try {
                LitematicaHelper.setInstance((LitematicaHelper) Class.forName("musheor.compat.LitematicaHelperImpl").getDeclaredConstructor().newInstance());
                LOG.info("LitematicaHelper initialized");
            } catch (Exception e) {
                LOG.error("Failed to initialize LitematicaHelper — is Malilib also installed?", e);
            }
        } else {
            LOG.warn("Litematica not found — Printer (Litematica mode) and KekNuker (Litematica mode) will warn if selected.");
        }

        Modules.get().add(new Printer());
        Modules.get().add(new AirPlace());
        Modules.get().add(new GrimScaffold());
        Modules.get().add(new AntiCheat());
        Modules.get().add(new LogoutGoal());
        Modules.get().add(new KekMine());
        Modules.get().add(new AxisViewer());
        Modules.get().add(new PortalSpawnESP());
        Modules.get().add(new KekFly());
        Modules.get().add(new NoJumpDelay());
        Modules.get().add(new CoordHider());
        Modules.get().add(new MoreTags());
        Modules.get().add(new Dispatcher());
        Modules.get().add(new HotbarReplenish());
        Modules.get().add(new HighwayBuilder());
        Modules.get().add(new EchestFarmer());
        Modules.get().add(new GatherItem());
        Modules.get().add(new SourceRemover());
        Modules.get().add(new InventoryCleaner());
        Modules.get().add(new InventoryManager()); // was: new e1lTf()
        Modules.get().add(new KekNuker());
        Modules.get().add(new Refill());
        Modules.get().add(new IceRailBuilder());
        Modules.get().add(new AutoBreeder());
        Modules.get().add(new ElytraTweakz());
        Modules.get().add(new KekBounce());
        Modules.get().add(new ReKit());
        if (XearoHelper.isLoaded()) {
            Modules.get().add(new WaypointFollower());
        } else {
            LOG.warn("Xaero not found — WaypointFollower module will not be available.");
        }

        Modules.get().add(new PortalSkipDetection());
        Modules.get().add(new AutoPortal());
        Modules.get().add(new ContainerTweaks());
        Modules.get().add(new Whisper());
        Modules.get().add(new MessageInteract());
        Modules.get().add(new AntiInteract());

        Commands.add(new WhisperCommand());   // was: JUKhlfIQiiGS
        Commands.add(new FindItem());         // was: D0Jn
        Commands.add(new CountItems());       // was: FDb5
        Commands.add(new RestockConfig());    // was: UkMm7uisd
        Commands.add(new Execute());          // was: aY0a71o
        Commands.add(new XaeroUtilsCommand()); // was: w4hkT
        Commands.add(new PearlStoreCommand()); // was: zQn6
        Commands.add(new FolderCommand());    // was: SDNZCBI
        Commands.add(new KitCommand());       // was: VGn8YrSOy

        Hud.get().register(StatsViewer.INFO);
        Hud.get().register(HudInfoPlus.INFO);
        Hud.get().register(DimensionalPosition.INFO); // was: DimensionalPosition.FvaNWO
        MeteorClient.EVENT_BUS.subscribe(this);
        if (VersionHelper.get().supportsHudButtons()) {
            MeteorClient.EVENT_BUS.subscribe(new HudEditorKeybind());
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) { // was: FvaNWO(Render2DEvent)
        HudInfoPlus.pruneExpired(); // was: HudInfoPlus.SOYyh5IPg26f7F()
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MAIN);
        Modules.registerCategory(AUTOMATION);
        Modules.registerCategory(PLUS);
    }

    @Override
    public String getPackage() {
        return "musheor";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("musheck", "musheor");
    }

    @Override
    public String getWebsite() {
        return "https://musheck.dev";
    }
}
