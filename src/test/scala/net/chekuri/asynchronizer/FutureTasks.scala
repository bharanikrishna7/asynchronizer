package net.chekuri.asynchronizer

import net.chekuri.asynchronizer.behaviors.ThreadBehavior

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object FutureTasks extends ThreadBehavior {
  val randomizer: Random = Random

  def futureSleepTask(
      durationInSeconds: Long,
      ec: ExecutionContext
  ): Future[Unit] = Future {
    this.SleepCurrentThread(durationInSeconds)
  }(ec)

  def futureRandomNumberTask(
      maxValue: Long,
      ec: ExecutionContext
  ): Future[Long] = Future {
    randomizer.nextLong(maxValue)
  }(ec)

  def poorlyImplementedExponentOperation(
      base: Long,
      power: Int,
      ec: ExecutionContext
  ): Future[BigInt] = Future {
    val c_base = BigInt.apply(base)
    var result = BigInt.apply(1)
    for (_ <- 0 to power) {
      result = result.*(c_base)
    }
    result
  }(ec)

  def futureExceptionTask(
      exceptionMessage: String,
      delayInSeconds: Long = 0,
      ec: ExecutionContext
  ): Future[Boolean] = Future {
    this.SleepCurrentThread(delayInSeconds)
    throw new Exception(exceptionMessage)
    false
  }(ec)

  def futureBigIntException(
      exceptionMessage: String,
      delayInSeconds: Long = 0,
      ec: ExecutionContext
  ): Future[BigInt] = Future {
    this.SleepCurrentThread(delayInSeconds)
    throw new Exception(exceptionMessage)
    BigInt.apply(100)
  }(ec)
}
