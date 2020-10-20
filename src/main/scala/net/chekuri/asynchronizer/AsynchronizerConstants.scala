package net.chekuri.asynchronizer

import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

object AsynchronizerConstants {

  object AsynchronizerStateValues extends Enumeration {
    val InitialAsynchronizerCoreState: Value = Value(
      "InitialAsynchronizerCoreState"
    )
    val InitializedAsynchronizerCoreState: Value = Value(
      "InitializedAsynchronizerCoreState"
    )
    val ProcessingAsynchronizerCoreState: Value = Value(
      "ProcessingAsynchronizerCoreState"
    )
    val CompletedAsynchronizerCoreState: Value = Value(
      "CompletedAsynchronizerCoreState"
    )
    val FailedAsynchronizerCoreState: Value = Value(
      "FailedAsynchronizerCoreState"
    )
    val InterruptedAsynchronizerCoreState: Value = Value(
      "InterruptedAsynchronizerCoreState"
    )

    def all: Array[Value] = {
      Array[Value](
        InitialAsynchronizerCoreState,
        InitializedAsynchronizerCoreState,
        ProcessingAsynchronizerCoreState,
        CompletedAsynchronizerCoreState,
        FailedAsynchronizerCoreState,
        InterruptedAsynchronizerCoreState
      )
    }

    def get(state: String): Option[Value] = {
      var result: Option[Value] = None
      val values = this.all
      for (value <- values) {
        if (value.toString.equalsIgnoreCase(state)) {
          result = Some(value)
        }
      }
      result
    }
  }

  case class AsynchronizerExecutionReport[T](
      results: Array[TaskExecutionResults[T]],
      total_tasks: Int,
      executed_tasks: Int,
      passed_tasks: Int,
      failed_tasks: Int,
      duration_in_ms: Double,
      failures_allowed: Boolean,
      was_cancelled: Boolean,
      status: String
  ) {
    override def toString: String = {
      val appender: StringBuilder = new StringBuilder()
      appender.append("\n-----------")
      appender.append("\n| Results |")
      appender.append("\n-----------")

      for (result <- results) {
        if (result != null) {
          appender.append(result.toString)
          appender.append('\n')
        }
      }
      appender.append(s"Total Tasks          : $total_tasks\n")
      appender.append(s"Executed Tasks       : $executed_tasks\n")
      appender.append(s"Passed Tasks         : $passed_tasks\n")
      appender.append(s"Failed Tasks         : $failed_tasks\n")
      appender.append(s"Execution Duration   : $duration_in_ms ms\n")
      appender.append(s"Failures Allowed     : $failures_allowed\n")
      appender.append(s"Was Cancelled        : $was_cancelled\n")
      appender.append(s"Asynchronizer Status : $status")
      appender.toString()
    }
  }

  case class AlternateAsynchronizerExecutionResults[T](
      task_id: String,
      task_result: TaskExecutionResults[T]
  ) {
    override def toString: String = {
      s"""
         | Task ID       : ${task_id}
         | -----------
         | Task Result
         | -----------
         | ${task_result.toString}
         |""".stripMargin
    }
  }

  case class AlternateAsynchronizerExecutionReport[T](
      results: Array[AlternateAsynchronizerExecutionResults[T]],
      total_tasks: Int,
      executed_tasks: Int,
      passed_tasks: Int,
      failed_tasks: Int,
      duration_in_ms: Double,
      failures_allowed: Boolean,
      was_cancelled: Boolean,
      status: String
  ) {
    override def toString: String = {
      val appender: StringBuilder = new StringBuilder()
      appender.append("\n-----------")
      appender.append("\n| Results |")
      appender.append("\n-----------")

      for (result <- results) {
        if (result != null) {
          appender.append(result.toString)
          appender.append('\n')
        }
      }
      appender.append(s"Total Tasks          : $total_tasks\n")
      appender.append(s"Executed Tasks       : $executed_tasks\n")
      appender.append(s"Passed Tasks         : $passed_tasks\n")
      appender.append(s"Failed Tasks         : $failed_tasks\n")
      appender.append(s"Execution Duration   : $duration_in_ms ms\n")
      appender.append(s"Failures Allowed     : $failures_allowed\n")
      appender.append(s"Was Cancelled        : $was_cancelled\n")
      appender.append(s"Asynchronizer Status : $status")
      appender.toString()
    }
  }

}
