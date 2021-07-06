package muramasa.antimatter.tile.single;

import muramasa.antimatter.capability.machine.MachineEnergyHandler;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.tile.TileEntityMachine;
import net.minecraft.util.Direction;

import static muramasa.antimatter.machine.MachineFlag.GENERATOR;

public class TileEntityGenerator<T extends TileEntityGenerator<T>> extends TileEntityMachine<T> {

    public TileEntityGenerator(Machine<?> type) {
        super(type);
        energyHandler.set(() -> new MachineEnergyHandler<T>((T)this, type.amps(),type.has(GENERATOR)){
            @Override
            public boolean canInput(Direction direction) {
                return false;
            }
            @Override
            public boolean canInput() {
                return false;
            }

            @Override
            public boolean canOutput(Direction direction) {
               return super.canOutput(direction) && direction == tile.getFacing();

            }
        });
    };
    @Override
    public Tier getPowerLevel() {
        return Tier.getMax();
    }

    
}
