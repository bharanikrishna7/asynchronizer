package net.chekuri.asynchronizer

import java.util.concurrent.ForkJoinPool

import net.chekuri.asynchronizer.behaviors.{LoggingBehavior, ThreadBehavior}
import net.chekuri.asynchronizer.task.AsynchronizerTask
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.{ExecutionContext, Future}

class AsynchronizerTaskSpec
    extends AnyFlatSpec
    with LoggingBehavior
    with ThreadBehavior {
  val executionContext: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool())

  "AsynchronizerTask" should "correctly execute and asynchronous task with no return value" in {
    logger.debug("Declaring future task.")
    val futureTask: Future[Unit] =
      FutureTasks.futureSleepTask(2L, executionContext)
    logger.debug("Future task declared.")
    logger.debug("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[Unit] =
      new AsynchronizerTask[Unit](futureTask)
    logger.debug("Asynchronizer task initialized.")
    logger.debug("Start processing task.")
    aTask.process()
    while (!aTask.is_finished.get()) {
      this.SleepCurrentThread(1L)
    }
    logger.info(s"Async Task Execution Time: ${aTask.duration_in_ms.get} ms")
    assert(true)
  }

  "AsynchronizerTask" should "correctly execute and asynchronous task with simple data type value" in {
    logger.debug("Declaring future task.")
    val futureTask: Future[Long] =
      FutureTasks.futureRandomNumberTask(200, 10, false, executionContext)
    logger.debug("Future task declared.")
    logger.debug("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[Long] =
      new AsynchronizerTask[Long](futureTask)
    logger.debug("Asynchronizer task initialized.")
    logger.debug("Start processing task.")
    aTask.process()

    while (!aTask.is_finished.get()) {
      this.SleepCurrentThread(1L)
    }
    val result = aTask.getResult(1L)
    logger.info(s"Result Retrieved: $result")
    logger.info(s"Async Task Execution Time: ${aTask.duration_in_ms.get} ms")
    assert(true)
  }

  "AsynchronizerTask" should "should throw exception when trying to retrieve the results before task has finished" in {
    logger.debug("Declaring future task.")
    val futureTask: Future[Unit] =
      FutureTasks.futureSleepTask(5L, executionContext)
    logger.debug("Future task declared.")
    logger.debug("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[Unit] =
      new AsynchronizerTask[Unit](futureTask)
    logger.debug("Asynchronizer task initialized.")
    logger.debug("Start processing task.")
    aTask.process()
    assertThrows[RuntimeException] {
      aTask.getResult()
    }
  }

  "AsynchronizerTask" should "correctly cancel an unfinished task" in {
    logger.debug("Declaring future task.")
    val futureTask: Future[Unit] =
      FutureTasks.futureSleepTask(20L, executionContext)
    logger.debug("Future task declared.")
    logger.debug("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[Unit] =
      new AsynchronizerTask[Unit](futureTask)
    logger.debug("Asynchronizer task initialized.")
    logger.debug("Start processing task.")
    aTask.process()
    aTask.interrupt
    assertThrows[Throwable] {
      aTask.getResult()
    }
    val throwable = aTask.exception.get
    logger.warn("Exception Message:")
    logger.warn(throwable.getMessage)
    assert(throwable == aTask.THROW_INTERRUPT)
  }
}
