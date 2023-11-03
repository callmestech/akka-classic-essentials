import sbt._

object Dependencies {
  object Akka {
    val version = "2.8.5"

    val Actor = "com.typesafe.akka" %% "akka-actor" % version
    val TestKit = "com.typesafe.akka" %% "akka-testkit" % version
  }

  object TestDeps {
    val version = "3.2.17"

    val ScalaTest = "org.scalatest" %% "scalatest" % version
  }
}
