package muramasa.antimatter.gui;

import muramasa.antimatter.capability.AntimatterCaps;
import muramasa.antimatter.capability.CoverHandler;
import muramasa.antimatter.capability.ICoverHandler;
import muramasa.antimatter.gui.container.ContainerCover;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.tile.pipe.TileEntityPipe;
import muramasa.antimatter.util.Utils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

public abstract class MenuHandlerCover<T extends ContainerCover>  extends MenuHandler<T> {

    public MenuHandlerCover(String domain, String id) {
        super(domain, id);
    }

    @Override
    public T onContainerCreate(int windowId, PlayerInventory inv, PacketBuffer data) {
        TileEntity tile = Utils.getTileFromBuf(data);
        if (tile != null) {
            Direction dir = Direction.byIndex(data.readInt());
            LazyOptional<ICoverHandler> coverHandler = tile.getCapability(AntimatterCaps.COVERABLE_HANDLER_CAPABILITY, dir);
            return getMenu(coverHandler.map(ch -> ch.get(dir)).orElse(null), inv, windowId);
        }
        return null;
    }
}
