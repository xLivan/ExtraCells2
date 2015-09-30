package extracells.api;

import extracells.api.storage.filter.FilterType;
import extracells.api.storage.filter.IFluidFilter;
import net.minecraftforge.fluids.Fluid;

public interface IECApi {

    String getVersion();

    /**
     *  Register a fluid filter.
     *
     * @param filter IFluidFilter implementation
     */
    void registerFluidFilter(IFluidFilter filter);

    void registerFuelBurnTime(Fluid fuel, int burnTime);

    /**
     * Check if fluid is allowed by registered filters
     *
     * @param filterType Type of filters to cehck
     * @param fluid fluid to check
     */
    boolean isFluidAllowed(FilterType filterType, Fluid fluid);

}
