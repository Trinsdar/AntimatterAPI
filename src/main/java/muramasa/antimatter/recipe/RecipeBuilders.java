package muramasa.antimatter.recipe;

import com.google.common.collect.ImmutableMap;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.material.MaterialItem;
import muramasa.antimatter.material.MaterialTypeItem;
import muramasa.antimatter.pipe.PipeItemBlock;
import muramasa.antimatter.pipe.PipeSize;
import muramasa.antimatter.pipe.types.FluidPipe;
import muramasa.antimatter.pipe.types.ItemPipe;
import muramasa.antimatter.pipe.types.PipeType;
import muramasa.antimatter.recipe.ingredient.PropertyIngredient;
import muramasa.antimatter.recipe.material.MaterialRecipe;
import muramasa.antimatter.tool.AntimatterToolType;
import muramasa.antimatter.tool.armor.AntimatterArmorType;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static muramasa.antimatter.Data.NULL;

public class RecipeBuilders {
    /** RECIPE BUILDERS **/

    public static final MaterialRecipe.Provider ARMOR_BUILDER = MaterialRecipe.registerProvider("armor", id -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            return AntimatterAPI.get(AntimatterArmorType.class, id).getToolStack((Material) mats.mats.get("primary"));
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            CompoundNBT nbt = stack.getTag(). getCompound(Ref.TAG_TOOL_DATA);
            Material primary = AntimatterAPI.get(Material.class, nbt.getString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL));
            return ImmutableMap.of("primary", primary != null ? primary : NULL);
        }
    });

    public static final MaterialRecipe.Provider ITEM_PIPE_BUILDER = MaterialRecipe.registerProvider("pipe", id  -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            PipeSize size = PipeSize.valueOf(id.toUpperCase(Locale.ENGLISH));
            Material mat = (Material) mats.mats.get("primary");
            PipeType p = AntimatterAPI.get(ItemPipe.class, "item_" + mat.getId());
            int amount = size == PipeSize.TINY ? 12 : size == PipeSize.SMALL ? 6 : size == PipeSize.NORMAL ? 2 : 1;
            return new ItemStack(p.getBlock(size), amount);
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            return ImmutableMap.of("primary",((PipeItemBlock)stack.getItem()).getPipe().getType().getMaterial());
        }
    });

    public static final MaterialRecipe.Provider DUST_BUILDER = MaterialRecipe.registerProvider("dust", id -> new MaterialRecipe.ItemBuilder() {
        final MaterialTypeItem type = AntimatterAPI.get(MaterialTypeItem.class, id);
        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            Material mat = (Material) mats.mats.get("primary");
            return type.get(mat, 1);
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            if (stack.getItem() instanceof MaterialItem) {
                return ImmutableMap.of("primary", ((MaterialItem)stack.getItem()).getMaterial());
            }
            Material mat = type.tryMaterialFromItem(stack);
            if (mat != null) {
                return ImmutableMap.of("primary", mat);
            }
            return null;
        }
    });

    public static final MaterialRecipe.Provider FLUID_PIPE_BUILDER = MaterialRecipe.registerProvider("fluid", id  -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            PipeSize size = PipeSize.valueOf(id.toUpperCase(Locale.ENGLISH));
            Material mat = (Material) mats.mats.get("primary");
            PipeType p = AntimatterAPI.get(FluidPipe.class, "fluid_" + mat.getId());
            int amount = size == PipeSize.TINY ? 12 : size == PipeSize.SMALL ? 6 : size == PipeSize.NORMAL ? 2 : 1;
            return new ItemStack(p.getBlock(size), amount);
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            return ImmutableMap.of("primary",((PipeItemBlock)stack.getItem()).getPipe().getType().getMaterial());
        }
    });

    public static final MaterialRecipe.Provider TOOL_BUILDER = MaterialRecipe.registerProvider("tool", id -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            Material m = (Material) mats.mats.get("secondary");
            AntimatterToolType type = AntimatterAPI.get(AntimatterToolType.class, id);
            ItemStack stack = type.getToolStack((Material) mats.mats.get("primary"), m == null ? NULL : m);
            return stack;
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            CompoundNBT nbt = stack.getTag().getCompound(Ref.TAG_TOOL_DATA);
            Material primary = AntimatterAPI.get(Material.class, nbt.getString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL));
            Material secondary = AntimatterAPI.get(Material.class, nbt.getString(Ref.KEY_TOOL_DATA_SECONDARY_MATERIAL));
            return ImmutableMap.of("primary", primary != null ? primary : NULL, "secondary", secondary != null ? secondary : NULL);
        }
    });

    public static final MaterialRecipe.Provider WOOD_TOOL_BUILDER = MaterialRecipe.registerProvider("wood_tool", id -> new MaterialRecipe.ItemBuilder() {

        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            Material m = (Material) mats.mats.get("secondary");
            AntimatterToolType type = AntimatterAPI.get(AntimatterToolType.class, id);
            ItemStack stack = type.getToolStack(Material.get("wood"), m == null ? NULL : m);
            return stack;
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            CompoundNBT nbt = stack.getTag().getCompound(Ref.TAG_TOOL_DATA);
            Material primary = AntimatterAPI.get(Material.class, nbt.getString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL));
            Material secondary = AntimatterAPI.get(Material.class, nbt.getString(Ref.KEY_TOOL_DATA_SECONDARY_MATERIAL));
            return ImmutableMap.of("primary", primary != null ? primary : NULL, "secondary", secondary != null ? secondary : NULL);
        }
    });

    public static final MaterialRecipe.Provider CROWBAR_BUILDER = MaterialRecipe.registerProvider("crowbar", id -> new MaterialRecipe.ItemBuilder() {
        @Override
        public ItemStack build(CraftingInventory inv, MaterialRecipe.Result mats) {
            int dye = ((DyeColor) mats.mats.get("secondary")).getColorValue();
            AntimatterToolType type = AntimatterAPI.get(AntimatterToolType.class, id);
            ItemStack stack = type.getToolStack(((Material) mats.mats.get("primary")), NULL);
            stack.getChildTag(Ref.TAG_TOOL_DATA).putInt(Ref.KEY_TOOL_DATA_SECONDARY_COLOUR, dye);
            return stack;
        }

        @Override
        public Map<String, Object> getFromResult(@Nonnull ItemStack stack) {
            CompoundNBT nbt = stack.getTag().getCompound(Ref.TAG_TOOL_DATA);
            Material primary = AntimatterAPI.get(Material.class, nbt.getString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL));
            int secondary = nbt.getInt(Ref.KEY_TOOL_DATA_SECONDARY_COLOUR);
            Optional<DyeColor> color = Arrays.stream(DyeColor.values()).filter(t -> t.getColorValue() == secondary).findFirst();
            return ImmutableMap.of("primary", primary != null ? primary : NULL, "secondary", color.orElse(DyeColor.WHITE));
        }
    });

    static {
        PropertyIngredient.addGetter(Tags.Items.DYES.getName(), DyeColor::getColor);
    }
}
