package k4unl.minecraft.Hydraulicraft.tileEntities.misc;

import k4unl.minecraft.Hydraulicraft.api.IHydraulicConsumer;
import k4unl.minecraft.Hydraulicraft.lib.helperClasses.Location;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileInterfaceValve extends TileEntity implements ISidedInventory, IFluidHandler {
	private int targetX;
	private int targetY;
	private int targetZ;
	private boolean targetHasChanged = true;
	
	private boolean isTank = false;
	private int tankSizeX = 0;
	private int tankSizeY = 0;
	private int tankSizeZ = 0;
	private Location tankCorner1;
	private Location tankCorner2;
	
	
	private IHydraulicConsumer target;
	private IFluidHandler fluidTarget;
	private ISidedInventory inventoryTarget;
	private boolean clientNeedsToResetTarget = false;
	private boolean clientNeedsToSetTarget = false;
	
	public void resetTarget(){
		target = null;
		targetX = xCoord;
		targetY = yCoord;
		targetZ = zCoord;
		targetHasChanged = true;
		
		if(!worldObj.isRemote){
			clientNeedsToResetTarget = true;
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public void setTarget(int x, int y, int z){
		targetX = x;
		targetY = y;
		targetZ = z;
		targetHasChanged = true;
		
		if(!worldObj.isRemote){
			clientNeedsToSetTarget = true;
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public IHydraulicConsumer getTarget(){
		if(targetHasChanged == true &&(targetX != xCoord || targetY != yCoord || targetZ != zCoord)){
			TileEntity t = worldObj.getTileEntity(targetX, targetY, targetZ);
			if(t instanceof IHydraulicConsumer){
				target = (IHydraulicConsumer) t;
				targetHasChanged = false;
			}
			if(t instanceof IFluidHandler){
				fluidTarget = (IFluidHandler) t;
			}
			if(t instanceof ISidedInventory){
				inventoryTarget = (ISidedInventory) t;
			}
		}else if(targetHasChanged == true && targetX == xCoord && targetY == yCoord && targetZ == zCoord){
			target = null;
			fluidTarget = null;
			inventoryTarget = null;
		}
		return target;
	}
	
	public IFluidHandler getFluidTarget(){
		if(targetHasChanged == true && (targetX != xCoord || targetY != yCoord || targetZ != zCoord)){
			TileEntity t = worldObj.getTileEntity(targetX, targetY, targetZ);
			if(t instanceof IHydraulicConsumer){
				target = (IHydraulicConsumer) t;
				if(t instanceof IFluidHandler){
					fluidTarget = (IFluidHandler) t;
				}
				if(t instanceof ISidedInventory){
					inventoryTarget = (ISidedInventory) t;
				}
				targetHasChanged = false;
			}
		}else if(targetHasChanged == true && targetX == xCoord && targetY == yCoord && targetZ == zCoord){
			target = null;
			fluidTarget = null;
			inventoryTarget = null;
		}
		return fluidTarget;
	}
	
	public ISidedInventory getInventoryTarget(){
		if(targetHasChanged == true && (targetX != xCoord || targetY != yCoord || targetZ != zCoord)){
			TileEntity t = worldObj.getTileEntity(targetX, targetY, targetZ);
			if(t instanceof IHydraulicConsumer){
				target = (IHydraulicConsumer) t;
				if(t instanceof IFluidHandler){
					fluidTarget = (IFluidHandler) t;
				}
				if(t instanceof ISidedInventory){
					inventoryTarget = (ISidedInventory) t;
				}
				targetHasChanged = false;
			}
		}else if(targetHasChanged == true && targetX == xCoord && targetY == yCoord && targetZ == zCoord){
			target = null;
			fluidTarget = null;
			inventoryTarget = null;
		}
		return inventoryTarget;
	}
	

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		targetX = tagCompound.getInteger("targetX");
		targetY = tagCompound.getInteger("targetY");
		targetZ = tagCompound.getInteger("targetZ");
		if(tagCompound.getBoolean("isTargetNull")){
			target = null;
		}
		if(worldObj != null && worldObj.isRemote){
			if(tagCompound.getBoolean("clientNeedsToResetTarget")){
				resetTarget();
			}
			if(tagCompound.getBoolean("clientNeedsToSetTarget")){
				targetHasChanged = true;
				getTarget();
			}
		}
	}
	
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		this.readFromNBT(tagCompound);
	}
	
	@Override
	public Packet getDescriptionPacket(){
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 4, tagCompound);
	}
	

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("targetX", targetX);
		tagCompound.setInteger("targetY", targetY);
		tagCompound.setInteger("targetZ", targetZ);
		tagCompound.setBoolean("isTargetNull", (target == null));
		if(target == null){
			tagCompound.setBoolean("isTargetNull", (target == null));			
		}
		if(worldObj != null && !worldObj.isRemote){
			tagCompound.setBoolean("clientNeedsToResetTarget", clientNeedsToResetTarget);
			tagCompound.setBoolean("clientNeedsToSetTarget", clientNeedsToSetTarget);
			clientNeedsToResetTarget = false;
			clientNeedsToSetTarget = false;
		}
		tagCompound.setBoolean("targetHasChanged", targetHasChanged);
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(getFluidTarget() != null){
			return getFluidTarget().fill(from, resource, doFill);
		}else{
			return 0;			
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		if(getFluidTarget() != null){
			return getFluidTarget().drain(from, resource, doDrain);
		}else{
			return null;
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(getFluidTarget() != null){
			return getFluidTarget().drain(from, maxDrain, doDrain);
		}else{
			return null;
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(getFluidTarget() != null){
			return getFluidTarget().canFill(from, fluid);
		}else{
			return false;
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(getFluidTarget() != null){
			return getFluidTarget().canDrain(from, fluid);
		}else{
			return false;
		}
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(getFluidTarget() != null){
			return getFluidTarget().getTankInfo(from);
		}else{
			return null;
		}
	}

	@Override
	public int getSizeInventory() {
		if(getInventoryTarget() != null){
			return getInventoryTarget().getSizeInventory();
		}
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().getStackInSlot(i);
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().decrStackSize(i, j);
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().getStackInSlotOnClosing(i);
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if(getInventoryTarget() != null){
			getInventoryTarget().setInventorySlotContents(i, itemstack);
		}
	}

	@Override
	public int getInventoryStackLimit() {
		if(getInventoryTarget() != null){
			return getInventoryTarget().getInventoryStackLimit();
		}
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().isUseableByPlayer(entityplayer);
		}
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().isItemValidForSlot(i, itemstack);
		}
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().getAccessibleSlotsFromSide(var1);
		}
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().canInsertItem(i, itemstack, j);
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		if(getInventoryTarget() != null){
			return getInventoryTarget().canExtractItem(i, itemstack, j);
		}
		return false;
	}

	@Override
	public String getInventoryName() {
		if(getInventoryTarget() != null){
			return getInventoryTarget().getInventoryName();
		}
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		if(getInventoryTarget() != null){
			return getInventoryTarget().hasCustomInventoryName();
		}
		return false;
	}

	@Override
	public void openInventory() {
		if(getInventoryTarget() != null){
			getInventoryTarget().openInventory();
		}		
	}

	@Override
	public void closeInventory() {
		if(getInventoryTarget() != null){
			getInventoryTarget().closeInventory();
		}	
	}

	public void checkTank() {
		
	}

}
