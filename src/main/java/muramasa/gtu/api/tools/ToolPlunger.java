package muramasa.gtu.api.tools;

import muramasa.gtu.api.materials.Material;
import net.minecraft.item.ItemStack;

public class ToolPlunger extends MaterialTool {

    public ToolPlunger(ToolType type) {
        super(type);
    }

    @Override
    public int getRGB(ItemStack stack, int i) {
        Material mat = getSecondary(stack);
        return i == 0 ? -1 : mat != null ? mat.getRGB() : -1;
    }
}