import sbt._

object Build extends sbt.Build {
  lazy val jp = Project("jp", file("."))
  lazy val liftJson = Project("jp-liftjson", file("liftjson")) dependsOn(jp)
}
