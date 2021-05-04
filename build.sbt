ThisBuild / organization := "com.github.prinsniels"
ThisBuild / scalaVersion := "3.0.0-RC3"

ThisBuild / scalacOptions := Seq("-unchecked", "-deprecation", "-language:higherKinds")

lazy val `playground` =
  project
    .in(file("playground"))
    .settings(       
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core" % "2.3.1",
        "org.typelevel" %% "cats-effect" % "2.3.1"
      ))

