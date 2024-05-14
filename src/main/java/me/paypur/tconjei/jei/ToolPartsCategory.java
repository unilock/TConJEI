package me.paypur.tconjei.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Collections;
import java.util.List;

import static me.paypur.tconjei.TConJEI.MOD_ID;
import static me.paypur.tconjei.TConJEI.inBox;

public class ToolPartsCategory implements IRecipeCategory<ToolPartsWrapper> {

    final ResourceLocation UID = new ResourceLocation(MOD_ID, "tool_parts");
    final IDrawable BACKGROUND, ICON, ANVIL, SLOT;
    final int WIDTH = 120;
    final int HEIGHT = 60;
    final int ITEM_SIZE = 16;

    public ToolPartsCategory(IGuiHelper guiHelper) {
        this.BACKGROUND = guiHelper.createDrawable(new ResourceLocation(MOD_ID, "textures/gui/toolparts/bg.png"), 0, 0, WIDTH, HEIGHT);
        this.ICON = guiHelper.createDrawableItemStack(TinkerTools.cleaver.get().getRenderTool());
        this.ANVIL = guiHelper.createDrawable(new ResourceLocation(MOD_ID, "textures/gui/toolparts/anvil.png"), 0, 0, 16, 16);
        this.SLOT = guiHelper.createDrawable(new ResourceLocation(MOD_ID, "textures/gui/toolparts/slot.png"), 0, 0, 18, 18);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ToolPartsWrapper recipe, IFocusGroup focuses) {
        recipe.getToolParts().forEach(parts -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(parts));

        List<LayoutSlot> slots = recipe.getSlots();
        List<ItemStack> items = recipe.getToolRecipe();

        assert items.size() == slots.size();

        xy offsets = getOffsets(recipe);
        for (int i = 0; i < items.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, slots.get(i).getX() + offsets.x, slots.get(i).getY() + offsets.y).addItemStack(items.get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 25, (HEIGHT - ITEM_SIZE) / 2).addItemStack(recipe.getTool());
    }

    @Override
    public void draw(ToolPartsWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe.isBroadTool()) {
            this.ANVIL.draw(guiGraphics, 65, 42);
        }

        xy offsets = getOffsets(recipe);
        for (LayoutSlot slot : recipe.getSlots()) {
            // need to offset by 1 because the inventory slot icons are 18x18
            this.SLOT.draw(guiGraphics, slot.getX() + offsets.x - 1, slot.getY() + offsets.y - 1);
        }
    }

    @Override
    public List<Component> getTooltipStrings(ToolPartsWrapper recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return recipe.isBroadTool() && inBox(mouseX, mouseY, 65, 42, ITEM_SIZE, ITEM_SIZE) ?
                Collections.singletonList(Component.literal("Broad tools require a Tinker's Anvil!")) :
                    Collections.emptyList();
    }

    private xy getOffsets(ToolPartsWrapper recipe) {
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

        return new xy(xOffset, yOffset);
    }

    private record xy(int x, int y) {}

    @Override
    public Component getTitle() {
        return Component.literal("Tool Recipe");
    }

    @Override
    public IDrawable getBackground() {
        return this.BACKGROUND;
    }

    @Override
    public IDrawable getIcon() {
        return this.ICON;
    }

    @Override
    public RecipeType<ToolPartsWrapper> getRecipeType() {
        return RecipeType.create(MOD_ID, "tool_parts", ToolPartsWrapper.class);
    }
}
