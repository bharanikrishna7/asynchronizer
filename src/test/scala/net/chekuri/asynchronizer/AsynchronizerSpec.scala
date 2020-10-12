package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.{ExecutionContext, Future}

class AsynchronizerSpec
    extends AnyFlatSpec
    with LoggingBehavior
    with ThreadBehavior {
  val executionContext: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool())

  "Asynchronizer" should "correctly execute all tasks when allow failures is set to false but we can gurantee tasks will pass" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 63) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      tasks = FutureTasks.poorlyImplementedExponentOperation(
        base,
        exp,
        executionContext
      ) :: tasks
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: Asynchronizer[BigInt] =
      new Asynchronizer[BigInt](tasks, false, executionContext)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(1000)
    }
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.debug("Printing Execution Report")
    logger.info(report.toString)
    assert(report.total_tasks == report.executed_tasks)
    assert(report.executed_tasks == report.passed_tasks)
    assert(report.failed_tasks == 0)
  }

  "Asynchronizer" should "correctly execute all tasks when allow failures is set to true but we can gurantee tasks will pass" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 63) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      tasks = FutureTasks.poorlyImplementedExponentOperation(
        base,
        exp,
        executionContext
      ) :: tasks
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer: Asynchronizer[BigInt] =
      new Asynchronizer[BigInt](tasks, true, executionContext)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(1000)
    }
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.debug("Printing Execution Report")
    logger.info(report.toString)
    assert(report.total_tasks == report.executed_tasks)
    assert(report.executed_tasks == report.passed_tasks)
    assert(report.failed_tasks == 0)
  }

  "Asynchronizer" should "correctly execute all tasks when allow failures is set to true but we are expecting some tasks to fail" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 63) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      if (index < 20) {
        tasks = FutureTasks.futureBigIntException(
          "Asynchronizer tasks fail but allow failures is true test exception.",
          1,
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
    val asynchronizer: Asynchronizer[BigInt] =
      new Asynchronizer[BigInt](tasks, true, executionContext)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(3000)
    }
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.debug("Printing Execution Report")
    logger.info(report.toString)
    assert(report.passed_tasks == 44)
    assert(report.executed_tasks > report.passed_tasks)
    assert(report.failed_tasks > 0)
  }

  "Asynchronizer" should "correctly throw exception when fail on exception is set to true and we can guarantee few exception tasks" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 63) {
      val base = FutureTasks.randomizer.nextLong(50000)
      val exp = FutureTasks.randomizer.nextInt(5000)
      if (index < 20 || index > 40) {
        val timeout = FutureTasks.randomizer.nextInt(10)
        tasks = FutureTasks.futureBigIntException(
          "Asynchronizer tasks fail but allow failures is true test exception.",
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
    val asynchronizer: Asynchronizer[BigInt] =
      new Asynchronizer[BigInt](tasks, false, executionContext)
    logger.info("Start processing tasks.")
    asynchronizer.process()
    SleepCurrentThreadInMillis(5000)
    while (!asynchronizer.ready.get()) {
      SleepCurrentThreadInMillis(1000)
    }
    val report = asynchronizer.generateAsynchronizerExecutionReport
    logger.info(report.toString)
    assert(asynchronizer.total_task_count > asynchronizer.executed_tasks.get())
    assert(asynchronizer.state_current.name == asynchronizer.state_failed.name)
  }
}
