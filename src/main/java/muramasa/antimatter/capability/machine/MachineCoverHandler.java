package muramasa.antimatter.capability.machine;

import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.CoverHandler;
import muramasa.antimatter.capability.Dispatch;
import muramasa.antimatter.capability.ICoverHandler;
import muramasa.antimatter.capability.IMachineHandler;
import muramasa.antimatter.cover.CoverStack;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.tool.AntimatterToolType;
import muramasa.antimatter.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

import static muramasa.antimatter.Data.COVERNONE;

public class MachineCoverHandler<T extends TileEntityMachine<T>> extends CoverHandler<T> implements IMachineHandler, Dispatch.Sided<ICoverHandler> {
    public MachineCoverHandler(T tile) {
        super(tile, tile.getValidCovers());
        Arrays.stream(Ref.DIRS).forEach(d -> {
            Direction facing = getTileFacing();
            Direction newDir = Utils.coverRotateFacing(d, facing);
            //Don't use set(), it calls onPlace which might call into Tesseract.
            CoverStack<T> cover = new CoverStack<T>(tile.getMachineType().defaultCover(newDir), tile, d);
            covers.put(d, cover);
            buildLookup(COVERNONE, cover.getCover(), d);
        });
    }

    public Direction getOutputFacing() {
        return lookupSingle(getTile().getMachineType().getOutputCover().getClass());
    }

    public CoverStack<T> getOutputCover() {
        return get(lookupSingle(getTile().getMachineType().getOutputCover().getClass()));
    }

    public boolean setOutputFacing(PlayerEntity entity, Direction side) {
        Direction dir = getOutputFacing();
        if (dir == null) return false;
        if (side == dir) return false;
        if (getTileFacing() == side && !getTile().getMachineType().allowsFrontCovers()) return false;
        boolean ok = moveCover(entity, dir, side);
        if (ok) {
            getTile().invalidateCaps();
        }
        return ok;
    }

    @Override
    public boolean set(Direction side, CoverStack<T> old, CoverStack<T> stack, boolean sync) {
        if (getTileFacing() == side && !getTile().getMachineType().allowsFrontCovers()) return false;
        return super.set(side, old, stack, sync);
    }

    @Override
    protected boolean canRemoveCover(ICover cover) {
        return !getTile().getMachineType().getOutputCover().isEqual(cover);
    }

    @Override
    public boolean onInteract(@Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull Direction side, @Nullable AntimatterToolType type) {
        return super.onInteract(player, hand, side, type);
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        covers.forEach((s, c) -> c.onMachineEvent(getTile(), event));
    }

    @Override
    public boolean isValid(@Nonnull Direction side, @Nonnull ICover replacement) {
        if (!validCovers.contains(replacement.getId())) return false;
        if (side == getOutputFacing()) return false;
        return (get(side).isEmpty() && !(replacement == COVERNONE)) || super.isValid(side, replacement);
    }

    @Override
    public Direction getTileFacing() {
        return getTile().getFacing();
    }

    @Override
    public LazyOptional<ICoverHandler> forSide(Direction side) {
        return LazyOptional.of(() -> this);
    }

    @Override
    public void refresh() {

    }
}
