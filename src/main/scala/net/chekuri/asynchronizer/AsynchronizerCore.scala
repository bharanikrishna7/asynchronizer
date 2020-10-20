package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerExecutionReport
import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.state._
import net.chekuri.asynchronizer.task.AsynchronizerTask
import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

import scala.concurrent.{ExecutionContext, Future}

class AsynchronizerCore[T](
    tasks: List[Future[T]],
    allowFailures: Boolean = false,
    executionContext: ExecutionContext =
      ExecutionContext.fromExecutor(new ForkJoinPool())
) extends LoggingBehavior
    with ThreadBehavior {
  logger.trace("Initializing Asynchronizer.")
  logger.trace(s"Current Thread ID : ${this.getCurrentThreadId}")

  logger.trace("Setting up class variables.")
  val total_task_count: Int = tasks.size
  var executed_task_count: Int = 0
  var completed_task_count: Int = 0
  var failed_task_count: Int = 0
  val is_cancelled: AtomicBoolean = new AtomicBoolean(false)
  val ready: AtomicBoolean = new AtomicBoolean(false)
  var execution_start_nanotime: Long = 0
  var execution_end_nanotime: Long = 0
  logger.trace("Class variables have been  successfully initialized.")

  val state_initial: AbstractAsynchronizerCoreState[T] =
    new InitialAsynchronizerCoreState[T](this)
  val state_initialized: AbstractAsynchronizerCoreState[T] =
    new InitializedAsynchronizerCoreState[T](this)
  val state_processing: AbstractAsynchronizerCoreState[T] =
    new ProcessingAsynchronizerCoreState[T](this)
  val state_completed: AbstractAsynchronizerCoreState[T] =
    new CompletedAsynchronizerCoreState[T](this)
  val state_failed: AbstractAsynchronizerCoreState[T] =
    new FailedAsynchronizerCoreState[T](this)
  val state_interrupted: AbstractAsynchronizerCoreState[T] =
    new InterruptedAsynchronizerCoreState[T](this)

  var state_current: AbstractAsynchronizerCoreState[T] = this.state_initial
  // validate state variables and initialize tasks as Asynchronizer Tasks.
  val asynchronizer_tasks: Array[AsynchronizerTask[T]] =
    if (state_current != null) {
      // update state to initialized.
      val initialized = state_current.initialize()
      this.changeState(state_initialized)
      initialized
    } else {
      new Array[AsynchronizerTask[T]](total_task_count)
    }
  val asynchronizer_results: Array[TaskExecutionResults[T]] =
    new Array[TaskExecutionResults[T]](total_task_count)

  /** Method to fetch the execution context assigned to
    * Asynchronous task object.
    *
    * @return execution context
    */
  def getExecutionContext: ExecutionContext = this.executionContext

  /** Method to start processing asynchronous tasks enqueued
    * by Asynchronizer.
    */
  def process(): List[String] = {
    val results: List[String] = state_current.process()
    logger.info(
      "Asynchronizer has nofitied all Asynchronus tasks to start executing."
    )
    results
  }

  /** Method to interrupt processing tasks. This process
    * will ask the spawned child threads to interrupt. This
    * will hopefully notify Garbage Collector to cleanup
    * the spawned threads and conserve memory (mostly) and a little
    * but of CPU Time.
    *
    * This method is very useful when the execution context is shared
    * between lot of tasks and we would want to interrupt the tasks
    * spawned by asynchronizer-core safely.
    */
  def interrupt(): Unit = {
    state_current.interrupt()
  }

  /** Method to retrieve results.
    *
    * This is unsafe method and will throw exception
    * if the Asynchronizer is not in appropriate state.
    *
    * @return Asynchronizer task execution results.
    */
  def results(): Array[TaskExecutionResults[T]] = {
    state_current.results()
  }

  /** Method to get list of tasks supplied to the class.
    *
    * @return returns list of tasks supplied to the class.
    */
  def getTasks: List[Future[T]] = this.tasks

  /** Method to update state.
    *
    * @param nextState new state value.
    */
  def changeState(nextState: AbstractAsynchronizerCoreState[T]): Unit = {
    logger.debug(s"Current State: ${state_current.name}")
    logger.debug(s"Next State: ${nextState.name}")
    logger.trace(s"changing state.")
    this.state_current = nextState
    logger.debug("State has been updated for current task.")
  }

  /** Method to get value of allow failures flag.
    * @return value of allow failures flag.
    */
  def getAllowFailures: Boolean = this.allowFailures

  /** Method to generate Asynchronizer execution's report.
    *
    * This is an unsafe method since it'll return if it's called
    * before results are ready.
    *
    * @return Asynchronizer execution's report
    */
  def generateAsynchronizerExecutionReport: AsynchronizerExecutionReport[T] = {
    AsynchronizerExecutionReport[T](
      results = this.asynchronizer_results,
      total_tasks = this.total_task_count,
      executed_tasks = this.getResultsPopulatedCount,
      passed_tasks = this.getSuccessResultsCount,
      failed_tasks = this.getFailResultsCount,
      duration_in_ms = computeExecutionTimeInMillis,
      failures_allowed = this.allowFailures,
      was_cancelled = this.is_cancelled.get(),
      status = this.state_current.name.toString
    )
  }

  /** Method to compute execution time in milliseconds.
    *
    * This method will return execution time as -1 if
    * task didnot finish execution.
    *
    * @return task execution duration in milliseconds
    */
  def computeExecutionTimeInMillis: Double = {
    if (execution_end_nanotime == 0) {
      -1
    } else {
      (execution_end_nanotime - execution_start_nanotime) / 1000000d
    }
  }

  /** Method to fetch the count of total executed
    * task count.
    *
    * Can be used in conjunction with success / fail
    * result count or with total task count to check
    * variaous aspects of task execution progress.
    *
    * @return total executed task count.
    */
  def getResultsPopulatedCount: Int = {
    var value: Int = 0
    for (result <- asynchronizer_results) {
      if (result != null) {
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
  def getSuccessResultsCount: Int = {
    var value: Int = 0
    for (result <- asynchronizer_results) {
      if (result != null && result.exception.isEmpty) {
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
  def getFailResultsCount: Int = {
    var value: Int = 0
    for (result <- asynchronizer_results) {
      if (result != null && result.exception.isDefined) {
        value += 1
      }
    }
    value
  }
}
