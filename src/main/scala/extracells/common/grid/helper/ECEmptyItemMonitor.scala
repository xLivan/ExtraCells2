package extracells.common.grid.helper

import appeng.api.AEApi
import appeng.api.config.{AccessRestriction, Actionable}
import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.data.{IAEItemStack, IAEStack, IItemList}
import appeng.api.storage.{IMEMonitor, IMEMonitorHandlerReceiver, StorageChannel}

class ECEmptyItemMonitor extends IMEMonitor[IAEItemStack] {

  override def isPrioritized(input: IAEItemStack) : Boolean = false
  override def canAccept(input: IAEItemStack) : Boolean = false

  override def addListener(hr: IMEMonitorHandlerReceiver[IAEItemStack],
                  verificationToken: AnyRef) : Unit = {}
  override def removeListener(hr: IMEMonitorHandlerReceiver[IAEItemStack]): Unit = {}

  override def injectItems(input: IAEItemStack, mode: Actionable, src: BaseActionSource) = input
  override def extractItems(request: IAEItemStack, mode: Actionable, src: BaseActionSource) = null
  override def getAvailableItems(itemList: IItemList[_ <: IAEStack[_]]): IItemList[_ <: IAEStack[_]] = itemList

  override def getAccess : AccessRestriction = AccessRestriction.NO_ACCESS
  override def getChannel : StorageChannel = StorageChannel.ITEMS
  override def getPriority : Int = 0
  override def getSlot : Int = 0
  override def getStorageList : IItemList[IAEItemStack] = AEApi.instance.storage.createItemList


  override def validForPass(i : Int): Boolean = true

}
