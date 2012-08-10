organization := "me.lessis"

name := "jp-liftson"

version := "0.1.0-SNAPSHOT"

description := "LiftJson bindings for jp json path parsing"

homepage := Some(url("https://github.com/softprops/jp"))

libraryDependencies <++= scalaVersion( sv =>
  Seq(sv.split("[.-]").toList match {
    case "2" :: "9" :: _ =>
      "net.liftweb" % "lift-json_2.9.1" % "2.4"
    case _ => "net.liftweb" %% "lift-json" % "2.4"
  })
)
