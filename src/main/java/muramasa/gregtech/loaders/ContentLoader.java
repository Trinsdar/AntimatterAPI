package muramasa.gregtech.loaders;

import muramasa.gregtech.GregTech;
import muramasa.gregtech.Ref;
import muramasa.gregtech.api.data.Machines;
import muramasa.gregtech.api.data.Materials;
import muramasa.gregtech.api.enums.*;
import muramasa.gregtech.api.interfaces.GregTechRegistrar;
import muramasa.gregtech.api.items.ItemFluidCell;
import muramasa.gregtech.api.items.MaterialItem;
import muramasa.gregtech.api.items.StandardItem;
import muramasa.gregtech.api.machines.MachineFlag;
import muramasa.gregtech.api.machines.types.Machine;
import muramasa.gregtech.api.materials.ItemFlag;
import muramasa.gregtech.api.materials.Material;
import muramasa.gregtech.api.materials.Prefix;
import muramasa.gregtech.api.pipe.types.Cable;
import muramasa.gregtech.api.pipe.types.FluidPipe;
import muramasa.gregtech.common.blocks.*;
import muramasa.gregtech.common.blocks.pipe.BlockCable;
import muramasa.gregtech.common.blocks.pipe.BlockFluidPipe;
import muramasa.gregtech.common.items.ItemBlockGT;
import muramasa.gregtech.common.tileentities.base.TileEntityMachine;
import muramasa.gregtech.common.tileentities.base.multi.TileEntityCasing;
import muramasa.gregtech.common.tileentities.base.multi.TileEntityCoil;
import muramasa.gregtech.common.tileentities.base.multi.TileEntityHatch;
import muramasa.gregtech.common.tileentities.base.multi.TileEntityMultiMachine;
import muramasa.gregtech.common.tileentities.overrides.*;
import muramasa.gregtech.common.tileentities.pipe.TileEntityCable;
import muramasa.gregtech.common.tileentities.pipe.TileEntityFluidPipe;
import muramasa.gregtech.common.tileentities.pipe.TileEntityPipe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber
public class ContentLoader {

    public static void init() {
        //Items
        for (Prefix prefix : Prefix.getAll()) {
            for (Material material : Materials.getAll()) {
                if (!prefix.allowGeneration(material)) continue;
                GregTechRegistry.register(new MaterialItem(prefix, material));
            }
        }
        for (ItemType type : ItemType.getAll()) {
            if (!type.isEnabled()) continue;
            GregTechRegistry.register(new StandardItem(type));
        }
        for (ToolType type : ToolType.values()) {
            GregTechRegistry.register(type.getInstance());
        }
        GregTechRegistry.register(new ItemFluidCell());

        //Blocks
        for (Machine type : Machines.getAll()) {
            GregTechRegistry.register(type.getBlock());
        }
        for (Cable type : Cable.getAll()) {
            GregTechRegistry.register(new BlockCable(type));
        }
        for (FluidPipe type : FluidPipe.getAll()) {
            GregTechRegistry.register(new BlockFluidPipe(type));
        }
        for (Casing type : Casing.getAll()) {
            GregTechRegistry.register(new BlockCasing(type));
        }
        for (Coil type : Coil.getAll()) {
            GregTechRegistry.register(new BlockCoil(type));
        }
        for (Material m : ItemFlag.ORE.getMats()) {
            for (StoneType type : StoneType.getAll()) {
                GregTechRegistry.register(new BlockOre(type, m));
            }
        }
        for (Material m : ItemFlag.BLOCK.getMats()) {
            GregTechRegistry.register(new BlockStorage(m));
        }
        for (StoneType type : StoneType.getGenerating()) {
            GregTechRegistry.register(new BlockStone(type));
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        for (Block block : GregTechRegistry.getRegisteredBlocks()) {
            e.getRegistry().register(block);
        }

        //TODO auto register all type tiles???
        GameRegistry.registerTileEntity(TileEntityMachine.class, new ResourceLocation(Ref.MODID, "tile_machine"));
        GameRegistry.registerTileEntity(TileEntityBasicMachine.class, new ResourceLocation(Ref.MODID, "tile_basic_machine"));
        GameRegistry.registerTileEntity(TileEntityItemMachine.class, new ResourceLocation(Ref.MODID, "tile_item_machine"));
        GameRegistry.registerTileEntity(TileEntityFluidMachine.class, new ResourceLocation(Ref.MODID, "tile_fluid_machine"));
        GameRegistry.registerTileEntity(TileEntityItemFluidMachine.class, new ResourceLocation(Ref.MODID, "tile_item_fluid_machine"));
        GameRegistry.registerTileEntity(TileEntitySteamMachine.class, new ResourceLocation(Ref.MODID, "tile_steam_machine"));
        GameRegistry.registerTileEntity(TileEntityMultiMachine.class, new ResourceLocation(Ref.MODID, "tile_multi_machine"));
        GameRegistry.registerTileEntity(TileEntityHatch.class, new ResourceLocation(Ref.MODID, "tile_hatch"));
        List<String> registeredTiles = new LinkedList<>();
        for (Machine type : Machines.getAll()) {
//            event.getRegistry().register(type.getBlock());
            if (type.hasFlag(MachineFlag.MULTI) && !registeredTiles.contains(type.getTileClass().getName())) {
                GameRegistry.registerTileEntity(type.getTileClass(), new ResourceLocation(Ref.MODID, "tile_" + type.getName()));
                registeredTiles.add(type.getTileClass().getName());
            }
        }

        GameRegistry.registerTileEntity(TileEntityPipe.class, new ResourceLocation(Ref.MODID, "block_pipe"));
        GameRegistry.registerTileEntity(TileEntityFluidPipe.class, new ResourceLocation(Ref.MODID, "block_fluid_pipe"));
        GameRegistry.registerTileEntity(TileEntityCable.class, new ResourceLocation(Ref.MODID, "block_cable"));
        GameRegistry.registerTileEntity(TileEntityCasing.class, new ResourceLocation(Ref.MODID, "block_casing"));
        GameRegistry.registerTileEntity(TileEntityCoil.class, new ResourceLocation(Ref.MODID, "block_coil"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        for (Item item : GregTechRegistry.getRegisteredItems()) {
            e.getRegistry().register(item);
        }
        for (Block block : GregTechRegistry.getRegisteredBlocks()) {
            e.getRegistry().register(new ItemBlockGT(block));
        }

        GregTech.INTERNAL_REGISTRAR.onCoverRegistration();
        for (GregTechRegistrar registrar : GregTechRegistry.getRegistrars()) {
            registrar.onCoverRegistration();
        }
    }
}
