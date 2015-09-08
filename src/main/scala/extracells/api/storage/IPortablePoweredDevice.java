package extracells.api.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IPortablePoweredDevice {

    /**
     * Checks if the device has enough power.
     * @param player Player holding it.
     * @param amount Amount of power required.
     * @param stack ItemStack of the item.
     * @return If it has enough power
     */
    boolean hasPower(EntityPlayer player, double amount, ItemStack stack);

    /**
     * Uses power from the device storage.
     * @param player Player holding it.
     * @param amount Amount of power required.
     * @param stack ItemStack of the item
     * @return If it succeeds in using power.
     */
    boolean usePower(EntityPlayer player, double amount, ItemStack stack);

    /**
     * Get the idle power drain of a device.
     * @param stack ItemStack of the device.
     * @return Idle power drain.
     */
    double getIdlePowerDrain(ItemStack stack);

}
