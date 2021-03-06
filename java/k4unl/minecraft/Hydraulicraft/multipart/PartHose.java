package k4unl.minecraft.Hydraulicraft.multipart;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.EntityDigIconFX;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.ISidedHollowConnect;
import codechicken.multipart.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import k4unl.minecraft.Hydraulicraft.api.*;
import k4unl.minecraft.Hydraulicraft.blocks.HCBlocks;
import k4unl.minecraft.Hydraulicraft.client.renderers.transportation.RendererPartHose;
import k4unl.minecraft.Hydraulicraft.lib.Functions;
import k4unl.minecraft.Hydraulicraft.lib.Log;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import k4unl.minecraft.Hydraulicraft.tileEntities.PressureNetwork;
import k4unl.minecraft.Hydraulicraft.tileEntities.TileHydraulicBase;
import k4unl.minecraft.Hydraulicraft.tileEntities.interfaces.ICustomNetwork;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class PartHose extends TMultiPart implements TSlottedPart, JNormalOcclusion, ISidedHollowConnect, IHydraulicTransporter, ICustomNetwork,
  ITieredBlock {
    public static Cuboid6[] boundingBoxes = new Cuboid6[14];
    private static int expandBounds = -1;
    
    private IBaseClass baseHandler;
    private boolean[] connectedSideFlags = new boolean[7];
    private boolean needToCheckNeighbors;
    private boolean connectedSidesHaveChanged = true;
    private boolean hasCheckedSinceStartup;

	private static float pixel = 1.0F / 16F;
    
    private PressureTier tier = PressureTier.INVALID;

    @SideOnly(Side.CLIENT)
    private static RendererPartHose renderer;
    
    @SideOnly(Side.CLIENT)
    private static IIcon breakIcon;
    
    static {
    	float center = 0.5F;
    	float offset = pixel*2;
    	//float offsetY = 0.2F;
    	//float offsetZ = 0.2F;
    	float centerFirst = center - offset;
    	float centerSecond = center + offset;
        double w = pixel*2;
        boundingBoxes[6] = new Cuboid6(centerFirst - w, centerFirst - w, centerFirst - w, centerFirst + w, centerFirst + w, centerFirst + w);
        boundingBoxes[13] = new Cuboid6(centerSecond - w, centerSecond - w, centerSecond - w, centerSecond + w, centerSecond + w, centerSecond + w);
        
        int i = 0;
    	for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
    		double xMin1 = (dir.offsetX < 0 ? 0.0 : (dir.offsetX == 0 ? centerFirst - w : centerFirst + w));
    		double xMax1 = (dir.offsetX > 0 ? 1.0 : (dir.offsetX == 0 ? centerFirst + w : centerFirst - w));
    		
    		double yMin1 = (dir.offsetY < 0 ? 0.0 : (dir.offsetY == 0 ? centerFirst - w : centerFirst + w));
    		double yMax1 = (dir.offsetY > 0 ? 1.0 : (dir.offsetY == 0 ? centerFirst + w : centerFirst - w));
    		
    		double zMin1 = (dir.offsetZ < 0 ? 0.0 : (dir.offsetZ == 0 ? centerFirst - w : centerFirst + w));
    		double zMax1 = (dir.offsetZ > 0 ? 1.0 : (dir.offsetZ == 0 ? centerFirst + w : centerFirst - w));
    		
    		double xMin2 = (dir.offsetX < 0 ? 0.0 : (dir.offsetX == 0 ? centerSecond - w : centerSecond + w));
    		double xMax2 = (dir.offsetX > 0 ? 1.0 : (dir.offsetX == 0 ? centerSecond + w : centerSecond - w));
    		
    		double yMin2 = (dir.offsetY < 0 ? 0.0 : (dir.offsetY == 0 ? centerSecond - w : centerSecond + w));
    		double yMax2 = (dir.offsetY > 0 ? 1.0 : (dir.offsetY == 0 ? centerSecond + w : centerSecond - w));
    		
    		double zMin2 = (dir.offsetZ < 0 ? 0.0 : (dir.offsetZ == 0 ? centerSecond - w : centerSecond + w));
    		double zMax2 = (dir.offsetZ > 0 ? 1.0 : (dir.offsetZ == 0 ? centerSecond + w : centerSecond - w));
    		
    		boundingBoxes[i] = new Cuboid6(xMin1, yMin1, zMin1, xMax1, yMax1, zMax1);
    		boundingBoxes[i+7] = new Cuboid6(xMin2, yMin2, zMin2, xMax2, yMax2, zMax2);
    		i++;
    	}
    }
    
	@Override
	public String getType() {
		return "tile." + Names.partHose[0].unlocalized;
	}

	public void preparePlacement(int itemDamage) {
		tier = PressureTier.fromOrdinal(itemDamage);
		
	}
	
	@Override
	 public void load(NBTTagCompound tagCompound){
		super.load(tagCompound);
		if(getHandler() != null){
			getHandler().validateI();
			getHandler().readFromNBTI(tagCompound);
		}
		tier = PressureTier.fromOrdinal(tagCompound.getInteger("tier"));
		readConnectedSidesFromNBT(tagCompound);
	}

	@Override
	public void save(NBTTagCompound tagCompound){
		super.save(tagCompound);
		getHandler().writeToNBTI(tagCompound);
		tagCompound.setInteger("tier", tier.toInt());
		writeConnectedSidesToNBT(tagCompound);
	}

	@Override
	public void writeDesc(MCDataOutput packet){
		packet.writeInt(getTier().toInt());

		NBTTagCompound mainCompound = new NBTTagCompound();
		NBTTagCompound handlerCompound = new NBTTagCompound();
		if(connectedSidesHaveChanged && world() != null && !world().isRemote){
			connectedSidesHaveChanged = false;
			mainCompound.setBoolean("connectedSidesHaveChanged", true);
		}
		getHandler().writeToNBTI(handlerCompound);

		mainCompound.setTag("handler", handlerCompound);

		packet.writeNBTTagCompound(mainCompound);
	}

	private void readConnectedSidesFromNBT(NBTTagCompound tagCompound){

		NBTTagCompound ourCompound = tagCompound.getCompoundTag("connectedSides");

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			connectedSideFlags[dir.ordinal()] = ourCompound.getBoolean(dir.ordinal()+"");
		}
		needToCheckNeighbors = true;
	}

	private void writeConnectedSidesToNBT(NBTTagCompound tagCompound){

		NBTTagCompound ourCompound = new NBTTagCompound();
		int i=0;
		for(boolean b : connectedSideFlags) {
			ourCompound.setBoolean(i+"", b);
			i++;
		}

		tagCompound.setTag("connectedSides", ourCompound);
	}

	@Override
	public void readDesc(MCDataInput packet){
		tier = PressureTier.fromOrdinal(packet.readInt());

		NBTTagCompound mainCompound = packet.readNBTTagCompound();
		NBTTagCompound handlerCompound = mainCompound.getCompoundTag("handler");
		if(mainCompound.getBoolean("connectedSidesHaveChanged")){
			hasCheckedSinceStartup = false;
		}

		getHandler().readFromNBTI(handlerCompound);
	}


	@Override
	public int getSlotMask() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		Iterable<Cuboid6> boxList = getCollisionBoxes();
		LinkedList<IndexedCuboid6> partList = new LinkedList<IndexedCuboid6>();
		for (Cuboid6 c : boxList)
			partList.add(new IndexedCuboid6(0, c));
		return partList;
	}

	@Override
	public boolean occlusionTest(TMultiPart npart){
		return NormalOcclusionTest.apply(this, npart);
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes(){
		if (expandBounds >= 0)
			return Arrays.asList(boundingBoxes[expandBounds]);

		return Arrays.asList(boundingBoxes[6], boundingBoxes[13]);
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes(){
		LinkedList<Cuboid6> list = new LinkedList<Cuboid6>();
		list.add(boundingBoxes[6]);
		list.add(boundingBoxes[13]);
		if(connectedSideFlags == null) return list;
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
			if(connectedSideFlags[dir.ordinal()]){
				list.add(boundingBoxes[Functions.getIntDirFromDirection(dir)]);
				list.add(boundingBoxes[Functions.getIntDirFromDirection(dir)+7]);
			}
		}
		return list;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass){
		if (pass == 0){
			if(renderer == null){
				renderer = new RendererPartHose();
			}
			GL11.glDisable(GL11.GL_LIGHTING);
			renderer.doRender(pos.x, pos.y, pos.z, 0, tier.toInt(), connectedSideFlags);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	private boolean shouldConnectTo(TileEntity entity, ForgeDirection dir){
		if(entity instanceof TileMultipart){
			List<TMultiPart> t = ((TileMultipart)entity).jPartList();

			for (TMultiPart p: t) {
				if(p instanceof IHydraulicTransporter){
					return ((IHydraulicTransporter)p).canConnectTo(dir.getOpposite());
				}
				if(p instanceof IHydraulicMachine){
					return ((IHydraulicMachine)p).canConnectTo(dir.getOpposite());
				}
			}
			return false;
		}else{
			if(entity instanceof IHydraulicMachine){
				return ((IHydraulicMachine)entity).canConnectTo(dir.getOpposite());
			}else{
				return false;
			}
		}
	}

	public boolean isConnectedTo(ForgeDirection side){
		int d = side.ordinal();

		if(world() != null && tile() != null){
			TileEntity te = world().getTileEntity(x() + side.offsetX, y() + side.offsetY, z() + side.offsetZ);
			return tile().canAddPart(new NormallyOccludedPart(boundingBoxes[d])) && shouldConnectTo(te, side);
		}else{
			return false;
		}
	}

	public void checkConnectedSides(){
		boolean[] oldSides = new boolean[7];

		if(connectedSideFlags != null) {
			System.arraycopy(connectedSideFlags, 0, oldSides, 0, 7);
		}
		connectedSideFlags = new boolean[7];

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
			int d = Functions.getIntDirFromDirection(dir);

			TileEntity te = world().getTileEntity(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ);
			if(shouldConnectTo(te, dir)) {
				if(tile().canAddPart(new NormallyOccludedPart(boundingBoxes[d]))){
					if(!oldSides[dir.ordinal()]){
						connectedSidesHaveChanged = true;
					}
					connectedSideFlags[dir.ordinal()] = true;
				}else{
					if(oldSides[dir.ordinal()]){
						connectedSidesHaveChanged = true;
					}
					connectedSideFlags[dir.ordinal()] = false;
				}
			}else if(oldSides[dir.ordinal()]){
				connectedSidesHaveChanged = true;
			}
		}
		if(connectedSidesHaveChanged && !world().isRemote){
			float pressure = 0.0F;
			int c = 0;
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(oldSides[dir.ordinal()]) {
					if (getHandler().getPressure(dir) > 0) {
						pressure += getHandler().getPressure(dir);
						c++;

					}
					if (getNetwork(dir) != null) {
						getNetwork(dir).removeMachine(this);
					}
				}
			}
			if(c > 1)
				pressure = pressure / c;
			
			getHandler().updateNetworkOnNextTick(pressure);
			getHandler().updateBlock();
		}
	}

	@Override
	public boolean canConnectTo(ForgeDirection side) {
		return tile().canAddPart(new NormallyOccludedPart(boundingBoxes[side.ordinal()]));
	}

	public void onNeighborChanged(){
		super.onNeighborChanged();
		needToCheckNeighbors = true;
	}

	public ItemStack getItem(){
		return new ItemStack(Multipart.itemPartHose, 1, tier.toInt());
	}

	@Override
	public void onPartChanged(TMultiPart part){
		super.onPartChanged(part);
		needToCheckNeighbors = true;
	}

	@Override
	public Iterable<ItemStack> getDrops() {

		LinkedList<ItemStack> items = new LinkedList<ItemStack>();
		items.add(getItem());
		return items;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit){
		return getItem();
	}


	@Override
	public void onFluidLevelChanged(int old) {	}

	public PressureTier getTier() {
		return tier;
	}

	@Override
	public IBaseClass getHandler() {
		if(baseHandler == null) baseHandler = HCApi.getInstance().getBaseClass(this, 2 * (getTier().toInt()+1));
		return baseHandler;
	}

	@Override
	public void update(){
		if(getHandler() != null){
			//This should never happen that this is null! :|
			getHandler().updateEntityI();
		}else{
			Log.error("PartHose does not have a handler!");
		}
		if(world() != null){
			if(world().getTotalWorldTime() % 2 == 0 && !hasCheckedSinceStartup){
				checkConnectedSides();
				hasCheckedSinceStartup = true;
				//Hack hack hack
				//Temporary bug fix that we will forget about
			}
			if(world().isRemote && world().getTotalWorldTime() % 20 == 0) {
				if (!getHandler().isOilStored() && getHandler().getPressure(ForgeDirection.UP) > 0) {
					//Do the particle thingie!
					//world.spawnParticle("cloud", x, y, z, d3, d4, d5);
					Random random = new Random();
					if (random.nextDouble() < 0.4){
						double x = x() + (((random.nextDouble()*.2)-.1) + 0.5);
						double z = z() + (((random.nextDouble()*.2)-.1) + 0.5);
						world().spawnParticle("dripWater", x, y(), z + 0.0, 0.0D, 0.0D, 0.0D);
					}
					//PacketPipeline.instance.sendToAllAround(new PacketSpawnParticle("dripwater", x() + .5, y() + .5, z() + .5, 0.0D, 0.0D, 0.0D),
					//  world());
				}
			}
		}

		if(needToCheckNeighbors) {
			needToCheckNeighbors = false;
			checkConnectedSides();
		}
	}

	public boolean[] getConnectedSides() {
		if(connectedSideFlags == null){
			checkConnectedSides();
		}
		return connectedSideFlags;
	}
	
	
	@Override
	public void updateNetwork(float oldPressure) {
		PressureNetwork newNetwork = null;
		PressureNetwork foundNetwork;
		PressureNetwork endNetwork = null;
		//This block can merge networks!
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
			if(!isConnectedTo(dir)){
				continue;
			}
			TileEntity ent = world().getTileEntity(x() + dir.offsetX, y()+dir.offsetY, z()+ dir.offsetZ);
			if(ent == null) continue;
			if(!connectedSideFlags[dir.ordinal()]) continue;
			foundNetwork = PressureNetwork.getNetworkInDir(world(), x(), y(), z(), dir);
			
			if(foundNetwork != null){

				if(endNetwork == null){
					endNetwork = foundNetwork;
				}else{
					newNetwork = foundNetwork;
				}
			}
			
			if(newNetwork != null){
				//Hmm.. More networks!? What's this!?
				//Log.info("Found an existing network (" + newNetwork.getRandomNumber() + ") @ " + x() + "," + y() + "," + z());
				endNetwork.mergeNetwork(newNetwork);
				newNetwork = null;
			}
			
		}
			
		if(endNetwork != null){
			((TileHydraulicBase)getHandler()).setNetwork(ForgeDirection.UP, endNetwork);
			endNetwork.addMachine(this, oldPressure, ForgeDirection.UP);
			//Log.info("Found an existing network (" + endNetwork.getRandomNumber() + ") @ " + x() + "," + y() + "," + z());
		}else{
			endNetwork = new PressureNetwork(this, oldPressure, ForgeDirection.UP);
			((TileHydraulicBase)getHandler()).setNetwork(ForgeDirection.UP, endNetwork);
			//Log.info("Created a new network (" + endNetwork.getRandomNumber() + ") @ " + x() + "," + y() + "," + z());
		}
	}
	
	@Override
	public void onRemoved(){
		super.onRemoved();
		if(!world().isRemote){
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(connectedSideFlags[dir.ordinal()]) {
					if (getNetwork(dir) != null) {
						getNetwork(dir).removeMachine(this);
					}
				}
	        }
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void addDestroyEffects(MovingObjectPosition hit, EffectRenderer effectRenderer){
		addDestroyEffects(effectRenderer);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void addDestroyEffects(EffectRenderer effectRenderer) {
		if(breakIcon == null){
			breakIcon = HCBlocks.hydraulicPressureWall.getIcon(0, 0);
		}
        EntityDigIconFX.addBlockDestroyEffects(world(), Cuboid6.full.copy()
                .add(Vector3.fromTileEntity(tile())), new IIcon[] { breakIcon,
                breakIcon, breakIcon, breakIcon, breakIcon, breakIcon },
                effectRenderer);
    }
	
	@SideOnly(Side.CLIENT)
    @Override
    public void addHitEffects(MovingObjectPosition hit,
            EffectRenderer effectRenderer) {
   
        EntityDigIconFX.addBlockHitEffects(world(),
                Cuboid6.full.copy().add(Vector3.fromTileEntity(tile())),
                hit.sideHit, breakIcon, effectRenderer);
    }
	
	@Override
	public float getStrength(MovingObjectPosition hit, EntityPlayer player){
		return 8F;
	}

	@Override
	public int getHollowSize(int arg0) {
		return 6;
	}

	@Override
	public PressureNetwork getNetwork(ForgeDirection dir) {
		return ((TileHydraulicBase)getHandler()).getNetwork(dir);
	}

	@Override
	public void setNetwork(ForgeDirection side, PressureNetwork toSet) {
		((TileHydraulicBase)getHandler()).setNetwork(side, toSet);
	}
}
