package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AlternateAsynchronizerCore
import net.chekuri.asynchronizer.AsynchronizerConstants.AlternateAsynchronizerExecutionResults
import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

class InitializedAlternateAsynchronizerCoreState[T](
    asynchronizer: AlternateAsynchronizerCore[T]
) extends InitializedAsynchronizerCoreState[T](asynchronizer) {

  override def updateResults(
      index: Int,
      payload: TaskExecutionResults[T]
  ): Unit = {
    val task_id: String = asynchronizer.asynchronizer_tasks(index).getTaskGuid
    asynchronizer.Results(index) =
      AlternateAsynchronizerExecutionResults[T](task_id, payload)
  }

  override protected def shouldUpdateAsyncTask(
      index: Int
  ): Boolean = {
    if (
      asynchronizer.ready
        .get() || (asynchronizer.Results(index).task_result != null)
    ) {
      false
    } else {
      true
    }
  }

  override def getAssociatedThreads: List[String] = {
    asynchronizer.Results.map(x => x.task_id).toList
  }
}
