package extracells.common.registries;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import extracells.api.storage.IPortableFluidStorageCell;
import extracells.api.storage.IWirelessFluidTermHandler;
import extracells.client.gui.GuiFluidStorage;
import extracells.common.container.implementations.ContainerFluidStorage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.StatCollector;

@SuppressWarnings("unchecked")
public enum GuiEnum {

    FluidTerm(null, "extracells.part.fluid.terminal.name") {
        public GuiContainer getGui(EntityPlayer player, String name, Object[] args) {
            return new GuiFluidStorage(player, name);
        }
        public Container getContainer(EntityPlayer player, Object[] args) {
            IMEMonitor<IAEFluidStack> monitor;
            monitor = (IMEMonitor<IAEFluidStack>) args[0];
            return new ContainerFluidStorage(monitor, player);
        }
    },
    PortableCell(FluidTerm, "extracells.item.storage.fluid.portable.name"){
        public Container getContainer(EntityPlayer player, Object[] args) {
            IMEMonitor<IAEFluidStack> monitor =
                    (IMEMonitor<IAEFluidStack>) args[0];
            IPortableFluidStorageCell cell =
                    (IPortableFluidStorageCell) args[1];
            return new ContainerFluidStorage(monitor, player, cell);
        }
    },
    WirelessTerm(FluidTerm, FluidTerm.unlocalizedName) {
        public Container getContainer(EntityPlayer player, Object[] args) {
            IMEMonitor<IAEFluidStack> monitor =
                    (IMEMonitor<IAEFluidStack>) args[0];
            IWirelessFluidTermHandler term =
                    (IWirelessFluidTermHandler) args[1];
            return new ContainerFluidStorage(monitor, player, term);
        }
    };

    private String unlocalizedName;
    private GuiEnum parent;

    GuiEnum(GuiEnum parent, String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
        this.parent = parent;
    }

    public String getUnlocalizedName() {
        return this.unlocalizedName;
    }

    public int getID() {
        return this.ordinal();
    }

    public String getLocalizedName() {
        return StatCollector.translateToLocal(this.unlocalizedName);
    }

    public GuiContainer getGui(EntityPlayer player, Object[] args) {
        if (parent != null)
            return parent.getGui(player, this.unlocalizedName, args);
        else
            return this.getGui(player, this.unlocalizedName, args);
    };

    public GuiContainer getGui(EntityPlayer player, String name, Object[] args) {
        if (parent != null)
            return parent.getGui(player, name,args);
        return null;
    }

    public Container getContainer(EntityPlayer player, Object[] args) {
        if (parent != null)
            return parent.getContainer(player, args);
        return null;
    }
}
