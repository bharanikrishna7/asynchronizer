package net.chekuri.asynchronizer.behaviors

import java.util.UUID

trait GuidBehavior {
  def generateGuid: String = UUID.randomUUID().toString
}
