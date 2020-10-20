package net.chekuri.asynchronizer.state

import java.lang.System.nanoTime

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.ProcessingAsynchronizerCoreState
import net.chekuri.asynchronizer.behaviors.LoggingBehavior
import net.chekuri.asynchronizer.{AsynchronizerConstants, AsynchronizerCore}

class ProcessingAsynchronizerCoreState[T](asynchronizer: AsynchronizerCore[T])
    extends AbstractAsynchronizerCoreState[T]
    with LoggingBehavior {
  override val name: AsynchronizerConstants.AsynchronizerStateValues.Value =
    ProcessingAsynchronizerCoreState

  override def interrupt(): Unit = {
    logger.debug("Setting is_cancelled to true for Asynchronizer.")
    logger.debug(
      "All unfinished tasks will be marked with `Thread < thread_id > was interrupted` exception."
    )
    logger.debug("Requesting all unfinished tasks to be interrupted.")
    for (index <- asynchronizer.asynchronizer_tasks.indices) {
      if (
        asynchronizer
          .asynchronizer_tasks(index)
          .state_current
          .name
          .eq("ProcessingTaskState")
      ) {
        asynchronizer.asynchronizer_tasks(index).interrupt
      }
    }
    logger.debug("All unfinished tasks were asked to be interrupted.")
    logger.debug("Changing state to InterruptedAsynchronizerState.")
    asynchronizer.changeState(asynchronizer.state_interrupted)
    logger.trace("Changing results ready value to true for asynchronizer.")
    asynchronizer.execution_end_nanotime = nanoTime
    asynchronizer.ready.set(true)
  }
}
