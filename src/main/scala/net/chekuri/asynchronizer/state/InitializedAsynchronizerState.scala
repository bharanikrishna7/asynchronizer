package net.chekuri.asynchronizer.state

import java.lang.System.nanoTime

import net.chekuri.asynchronizer.Asynchronizer
import net.chekuri.asynchronizer.behaviors.LoggingBehavior
import net.chekuri.asynchronizer.task.TaskConstants
import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class InitializedAsynchronizerState[T](asynchronizer: Asynchronizer[T])
    extends AbstractAsynchronizerState[T]
    with LoggingBehavior {
  override val name = "InitializedAsynchronizerState"
  implicit val ec: ExecutionContext = asynchronizer.getExecutionContext

  private def checkAllCompleted: Boolean = {
    if (
      asynchronizer.total_task_count == asynchronizer.executed_tasks
        .get() || asynchronizer.ready.get()
    ) {
      logger.debug(
        "All Tasks have completed execution based on supplied arguments."
      )
      true
    } else {
      false
    }
  }

  override def process(): Unit = {
    logger.debug(s"Asynchronizer starting all asynchronizer tasks.")
    logger.debug("Changing state to ProcessingAsynchronizerState.")
    asynchronizer.changeState(asynchronizer.state_processing)

    for (index <- asynchronizer.asynchronizer_tasks.indices) {
      val start_time: Double = nanoTime
      asynchronizer
        .asynchronizer_tasks(index)
        .changeState(asynchronizer.asynchronizer_tasks(index).state_processing)
      asynchronizer.asynchronizer_tasks(index).getTask.andThen {
        case Success(value) =>
          logger.warn(
            s"Asynchronizer Results Size: ${asynchronizer.asynchronizer_results.size}"
          )
          if (
            shouldUpdateAsyncTask(
              asynchronizer.asynchronizer_results(index) == null
            )
          ) {
            asynchronizer.executed_tasks
              .set(asynchronizer.executed_tasks.get() + 1)
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
            asynchronizer.asynchronizer_results(index) =
              TaskExecutionResults[T](
                Some(value),
                None,
                duration_in_ms,
                asynchronizer.asynchronizer_tasks(index).state_current.name
              )
            if (checkAllCompleted) {
              asynchronizer.changeState(asynchronizer.state_completed)
              asynchronizer.execution_end_nanotime = nanoTime
              asynchronizer.ready.set(true)
            }
          }
        case Failure(throwable) =>
          if (
            shouldUpdateAsyncTask(
              asynchronizer.asynchronizer_results(index) == null
            )
          ) {
            asynchronizer.executed_tasks
              .set(asynchronizer.executed_tasks.get() + 1)
            val end_time: Double = nanoTime
            val duration_in_ms: Double = (end_time - start_time) / 1000000d
            asynchronizer.asynchronizer_tasks(index).duration_in_ms =
              Some(duration_in_ms)
            asynchronizer.asynchronizer_tasks(index).exception = Some(throwable)
            asynchronizer.execution_end_nanotime = nanoTime
            asynchronizer.asynchronizer_tasks(index).is_finished.set(true)
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
            asynchronizer.asynchronizer_results(index) =
              TaskExecutionResults[T](
                None,
                Some(throwable),
                duration_in_ms,
                asynchronizer.asynchronizer_tasks(index).state_current.name
              )
            if (checkAllCompleted && asynchronizer.getAllowFailures) {
              asynchronizer.changeState(asynchronizer.state_completed)
              asynchronizer.execution_end_nanotime = nanoTime
              asynchronizer.ready.set(true)
            }
          }
          if (!asynchronizer.getAllowFailures) {
            logger.warn("Fail on exception is set to false.")
            logger.warn("Will throw encountered exception")
            asynchronizer.execution_end_nanotime = nanoTime
            throw throwable
            asynchronizer.changeState(asynchronizer.state_failed)
            logger
              .trace("Changing results ready value to true for asynchronizer.")
            asynchronizer.ready.set(true)
          }
      }
    }
    logger.info("All tasks have started processing.")
  }

  private def shouldUpdateAsyncTask(
      result_or_exception_present: Boolean
  ): Boolean = {
    if (asynchronizer.ready.get() || !result_or_exception_present) {
      false
    } else {
      true
    }
  }
}
