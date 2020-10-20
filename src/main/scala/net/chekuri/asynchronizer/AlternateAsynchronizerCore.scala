package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool

import net.chekuri.asynchronizer.AsynchronizerConstants.{
  AlternateAsynchronizerExecutionReport,
  AlternateAsynchronizerExecutionResults
}
import net.chekuri.asynchronizer.behaviors.GuidBehavior
import net.chekuri.asynchronizer.state.{
  AbstractAsynchronizerCoreState,
  FailedAlternateAsynchronizerCoreState,
  InitialAlternateAsynchronizerCoreState,
  InitializedAlternateAsynchronizerCoreState
}
import net.chekuri.asynchronizer.task.{AsynchronizerTask, TaskConstants}

import scala.concurrent.{ExecutionContext, Future}

class AlternateAsynchronizerCore[T](
    tasks: List[Future[T]],
    allowFailures: Boolean = false,
    parallelism: Int = Runtime.getRuntime.availableProcessors()
) extends AsynchronizerCore[T](
      tasks,
      allowFailures,
      ExecutionContext.fromExecutor(new ForkJoinPool(parallelism))
    )
    with GuidBehavior {

  /** Method to fetch current state name.
    * @return current state name.
    */
  def getState: AsynchronizerConstants.AsynchronizerStateValues.Value =
    this.state_current.name

  /** Method to check if asynchronizer has finished
    * executing results.
    * @return false if there are pending tasks to execute,
    *         true otherwise.
    */
  def isReady: Boolean = this.ready.get()

  val Results: Array[AlternateAsynchronizerExecutionResults[T]] =
    new Array[AlternateAsynchronizerExecutionResults[T]](total_task_count)

  override val state_initial: AbstractAsynchronizerCoreState[T] =
    new InitialAlternateAsynchronizerCoreState[T](this)
  override val state_initialized: AbstractAsynchronizerCoreState[T] =
    new InitializedAlternateAsynchronizerCoreState[T](this)
  override val state_failed: AbstractAsynchronizerCoreState[T] =
    new FailedAlternateAsynchronizerCoreState[T](this)

  this.state_current = this.state_initial

  override val asynchronizer_tasks: Array[AsynchronizerTask[T]] =
    state_current.initialize()

  this.changeState(this.state_initialized)

  /** Method to fetch the count of total executed
    * task count.
    *
    * Can be used in conjunction with success / fail
    * result count or with total task count to check
    * variaous aspects of task execution progress.
    *
    * @return total executed task count.
    */
  override def getResultsPopulatedCount: Int = {
    var value: Int = 0
    for (result <- Results) {
      if (result != null && result.task_result != null) {
        value += 1
      }
    }
    value
  }

  /** Method to fetch the count of tasks which executed
    * without any exceptions.
    *
    * @return success task count (does not talk about
    *         executing / yet to execute tasks)
    */
  override def getSuccessResultsCount: Int = {
    var value: Int = 0
    for (result <- Results) {
      if (
        result != null && result.task_result != null && result.task_result.exception.isEmpty
      ) {
        value += 1
      }
    }
    value
  }

  /** Method to fetch the count of exceptions thrown by the
    * queued tasks which were produced while executing the
    * enqueued tasks.
    * @return failed task count (does not talk about
    *         executing / yet to execute tasks)
    */
  override def getFailResultsCount: Int = {
    var value: Int = 0
    for (result <- Results) {
      if (
        result != null && result.task_result != null && result.task_result.exception.isDefined
      ) {
        value += 1
      }
    }
    value
  }

  /** Method to retrieve results.
    * @return will return results if state is completed,
    *         else it'll throw exception.
    */
  def getResults(): Array[AlternateAsynchronizerExecutionResults[T]] = {
    if (state_current != state_completed) {
      state_current.results()
    }
    Results
  }

  // make unnecessary methods and variables private.
  override def results(): Array[TaskConstants.TaskExecutionResults[T]] = {
    throw new Exception("method unsupported, use getResults() instead.")
    super.results()
  }

  def generateAlternateAsynchronizerExecutionReport
      : AlternateAsynchronizerExecutionReport[T] = {
    AlternateAsynchronizerExecutionReport[T](
      results = this.Results,
      total_tasks = this.total_task_count,
      executed_tasks = this.getResultsPopulatedCount,
      passed_tasks = this.getSuccessResultsCount,
      failed_tasks = this.getFailResultsCount,
      duration_in_ms = computeExecutionTimeInMillis,
      failures_allowed = this.allowFailures,
      was_cancelled = this.is_cancelled.get(),
      status = this.getState.toString
    )
  }
}
