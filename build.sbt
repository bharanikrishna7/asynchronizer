name := "asynchronizer"
version := "0.6"
organization := "net.chekuri"
developers := List(
  Developer(
    "bharanikrishna7",
    "Venkata Bharani Krishna Chekuri",
    "bharanikrishna7@gmail.com",
    url("https://github.com/bharanikrishna7/")
  )
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    scalaVersion := scala21303,
    // build and test against other supported scala versions
    crossScalaVersions := supportedScalaVersions,
    // build info plugin settings
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "build",
    // logging libraries
    libraryDependencies ++= Seq(log4j_api_library, log4j_core_library),
    // random data generator libraries (TEST ONLY)
    libraryDependencies ++= Seq(random_data_generator_library),
    // scalatest libraries (test only) + unit test framework
    libraryDependencies ++= Seq(scalactic_library, scalatest_library),
    // set code reformat on compile to true
    scalafmtOnCompile := true,
    // disable generating jar on compile
    Compile / packageBin / publishArtifact := false,
    // compiled jar file name
    assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}_${version.value}.jar",
    // disable `package` artifact in assembly generated during compile process
    artifact in (Compile, assembly) := {
      (artifact in (Compile, assembly)).value
    },
    // add all necessary artifacts
    addArtifact(artifact in (Compile, assembly), assembly)
  )

// library versions
val log4j_version: String = "2.13.3"
val random_data_generator_version: String = "2.9"
val scalatest_version: String = "3.2.2"

// library modules
val log4j_api_library: ModuleID = "org.apache.logging.log4j" % "log4j-api" % log4j_version
val log4j_core_library: ModuleID = "org.apache.logging.log4j" % "log4j-core" % log4j_version
val random_data_generator_library: ModuleID = "com.danielasfregola" %% "random-data-generator" % random_data_generator_version % "test"
val scalactic_library: ModuleID = "org.scalactic" %% "scalactic" % scalatest_version
val scalatest_library: ModuleID = "org.scalatest" %% "scalatest" % scalatest_version % "test"

// x-compile settings
lazy val scala21303 = "2.13.3"
lazy val scala21300 = "2.13.0"
lazy val scala21210 = "2.12.10"
lazy val scala21209 = "2.12.9"
lazy val scala21208 = "2.12.8"
lazy val supportedScalaVersions = List(scala21303, scala21300, scala21210, scala21209, scala21208)

// Assembly Strategy when encountering different files with same name.
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "LICENSE")               => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF")           => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)                 => MergeStrategy.discard
  case "application.conf"                            => MergeStrategy.concat
  case "reference.conf"                              => MergeStrategy.concat
  case "play/reference-overrides.conf"               => MergeStrategy.concat
  case _                                             => MergeStrategy.first
}