package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues
import net.chekuri.asynchronizer.task.AsynchronizerTask
import net.chekuri.asynchronizer.task.TaskConstants.TaskExecutionResults

trait AbstractAsynchronizerCoreState[T] {
  val name: AsynchronizerStateValues.Value

  /** Method to initialize task.
    */
  def initialize(): Array[AsynchronizerTask[T]] = {
    throw new Exception(
      s"Cannot initial Asynchronizer Tasks when Asynchronizer is in '$name' State. Asynchronizer needs to be in Initialized State to call this function."
    )
  }

  /** Method to start processing a task.
    */
  def process(): List[String] = {
    throw new Exception(
      s"Cannot process Asynchronizer Tasks when Asynchronizer is in '$name' State. Asynchronizer needs to be in Initialized State to call this function."
    )
  }

  /** Method to interrupt processing.
    */
  def interrupt(): Unit = {
    throw new Exception(
      s"Cannot interrupt Asynchronizer Tasks when Asynchronizer is in '$name' State. Asynchronizer needs to be in Processing State to call this function."
    )
  }

  /** Method to retrieve results.
    */
  def results(): Array[TaskExecutionResults[T]] = {
    throw new Exception(
      s"Cannot retrieve Asynchronizer Task(s) results when Asynchronizer is in '$name' State. Asynchronizer needs to be in Completed State to call this function."
    )
  }

  /** Method to retrieve results.
    */
  def success(): Array[TaskExecutionResults[T]] = {
    throw new Exception(
      s"Cannot retrieve Asynchronizer Task(s) results when Asynchronizer is in '$name' State. Asynchronizer needs to be in Completed State to call this function."
    )
  }

  /** Method to retrieve results.
    */
  def failures(): Array[TaskExecutionResults[T]] = {
    throw new Exception(
      s"Cannot retrieve Asynchronizer Task(s) results when Asynchronizer is in '$name' State. Asynchronizer needs to be in Completed State to call this function."
    )
  }
}
