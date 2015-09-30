package extracells.api.storage;

import appeng.api.features.INetworkEncodable;
import net.minecraft.item.ItemStack;

public interface IWirelessFluidTermHandler extends IPortablePoweredDevice, INetworkEncodable {

    boolean canHandle(ItemStack is);

    boolean isItemNormalWirelessTermToo(ItemStack is);

}
