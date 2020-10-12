package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.Asynchronizer

class InterruptedAsynchronizerState[T](asynchronizer: Asynchronizer[T])
    extends CompletedAsynchronizerState[T](asynchronizer) {
  override val name: String = "InterruptedAsynchronizerState"
}
