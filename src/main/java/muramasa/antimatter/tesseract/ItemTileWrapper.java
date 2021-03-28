package muramasa.antimatter.tesseract;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import muramasa.antimatter.Data;
import muramasa.antimatter.cover.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.Tesseract;
import tesseract.api.item.IItemNode;
import tesseract.api.item.ItemData;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;

public class ItemTileWrapper implements IItemNode {

    private TileEntity tile;
    private boolean removed;
    private IItemHandler handler;

    private CoverStack[] covers = new CoverStack[] {
        Data.COVER_EMPTY, Data.COVER_EMPTY, Data.COVER_EMPTY, Data.COVER_EMPTY, Data.COVER_EMPTY, Data.COVER_EMPTY
    };

    private ItemTileWrapper(TileEntity tile, IItemHandler handler) {
        this.tile = tile;
        this.handler = handler;
    }

    @Nullable
    public static ItemTileWrapper of(World world, BlockPos pos, Direction side, Supplier<TileEntity> supplier) {
        Tesseract.ITEM.registerNode(world.getDimensionKey(),pos.toLong(), () -> {
            TileEntity tile = supplier.get();
            LazyOptional<IItemHandler> capability = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (capability.isPresent()) {
                ItemTileWrapper node = new ItemTileWrapper(tile, capability.orElse(null));
                capability.addListener(o -> node.onRemove(null));
                //Tesseract.ITEM.registerNode(tile.getWorld().getDimensionKey(), tile.getPos().toLong(), () -> node);
                return node;
            }
            throw new RuntimeException("invalid capability");
        });
        /*LazyOptional<IItemHandler> capability = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (capability.isPresent()) {
            ItemTileWrapper node = new ItemTileWrapper(tile, capability.orElse(null));
            capability.addListener(o -> node.onRemove(null));
            Tesseract.ITEM.registerNode(tile.getWorld().getDimensionKey(), tile.getPos().toLong(), () -> node);
            return node;
        }*/
        return null;
    }

    public void onRemove(@Nullable Direction side) {
        if (side == null) {
            if (tile.isRemoved()) {
                Tesseract.ITEM.remove(tile.getWorld().getDimensionKey(), tile.getPos().toLong());
                removed = true;
            } else {
                // What if tile is recreate cap ?
            }
        } else {
            covers[side.getIndex()] = Data.COVER_EMPTY;
        }
    }

    @Override
    public int insert(ItemStack stack, boolean simulate) {
        int count = stack.getCount();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inserted = handler.insertItem(i, stack, simulate);
            if (inserted.getCount() < stack.getCount()) {
                return inserted.getCount();
            }
        }
        return count;
    }

    @Nullable
    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        ItemStack stack = handler.extractItem(slot, amount, simulate);
        return stack;
    }

    @Nonnull
    @Override
    public IntList getAvailableSlots(Dir direction) {
        Set<?> filtered = getFiltered(direction.getIndex());
        int size = handler.getSlots();
        IntList slots = new IntArrayList(size);
        if (filtered.isEmpty()) {
            for (int i = 0; i < size; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    slots.add(i);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty() && filtered.contains(stack.getItem())) {
                    slots.add(i);
                }
            }
        }
        return slots;
    }

    @Override
    public int getOutputAmount(Dir direction) {
        return 1;
    }

    @Override
    public int getPriority(Dir direction) {
        return 0;
    }

    @Override
    public boolean isEmpty(int slot) {
        return handler.getStackInSlot(slot).isEmpty();
    }

    @Override
    public boolean canOutput() {
        return handler != null;
    }

    @Override
    public boolean canInput() {
        return handler != null;
    }

    @Override
    public boolean canOutput(Dir direction) {
        return true;
    }

    @Override
    public boolean canInput(ItemStack item, Dir direction) {
        return isItemAvailable(item, direction.getIndex()) && getFirstValidSlot(item) != -1;
    }

    @Override
    public boolean connects(Dir direction) {
        return true;
    }

    private boolean isItemAvailable(ItemStack item, int dir) {
        if (covers[dir].getCover() instanceof CoverTintable) return false;
        Set<?> filtered = getFiltered(dir);
        return filtered.isEmpty() || filtered.contains(item);
    }

    // Fast way to find available slot for item
    private int getFirstValidSlot(ItemStack item) {
        int slot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty() && slot == -1) {
                slot = i;
            } else {
                if (stack.getItem().equals(item.getItem()) && stack.getMaxStackSize() > stack.getCount()){
                    return i;
                }
            }
        }
        return slot;
    }

    private Set<?> getFiltered(int index) {
        return covers[index].getCover() instanceof CoverFilter<?> ? ((CoverFilter<?>) covers[index].getCover()).getFilter() : ObjectSets.EMPTY_SET;
    }
}
