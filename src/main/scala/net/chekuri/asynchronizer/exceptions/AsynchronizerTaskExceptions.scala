package net.chekuri.asynchronizer.exceptions

import net.chekuri.asynchronizer.behaviors.LoggingBehavior

object AsynchronizerTaskExceptions extends LoggingBehavior {
  val AsynchronizerTaskInterruptedExceptionMessage: String =
    "Task was interrupted"

  class AsynchronizerTaskInterruptedException(thread_id: Long)
      extends InterruptedException(
        s"$AsynchronizerTaskInterruptedExceptionMessage. Associated Thread ID : $thread_id"
      )
}
