package muramasa.antimatter;

import muramasa.antimatter.dynamic.ModelConfig;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.pipe.PipeSize;
import muramasa.antimatter.texture.Texture;
import muramasa.antimatter.tile.TileEntityBase;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.function.Function;

public class AntimatterProperties {

    /** Block Dynamic Properties **/
    public static final ModelProperty<ModelConfig> DYNAMIC_CONFIG = new ModelProperty<>();

    /** Block Machine Properties **/
    public static final ModelProperty<Machine<?>> MACHINE_TYPE = new ModelProperty<>();
    public static final ModelProperty<MachineState> MACHINE_STATE = new ModelProperty<>();
    //TODO: Probably converge these two into one somehow.
    public static final ModelProperty<Function<Direction, Texture>> MACHINE_TEXTURE = new ModelProperty<>();
    public static final ModelProperty<Function<Direction, Texture>> MULTI_MACHINE_TEXTURE = new ModelProperty<>();
    public static final ModelProperty<TileEntityBase<?>> TILE_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<BlockState> STATE_MODEL_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<Texture> TEXTURE_MODEL_PROPERTY = new ModelProperty<>();

    /** Block Pipe Properties **/
    //public static PropertyBool PIPE_INSULATED = PropertyBool.create("insulated");
    //public static PropertyBool PIPE_RESTRICTIVE = PropertyBool.create("restrictive");
    public static final ModelProperty<PipeSize> PIPE_SIZE = new ModelProperty<>();
    public static final ModelProperty<Byte> PIPE_CONNECTIONS = new ModelProperty<>();
}
