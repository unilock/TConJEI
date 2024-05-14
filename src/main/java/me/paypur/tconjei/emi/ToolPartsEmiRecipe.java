package me.paypur.tconjei.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.paypur.tconjei.xplat.ToolPartsWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;

import java.util.Collections;
import java.util.List;

import static me.paypur.tconjei.TConJEI.*;
import static me.paypur.tconjei.xplat.ToolPartsWrapper.*;

public class ToolPartsEmiRecipe implements EmiRecipe {
    private static final EmiTexture BACKGROUND = new EmiTexture(new ResourceLocation(MOD_ID, "textures/gui/toolparts/bg.png"), 0, 0, WIDTH, HEIGHT);
    private static final EmiTexture ANVIL = new EmiTexture(new ResourceLocation(MOD_ID, "textures/gui/toolparts/anvil.png"), 0, 0, 16, 16);
    private static final EmiTexture SLOT = new EmiTexture(new ResourceLocation(MOD_ID, "textures/gui/toolparts/slot.png"), 0, 0, 18, 18);

    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    private final ToolPartsWrapper recipe;

    public ToolPartsEmiRecipe(ToolPartsWrapper recipe) {
        this.id = recipe.definition().getId();
        this.input = recipe.getToolParts().stream().map(parts -> EmiIngredient.of(parts.stream().map(EmiStack::of).toList())).toList();
        this.output = List.of(EmiStack.of(recipe.getTool()));

        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TConEMIPlugin.TOOL_PARTS;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.output;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0);

        List<LayoutSlot> slots = recipe.getSlots();
        List<ItemStack> items = recipe.getToolRecipe();

        assert items.size() == slots.size();

        Point offsets = getOffsets(recipe);
        for (int i = 0; i < items.size(); i++) {
            // need to offset by 1 because the inventory slot icons are 18x18
            widgets.addTexture(SLOT, slots.get(i).getX() + offsets.x() - 1, slots.get(i).getY() + offsets.y() - 1);
            widgets.addSlot(EmiStack.of(items.get(i)), slots.get(i).getX() + offsets.x(), slots.get(i).getY() + offsets.y()).drawBack(false);
        }

        widgets.addSlot(output.get(0), WIDTH - 25, (HEIGHT - ITEM_SIZE) / 2).drawBack(false).recipeContext(this);

        if (recipe.isBroadTool()) {
            widgets.addTexture(ANVIL, 65, 42);
            widgets.addTooltipText(Collections.singletonList(Component.literal("Broad tools require a Tinker's Anvil!")), 65, 42, ITEM_SIZE, ITEM_SIZE);
        }
    }
}
