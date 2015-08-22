package extracells.api.storage;

import appeng.api.storage.ICellWorkbenchItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;

public interface IFluidStorageCell extends ICellWorkbenchItem {

    /**
     * Get list of preformatted fluids.
     *
     * @param ItemStack The cell item.
     * @return List of all preformatted fluids. Empty if its not preformatted.
     */
    List<Fluid> getPreformatted(ItemStack is);

    int getBytesPerType(ItemStack is);

    int getMaxBytes(ItemStack is);

    int getMaxTypes(ItemStack is);

}
