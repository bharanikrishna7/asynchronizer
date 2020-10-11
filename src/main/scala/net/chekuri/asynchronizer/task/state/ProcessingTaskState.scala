package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.behaviors.LoggingBehavior
import net.chekuri.asynchronizer.task.AsynchronizerTask

class ProcessingTaskState[T](task: AsynchronizerTask[T])
    extends AbstractTaskState[T]
    with LoggingBehavior {
  override val name = "InitializedTaskState"

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
