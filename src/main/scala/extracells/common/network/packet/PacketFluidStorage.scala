package extracells.common.network.packet

import appeng.api.AEApi
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import extracells.client.gui.GuiFluidStorage
import extracells.common.container.implementations.ContainerFluidStorage
import extracells.common.network.AbstractPacketBase
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fluids.{Fluid, FluidStack}

class PacketFluidStorage(player: EntityPlayer) extends AbstractPacketBase(player) {
  private var fluidStackList: IItemList[IAEFluidStack] = _
  private var currentFluid: Fluid = _
  private var hasTermHandler: Boolean = _
  this.mode = 2

  def this(player: EntityPlayer, hasTermHandler: Boolean) {
    this(player)
    this.mode = 3
    this.hasTermHandler = hasTermHandler
  }

  def this(player: EntityPlayer, currentFluid: Fluid) {
    this(player)
    this.mode = 1
    this.currentFluid = currentFluid
  }

  def this(player: EntityPlayer, list: IItemList[IAEFluidStack]) {
    this(player)
    this.mode = 0
    this.fluidStackList = list
  }

  override def execute(): Unit = {
    this.mode match {
      case 0 =>
        if (this.player != null && this.player.isClientWorld) {
          Minecraft.getMinecraft.currentScreen match {
            case storage: GuiFluidStorage => storage
              .inventorySlots.asInstanceOf[ContainerFluidStorage]
              .updateFluidList(this.fluidStackList)
          }
        }
      case 1 =>
        if (this.player != null)
          this.player.openContainer match {
            case storage: ContainerFluidStorage => storage
              .receiveSelectedFluid(this.currentFluid)
          }
      case 2 =>
        if (this.player != null && !this.player.worldObj.isRemote) {
          this.player.openContainer match {
            case container: ContainerFluidStorage =>
              container.forceFluidUpdate()
              container.doWork()
          }
        }
      case 3 =>
        if (this.player != null && this.player.isClientWorld) {
          val gui: Gui = Minecraft.getMinecraft.currentScreen
          gui match {
            case storage: GuiFluidStorage =>
              storage.inventorySlots.asInstanceOf[ContainerFluidStorage]
                .hasWirelessTermHandler = this.hasTermHandler
          }
        }
    }
  }

  override def readData(in: ByteBuf): Unit = {
    this.mode match {
      case 0 =>
        this.fluidStackList = AEApi.instance.storage.createFluidList
        while (in.readableBytes() > 0) {
          val fluid: Fluid = PacketHelper.readFluid(in)
          val fluidAmt: Long = in.readLong()
          if (fluid == null)
            return
          val stack: IAEFluidStack = AEApi.instance.storage.createFluidStack(new FluidStack(fluid, 1))
          stack.setStackSize(fluidAmt)
          this.fluidStackList.add(stack)
        }
      case 1 =>
        this.currentFluid = PacketHelper.readFluid(in)
      case 3 =>
        this.hasTermHandler = in.readBoolean()
    }
  }

  override def writeData(out: ByteBuf): Unit = {
    this.mode match {
      case 0 =>
        for (stack: IAEFluidStack <- this.fluidStackList) {
          PacketHelper.writeFluid(stack.getFluid, out)
          out.writeLong(stack.getStackSize)
        }
      case 1 =>
        PacketHelper.writeFluid(this.currentFluid, out)
      case 3 =>
    }
  }
}
