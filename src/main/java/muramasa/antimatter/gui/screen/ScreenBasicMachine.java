package muramasa.antimatter.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import muramasa.antimatter.Antimatter;
import muramasa.antimatter.gui.ButtonOverlay;
import muramasa.antimatter.gui.GuiData;
import muramasa.antimatter.gui.container.ContainerBasicMachine;
import muramasa.antimatter.gui.event.GuiEvent;
import muramasa.antimatter.gui.widget.SwitchWidget;
import muramasa.antimatter.machine.MachineFlag;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.network.packets.TileGuiEventPacket;
import muramasa.antimatter.tile.TileEntityMachine;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static muramasa.antimatter.Data.COVEROUTPUT;

public class ScreenBasicMachine<T extends TileEntityMachine<T>, U extends ContainerBasicMachine<T>> extends ScreenMachine<T,U> {

    private final static ButtonOverlay FLUID = new ButtonOverlay("fluid_eject", 177, 19, 16, 16);
    private final static ButtonOverlay ITEM = new ButtonOverlay("item_eject", 177, 37, 16, 16);
    private Widget item, fluid;

    public ScreenBasicMachine(U container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(stack, mouseX, mouseY);
        if (container.getTile().has(MachineFlag.RECIPE))
            drawTooltipInArea(stack, container.getTile().getMachineState().getDisplayName(), mouseX, mouseY, (xSize / 2) - 5, 45, 10, 8);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(stack, partialTicks, mouseX, mouseY);
        drawProgress(stack, partialTicks, mouseX, mouseY);
        //Draw error.
        if (container.getTile().has(MachineFlag.RECIPE)) {
            if (container.getTile().getMachineState() == MachineState.POWER_LOSS) {
                drawTexture(stack, gui, guiLeft + container.getTile().getMachineType().getGui().getState().x, guiTop + container.getTile().getMachineType().getGui().getState().y, container.getTile().getMachineType().getGui().getStateLocation().x, container.getTile().getMachineType().getGui().getStateLocation().y, container.getTile().getMachineType().getGui().getState().z, container.getTile().getMachineType().getGui().getState().w);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        GuiData data = container.getTile().getMachineType().getGui();
        ResourceLocation loc = data.getButtonLocation();
        boolean shouldDrawIO = container.getTile().getMachineTier().getVoltage() > 0 && data.hasIOButton();
        if (shouldDrawIO) {
            if (container.getTile().has(MachineFlag.ITEM)) {
                item = new SwitchWidget(gui, guiLeft + data.getItem().x, guiTop + data.getItem().y, data.getItem().z, data.getItem().w, data.getItemLocation(), (b, s) -> {
                    Antimatter.NETWORK.sendToServer(new TileGuiEventPacket(GuiEvent.ITEM_EJECT, container.getTile().getPos(), s ? 1 : 0));
                }, container.getTile().coverHandler.map(t -> COVEROUTPUT.shouldOutputItems(t.get(t.getOutputFacing()))).orElse(false));
            }
            if (container.getTile().has(MachineFlag.FLUID)) {
                fluid = new SwitchWidget(gui, guiLeft + data.getFluid().x, guiTop + data.getFluid().y, data.getFluid().z, data.getFluid().w, data.getFluidLocation(), (b, s) -> {
                    Antimatter.NETWORK.sendToServer(new TileGuiEventPacket(GuiEvent.FLUID_EJECT, container.getTile().getPos(), s ? 1 : 0));
                },container.getTile().coverHandler.map(t -> COVEROUTPUT.shouldOutputFluids(t.get(t.getOutputFacing()))).orElse(false));
            }
            if (item != null || fluid != null) {
                addButton(new SwitchWidget(loc, guiLeft + data.getIo().x, guiTop + data.getIo().y, data.getIo().z, data.getIo().w, ButtonOverlay.INPUT_OUTPUT , (b, s) -> {
                    if (s) {
                        if (item != null) addButton(item);
                        if (fluid != null) addButton(fluid);
                    } else {
                        if (item != null) removeButton(item);
                        if (fluid != null) removeButton(fluid);
                    }
                }, false));
            }
        }
    }
}
