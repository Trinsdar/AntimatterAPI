package muramasa.itech.common.items;

import muramasa.itech.api.machines.Machine;
import muramasa.itech.api.machines.MachineList;
import muramasa.itech.api.machines.Tier;
import muramasa.itech.common.utils.Ref;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockMachines extends ItemBlock {

    public ItemBlockMachines(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(Ref.TAG_MACHINE_STACK_DATA)) {
            NBTTagCompound data = (NBTTagCompound) stack.getTagCompound().getTag(Ref.TAG_MACHINE_STACK_DATA);
            Machine machine = MachineList.get(data.getString(Ref.KEY_MACHINE_STACK_TYPE));
            Tier tier = Tier.get(data.getString(Ref.KEY_MACHINE_STACK_TIER));
            if (machine != null && tier != null) {
                return tier.getRarityColor() + I18n.format("machine." + machine.getName() + "." + tier.getName() + ".name");
            }
        }
        return getUnlocalizedName();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(Ref.TAG_MACHINE_STACK_DATA)) {
            NBTTagCompound data = (NBTTagCompound) stack.getTagCompound().getTag(Ref.TAG_MACHINE_STACK_DATA);
            tooltip.add(data.getString(Ref.KEY_MACHINE_STACK_TYPE));
            tooltip.add(data.getString(Ref.KEY_MACHINE_STACK_TIER));
        }
    }
}