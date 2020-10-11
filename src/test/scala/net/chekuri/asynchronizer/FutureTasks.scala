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
}
