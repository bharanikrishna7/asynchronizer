package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.AsynchronizerConstants.AsynchronizerStateValues.InterruptedAsynchronizerCoreState
import net.chekuri.asynchronizer.{AsynchronizerConstants, AsynchronizerCore}

class InterruptedAsynchronizerCoreState[T](asynchronizer: AsynchronizerCore[T])
    extends CompletedAsynchronizerCoreState[T](asynchronizer) {
  override val name: AsynchronizerConstants.AsynchronizerStateValues.Value =
    InterruptedAsynchronizerCoreState
}
