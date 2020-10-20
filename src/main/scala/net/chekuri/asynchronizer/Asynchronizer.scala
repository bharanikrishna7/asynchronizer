package net.chekuri.asynchronizer

import net.chekuri.asynchronizer.behaviors.LoggingBehavior
import net.chekuri.asynchronizer.exceptions.AsynchronizerExceptions.AsynchronizerResultsNotReadyException

import scala.concurrent.Future

/** Provides ability to execute asynchronous tasks
  * effectively with emphasis on debugging and
  * efficiency.
  * @param tasks tasks to execute
  * @param allowFailures should allow failures (false by default)
  * @param parallelism max parallelism to use to execute tasks (will use processor count detected by JVM by default)
  * @tparam T Type associated with expected results.
  */
class Asynchronizer[T](
    tasks: List[Future[T]],
    allowFailures: Boolean = false,
    parallelism: Int = Runtime.getRuntime.availableProcessors()
) extends LoggingBehavior {
  logger.debug("Initializing Asynchronizer Class Variables.")
  private val asynchronizer_core =
    new AlternateAsynchronizerCore[T](tasks, allowFailures, parallelism)

  /** Method to start processing the enqueued
    * tasks. Will return the list of task_guids
    * associated with asynchronizer task.
    * @return list of unique guid to identify task
    *         execution results when ready.
    */
  def process(): List[String] = asynchronizer_core.process()

  /** Method to interrupt non-finished tasks enqueued
    * in asynchronizer.
    */
  def interrupt(): Unit = asynchronizer_core.interrupt()

  /** Method to get total task count.
    * @return total task count.
    */
  def getTotalTaskCount(): Int = asynchronizer_core.total_task_count

  /** Method to get current executed task count.
    * @return current executed task count.
    */
  def getExecutedTaskCount(): Int = asynchronizer_core.getResultsPopulatedCount

  /** Method to get current success task count.
    * @return current success task count.
    */
  def getSuccessTaskCount(): Int = asynchronizer_core.getSuccessResultsCount

  /** Method to get current failed task count.
    * @return current failed task count.
    */
  def getFailureTaskCount(): Int = {
    var result: Int = 0
    for (index <- asynchronizer_core.Results.indices) {
      if (
        (asynchronizer_core.Results(index).task_result != null) &&
        asynchronizer_core
          .Results(index)
          .task_result
          .taskExecutionState
          .equalsIgnoreCase("FailureTaskState")
      ) {
        result += 1
      }
    }
    result
  }

  /** Method to get current interrupted task count.
    * @return current interrupted task count.
    */
  def getInterruptedTaskCount(): Int = {
    var result: Int = 0
    for (index <- asynchronizer_core.Results.indices) {
      if (
        (asynchronizer_core.Results(index).task_result != null) &&
        asynchronizer_core
          .Results(index)
          .task_result
          .taskExecutionState
          .equalsIgnoreCase("InterruptedTaskState")
      ) {
        result += 1
      }
    }
    result
  }

  /** Method to retrieve results.
    * @return
    */
  def retrieveResults: Array[
    AsynchronizerConstants.AlternateAsynchronizerExecutionResults[T]
  ] = if (asynchronizer_core.isReady) {
    asynchronizer_core.Results
  } else {
    throw new AsynchronizerResultsNotReadyException(
      getTotalTaskCount,
      getExecutedTaskCount
    )
  }

  /** Method to check if asynchronizer
    * still processing any tasks or if
    * it's done processing all it's tasks.
    * @return false is there are tasks processing,
    *         true otherwise. [completed, failure, interrupted]
    */
  def isReady: Boolean = asynchronizer_core.isReady

  /** Method to retrieve the asynchronizer
    * processing time in milliseconds.
    *
    * @return asynchronizer processing
    *         time in milliseconds.
    */
  def getProcessingTimeInMillis: Double =
    asynchronizer_core.computeExecutionTimeInMillis

  /** Method to generate execution report.
    * @return execution report.
    */
  def generateExecutionReport
      : AsynchronizerConstants.AlternateAsynchronizerExecutionReport[T] = {
    if (!this.isReady) {
      logger.warn("Requested to generate report while tasks are executing.")
      logger.warn(
        "Beware trying to access the results can throw null pointer exception."
      )
    }
    asynchronizer_core.generateAlternateAsynchronizerExecutionReport
  }

  /** Method to retrieve current state of asynchronizer.
    * @return current state of asynchronizer.
    */
  def getCurrentState: AsynchronizerConstants.AsynchronizerStateValues.Value =
    asynchronizer_core.getState
}
