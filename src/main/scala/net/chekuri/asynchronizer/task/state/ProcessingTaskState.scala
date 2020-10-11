package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.behaviors.LoggingBehavior
import net.chekuri.asynchronizer.task.AsynchronizerTask

/** `Asynchronizer Task State` to handle
  * processing when Asynchronizer Task's
  * execution is in processing state.
  *
  * @param task asynchronizer task
  * @tparam T Data Type associated with the result.
  */
class ProcessingTaskState[T](task: AsynchronizerTask[T])
    extends AbstractTaskState[T]
    with LoggingBehavior {
  override val name = "ProcessingTaskState"

  /** Method to interrupt the currently
    * executing async task.
    */
  override def interrupt(): Unit = {
    logger.debug("Setting is_cancelled to true for current task")
    task.is_cancelled.set(true)
    logger.trace("Injecting value into exception manually.")
    task.exception = Some(task.THROW_INTERRUPT)
    logger.debug("Changing state to InterruptedTaskState")
    task.changeState(task.state_interrupted)
  }
}
