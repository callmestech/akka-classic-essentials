import Dependencies._

val scala213Version = "2.13.12"

ThisBuild / crossScalaVersions := List(scala213Version)
// sbt-github-actions defaults to using JDK 8 for testing and publishing.
// The following adds JDK 17 for testing.
ThisBuild / githubWorkflowJavaVersions := List(JavaSpec.temurin("17"))
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Sbt(
  List("Test/compile"),
  name = Some("Compile tests")
)
ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Sbt(
  List("scalafmtCheckAll", "scalafmtSbtCheck"),
  name = Some("Check formatting")
)

lazy val root = (project in file("."))
  .settings(
    name := "akka-classic-essentials",
    version := "0.1.0",
    scalaVersion := scala213Version,
    libraryDependencies ++= List(
      Akka.Actor,
      Akka.TestKit,
      TestDeps.ScalaTest
    )
  )
