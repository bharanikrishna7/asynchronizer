package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.task.AsynchronizerTask

/**
  * `Asynchronizer Task State` to handle
  * processing when Asynchronizer Task's
  * execution has successfully completed.
  *
  * @param task asynchronizer task
  * @tparam T Data Type associated with the result.
  */
class SuccessTaskState[T](task: AsynchronizerTask[T])
    extends AbstractTaskState[T] {
  override val name = "SuccessTaskState"

  /** Method to retrieve results,
    * throws exception if result
    * is missing.
    *
    * @return results of the async task.
    */
  override def results(): T = {
    if (task.is_finished.get()) {
      if (task.result.isEmpty) {
        throw task.exception.get
      } else {
        task.result.get
      }
    } else {
      throw new Exception(
        "Task is not yet completed. Results are not yet ready to be retrieved."
      )
    }
  }
}
