package muramasa.antimatter.capability.machine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.EnergyHandler;
import muramasa.antimatter.capability.IMachineHandler;
import muramasa.antimatter.machine.MachineFlag;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.event.ContentEvent;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.event.MachineEvent;
import muramasa.antimatter.recipe.Recipe;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static muramasa.antimatter.machine.MachineState.*;

public class MachineRecipeHandler<T extends TileEntityMachine> implements IMachineHandler {

    protected final T tile;
    protected final boolean generator;
    /**
     * Indices:
     * 0 -> Progress of recipe
     */
    protected final IIntArray GUI_SYNC_DATA = new IntArray(1);

    protected Recipe activeRecipe;
    protected boolean consumedResources;
    protected int currentProgress, maxProgress;
    protected int overclock;

    //20 seconds per check.
    static final int WAIT_TIME = 20*20;
    static final int WAIT_TIME_POWER_LOSS = 20*5;
    protected int tickTimer = 0;

    //Consuming resources can call into the recipe handler, causing a loop.
    //For instance, consuming fluid in the fluid handlers calls back into the MachineRecipeHandler, deadlocking.
    //So just 'lock' during recipe ticking.
    private boolean tickingRecipe = false;

    //Items used to find recipe
    protected List<ItemStack> itemInputs = new ObjectArrayList<>();
    protected List<FluidStack> fluidInputs = new ObjectArrayList<>();

    public MachineRecipeHandler(T tile) {
        this.tile = tile;
        GUI_SYNC_DATA.set(0,0);
        this.generator = tile.getMachineType().has(MachineFlag.GENERATOR);
    }

    public IIntArray getProgressData() {
        return GUI_SYNC_DATA;
    }

    public void setClientProgress() {
        setClientProgress(Float.floatToRawIntBits(this.currentProgress / (float) this.maxProgress));
    }

    public void getInfo(List<String> builder) {
        if (activeRecipe != null) {
            if (tile.getMachineState() != ACTIVE) {
                builder.add("Active recipe but not running");
            }
            builder.add("Progress: " + currentProgress + "/" + maxProgress);
        } else {
            builder.add("No recipe active");
        }
    }

    public boolean hasRecipe() {
        return activeRecipe != null;
    }

    public void setClientProgress(int value) {
        this.GUI_SYNC_DATA.set(0, value);
    }

    @OnlyIn(Dist.CLIENT)
    public float getClientProgress() {
        return Float.intBitsToFloat(this.GUI_SYNC_DATA.get(0));
    }

    @Override
    public void init() {
        checkRecipe();
    }

    public void onServerUpdate() {
        //First, a few timer related tasks that ensure the machine can recover from certain situations.
        if (activeRecipe == null && tickTimer >= WAIT_TIME) {
            tickTimer = 0;
            //Convert from power_loss to idle.
            checkRecipe();
        }
        else if (tile.getMachineState() == POWER_LOSS && tickTimer >= WAIT_TIME_POWER_LOSS) {
            tile.setMachineState(NO_POWER);
            tickTimer = 0;
        }
        else if (activeRecipe == null || tile.getMachineState() == POWER_LOSS) {
            tickTimer++;
            return;
        }
        if (tickingRecipe || activeRecipe == null) return;
        tickingRecipe = true;
        MachineState state;
        switch (tile.getMachineState()) {
            case ACTIVE:
                tile.setMachineState(tickRecipe());
                break;
            case IDLE:
                break;
            case POWER_LOSS:
                break;
            case OUTPUT_FULL:
                break;
            default:
                state = tickRecipe();
                if (state != ACTIVE) {
                    tile.setMachineState(IDLE);
                } else {
                    tile.setMachineState(state);
                }
                break;
        }
        tickingRecipe = false;
    }

    public Recipe findRecipe() {
        return tile.getMachineType().getRecipeMap().find(tile.getPowerLevel(), tile.itemHandler, tile.fluidHandler);
    }


    //called when a new recipe is found, to process overclocking
    public void activateRecipe(boolean reset) {
        //if (canOverclock)
        if (reset) currentProgress = 0;
        consumedResources = false;
        maxProgress = activeRecipe.getDuration();
        overclock = 0;
        if (this.tile.getPowerLevel().getVoltage() > activeRecipe.getPower()) {
            long voltage = this.activeRecipe.getPower();
            int tier = 0;
            //Dont use utils, because we allow overclocking from ulv. (If we don't just change this).
            for (int i = 0; i < Ref.V.length; i++) {
                if (voltage <= Ref.V[i]) {
                    tier = i;
                    break;
                }
            }
            int tempoverclock = (this.tile.getPowerLevel().getVoltage() / Ref.V[tier]);
            while (tempoverclock > 1) {
                tempoverclock >>= 2;
                overclock++;
            }
        }
        maxProgress = Math.max(1, maxProgress >>= overclock);
    }

    protected void addOutputs() {
        if (activeRecipe.hasOutputItems()) {
            tile.itemHandler.ifPresent(h -> {
                h.addOutputs(activeRecipe.getOutputItems());
                tile.onMachineEvent(MachineEvent.ITEMS_OUTPUTTED);
            });
        }
        if (activeRecipe.hasOutputFluids()) {
            tile.fluidHandler.ifPresent(h -> {
                for (FluidStack stack : activeRecipe.getOutputFluids()) {
                    h.addOutputs(stack);
                    // h.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                }
                tile.onMachineEvent(MachineEvent.FLUIDS_OUTPUTTED);
            });
        }
    }

    protected MachineState recipeFinish() {
        addOutputs();
        if (this.generator) {
            currentProgress = 0;
            return ACTIVE;
        }
        if (!canRecipeContinue()) {
            this.resetRecipe();
            return IDLE;
        } else {
            activateRecipe(true);
            return ACTIVE;
        }
    }

    protected MachineState tickRecipe() {
        if (this.activeRecipe == null) {
            System.out.println("Check Recipe when active recipe is null");
            return tile.getMachineState();
        } else if (this.currentProgress == this.maxProgress) {
            if (!canOutput()) {
                setClientProgress(0);
                return OUTPUT_FULL;
            }
            return recipeFinish();
        }
        else {
            tile.onRecipePreTick();
            if (!consumeResourceForRecipe()) {
                if ((currentProgress == 0 && tile.getMachineState() == IDLE) || generator) {
                    //Cannot start a recipe :(
                    resetRecipe();
                    return IDLE;
                } else {
                    //TODO: Hard-mode here?
                    recipeFailure();
                }
                return POWER_LOSS;
            } else {
            }
            if (currentProgress == 0 && !consumedResources) this.consumeInputs();
            this.currentProgress++;
            setClientProgress();
            tile.onRecipePostTick();
            return ACTIVE;
        }
    }

    private void recipeFailure() {
        currentProgress = 0;
        setClientProgress(0);
    }

    public boolean consumeResourceForRecipe() {
        if (tile.energyHandler.isPresent()) {
            if (!generator) {
                if (tile.energyHandler.get().extract((activeRecipe.getPower() * (1L << overclock)), true) >= activeRecipe.getPower() * (1L << overclock)) {
                    tile.energyHandler.get().extract((activeRecipe.getPower() * (1L << overclock)), false);
                    return true;
                }
            } else {
                return consumeGeneratorResources();
            }
        }
        return false;
    }

    protected boolean validateRecipe(Recipe r) {
        int voltage = this.generator ? tile.energyHandler.map(EnergyHandler::getOutputVoltage).orElse(0) : tile.getMaxInputVoltage();
        boolean ok = voltage >= r.getPower()/ r.getAmps();
        return ok;
    }

    protected boolean hasLoadedInput() {
        return itemInputs.size() > 0 || fluidInputs.size() > 0;
    }

    protected void checkRecipe() {
        if (activeRecipe != null) {
            return;
        }
        //First lookup.
        if (!this.tile.hadFirstTick() && hasLoadedInput()) {
            activeRecipe = tile.getMachineType().getRecipeMap().find(itemInputs.toArray(new ItemStack[0]), fluidInputs.toArray(new FluidStack[0]));
            if (activeRecipe == null) return;
            activateRecipe(false);
            tile.setMachineState(ACTIVE);
            return;
        }
        if (tile.getMachineState().allowRecipeCheck()) {
            if ((activeRecipe = findRecipe()) != null) {
                if (!validateRecipe(activeRecipe)) {
                    tile.setMachineState(INVALID_TIER);
                    activeRecipe = null;
                    return;
                }
                if (!canOutput()) {
                    activeRecipe = null;
                    tile.setMachineState(IDLE);
                    return;
                }
                if (generator && (!activeRecipe.hasInputFluids() || activeRecipe.getInputFluids().length != 1)) {
                    return;
                }
                activateRecipe(true);
                tile.setMachineState(ACTIVE);
                return;
            }
            setClientProgress(0);
        }
    }

    @Nullable
    public Recipe getActiveRecipe() {
        return activeRecipe;
    }

    public void consumeInputs() {
        if (!tile.hadFirstTick()) return;
        if (activeRecipe.hasInputItems()) {
            tile.itemHandler.ifPresent(h -> {
                this.itemInputs = h.consumeInputs(activeRecipe,false);
            });
        }
        if (activeRecipe.hasInputFluids()) {
            tile.fluidHandler.ifPresent(h -> {
                this.fluidInputs = h.consumeAndReturnInputs(Arrays.asList(activeRecipe.getInputFluids()));
            });
        }
        consumedResources = true;
    }

    public boolean canOutput() {
        if (tile.itemHandler.isPresent() && activeRecipe.hasOutputItems() && !tile.itemHandler.map(t -> t.canOutputsFit(activeRecipe.getOutputItems())).orElse(false))
            return false;
        return !tile.fluidHandler.isPresent() || !activeRecipe.hasOutputFluids() || tile.fluidHandler.map(t -> t.canOutputsFit(activeRecipe.getOutputFluids())).orElse(false);
    }

    protected boolean canRecipeContinue() {
        return canOutput() && tile.itemHandler.map(i -> i.consumeInputs(this.activeRecipe, true).size() > 0).orElse(false) || Utils.doFluidsMatchAndSizeValid(activeRecipe.getInputFluids(), tile.fluidHandler.map(MachineFluidHandler::getInputs).orElse(new FluidStack[0]));
    }
    /*
      Helper to consume resources for a generator.
     */
    protected boolean consumeGeneratorResources() {
        if (!activeRecipe.hasInputFluids()) {
            throw new RuntimeException("Missing fuel in active generator recipe!");
        }
        boolean shouldRun = tile.energyHandler.map(h -> h.insert((long)((double)tile.getMachineTier().getVoltage()),true) > 0).orElse(false);
        if (!shouldRun) return false;
        long toConsume = calculateGeneratorConsumption(tile.getMachineTier().getVoltage(), activeRecipe);
        if (tile.fluidHandler.map(h -> {
            int amount = h.getInputTanks().drain(new FluidStack(activeRecipe.getInputFluids()[0],(int)toConsume), IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (amount == toConsume) {
                h.getInputTanks().drain(new FluidStack(activeRecipe.getInputFluids()[0],(int)toConsume), IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
            return false;
        }).orElse(false)) {
            //Input energy
            tile.energyHandler.ifPresent(handler -> {
                handler.insert((long)((double)tile.getMachineTier().getVoltage()), false);
            });
            return true;
        }
        return false;
    }

    protected long calculateGeneratorConsumption(int volt, Recipe r) {
       return ((long) (((double)volt / (r.getPower() /(double) Objects.requireNonNull(r.getInputFluids())[0].getAmount())) / (tile.getMachineType().getMachineEfficiency())));

    }

    public void resetRecipe() {
        this.activeRecipe = null;
        this.consumedResources = false;
        this.currentProgress = 0;
        this.overclock = 0;
        setClientProgress(0);
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (tickingRecipe) return;
        if (event instanceof ContentEvent) {
            switch ((ContentEvent) event) {
                case FLUID_INPUT_CHANGED:
                case FLUID_OUTPUT_CHANGED:
                case ITEM_INPUT_CHANGED:
                case ITEM_OUTPUT_CHANGED:
                    if (tile.getMachineState() == OUTPUT_FULL && canOutput()) {
                        tickingRecipe = true;
                        tile.setMachineState(recipeFinish());
                        tickingRecipe = false;
                        return;
                    }
                    if (tile.getMachineState().allowRecipeCheck()) {
                        this.checkRecipe();
                    }
                    break;
            }
        } else if (event instanceof MachineEvent) {
            switch ((MachineEvent) event) {
                case ENERGY_INPUTTED:
                    if (tile.getMachineState() == IDLE && activeRecipe != null) {
                        tile.setMachineState(NO_POWER);
                    }
                    break;
                case ENERGY_DRAINED:
                    if (generator && tile.getMachineState() == tile.getDefaultMachineState()) {
                        if (activeRecipe != null) tile.setMachineState(NO_POWER);
                        else checkRecipe();
                    }
                    break;
            }
        }
    }

    /** NBT STUFF **/

    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT item = new ListNBT();
        if (itemInputs.size() > 0) {
            itemInputs.forEach(t -> item.add(t.serializeNBT()));
        }
        ListNBT fluid = new ListNBT();
        if (itemInputs.size() > 0) {
            itemInputs.forEach(t -> item.add(t.serializeNBT()));
        }
        nbt.put("I", item);
        nbt.put("F", fluid);
        nbt.putInt("P", currentProgress);
        return nbt;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        itemInputs = new ObjectArrayList<>();
        fluidInputs = new ObjectArrayList<>();
        nbt.getList("I",10).forEach(t -> itemInputs.add(ItemStack.read((CompoundNBT) t)));
        nbt.getList("F",10).forEach(t -> fluidInputs.add(FluidStack.loadFluidStackFromNBT((CompoundNBT) t)));
        this.currentProgress = nbt.getInt("P");
    }
}
