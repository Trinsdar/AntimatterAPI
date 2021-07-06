package muramasa.antimatter.gui.container;

import muramasa.antimatter.gui.MenuHandlerMachine;
import muramasa.antimatter.tile.TileEntityMachine;
import net.minecraft.entity.player.PlayerInventory;

public class ContainerBasicMachine<T extends TileEntityMachine<T>> extends ContainerMachine<T> {

    private int lastProgress = -1;

    public ContainerBasicMachine(T tile, PlayerInventory playerInv, MenuHandlerMachine handler, int windowId) {
        super(tile, playerInv, handler, windowId);
    }

//    @Override
//    public void detectAndSendChanges() {
//        super.detectAndSendChanges();
//        int curProgress = tile.getCurProgress();
//        if (Math.abs(curProgress - lastProgress) >= GuiEvent.PROGRESS.getUpdateThreshold()) {
//            int progress = (int) (((float) curProgress / (float) tile.getMaxProgress()) * Short.MAX_VALUE);
//            listeners.forEach(l -> l.sendWindowProperty(this, GuiEvent.PROGRESS.ordinal(), progress));
//            lastProgress = curProgress;
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public void updateProgressBar(int id, int data) {
//        super.updateProgressBar(id, data);
//        if (id == GuiEvent.PROGRESS.ordinal()) {
//            ((TileEntityRecipeMachine) tile).setClientProgress((float)data / (float)Short.MAX_VALUE);
//        }
//    }
}
