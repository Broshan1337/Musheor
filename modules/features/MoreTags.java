// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.musheor;
import musheor.utils.PearlStore;
import musheor.utils.TagUtils;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class MoreTags
extends Module {
    private final SettingGroup sgPlayer;
    private final SettingGroup sgPearl;
    public final Setting<Boolean> playerTags;
    private final Setting<Double> playerTagScale;
    private final Setting<Boolean> playerTagDistanceScaling;
    private final Setting<Boolean> ignoreSelf;
    private final Setting<NameColorType> nameColorType;
    private final Setting<SettingColor> nameColor;
    private final Setting<SettingColor> nameGradientStart;
    private final Setting<SettingColor> nameGradientEnd;
    private final Setting<SettingColor> friendColor;
    private final Setting<Boolean> showBackground;
    private final Setting<SettingColor> backgroundColor;
    private final Setting<Double> backgroundPadding;
    private final Setting<OutlineType> outlineType;
    private final Setting<SettingColor> outlineColorPlayer;
    private final Setting<Double> backgroundRounding;
    private final Setting<SettingColor> outlineGradientStart;
    private final Setting<SettingColor> outlineGradientEnd;
    private final Setting<DistanceType> distanceType;
    private final Setting<SettingColor> distanceColor;
    private final Setting<Boolean> showPlayerSkin;
    private final Setting<Double> playerSkinScale;
    private final Setting<Boolean> showItems;
    private final Setting<Double> itemScale;
    private final Setting<Double> rowSpacing;
    private final Setting<PearlRenderingType> renderPearl;
    private final Setting<SettingColor> outlineColor;
    private final Setting<SettingColor> unknownColor;
    private final Setting<TagDisplayType> tagDisplay;
    private final Setting<Boolean> tagDistanceScaling;
    private final Setting<Boolean> lockTagPos;
    private final Setting<SettingColor> tagColor;
    private final Setting<Double> scale;
    private final Setting<Double> skinScale;
    public static final Map<Integer, String> pearlOwnerMap = new HashMap<Integer, String>();
    private final Set<Integer> pendingPearls;
    private final Map<Integer, Integer> pearlCountMap;
    private final Map<String, Identifier> skinTextureMap; // was: qMP0ctta2esan3W
    private final Set<String> loadingSkins;              // was: ydrC4rD1c1Q8

    public MoreTags() {
        super(musheor.MAIN, "kek-tags", "Enhanced nametags for players, pearls and items (WIP)");
        this.sgPlayer = this.settings.createGroup("Player");
        this.sgPearl = this.settings.createGroup("Pearl");
        this.playerTags = this.sgPlayer.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enabled")).description("Show custom nametags above players.")).defaultValue((Object)true)).build());
        this.playerTagScale = this.sgPlayer.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Scale of the player nametag.")).defaultValue(1.0).sliderRange(0.5, 3.0).decimalPlaces(1).visible(() -> this.playerTags.get())).build());
        this.playerTagDistanceScaling = this.sgPlayer.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance-scaling")).description("Scales the nametag based on distance from the player.")).defaultValue((Object)false)).visible(() -> this.playerTags.get())).build());
        this.ignoreSelf = this.sgPlayer.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Don't render a nametag above your own player.")).defaultValue((Object)true)).visible(() -> this.playerTags.get())).build());
        this.nameColorType = this.sgPlayer.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("name-color")).description("How to color the player's name.")).defaultValue((Object)NameColorType.Default)).visible(() -> this.playerTags.get())).build());
        this.nameColor = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("name-color-value")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> (Boolean)this.playerTags.get() != false && this.nameColorType.get() == NameColorType.Custom)).build());
        this.nameGradientStart = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("name-gradient-start")).defaultValue(new SettingColor(255, 100, 100)).visible(() -> (Boolean)this.playerTags.get() != false && this.nameColorType.get() == NameColorType.Gradient)).build());
        this.nameGradientEnd = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("name-gradient-end")).defaultValue(new SettingColor(100, 100, 255)).visible(() -> (Boolean)this.playerTags.get() != false && this.nameColorType.get() == NameColorType.Gradient)).build());
        this.friendColor = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("friend-color")).description("Name color for friends.")).defaultValue(new SettingColor(Color.GREEN)).visible(() -> this.playerTags.get())).build());
        this.showBackground = this.sgPlayer.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Show a background behind the nametag.")).defaultValue((Object)true)).visible(() -> this.playerTags.get())).build());
        this.backgroundColor = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).defaultValue(new SettingColor(0, 0, 0, 160)).visible(() -> (Boolean)this.playerTags.get() != false && (Boolean)this.showBackground.get() != false)).build());
        this.backgroundPadding = this.sgPlayer.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("background-padding")).defaultValue(6.0).sliderRange(0.0, 20.0).decimalPlaces(1).visible(() -> (Boolean)this.playerTags.get() != false && (Boolean)this.showBackground.get() != false)).build());
        this.outlineType = this.sgPlayer.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("outline")).defaultValue((Object)OutlineType.None)).visible(() -> this.playerTags.get())).build());
        this.outlineColorPlayer = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("outline-color")).defaultValue(new SettingColor(255, 255, 255, 200)).visible(() -> (Boolean)this.playerTags.get() != false && this.outlineType.get() == OutlineType.Solid)).build());
        this.backgroundRounding = this.sgPlayer.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("outline-rounding")).defaultValue(3.0).sliderRange(0.0, 10.0).decimalPlaces(1).visible(() -> (Boolean)this.playerTags.get() != false && this.outlineType.get() != OutlineType.None)).build());
        this.outlineGradientStart = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("outline-gradient-start")).defaultValue(new SettingColor(255, 100, 100)).visible(() -> (Boolean)this.playerTags.get() != false && this.outlineType.get() == OutlineType.Gradient)).build());
        this.outlineGradientEnd = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("outline-gradient-end")).defaultValue(new SettingColor(100, 100, 255)).visible(() -> (Boolean)this.playerTags.get() != false && this.outlineType.get() == OutlineType.Gradient)).build());
        this.distanceType = this.sgPlayer.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("distance")).defaultValue((Object)DistanceType.Colored)).visible(() -> this.playerTags.get())).build());
        this.distanceColor = this.sgPlayer.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-color")).defaultValue(new SettingColor(Color.WHITE)).visible(() -> (Boolean)this.playerTags.get() != false && this.distanceType.get() == DistanceType.Custom)).build());
        this.showPlayerSkin = this.sgPlayer.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-skin")).description("Show the player's face icon next to their name.")).defaultValue((Object)true)).visible(() -> this.playerTags.get())).build());
        this.playerSkinScale = this.sgPlayer.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("skin-scale")).defaultValue(1.0).sliderRange(0.5, 3.0).decimalPlaces(1).visible(() -> (Boolean)this.playerTags.get() != false && (Boolean)this.showPlayerSkin.get() != false)).build());
        this.showItems = this.sgPlayer.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-items")).description("Show armor and held items above the nametag.")).defaultValue((Object)true)).visible(() -> this.playerTags.get())).build());
        this.itemScale = this.sgPlayer.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("item-scale")).defaultValue(1.0).sliderRange(0.5, 2.0).decimalPlaces(1).visible(() -> (Boolean)this.playerTags.get() != false && (Boolean)this.showItems.get() != false)).build());
        this.rowSpacing = this.sgPlayer.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("row-spacing")).description("Vertical spacing between nametag rows.")).defaultValue(2.0).sliderRange(0.0, 10.0).decimalPlaces(1).visible(() -> this.playerTags.get())).build());
        this.renderPearl = this.sgPearl.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("pearl-rendering")).description("Choose whether to render only pearls that have been assigned an owner or render all pearls.")).defaultValue((Object)PearlRenderingType.Assigned)).build());
        this.outlineColor = this.sgPearl.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("assigned-rendering-color")).description("Outline color of a pearl whose owner has been resolved.")).defaultValue(new SettingColor(Color.MAGENTA)).visible(() -> this.renderPearl.get() == PearlRenderingType.Assigned || this.renderPearl.get() == PearlRenderingType.All)).build());
        this.unknownColor = this.sgPearl.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("unknown-rendering-color")).description("Outline color of a pearl whose owner is still unresolved.")).defaultValue(new SettingColor(Color.WHITE)).visible(() -> this.renderPearl.get() == PearlRenderingType.All)).build());
        this.tagDisplay = this.sgPearl.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("tag-display")).description("What to display above the pearl.")).defaultValue((Object)TagDisplayType.NameAndSkin)).build());
        this.tagDistanceScaling = this.sgPearl.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tag-distance-scaling")).description("Scales the tag automatically based on the distance from the player.")).defaultValue((Object)true)).visible(() -> this.tagDisplay.get() != TagDisplayType.None)).build());
        this.lockTagPos = this.sgPearl.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lock-tag")).description("Locks the tag's position 1 block above the pearl (stops it from bobbing in stasis chambers).")).defaultValue((Object)true)).build());
        this.tagColor = this.sgPearl.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("tag-color")).description("Color of the owner's tag rendered above a pearl.")).defaultValue(new SettingColor(Color.CYAN)).visible(() -> this.tagDisplay.get() != TagDisplayType.None)).build());
        this.scale = this.sgPearl.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("tag-scale")).description("Scale of the nametag rendered above pearls.")).defaultValue(1.0).decimalPlaces(1).visible(() -> this.tagDisplay.get() != TagDisplayType.None)).build());
        this.skinScale = this.sgPearl.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("skin-scale")).description("Scale of the player face icon rendered above a pearl.")).defaultValue(1.0).decimalPlaces(1).visible(() -> this.tagDisplay.get() == TagDisplayType.SkinOnly || this.tagDisplay.get() == TagDisplayType.NameAndSkin)).build());
        this.pendingPearls = new HashSet<Integer>();
        this.pearlCountMap = new HashMap<Integer, Integer>();
        this.skinTextureMap = new HashMap<String, Identifier>();
        this.loadingSkins = new HashSet<String>();
    }

    public void onActivate() {
        PearlStore.load();
        if (this.mc.world == null) {
            return;
        }
        for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity pearl = (LivingEntity)entity;
            Entity owner = pearl.getOwner();
            if (owner != null) {
                String ownerName = owner.getName().getString();
                pearlOwnerMap.put(pearl.getId(), ownerName);
                BlockPos chestPos = this.findNearbyEnderChest(pearl);
                if (chestPos == null) continue;
                PearlStore.addOrUpdate(ownerName, chestPos, pearl.getId());
                continue;
            }
            BlockPos nearbyChest = this.findNearbyEnderChest(pearl);
            if (nearbyChest != null) {
                PearlStore.PearlRecord record = PearlStore.getByPos(nearbyChest);
                if (record != null) {
                    pearlOwnerMap.put(pearl.getId(), record.owner);
                    PearlStore.updateEntityId(nearbyChest, pearl.getId());
                    continue;
                }
            }
            Integer countId = this.pearlCountMap.get(pearl.getId());
            if (countId != null) {
                Entity countEntity = this.mc.world.getEntityById(countId.intValue());
                if (countEntity instanceof PlayerEntity player) {
                    String playerName = player.getName().getString();
                    pearlOwnerMap.put(pearl.getId(), playerName);
                    if (nearbyChest != null) {
                        PearlStore.addOrUpdate(playerName, nearbyChest, pearl.getId());
                    }
                    continue;
                }
            }
            Iterator<PearlStore.PearlRecord> it = PearlStore.getPearls().iterator();
            while (it.hasNext()) {
                PearlStore.PearlRecord pearlRecord = it.next();
                if (pearlRecord.BLEzYuOlPg4yGLp != pearl.getId()) continue;
                pearlOwnerMap.put(pearl.getId(), pearlRecord.owner);
                PearlStore.updateEntityId(pearlRecord.pos, pearl.getId());
                break;
            }
            this.pendingPearls.add(pearl.getId());
        }
    }

    public void onDeactivate() {
        pearlOwnerMap.clear();
        this.pendingPearls.clear();
        this.skinTextureMap.forEach((string, identifier) -> this.mc.getTextureManager().destroyTexture(identifier));
        this.skinTextureMap.clear();
        this.loadingSkins.clear();
    }

    @EventHandler(priority=-200)
    private void onEntityAdded(EntityAddedEvent entityAddedEvent) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        Object object = entityAddedEvent.entity;
        if (object instanceof LivingEntity) {
            LivingEntity LivingEntity2 = (LivingEntity)object;
            this.processPearlEntity(LivingEntity2);
        } else {
            object = entityAddedEvent.entity;
            if (object instanceof PlayerEntity) {
                PlayerEntity PlayerEntity2 = (PlayerEntity)object;
                if (this.skinTextureMap.containsKey(object = PlayerEntity2.getName().getString())) {
                    this.mc.getTextureManager().destroyTexture(this.skinTextureMap.get(object));
                    this.skinTextureMap.remove(object);
                }
                this.pearlCountMap.entrySet().removeIf(entry -> {
                    if (((Integer)entry.getValue()).intValue() == PlayerEntity2.getId()) {
                        LivingEntity LivingEntity2;
                        BlockPos BlockPos2;
                        int n = (Integer)entry.getKey();
                        pearlOwnerMap.put(n, PlayerEntity2.getName().getString());
                        Entity Entity2 = this.mc.world.getEntityById(n);
                        if (Entity2 instanceof LivingEntity && (BlockPos2 = this.findNearbyEnderChest(LivingEntity2 = (LivingEntity)Entity2)) != null) {
                            PearlStore.jOdDDFXSeWl4(PlayerEntity2.getName().getString(), BlockPos2, n);
                        }
                        this.pendingPearls.remove(n);
                        return true;
                    }
                    return false;
                });
                this.resolvePendingPearls();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.world == null || this.pendingPearls.isEmpty()) {
            return;
        }
        this.resolvePendingPearls();
    }

    private void processPearlEntity(LivingEntity LivingEntity2) {
        PearlStore.PearlRecord pearlRecord;
        Entity Entity2 = LivingEntity2.getOwner();
        if (Entity2 != null) {
            String string = Entity2.getName().getString();
            pearlOwnerMap.put(LivingEntity2.getId(), string);
            BlockPos BlockPos2 = this.findNearbyEnderChest(LivingEntity2);
            if (BlockPos2 != null) {
                PearlStore.addOrUpdate(string, BlockPos2, LivingEntity2.getId());
            }
            return;
        }
        BlockPos BlockPos3 = this.findNearbyEnderChest(LivingEntity2);
        if (BlockPos3 != null && (pearlRecord = PearlStore.getByPos(BlockPos3)) != null) {
            pearlOwnerMap.put(LivingEntity2.getId(), pearlRecord.owner);
            PearlStore.addOrUpdate(BlockPos3, LivingEntity2.getId());
            return;
        }
        this.pendingPearls.add(LivingEntity2.getId());
    }

    private void resolvePendingPearls() {
        this.pendingPearls.removeIf(n -> {
            Entity Entity2 = this.mc.world.getEntityById(n.intValue());
            if (Entity2 == null) {
                return true;
            }
            if (!(Entity2 instanceof LivingEntity)) {
                return true;
            }
            LivingEntity LivingEntity2 = (LivingEntity)Entity2;
            UUID uUID = this.getOwnerUuid(LivingEntity2);
            if (uUID == null) {
                return false;
            }
            PlayerEntity PlayerEntity2 = this.mc.world.getPlayers().stream().filter(p -> p.getUuid().equals(uUID)).findFirst().orElse(null);
            if (PlayerEntity2 == null) {
                return false;
            }
            String string = PlayerEntity2.getName().getString();
            pearlOwnerMap.put((Integer)n, string);
            BlockPos BlockPos2 = this.findNearbyEnderChest(LivingEntity2);
            if (BlockPos2 != null) {
                PearlStore.addOrUpdate(string, BlockPos2, n);
            }
            return true;
        });
    }

    private BlockPos findNearbyEnderChest(LivingEntity LivingEntity2) {
        if (this.mc.world == null) {
            return null;
        }
        BlockPos BlockPos2 = LivingEntity2.getBlockPos();
        for (BlockPos BlockPos3 : new BlockPos[]{BlockPos2, BlockPos2.up()}) {
            if (!(this.mc.world.getBlockState(BlockPos3).getBlock() instanceof EnderChestBlock)) continue;
            return BlockPos3;
        }
        return null;
    }

    public void setPearlCount(int n, int n2) {
        this.pearlCountMap.put(n, n2);
    }

    private UUID getOwnerUuid(LivingEntity LivingEntity2) {
        try {
            Field field = ProjectileEntity.class.getDeclaredField("ownerUuid");
            field.setAccessible(true);
            return (UUID)field.get(LivingEntity2);
        }
        catch (Exception exception) {
            return null;
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent render2DEvent) {
        if (this.tagDisplay.get() == TagDisplayType.None || this.mc.world == null || pearlOwnerMap.isEmpty()) {
            return;
        }
        boolean bl = this.tagDisplay.get() == TagDisplayType.SkinOnly || this.tagDisplay.get() == TagDisplayType.NameAndSkin;
        boolean bl2 = this.tagDisplay.get() == TagDisplayType.NameOnly || this.tagDisplay.get() == TagDisplayType.NameAndSkin;
        for (Map.Entry<Integer, String> entry : pearlOwnerMap.entrySet()) {
            float f;
            boolean bl3;
            Entity Entity2 = this.mc.world.getEntityById(entry.getKey().intValue());
            if (!(Entity2 instanceof LivingEntity)) continue;
            LivingEntity LivingEntity2 = (LivingEntity)Entity2;
            String string = entry.getValue();
            if (bl) {
                this.loadSkin(string);
            }
            Vec3d Vec3d2 = LivingEntity2.getLerpedPos(render2DEvent.tickDelta);
            double d = Vec3d2.y + (double)LivingEntity2.getHeight() + 0.5;
            Vector3d vector3d = new Vector3d(Vec3d2.x, (Boolean)this.lockTagPos.get() == false ? d : Math.ceil(d), Vec3d2.z);
            if (!NametagUtils.to2D((Vector3d)vector3d, (double)((Double)this.scale.get()), (boolean)((Boolean)this.tagDistanceScaling.get()))) continue;
            NametagUtils.begin((Vector3d)vector3d);
            TextRenderer textRenderer = TextRenderer.get();
            float f2 = (float)(8.0 * (Double)this.skinScale.get() * 2.0);
            boolean bl4 = bl3 = bl && this.skinTextureMap.containsKey(string);
            if (bl3) {
                Identifier Identifier2 = this.skinTextureMap.get(string);
                float f3 = bl2 ? -f2 - 2.0f : -f2 / 2.0f;
                f = -f2 / 2.0f;
                VersionHelper.get().drawSkinTexture(Identifier2, f3, f, f2, f2);
            }
            if (bl2) {
                textRenderer.beginBig();
                double d2 = textRenderer.getWidth(string);
                f = bl3 ? 2.0f : (float)(-d2 / 2.0);
                textRenderer.render(string, (double)f, -textRenderer.getHeight() / 2.0, (meteordevelopment.meteorclient.utils.render.color.Color)this.tagColor.get());
                textRenderer.end();
            }
            NametagUtils.end();
        }
    }

    @EventHandler
    private void onRenderPlayerTags(Render2DEvent render2DEvent) {
        if (!((Boolean)this.playerTags.get()).booleanValue() || this.mc.world == null) {
            return;
        }
        for (PlayerEntity PlayerEntity2 : this.mc.world.getPlayers()) {
            float f;
            float f2;
            boolean bl;
            boolean bl2 = bl = !((Freecam)Modules.get().get(Freecam.class)).isActive() && this.mc.options.getPerspective().isFirstPerson();
            if (PlayerEntity2 == this.mc.player && (((Boolean)this.ignoreSelf.get()).booleanValue() || bl)) continue;
            String string = PlayerEntity2.getName().getString();
            boolean bl3 = TagUtils.VYEwzRq(PlayerEntity2);
            double d = TagUtils.vgrtgn5(PlayerEntity2);
            if (((Boolean)this.showPlayerSkin.get()).booleanValue()) {
                this.loadSkin(string);
            }
            Vec3d Vec3d2 = PlayerEntity2.getLerpedPos(render2DEvent.tickDelta).add(0.0, (double)PlayerEntity2.getEyeHeight(PlayerEntity2.getPose()) + 0.6, 0.0);
            Vector3d vector3d = new Vector3d(Vec3d2.x, Vec3d2.y, Vec3d2.z);
            if (!NametagUtils.to2D((Vector3d)vector3d, (double)((Double)this.playerTagScale.get()), (boolean)((Boolean)this.playerTagDistanceScaling.get()))) continue;
            NametagUtils.begin((Vector3d)vector3d);
            TextRenderer textRenderer = TextRenderer.get();
            float f3 = ((Double)this.backgroundPadding.get()).floatValue();
            float f4 = ((Double)this.rowSpacing.get()).floatValue();
            float f5 = (float)(8.0 * (Double)this.playerSkinScale.get() * 2.0);
            boolean bl4 = (Boolean)this.showPlayerSkin.get() != false && this.skinTextureMap.containsKey(string);
            textRenderer.beginBig();
            float f6 = (float)textRenderer.getWidth(string);
            float f7 = (float)textRenderer.getHeight();
            textRenderer.end();
            float f8 = bl4 ? f5 + 2.0f : 0.0f;
            float f9 = f6 + f8;
            float f10 = Math.max(f7, bl4 ? f5 : 0.0f);
            float f11 = -(f9 / 2.0f) - f3;
            float f12 = -(f10 / 2.0f) - f3;
            float f13 = f9 + f3 * 2.0f;
            float f14 = f10 + f3 * 2.0f;
            if (((Boolean)this.showBackground.get()).booleanValue()) {
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad((double)f11, (double)f12, (double)f13, (double)f14, (meteordevelopment.meteorclient.utils.render.color.Color)this.backgroundColor.get());
                VersionHelper.get().renderColorRenderer();
            }
            if (this.outlineType.get() != OutlineType.None) {
                int n;
                int n2;
                if (this.outlineType.get() == OutlineType.Gradient) {
                    n2 = this.toColorInt((SettingColor)this.outlineGradientStart.get());
                    n = this.toColorInt((SettingColor)this.outlineGradientEnd.get());
                } else {
                    n = n2 = this.toColorInt((SettingColor)this.outlineColorPlayer.get());
                }
                this.drawRoundedOutline(f11, f12, f13, f14, ((Double)this.backgroundRounding.get()).floatValue(), n2, n);
            }
            if (bl4) {
                Identifier Identifier2 = this.skinTextureMap.get(string);
                float f15 = -(f9 / 2.0f);
                float f16 = -f5 / 2.0f;
                VersionHelper.get().drawSkinTexture(Identifier2, f15, f16, f5, f5);
            }
            textRenderer.beginBig();
            float f17 = bl4 ? -(f9 / 2.0f) + f8 : -(f6 / 2.0f);
            float f18 = -(f7 / 2.0f);
            if (bl3) {
                textRenderer.render(string, (double)f17, (double)f18, (meteordevelopment.meteorclient.utils.render.color.Color)this.friendColor.get());
            } else if (this.nameColorType.get() == NameColorType.Gradient) {
                this.renderGradientText(textRenderer, string, f17, f18, (SettingColor)this.nameGradientStart.get(), (SettingColor)this.nameGradientEnd.get());
            } else if (this.nameColorType.get() == NameColorType.Custom) {
                textRenderer.render(string, (double)f17, (double)f18, (meteordevelopment.meteorclient.utils.render.color.Color)this.nameColor.get());
            } else {
                textRenderer.render(string, (double)f17, (double)f18, (meteordevelopment.meteorclient.utils.render.color.Color)new SettingColor(255, 255, 255));
            }
            textRenderer.end();
            if (this.distanceType.get() != DistanceType.None) {
                int n;
                String string2 = String.format("[%dm]", (int)d);
                textRenderer.begin();
                f2 = (float)textRenderer.getWidth(string2);
                f = f10 / 2.0f + f4;
                if (this.distanceType.get() == DistanceType.Colored) {
                    n = TagUtils.BX92A0OIIvD9(d);
                } else {
                    SettingColor settingColor = (SettingColor)this.distanceColor.get();
                    n = settingColor.r << 16 | settingColor.g << 8 | settingColor.b;
                }
                int n3 = n >> 16 & 0xFF;
                int n4 = n >> 8 & 0xFF;
                int n5 = n & 0xFF;
                textRenderer.render(string2, (double)(-(f2 / 2.0f)), (double)f, (meteordevelopment.meteorclient.utils.render.color.Color)new SettingColor(n3, n4, n5, 255));
                textRenderer.end();
            }
            if (((Boolean)this.showItems.get()).booleanValue() && TagUtils.setPearlCount(PlayerEntity2)) {
                ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
                arrayList.add(TagUtils.mp3zoXQFKUKYj5(PlayerEntity2));
                arrayList.addAll(TagUtils.jOdDDFXSeWl4(PlayerEntity2));
                arrayList.add(TagUtils.Gt56Sj4a6BWhgB(PlayerEntity2));
                arrayList.removeIf(ItemStack::isEmpty);
                f2 = (float)(16.0 * (Double)this.itemScale.get());
                f = 2.0f;
                float f19 = (float)arrayList.size() * (f2 + f) - f;
                float f20 = -f19 / 2.0f;
                float f21 = -(f10 / 2.0f) - f3 - f2 - f4;
                for (ItemStack ItemStack2 : arrayList) {
                    RenderUtils.drawItem((DrawContext)render2DEvent.drawContext, (ItemStack)ItemStack2, (int)((int)f20), (int)((int)f21), (float)((Double)this.itemScale.get()).floatValue(), (boolean)true);
                    if (ItemStack2.getCount() > 1) {
                        String string3 = String.valueOf(ItemStack2.getCount());
                        textRenderer.begin(0.5, false, true);
                        float f22 = (float)textRenderer.getWidth(string3);
                        textRenderer.render(string3, (double)(f20 + f2 - f22 - 1.0f), (double)(f21 + f2 - (float)textRenderer.getHeight()), (meteordevelopment.meteorclient.utils.render.color.Color)new SettingColor(255, 255, 255));
                        textRenderer.end();
                    }
                    f20 += f2 + f;
                }
            }
            NametagUtils.end();
        }
    }

    /** Returns true if the given pearl entity should have a tag rendered above it. */
    public boolean shouldRenderPearlTag(LivingEntity LivingEntity2) { // was: TAdu5cndwWu3A1
        if (!this.isActive() || this.renderPearl.get() == PearlRenderingType.None) {
            return false;
        }
        int n = LivingEntity2.getId();
        return switch (((PearlRenderingType)((Object)this.renderPearl.get())).ordinal()) {
            case 1 -> pearlOwnerMap.containsKey(n);
            case 2 -> true;
            default -> false;
        };
    }

    /** Returns the outline color (packed RGB int) for the given pearl entity. */
    public int getPearlOutlineColor(LivingEntity LivingEntity2) { // was: vgrtgn5
        SettingColor settingColor = pearlOwnerMap.containsKey(LivingEntity2.getId()) ? (SettingColor)this.outlineColor.get() : (SettingColor)this.unknownColor.get();
        return settingColor.r << 16 | settingColor.g << 8 | settingColor.b;
    }

    /** Asynchronously fetches and registers the skin texture for the given player name. */
    private void loadSkin(String string) { // was: e5oi2ZF
        if (this.skinTextureMap.containsKey(string) || this.loadingSkins.contains(string)) {
            return;
        }
        this.loadingSkins.add(string);
        Thread thread = new Thread(() -> {
            try {
                URL uRL = new URL("https://mc-heads.net/avatar/" + string + "/64");
                HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setRequestProperty("User-Agent", "musheor-addon");
                BufferedImage bufferedImage = ImageIO.read(httpURLConnection.getInputStream());
                if (bufferedImage == null) {
                    this.loadingSkins.remove(string);
                    return;
                }
                boolean bl = this.isOnSameServer(string);
                if (!bl) {
                    bufferedImage = this.toGrayscale(bufferedImage);
                }
                NativeImage nativeImage = this.toNativeImage(bufferedImage);
                Identifier Identifier2 = Identifier.of((String)"musheor", (String)("pearl_skin_" + string.toLowerCase()));
                this.mc.execute(() -> {
                    VersionHelper.get().registerSkinTexture(nativeImage, string, Identifier2);
                    this.skinTextureMap.put(string, Identifier2);
                    this.loadingSkins.remove(string);
                });
            }
            catch (Exception exception) {
                this.loadingSkins.remove(string);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /** Returns true if the given player name is on the same server as the local player. */
    private boolean isOnSameServer(String string) { // was: BX92A0OIIvD9
        return VersionHelper.get().onSameServer(string);
    }

    /** Converts a BufferedImage to grayscale (used for offline/unknown players). */
    private BufferedImage toGrayscale(BufferedImage bufferedImage) { // was: jOdDDFXSeWl4(BufferedImage)
        BufferedImage bufferedImage2 = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), 2);
        for (int i = 0; i < bufferedImage.getWidth(); ++i) {
            for (int j = 0; j < bufferedImage.getHeight(); ++j) {
                int n = bufferedImage.getRGB(i, j);
                int n2 = n >> 24 & 0xFF;
                int n3 = n >> 16 & 0xFF;
                int n4 = n >> 8 & 0xFF;
                int n5 = n & 0xFF;
                int n6 = (int)(0.299 * (double)n3 + 0.587 * (double)n4 + 0.114 * (double)n5);
                bufferedImage2.setRGB(i, j, n2 << 24 | n6 << 16 | n6 << 8 | n6);
            }
        }
        return bufferedImage2;
    }

    /** Converts a BufferedImage to a NativeImage for registration as a texture. */
    private NativeImage toNativeImage(BufferedImage bufferedImage) { // was: mp3zoXQFKUKYj5(BufferedImage)
        NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int i = 0; i < bufferedImage.getWidth(); ++i) {
            for (int j = 0; j < bufferedImage.getHeight(); ++j) {
                nativeImage.setColorArgb(i, j, bufferedImage.getRGB(i, j));
            }
        }
        return nativeImage;
    }

    /** Draws a rounded outline rectangle with gradient color from n to n2. */
    private void drawRoundedOutline(float f, float f2, float f3, float f4, float f5, int n, int n2) { // was: jOdDDFXSeWl4(float,float,float,float,float,int,int)
        if (f5 < 0.0f) {
            f5 = 0.0f;
        }
        int n3 = 10;
        float[][] fArrayArray = new float[][]{{f + f5, f2 + f5}, {f + f3 - f5, f2 + f5}, {f + f3 - f5, f2 + f4 - f5}, {f + f5, f2 + f4 - f5}};
        double[] dArray = new double[]{Math.PI, 4.71238898038469, 0.0, 1.5707963267948966};
        Renderer2D.COLOR.begin();
        float f6 = 0.0f;
        float f7 = 0.0f;
        boolean bl = true;
        for (int i = 0; i < 4; ++i) {
            float f8;
            float f9;
            int n4;
            for (n4 = 0; n4 <= n3; ++n4) {
                double d = dArray[i] + (double)n4 * 1.5707963267948966 / (double)n3;
                f9 = fArrayArray[i][0] + (float)(Math.cos(d) * (double)f5);
                f8 = fArrayArray[i][1] + (float)(Math.sin(d) * (double)f5);
                if (!bl) {
                    this.drawOutlineSegment(f6, f7, f9, f8, f, f3, n, n2);
                }
                bl = false;
                f6 = f9;
                f7 = f8;
            }
            n4 = (i + 1) % 4;
            float f10 = fArrayArray[n4][0] + (float)(Math.cos(dArray[n4]) * (double)f5);
            float f11 = fArrayArray[n4][1] + (float)(Math.sin(dArray[n4]) * (double)f5);
            f9 = (f10 - f6) / (float)n3;
            f8 = (f11 - f7) / (float)n3;
            for (int j = 0; j < n3; ++j) {
                float f12 = f6 + f9;
                float f13 = f7 + f8;
                this.drawOutlineSegment(f6, f7, f12, f13, f, f3, n, n2);
                f6 = f12;
                f7 = f13;
            }
        }
        VersionHelper.get().renderColorRenderer();
    }

    /** Draws a single outline segment with interpolated gradient color. */
    private void drawOutlineSegment(float f, float f2, float f3, float f4, float f5, float f6, int n, int n2) { // was: jOdDDFXSeWl4(float,float,float,float,float,float,int,int)
        float f7 = Math.max(0.0f, Math.min(1.0f, ((f + f3) / 2.0f - f5) / f6));
        int n3 = TagUtils.jOdDDFXSeWl4(n, n2, f7);
        Renderer2D.COLOR.line((double)f, (double)f2, (double)f3, (double)f4, (meteordevelopment.meteorclient.utils.render.color.Color)new SettingColor(n3 >> 16 & 0xFF, n3 >> 8 & 0xFF, n3 & 0xFF, 255));
    }

    /** Renders a string with a left-to-right gradient between two colors. */
    private void renderGradientText(TextRenderer textRenderer, String string, float f, float f2, SettingColor settingColor, SettingColor settingColor2) { // was: jOdDDFXSeWl4(TextRenderer,String,float,float,SettingColor,SettingColor)
        float f3 = f;
        float f4 = (float)textRenderer.getWidth(string);
        for (int i = 0; i < string.length(); ++i) {
            String string2 = String.valueOf(string.charAt(i));
            float f5 = f4 > 0.0f ? (f3 - f) / f4 : 0.0f;
            int n = TagUtils.jOdDDFXSeWl4(this.toColorInt(settingColor), this.toColorInt(settingColor2), f5);
            SettingColor settingColor3 = new SettingColor(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, 255);
            textRenderer.render(string2, (double)f3, (double)f2, (meteordevelopment.meteorclient.utils.render.color.Color)settingColor3);
            f3 += (float)textRenderer.getWidth(string2);
        }
    }

    /** Packs a SettingColor into an RGB int. */
    private int toColorInt(SettingColor settingColor) { // was: jOdDDFXSeWl4(SettingColor)
        return settingColor.r << 16 | settingColor.g << 8 | settingColor.b;
    }

    static final class NameColorType
    extends Enum<NameColorType> {
        public static final /* enum */ NameColorType Default = new NameColorType();  // was: YqwfVX
        public static final /* enum */ NameColorType Custom = new NameColorType();   // was: TQkkPszTZ
        public static final /* enum */ NameColorType Gradient = new NameColorType(); // was: Mz2EP5
        private static final /* synthetic */ NameColorType[] $VALUES;

        public static NameColorType[] values() {
            return (NameColorType[])$VALUES.clone();
        }

        public static NameColorType valueOf(String string) {
            return Enum.valueOf(NameColorType.class, string);
        }

        private static /* synthetic */ NameColorType[] $values() {
            return new NameColorType[]{Default, Custom, Gradient};
        }

        static {
            $VALUES = NameColorType.$values();
        }
    }

    static final class OutlineType
    extends Enum<OutlineType> {
        public static final /* enum */ OutlineType None = new OutlineType();     // was: Tz7qNAG6
        public static final /* enum */ OutlineType Solid = new OutlineType();    // was: yQzOzveN8BjKJN5
        public static final /* enum */ OutlineType Gradient = new OutlineType(); // was: merMGMToO0ZYtkEn
        private static final /* synthetic */ OutlineType[] $VALUES;

        public static OutlineType[] values() {
            return (OutlineType[])$VALUES.clone();
        }

        public static OutlineType valueOf(String string) {
            return Enum.valueOf(OutlineType.class, string);
        }

        private static /* synthetic */ OutlineType[] $values() {
            return new OutlineType[]{None, Solid, Gradient};
        }

        static {
            $VALUES = OutlineType.$values();
        }
    }

    static final class DistanceType
    extends Enum<DistanceType> {
        public static final /* enum */ DistanceType None = new DistanceType();    // was: J9PESj
        public static final /* enum */ DistanceType Custom = new DistanceType();  // was: IQLoNzzVjej0Fh
        public static final /* enum */ DistanceType Colored = new DistanceType(); // was: xf86w8EXQDMegty
        private static final /* synthetic */ DistanceType[] $VALUES;

        public static DistanceType[] values() {
            return (DistanceType[])$VALUES.clone();
        }

        public static DistanceType valueOf(String string) {
            return Enum.valueOf(DistanceType.class, string);
        }

        private static /* synthetic */ DistanceType[] $values() {
            return new DistanceType[]{None, Custom, Colored};
        }

        static {
            $VALUES = DistanceType.$values();
        }
    }

    static final class PearlRenderingType
    extends Enum<PearlRenderingType> {
        public static final /* enum */ PearlRenderingType None = new PearlRenderingType();     // was: gaJr0zjHBLiO
        public static final /* enum */ PearlRenderingType Assigned = new PearlRenderingType(); // was: MCTY8c
        public static final /* enum */ PearlRenderingType All = new PearlRenderingType();      // was: qy8UwM99rVr
        private static final /* synthetic */ PearlRenderingType[] $VALUES;

        public static PearlRenderingType[] values() {
            return (PearlRenderingType[])$VALUES.clone();
        }

        public static PearlRenderingType valueOf(String string) {
            return Enum.valueOf(PearlRenderingType.class, string);
        }

        private static /* synthetic */ PearlRenderingType[] $values() {
            return new PearlRenderingType[]{None, Assigned, All};
        }

        static {
            $VALUES = PearlRenderingType.$values();
        }
    }

    static final class TagDisplayType
    extends Enum<TagDisplayType> {
        public static final /* enum */ TagDisplayType None = new TagDisplayType();
        public static final /* enum */ TagDisplayType NameOnly = new TagDisplayType();
        public static final /* enum */ TagDisplayType SkinOnly = new TagDisplayType();
        public static final /* enum */ TagDisplayType NameAndSkin = new TagDisplayType();
        private static final /* synthetic */ TagDisplayType[] $VALUES;

        public static TagDisplayType[] values() {
            return (TagDisplayType[])$VALUES.clone();
        }

        public static TagDisplayType valueOf(String string) {
            return Enum.valueOf(TagDisplayType.class, string);
        }

        private static /* synthetic */ TagDisplayType[] $values() {
            return new TagDisplayType[]{None, NameOnly, SkinOnly, NameAndSkin};
        }

        static {
            $VALUES = TagDisplayType.$values();
        }
    }
}