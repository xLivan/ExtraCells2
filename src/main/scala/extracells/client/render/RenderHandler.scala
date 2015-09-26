package extracells.client.render

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess

class RenderHandler(val renderID: Int) extends ISimpleBlockRenderingHandler{
  RenderHandler.renderPass = 0

  override def getRenderId: Int = renderID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int,
                                    modelId: Int, renderer: RenderBlocks): Unit = {}

  //Todo: Renderer for certus tank.
  override def renderWorldBlock(world: IBlockAccess,
                                x: Int, y: Int, z: Int,
                                block: Block, modelId: Int,
                                renderer: RenderBlocks): Boolean = {
    false
  }
}

object RenderHandler {
  var renderPass: Int = 0
}