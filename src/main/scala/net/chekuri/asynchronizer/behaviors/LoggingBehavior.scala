package net.chekuri.asynchronizer.behaviors

import org.apache.logging.log4j.{LogManager, Logger}

/** Trait (or) Interface having logger object.
  */
trait LoggingBehavior {
  protected val logger: Logger = LogManager.getLogger(this.getClass)
}
