package muramasa.antimatter.structure;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import muramasa.antimatter.machine.BlockMachine;
import muramasa.antimatter.tile.multi.TileEntityBasicMultiMachine;
import muramasa.antimatter.util.int2;
import muramasa.antimatter.util.int3;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static muramasa.antimatter.util.Dir.*;

public class Structure {

    private ImmutableMap<int3, StructureElement> elements;
    private Object2ObjectMap<String, IRequirement> requirements = new Object2ObjectOpenHashMap<>();
    private int3 size;
    private int2 offset = new int2();

    public Structure(int3 size, ImmutableMap<int3, StructureElement> elements) {
        this.size = size;
        this.elements = elements;
    }

    public Structure offset(int x, int y) {
        offset.set(x, y);
        return this;
    }

    public Structure exact(int i, Object... objects) {
        Arrays.stream(StructureBuilder.getAntiObjects(objects)).forEach(o -> addReq(o.getId(), (c, s) -> (c.containsKey(o.getId()) && c.get(o.getId()).size() == i) || (s.containsKey(o.getId()) && s.get(o.getId()).size() == i)));
        return this;
    }

    public Structure min(int i, Object... objects) {
        Arrays.stream(StructureBuilder.getAntiObjects(objects)).forEach(o -> addReq(o.getId(), (c, s) -> (c.containsKey(o.getId()) && c.get(o.getId()).size() >= i) || (s.containsKey(o.getId()) && s.get(o.getId()).size() >= i)));
        return this;
    }

    public Structure addReq(String id, IRequirement req) {
        requirements.put(id, req);
        return this;
    }

    public Map<int3, StructureElement> getElements() {
        return elements;
    }

    public Map<String, IRequirement> getRequirements() {
        return requirements;
    }

    public StructureResult evaluate(@Nonnull TileEntityBasicMultiMachine<?> tile) {
        StructureResult result = new StructureResult(this);
        Direction h = null;
        if (tile.getMachineType().allowVerticalFacing() && tile.getFacing().getAxis() == Axis.Y) {
            h = tile.getBlockState().get(BlockMachine.HORIZONTAL_FACING);
        }
        for (Iterator<Point> it = forAllElements(tile.getPos(), tile.getFacing(), h); it.hasNext(); ) {
            Point point = it.next();
            if (!point.el.evaluate(tile, point.pos, result)) {
                return result;
            } else {
                result.register(point.pos.toImmutable(), point.el);
            }
        }
        return result;
    }

    public boolean evaluatePosition(@Nonnull StructureResult res, @Nonnull TileEntityBasicMultiMachine<?> tile, @Nonnull BlockPos pos) {
        StructureElement el = res.get(pos);
        if (el != null) {
            return el.evaluate(tile, new int3(pos.getX(), pos.getY(), pos.getZ()), res);
        }
        return true;
    }

    public static class Point {
        public int3 pos;
        public int3 offset;
        public StructureElement el;
    }


    public Iterator<Point> forAllElements(@Nonnull BlockPos source, @Nonnull Direction facing, @Nullable Direction hFacing) {
        return new Iterator<Point>() {
            final int3 corner = hFacing == null ? new int3(source, facing).left(size.getX() / 2).back(offset.x).up(offset.y) : new int3(source, facing, hFacing).left(size.getX() / 2).back(offset.x).up(offset.y);
            final int3 working = new int3(facing, hFacing);
            final Point point = new Point();
            final Iterator<Map.Entry<int3, StructureElement>> it = elements.entrySet().iterator();
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Point next() {
                Map.Entry<int3, StructureElement> next = it.next();
                working.set(corner).offset(next.getKey(), RIGHT, UP, FORWARD);
                point.el = next.getValue();
                point.offset = next.getKey();
                point.pos = working;
                return point;
            }
        };
    }

    public LongList getStructure(TileEntityBasicMultiMachine<?> tile) {
        LongList l = new LongArrayList();
        Direction h = null;
        if (tile.getMachineType().allowVerticalFacing() && tile.getFacing().getAxis() == Axis.Y){
            h = tile.getBlockState().get(BlockMachine.HORIZONTAL_FACING);
        }
        for (Iterator<Point> it = forAllElements(tile.getPos(), tile.getFacing(), h); it.hasNext(); ) {
            l.add(it.next().pos.toLong());
        }
        return l;
    }

    public int3 size() {
        return size;
    }

    public int2 getOffset() {
        return offset;
    }
}
