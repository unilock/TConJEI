package me.paypur.tconjei.jei;

import me.paypur.tconjei.xplat.MaterialStatsWrapper;
import me.paypur.tconjei.xplat.ToolPartsWrapper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tables.TinkerTables;

import static me.paypur.tconjei.TConJEI.*;

@SuppressWarnings("unused")
@JeiPlugin
public class TConJEIPlugin implements IModPlugin {

    private static final RecipeType<MaterialStatsWrapper> MATERIAL_STATS = RecipeType.create(MOD_ID, "material_stats", MaterialStatsWrapper.class);
    private static final RecipeType<ToolPartsWrapper> TOOL_PARTS = RecipeType.create(MOD_ID, "tool_parts", ToolPartsWrapper.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(MATERIAL_STATS, materials());
        registration.addRecipes(TOOL_PARTS, toolDefinitions());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MaterialStatsCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ToolPartsCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(TinkerTables.tinkerStation.asItem()), MATERIAL_STATS);
        registration.addRecipeCatalyst(new ItemStack(TinkerTables.tinkersAnvil.asItem()), MATERIAL_STATS);
        registration.addRecipeCatalyst(new ItemStack(TinkerTables.scorchedAnvil.asItem()), MATERIAL_STATS);
        registration.addRecipeCatalyst(new ItemStack(TinkerTables.tinkerStation.asItem()), TOOL_PARTS);
        registration.addRecipeCatalyst(new ItemStack(TinkerTables.tinkersAnvil.asItem()), TOOL_PARTS);
        registration.addRecipeCatalyst(new ItemStack(TinkerTables.scorchedAnvil.asItem()), TOOL_PARTS);
    }
}
