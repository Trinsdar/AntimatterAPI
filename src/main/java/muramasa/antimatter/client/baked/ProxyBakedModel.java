package muramasa.antimatter.client.baked;

import com.google.common.collect.Sets;
import muramasa.antimatter.AntimatterProperties;
import muramasa.antimatter.Data;
import muramasa.antimatter.client.dynamic.DynamicTexturer;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.texture.Texture;
import muramasa.antimatter.tile.TileEntityFakeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ProxyBakedModel extends AntimatterBakedModel<ProxyBakedModel> {

    public ProxyBakedModel() {
        super();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        if (tileData instanceof EmptyModelData) {
            tileData = new ModelDataMap.Builder().build();
        }
        TileEntityFakeBlock fake;
        if (tileData.hasProperty(AntimatterProperties.TILE_PROPERTY)) {
            fake = (TileEntityFakeBlock) tileData.getData(AntimatterProperties.TILE_PROPERTY);
        } else {
            fake = (TileEntityFakeBlock) world.getTileEntity(pos);
        }
        if (fake.getState() == null) {
            return tileData;
        }
        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(fake.getState());
        IUnbakedModel m = ModelLoader.instance().getUnbakedModel(BlockModelShapes.getModelLocation(fake.getState()));

        Collection<RenderMaterial> mats = m.getTextures(ModelLoader.defaultModelGetter(), Sets.newLinkedHashSet());
        RenderMaterial first = mats.iterator().next();
        tileData.setData(AntimatterProperties.TEXTURE_MODEL_PROPERTY, new Texture(first.getTextureLocation().toString()));
        tileData = model.getModelData(world, pos, state, tileData);

        if (!tileData.hasProperty(AntimatterProperties.STATE_MODEL_PROPERTY))
            tileData.setData(AntimatterProperties.STATE_MODEL_PROPERTY, fake.getState());
        if (!tileData.hasProperty(AntimatterProperties.TILE_PROPERTY))
            tileData.setData(AntimatterProperties.TILE_PROPERTY, fake);
        return tileData;
    }


    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(data.getData(AntimatterProperties.STATE_MODEL_PROPERTY));
        return model != null ? model.getParticleTexture(data) : getParticleTexture();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        BlockState realState = data.getData(AntimatterProperties.STATE_MODEL_PROPERTY);
        if (realState == null) return Collections.emptyList();
        TileEntityFakeBlock fake = (TileEntityFakeBlock) data.getData(AntimatterProperties.TILE_PROPERTY);
        if (side == null) return Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(realState).getQuads(realState, side, rand, data);
        ICover cover = fake.getCover(side);
        if (cover == null) return Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(realState).getQuads(realState, side, rand, data);
        DynamicTexturer<ICover, ICover.DynamicKey> texturer = fake.getTexturer(side);
        return texturer.getQuads(new LinkedList<>(), realState, cover, new ICover.DynamicKey(fake.facing, data.getData(AntimatterProperties.TEXTURE_MODEL_PROPERTY), Data.COVEROUTPUT.getId()),side.getIndex(), data);
    }
}
