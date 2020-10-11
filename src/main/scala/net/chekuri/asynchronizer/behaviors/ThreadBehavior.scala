package net.chekuri.asynchronizer.behaviors

/** Interface / Trait to expose methods to
  * get more information or perform operations
  * on thread which is being used by object
  * created using this interface.
  *
  * There might be cases where a thread might
  * spawn more threads in such scenarios it'll
  * return the thread id of the top level object
  * which is spawning those new threads.
  */
trait ThreadBehavior {

  /** Method to get thread id associated with
    * current thread.
    *
    * @return current thread id.
    */
  def getCurrentThreadId: Long = {
    Thread.currentThread().getId
  }

  /** Method to stop current thread.
    */
  def StopCurrentThread(): Unit = {
    Thread.currentThread().interrupt()
  }

  /** Method to get thread name associated with
    * current thread.
    *
    * @return current thread name.
    */
  def getCurrentThreadName: String = {
    Thread.currentThread().getName
  }

  /** Method to sleep current thread.
    * @param sleepDurationInSeconds sleep duration in seconds.
    */
  def SleepCurrentThread(sleepDurationInSeconds: Long): Unit = {
    val convert_to_milliseconds: Long = sleepDurationInSeconds * 1000
    Thread.sleep(convert_to_milliseconds)
  }
}
