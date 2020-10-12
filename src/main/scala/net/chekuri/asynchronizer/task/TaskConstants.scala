package net.chekuri.asynchronizer.task

object TaskConstants {
  private val standardAsynchronizerTaskInterruptedExceptionMessage: String =
    "Thread was interrupted"
  class AsynchronizerTaskInterruptedException(thread_id: Long)
      extends InterruptedException(
        s"$standardAsynchronizerTaskInterruptedExceptionMessage. Associated Thread ID : $thread_id"
      )

  def checkIfAsynchronizerTaskInterruptedException(
      exception: Throwable
  ): Boolean = {
    if (
      exception.getMessage.contains(
        standardAsynchronizerTaskInterruptedExceptionMessage
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
