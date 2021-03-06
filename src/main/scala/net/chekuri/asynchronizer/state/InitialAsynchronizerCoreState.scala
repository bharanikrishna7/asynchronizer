package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.InitialAsynchronizerCoreState
import net.chekuri.asynchronizer.{AsynchronizerConstants, AsynchronizerCore}
import net.chekuri.asynchronizer.task.AsynchronizerTask

import scala.concurrent.ExecutionContext

/** Asynchronizer State to handle processing when Asynchronizer has just been created.
  * @param asynchronizer Asynchronizer
  * @tparam T Data type associated with the result.
  */
class InitialAsynchronizerCoreState[T](asynchronizer: AsynchronizerCore[T])
    extends AbstractAsynchronizerCoreState[T] {
  override val name: AsynchronizerConstants.AsynchronizerStateValues.Value =
    InitialAsynchronizerCoreState
  implicit val ec: ExecutionContext = asynchronizer.getExecutionContext

  /** Method to validate that asynchronizer has been initialized properly
    * and convert tasks to AsynchronizerTask object.
    */
  override def initialize(): Array[AsynchronizerTask[T]] = {
    if (asynchronizer.is_cancelled.get()) {
      new Throwable("Asynchronizer has been initialized incorrectly.")
    }
    if (asynchronizer.asynchronizer_results != null) {
      new Throwable(
        "It appears that the tasks has already started execution. Asynchronizer should not be in this state once the tasks started producing results."
      )
    }
    val total_task_count: Int = asynchronizer.total_task_count
    val asynchronous_tasks: Array[AsynchronizerTask[T]] =
      new Array[AsynchronizerTask[T]](total_task_count)
    for (index <- asynchronizer.getTasks.indices) {
      asynchronous_tasks(index) =
        new AsynchronizerTask[T](asynchronizer.getTasks(index))
    }
    asynchronous_tasks.reverse
  }
}
