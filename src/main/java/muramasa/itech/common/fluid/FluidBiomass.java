package muramasa.itech.common.fluid;

import muramasa.itech.common.utils.Ref;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidBiomass extends Fluid {

    public FluidBiomass() {
        super("biomass", new ResourceLocation(Ref.MODID, "textures/blocks/fluidStack/biomass"), new ResourceLocation(Ref.MODID, "textures/blocks/fluidStack/biomass"));
        FluidRegistry.registerFluid(this);
    }
}
