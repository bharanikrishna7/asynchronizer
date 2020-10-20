package net.chekuri.asynchronizer.state

import java.lang.System.nanoTime

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.InitializedAsynchronizerCoreState
import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.exceptions.AsynchronizerExceptions
import net.chekuri.asynchronizer.task.TaskConstants
import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults
import net.chekuri.asynchronizer.{AsynchronizerConstants, AsynchronizerCore}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class InitializedAsynchronizerCoreState[T](asynchronizer: AsynchronizerCore[T])
    extends AbstractAsynchronizerCoreState[T]
    with ThreadBehavior
    with LoggingBehavior {
  override val name: AsynchronizerConstants.AsynchronizerStateValues.Value =
    InitializedAsynchronizerCoreState
  implicit val ec: ExecutionContext = asynchronizer.getExecutionContext

  /** Method to validate and update asynchronizer task
    * watchers progress.
    *
    * Task watchers are variables we are using to watch
    * progress of the executions. Current task watchers:
    * - executed_task_count
    * - completed_task_count
    * - failed_task_count
    */
  protected def updateAsynchronizerTaskWatcherValues() = {
    if (!asynchronizer.ready.get()) {
      val executed_tasks_count = asynchronizer.getResultsPopulatedCount
      val passed_task_count = asynchronizer.getSuccessResultsCount
      val failed_task_count = asynchronizer.getFailResultsCount
      asynchronizer.executed_task_count = executed_tasks_count
      asynchronizer.completed_task_count = passed_task_count
      asynchronizer.failed_task_count = failed_task_count
      if (asynchronizer.total_task_count == executed_tasks_count) {
        logger.info(
          s"Asynchronizer finished processing ${asynchronizer.total_task_count} tasks."
        )
        asynchronizer.execution_end_nanotime = nanoTime
        asynchronizer.ready.set(true)
        if (asynchronizer.state_current == asynchronizer.state_processing) {
          asynchronizer.changeState(asynchronizer.state_completed)
        }
      }

      if (!asynchronizer.getAllowFailures && failed_task_count > 0) {
        logger.error(
          s"Asynchronizer encountered exception but it has been set to not allow errors on execution."
        )
        // set ready first to ensure the unfinished
        // results are not updated with interrupted exception.
        asynchronizer.ready.set(true)
        logger.warn("Interrupting all unfinished tasks.")
        this.interruptUnfinishedTasks(
          asynchronizer.execution_start_nanotime
        )
        logger.warn(s"Setting asynchronizer state to fail.")
        asynchronizer.changeState(asynchronizer.state_failed)
        asynchronizer.execution_end_nanotime = nanoTime
      }
    }
  }

  def updateResults(index: Int, payload: TaskExecutionResults[T]) = {
    if (!asynchronizer.ready.get()) {
      asynchronizer.asynchronizer_results(index) = payload
    }
  }

  override def process(): List[String] = {
    logger.debug(s"Asynchronizer starting all asynchronizer tasks.")
    logger.debug("Changing state to ProcessingAsynchronizerState.")
    asynchronizer.execution_start_nanotime = nanoTime
    asynchronizer.changeState(asynchronizer.state_processing)

    for (index <- asynchronizer.asynchronizer_tasks.indices) {
      val start_time: Double = nanoTime
      asynchronizer
        .asynchronizer_tasks(index)
        .changeState(asynchronizer.asynchronizer_tasks(index).state_processing)
      asynchronizer
        .asynchronizer_tasks(index)
        .getTask
        .andThen {
          case Success(value) =>
            if (shouldUpdateAsyncTask(index)) {
              val end_time: Double = nanoTime
              val duration_in_ms: Double = (end_time - start_time) / 1000000d
              asynchronizer.asynchronizer_tasks(index).duration_in_ms =
                Some(duration_in_ms)
              asynchronizer.asynchronizer_tasks(index).result = Some(value)
              asynchronizer.asynchronizer_tasks(index).is_finished.set(true)
              asynchronizer
                .asynchronizer_tasks(index)
                .changeState(
                  asynchronizer.asynchronizer_tasks(index).state_success
                )
              updateResults(
                index,
                TaskExecutionResults[T](
                  Some(value),
                  None,
                  duration_in_ms,
                  asynchronizer.asynchronizer_tasks(index).state_current.name
                )
              )
            }
          case Failure(throwable) =>
            if (shouldUpdateAsyncTask(index)) {
              val end_time: Double = nanoTime
              val duration_in_ms: Double = (end_time - start_time) / 1000000d
              asynchronizer.asynchronizer_tasks(index).duration_in_ms =
                Some(duration_in_ms)
              asynchronizer.asynchronizer_tasks(index).exception =
                Some(throwable)
              asynchronizer.execution_end_nanotime = nanoTime
              asynchronizer.asynchronizer_tasks(index).is_finished.set(true)
              // update task state.
              if (
                TaskConstants
                  .checkIfAsynchronizerTaskInterruptedException(throwable)
              ) {
                // interrupt exception
                asynchronizer
                  .asynchronizer_tasks(index)
                  .changeState(
                    asynchronizer.asynchronizer_tasks(index).state_interrupted
                  )
              } else {
                // actual exception
                asynchronizer
                  .asynchronizer_tasks(index)
                  .changeState(
                    asynchronizer.asynchronizer_tasks(index).state_failure
                  )
              }
              updateResults(
                index,
                TaskExecutionResults[T](
                  None,
                  Some(throwable),
                  duration_in_ms,
                  asynchronizer.asynchronizer_tasks(index).state_current.name
                )
              )
            }
        }
        .andThen(_ => this.updateAsynchronizerTaskWatcherValues())
    }
    logger.info("All tasks have started processing.")
    val task_thread_ids: List[String] = getAssociatedThreads
    logger.debug("Collected thread ids associated with tasks.")
    task_thread_ids
  }

  def getAssociatedThreads: List[String] = asynchronizer.asynchronizer_tasks
    .map(x => x.getCurrentThreadId.toString)
    .toList

  /** Method to interrupt all unfinished tasks. This method improves
    * the memory usage by JVM by marking the tasks which we want to
    * interrupt as interrupted, so JVM can release the memory next
    * time Garbage Collection begins.
    * @param start_time execution start nanotime.
    */
  private def interruptUnfinishedTasks(start_time: Double): Unit = {
    val end_time: Double = nanoTime
    val duration_in_ms: Double = (end_time - start_time) / 1000000d
    val throwable =
      new AsynchronizerExceptions.AsynchronizerTaskInterruptedException(
        this.getCurrentThreadId
      )
    for (index <- asynchronizer.asynchronizer_tasks.indices) {
      if (
        asynchronizer
          .asynchronizer_tasks(index)
          .state_current
          .name
          .equals("ProcessingTaskState")
      ) {
        updateResults(
          index,
          TaskExecutionResults[T](
            None,
            Some(throwable),
            duration_in_ms,
            asynchronizer.asynchronizer_tasks(index).state_interrupted.name
          )
        )
        // stop processing task.
        asynchronizer.asynchronizer_tasks(index).interrupt
      }
    }
  }

  protected def shouldUpdateAsyncTask(
      index: Int
  ): Boolean = {
    if (
      asynchronizer.ready
        .get() || (asynchronizer.asynchronizer_results(index) != null)
    ) {
      false
    } else {
      true
    }
  }
}
