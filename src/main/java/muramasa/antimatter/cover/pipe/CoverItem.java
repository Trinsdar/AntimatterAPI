package muramasa.antimatter.cover.pipe;

import it.unimi.dsi.fastutil.ints.IntList;
import muramasa.antimatter.cover.Cover;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import tesseract.api.item.IItemNode;
import tesseract.api.item.ItemData;
import tesseract.graph.ITickingController;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoverItem extends Cover implements IItemNode {

    @Override
    public String getId() {
        return "item";
    }

    /*@Override
    public void onSet(TileEntity tile, Direction side) {
        World world = tile.getWorld();
        if (world != null)
            TesseractAPI.registerItemNode(world.getDimension().getType().getId(), tile.getPos().toLong(), this);
    }*/

    @Override
    public void onRemove(TileEntity tile, Direction side) {

    }

    @Override
    public void onUpdate(TileEntity tile, Direction side) {

    }

    @Override
    public int insert(@Nonnull ItemData data, boolean simulate) {
        return 0;
    }

    @Nullable
    @Override
    public ItemData extract(int slot, int amount, boolean simulate) {
        return null;
    }

    @Nonnull
    @Override
    public IntList getAvailableSlots(@Nonnull Dir direction) {
        return null;
    }

    @Override
    public int getOutputAmount(@Nonnull Dir direction) {
        return 0;
    }

    @Override
    public int getPriority(@Nonnull Dir direction) {
        return 0;
    }

    @Override
    public boolean isEmpty(int slot) {
        return false;
    }

    @Override
    public boolean canOutput() {
        return false;
    }

    @Override
    public boolean canInput() {
        return false;
    }

    @Override
    public boolean canOutput(@Nonnull Dir direction) {
        return false;
    }

    @Override
    public boolean canInput(@Nonnull Object item, @Nonnull Dir direction) {
        return false;
    }

    @Override
    public boolean connects(@Nonnull Dir direction) {
        return false;
    }

    @Override
    public void reset(@Nullable ITickingController oldController, @Nullable ITickingController newController) {

    }
}
