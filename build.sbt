import org.scalafmt.bootstrap.ScalafmtBootstrap

name := "claymore-logger-service"

version := "1.0"

scalaVersion := "2.12.2"

enablePlugins(UniversalPlugin)
enablePlugins(JavaAppPackaging)
resourceDirectory in Compile := (resourceDirectory in Compile).value

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.9",
  "io.spray" %% "spray-json" % "1.3.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.9" % Test
)

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.7.2",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

val format = TaskKey[Unit]("format", "Run format.") //this will create a new sbt task with no parameters
format := ScalafmtBootstrap.main(Seq("--non-interactive")) //this will assign a plugin's main class to the format

val formatTest = TaskKey[Unit]("formatTest", "Run formatTest.") //this will create a new sbt task with no parameters
formatTest := ScalafmtBootstrap.main(Seq("--non-interactive", "--test")) //this will assign a plugin's main class to the formatTest
