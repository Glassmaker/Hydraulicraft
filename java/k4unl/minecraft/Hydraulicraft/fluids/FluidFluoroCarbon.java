package k4unl.minecraft.Hydraulicraft.fluids;

import k4unl.minecraft.Hydraulicraft.blocks.ITooltipProvider;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * @author Koen Beckers (K-4U)
 */
public class FluidFluoroCarbon extends Fluid implements ITooltipProvider {

    public FluidFluoroCarbon() {
        super(Names.fluidFluoroCarbon.unlocalized);
        setDensity(10); //How thick the fluid is, affects movement inside the fluid.
        setViscosity(3000); // How fast the fluid flows.

        setUnlocalizedName(Names.fluidFluoroCarbon.unlocalized);
        setBlock(Fluids.fluidFluoroCarbonFluidBlock);

        FluidRegistry.registerFluid(this);
    }

    @Override
    public String getToolTip() {

        return "Used to breathe underwater";
    }
}
