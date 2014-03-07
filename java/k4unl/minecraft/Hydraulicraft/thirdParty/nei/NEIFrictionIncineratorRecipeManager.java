package k4unl.minecraft.Hydraulicraft.thirdParty.nei;

import java.util.List;
import java.util.Map;

import k4unl.minecraft.Hydraulicraft.client.GUI.GuiCrusher;
import k4unl.minecraft.Hydraulicraft.client.GUI.IconRenderer;
import k4unl.minecraft.Hydraulicraft.lib.config.ModInfo;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIFrictionIncineratorRecipeManager extends ShapedRecipeHandler{
    private ShapedRecipeHandler.CachedShapedRecipe getShape(Map.Entry<List<Integer>, ItemStack> recipe) {
        ShapedRecipeHandler.CachedShapedRecipe shape = new
                ShapedRecipeHandler.CachedShapedRecipe(0, 0, null, recipe.getValue());


        PositionedStack stack = new PositionedStack(new ItemStack(recipe.getKey().get(0), 1, recipe.getKey().get(1)), 35, 8);
        stack.setMaxSize(2);
        shape.ingredients.add(stack);
        shape.result.relx = 113;
        shape.result.rely = 8;
        return shape;
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
    	//Input, output
    	Map<List<Integer>, ItemStack> recipes = FurnaceRecipes.smelting().getMetaSmeltingList(); 
    	
        for(Map.Entry<List<Integer>, ItemStack> recipe: recipes.entrySet()) {
            if(NEIClientUtils.areStacksSameTypeCrafting(recipe.getValue(), result)) {
                this.arecipes.add(getShape(recipe));
            }
        }
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass(){
        return GuiCrusher.class;
    }

    @Override
    public String getRecipeName(){
        return Names.blockHydraulicFrictionIncinerator.localized;
    }

    @Override
    public String getGuiTexture(){
        return ModInfo.LID + ":textures/gui/incinerator.png";
    }


    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe){
        return false;
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
    	Map<List<Integer>, ItemStack> recipes = FurnaceRecipes.smelting().getMetaSmeltingList(); 
    	
        for(Map.Entry<List<Integer>, ItemStack> recipe: recipes.entrySet()) {
        	ItemStack src = new ItemStack(recipe.getKey().get(0), 1, recipe.getKey().get(1));
            if(NEIClientUtils.areStacksSameTypeCrafting(src, ingredient)) {
                this.arecipes.add(getShape(recipe));
                break;
            }
        }
    }
    
    @Override
	public void drawExtras(int recipeIndex){
    	int ticks = 48;
    	float percentage = cycleticks % ticks / (float)ticks;
    	
    	CachedRecipe recipe = arecipes.get(recipeIndex); 
    	
    	
    	ItemStack smeltingItem = recipe.getIngredients().get(0).item;
		ItemStack targetItem = recipe.getResult().item;

		int startX = 38;
		int targetX = 114;
		int travelPath = targetX - startX;
		int xPos = startX + (int) (travelPath * percentage);
        //TODO decide whether one should have a wobbling effect or not (the last parameter)
        IconRenderer.drawMergedIcon(xPos, 8, 0, smeltingItem, targetItem, percentage, smeltingItem.stackSize % 2 == 0);
		//drawProgressBar(80, 21, 207, 0, 34, 19, 48, 2 | (1 << 3));
    }
}
