package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.{
  CompletedAsynchronizerCoreState,
  FailedAsynchronizerCoreState
}
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
    val asynchronizer = new Asynchronizer[BigInt](tasks, false)
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
    assert(asynchronizer.getCurrentState == CompletedAsynchronizerCoreState)
    assert(
      asynchronizer.getTotalTaskCount == asynchronizer.getExecutedTaskCount
    )
    assert(
      asynchronizer.getExecutedTaskCount == asynchronizer.getSuccessTaskCount
    )
    assert(asynchronizer.getFailureTaskCount == 0)
    val report = asynchronizer.generateExecutionReport
    logger.debug("Asynchronizer Execution Report ...")
    logger.debug(report.toString)
  }

  "Asynchronizer" should "correctly execute all tasks when allow failures is set to true but we can gurantee tasks will pass" in {
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
    val asynchronizer = new Asynchronizer[BigInt](tasks, false)
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
    assert(asynchronizer.getCurrentState == CompletedAsynchronizerCoreState)
    assert(
      asynchronizer.getTotalTaskCount == asynchronizer.getExecutedTaskCount
    )
    assert(
      asynchronizer.getExecutedTaskCount == asynchronizer.getSuccessTaskCount
    )
    assert(asynchronizer.getFailureTaskCount == 0)
    val report = asynchronizer.generateExecutionReport
    logger.debug("Asynchronizer Execution Report ...")
    logger.debug(report.toString)
  }

  "Asynchronizer" should "correctly execute all tasks when allow failures is set to true but we can gurantee some tasks will fail" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 23) {
      if (index % 2 == 0) {
        val base = FutureTasks.randomizer.nextLong(50000)
        val exp = FutureTasks.randomizer.nextInt(5000)
        tasks = FutureTasks.poorlyImplementedExponentOperation(
          base,
          exp,
          executionContext
        ) :: tasks
      } else {
        val timeout = FutureTasks.randomizer.nextInt(30)
        tasks = FutureTasks.futureBigIntException(
          "Asynchronizer Test Exception: correctly throw exception when allow failures is set to false and we can guarantee few exception tasks.",
          timeout,
          executionContext
        ) :: tasks
      }
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer = new Asynchronizer[BigInt](tasks, true)
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
    assert(asynchronizer.getCurrentState == CompletedAsynchronizerCoreState)
    assert(
      asynchronizer.getTotalTaskCount == asynchronizer.getExecutedTaskCount
    )
    assert(
      asynchronizer.getExecutedTaskCount > asynchronizer.getSuccessTaskCount
    )
    assert(asynchronizer.getFailureTaskCount > 0)
    val report = asynchronizer.generateExecutionReport
    logger.debug("Asynchronizer Execution Report ...")
    logger.debug(report.toString)
  }

  "Asynchronizer" should "change state to failed when allow failures is set to false and we can gurantee some tasks will fail" in {
    var tasks: List[Future[BigInt]] = List[Future[BigInt]]()
    logger.info("Populating tasks.")
    for (index <- 0 to 23) {
      if (index % 2 == 0) {
        val base = FutureTasks.randomizer.nextLong(50000)
        val exp = FutureTasks.randomizer.nextInt(5000)
        tasks = FutureTasks.poorlyImplementedExponentOperation(
          base,
          exp,
          executionContext
        ) :: tasks
      } else {
        val timeout = FutureTasks.randomizer.nextInt(30)
        tasks = FutureTasks.futureBigIntException(
          "Asynchronizer Test Exception: correctly throw exception when allow failures is set to false and we can guarantee few exception tasks.",
          timeout,
          executionContext
        ) :: tasks
      }
    }
    logger.info("Tasks successfully populated.")
    val asynchronizer = new Asynchronizer[BigInt](tasks, false)
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
    SleepCurrentThread(10)
    assert(tasks.size == task_ids.size)
    assert(asynchronizer.getCurrentState == FailedAsynchronizerCoreState)
    assert(
      asynchronizer.getTotalTaskCount == asynchronizer.getExecutedTaskCount
    )
    assert(
      asynchronizer.getExecutedTaskCount > asynchronizer.getSuccessTaskCount
    )
    assert(asynchronizer.getFailureTaskCount > 0)
    val report = asynchronizer.generateExecutionReport
    logger.debug("Asynchronizer Execution Report ...")
    logger.debug(report.toString)
  }
}
