package extracells.api.storage.filter;

import net.minecraftforge.fluids.Fluid;

import java.util.EnumSet;

/**
 * Interface for filtering fluids.
 * Used to blacklist fluids from EC grid functions.
 */
public interface IFluidFilter {
    /**
     * @param filterTypes A list of modes to filter for, see {@link FilterType}
     * @param fluid Fluid to check
     * @return True if allowed, false if not.
     */
    boolean isAllowed(FilterType filterType, Fluid fluid);
}
