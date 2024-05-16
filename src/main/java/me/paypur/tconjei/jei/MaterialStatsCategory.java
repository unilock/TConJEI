package me.paypur.tconjei.jei;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import me.paypur.tconjei.xplat.MaterialStatsWrapper;
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
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.tools.stats.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.fabricators_of_create.porting_lib.util.ForgeI18n.getPattern;
import static me.paypur.tconjei.TConJEI.*;
import static me.paypur.tconjei.xplat.MaterialStatsWrapper.*;

public class MaterialStatsCategory implements IRecipeCategory<MaterialStatsWrapper> {

    final ResourceLocation UID = new ResourceLocation(MOD_ID, "material_stats");
    final IDrawable BACKGROUND, ICON;

    public MaterialStatsCategory(IGuiHelper guiHelper) {
        this.BACKGROUND = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.ICON = guiHelper.createDrawable(new ResourceLocation(MOD_ID, "textures/gui/materialstats/icon.png"), 0, 0, 16, 16);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MaterialStatsWrapper recipe, IFocusGroup focuses) {
        FluidStack fluidStack = recipe.getFluidStack();
        if (!fluidStack.isEmpty()) {
            final int BUCKET = 81000; // droplets
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 18, 0).addFluidStack(fluidStack.getFluid(), BUCKET);
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addFluidStack(fluidStack.getFluid(), BUCKET);
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 0, 0).addItemStacks(recipe.getItemStacks());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(recipe.getItemStacks());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, WIDTH - 16, 0).addItemStacks(recipe.getToolParts());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(recipe.getToolParts());
    }

    @Override
    public void draw(MaterialStatsWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        final String materialName = getPattern(String.format("material.%s.%s", recipe.getMaterialId().getNamespace(), recipe.getMaterialId().getPath()));
        final int MATERIAL_COLOR = MaterialTooltipCache.getColor(recipe.getMaterialId()).getValue();
        float lineNumber = 0;
        // Name
        guiGraphics.drawString(FONT, materialName,  (int) ((WIDTH - FONT.width(materialName)) / 2f), 4, MATERIAL_COLOR, true);
        Optional<HeadMaterialStats> headStats = recipe.getStats(HeadMaterialStats.ID);
        Optional<ExtraMaterialStats> extraStats = recipe.getStats(ExtraMaterialStats.ID);
        Optional<HandleMaterialStats> handleStats = recipe.getStats(HandleMaterialStats.ID);
        Optional<LimbMaterialStats> limbStats = recipe.getStats(LimbMaterialStats.ID);
        Optional<GripMaterialStats> gripStats = recipe.getStats(GripMaterialStats.ID);
        Optional<BowstringMaterialStats> stringStats = recipe.getStats(BowstringMaterialStats.ID);
        // HEAD
        if (headStats.isPresent()) {
            String miningLevelNamespace = headStats.get().getTierId().getNamespace();
            String miningLevelPath = headStats.get().getTierId().getPath();
            drawTraits(guiGraphics, recipe, HeadMaterialStats.ID, lineNumber);
            guiGraphics.drawString(FONT, String.format("[%s]", getPattern("stat.tconstruct.head")), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.valueOf(headStats.get().getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.harvest_tier", getPattern("stat.tconstruct.harvest_tier." + miningLevelNamespace + "." + miningLevelPath), lineNumber++, getMiningLevelColor(miningLevelPath));
            drawStats(guiGraphics, "tool_stat.tconstruct.mining_speed", String.format("%.2f", headStats.get().getMiningSpeed()), lineNumber++, MINING_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_damage", String.format("%.2f", headStats.get().getAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // EXTRA
        // only draw extra if others don't exist
        else if (extraStats.isPresent()) {
            drawTraits(guiGraphics, recipe, ExtraMaterialStats.ID, lineNumber);
            guiGraphics.drawString(FONT, String.format("[%s]", getPattern("stat.tconstruct.extra")), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR);
            lineNumber += 0.4f;
        }
        // HANDLE
        if (handleStats.isPresent()) {
            drawTraits(guiGraphics, recipe, HandleMaterialStats.ID, lineNumber);
            guiGraphics.drawString(FONT, String.format("[%s]", getPattern("stat.tconstruct.handle")), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.format("%.2fx", handleStats.get().getDurability()), lineNumber++, getMultiplierColor(handleStats.get().getDurability()));
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_damage", String.format("%.2fx", handleStats.get().getAttackDamage()), lineNumber++, getMultiplierColor(handleStats.get().getAttackDamage()));
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_speed", String.format("%.2fx", handleStats.get().getAttackSpeed()), lineNumber++, getMultiplierColor(handleStats.get().getAttackSpeed()));
            drawStats(guiGraphics, "tool_stat.tconstruct.mining_speed", String.format("%.2fx", handleStats.get().getMiningSpeed()), lineNumber++, getMultiplierColor(handleStats.get().getMiningSpeed()));
            lineNumber += 0.4f;
        }
        // LIMB
        if (limbStats.isPresent()) {
            drawTraits(guiGraphics, recipe, LimbMaterialStats.ID, lineNumber);
            guiGraphics.drawString(FONT, String.format("[%s]", getPattern("stat.tconstruct.limb")), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.valueOf(limbStats.get().getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.draw_speed", signedString(limbStats.get().getDrawSpeed()), lineNumber++, getDifferenceColor(limbStats.get().getDrawSpeed()));
            drawStats(guiGraphics, "tool_stat.tconstruct.velocity", signedString(limbStats.get().getVelocity()), lineNumber++, getDifferenceColor(limbStats.get().getVelocity()));
            drawStats(guiGraphics, "tool_stat.tconstruct.accuracy", signedString(limbStats.get().getAccuracy()), lineNumber++, getDifferenceColor(limbStats.get().getAccuracy()));
            lineNumber += 0.4f;
        }
        // GRIP
        if (gripStats.isPresent()) {
            drawTraits(guiGraphics, recipe, GripMaterialStats.ID, lineNumber);
            guiGraphics.drawString(FONT, String.format("[%s]", getPattern("stat.tconstruct.grip")), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.format("%.2fx", gripStats.get().getDurability()), lineNumber++, getMultiplierColor(gripStats.get().getDurability()));
            drawStats(guiGraphics, "tool_stat.tconstruct.accuracy", signedString(gripStats.get().getAccuracy()), lineNumber++, getDifferenceColor(gripStats.get().getAccuracy()));
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_damage", String.format("%.2f", gripStats.get().getMeleeAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // STRING
        if (stringStats.isPresent()) {
            drawTraits(guiGraphics, recipe, BowstringMaterialStats.ID, lineNumber);
            guiGraphics.drawString(FONT, String.format("[%s]", getPattern("stat.tconstruct.bowstring")), 0, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR);
        }

    }

    @Override
    public List<Component> getTooltipStrings(MaterialStatsWrapper recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        final String materialNamespace = recipe.getMaterialId().getNamespace();
        final String materialPath = recipe.getMaterialId().getPath();
        float lineNumber = 0;

        // TRAIT
        int matWidth = FONT.width(materialPath);
        if (inBox(mouseX, mouseY, (WIDTH - matWidth) / 2f, 3, matWidth, LINE_HEIGHT)) {
            return List.of(Component.translatable(String.format("material.%s.%s.flavor", materialNamespace, materialPath)).setStyle(Style.EMPTY.withItalic(true).withColor(WHITE)));
        }

        Optional<HeadMaterialStats> headStats = MaterialRegistry.getInstance().getMaterialStats(recipe.getMaterialId(), HeadMaterialStats.ID);
        Optional<ExtraMaterialStats> extraStats = MaterialRegistry.getInstance().getMaterialStats(recipe.getMaterialId(), ExtraMaterialStats.ID);
        Optional<HandleMaterialStats> handleStats = MaterialRegistry.getInstance().getMaterialStats(recipe.getMaterialId(), HandleMaterialStats.ID);
        Optional<LimbMaterialStats> limbStats = MaterialRegistry.getInstance().getMaterialStats(recipe.getMaterialId(), LimbMaterialStats.ID);
        Optional<GripMaterialStats> gripStats = MaterialRegistry.getInstance().getMaterialStats(recipe.getMaterialId(), GripMaterialStats.ID);
        Optional<BowstringMaterialStats> stringStats = MaterialRegistry.getInstance().getMaterialStats(recipe.getMaterialId(), BowstringMaterialStats.ID);
        // HEAD
        if (headStats.isPresent()) {
            Optional<List<Component>> component = Stream.of(
                    getTraitTooltips(recipe, HeadMaterialStats.ID, mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.durability", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.harvest_tier", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.mining_speed", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.attack_damage", mouseX, mouseY, lineNumber++))
                    .filter(list -> !list.isEmpty())
                    .findFirst();
            if (component.isPresent()) {
                return component.get();
            }
            lineNumber += 0.4f;
        }
        // EXTRA
        // only draw extra if others don't exist
        else if (extraStats.isPresent()) {
            List<Component> component = getTraitTooltips(recipe, ExtraMaterialStats.ID, mouseX, mouseY, lineNumber++);
            if (!component.isEmpty()) {
                return component;
            }
            lineNumber += 0.4f;
        }
        // HANDLE
        if (handleStats.isPresent()) {
            Optional<List<Component>> component = Stream.of(
                    getTraitTooltips(recipe, HandleMaterialStats.ID, mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.durability", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.attack_damage", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.attack_speed", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.mining_speed", mouseX, mouseY, lineNumber++))
                    .filter(list -> !list.isEmpty())
                    .findFirst();
            if (component.isPresent()) {
                return component.get();
            }
            lineNumber += 0.4f;
        }
        // LIMB
        if (limbStats.isPresent()) {
            Optional<List<Component>> component = Stream.of(
            getTraitTooltips(recipe, LimbMaterialStats.ID, mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.durability", mouseX, mouseY, lineNumber++),
                    getStatTooltip( "tool_stat.tconstruct.draw_speed", mouseX, mouseY, lineNumber++),
                    getStatTooltip( "tool_stat.tconstruct.velocity", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.accuracy", mouseX, mouseY, lineNumber++))
                    .filter(list -> !list.isEmpty())
                    .findFirst();
            if (component.isPresent()) {
                return component.get();
            }
            lineNumber += 0.4f;
        }
        // GRIP
        if (gripStats.isPresent()) {
            Optional<List<Component>> component = Stream.of(
                    getTraitTooltips(recipe, GripMaterialStats.ID, mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.durability", mouseX, mouseY, lineNumber++),
                    getStatTooltip("tool_stat.tconstruct.accuracy", mouseX, mouseY, lineNumber++),
                    getStatTooltip( "tool_stat.tconstruct.attack_damage", mouseX, mouseY, lineNumber++))
                    .filter(list -> !list.isEmpty())
                    .findFirst();
            if (component.isPresent()) {
                return component.get();
            }
            lineNumber += 0.4f;
        }
        // STRING
        if (stringStats.isPresent()) {
            List<Component> component = getTraitTooltips(recipe, BowstringMaterialStats.ID, mouseX, mouseY, lineNumber);
            if (!component.isEmpty()) {
                return component;
            }
        }

        return Collections.emptyList();
    }

    private void drawStats(GuiGraphics guiGraphics, String type, String stat, float lineNumber, int ACCENT_COLOR) {
        String pattern = getPattern(type);
        float width = FONT.getSplitter().stringWidth(pattern);
        guiGraphics.drawString(FONT, pattern, 0, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), TEXT_COLOR, false);
        guiGraphics.drawString(FONT, stat, (int) width, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), ACCENT_COLOR, false);
    }

    private void drawTraits(GuiGraphics guiGraphics, MaterialStatsWrapper statsWrapper, MaterialStatsId statsId, float lineNumber) {
        final int MATERIAL_COLOR = MaterialTooltipCache.getColor(statsWrapper.getMaterialId()).getValue();
        for (ModifierEntry trait : statsWrapper.getTraits(statsId)) {
            String pattern = getPattern(String.format("modifier.%s.%s", trait.getId().getNamespace(), trait.getId().getPath()));
            guiGraphics.drawString(FONT, pattern, (int) (WIDTH - FONT.getSplitter().stringWidth(pattern)), (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
        }
    }

    private List<Component> getStatTooltip(String pattern, double mouseX, double mouseY, float lineNumber) {
        String string = getPattern(pattern);
        int textWidth = FONT.width(string);
        if (inBox(mouseX, mouseY, 0, lineNumber * LINE_HEIGHT + LINE_OFFSET_HOVER, textWidth, LINE_HEIGHT)) {
            return List.of(Component.translatable(pattern + ".description"));
        }
        return Collections.emptyList();
    }

    private List<Component> getTraitTooltips(MaterialStatsWrapper statsWrapper, MaterialStatsId statsId, double mouseX, double mouseY, float lineNumber) {
        for (ModifierEntry trait : statsWrapper.getTraits(statsId)) {
            String namespace = trait.getId().getNamespace();
            String path = trait.getId().getPath();
            String pattern = getPattern(String.format("modifier.%s.%s", namespace, path));
            int textWidth = FONT.width(pattern);
            if (inBox(mouseX, mouseY, WIDTH - textWidth, lineNumber++ * LINE_HEIGHT + LINE_OFFSET_HOVER, textWidth, LINE_HEIGHT)) {
                return List.of(Component.translatable(String.format("modifier.%s.%s.flavor", namespace, path)).setStyle(Style.EMPTY.withItalic(true).withColor(WHITE)),
                        Component.translatable(String.format("modifier.%s.%s.description", namespace, path)));
            }
        }
        return Collections.emptyList();
    }


    @Override
    public Component getTitle() {
        return Component.literal("Material Stats");
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
    public RecipeType<MaterialStatsWrapper> getRecipeType() {
        return RecipeType.create(MOD_ID, "material_stats", MaterialStatsWrapper.class);
    }

}
