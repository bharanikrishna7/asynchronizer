package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues
import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.CompletedAsynchronizerCoreState
import net.chekuri.asynchronizer.AsynchronizerCore
import net.chekuri.asynchronizer.task.TaskConstants

class CompletedAsynchronizerCoreState[T](asynchronizer: AsynchronizerCore[T])
    extends AbstractAsynchronizerCoreState[T] {
  override val name: AsynchronizerStateValues.Value =
    CompletedAsynchronizerCoreState

  override def results(): Array[TaskConstants.TaskExecutionResults[T]] = {
    asynchronizer.asynchronizer_results
  }

  override def success(): Array[TaskConstants.TaskExecutionResults[T]] = {
    asynchronizer.asynchronizer_results.filter(x =>
      x.taskExecutionState.equals("SuccessTaskState")
    )
  }

  override def failures(): Array[TaskConstants.TaskExecutionResults[T]] = {
    asynchronizer.asynchronizer_results.filter(x =>
      !x.taskExecutionState.equals("FailureTaskState")
    )
  }
}
