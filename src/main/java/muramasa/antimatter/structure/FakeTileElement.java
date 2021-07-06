package muramasa.antimatter.structure;

import muramasa.antimatter.Data;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.tile.TileEntityFakeBlock;
import muramasa.antimatter.tile.multi.TileEntityBasicMultiMachine;
import muramasa.antimatter.util.int3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * FakeTileElement represents a fake block for this multiblock. It takes on the appearance of another block
 * as well as rendering possible covers on it. It also forwards capability calls to the master controller.
 * (In the case of multiple controllers, it returns the first one that is non-empty).
 */
public class FakeTileElement extends StructureElement {

    private final IBlockStatePredicate[] preds;
    private final EnumMap<Direction, ICover> covers = new EnumMap<>(Direction.class);

    public FakeTileElement(IBlockStatePredicate... pred) {
        this.preds = pred;
    }

    public FakeTileElement(Block... pred) {
        this.preds = Arrays.stream(pred).map(t -> (IBlockStatePredicate) (reader, pos, state) -> state.getBlock() == Data.PROXY_INSTANCE || state.getBlock().matchesBlock(t)).toArray(IBlockStatePredicate[]::new);
    }

    public FakeTileElement(BlockState... pred) {
        this.preds = Arrays.stream(pred).map(t -> (IBlockStatePredicate) (reader, pos, state) -> state.getBlock() == Data.PROXY_INSTANCE || state.equals(t)).toArray(IBlockStatePredicate[]::new);
    }


    public FakeTileElement() {
        this.preds = new IBlockStatePredicate[0];
    }

    @Override
    public boolean evaluate(TileEntityBasicMultiMachine<?> machine, int3 pos, StructureResult result) {
        BlockState state = machine.getWorld().getBlockState(pos);
        if (state.getBlock().matchesBlock(Data.PROXY_INSTANCE)) {
            TileEntity tile = machine.getWorld().getTileEntity(pos);
            if (tile instanceof TileEntityFakeBlock) {
                BlockState st = ((TileEntityFakeBlock)tile).getState();
                if (st == null) {
                    result.withError("Missing state in fake tile.");
                    return false;
                }
                for (IBlockStatePredicate pred : preds) {
                    if (pred.evaluate((IWorldReader)machine.getWorld(), (BlockPos) pos, st)) {
                        result.addState("fake", pos, st);
                        return true;
                    }
                }
                if (preds.length == 0) {
                    result.addState("fake", pos, st);
                    return true;
                }
            }
            result.withError("Invalid BlockProxy state.");
            return false;
        } else if (StructureCache.refCount(machine.getWorld(), pos) > 0) {
            result.withError("FakeTile sharing a block that is not of proxy type.");
            return false;
        }
        if (state.hasTileEntity()) {
            result.withError("BlockProxy replacement should not have Tile.");
            return false;
        }
        if (preds.length == 0) {
            result.addState("fake", pos, state);
            return true;
        }
        for (IBlockStatePredicate pred : preds) {
            if (pred.evaluate((IWorldReader)machine.getWorld(), (BlockPos) pos, state)) {
                result.addState("fake", pos, state);
                return true;
            }
        }
        result.withError("No matching blocks for FakeTile");
        return false;
    }

    public FakeTileElement cover(Direction side, ICover cover) {
        this.covers.put(side, cover);
        return this;
    }

    @Override
    public void onBuild(TileEntityBasicMultiMachine machine, BlockPos pos, StructureResult result, int count) {
        World world = machine.getWorld();
        BlockState oldState = world.getBlockState(pos);
        //Already set.
        if (count > 1 || oldState.getBlock().matchesBlock(Data.PROXY_INSTANCE)) {
            ((TileEntityFakeBlock) world.getTileEntity(pos)).addController(machine);
            return;
        }
        world.setBlockState(pos, Data.PROXY_INSTANCE.getDefaultState(), 2 | 8);
        TileEntityFakeBlock tile = (TileEntityFakeBlock) world.getTileEntity(pos);
        tile.setState(oldState).setFacing(machine.getFacing()).setCovers(covers);
        tile.addController(machine);
        super.onBuild(machine, pos, result, count);
    }

    @Override
    public void onRemove(TileEntityBasicMultiMachine machine, BlockPos pos, StructureResult result, int count) {
        World world = machine.getWorld();
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityFakeBlock)) return;
        if (count == 0) {
            BlockState state = ((TileEntityFakeBlock)tile).getState();
            world.setBlockState(pos, state, 1 | 2 | 8);
            return;
        } else {
            ((TileEntityFakeBlock)tile).removeController(machine);
        }
        super.onRemove(machine, pos, result, count);
    }
}
