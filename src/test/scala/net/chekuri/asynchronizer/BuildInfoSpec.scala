package net.chekuri.asynchronizer

import org.scalatest.flatspec.AnyFlatSpec
import build.BuildInfo

class BuildInfoSpec extends AnyFlatSpec {
  "BuildInfo" should "correctly fetch build name" in {
    val actual: String = BuildInfo.name
    val expected: String = "asynchronizer"
    assert(actual == expected)
  }

  "BuildInfo" should "correctly fetch build version" in {
    val actual: String = BuildInfo.version
    val expected: String = "0.6"
    assert(actual == expected)
  }

  "BuildInfo" should "correctly fetch sbt version" in {
    val actual: String = BuildInfo.sbtVersion
    val expected: String = "1.4.0"
    assert(actual == expected)
  }
}
