package muramasa.antimatter.gui.slot;

import muramasa.antimatter.capability.machine.MachineFluidHandler;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.tile.TileEntityMachine;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;

public class SlotFakeFluid extends AbstractSlot<SlotFakeFluid> {

    public final MachineFluidHandler.FluidDirection dir;

    public SlotFakeFluid(SlotType<SlotFakeFluid> type, TileEntityMachine<?> tile, MachineFluidHandler.FluidDirection dir, int index, int x, int y) {
        super(type, tile, new EmptyHandler(), index, x, y);
        this.dir = dir;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return false;
    }
}
