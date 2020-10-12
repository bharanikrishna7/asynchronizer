package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.Asynchronizer
import net.chekuri.asynchronizer.task.TaskConstants

class FailedAsynchronizerState[T](asynchronizer: Asynchronizer[T])
    extends CompletedAsynchronizerState[T](asynchronizer) {
  override val name: String = "FailedAsynchronizerState"

  override def results(): Array[TaskConstants.TaskExecutionResults[T]] = {
    val fail_message = asynchronizer.asynchronizer_results
      .filter(x => x.taskExecutionState.equals("FailureTaskState"))
      .lastOption
    throw fail_message.get.exception.get
  }
}
