package net.chekuri.asynchronizer

import java.lang.System.nanoTime
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerExecutionReport
import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.state._
import net.chekuri.asynchronizer.task.AsynchronizerTask
import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

import scala.concurrent.{ExecutionContext, Future}

class Asynchronizer[T](
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
  val executed_tasks: AtomicInteger = new AtomicInteger(0)
  val successful_tasks: AtomicInteger = new AtomicInteger(0)
  val failure_tasks: AtomicInteger = new AtomicInteger(0)
  val is_cancelled: AtomicBoolean = new AtomicBoolean(false)
  val ready: AtomicBoolean = new AtomicBoolean(false)
  var execution_start_nanotime: Long = 0
  var execution_end_nanotime: Long = 0
  logger.trace("Class variables have been  successfully initialized.")

  val state_initial: AbstractAsynchronizerState[T] =
    new InitialAsynchronizerState[T](this)
  val state_initialized: AbstractAsynchronizerState[T] =
    new InitializedAsynchronizerState[T](this)
  val state_processing: AbstractAsynchronizerState[T] =
    new ProcessingAsynchronizerState[T](this)
  val state_completed: AbstractAsynchronizerState[T] =
    new CompletedAsynchronizerState[T](this)
  val state_failed: AbstractAsynchronizerState[T] =
    new FailedAsynchronizerState[T](this)
  val state_interrupted: AbstractAsynchronizerState[T] =
    new InterruptedAsynchronizerState[T](this)

  var state_current: AbstractAsynchronizerState[T] = this.state_initial
  // validate state variables and initialize tasks as Asynchronizer Tasks.
  val asynchronizer_tasks: Array[AsynchronizerTask[T]] =
    state_current.initialize()
  val asynchronizer_results: Array[TaskExecutionResults[T]] =
    new Array[TaskExecutionResults[T]](total_task_count)

  // update state to initialized.
  this.changeState(state_initialized)

  /** Method to fetch the execution context assigned to
    * Asynchronous task object.
    *
    * @return execution context
    */
  def getExecutionContext: ExecutionContext = this.executionContext

  /** Method to start processing asynchronous tasks enqueued
    * by Asynchronizer.
    */
  def process(): Unit = {
    execution_start_nanotime = nanoTime
    state_current.process()
    logger.info(
      "Asynchronizer has nofitied all Asynchronus tasks to start executing."
    )
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
  def changeState(nextState: AbstractAsynchronizerState[T]): Unit = {
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
      executed_tasks = this.executed_tasks.get(),
      passed_tasks = this.successful_tasks.get(),
      failed_tasks = this.failure_tasks.get(),
      duration_in_ms = computeExecutionTimeInMillis,
      failures_allowed = this.allowFailures,
      was_cancelled = this.is_cancelled.get()
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
}
