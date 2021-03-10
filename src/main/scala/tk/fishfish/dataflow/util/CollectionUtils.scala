package tk.fishfish.dataflow.util

/**
 * 集合工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object CollectionUtils {

  def isEmpty(collection: Seq[_]): Boolean = collection == null || collection.isEmpty

  def isNotEmpty(collection: Seq[_]): Boolean = !isEmpty(collection)

  def isEmpty(collection: java.util.Set[_]): Boolean = collection == null || collection.isEmpty

  def isNotEmpty(collection: java.util.Set[_]): Boolean = !isEmpty(collection)

}
