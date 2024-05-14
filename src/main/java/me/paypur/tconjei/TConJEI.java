package me.paypur.tconjei;

import me.paypur.tconjei.xplat.MaterialStatsWrapper;
import me.paypur.tconjei.xplat.ToolPartsWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.Material;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class TConJEI implements ClientModInitializer {
    public static final String MOD_ID = "tconjei";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Font FONT = Minecraft.getInstance().font;
    public static final int WHITE = 0xFFFFFF;

    public static int TEXT_COLOR = 0x3F3F3F;
    public static int DURABILITY_COLOR = 0x46CA46;
    public static int MINING_COLOR = 0x779ECB;
    public static int ATTACK_COLOR = 0xD46363;

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            private static final ResourceLocation ID = new ResourceLocation(MOD_ID, "reload");

            @Override
            public ResourceLocation getFabricId() {
                return ID;
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                try {
                    ResourceLocation palette = new ResourceLocation(MOD_ID, "textures/gui/palette.png");
                    InputStream stream = resourceManager.getResource(palette).orElseThrow().open();
                    BufferedImage image = ImageIO.read(stream);
                    TEXT_COLOR = image.getRGB(0, 0);
                    DURABILITY_COLOR = image.getRGB(1, 0);
                    MINING_COLOR = image.getRGB(0, 1);
                    ATTACK_COLOR = image.getRGB(1, 1);
                } catch (IOException | NoSuchElementException e) {
                    LOGGER.error("Error loading palette", e);
                }
            }
        });
    }

    public static boolean inBox(double mX, double mY, float x, float y, float w, float h) {
        return (x <= mX && mX <= x + w && y <= mY && mY <= y + h);
    }

    public static List<MaterialStatsWrapper> materials() {
        return MaterialRegistry.getMaterials()
                .stream()
                .filter(iMaterial -> !iMaterial.isHidden())
                .map(stats -> new MaterialStatsWrapper((Material) stats))
                .filter(MaterialStatsWrapper::hasTraits)
                .toList();
    }

    public static List<ToolPartsWrapper> toolDefinitions() {
        return ToolDefinitionLoader.getInstance().getRegisteredToolDefinitions()
                .stream()
                .filter(definition -> definition.isMultipart() && !definition.getId().equals(new ResourceLocation("tconstruct", "slime_helmet")))
                .sorted(Comparator.comparingInt(a -> StationSlotLayoutLoader.getInstance().get(a.getId()).getSortIndex()))
                .map(ToolPartsWrapper::new)
                .toList();
    }
}
