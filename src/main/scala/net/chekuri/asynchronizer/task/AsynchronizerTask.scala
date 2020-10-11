package net.chekuri.asynchronizer.task

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.task.state._

import scala.concurrent.{ExecutionContext, Future}

/** Class to asynchronously execute a task
  * with ability to debug and cancel.
  *
  * @tparam T The class type of the task
  */
class AsynchronizerTask[T](
    task: Future[T],
    executionContext: ExecutionContext =
      ExecutionContext.fromExecutor(new ForkJoinPool())
) extends LoggingBehavior
    with ThreadBehavior {

  /* throwable object when task is interrupted */
  val THROW_INTERRUPT = new Throwable(
    s"Thread ${this.getCurrentThreadId} was interrupted."
  )

  val is_cancelled: AtomicBoolean = new AtomicBoolean(false)
  val is_finished: AtomicBoolean = new AtomicBoolean(false)
  var result: Option[T] = None
  var exception: Option[Throwable] = None

  /* variable to hold initial state */
  val state_initial: AbstractTaskState[T] = new InitialTaskState[T](this)
  /* variable to hold initialized state */
  val state_initialized: AbstractTaskState[T] =
    new InitializedTaskState[T](this)
  /* variable to hold processing state */
  val state_processing: AbstractTaskState[T] = new ProcessingTaskState[T](this)
  /* variable to hold success state */
  val state_success: AbstractTaskState[T] = new SuccessTaskState[T](this)
  /* variable to hold failure state */
  val state_failure: AbstractTaskState[T] = new FailureTaskState[T](this)
  /* variable to hold interrupted state */
  val state_interrupted: AbstractTaskState[T] =
    new InterruptedTaskState[T](this)
  /* variable to hold current state */
  var state_current: AbstractTaskState[T] = new InitialTaskState[T](this)

  this.changeState(state_initialized)

  /** Method to retrieve result.
    *
    * If results are not ready then exception
    * will be thrown by this method.
    *
    * @return result / exception associated
    *         with task to execute.
    */
  def getResult(maxWaitDurationInSeconds: Long = 0): T = {
    if (
      state_current == state_initialized || state_current == state_processing
    ) {
      logger.warn(
        "Asynchronous task needs to be in correct state before it can retrieve the results."
      )
      logger.warn(
        "Asynchronous task wait for internal state update. (based on task execution progress states will change)."
      )
      this.SleepCurrentThread(maxWaitDurationInSeconds)
    }
    if (is_finished.get() || is_cancelled.get()) {
      logger.trace("Task seems to be completed / cancelled.")
      state_current.results()
    } else {
      throw new RuntimeException(
        "Task is still executing. Cannot retrieve results unless task has finished."
      )
    }
  }

  /** Method to start processing task assigned
    * to async task (this) object.
    */
  def process(): Unit = {
    state_current.process()
    logger.info(s"started processing task on thread : $getCurrentThreadId")
  }

  /** Method to interrupt current task.
    *
    * If the task is already cancelled stopped.
    *
    * @return true if interrupt operation
    *         was successful, false otherwise.
    */
  def interrupt: Boolean = {
    if (is_cancelled.get()) {
      logger.warn("Task has already been interrupted.")
      false
    } else if (is_finished.get()) {
      logger.warn("Task has already been completed.")
      false
    } else {
      state_current.interrupt()
      true
    }
  }

  /** Method to update state.
    *
    * @param nextState new state value.
    */
  def changeState(nextState: AbstractTaskState[T]): Unit = {
    logger.debug(s"Current State: ${state_current.name}")
    logger.debug(s"Next State: ${nextState.name}")
    logger.trace(s"changing state.")
    this.state_current = nextState
    logger.debug("State has been updated for current task.")
  }

  /** Method to fetch the task assigned to Asynchronous
    * task object.
    *
    * @return task
    */
  def getTask: Future[T] = this.task

  /** Method to fetch the execution context assigned to
    * Asynchronous task object.
    *
    * @return execution context
    */
  def getExecutionContext: ExecutionContext = this.executionContext

  /** Method to cancel current thread execution.
    *
    * This method will interrupt the current thread
    * so if a future has not started execution
    * then it'll not spawn thread to process child.
    *
    * But if the current thread has already spawned a
    * new child thread to process then this method
    * will not be ideal.
    */
  def cancel(): Unit = {
    this.interrupt
  }
}
