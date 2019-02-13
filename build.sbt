lazy val root = (project in file("."))
  .settings(
     name := "sbt-acceptance-plugin",
     organization := "name.mtkachev",
     description := "run tests from unmanaged libs to provide additional checks for students",
     version := "0.1",
     sbtPlugin := true,
     scalaVersion := "2.12.6"
   )