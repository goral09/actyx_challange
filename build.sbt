import sbt.Project.projectToRef
import sbt.Keys._
import Dependencies._

version := "1.0"

lazy val scalaV = "2.11.8"

lazy val commonSettings = Seq(
  name := "actyx-challenge",
  version := "0.1.0",
  scalaVersion := scalaV)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file ("shared"))
  .settings(
     scalaVersion := scalaV,
     libraryDependencies ++= dateTimeDependencies)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val client = (project in file ("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := scalaV,
    persistLauncher := true,
    persistLauncher in Test := false,
    scalaJSUseRhino in Global := false,
    libraryDependencies ++=
      dateTimeDependencies ++ Seq(
        "org.scala-js"  %%% "scalajs-dom" % "0.9.0",
        "io.monix"      %%% "monix"       % "2.0-RC9"))
  .dependsOn(sharedJs)

lazy val server = (project in file("server"))
  .enablePlugins(JavaServerAppPackaging)
  .settings(commonSettings:_*)
  .settings(
    resolvers ++= Seq(
      "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
			Resolver.bintrayRepo("hseeberger", "maven"),
      Resolver.sonatypeRepo("snapshots")),
    libraryDependencies ++= dependencies,
    libraryDependencies ++= Seq("org.webjars" % "jquery" % "1.12.3"),
		 // Heroku specific
		 herokuAppName in Compile := "actyx-challenge",
		 herokuSkipSubProjects in Compile := false,
    (resourceGenerators in Compile) <+= (fastOptJS in Compile in client)
             .map(f => Seq(f.data)),
    watchSources <++= (watchSources in client))
  .aggregate(client)
  .dependsOn(sharedJvm)

lazy val root =
  (project in file("."))
		.aggregate(client, server)

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value