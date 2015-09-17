package extracells.common.util

import appeng.api.AEApi
import appeng.api.storage.data.{IItemList, IAEStack}
import scala.collection.immutable
import scala.collection.mutable

object ConversionUtil {
  def AEListToScalaList[T <: IAEStack](AEList: IItemList[T]): immutable.List[T] = {
    val list = mutable.ListBuffer[T]()
    for (stack: T <- AEList.iterator())
      if (stack.ne(null))
        list.append(stack)
    list.toList
  }
}
