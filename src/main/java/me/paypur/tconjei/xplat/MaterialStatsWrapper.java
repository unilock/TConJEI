package me.paypur.tconjei.xplat;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.Material;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.BaseMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.stats.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.paypur.tconjei.TConJEI.*;

public record MaterialStatsWrapper(Material material) {

    public static final int WIDTH = 164, HEIGHT = 220;
    public static final int LINE_OFFSET = 20;
    public static final int LINE_OFFSET_HOVER = LINE_OFFSET - 1;
    public static final int LINE_HEIGHT = 10;

    // taken from AbstractMaterialContent
    public List<ItemStack> getItemStacks() {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return Collections.emptyList();
        }
        List<ItemStack> repairStacks;
        // simply combine all items from all recipes
        MaterialVariantId variantId = MaterialVariantId.parse(material.getIdentifier().toString());
        repairStacks = RecipeHelper.getUIRecipes(world.getRecipeManager(), TinkerRecipeTypes.MATERIAL.get(), MaterialRecipe.class, recipe -> variantId.matchesVariant(recipe.getMaterial()))
                .stream()
                .flatMap(recipe -> Arrays.stream(recipe.getIngredient().getItems()))
                .toList();
        // no repair items? use the repair kit
        if (repairStacks.isEmpty()) {
            // bypass the valid check, because we need to show something
            repairStacks = Collections.singletonList(TinkerToolParts.repairKit.get().withMaterialForDisplay(variantId));
        }
        return repairStacks;
    }

    // taken from AbstractMaterialContent
    public FluidStack getFluidStack() {
        return MaterialCastingLookup.getCastingFluids(material.getIdentifier())
                .stream()
                .flatMap(recipe -> recipe.getFluids().stream())
                .findFirst()
                .orElse(FluidStack.EMPTY);
    }

    // taken from AbstractMaterialContent
    public List<ItemStack> getToolParts() {
        return RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.TOOL_PARTS)
                .filter(part -> part instanceof IToolPart)
                .map(part -> (IToolPart) part)
                .filter(part -> part.canUseMaterial(material))
                .map(part -> part.withMaterial(material.getIdentifier()))
                .collect(Collectors.toList());
    }

    public MaterialId getMaterialId() {
        return material.getIdentifier().getId();
    }

    public <T extends BaseMaterialStats> Optional<T> getStats(MaterialStatsId materialStatsId) {
        return MaterialRegistry.getInstance().getMaterialStats(getMaterialId(), materialStatsId);
    }

    public List<ModifierEntry> getTraits(MaterialStatsId materialStatsId) {
        return MaterialRegistry.getInstance().getTraits(material.getIdentifier().getId(), materialStatsId);
    }

    public boolean hasTraits() {
        List<MaterialStatsId> stats = List.of(HeadMaterialStats.ID, ExtraMaterialStats.ID, HandleMaterialStats.ID, LimbMaterialStats.ID, GripMaterialStats.ID, BowstringMaterialStats.ID);
        return stats.stream().anyMatch(stat -> !getTraits(stat).isEmpty());
    }

    public static int getMiningLevelColor(String miningLevel) {
        return switch (miningLevel) {
            case "wood" -> 9200923;
            case "gold" -> 16558080;
            case "stone" -> 9934743;
            case "iron" -> 14342874; // default color 13158600 is not visible in light mode
            case "diamond" -> 5569788;
            case "netherite" -> 4997443;
            default -> TEXT_COLOR;
        };
    }

    // @formatter:off
    // TODO: found colors in assets/tconstruct/mantle/colors.json
    public static int getMultiplierColor(Float f) {
        if (f < 0.55f) { return 12386304; } //bd0000
        if (f < 0.60f) { return 12396032; } //bd2600
        if (f < 0.65f) { return 12405504; } //bd4b00
        if (f < 0.70f) { return 12415232; } //bd7100
        if (f < 0.75f) { return 12424960; } //bd9700
        if (f < 0.80f) { return 12434688; } //bdbd00
        if (f < 0.85f) { return 9944320; } //97bd00
        if (f < 0.90f) { return 7453952; } //71bd00
        if (f < 0.95f) { return 4963584; } //4bbd00
        if (f < 1.00f) { return 2538752; } //26bd00
        if (f < 1.05f) { return 48384; } //00bd00
        if (f < 1.10f) { return 48422; } //00bd26
        if (f < 1.15f) { return 48459; } //00bd4b
        if (f < 1.20f) { return 48497; } //00bd71
        if (f < 1.25f) { return 48535; } //00bd97
        if (f < 1.30f) { return 48573; } //00bdbd
        if (f < 1.35f) { return 38845; } //0097bd
        if (f < 1.4f) { return 29117; } //0071bd
        return 19389; //004bbd
    }
    // @formatter:on

    public static int getDifferenceColor(Float f) {
        return getMultiplierColor(f + 1f);
    }

    public static String signedString(float f) {
        return String.format("%s%.2f", f >= 0 ? "+" : "", f);
    }

}
