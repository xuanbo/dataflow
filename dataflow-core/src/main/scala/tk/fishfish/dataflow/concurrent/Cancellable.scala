package tk.fishfish.dataflow.concurrent

import java.util.concurrent.{Callable, FutureTask}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/**
 * Scala Future 支持 Cancel
 *
 * @author 奔波儿灞
 * @see [[https://stackoverflow.com/questions/16009837/how-to-cancel-future-in-scala]]
 * @version 1.0.0
 */
class Cancellable[T](executionContext: ExecutionContext, todo: => T) {

  private val promise = Promise[T]()

  private val javaFuture: FutureTask[T] = new FutureTask[T](
    new Callable[T] {
      override def call(): T = todo
    }
  ) {
    override def done(): Unit = promise.complete(Try(get()))
  }

  def future: Future[T] = promise.future

  def cancel(): Unit = javaFuture.cancel(true)

  executionContext.execute(javaFuture)

}

object Cancellable {

  def apply[T](todo: => T)(implicit executionContext: ExecutionContext): Cancellable[T] =
    new Cancellable[T](executionContext, todo)

}
