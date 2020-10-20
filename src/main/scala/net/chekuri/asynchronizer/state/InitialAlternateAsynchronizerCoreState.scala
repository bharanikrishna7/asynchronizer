package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AlternateAsynchronizerCore
import net.chekuri.asynchronizer.AsynchronizerConstants.AlternateAsynchronizerExecutionResults
import net.chekuri.asynchronizer.behaviors.GuidBehavior
import net.chekuri.asynchronizer.task.AsynchronizerTask

/** Asynchronizer State to handle processing when Asynchronizer has just been created.
  *
  * @param asynchronizer Asynchronizer
  * @tparam T Data type associated with the result.
  */
class InitialAlternateAsynchronizerCoreState[T](
    asynchronizer: AlternateAsynchronizerCore[T]
) extends InitialAsynchronizerCoreState[T](asynchronizer)
    with GuidBehavior {

  /** Method to validate that asynchronizer has been initialized properly
    * and convert tasks to AsynchronizerTask object.
    */
  override def initialize(): Array[AsynchronizerTask[T]] = {
    // throw exception if task is cancelled.
    if (asynchronizer.is_cancelled.get()) {
      new Throwable("Asynchronizer has been initialized incorrectly.")
    }

    // validate none of the results
    // have been initialized.
    for (index <- asynchronizer.Results.indices) {
      if (asynchronizer.Results(index) != null) {
        new Throwable(
          "It appears that the tasks has already started execution. Asynchronizer should not be in this state once the tasks started producing results."
        )
      }
      // generate task result with .
      asynchronizer.Results(index) = AlternateAsynchronizerExecutionResults[T](
        generateGuid,
        null
      )
    }

    // produce tasks from futures.
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
