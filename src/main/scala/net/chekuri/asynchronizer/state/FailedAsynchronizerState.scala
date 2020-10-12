package net.chekuri.asynchronizer.state

import net.chekuri.asynchronizer.Asynchronizer

class FailedAsynchronizerState[T](asynchronizer: Asynchronizer[T])
    extends CompletedAsynchronizerState[T](asynchronizer) {
  override val name: String = "FailedAsynchronizerState"
}
