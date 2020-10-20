package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AlternateAsynchronizerCore
import net.chekuri.asynchronizer.task.TaskConstants

class FailedAlternateAsynchronizerCoreState[T](
    asynchronizer: AlternateAsynchronizerCore[T]
) extends FailedAsynchronizerCoreState[T](asynchronizer) {

  override def results(): Array[TaskConstants.TaskExecutionResults[T]] = {
    var fail_message: Option[TaskConstants.TaskExecutionResults[T]] = None
    for (result <- asynchronizer.Results) {
      if (
        result.task_result.taskExecutionState.equalsIgnoreCase(
          "FailureTaskState"
        )
      ) {
        fail_message = Some(result.task_result)
      }
    }
    throw fail_message.get.exception.get
  }
}
