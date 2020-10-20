package net.chekuri.asynchronizer.task.state

/** Interface / trait to define states
  * and behaviours to efficiently handle
  * AsynchronousTask object
  * @tparam T Data Type associated with the result.
  */
trait AbstractTaskState[T] {
  val name: String

  /** Method to initialize task.
    */
  def initialize(): Unit = {
    // do nothing by default
  }

  /** Method to start processing a task.
    */
  def process(): Long = {
    throw new Exception(
      s"Cannot process task when Task is in '$name' State. Task needs to be in Initialized State to call this function."
    )
  }

  /** Method to interrupt processing.
    */
  def interrupt(): Unit = {
    throw new Exception(
      s"Cannot interrupt task when Task is in '$name' State. Task needs to be in Processing State to call this function."
    )
  }

  /** Method to retrieve results.
    */
  def results(): T = {
    throw new Exception(
      s"Cannot retrieve task results when Task is in '$name' State. Task needs to be in Success State to call this function."
    )
  }

  /** Method to retrieve exception.
    * @return
    */
  def exception(): Throwable = {
    throw new Exception(
      s"Cannot retrieve task exception when Task is in '$name' State. Task needs to be in Failure State to call this function."
    )
  }
}
