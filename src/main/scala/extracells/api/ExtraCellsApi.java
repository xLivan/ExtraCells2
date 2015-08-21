package extracells.api;

import extracells.api.storage.IWirelessFluidTermHandler;
import extracells.api.storage.filter.IFluidFilter;
import net.minecraftforge.fluids.Fluid;

public interface ExtraCellsApi {

    String getVersion();

    /**
     *  Register a fluid filter.
     * @param filter IFluidFilter implementation
     */
    void registerFluidFilter(IFluidFilter filter);

    void registerFuelBurnTime(Fluid fuel, int burnTime);

}
