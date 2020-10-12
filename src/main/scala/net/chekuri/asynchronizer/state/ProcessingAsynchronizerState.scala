package net.chekuri.asynchronizer.state

import java.lang.System.nanoTime

import net.chekuri.asynchronizer.Asynchronizer
import net.chekuri.asynchronizer.behaviors.LoggingBehavior

class ProcessingAsynchronizerState[T](asynchronizer: Asynchronizer[T])
    extends AbstractAsynchronizerState[T]
    with LoggingBehavior {
  override val name: String = "ProcessingAsynchronizerState"

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
