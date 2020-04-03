package muramasa.antimatter.pipe.types;

import muramasa.antimatter.material.Material;
import muramasa.antimatter.pipe.BlockFluidPipe;
import muramasa.antimatter.pipe.PipeSize;
import muramasa.antimatter.tile.pipe.TileEntityFluidPipe;
import net.minecraft.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

public class FluidPipe<T extends FluidPipe<T>> extends PipeType<T> {

    protected int maxTemp;
    protected boolean gasProof;
    protected int[] caps;

    public FluidPipe(String domain, Material material, int maxTemp, boolean gasProof) {
        super(domain, material);
        this.maxTemp = maxTemp;
        this.gasProof = gasProof;
        setTile(() -> new TileEntityFluidPipe(this));
    }

    @Override
    public String getId() {
        return "fluid";
    }

    @Override
    public Set<Block> getBlocks() {
        return sizes.stream().map(s -> new BlockFluidPipe(this, s)).collect(Collectors.toSet());
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public boolean isGasProof() {
        return gasProof;
    }

    public int getCapacity(PipeSize size) {
        return caps[size.ordinal()];
    }

    public T caps(int baseCap) {
        this.caps = new int[]{baseCap / 6, baseCap / 6, baseCap / 3, baseCap, baseCap * 2, baseCap * 4};
        return (T) this;
    }

    public T caps(int... caps) {
        this.caps = caps;
        return (T) this;
    }
}
