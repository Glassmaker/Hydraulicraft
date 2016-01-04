package k4unl.minecraft.Hydraulicraft.client.renderers.consumers.harvester;

import k4unl.minecraft.Hydraulicraft.client.models.ModelHarvesterFrame;
import k4unl.minecraft.Hydraulicraft.lib.config.ModInfo;
import k4unl.minecraft.Hydraulicraft.tileEntities.harvester.TileHarvesterFrame;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RendererHarvesterFrame extends TileEntitySpecialRenderer {

    private static final ResourceLocation resLoc =
            new ResourceLocation(ModInfo.LID, "textures/model/harvesterFrame_tmap.png");


    private ModelHarvesterFrame renderModel;

    public RendererHarvesterFrame() {
        renderModel = new ModelHarvesterFrame();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        //Open the GL Matrix
        GL11.glPushMatrix();

        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

        if (tileEntity != null) {
            TileHarvesterFrame frame = (TileHarvesterFrame) tileEntity;
            if (frame.getIsRotated()) {
                GL11.glRotatef(180, 1F, 0F, 1F);
            } else {
                GL11.glRotatef(180, 0F, 0F, 1F);
            }
        } else {
            GL11.glRotatef(180, 0F, 0F, 1F);
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(resLoc);

        GL11.glPushMatrix();

        renderModel.render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

}
