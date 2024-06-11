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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.tools.stats.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.fabricators_of_create.porting_lib.util.ForgeI18n.getPattern;
import static me.paypur.tconjei.ColorManager.*;
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
        final String materialName = getPattern(Util.makeTranslationKey("material", recipe.getMaterialId()));
        final int MATERIAL_COLOR = MaterialTooltipCache.getColor(recipe.getMaterialId()).getValue();
        // Name
        drawShadow(guiGraphics, materialName, (WIDTH - FONT.width(materialName)) / 2f, 0.4f, MATERIAL_COLOR);
        Optional<HeadMaterialStats> headOptional = recipe.getStats(HeadMaterialStats.ID);
        Optional<ExtraMaterialStats> extraOptional = recipe.getStats(ExtraMaterialStats.ID);
        Optional<HandleMaterialStats> handleOptional = recipe.getStats(HandleMaterialStats.ID);
        Optional<LimbMaterialStats> limbOptional = recipe.getStats(LimbMaterialStats.ID);
        Optional<GripMaterialStats> gripOptional = recipe.getStats(GripMaterialStats.ID);
        Optional<BowstringMaterialStats> stringOptional = recipe.getStats(BowstringMaterialStats.ID);
        float lineNumber = 2f;
        // HEAD
        if (headOptional.isPresent()) {
            HeadMaterialStats headStats = headOptional.get();
            ResourceLocation miningLevel = headStats.getTierId();
            drawTraits(guiGraphics, recipe.getTraits(HeadMaterialStats.ID), lineNumber);
            drawShadow(guiGraphics, String.format("[%s]", getPattern("stat.tconstruct.head")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.valueOf(headOptional.get().getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStatsShadow(guiGraphics, "tool_stat.tconstruct.harvest_tier", getPattern(Util.makeTranslationKey("stat.tconstruct.harvest_tier", miningLevel)), lineNumber++, getMiningLevelColor(miningLevel));
            drawStats(guiGraphics, "tool_stat.tconstruct.mining_speed", String.format("%.2f", headOptional.get().getMiningSpeed()), lineNumber++, MINING_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_damage", String.format("%.2f", headOptional.get().getAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // EXTRA
        // only draw extra if others don't exist
        else if (extraOptional.isPresent()) {
            drawTraits(guiGraphics, recipe.getTraits(ExtraMaterialStats.ID), lineNumber);
            drawShadow(guiGraphics, String.format("[%s]", getPattern("stat.tconstruct.extra")), 0, lineNumber++, MATERIAL_COLOR);
            lineNumber += 0.4f;
        }
        // HANDLE
        if (handleOptional.isPresent()) {
            HandleMaterialStats handleStats = handleOptional.get();
            drawTraits(guiGraphics, recipe.getTraits(HandleMaterialStats.ID), lineNumber);
            drawShadow(guiGraphics, String.format("[%s]", getPattern("stat.tconstruct.handle")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.format("%.2fx", handleStats.getDurability()), lineNumber++, getMultiplierColor(handleStats.getDurability()));
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_damage", String.format("%.2fx", handleStats.getAttackDamage()), lineNumber++, getMultiplierColor(handleStats.getAttackDamage()));
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_speed", String.format("%.2fx", handleStats.getAttackSpeed()), lineNumber++, getMultiplierColor(handleStats.getAttackSpeed()));
            drawStats(guiGraphics, "tool_stat.tconstruct.mining_speed", String.format("%.2fx", handleStats.getMiningSpeed()), lineNumber++, getMultiplierColor(handleStats.getMiningSpeed()));
            lineNumber += 0.4f;
        }
        // LIMB
        if (limbOptional.isPresent()) {
            LimbMaterialStats limbStats = limbOptional.get();
            drawTraits(guiGraphics, recipe.getTraits(LimbMaterialStats.ID), lineNumber);
            drawShadow(guiGraphics, String.format("[%s]", getPattern("stat.tconstruct.limb")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.valueOf(limbStats.getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.draw_speed", signedString(limbStats.getDrawSpeed()), lineNumber++, getDifferenceColor(limbStats.getDrawSpeed()));
            drawStats(guiGraphics, "tool_stat.tconstruct.velocity", signedString(limbStats.getVelocity()), lineNumber++, getDifferenceColor(limbStats.getVelocity()));
            drawStats(guiGraphics, "tool_stat.tconstruct.accuracy", signedString(limbStats.getAccuracy()), lineNumber++, getDifferenceColor(limbStats.getAccuracy()));
            lineNumber += 0.4f;
        }
        // GRIP
        if (gripOptional.isPresent()) {
            GripMaterialStats gripStats = gripOptional.get();
            drawTraits(guiGraphics, recipe.getTraits(GripMaterialStats.ID), lineNumber);
            drawShadow(guiGraphics, String.format("[%s]", getPattern("stat.tconstruct.grip")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(guiGraphics, "tool_stat.tconstruct.durability", String.format("%.2fx", gripStats.getDurability()), lineNumber++, getMultiplierColor(gripStats.getDurability()));
            drawStats(guiGraphics, "tool_stat.tconstruct.accuracy", signedString(gripStats.getAccuracy()), lineNumber++, getDifferenceColor(gripStats.getAccuracy()));
            drawStats(guiGraphics, "tool_stat.tconstruct.attack_damage", String.format("%.2f", gripStats.getMeleeAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // STRING
        else if (stringOptional.isPresent()) {
            drawTraits(guiGraphics, recipe.getTraits(BowstringMaterialStats.ID), lineNumber);
            drawShadow(guiGraphics, String.format("[%s]", getPattern("stat.tconstruct.bowstring")), 0, lineNumber, MATERIAL_COLOR);
        }

    }

    @Override
    public List<Component> getTooltipStrings(MaterialStatsWrapper recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        final String materialNamespace = recipe.getMaterialId().getNamespace();
        final String materialPath = recipe.getMaterialId().getPath();
        // TRAIT
        int matWidth = FONT.width(materialPath);
        if (inBox(mouseX, mouseY, (WIDTH - matWidth) / 2f, 0.4f * LINE_HEIGHT - 1, matWidth, LINE_HEIGHT)) {
            return List.of(Component.translatable(String.format("material.%s.%s.flavor", materialNamespace, materialPath)).withStyle(ChatFormatting.ITALIC));
        }
        float lineNumber = 2f;
        Optional<HeadMaterialStats> headOptional = recipe.getStats(HeadMaterialStats.ID);
        Optional<ExtraMaterialStats> extraOptional = recipe.getStats(ExtraMaterialStats.ID);
        Optional<HandleMaterialStats> handleOptional =  recipe.getStats(HandleMaterialStats.ID);
        Optional<LimbMaterialStats> limbOptional = recipe.getStats(LimbMaterialStats.ID);
        Optional<GripMaterialStats> gripOptional = recipe.getStats(GripMaterialStats.ID);
        Optional<BowstringMaterialStats> stringOptional = recipe.getStats(BowstringMaterialStats.ID);
        // HEAD
        if (headOptional.isPresent()) {
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
        else if (extraOptional.isPresent()) {
            List<Component> component = getTraitTooltips(recipe, ExtraMaterialStats.ID, mouseX, mouseY, lineNumber++);
            if (!component.isEmpty()) {
                return component;
            }
            lineNumber += 0.4f;
        }
        // HANDLE
        if (handleOptional.isPresent()) {
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
        if (limbOptional.isPresent()) {
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
        if (gripOptional.isPresent()) {
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
        else if (stringOptional.isPresent()) {
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
        guiGraphics.drawString(FONT, pattern, 0, (int) (lineNumber * LINE_HEIGHT), TEXT_COLOR, false);
        guiGraphics.drawString(FONT, stat, (int) width, (int) (lineNumber * LINE_HEIGHT), ACCENT_COLOR, false);
    }

    private void drawStatsShadow(GuiGraphics guiGraphics, String type, String stat, float lineNumber, int ACCENT_COLOR) {
        String pattern = getPattern(type);
        float width = FONT.getSplitter().stringWidth(pattern);
        guiGraphics.drawString(FONT, pattern, 0, (int) (lineNumber * LINE_HEIGHT), TEXT_COLOR, false);
        drawShadow(guiGraphics, stat, width, lineNumber, ACCENT_COLOR);
    }

    private void drawTraits(GuiGraphics guiGraphics, List<ModifierEntry> traits, float lineNumber) {
        for (ModifierEntry trait : traits) {
            String pattern = getPattern(Util.makeTranslationKey("modifier", trait.getId()));
            int traitColor = ResourceColorManager.getColor(Util.makeTranslationKey("modifier", trait.getId()));
            drawShadow(guiGraphics, pattern, WIDTH - FONT.getSplitter().stringWidth(pattern), lineNumber++, traitColor);
        }
    }

    private void drawShadow(GuiGraphics guiGraphics, String string, float x, float lineNumber, int color) {
        guiGraphics.drawString(FONT, string, (int) (x + 1f), (int) (lineNumber * LINE_HEIGHT + 1f), getShade(color, 6));
        guiGraphics.drawString(FONT, string, (int) x, (int) (lineNumber * LINE_HEIGHT), color);
    }

    private List<Component> getStatTooltip(String pattern, double mouseX, double mouseY, float lineNumber) {
        String string = getPattern(pattern);
        int textWidth = FONT.width(string);
        if (inBox(mouseX, mouseY, 0, lineNumber * LINE_HEIGHT - 1, textWidth, LINE_HEIGHT)) {
            return List.of(Component.translatable(pattern + ".description"));
        }
        return Collections.emptyList();
    }

    private List<Component> getTraitTooltips(MaterialStatsWrapper statsWrapper, MaterialStatsId statsId, double mouseX, double mouseY, float lineNumber) {
        for (ModifierEntry trait : statsWrapper.getTraits(statsId)) {
            String namespace = trait.getId().getNamespace();
            String path = trait.getId().getPath();
            String pattern = getPattern(Util.makeTranslationKey("modifier", trait.getId()));
            int textWidth = FONT.width(pattern);
            if (inBox(mouseX, mouseY, WIDTH - textWidth, lineNumber++ * LINE_HEIGHT - 1, textWidth, LINE_HEIGHT)) {
                return List.of(Component.translatable(String.format("modifier.%s.%s.flavor", namespace, path)).withStyle(ChatFormatting.ITALIC),
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
