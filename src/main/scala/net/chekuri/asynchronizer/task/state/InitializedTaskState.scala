package net.chekuri.asynchronizer.task.state

import java.lang.System.nanoTime

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.task.AsynchronizerTask

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** `Asynchronizer Task State` to handle
  * processing when Asynchronizer Task
  * has been correctly initialized.
  * @param asynchronizerTask asynchronizer task
  * @tparam T Data Type associated with the result.
  */
class InitializedTaskState[T](asynchronizerTask: AsynchronizerTask[T])
    extends AbstractTaskState[T]
    with ThreadBehavior
    with LoggingBehavior {
  override val name = "InitializedTaskState"
  implicit val ec: ExecutionContext = asynchronizerTask.getExecutionContext

  /** Method to process the task assigned
    * to async task and update the states
    * and results (or) exception in background.
    */
  override def process(): Long = {
    val task: Future[T] = asynchronizerTask.getTask
    logger.debug("Changing state to ProcessingTaskState.")
    asynchronizerTask.changeState(asynchronizerTask.state_processing)
    logger.debug("Changed state to ProcessingTaskState.")
    val start_time: Double = nanoTime
    task.andThen {
      case Success(value) =>
        if (shouldUpdateAsyncTask()) {
          val end_time: Double = nanoTime
          val duration_in_ms: Double = (end_time - start_time) / 1000000d
          asynchronizerTask.duration_in_ms = Some(duration_in_ms)
          asynchronizerTask.result = Some(value)
          asynchronizerTask.is_finished.set(true)
          asynchronizerTask.changeState(asynchronizerTask.state_success)
        }
      case Failure(throwable) =>
        if (shouldUpdateAsyncTask()) {
          val end_time: Double = nanoTime
          val duration_in_ms: Double = (end_time - start_time) / 1000000d
          asynchronizerTask.duration_in_ms = Some(duration_in_ms)
          asynchronizerTask.exception = Some(throwable)
          asynchronizerTask.is_finished.set(true)
          asynchronizerTask.changeState(asynchronizerTask.state_failure)
        }
    }
    val current_thread_id: Long = getCurrentThreadId
    logger.info(s"started processing task on thread : $current_thread_id")
    current_thread_id
  }

  /** Method to check if we should update
    * the async task object.
    *
    * Async task should only be updated if:
    * 1. task is cancelled
    * 2. task already has result (usually will not happen)
    * 3. task already has exception (most likely it'll be is cancelled generated exception)
    *
    * @return true if async task should be updated, false otherwise
    */
  private def shouldUpdateAsyncTask(): Boolean = {
    if (
      asynchronizerTask.is_cancelled
        .get() || asynchronizerTask.result.isDefined || asynchronizerTask.exception.isDefined
    ) {
      false
    } else {
      true
    }
  }
}
