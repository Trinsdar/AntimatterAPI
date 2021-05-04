package muramasa.antimatter.capability.machine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.capability.FluidHandler;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.machine.event.ContentEvent;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.recipe.Recipe;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import tesseract.Tesseract;

import javax.annotation.Nonnull;
import java.util.*;

import static muramasa.antimatter.machine.MachineFlag.GENERATOR;
import static muramasa.antimatter.machine.MachineFlag.GUI;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class MachineFluidHandler<T extends TileEntityMachine> extends FluidHandler<T> {

    private boolean fillingCell = false;

    public MachineFluidHandler(T tile, int capacity, int pressure) {
        super(tile, capacity, pressure, tile.has(GUI) ? tile.getMachineType().getGui().getSlots(SlotType.FL_IN, tile.getMachineTier()).size() : 0,
            tile.has(GUI) ? tile.getMachineType().getGui().getSlots(SlotType.FL_OUT, tile.getMachineTier()).size() : 0);
    }

    public MachineFluidHandler(T tile) {
        this(tile, 8000 * (1 + tile.getMachineTier().getIntegerId()), 1000 * (250 + tile.getMachineTier().getIntegerId()));
    }

    public int fillCell(int cellSlot, int maxFill) {
        if (fillingCell) return 0;
        fillingCell = true;
        if (getInputTanks() != null) {
            tile.itemHandler.ifPresent(ih -> {
                if (ih.getCellInputHandler() == null) return;
                ItemStack cell = ih.getCellInputHandler().getStackInSlot(cellSlot);
                if (cell.isEmpty()) return;
                ItemStack toActOn = cell.copy();
                toActOn.setCount(1);
                toActOn.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(cfh -> {
                    ItemStack checkContainer = toActOn.copy().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(t -> {
                                if (t.getFluidInTank(0).isEmpty()) {
                                    t.fill(FluidUtil.tryFluidTransfer(t,this.getAllTanks(), maxFill, false), EXECUTE);
                                } else {
                                    t.drain(maxFill, EXECUTE);
                                }
                                return t.getContainer();
                            }).orElse(null/* throw exception */);
                    if (!MachineItemHandler.insertIntoOutput(ih.getCellOutputHandler(),cellSlot,checkContainer,true).isEmpty()) return;

                    FluidStack stack;
                    if (cfh.getFluidInTank(0).isEmpty()) {
                        stack = FluidUtil.tryFluidTransfer(cfh,this.getAllTanks(), maxFill, true);
                    } else {
                        stack = FluidUtil.tryFluidTransfer(this.getAllTanks(),cfh, maxFill, true);
                    }
                    if (!stack.isEmpty()) {
                        ItemStack insert = cfh.getContainer();
                        insert.setCount(1);
                        MachineItemHandler.insertIntoOutput(ih.getCellOutputHandler(),cellSlot, insert, false);
                        ih.getCellInputHandler().extractItem(cellSlot, 1, false);
                    }
                });
            });
        }
        fillingCell = false;
        return 0;
    }

    protected boolean checkValidFluid(FluidStack fluid) {
        if (tile.has(GENERATOR)) {
            Recipe recipe = tile.getMachineType().getRecipeMap().find(new ItemStack[0], new FluidStack[]{fluid}, r -> true);
            if (recipe != null) {
                return true;
            }
        }
        return true;
    }

    protected void tryFillCell(int slot, int maxFill) {
        if (tile.itemHandler.map(MachineItemHandler::getCellCount).orElse(0) > 0) {
            fillCell(slot, maxFill);
        }
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object ...data) {
        super.onMachineEvent(event, data);
        if (event instanceof ContentEvent) {
            switch ((ContentEvent)event) {
                case ITEM_CELL_CHANGED:
                    if (data[0] instanceof Integer) tryFillCell((Integer) data[0], 1000);
                    break;
                case FLUID_INPUT_CHANGED:
                case FLUID_OUTPUT_CHANGED:
                    if (data[0] instanceof Integer) tryFillCell((Integer) data[0], 1000);
                    break;
            }
        }
    }

    public boolean canOutputsFit(FluidStack[] outputs) {
        return getSpaceForOutputs(outputs) >= outputs.length;
    }

    public int getSpaceForOutputs(FluidStack[] outputs) {
        int matchCount = 0;
        if (getOutputTanks() != null) {
            for (FluidStack output : outputs) {
                if (fillOutput(output, SIMULATE) == output.getAmount()) {
                    matchCount++;
                }
            }
        }
        return matchCount;
    }

    public void addOutputs(FluidStack... fluids) {
        if (getOutputTanks() == null) {
            return;
        }
        if (fluids != null) {
            for (FluidStack input : fluids) {
                fillOutput(input,EXECUTE);
            }
        }
    }
    @Nonnull
    public List<FluidStack> consumeAndReturnInputs(List<FluidStack> inputs, boolean simulate) {
        if (getInputTanks() == null) {
            return Collections.emptyList();
        }
        List<FluidStack> notConsumed = new ObjectArrayList<>();
        FluidStack result;
        if (inputs != null) {
            for (FluidStack input : inputs) {
                result = drainInput(input, simulate ? SIMULATE : EXECUTE);
                if (result != FluidStack.EMPTY) {
                    if (result.getAmount() != input.getAmount()) { //Fluid was partially consumed
                        notConsumed.add(Utils.ca(input.getAmount() - result.getAmount(), input));
                    }
                } else {
                    notConsumed.add(input); //Fluid not present in input tanks
                }
            }
        }
        return notConsumed;
    }

    public FluidStack[] exportAndReturnOutputs(FluidStack... outputs) {
        if (getOutputTanks() == null) {
            return new FluidStack[0];
        }
        List<FluidStack> notExported = new ObjectArrayList<>();
        int result;
        for (int i = 0; i < outputs.length; i++) {
            result = fill(outputs[i], EXECUTE);
            if (result == 0) notExported.add(outputs[i]); //Valid space was not found
            else outputs[i] = Utils.ca(result, outputs[i]); //Fluid was partially exported
        }
        return notExported.toArray(new FluidStack[0]);
    }

    @Override
    public boolean canOutput(Direction direction) {
        if (tile.getFacing().getIndex() == direction.getIndex() && !tile.getMachineType().allowsFrontCovers()) return false;
        return super.canOutput();
    }

    @Override
    public boolean canInput(FluidStack fluid, Direction direction) {
        return true;
    }

    @Override
    public boolean canInput(Direction direction) {
        if (tile.getFacing().getIndex() == direction.getIndex() && !tile.getMachineType().allowsFrontCovers()) return false;
        return super.canInput();
    }

    @Override
    public void refreshNet() {
        Tesseract.FLUID.refreshNode(this.tile.getWorld(), this.tile.getPos().toLong());
    }
}
