package muramasa.antimatter.capability.machine;

import muramasa.antimatter.recipe.ingredient.RecipeIngredient;
import muramasa.antimatter.recipe.ingredient.impl.Ingredients;
import muramasa.antimatter.tile.TileEntityMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IIntArray;
import net.minecraftforge.common.ForgeHooks;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CookingRecipeHandler<T extends TileEntityMachine<T>> extends MachineRecipeHandler<T> {

    protected int burnDuration = 0;
    protected static final Supplier<List<RecipeIngredient>> BURNABLE = () -> Collections.singletonList(RecipeIngredient.of(Ingredients.BURNABLES, 1));

    public CookingRecipeHandler(T tile) {
        super(tile);
    }

    private boolean consume(boolean simulate) {
        List<ItemStack> stack;
        if (simulate) {
            stack = tile.itemHandler.map(t -> t.consumeInputs(BURNABLE.get(), true)).orElse(Collections.emptyList());
            return !stack.isEmpty();
        }
        if (!(stack = tile.itemHandler.map(t -> t.consumeInputs(BURNABLE.get(), false)).orElse(Collections.emptyList())).isEmpty()) {
            burnDuration += ForgeHooks.getBurnTime(stack.get(0)) / 10;
            return true;
        }
        return false;
    }

    @Override
    public IIntArray getProgressData() {
        IIntArray sup = super.getProgressData();
        return new IIntArray() {
            @Override
            public int get(int index) {
                if (index == sup.size()) {
                    return CookingRecipeHandler.this.burnDuration;
                }
                return sup.get(index);
            }

            @Override
            public void set(int index, int value) {
                if (index == sup.size()) {
                    CookingRecipeHandler.this.burnDuration = value;
                    return;
                }
                sup.set(index, value);
            }

            @Override
            public int size() {
                return sup.size() + 1;
            }
        };
    }

    @Override
    public boolean consumeResourceForRecipe(boolean simulate) {
        if (simulate) return consume(true);
        if (burnDuration == 0) {
            if (!consume(false)) return false;
        } else {
            burnDuration--;
            return burnDuration >= 0;
        }
        return burnDuration > 0;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("burn", burnDuration);
        return nbt;
    }

    @Override
    public void getInfo(List<String> builder) {
        super.getInfo(builder);
        if (burnDuration > 0) builder.add("Current burn time left: " + burnDuration);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return super.accepts(stack) || Ingredients.BURNABLES.test(stack);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.burnDuration = nbt.getInt("burn");
    }
}
