package muramasa.antimatter.machine.types;

import muramasa.antimatter.Data;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.tile.multi.TileEntityHatch;

import static muramasa.antimatter.Data.COVERINPUT;
import static muramasa.antimatter.Data.COVERNONE;
import static muramasa.antimatter.machine.MachineFlag.COVERABLE;
import static muramasa.antimatter.machine.MachineFlag.HATCH;

public class HatchMachine extends Machine<HatchMachine> {

    public HatchMachine(String domain, String id, ICover cover) {
        super(domain, id);
        setTile(() -> new TileEntityHatch<>(this));
        setTiers(Tier.getAllElectric());
        addFlags(HATCH, COVERABLE);
        setGUI(Data.HATCH_MENU_HANDLER);
        setAllowVerticalFacing(true);
        covers(COVERNONE,COVERNONE,cover,COVERNONE,COVERNONE,COVERNONE);
        setOutputCover(cover);
        frontCovers();
    }
}
