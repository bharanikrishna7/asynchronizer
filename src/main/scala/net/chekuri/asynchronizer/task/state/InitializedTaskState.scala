package net.chekuri.asynchronizer.task.state

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.task.AsynchronizerTask

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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
  override def process(): Unit = {
    val task: Future[T] = asynchronizerTask.getTask
    logger.debug("Changing state to ProcessingTaskState.")
    asynchronizerTask.changeState(asynchronizerTask.state_processing)
    logger.debug("Changed state to ProcessingTaskState.")
    task.andThen {
      case Success(value) =>
        if (shouldUpdateAsyncTask()) {
          asynchronizerTask.result = Some(value)
          asynchronizerTask.is_finished.set(true)
          asynchronizerTask.changeState(asynchronizerTask.state_success)
        }
      case Failure(throwable) =>
        if (shouldUpdateAsyncTask()) {
          asynchronizerTask.exception = Some(throwable)
          asynchronizerTask.is_finished.set(true)
          asynchronizerTask.changeState(asynchronizerTask.state_failure)
        }
    }
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
