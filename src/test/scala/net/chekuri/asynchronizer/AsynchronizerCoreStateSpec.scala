package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.{ExecutionContext, Future}

class AsynchronizerCoreStateSpec
    extends AnyFlatSpec
    with LoggingBehavior
    with ThreadBehavior {
  val executionContext: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool())

  "AsynchronizerCoreState" should "correctly change states when when fail on exception is set to false and we can guarantee no exception tasks" in {
    var tasks: List[Future[Long]] = List[Future[Long]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 63) {
      tasks = FutureTasks.futureRandomNumberTask(
        Long.MaxValue,
        100,
        false,
        executionContext
      ) :: tasks
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: AsynchronizerCore[Long] =
      new AsynchronizerCore[Long](tasks, false)
    logger.info("Asynchronizer initialized.")
    logger.info(
      "Asynchronizer should now be in 'InitializedAsynchronizerState'"
    )
    assert(asynchronizer.state_current == asynchronizer.state_initialized)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    logger.info("Asynchronizer started processing tasks.")
    logger.info("Asynchronizer should now be in processing state.")
    logger.info("Asynchronizer should now be in 'ProcessingAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_processing)
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(1000)
    }
    logger.info(
      "We ensured that at least 1 task will fail. And Allow Failures = FALSE"
    )
    logger.info("Asynchronizer should now be in 'CompletedAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_completed)
    logger.info("All State Transitions have completed as expected.")
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.debug("Printing Execution Report")
    logger.info(report.toString)
    assert(report.total_tasks == report.executed_tasks)
    assert(report.executed_tasks == report.passed_tasks)
    assert(report.failed_tasks == 0)
  }

  "AsynchronizerCoreState" should "correctly change states when when fail on exception is set to true and we can guarantee no exception tasks" in {
    var tasks: List[Future[Long]] = List[Future[Long]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 8) {
      tasks = FutureTasks.futureRandomNumberTask(
        Long.MaxValue,
        30,
        false,
        executionContext
      ) :: tasks
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: AsynchronizerCore[Long] =
      new AsynchronizerCore[Long](tasks, true)
    logger.info("Asynchronizer initialized.")
    logger.info(
      "Asynchronizer should now be in 'InitializedAsynchronizerState'"
    )
    assert(asynchronizer.state_current == asynchronizer.state_initialized)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    logger.info("Asynchronizer started processing tasks.")
    logger.info("Asynchronizer should now be in processing state.")
    logger.info("Asynchronizer should now be in 'ProcessingAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_processing)
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(100)
    }
    logger.info(
      "We ensured that at least 1 task will fail. And Allow Failures = FALSE"
    )
    logger.info("Asynchronizer should now be in 'CompletedAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_completed)
    logger.info("All State Transitions have completed as expected.")
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.debug("Printing Execution Report")
    logger.info(report.toString)
    assert(report.total_tasks == report.executed_tasks)
    assert(report.executed_tasks == report.passed_tasks)
    assert(report.failed_tasks == 0)
  }

  "AsynchronizerCoreState" should "correctly change states when when fail on exception is set to false and we can guarantee few exception tasks" in {
    var tasks: List[Future[Long]] = List[Future[Long]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 24) {
      if (index < 20) {
        tasks = FutureTasks.futureRandomNumberTask(
          Long.MaxValue,
          100,
          true,
          executionContext
        ) :: tasks
      } else {
        tasks = FutureTasks.futureRandomNumberTask(
          Long.MaxValue,
          10,
          false,
          executionContext
        ) :: tasks
      }
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: AsynchronizerCore[Long] =
      new AsynchronizerCore[Long](tasks, true)
    logger.info("Asynchronizer initialized.")
    logger.info(
      "Asynchronizer should now be in 'InitializedAsynchronizerState'"
    )
    assert(asynchronizer.state_current == asynchronizer.state_initialized)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    logger.info("Asynchronizer started processing tasks.")
    logger.info("Asynchronizer should now be in processing state.")
    logger.info("Asynchronizer should now be in 'ProcessingAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_processing)
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(1000)
    }
    logger.info(
      "We ensured that at least 1 task will fail. And Allow Failures = FALSE"
    )
    logger.info("Asynchronizer should now be in 'CompletedAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_completed)
    logger.info("All State Transitions have completed as expected.")
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.debug("Printing Execution Report")
    logger.info(report.toString)
    assert(report.executed_tasks > report.passed_tasks)
    assert(report.failed_tasks > 0)
  }

  "AsynchronizerCoreState" should "correctly change states when when fail on exception is set to true and we can guarantee few exception tasks" in {
    var tasks: List[Future[Long]] = List[Future[Long]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 199) {
      val timeout = FutureTasks.randomizer.nextInt(100)
      if (index > 150) {
        tasks = FutureTasks.futureRandomNumberTask(
          Long.MaxValue,
          timeout,
          true,
          executionContext
        ) :: tasks
      } else {
        tasks = FutureTasks.futureRandomNumberTask(
          Long.MaxValue,
          timeout,
          false,
          executionContext
        ) :: tasks
      }
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: AsynchronizerCore[Long] =
      new AsynchronizerCore[Long](tasks, false)
    logger.info("Asynchronizer initialized.")
    logger.info(
      "Asynchronizer should now be in 'InitializedAsynchronizerState'"
    )
    assert(asynchronizer.state_current == asynchronizer.state_initialized)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    logger.info("Asynchronizer started processing tasks.")
    logger.info("Asynchronizer should now be in processing state.")
    logger.info("Asynchronizer should now be in 'ProcessingAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_processing)
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(1000)
    }
    SleepCurrentThreadInMillis(4000)
    logger.info(
      "We ensured that at least 1 task will fail. And Allow Failures = FALSE"
    )
    logger.info("Asynchronizer should now be in 'FailedAsynchronizerState'")
    assert(asynchronizer.state_current == asynchronizer.state_failed)
    logger.info("All State Transitions have completed as expected.")
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.info(report.toString)
  }
}
