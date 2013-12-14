package pet.minecraft.Hydraulicraft.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pet.minecraft.Hydraulicraft.baseClasses.MachineBlock;
import pet.minecraft.Hydraulicraft.lib.config.Ids;
import pet.minecraft.Hydraulicraft.lib.config.Names;

public class BlockHydraulicPiston extends MachineBlock {

	protected BlockHydraulicPiston() {
		super(Ids.blockHydraulicPiston, Names.blockHydraulicPiston);
		this.hasTopIcon = true;
		this.hasBottomIcon = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
