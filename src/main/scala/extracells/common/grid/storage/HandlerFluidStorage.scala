package extracells.common.grid.storage

import java.util.{ArrayList => JavaArrayList, List => JavaList}

import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.data.IAEFluidStack
import extracells.api.IHandlerFluidStorage
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidStack

class HandlerFluidStorage extends IMEInventoryHandler[IAEFluidStack] with IHandlerFluidStorage{
  private var stackTag: NBTTagCompound = _
  protected var fluidStacks: JavaArrayList[FluidStack] = new JavaArrayList[FluidStack]
  private var prioritizedFluids: JavaArrayList[FluidStack] = new JavaArrayList[FluidStack]
  private var totalTypes: Int = _
  private var totalBytes: Int = _
  private var containers: JavaList[ContainerFluidStorage]
}
