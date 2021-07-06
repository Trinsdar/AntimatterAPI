package muramasa.antimatter.material;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.registration.IAntimatterObject;
import muramasa.antimatter.util.TagUtils;
import muramasa.antimatter.util.Utils;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.ITag;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class MaterialTypeFluid<T> extends MaterialType<T> {

    public MaterialTypeFluid(String id, int layers, boolean visible, int unitValue) {
        super(id, layers, visible, unitValue);
        AntimatterAPI.register(MaterialTypeFluid.class, id, this);
    }

    public static FluidStack getEmptyFluidAndLog(MaterialType<?> type, IAntimatterObject... objects) {
        Utils.onInvalidData("Tried to create " + type.getId() + " for objects: " + Arrays.toString(Arrays.stream(objects).map(IAntimatterObject::getId).toArray(String[]::new)));
        return new FluidStack(Fluids.WATER, 1);
    }

    @Override
    protected ITag.INamedTag<?> tagFromString(String name) {
        return TagUtils.getForgeFluidTag(name);
    }

    public interface IFluidGetter {
        FluidStack get(Material m, int amount);
    }
}
