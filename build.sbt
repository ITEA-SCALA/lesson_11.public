name := "Lesson-11"
version := "1.0.0-SNAPSHOT"

scalaVersion := "2.13.6"

val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.8"
val monocleVersion = "3.0.0"
val setLog4jDebug = sys.props("log4j2.debug") = "true"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",

  "com.typesafe" % "config" % "1.4.1",
  "com.beachape" %% "enumeratum" % "1.7.0",

  "io.bfil" %% "automapper" % "0.7.0",

  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,

  // only for Scala 2.13
  "dev.optics" %% "monocle-core"  % monocleVersion,
  "dev.optics" %% "monocle-macro" % monocleVersion,

  "com.softwaremill.macwire" %% "macros" % "2.4.1" % "provided",

  "org.scalatest" %% "scalatest" % "3.3.0-SNAP3" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)
