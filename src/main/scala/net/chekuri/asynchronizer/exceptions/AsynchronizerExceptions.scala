package net.chekuri.asynchronizer.exceptions

import net.chekuri.asynchronizer.behaviors.LoggingBehavior

object AsynchronizerExceptions extends LoggingBehavior {
  val AsynchronizerTaskInterruptedExceptionMessage: String =
    "Task was interrupted"

  class AsynchronizerTaskInterruptedException(thread_id: Long)
      extends InterruptedException(
        s"$AsynchronizerTaskInterruptedExceptionMessage. Associated Thread ID : $thread_id"
      )

  val AsynchronizerResultsNotReadyExceptionMessage: String =
    "Tasks are still executing. Cannot retrieve tasks before they have finished execution."

  class AsynchronizerResultsNotReadyException(expected: Int, actual: Int)
      extends NullPointerException(
        s"$AsynchronizerResultsNotReadyExceptionMessage. Current task execution progress : $actual | $expected"
      )
}
