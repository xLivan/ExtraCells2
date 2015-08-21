package extracells.common.inventory

trait TInevntoryUpdateReceiver {
  def onInventoryChanged(): Unit
}
