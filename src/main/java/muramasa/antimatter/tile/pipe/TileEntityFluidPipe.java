package muramasa.antimatter.tile.pipe;

import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.pipe.PipeCoverHandler;
import muramasa.antimatter.capability.pipe.PipeFluidHandler;
import muramasa.antimatter.pipe.types.FluidPipe;
import muramasa.antimatter.pipe.types.PipeType;
import muramasa.antimatter.tesseract.FluidTileWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import tesseract.Tesseract;
import tesseract.api.capability.TesseractFluidCapability;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;

import java.util.List;

public class TileEntityFluidPipe extends TileEntityPipe implements IFluidPipe {

    protected LazyOptional<PipeFluidHandler> fluidHandler;

    public TileEntityFluidPipe(PipeType<?> type) {
        super(type);
        if (fluidHandler == null) {
            fluidHandler = FluidController.SLOOSH ? LazyOptional.of(() -> new PipeFluidHandler(this,1000*(getPipeSize().ordinal()+1),1000,1,0)) : LazyOptional.empty();
        } 
    }

    @Override
    protected void initTesseract() {
        if (isServerSide()) Tesseract.FLUID.registerConnector(getWorld(), pos.toLong(), this); // this is connector class
        super.initTesseract();
    }

    @Override
    public boolean validateTile(TileEntity tile, Direction side) {
        return tile instanceof TileEntityFluidPipe || tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).isPresent();
    }

    @Override
    public void refreshConnection() {
        if (isServerSide()) {
            if (Tesseract.FLUID.remove(getWorld(), pos.toLong())) {
                Tesseract.FLUID.registerConnector(getWorld(), pos.toLong(), this); // this is connector class
            }
        }
        super.refreshConnection();
    }

    

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        if (tag.contains(Ref.KEY_MACHINE_FLUIDS)) fluidHandler.ifPresent(t -> t.deserializeNBT(tag.getCompound(Ref.KEY_MACHINE_FLUIDS)));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        CompoundNBT nbt = super.write(tag);
        fluidHandler.ifPresent(t -> tag.put(Ref.KEY_MACHINE_FLUIDS, t.serializeNBT()));
        return nbt;
    }

    @Override
    public void registerNode(BlockPos pos, Direction side, boolean remove) {
        if (!remove) {
            FluidTileWrapper.wrap(this, getWorld(), pos, side, () -> world.getTileEntity(pos));
        } else {
            Tesseract.FLUID.remove(getWorld(), pos.toLong());
        }
    }

    @Override
    public void onRemove() {
        if (isServerSide()) Tesseract.FLUID.remove(getWorld(), pos.toLong());
        fluidHandler.ifPresent(t -> t.onRemove());
        fluidHandler.invalidate();
        super.onRemove();
    }

    @Override
    public boolean isGasProof() {
        return ((FluidPipe<?>)getPipeType()).isGasProof();
    }

    @Override
    public int getCapacity() {
        return ((FluidPipe<?>)getPipeType()).getCapacity(getPipeSize());
    }

    @Override
    public int getPressure() {
        return ((FluidPipe<?>)getPipeType()).getPressure(getPipeSize());
    }

    @Override
    public int getTemperature() {
        return ((FluidPipe<?>)getPipeType()).getTemperature();
    }

    @Override
    public boolean connects(Direction direction) {
        return canConnect(direction.getIndex());
    }

    @Override
    protected Capability<?> getCapability() {
        return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    public static class TileEntityCoveredFluidPipe extends TileEntityFluidPipe implements ITickablePipe {

        public TileEntityCoveredFluidPipe(PipeType<?> type) {
            super(type);
        }

        @Override
        public LazyOptional<PipeCoverHandler<?>> getCoverHandler() {
            return this.coverHandler;
        }
    }

    @Override
    public IFluidNode getNode() {
        return this.fluidHandler.orElse(null);
    }

    @Override
    public List<String> getInfo() {
        List<String> list = super.getInfo();
        fluidHandler.ifPresent(t -> {
            for (int i = 0; i < t.getTanks(); i++) {
                FluidStack stack = t.getFluidInTank(i);
                list.add(stack.getFluid().getRegistryName().toString() + " " + stack.getAmount() + " mb.");
            }
        });
        return list;
    }


    @Override
    protected LazyOptional<?> buildCapForSide(Direction side) {
        if (FluidController.SLOOSH) {
            if (fluidHandler == null) {
                fluidHandler = LazyOptional.of(() -> new PipeFluidHandler(this,1000*(getPipeSize().ordinal()+1),1000,1,0));
            }
        } else {
            return LazyOptional.of(() -> new TesseractFluidCapability(this, side));
        }
        return fluidHandler;
    }
}
