package net.chekuri.asynchronizer.task

import net.chekuri.asynchronizer.exceptions.AsynchronizerTaskExceptions._

object TaskConstants {
  def checkIfAsynchronizerTaskInterruptedException(
      exception: Throwable
  ): Boolean = {
    if (
      exception.getMessage.contains(
        AsynchronizerTaskInterruptedExceptionMessage
      ) && exception.getClass.getName.equals(
        new AsynchronizerTaskInterruptedException(
          -1
        ).getClass.getName
      )
    ) {
      true
    } else {
      false
    }
  }

  case class TaskExecutionResults[T](
      payload: Option[T],
      exception: Option[Throwable],
      durationInMilliSeconds: Double,
      taskExecutionState: String
  ) {
    override def toString: String = {
      s"""
         | Payload       : ${payload.toString}
         | Exception     : ${exception.toString}
         | Duration (ms) : $durationInMilliSeconds ms
         | Task State    : $taskExecutionState
         |""".stripMargin
    }
  }

  case class TaskExecutionSuccessResults[T](
      payload: T,
      durationInMilliSeconds: Double
  )

  case class TaskExecutionExceptionResults[T](
      exception: Throwable,
      durationInMilliSeconds: Double,
      taskExecutionState: String
  )
}
