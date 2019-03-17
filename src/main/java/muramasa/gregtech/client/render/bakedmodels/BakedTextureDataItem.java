package muramasa.gregtech.client.render.bakedmodels;

import muramasa.gregtech.api.texture.TextureData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;

public class BakedTextureDataItem extends BakedBase {

    protected IBakedModel baked;
    protected TextureData data;

    public BakedTextureDataItem(IBakedModel baked, TextureData data) {
        this.baked = baked;
        this.data = data;
    }

    @Override
    public List<BakedQuad> getBakedQuads(@Nullable IExtendedBlockState exState, @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return data.apply(baked);
    }
}