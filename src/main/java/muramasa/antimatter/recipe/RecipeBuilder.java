package muramasa.antimatter.recipe;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import muramasa.antimatter.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class RecipeBuilder {

    private RecipeMap recipeMap;
    private ItemStack[] itemsOutput;
    private List<AntimatterIngredient> ingredientInput;
    private FluidStack[] fluidsInput, fluidsOutput;
    private int[] chances;
    private int duration, special;
    private long power;
    private boolean hidden;
    private Set<RecipeTag> tags = new ObjectOpenHashSet<>();

    public Recipe add() {
        if (itemsOutput != null && !Utils.areItemsValid(itemsOutput)) {
            Utils.onInvalidData("RECIPE BUILDER ERROR - OUTPUT ITEMS INVALID!");
            return Utils.getEmptyRecipe();
        }
        if (fluidsInput != null && !Utils.areFluidsValid(fluidsInput)) {
            Utils.onInvalidData("RECIPE BUILDER ERROR - INPUT FLUIDS INVALID!");
            return Utils.getEmptyRecipe();
        }
        if (fluidsOutput != null && !Utils.areFluidsValid(fluidsOutput)) {
            Utils.onInvalidData("RECIPE BUILDER ERROR - OUTPUT FLUIDS INVALID!");
            return Utils.getEmptyRecipe();
        }
        if (ingredientInput != null) {
            for (AntimatterIngredient ai : ingredientInput) {
                if (ai.getMatchingStacks().length == 0) {
                    Utils.onInvalidData("RECIPE BUILDER ERROR - INPUT ITEMS INVALID!");
                    return Utils.getEmptyRecipe();
                }
            }
        }
        if (/*AntimatterConfig.RECIPE.ENABLE_RECIPE_UNIFICATION && */itemsOutput != null) {
            for (int i = 0; i < itemsOutput.length; i++) {
                itemsOutput[i] = Unifier.get(itemsOutput[i]);
            }
        }

        //TODO validate item/fluid inputs/outputs do not exceed machine gui values
        //TODO get a recipe build method to machine type so it can be overriden?
        //otherwise we get NPEs everywhere :S so keep this non-null
        if (ingredientInput == null) ingredientInput = Collections.emptyList();
        Recipe recipe = new Recipe(
            ingredientInput,
            itemsOutput != null ? itemsOutput.clone() : null,
            fluidsInput != null ? fluidsInput.clone() : null,
            fluidsOutput != null ? fluidsOutput.clone() : null,
            duration, power, special
        );
        if (chances != null) recipe.addChances(chances);
        recipe.setHidden(hidden);
        recipe.addTags(new ObjectOpenHashSet<>(tags));

        recipeMap.add(recipe);

        return recipe;
    }

    public Recipe add(long duration, long power, long special) {
        this.duration = (int)duration;
        this.power = power;
        this.special = (int)special;
        return add();
    }

    public Recipe add(long duration, long power) {
        return add(duration, power, 0);
    }

    public Recipe add(int duration) {
        return add(duration, 0, 0);
    }

    public RecipeBuilder ii(AntimatterIngredient... stacks) {
        ingredientInput = Arrays.asList(stacks);
        return this;
    }

    public RecipeBuilder ii(List<AntimatterIngredient> stacks) {
        ingredientInput = stacks;
        return this;
    }

    public RecipeBuilder io(ItemStack... stacks) {
        itemsOutput = stacks;
        return this;
    }

    public RecipeBuilder fi(FluidStack... stacks) {
        fluidsInput = stacks;
        return this;
    }

    public RecipeBuilder fo(FluidStack... stacks) {
        fluidsOutput = stacks;
        return this;
    }

    /** 10 = 10%, 75 = 75% etc **/
    public RecipeBuilder chances(int... values) {
        chances = values;
        return this;
    }

    public RecipeBuilder hide() {
        hidden = true;
        return this;
    }

    public RecipeBuilder tags(RecipeTag... tags) {
        this.tags = new ObjectOpenHashSet<>(tags);
        return this;
    }

    public void clear() {
        itemsOutput = null;
        ingredientInput = null;
        fluidsInput = fluidsOutput = null;
        chances = null;
        duration = special = 0;
        power = 0;
        hidden = false;
        tags.clear();
    }

    public RecipeMap getMap() {
        return recipeMap;
    }

    public void setMap(RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }
}
