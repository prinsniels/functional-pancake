import sbt._

object Dependencies {

  case object scalatest {
    val core =
      "org.scalatest" %% "scalatest" % "3.2.8"
  }

  case object cats {
    val core = "org.typelevel" %% "cats-core" % "2.3.0"
    val effect = "org.typelevel" %% "cats-effect" % "3.1.0"
  }

}
