package extracells.core.client.render.item

import extracells.core.client.gui.GuiUtil
import extracells.core.common.util.FluidUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.{ItemRendererHelper, ItemRenderType}

class ItemRendererFluidPlaceholder extends IItemRenderer{
  override def handleRenderType(stack: ItemStack,
                                rType: ItemRenderType): Boolean = true

  override def shouldUseRenderHelper(rType: ItemRenderType,
                                     stack: ItemStack,
                                     helper: ItemRendererHelper): Boolean = false

  override def renderItem(rType: ItemRenderType,
                          stack: ItemStack, data: AnyRef*): Unit = {
    FluidUtil.getFluidFromPlaceholder(stack)
      .map(_.getIcon)
      .foreach {icon =>
        Minecraft.getMinecraft.renderEngine
          .bindTexture(TextureMap.locationBlocksTexture)
        GuiUtil.drawIcon(icon, 0, 0, 0, 16, 16)
        Minecraft.getMinecraft.renderEngine
          .bindTexture(TextureMap.locationItemsTexture)
      }


  }
}
