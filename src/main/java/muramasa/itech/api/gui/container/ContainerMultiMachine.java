package muramasa.itech.api.gui.container;

import muramasa.itech.common.tileentities.base.TileEntityMachine;
import net.minecraft.inventory.IInventory;

public class ContainerMultiMachine extends ContainerMachine {

    public ContainerMultiMachine(TileEntityMachine tile, IInventory playerInv) {
        super(tile, playerInv);
    }
}