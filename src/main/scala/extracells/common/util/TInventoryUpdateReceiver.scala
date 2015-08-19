package extracells.common.util

trait TInventoryUpdateReceiver {
  def onInventoryChanged() : Unit
}
