package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.{ExecutionContext, Future}

class AlternateAsynchronizerCoreSpec
    extends AnyFlatSpec
    with LoggingBehavior
    with ThreadBehavior {
  val executionContext: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool())

  "AlternateAsynchronizerCore" should "correctly execute all tasks when allow failures is set to false but we can gurantee tasks will pass" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 23) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      tasks = FutureTasks.poorlyImplementedExponentOperation(
        base,
        exp,
        executionContext
      ) :: tasks
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer = new AlternateAsynchronizerCore[BigInt](tasks, false)
    logger.info(
      s"${tasks.size} Threads have been initialized by asynchronizer."
    )
    val task_ids: List[String] = asynchronizer.process()
    logger.info(
      s"${task_ids.size} Threads started processing by asynchronizer."
    )
    logger.info("Task IDs:")
    task_ids.foreach(x => logger.info(x))
    while (!asynchronizer.isReady) {
      logger.trace("Sleeping for 1 second.")
      SleepCurrentThread(1)
    }
    assert(tasks.size == task_ids.size)
    assert(asynchronizer.getState == asynchronizer.state_completed.name)
    assert(
      asynchronizer.total_task_count == asynchronizer.getResultsPopulatedCount
    )
    assert(
      asynchronizer.getResultsPopulatedCount == asynchronizer.getSuccessResultsCount
    )
    assert(asynchronizer.getFailResultsCount == 0)
    logger.info("Execution Report")
    asynchronizer.getResults().map(x => logger.info(x))
  }

  "AlternateAsynchronizerCore" should "correctly execute all tasks when allow failures is set to true but we can gurantee tasks will pass" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 23) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      tasks = FutureTasks.poorlyImplementedExponentOperation(
        base,
        exp,
        executionContext
      ) :: tasks
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer = new AlternateAsynchronizerCore[BigInt](tasks, true)
    logger.info(
      s"${tasks.size} Threads have been initialized by asynchronizer."
    )
    val task_ids: List[String] = asynchronizer.process()
    logger.info(
      s"${task_ids.size} Threads started processing by asynchronizer."
    )
    logger.info("Task IDs:")
    task_ids.foreach(x => logger.info(x))
    while (!asynchronizer.isReady) {
      logger.trace("Sleeping for 1 second.")
      SleepCurrentThread(1)
    }
    assert(tasks.size == task_ids.size)
    assert(asynchronizer.getState == asynchronizer.state_completed.name)
    assert(
      asynchronizer.total_task_count == asynchronizer.getResultsPopulatedCount
    )
    assert(
      asynchronizer.getResultsPopulatedCount == asynchronizer.getSuccessResultsCount
    )
    assert(asynchronizer.getFailResultsCount == 0)
    logger.info("Execution Report")
    asynchronizer.getResults().map(x => logger.info(x))
  }

  "AlternateAsynchronizerCore" should "correctly throw exception when allow failures is set to false and we can guarantee few exception tasks" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 40) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      val should_throw_exception: Boolean = FutureTasks.randomizer.nextBoolean()
      if (should_throw_exception) {
        val timeout = FutureTasks.randomizer.nextInt(30)
        tasks = FutureTasks.futureBigIntException(
          "Asynchronizer Test Exception: correctly throw exception when allow failures is set to false and we can guarantee few exception tasks.",
          timeout,
          executionContext
        ) :: tasks
      } else {
        tasks = FutureTasks.poorlyImplementedExponentOperation(
          base,
          exp,
          executionContext
        ) :: tasks
      }
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: AlternateAsynchronizerCore[BigInt] =
      new AlternateAsynchronizerCore[BigInt](tasks, false)
    logger.info("Start processing tasks.")
    val task_ids = asynchronizer.process()
    while (!asynchronizer.isReady) {
      logger.trace("Sleeping for 1 second.")
      SleepCurrentThread(1)
    }
    SleepCurrentThreadInMillis(2000)
    val report = asynchronizer.generateAlternateAsynchronizerExecutionReport
    assert(report.status == asynchronizer.state_failed.name.toString)
    assert(
      report.total_tasks == report.executed_tasks
    )
    assert(
      report.executed_tasks > report.passed_tasks
    )
    assert(report.failed_tasks > 0)
    assertThrows[Exception](asynchronizer.getResults().map(x => logger.info(x)))
    logger.info("Execution Report")
    logger.info(report.toString)
  }

  "AlternateAsynchronizerCore" should "not throw exception when allow failures is set to true and we can guarantee few exception tasks" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 40) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      val should_throw_exception: Boolean = FutureTasks.randomizer.nextBoolean()
      if (should_throw_exception) {
        val timeout = FutureTasks.randomizer.nextInt(10)
        tasks = FutureTasks.futureBigIntException(
          "Asynchronizer Test Exception: correctly throw exception when allow failures is set to false and we can guarantee few exception tasks.",
          timeout,
          executionContext
        ) :: tasks
      } else {
        tasks = FutureTasks.poorlyImplementedExponentOperation(
          base,
          exp,
          executionContext
        ) :: tasks
      }
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: AlternateAsynchronizerCore[BigInt] =
      new AlternateAsynchronizerCore[BigInt](tasks, true)
    logger.info("Start processing tasks.")
    val task_ids = asynchronizer.process()
    while (!asynchronizer.isReady) {
      logger.trace("Sleeping for 1 second.")
      SleepCurrentThread(1)
    }
    SleepCurrentThreadInMillis(2000)
    val report = asynchronizer.generateAlternateAsynchronizerExecutionReport

    assert(report.status == asynchronizer.state_completed.name.toString)
    assert(
      report.total_tasks == report.executed_tasks
    )
    assert(
      report.executed_tasks > report.passed_tasks
    )
    assert(report.failed_tasks > 0)
    logger.info("Execution Report")
    logger.info(report.toString)
  }
}
