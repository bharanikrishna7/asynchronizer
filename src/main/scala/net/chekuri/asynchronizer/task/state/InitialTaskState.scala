package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.task.AsynchronizerTask

class InitialTaskState[T](task: AsynchronizerTask[T])
    extends AbstractTaskState[T] {
  override val name = "InitialTaskState"

  /** Method to validate that the task has been initialized appropriately.
    */
  override def initialize(): Unit = {
    if (task.is_cancelled.get()) {
      new Throwable("Task has been initialized incorrectly.")
    }
    if (task.result.isDefined || task.exception.isDefined) {
      new Throwable(
        "It appears that the task has already been executed. There is no value in re-initializing the task."
      )
    }
  }
}
