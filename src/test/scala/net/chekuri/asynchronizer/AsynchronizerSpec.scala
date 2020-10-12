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

  "Asynchronizer" should "correctly execute all tasks" in {
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
  }
}
