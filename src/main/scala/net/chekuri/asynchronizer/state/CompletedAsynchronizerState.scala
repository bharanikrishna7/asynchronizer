package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.Asynchronizer
import net.chekuri.asynchronizer.task.TaskConstants

class CompletedAsynchronizerState[T](asynchronizer: Asynchronizer[T])
    extends AbstractAsynchronizerState[T] {
  override val name: String = "CompletedAsynchronizerState"

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
