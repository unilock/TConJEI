package me.paypur.tconjei.xplat;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.Material;
import slimeknights.tconstruct.library.tools.definition.PartRequirement;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.ArrayList;
import java.util.List;

public record ToolPartsWrapper(ToolDefinition definition) {

    public static final int WIDTH = 120, HEIGHT = 60;
    public static final int ITEM_SIZE = 16;

    private static final List<Material> MATERIALS = MaterialRegistry.getMaterials()
            .stream()
            .map(material -> (Material) material)
            .filter(material -> !material.isHidden())
            .toList();

    public StationSlotLayout getSlotLayout() {
        return StationSlotLayoutLoader.getInstance().get(definition.getId());
    }

    public ItemStack getTool() {
        return getSlotLayout().getIcon().getValue(ItemStack.class);
    }

    public List<LayoutSlot> getSlots() {
        return getSlotLayout().getInputSlots();
    }

    public boolean isBroadTool() {
        // assumption might not always be true
        return getSlotLayout().getSortIndex() > 8;
    }

    public List<List<ItemStack>> getToolParts() {
        return definition.getData().getParts().stream()
            .map(PartRequirement::getPart)
            .map(part -> MATERIALS.stream()
                    .filter(part::canUseMaterial)
                    .map(material -> part.withMaterial(material.getIdentifier()))
                    .toList()
            ).toList();
    }

    public List<ItemStack> getToolRecipe() {
        List<IToolPart> parts = definition.getData().getParts()
                .stream()
                .map(PartRequirement::getPart)
                .toList();

        // ContentTool
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
             ItemStack item = parts.get(i).withMaterialForDisplay(ToolBuildHandler.getRenderMaterial(i));
             item.getOrCreateTag().putBoolean(TooltipUtil.KEY_DISPLAY, true);
             items.add(item);
        }
        return items;
    }

    public static Point getOffsets(ToolPartsWrapper recipe) {
        List<LayoutSlot> slots = recipe.getSlots();

        int minX, maxX, minY, maxY;
        minX = slots.get(0).getX();
        maxX = slots.get(0).getX();
        minY = slots.get(0).getY();
        maxY = slots.get(0).getY();

        for (int i = 1; i < slots.size(); i++) {
            minX = Math.min(slots.get(i).getX(), minX);
            maxX = Math.max(slots.get(i).getX(), maxX);
            minY = Math.min(slots.get(i).getY(), minY);
            maxY = Math.max(slots.get(i).getY(), maxY);
        }

        // centers slots vertically
        int yOffset = (HEIGHT - (ITEM_SIZE + maxY - minY)) / 2 - minY;
        // centers slots horizontally within square
        int xOffset = (HEIGHT - (ITEM_SIZE + maxX - minX)) / 2 - minX;

        return new Point(xOffset, yOffset);
    }

    public record Point(int x, int y) {}

}
