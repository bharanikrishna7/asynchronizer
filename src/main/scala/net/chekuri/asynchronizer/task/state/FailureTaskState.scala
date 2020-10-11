package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.task.AsynchronizerTask

/**
  * `Asynchronizer Task State` to handle
  * processing when Asynchronizer Task's
  * execution failed.
  * @param task asynchronizer task
  * @tparam T Data Type associated with the result.
  */
class FailureTaskState[T](task: AsynchronizerTask[T])
    extends AbstractTaskState[T] {
  override val name = "FailureTaskState"

  /** Method to retrieve thrown
    * exception.
    *
    * @return exception generated while executing the async task.
    */
  override def exception(): Throwable = {
    if (task.exception.isDefined) {
      throw task.exception.get
    } else {
      throw new Exception(
        "No exception was throw when executing associated task."
      )
    }
  }

  /** Method to retrieve results.
    * Since there are no results in this
    * state it'll throw the exception message.
    *
    * @return
    */
  override def results(): T = {
    throw this.exception()
  }
}
