package extracells.common.util

import appeng.api.AEApi
import appeng.api.storage.data.{IItemList, IAEStack}
import scala.collection.immutable
import scala.collection.mutable

object ConversionUtil {
  def AEListToScalaList[T <: IAEStack[T]](AEList: IItemList[T]): immutable.List[T] = {
    val list = mutable.ListBuffer[T]()
    val it = AEList.iterator()
    while (it.hasNext)
      list.append(it.next())
    list.filter(_.ne(null)).toList
  }
}
