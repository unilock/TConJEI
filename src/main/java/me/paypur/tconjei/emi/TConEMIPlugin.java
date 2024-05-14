package me.paypur.tconjei.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerTools;

import static me.paypur.tconjei.TConJEI.*;

public class TConEMIPlugin implements EmiPlugin {
    public static final EmiRecipeCategory MATERIAL_STATS = new EmiRecipeCategory(new ResourceLocation(MOD_ID, "material_stats"), new EmiTexture(new ResourceLocation(MOD_ID, "textures/gui/materialstats/icon.png"), 0, 0, 16, 16));
    public static final EmiRecipeCategory TOOL_PARTS = new EmiRecipeCategory(new ResourceLocation(MOD_ID, "tool_parts"), EmiStack.of(TinkerTools.cleaver.get().getRenderTool()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(MATERIAL_STATS);
        registry.addCategory(TOOL_PARTS);

        registry.addWorkstation(MATERIAL_STATS, EmiStack.of(TinkerTables.tinkerStation.asItem()));
        registry.addWorkstation(MATERIAL_STATS, EmiStack.of(TinkerTables.tinkersAnvil.asItem()));
        registry.addWorkstation(MATERIAL_STATS, EmiStack.of(TinkerTables.scorchedAnvil.asItem()));
        registry.addWorkstation(TOOL_PARTS, EmiStack.of(TinkerTables.tinkerStation.asItem()));
        registry.addWorkstation(TOOL_PARTS, EmiStack.of(TinkerTables.tinkersAnvil.asItem()));
        registry.addWorkstation(TOOL_PARTS, EmiStack.of(TinkerTables.scorchedAnvil.asItem()));

        materials().forEach(wrapper -> registry.addRecipe(new MaterialStatsEmiRecipe(wrapper)));
        toolDefinitions().forEach(wrapper -> registry.addRecipe(new ToolPartsEmiRecipe(wrapper)));
    }
}
