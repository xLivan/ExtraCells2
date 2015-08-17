package extracells.common.networking

import java.util

import appeng.api.networking.{GridFlags, IGrid, IGridBlock}
import appeng.api.util.AEColor
import extracells.common.part.PartECBase
import net.minecraftforge.common.util.ForgeDirection

class ECBaseGridBlock(protected var host: PartECBase) extends IGridBlock {
  protected var color: AEColor
  protected var grid: IGrid
  protected var usedChannels: Int

  override def getFlags: util.EnumSet[GridFlags] = util.EnumSet.of(GridFlags.REQUIRE_CHANNEL)


}
