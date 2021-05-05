import Dependencies._

ThisBuild / scalaVersion := "2.13.3"

ThisBuild / scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-language:higherKinds"
)

lazy val `playground` =
  project
    .in(file("playground"))
    .settings(
      name := "playground",
      libraryDependencies := Seq(
        Dependencies.cats.core,
        Dependencies.cats.effect
      )
    )

lazy val root =
  project
    .in(file("."))
    .settings(
      name := "pancake",
      organization := "com.github.prinsniels",
        libraryDependencies := Seq (
          Dependencies.scalatest.core % Test
        )
    )
