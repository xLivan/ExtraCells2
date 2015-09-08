package extracells.common.inventory

trait TInventoryUpdateReceiver {
  /**
   * Called when the inventory is changed.
   */
  def onInventoryChanged(): Unit
}
