package extracells.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidInterface {

	public Fluid getFilter(EnumFacing side);

	public IFluidTank getFluidTank(EnumFacing side);

	public IInventory getPatternInventory();

	public void setFilter(EnumFacing side, Fluid fluid);

	public void setFluidTank(EnumFacing side, FluidStack fluid);

}
