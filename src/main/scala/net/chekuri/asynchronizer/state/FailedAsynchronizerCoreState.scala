package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues
import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.FailedAsynchronizerCoreState
import net.chekuri.asynchronizer.AsynchronizerCore
import net.chekuri.asynchronizer.task.TaskConstants

class FailedAsynchronizerCoreState[T](asynchronizer: AsynchronizerCore[T])
    extends CompletedAsynchronizerCoreState[T](asynchronizer) {
  override val name: AsynchronizerStateValues.Value =
    FailedAsynchronizerCoreState

  override def results(): Array[TaskConstants.TaskExecutionResults[T]] = {
    val fail_message = asynchronizer.asynchronizer_results
      .filter(x => x.taskExecutionState.equals("FailureTaskState"))
      .lastOption
    throw fail_message.get.exception.get
  }
}
