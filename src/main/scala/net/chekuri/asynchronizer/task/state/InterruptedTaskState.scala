package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.task.AsynchronizerTask

/**
  * `Asynchronizer Task State` to handle
  * processing when Asynchronizer Task's
  * execution has been interrupted.
  *
  * @param task asynchronizer task
  * @tparam T Data Type associated with the result.
  */
class InterruptedTaskState[T](task: AsynchronizerTask[T])
    extends FailureTaskState[T](task) {
  override val name = "InterruptedTaskState"

  /** Method to retrieve results,
    * throws exception if result
    * is missing.
    *
    * @return results of the async task.
    */
  override def exception(): Throwable = {
    if (checkIfTaskIsCancelled()) {
      if (task.exception.isDefined) {
        task.exception.get
      } else {
        throw new Exception(
          "Invalid branch. Task should have an exception associated with it."
        )
      }
    } else {
      throw new Exception(
        "Invalid branch. Further inspection of task revealed that the task was not cancelled."
      )
    }
  }

  /** Method to check if task is cancelled.
    *
    * @return true if task was cancelled, false if otherwise.
    */
  def checkIfTaskIsCancelled(): Boolean = task.is_cancelled.get()
}
