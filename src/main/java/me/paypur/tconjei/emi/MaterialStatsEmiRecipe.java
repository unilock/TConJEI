package me.paypur.tconjei.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.paypur.tconjei.xplat.MaterialStatsWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.tools.stats.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.fabricators_of_create.porting_lib.util.ForgeI18n.getPattern;
import static me.paypur.tconjei.ColorManager.*;
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
        if (this.fluidStack != null) {
            widgets.addSlot(this.fluidStack, 18, 0);
        }

        widgets.addSlot(this.itemStacks, 0, 0);
        widgets.addSlot(this.toolParts, WIDTH - 16, 0).recipeContext(this);

        final String materialName = getPattern(Util.makeTranslationKey("material", recipe.getMaterialId()));
        final String materialTooltip = getPattern(Util.makeTranslationKey("material", recipe.getMaterialId()) + ".flavor");
        final int MATERIAL_COLOR = MaterialTooltipCache.getColor(recipe.getMaterialId()).getValue();
        // Name
        drawShadow(widgets, materialName, (WIDTH - FONT.width(materialName)) / 2f, 0.4f, MATERIAL_COLOR);
        widgets.addTooltipText(List.of(Component.literal(materialTooltip).setStyle(Style.EMPTY.withItalic(true).withColor(WHITE))), (int) ((WIDTH - FONT.width(materialName)) / 2f), (int) (0.4f * LINE_HEIGHT - 1), FONT.width(materialName), LINE_HEIGHT);
        float lineNumber = 2f;
        Optional<HeadMaterialStats> headOptional = recipe.getStats(HeadMaterialStats.ID);
        Optional<ExtraMaterialStats> extraOptional = recipe.getStats(ExtraMaterialStats.ID);
        Optional<HandleMaterialStats> handleOptional =  recipe.getStats(HandleMaterialStats.ID);
        Optional<LimbMaterialStats> limbOptional = recipe.getStats(LimbMaterialStats.ID);
        Optional<GripMaterialStats> gripOptional = recipe.getStats(GripMaterialStats.ID);
        Optional<BowstringMaterialStats> stringOptional = recipe.getStats(BowstringMaterialStats.ID);
        // HEAD
        if (headOptional.isPresent()) {
            HeadMaterialStats headStats = headOptional.get();
            ResourceLocation miningLevel = headStats.getTierId();
            drawTraits(widgets, recipe.getTraits(HeadMaterialStats.ID), lineNumber);
            drawShadow(widgets, String.format("[%s]", getPattern("stat.tconstruct.head")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.valueOf(headOptional.get().getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStatsShadow(widgets, "tool_stat.tconstruct.harvest_tier", getPattern(Util.makeTranslationKey("stat.tconstruct.harvest_tier", miningLevel)), lineNumber++, getMiningLevelColor(miningLevel));
            drawStats(widgets, "tool_stat.tconstruct.mining_speed", String.format("%.2f", headOptional.get().getMiningSpeed()), lineNumber++, MINING_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.attack_damage", String.format("%.2f", headOptional.get().getAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // EXTRA
        // only draw extra if others don't exist
        else if (extraOptional.isPresent()) {
            drawTraits(widgets, recipe.getTraits(ExtraMaterialStats.ID), lineNumber);
            drawShadow(widgets, String.format("[%s]", getPattern("stat.tconstruct.extra")), 0, lineNumber++, MATERIAL_COLOR);
            lineNumber += 0.4f;
        }
        // HANDLE
        if (handleOptional.isPresent()) {
            HandleMaterialStats handleStats = handleOptional.get();
            drawTraits(widgets, recipe.getTraits(HandleMaterialStats.ID), lineNumber);
            drawShadow(widgets, String.format("[%s]", getPattern("stat.tconstruct.handle")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.format("%.2fx", handleStats.getDurability()), lineNumber++, getMultiplierColor(handleStats.getDurability()));
            drawStats(widgets, "tool_stat.tconstruct.attack_damage", String.format("%.2fx", handleStats.getAttackDamage()), lineNumber++, getMultiplierColor(handleStats.getAttackDamage()));
            drawStats(widgets, "tool_stat.tconstruct.attack_speed", String.format("%.2fx", handleStats.getAttackSpeed()), lineNumber++, getMultiplierColor(handleStats.getAttackSpeed()));
            drawStats(widgets, "tool_stat.tconstruct.mining_speed", String.format("%.2fx", handleStats.getMiningSpeed()), lineNumber++, getMultiplierColor(handleStats.getMiningSpeed()));
            lineNumber += 0.4f;
        }
        // LIMB
        if (limbOptional.isPresent()) {
            LimbMaterialStats limbStats = limbOptional.get();
            drawTraits(widgets, recipe.getTraits(LimbMaterialStats.ID), lineNumber);
            drawShadow(widgets, String.format("[%s]", getPattern("stat.tconstruct.limb")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.valueOf(limbStats.getDurability()), lineNumber++, DURABILITY_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.draw_speed", signedString(limbStats.getDrawSpeed()), lineNumber++, getDifferenceColor(limbStats.getDrawSpeed()));
            drawStats(widgets, "tool_stat.tconstruct.velocity", signedString(limbStats.getVelocity()), lineNumber++, getDifferenceColor(limbStats.getVelocity()));
            drawStats(widgets, "tool_stat.tconstruct.accuracy", signedString(limbStats.getAccuracy()), lineNumber++, getDifferenceColor(limbStats.getAccuracy()));
            lineNumber += 0.4f;
        }
        // GRIP
        if (gripOptional.isPresent()) {
            GripMaterialStats gripStats = gripOptional.get();
            drawTraits(widgets, recipe.getTraits(GripMaterialStats.ID), lineNumber);
            drawShadow(widgets, String.format("[%s]", getPattern("stat.tconstruct.grip")), 0, lineNumber++, MATERIAL_COLOR);
            drawStats(widgets, "tool_stat.tconstruct.durability", String.format("%.2fx", gripStats.getDurability()), lineNumber++, getMultiplierColor(gripStats.getDurability()));
            drawStats(widgets, "tool_stat.tconstruct.accuracy", signedString(gripStats.getAccuracy()), lineNumber++, getDifferenceColor(gripStats.getAccuracy()));
            drawStats(widgets, "tool_stat.tconstruct.attack_damage", String.format("%.2f", gripStats.getMeleeAttack()), lineNumber++, ATTACK_COLOR);
            lineNumber += 0.4f;
        }
        // STRING
        else if (stringOptional.isPresent()) {
            drawTraits(widgets, recipe.getTraits(BowstringMaterialStats.ID), lineNumber);
            drawShadow(widgets, String.format("[%s]", getPattern("stat.tconstruct.bowstring")), 0, lineNumber, MATERIAL_COLOR);
        }
    }

    private void drawStats(WidgetHolder widgets, String type, String stat, float lineNumber, int ACCENT_COLOR) {
        String pattern = getPattern(type);
        int width = FONT.width(pattern);
        widgets.addText(Component.literal(pattern), 0, (int) (lineNumber * LINE_HEIGHT), TEXT_COLOR, false);
        widgets.addText(Component.literal(stat), width, (int) (lineNumber * LINE_HEIGHT), ACCENT_COLOR, false);
        widgets.addTooltipText(List.of(Component.translatable(type + ".description")), 0, (int) (lineNumber * LINE_HEIGHT - 1), width, LINE_HEIGHT);
    }

    private void drawStatsShadow(WidgetHolder widgets, String type, String stat, float lineNumber, int ACCENT_COLOR) {
        String pattern = getPattern(type);
        int width = FONT.width(pattern);
        widgets.addText(Component.literal(pattern), 0, (int) (lineNumber * LINE_HEIGHT), TEXT_COLOR, false);
        drawShadow(widgets, stat, width, lineNumber, ACCENT_COLOR);
        widgets.addTooltipText(List.of(Component.translatable(type + ".description")), 0, (int) (lineNumber * LINE_HEIGHT - 1), width, LINE_HEIGHT);
    }

    private void drawTraits(WidgetHolder widgets, List<ModifierEntry> traits, float lineNumber) {
        for (ModifierEntry trait : traits) {
            String patternKey = Util.makeTranslationKey("modifier", trait.getId());
            String pattern = getPattern(patternKey);
            int traitColor = ResourceColorManager.getColor(patternKey);
            int width = FONT.width(pattern);
            drawShadow(widgets, pattern, WIDTH - width, lineNumber, traitColor);
            widgets.addTooltipText(List.of(Component.translatable(patternKey + ".flavor").withStyle(ChatFormatting.ITALIC),
                    Component.translatable(patternKey + ".description")), WIDTH - width, (int) (lineNumber * LINE_HEIGHT - 1), width, LINE_HEIGHT);
        }
    }

    private void drawShadow(WidgetHolder widgets, String string, float x, float lineNumber, int color) {
        widgets.addText(Component.literal(string), (int) (x + 1f), (int) (lineNumber * LINE_HEIGHT + 1f), getShade(color, 6), false);
        widgets.addText(Component.literal(string), (int) x, (int) (lineNumber * LINE_HEIGHT), color, false);
    }
}
