package k4unl.minecraft.Hydraulicraft.tileEntities.harvester.trolleys;

import java.util.ArrayList;

import k4unl.minecraft.Hydraulicraft.api.IHarvesterTrolley;
import k4unl.minecraft.Hydraulicraft.lib.config.ModInfo;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class TrolleyCrops implements IHarvesterTrolley{
    private static final ResourceLocation resLoc =
            new ResourceLocation(ModInfo.LID,"textures/model/harvesterTrolley_tmap.png");
    
	@Override
	public ArrayList<ItemStack> getHandlingSeeds() {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(Items.wheat_seeds));
		ret.add(new ItemStack(Items.carrot));
		ret.add(new ItemStack(Items.potato));
		return ret;
	}

	@Override
	public String getName() {
		return "crops";
	}

	@Override
	public boolean canPlant(World world, int x, int y, int z,
			ItemStack seed) {
		if(seed.getItem() instanceof IPlantable){
			Block soil = world.getBlock(x, y-1, z);
		    return (world.getFullBlockLightValue(x, y, z) >= 8 ||
		    		world.canBlockSeeTheSky(x, y, z)) &&
		            soil != null && soil.canSustainPlant(world, x, y - 1, z,
		                  ForgeDirection.UP, (IPlantable)seed.getItem());			
		}else{
			return false;
		}
	}

	@Override
	public Block getBlockForSeed(ItemStack seed) {
		IPlantable plantingItem = (IPlantable)seed.getItem();
		return plantingItem.getPlant(null, 0, 0, 0);
	}

	@Override
	public boolean canHarvest(World world, int x, int y, int z) {
		for(ItemStack s : getHandlingSeeds()){
			if(world.getBlock(x, y, z).equals(getBlockForSeed(s))){
				if(world.getBlockMetadata(x, y, z) == 7){
					return true;
				}
			}
		}
		
		return false;
	}

	
	@Override
    public ResourceLocation getTexture(){
        return resLoc;
    }

	@Override
	public int getPlantHeight(World world, int x, int y, int z) {
		return 1;
	}

}
