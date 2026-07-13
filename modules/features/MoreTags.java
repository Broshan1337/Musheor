// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.features;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.utils.PearlStore;
import musheor.utils.TagUtils;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EnderPearlEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

/**
 * "kek-tags" (WIP) — enhanced client nametags. Renders custom player nametags (name colour
 * modes, friend colour, outline, background, distance, skin face and equipment icons) and
 * owner tags/skins above thrown ender pearls resting in trapdoor stasis chambers. Pearl
 * owners are resolved from the pearl's owner entity/UUID, a persisted {@link PearlStore}, or
 * a queued owner-entity id. Player face skins are fetched from mc-heads.net on a background
 * thread and greyscaled for offline players.
 */
public class MoreTags extends Module {
    private final SettingGroup sgPlayer = this.settings.createGroup("Player"); // was: psJq59YIbp3Z
    private final SettingGroup sgPearl = this.settings.createGroup("Pearl");   // was: SOYyh5IPg26f7F

    public final Setting<Boolean> enabled = sgPlayer.add(new BoolSetting.Builder() // was: FvaNWO
        .name("enabled").description("Show custom nametags above players.").defaultValue(true).build());
    private final Setting<Double> scale = sgPlayer.add(new DoubleSetting.Builder() // was: rKbT3Ifwo
        .name("scale").description("Scale of the player nametag.").defaultValue(1.0).sliderRange(0.5, 3.0).decimalPlaces(1).visible(enabled::get).build());
    private final Setting<Boolean> distanceScaling = sgPlayer.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("distance-scaling").description("Scales the nametag based on distance from the player.").defaultValue(false).visible(enabled::get).build());
    private final Setting<Boolean> ignoreSelf = sgPlayer.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("ignore-self").description("Don't render a nametag above your own player.").defaultValue(true).visible(enabled::get).build());
    private final Setting<NameColorType> nameColorType = sgPlayer.add(new EnumSetting.Builder<NameColorType>() // was: xQr5FhbwpQPWgIQ
        .name("name-color").description("How to color the player's name.").defaultValue(NameColorType.DEFAULT).visible(enabled::get).build());
    private final Setting<SettingColor> nameColor = sgPlayer.add(new ColorSetting.Builder() // was: OMMZL1F3q
        .name("name-color-value").defaultValue(new SettingColor(255, 255, 255)).visible(() -> enabled.get() && nameColorType.get() == NameColorType.CUSTOM).build());
    private final Setting<SettingColor> nameGradientStart = sgPlayer.add(new ColorSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("name-gradient-start").defaultValue(new SettingColor(255, 100, 100)).visible(() -> enabled.get() && nameColorType.get() == NameColorType.GRADIENT).build());
    private final Setting<SettingColor> nameGradientEnd = sgPlayer.add(new ColorSetting.Builder() // was: krxNb5lcQuWA
        .name("name-gradient-end").defaultValue(new SettingColor(100, 100, 255)).visible(() -> enabled.get() && nameColorType.get() == NameColorType.GRADIENT).build());
    private final Setting<SettingColor> friendColor = sgPlayer.add(new ColorSetting.Builder() // was: nt0HZnvBBp
        .name("friend-color").description("Name color for friends.").defaultValue(new SettingColor(Color.GREEN)).visible(enabled::get).build());
    private final Setting<Boolean> background = sgPlayer.add(new BoolSetting.Builder() // was: amz3UB1vE
        .name("background").description("Show a background behind the nametag.").defaultValue(true).visible(enabled::get).build());
    private final Setting<SettingColor> backgroundColor = sgPlayer.add(new ColorSetting.Builder() // was: sBBIyQG5NWq0K
        .name("background-color").defaultValue(new SettingColor(0, 0, 0, 160)).visible(() -> enabled.get() && background.get()).build());
    private final Setting<Double> backgroundPadding = sgPlayer.add(new DoubleSetting.Builder() // was: sZkZ1izAy
        .name("background-padding").defaultValue(6.0).sliderRange(0.0, 20.0).decimalPlaces(1).visible(() -> enabled.get() && background.get()).build());
    private final Setting<OutlineType> outlineType = sgPlayer.add(new EnumSetting.Builder<OutlineType>() // was: QYKUhjp
        .name("outline").defaultValue(OutlineType.OFF).visible(enabled::get).build());
    private final Setting<SettingColor> outlineColor = sgPlayer.add(new ColorSetting.Builder() // was: NIz4xic3Js9
        .name("outline-color").defaultValue(new SettingColor(255, 255, 255, 200)).visible(() -> enabled.get() && outlineType.get() == OutlineType.SOLID).build());
    private final Setting<Double> outlineRounding = sgPlayer.add(new DoubleSetting.Builder() // was: u1WFwbQRSKa
        .name("outline-rounding").defaultValue(3.0).sliderRange(0.0, 10.0).decimalPlaces(1).visible(() -> enabled.get() && outlineType.get() != OutlineType.OFF).build());
    private final Setting<SettingColor> outlineGradientStart = sgPlayer.add(new ColorSetting.Builder() // was: LGDfbZq
        .name("outline-gradient-start").defaultValue(new SettingColor(255, 100, 100)).visible(() -> enabled.get() && outlineType.get() == OutlineType.GRADIENT).build());
    private final Setting<SettingColor> outlineGradientEnd = sgPlayer.add(new ColorSetting.Builder() // was: to3T8DJCDVX8po
        .name("outline-gradient-end").defaultValue(new SettingColor(100, 100, 255)).visible(() -> enabled.get() && outlineType.get() == OutlineType.GRADIENT).build());
    private final Setting<DistanceType> distanceType = sgPlayer.add(new EnumSetting.Builder<DistanceType>() // was: Sd3jEwKuGABy
        .name("distance").defaultValue(DistanceType.DISTANCE_BASED).visible(enabled::get).build());
    private final Setting<SettingColor> distanceColor = sgPlayer.add(new ColorSetting.Builder() // was: kJfFkD47Vh
        .name("distance-color").defaultValue(new SettingColor(Color.WHITE)).visible(() -> enabled.get() && distanceType.get() == DistanceType.CUSTOM).build());
    private final Setting<Boolean> showSkin = sgPlayer.add(new BoolSetting.Builder() // was: ubHptFBRn5bO
        .name("show-skin").description("Show the player's face icon next to their name.").defaultValue(true).visible(enabled::get).build());
    private final Setting<Double> skinScale = sgPlayer.add(new DoubleSetting.Builder() // was: apOpfoOHr3fJVwT
        .name("skin-scale").defaultValue(1.0).sliderRange(0.5, 3.0).decimalPlaces(1).visible(() -> enabled.get() && showSkin.get()).build());
    private final Setting<Boolean> showItems = sgPlayer.add(new BoolSetting.Builder() // was: hq1pN0qY
        .name("show-items").description("Show armor and held items above the nametag.").defaultValue(true).visible(enabled::get).build());
    private final Setting<Double> itemScale = sgPlayer.add(new DoubleSetting.Builder() // was: ptxWcpd1WV763T5
        .name("item-scale").defaultValue(1.0).sliderRange(0.5, 2.0).decimalPlaces(1).visible(() -> enabled.get() && showItems.get()).build());
    private final Setting<Double> rowSpacing = sgPlayer.add(new DoubleSetting.Builder() // was: DnAk86nuI
        .name("row-spacing").description("Vertical spacing between nametag rows.").defaultValue(2.0).sliderRange(0.0, 10.0).decimalPlaces(1).visible(enabled::get).build());

    private final Setting<PearlRenderingType> pearlRendering = sgPearl.add(new EnumSetting.Builder<PearlRenderingType>() // was: LlN8EpIZKbk
        .name("pearl-rendering").description("Choose whether to render only pearls that have been assigned an owner or render all pearls.").defaultValue(PearlRenderingType.ASSIGNED_ONLY).build());
    private final Setting<SettingColor> assignedColor = sgPearl.add(new ColorSetting.Builder() // was: pgjj9cLYUTE5g
        .name("assigned-rendering-color").description("Outline color of a pearl whose owner has been resolved.").defaultValue(new SettingColor(Color.MAGENTA))
        .visible(() -> pearlRendering.get() == PearlRenderingType.ASSIGNED_ONLY || pearlRendering.get() == PearlRenderingType.ALL).build());
    private final Setting<SettingColor> unknownColor = sgPearl.add(new ColorSetting.Builder() // was: IeStEJRJ9eb3l
        .name("unknown-rendering-color").description("Outline color of a pearl whose owner is still unresolved.").defaultValue(new SettingColor(Color.WHITE))
        .visible(() -> pearlRendering.get() == PearlRenderingType.ALL).build());
    private final Setting<TagDisplayType> tagDisplay = sgPearl.add(new EnumSetting.Builder<TagDisplayType>() // was: sFazojak6ig8QgGq
        .name("tag-display").description("What to display above the pearl.").defaultValue(TagDisplayType.BOTH).build());
    private final Setting<Boolean> tagDistanceScaling = sgPearl.add(new BoolSetting.Builder() // was: ewq603nIlCd9Gbu
        .name("tag-distance-scaling").description("Scales the tag automatically based on the distance from the player.").defaultValue(true).visible(() -> tagDisplay.get() != TagDisplayType.OFF).build());
    private final Setting<Boolean> lockTag = sgPearl.add(new BoolSetting.Builder() // was: ExGM8SQ9Qni
        .name("lock-tag").description("Locks the tag's position 1 block above the pearl (stops it from bobbing in stasis chambers).").defaultValue(true).build());
    private final Setting<SettingColor> tagColor = sgPearl.add(new ColorSetting.Builder() // was: yS4isXf3gAzs
        .name("tag-color").description("Color of the owner's tag rendered above a pearl.").defaultValue(new SettingColor(Color.CYAN)).visible(() -> tagDisplay.get() != TagDisplayType.OFF).build());
    private final Setting<Double> tagScale = sgPearl.add(new DoubleSetting.Builder() // was: eC9HV2bWGX
        .name("tag-scale").description("Scale of the nametag rendered above pearls.").defaultValue(1.0).decimalPlaces(1).visible(() -> tagDisplay.get() != TagDisplayType.OFF).build());
    private final Setting<Double> pearlSkinScale = sgPearl.add(new DoubleSetting.Builder() // was: w9spWeVv3AvI
        .name("skin-scale").description("Scale of the player face icon rendered above a pearl.").defaultValue(1.0).decimalPlaces(1)
        .visible(() -> tagDisplay.get() == TagDisplayType.SKIN || tagDisplay.get() == TagDisplayType.BOTH).build());

    /** pearl entity id -> resolved owner name. */
    public static final Map<Integer, String> pearlOwners = new HashMap<>();     // was: Q90GLXQ0Pef (static)
    private final Set<Integer> pendingPearls = new HashSet<>();                 // was: HvulV2j9tKjohNgh
    private final Map<Integer, Integer> pearlOwnerEntityId = new HashMap<>();   // was: Qco5OF (pearl id -> owner entity id)
    private final Map<String, Identifier> skinTextures = new HashMap<>();       // was: cgqo7J5iR6
    private final Set<String> skinFetchInProgress = new HashSet<>();            // was: u2kcN4vsQhS46w5s

    public MoreTags() {
        super(musheor.MAIN, "kek-tags", "Enhanced nametags for players, pearls and items (WIP)");
    }

    @Override
    public void onActivate() {
        PearlStore.load();
        if (this.mc.world == null) return;
        for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof EnderPearlEntity pearl)) continue;
            Entity owner = pearl.getOwner();
            if (owner != null) {
                String name = owner.getName().getString();
                pearlOwners.put(pearl.getId(), name);
                BlockPos pos = this.getTrapdoorPos(pearl);
                if (pos != null) PearlStore.addOrUpdate(name, pos, pearl.getId());
            } else {
                BlockPos pos = this.getTrapdoorPos(pearl);
                if (pos != null) {
                    PearlStore.PearlRecord record = PearlStore.getByPos(pos);
                    if (record != null) {
                        pearlOwners.put(pearl.getId(), record.owner);
                        PearlStore.updateEntityId(pos, pearl.getId());
                        continue;
                    }
                }
                Integer ownerEntityId = this.pearlOwnerEntityId.get(pearl.getId());
                if (ownerEntityId != null && this.mc.world.getEntityById(ownerEntityId) instanceof PlayerEntity player) {
                    String name = player.getName().getString();
                    pearlOwners.put(pearl.getId(), name);
                    if (pos != null) PearlStore.addOrUpdate(name, pos, pearl.getId());
                } else {
                    for (PearlStore.PearlRecord record : PearlStore.getPearls()) {
                        if (record.entityId != pearl.getId()) continue;
                        pearlOwners.put(pearl.getId(), record.owner);
                        PearlStore.updateEntityId(record.pos, pearl.getId());
                    }
                    this.pendingPearls.add(pearl.getId());
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        pearlOwners.clear();
        this.pendingPearls.clear();
        this.skinTextures.forEach((name, id) -> this.mc.getTextureManager().destroyTexture(id));
        this.skinTextures.clear();
        this.skinFetchInProgress.clear();
    }

    @EventHandler(priority = -200)
    private void onEntityAdded(EntityAddedEvent event) { // was: FvaNWO(EntityAddedEvent)
        if (this.mc.world == null || this.mc.player == null) return;
        if (event.entity instanceof EnderPearlEntity pearl) {
            this.trackPearl(pearl);
        } else if (event.entity instanceof PlayerEntity player) {
            String name = player.getName().getString();
            if (this.skinTextures.containsKey(name)) {
                this.mc.getTextureManager().destroyTexture(this.skinTextures.get(name));
                this.skinTextures.remove(name);
            }
            this.pearlOwnerEntityId.entrySet().removeIf(entry -> {
                if (entry.getValue() == player.getId()) {
                    int pearlId = entry.getKey();
                    pearlOwners.put(pearlId, player.getName().getString());
                    if (this.mc.world.getEntityById(pearlId) instanceof EnderPearlEntity pearl2) {
                        BlockPos pos = this.getTrapdoorPos(pearl2);
                        if (pos != null) PearlStore.addOrUpdate(player.getName().getString(), pos, pearlId);
                    }
                    this.pendingPearls.remove(pearlId);
                    return true;
                }
                return false;
            });
            this.resolvePendingPearls();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) { // was: FvaNWO(Post)
        if (this.mc.world != null && !this.pendingPearls.isEmpty()) this.resolvePendingPearls();
    }

    /** Records a newly-added pearl's owner (directly, from the store, or as pending). */
    private void trackPearl(EnderPearlEntity pearl) { // was: psJq59YIbp3Z(EnderPearlEntity)
        Entity owner = pearl.getOwner();
        if (owner != null) {
            String name = owner.getName().getString();
            pearlOwners.put(pearl.getId(), name);
            BlockPos pos = this.getTrapdoorPos(pearl);
            if (pos != null) PearlStore.addOrUpdate(name, pos, pearl.getId());
        } else {
            BlockPos pos = this.getTrapdoorPos(pearl);
            if (pos != null) {
                PearlStore.PearlRecord record = PearlStore.getByPos(pos);
                if (record != null) {
                    pearlOwners.put(pearl.getId(), record.owner);
                    PearlStore.updateEntityId(pos, pearl.getId());
                    return;
                }
            }
            this.pendingPearls.add(pearl.getId());
        }
    }

    /** Tries to resolve any pending pearls' owners from their (reflected) owner UUID. */
    private void resolvePendingPearls() { // was: FvaNWO()
        this.pendingPearls.removeIf(id -> {
            Entity entity = this.mc.world.getEntityById(id);
            if (entity == null) return true;
            if (!(entity instanceof EnderPearlEntity pearl)) return true;
            UUID ownerUuid = this.getOwnerUuid(pearl);
            if (ownerUuid == null) return false;
            PlayerEntity owner = this.mc.world.getPlayers().stream().filter(p -> p.getUuid().equals(ownerUuid)).findFirst().orElse(null);
            if (owner == null) return false;
            String name = owner.getName().getString();
            pearlOwners.put(id, name);
            BlockPos pos = this.getTrapdoorPos(pearl);
            if (pos != null) PearlStore.addOrUpdate(name, pos, id);
            return true;
        });
    }

    /** The trapdoor position the pearl is resting on (at its block, or one below), or null. */
    private BlockPos getTrapdoorPos(EnderPearlEntity pearl) { // was: SOYyh5IPg26f7F(EnderPearlEntity)
        if (this.mc.world == null) return null;
        BlockPos base = pearl.getBlockPos();
        for (BlockPos candidate : new BlockPos[]{base.down(), base}) {
            if (this.mc.world.getBlockState(candidate).getBlock() instanceof TrapdoorBlock) return candidate;
        }
        return null;
    }

    /** Assigns a pearl's owner by entity id (called externally when the owner is known). */
    public void assignPearlOwner(int pearlId, int ownerEntityId) { // was: FvaNWO(int,int)
        this.pearlOwnerEntityId.put(pearlId, ownerEntityId);
    }

    /** Reads the private {@code ownerUuid} field of the pearl's projectile via reflection. */
    private UUID getOwnerUuid(EnderPearlEntity pearl) { // was: rKbT3Ifwo(EnderPearlEntity)
        try {
            Field f = ProjectileEntity.class.getDeclaredField("ownerUuid");
            f.setAccessible(true);
            return (UUID) f.get(pearl);
        } catch (Exception e) {
            return null;
        }
    }

    @EventHandler
    private void renderPearlTags(Render2DEvent event) { // was: FvaNWO(Render2DEvent)
        if (this.tagDisplay.get() == TagDisplayType.OFF || this.mc.world == null || pearlOwners.isEmpty()) return;
        boolean showSkin = this.tagDisplay.get() == TagDisplayType.SKIN || this.tagDisplay.get() == TagDisplayType.BOTH;
        boolean showName = this.tagDisplay.get() == TagDisplayType.NAME || this.tagDisplay.get() == TagDisplayType.BOTH;

        for (Map.Entry<Integer, String> entry : pearlOwners.entrySet()) {
            if (!(this.mc.world.getEntityById(entry.getKey()) instanceof EnderPearlEntity pearl)) continue;
            String playerName = entry.getValue();
            if (showSkin) this.fetchSkin(playerName);

            Vec3d lerpedPos = pearl.getLerpedPos(event.tickDelta);
            double lerpedY = lerpedPos.y + pearl.getHeight() + 0.5;
            Vector3d pos = new Vector3d(lerpedPos.x, !this.lockTag.get() ? lerpedY : Math.ceil(lerpedY), lerpedPos.z);
            if (NametagUtils.to2D(pos, this.tagScale.get(), this.tagDistanceScaling.get())) {
                NametagUtils.begin(pos);
                TextRenderer textRenderer = TextRenderer.get();
                float iconSize = (float) (8.0 * this.pearlSkinScale.get() * 2.0);
                boolean hasSkin = showSkin && this.skinTextures.containsKey(playerName);
                if (hasSkin) {
                    Identifier skinId = this.skinTextures.get(playerName);
                    float x0 = showName ? -iconSize - 2.0F : -iconSize / 2.0F;
                    float y0 = -iconSize / 2.0F;
                    VersionHelper.get().drawSkinTexture(skinId, x0, y0, iconSize, iconSize);
                }
                if (showName) {
                    textRenderer.beginBig();
                    double w = textRenderer.getWidth(playerName);
                    float nameX = hasSkin ? 2.0F : (float) (-w / 2.0);
                    textRenderer.render(playerName, nameX, -textRenderer.getHeight() / 2.0, this.tagColor.get());
                    textRenderer.end();
                }
                NametagUtils.end();
            }
        }
    }

    @EventHandler
    private void renderPlayerTags(Render2DEvent event) { // was: Q90GLXQ0Pef(Render2DEvent)
        if (!this.enabled.get() || this.mc.world == null) return;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            boolean noSpecialView = !((Freecam) Modules.get().get(Freecam.class)).isActive() && this.mc.options.getPerspective().isFirstPerson();
            if (player == this.mc.player && (this.ignoreSelf.get() || noSpecialView)) continue;

            String name = player.getName().getString();
            boolean friend = TagUtils.isFriend(player);
            double distance = TagUtils.distanceTo(player);
            if (this.showSkin.get()) this.fetchSkin(name);

            Vec3d headPos = player.getLerpedPos(event.tickDelta).add(0.0, player.getEyeHeight(player.getPose()) + 0.6, 0.0);
            Vector3d pos = new Vector3d(headPos.x, headPos.y, headPos.z);
            if (!NametagUtils.to2D(pos, this.scale.get(), this.distanceScaling.get())) continue;

            NametagUtils.begin(pos);
            TextRenderer tr = TextRenderer.get();
            float pad = this.backgroundPadding.get().floatValue();
            float spacing = this.rowSpacing.get().floatValue();
            float iconSize = (float) (8.0 * this.skinScale.get() * 2.0);
            boolean hasSkin = this.showSkin.get() && this.skinTextures.containsKey(name);
            tr.beginBig();
            float nameW = (float) tr.getWidth(name);
            float nameH = (float) tr.getHeight();
            tr.end();
            float skinW = hasSkin ? iconSize + 2.0F : 0.0F;
            float totalNameLineW = nameW + skinW;
            float totalNameLineH = Math.max(nameH, hasSkin ? iconSize : 0.0F);
            float bgX = -(totalNameLineW / 2.0F) - pad;
            float bgY = -(totalNameLineH / 2.0F) - pad;
            float bgW = totalNameLineW + pad * 2.0F;
            float bgH = totalNameLineH + pad * 2.0F;

            if (this.background.get()) {
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(bgX, bgY, bgW, bgH, this.backgroundColor.get());
                VersionHelper.get().renderColorRenderer();
            }

            if (this.outlineType.get() != OutlineType.OFF) {
                int colA;
                int colB;
                if (this.outlineType.get() == OutlineType.GRADIENT) {
                    colA = this.toRgb(this.outlineGradientStart.get());
                    colB = this.toRgb(this.outlineGradientEnd.get());
                } else {
                    colA = this.toRgb(this.outlineColor.get());
                    colB = colA;
                }
                this.drawRoundedOutline(bgX, bgY, bgW, bgH, this.outlineRounding.get().floatValue(), colA, colB);
            }

            if (hasSkin) {
                Identifier skinId = this.skinTextures.get(name);
                float skinX = -(totalNameLineW / 2.0F);
                float skinY = -iconSize / 2.0F;
                VersionHelper.get().drawSkinTexture(skinId, skinX, skinY, iconSize, iconSize);
            }

            tr.beginBig();
            float textX = hasSkin ? -(totalNameLineW / 2.0F) + skinW : -(nameW / 2.0F);
            float textY = -(nameH / 2.0F);
            if (friend) {
                tr.render(name, textX, textY, this.friendColor.get());
            } else if (this.nameColorType.get() == NameColorType.GRADIENT) {
                this.renderGradientText(tr, name, textX, textY, this.nameGradientStart.get(), this.nameGradientEnd.get());
            } else if (this.nameColorType.get() == NameColorType.CUSTOM) {
                tr.render(name, textX, textY, this.nameColor.get());
            } else {
                tr.render(name, textX, textY, new SettingColor(255, 255, 255));
            }
            tr.end();

            if (this.distanceType.get() != DistanceType.OFF) {
                String distStr = String.format("[%dm]", (int) distance);
                tr.begin();
                float distW = (float) tr.getWidth(distStr);
                float distY = totalNameLineH / 2.0F + spacing;
                int distCol;
                if (this.distanceType.get() == DistanceType.DISTANCE_BASED) {
                    distCol = TagUtils.distanceColor(distance);
                } else {
                    SettingColor dc = this.distanceColor.get();
                    distCol = dc.r << 16 | dc.g << 8 | dc.b;
                }
                int dr = distCol >> 16 & 0xFF;
                int dg = distCol >> 8 & 0xFF;
                int db = distCol & 0xFF;
                tr.render(distStr, -(distW / 2.0F), distY, new SettingColor(dr, dg, db, 255));
                tr.end();
            }

            if (this.showItems.get() && TagUtils.hasAnyEquipment(player)) {
                List<ItemStack> items = new ArrayList<>();
                items.add(TagUtils.getMainHand(player));
                items.addAll(TagUtils.getArmor(player));
                items.add(TagUtils.getOffHand(player));
                items.removeIf(ItemStack::isEmpty);
                float itemSz = (float) (16.0 * this.itemScale.get());
                float itemGap = 2.0F;
                float totalItemW = items.size() * (itemSz + itemGap) - itemGap;
                float itemStartX = -totalItemW / 2.0F;
                float itemY = -(totalNameLineH / 2.0F) - pad - itemSz - spacing;
                for (ItemStack stack : items) {
                    RenderUtils.drawItem(event.drawContext, stack, (int) itemStartX, (int) itemY, this.itemScale.get().floatValue(), false);
                    if (stack.getCount() > 1) {
                        String countStr = String.valueOf(stack.getCount());
                        tr.begin(0.5, false, true);
                        float countW = (float) tr.getWidth(countStr);
                        tr.render(countStr, itemStartX + itemSz - countW - 1.0F, itemY + itemSz - (float) tr.getHeight(), new SettingColor(255, 255, 255));
                        tr.end();
                    }
                    itemStartX += itemSz + itemGap;
                }
            }

            NametagUtils.end();
        }
    }

    /** True if a pearl should be ESP-rendered under the current pearl-rendering mode. */
    public boolean shouldRenderPearl(EnderPearlEntity pearl) { // was: FvaNWO(EnderPearlEntity)
        if (!this.isActive() || this.pearlRendering.get() == PearlRenderingType.OFF) return false;
        int id = pearl.getId();
        return switch (this.pearlRendering.get()) {
            case ASSIGNED_ONLY -> pearlOwners.containsKey(id);
            case ALL -> true;
            default -> false;
        };
    }

    /** The outline colour (RGB) to render a pearl with, depending on whether its owner is known. */
    public int getPearlOutlineColor(EnderPearlEntity pearl) { // was: Q90GLXQ0Pef(EnderPearlEntity)
        SettingColor color = pearlOwners.containsKey(pearl.getId()) ? this.assignedColor.get() : this.unknownColor.get();
        return color.r << 16 | color.g << 8 | color.b;
    }

    /** Fetches a player's face from mc-heads.net on a background thread and registers it as a texture. */
    private void fetchSkin(String playerName) { // was: FvaNWO(String)
        if (this.skinTextures.containsKey(playerName) || this.skinFetchInProgress.contains(playerName)) return;
        this.skinFetchInProgress.add(playerName);
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://mc-heads.net/avatar/" + playerName + "/64");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "musheor-addon");
                BufferedImage img = ImageIO.read(conn.getInputStream());
                if (img == null) {
                    this.skinFetchInProgress.remove(playerName);
                    return;
                }
                boolean online = this.isOnSameServer(playerName);
                if (!online) img = this.toGrayscale(img);
                NativeImage nativeImage = this.toNativeImage(img);
                Identifier id = Identifier.of("musheor", "pearl_skin_" + playerName.toLowerCase());
                this.mc.execute(() -> {
                    VersionHelper.get().registerSkinTexture(nativeImage, playerName, id);
                    this.skinTextures.put(playerName, id);
                    this.skinFetchInProgress.remove(playerName);
                });
            } catch (Exception e) {
                this.skinFetchInProgress.remove(playerName);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private boolean isOnSameServer(String playerName) { // was: Q90GLXQ0Pef(String)
        return VersionHelper.get().onSameServer(playerName);
    }

    /** Converts an image to greyscale (used for offline players). */
    private BufferedImage toGrayscale(BufferedImage img) { // was: FvaNWO(BufferedImage)
        BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int argb = img.getRGB(x, y);
                int a = argb >> 24 & 0xFF;
                int r = argb >> 16 & 0xFF;
                int g = argb >> 8 & 0xFF;
                int b = argb & 0xFF;
                int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                gray.setRGB(x, y, a << 24 | lum << 16 | lum << 8 | lum);
            }
        }
        return gray;
    }

    private NativeImage toNativeImage(BufferedImage img) { // was: Q90GLXQ0Pef(BufferedImage)
        NativeImage nativeImage = new NativeImage(img.getWidth(), img.getHeight(), false);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                nativeImage.setColorArgb(x, y, img.getRGB(x, y));
            }
        }
        return nativeImage;
    }

    /** Draws a rounded (possibly gradient) outline rectangle. */
    private void drawRoundedOutline(float x, float y, float w, float h, float r, int colA, int colB) { // was: FvaNWO(float x6)
        if (r < 0.0F) r = 0.0F;
        int n = 10;
        float[][] ctr = {{x + r, y + r}, {x + w - r, y + r}, {x + w - r, y + h - r}, {x + r, y + h - r}};
        double[] arcStart = {Math.PI, Math.PI * 3.0 / 2.0, 0.0, Math.PI / 2};
        Renderer2D.COLOR.begin();
        float prevX = 0.0F;
        float prevY = 0.0F;
        boolean first = true;
        for (int c = 0; c < 4; c++) {
            for (int i = 0; i <= n; i++) {
                double a = arcStart[c] + i * (Math.PI / 2) / n;
                float vx = ctr[c][0] + (float) (Math.cos(a) * r);
                float vy = ctr[c][1] + (float) (Math.sin(a) * r);
                if (!first) this.drawGradientLine(prevX, prevY, vx, vy, x, w, colA, colB);
                first = false;
                prevX = vx;
                prevY = vy;
            }
            int nextC = (c + 1) % 4;
            float ex = ctr[nextC][0] + (float) (Math.cos(arcStart[nextC]) * r);
            float ey = ctr[nextC][1] + (float) (Math.sin(arcStart[nextC]) * r);
            float dx = (ex - prevX) / n;
            float dy = (ey - prevY) / n;
            for (int i = 0; i < n; i++) {
                float nx = prevX + dx;
                float ny = prevY + dy;
                this.drawGradientLine(prevX, prevY, nx, ny, x, w, colA, colB);
                prevX = nx;
                prevY = ny;
            }
        }
        VersionHelper.get().renderColorRenderer();
    }

    /** Draws a line segment whose colour is interpolated across the box width. */
    private void drawGradientLine(float x1, float y1, float x2, float y2, float bx, float bw, int colA, int colB) { // was: FvaNWO(float x8)
        float t = Math.max(0.0F, Math.min(1.0F, ((x1 + x2) / 2.0F - bx) / bw));
        int col = TagUtils.lerpColor(colA, colB, t);
        Renderer2D.COLOR.line(x1, y1, x2, y2, new SettingColor(col >> 16 & 0xFF, col >> 8 & 0xFF, col & 0xFF, 255));
    }

    /** Renders text with a per-character horizontal colour gradient. */
    private void renderGradientText(TextRenderer tr, String text, float x, float y, SettingColor start, SettingColor end) { // was: FvaNWO(TextRenderer,...)
        float cursor = x;
        float totalW = (float) tr.getWidth(text);
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            float t = totalW > 0.0F ? (cursor - x) / totalW : 0.0F;
            int col = TagUtils.lerpColor(this.toRgb(start), this.toRgb(end), t);
            SettingColor c = new SettingColor(col >> 16 & 0xFF, col >> 8 & 0xFF, col & 0xFF, 255);
            tr.render(ch, cursor, y, c);
            cursor += (float) tr.getWidth(ch);
        }
    }

    private int toRgb(SettingColor c) { // was: FvaNWO(SettingColor)
        return c.r << 16 | c.g << 8 | c.b;
    }

    /** How the player's name is coloured. */ // was: enum NameColorType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum NameColorType { DEFAULT, CUSTOM, GRADIENT }

    /** Nametag outline style. */ // was: enum OutlineType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum OutlineType { OFF, SOLID, GRADIENT }

    /** How the distance readout is coloured. */ // was: enum DistanceType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum DistanceType { OFF, CUSTOM, DISTANCE_BASED }

    /** Which pearls to ESP-render. */ // was: enum PearlRenderingType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum PearlRenderingType { OFF, ASSIGNED_ONLY, ALL }

    /** What to show above a pearl. */ // was: enum TagDisplayType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F}
    private enum TagDisplayType { OFF, NAME, SKIN, BOTH }
}
