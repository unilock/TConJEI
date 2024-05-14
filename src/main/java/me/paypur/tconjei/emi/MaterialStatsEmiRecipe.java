package me.paypur.tconjei.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.paypur.tconjei.xplat.MaterialStatsWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.tools.stats.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.fabricators_of_create.porting_lib.util.ForgeI18n.getPattern;
import static me.paypur.tconjei.TConJEI.*;
import static me.paypur.tconjei.xplat.MaterialStatsWrapper.*;

public class MaterialStatsEmiRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    private final EmiIngredient fluidStack;
    private final EmiIngredient itemStacks;
    private final EmiIngredient toolParts;

    private final MaterialStatsWrapper recipe;

    public MaterialStatsEmiRecipe(MaterialStatsWrapper recipe) {
        this.id = recipe.getMaterialId();

        List<EmiIngredient> input = new ArrayList<>();

        if (!recipe.getFluidStack().isEmpty()) {
            final int BUCKET = 81000; // droplets
            this.fluidStack = EmiStack.of(recipe.getFluidStack().getFluid(), BUCKET);
            input.add(this.fluidStack);
        } else {
            this.fluidStack = null;
        }

        List<EmiStack> itemStacks = recipe.getItemStacks().stream().map(EmiStack::of).toList();
        input.addAll(itemStacks);
        this.itemStacks = EmiIngredient.of(itemStacks);

        List<EmiStack> toolParts = recipe.getToolParts().stream().map(EmiStack::of).toList();
        input.addAll(toolParts);
        this.toolParts = EmiIngredient.of(toolParts);

        this.input = input;
        this.output = toolParts;

        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TConEMIPlugin.MATERIAL_STATS;
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
        final String materialNamespace = recipe.getMaterialId().getNamespace();
        final String materialPath = recipe.getMaterialId().getPath();

        if (this.fluidStack != null) {
            widgets.addSlot(this.fluidStack, 18, 0);
        }

        widgets.addSlot(this.itemStacks, 0, 0);
        widgets.addSlot(this.toolParts, WIDTH - 16, 0).recipeContext(this);

        final String materialName = getPattern(String.format("material.%s.%s", recipe.getMaterialId().getNamespace(), recipe.getMaterialId().getPath()));
        final int MATERIAL_COLOR = MaterialTooltipCache.getColor(recipe.getMaterialId()).getValue();
        float lineNumber = 0;
        // Name
        widgets.addText(Component.literal(materialName), (int) ((WIDTH - FONT.width(materialName)) / 2f), 4, MATERIAL_COLOR, true);
        widgets.addTooltipText(List.of(Component.translatable(String.format("material.%s.%s.flavor", materialNamespace, materialPath)).setStyle(Style.EMPTY.withItalic(true).withColor(WHITE))), (int) ((WIDTH - FONT.width(materialPath)) / 2f), 3, FONT.width(materialPath), LINE_HEIGHT);
        Optional<HeadMaterialStats> headStats = recipe.getStats(HeadMaterialStats.ID);
        Optional<ExtraMaterialStats> extraStats = recipe.getStats(ExtraMaterialStats.ID);
        Optional<HandleMaterialStats> handleStats = recipe.getStats(HandleMaterialStats.ID);
        Optional<LimbMaterialStats> limbStats = recipe.getStats(LimbMaterialStats.ID);
        Optional<GripMaterialStats> gripStats = recipe.getStats(GripMaterialStats.ID);
        Optional<BowstringMaterialStats> stringStats = recipe.getStats(BowstringMaterialStats.ID);
        // HEAD
        if (headStats.isPresent()) {
            String miningLevel = headStats.get().getTierId().getPath();
            drawTraits(widgets, recipe, HeadMaterialStats.ID, lineNumber);
            widgets.addText(Component.literal(String.format("[%s]", getPattern("stat.tconstruct.head"))), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.valueOf(headStats.get().getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.harvest_tier", getPattern("stat.tconstruct.harvest_tier.minecraft." + miningLevel), lineNumber++, getMiningLevelColor(miningLevel));
            drawStats(widgets, "tool_stat.tconstruct.mining_speed", String.format("%.2f", headStats.get().getMiningSpeed()), lineNumber++, MINING_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.attack_damage", String.format("%.2f", headStats.get().getAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // EXTRA
        // only draw extra if others don't exist
        else if (extraStats.isPresent()) {
            drawTraits(widgets, recipe, ExtraMaterialStats.ID, lineNumber);
            widgets.addText(Component.literal(String.format("[%s]", getPattern("stat.tconstruct.extra"))), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
            lineNumber += 0.4f;
        }
        // HANDLE
        if (handleStats.isPresent()) {
            drawTraits(widgets, recipe, HandleMaterialStats.ID, lineNumber);
            widgets.addText(Component.literal(String.format("[%s]", getPattern("stat.tconstruct.handle"))), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.format("%.2fx", handleStats.get().getDurability()), lineNumber++, getMultiplierColor(handleStats.get().getDurability()));
            drawStats(widgets, "tool_stat.tconstruct.attack_damage", String.format("%.2fx", handleStats.get().getAttackDamage()), lineNumber++, getMultiplierColor(handleStats.get().getAttackDamage()));
            drawStats(widgets, "tool_stat.tconstruct.attack_speed", String.format("%.2fx", handleStats.get().getAttackSpeed()), lineNumber++, getMultiplierColor(handleStats.get().getAttackSpeed()));
            drawStats(widgets, "tool_stat.tconstruct.mining_speed", String.format("%.2fx", handleStats.get().getMiningSpeed()), lineNumber++, getMultiplierColor(handleStats.get().getMiningSpeed()));
            lineNumber += 0.4f;
        }
        // LIMB
        if (limbStats.isPresent()) {
            drawTraits(widgets, recipe, LimbMaterialStats.ID, lineNumber);
            widgets.addText(Component.literal(String.format("[%s]", getPattern("stat.tconstruct.limb"))), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.valueOf(limbStats.get().getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.draw_speed", signedString(limbStats.get().getDrawSpeed()), lineNumber++, getDifferenceColor(limbStats.get().getDrawSpeed()));
            drawStats(widgets, "tool_stat.tconstruct.velocity", signedString(limbStats.get().getVelocity()), lineNumber++, getDifferenceColor(limbStats.get().getVelocity()));
            drawStats(widgets, "tool_stat.tconstruct.accuracy", signedString(limbStats.get().getAccuracy()), lineNumber++, getDifferenceColor(limbStats.get().getAccuracy()));
            lineNumber += 0.4f;
        }
        // GRIP
        if (gripStats.isPresent()) {
            drawTraits(widgets, recipe, GripMaterialStats.ID, lineNumber);
            widgets.addText(Component.literal(String.format("[%s]", getPattern("stat.tconstruct.grip"))), 0, (int) (lineNumber++ * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.format("%.2fx", gripStats.get().getDurability()), lineNumber++, getMultiplierColor(gripStats.get().getDurability()));
            drawStats(widgets, "tool_stat.tconstruct.accuracy", signedString(gripStats.get().getAccuracy()), lineNumber++, getDifferenceColor(gripStats.get().getAccuracy()));
            drawStats(widgets, "tool_stat.tconstruct.attack_damage", String.format("%.2f", gripStats.get().getMeleeAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // STRING
        if (stringStats.isPresent()) {
            drawTraits(widgets, recipe, BowstringMaterialStats.ID, lineNumber);
            widgets.addText(Component.literal(String.format("[%s]", getPattern("stat.tconstruct.bowstring"))), 0, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
        }
    }

    private void drawStats(WidgetHolder widgets, String type, String stat, float lineNumber, int ACCENT_COLOR) {
        String pattern = getPattern(type);
        int width = FONT.width(pattern);
        widgets.addText(Component.literal(pattern), 0, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), TEXT_COLOR, false);
        widgets.addText(Component.literal(stat), width, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), ACCENT_COLOR, false);
        widgets.addTooltipText(List.of(Component.translatable(type + ".description")), 0, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET_HOVER), width, LINE_HEIGHT);
    }

    private void drawTraits(WidgetHolder widgets, MaterialStatsWrapper statsWrapper, MaterialStatsId statsId, float lineNumber) {
        final int MATERIAL_COLOR = MaterialTooltipCache.getColor(statsWrapper.getMaterialId()).getValue();
        for (ModifierEntry trait : statsWrapper.getTraits(statsId)) {
            String namespace = trait.getId().getNamespace();
            String path = trait.getId().getPath();
            String pattern = getPattern(String.format("modifier.%s.%s", namespace, path));
            int width = FONT.width(pattern);
            widgets.addText(Component.literal(pattern), WIDTH - width, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), MATERIAL_COLOR, true);
            widgets.addTooltipText(List.of(Component.translatable(String.format("modifier.%s.%s.flavor", namespace, path)).setStyle(Style.EMPTY.withItalic(true).withColor(WHITE)), Component.translatable(String.format("modifier.%s.%s.description", namespace, path))), WIDTH - width, (int) (lineNumber * LINE_HEIGHT + LINE_OFFSET), width, LINE_HEIGHT);
            lineNumber++;
        }
    }
}
