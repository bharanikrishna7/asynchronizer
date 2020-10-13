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

  "AsynchronizerTaskState" should "correctly change states when a task which will be successful is being through it" in {
    logger.debug("Declaring future task.")
    val base: Long = FutureTasks.randomizer.nextLong(100000)
    val power: Int = FutureTasks.randomizer.nextInt(10000)
    val futureTask: Future[BigInt] = FutureTasks
      .poorlyImplementedExponentOperation(base, power, executionContext)
    logger.debug("Future task declared.")
    logger.info("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[BigInt] =
      new AsynchronizerTask[BigInt](futureTask)
    logger.info("Asynchronizer task initialized.")
    logger.info("Asynchronizer should now be in 'InitializedTaskState'")
    assert(aTask.state_current == aTask.state_initialized)
    logger.info("Asynchronizer starting processing task.")
    aTask.process()
    logger.info("Asynchronizer should now be in 'ProcessingTaskState'")
    assert(aTask.state_current == aTask.state_processing)
    while (!aTask.is_finished.get()) {
      this.SleepCurrentThread(1L)
    }
    logger.info("Asynchronizer Successfully completed task execution.")
    logger.info(s"$base pow ($power) = ${aTask.getResult()}")
    logger.info(s"Time taken to compete task : ${aTask.duration_in_ms.get} ms.")
    logger.info("Asynchronizer should now be in 'SuccessTaskState'")
    assert(aTask.state_current == aTask.state_success)
    logger.info("Asynchronizer successfully completed state transition test.")
  }

  "AsynchronizerTaskState" should "correctly change states when a task is cancelled between execution" in {
    logger.debug("Declaring future task.")
    val futureTask: Future[Unit] =
      FutureTasks.futureSleepTask(20L, executionContext)
    logger.debug("Future task declared.")
    logger.info("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[Unit] =
      new AsynchronizerTask[Unit](futureTask)
    logger.info("Asynchronizer task initialized.")
    logger.info("Asynchronizer should now be in 'InitializedTaskState'")
    assert(aTask.state_current == aTask.state_initialized)
    logger.info("Asynchronizer starting processing task.")
    aTask.process()
    logger.info("Asynchronizer should now be in 'ProcessingTaskState'")
    assert(aTask.state_current == aTask.state_processing)
    logger.info("Interrupting Asynchronizer task.")
    aTask.interrupt
    logger.info("Asynchronizer should now be in 'InterruptedTaskState'")
    assert(aTask.state_current == aTask.state_interrupted)
    logger.info("Asynchronizer successfully completed state transition test.")
  }

  "AsynchronizerTaskState" should "correctly change states when a task fails while executing" in {
    logger.debug("Declaring future task.")
    val exception_message: String =
      "Asynchronizer Task State Failure Exception Message"
    val futureTask: Future[Boolean] =
      FutureTasks.futureExceptionTask(exception_message, 1L, executionContext)
    logger.debug("Future task declared.")
    logger.info("Initializing asynchronizer task.")
    val aTask: AsynchronizerTask[Boolean] =
      new AsynchronizerTask[Boolean](futureTask)
    logger.info("Asynchronizer task initialized.")
    logger.info("Asynchronizer should now be in 'InitializedTaskState'")
    assert(aTask.state_current == aTask.state_initialized)
    logger.info("Asynchronizer starting processing task.")
    aTask.process()
    logger.info("Asynchronizer should now be in 'ProcessingTaskState'")
    assert(aTask.state_current == aTask.state_processing)
    logger.info("Wait for task to complete.")
    while (!aTask.is_finished.get()) {
      this.SleepCurrentThreadInMillis(100L)
    }
    logger.info("The task should have thrown exception.")
    logger.info(
      "Ensuring that exception message generated is actually by this method."
    )
    assert(aTask.exception.get.getMessage == exception_message)
    logger.info(
      s"Asynchronizer time spent executing Task : ${aTask.duration_in_ms.get} ms."
    )
    logger.info("Asynchronizer should now be in 'FailureTaskState'")
    assert(aTask.state_current == aTask.state_failure)
    logger.info("Asynchronizer successfully completed state transition test.")
  }
}
