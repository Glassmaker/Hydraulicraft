package k4unl.minecraft.Hydraulicraft.thirdParty.industrialcraft.blocks;

import k4unl.minecraft.Hydraulicraft.api.ITieredBlock;
import k4unl.minecraft.Hydraulicraft.api.PressureTier;
import k4unl.minecraft.Hydraulicraft.blocks.HydraulicBlockContainerBase;
import k4unl.minecraft.Hydraulicraft.blocks.IRotateableBlock;
import k4unl.minecraft.Hydraulicraft.lib.config.GuiIDs;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import k4unl.minecraft.Hydraulicraft.thirdParty.industrialcraft.tileEntities.TileHydraulicGenerator;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockHydraulicGenerator extends HydraulicBlockContainerBase implements ITieredBlock, IRotateableBlock {

	public BlockHydraulicGenerator() {
		super(Names.blockHydraulicGenerator, true);
		hasFrontIcon = true;
		hasTextures = false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileHydraulicGenerator();
	}

	@Override
	public GuiIDs getGUIID() {

		return GuiIDs.GENERATOR;
	}

	public boolean canConnectRedstone(IBlockAccess iba, int i, int j, int k, int dir){
		return true;
    }
	
	@Override
    public int getRenderType(){
        return -1;
    }

    @Override
    public boolean isOpaqueCube(){
        return false;
    }

    @Override
    public boolean renderAsNormalBlock(){
        return false;
    }
	
	@SuppressWarnings("cast")
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack iStack){
		super.onBlockPlacedBy(world, x, y, z, player, iStack);
		TileEntity ent = world.getTileEntity(x, y, z);
		if(ent instanceof TileHydraulicGenerator){
			if(iStack != null){
				
				int sideToPlace = MathHelper.floor_double((double)(player.rotationYaw / 90F) + 0.5D) & 3;
				int metaDataToSet = 0;
				switch(sideToPlace){
				case 0:
					metaDataToSet = 2;
					break;
				case 1:
					metaDataToSet = 5;
					break;
				case 2:
					metaDataToSet = 3;
					break;
				case 3:
					metaDataToSet = 4;
					break;
				}
				((TileHydraulicGenerator)ent).setFacing(ForgeDirection.getOrientation(metaDataToSet));
			}
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y,
				int z, Block blockId) {
		super.onNeighborBlockChange(world, x, y, z, blockId);
		
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileHydraulicGenerator){
			((TileHydraulicGenerator)tile).checkRedstonePower();			
		}
	}
	@Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection side){
		if(!world.isRemote) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileHydraulicGenerator) {
				if (side.equals(ForgeDirection.UP) || side.equals(ForgeDirection.DOWN)) {
					TileHydraulicGenerator e = (TileHydraulicGenerator) te;
					ForgeDirection facing = e.getFacing();
					e.setFacing(facing.getRotation(side));
					e.getHandler().updateBlock();
					world.notifyBlocksOfNeighborChange(x, y, z, this);
				}
			}
		}
		
		return true;
    }

    @Override
    public PressureTier getTier() {

        return PressureTier.HIGHPRESSURE;
    }
}
