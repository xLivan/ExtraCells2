package extracells.common.networking;

import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;

public interface IGridProxyable extends IGridHost {
    ECGridProxy getProxy();
    DimensionalCoord getLocation();
    void gridChanged();
}
