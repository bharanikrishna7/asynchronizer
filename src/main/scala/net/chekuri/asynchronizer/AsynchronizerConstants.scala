package net.chekuri.asynchronizer

import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

object AsynchronizerConstants {
  case class AsynchronizerExecutionReport[T](
      results: Array[TaskExecutionResults[T]],
      total_tasks: Int,
      executed_tasks: Int,
      passed_tasks: Int,
      failed_tasks: Int,
      duration_in_ms: Double,
      failures_allowed: Boolean,
      was_cancelled: Boolean
  ) {
    override def toString: String = {
      val appender: StringBuilder = new StringBuilder()
      appender.append("-----------")
      appender.append("| Results |")
      appender.append("-----------")

      for (result <- results) {
        appender.append(result.toString)
        appender.append('\n')
      }
      appender.append(s"Total Tasks      : $total_tasks\n")
      appender.append(s"Executed Tasks   : $executed_tasks\n")
      appender.append(s"Passed Tasks     : $passed_tasks\n")
      appender.append(s"Failed Tasks     : $failed_tasks\n")
      appender.append(s"Failures Allowed : $failed_tasks\n")
      appender.append(s"Was Cancelled    : $failed_tasks")
      appender.toString()
    }
  }

}
