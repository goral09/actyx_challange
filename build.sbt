import sbt.Project.projectToRef
import sbt.Keys._
import Dependencies._
import complete.DefaultParsers._

version := "1.0"

lazy val scalaV = "2.11.8"
lazy val projectName   = "actyx-challange"

lazy val commonSettings = Seq(
  name := projectName,
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
    scalaVersion      := scalaV,
    persistLauncher   := true,
    persistLauncher in Test   := false,
    scalaJSUseRhino in Global := false,
    libraryDependencies ++=
      dateTimeDependencies ++ Seq(
        "org.scala-js"  %%% "scalajs-dom" % "0.9.0",
        "io.monix"      %%% "monix"       % "2.0-RC9"))
  .dependsOn(sharedJs)


lazy val server = (project in file("server"))
  .enablePlugins(JavaServerAppPackaging, sbtdocker.DockerPlugin)
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
		 herokuSkipSubProjects  in Compile  :=  false,
    (resourceGenerators     in Compile) <+= (fastOptJS in Compile in client).map(f => Seq(f.data)),
    watchSources <++= (watchSources in client),
    // sbt docker
    dockerfile in docker := {
      val appDir    = stage.value
      val targetDir = "/app"

      new Dockerfile {
        from("java")
        expose(8080)
        env("env" → "test")
        copy(appDir, targetDir)
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
      }
    },
    buildOptions in docker := BuildOptions(
      cache                         = false,
      removeIntermediateContainers  = BuildOptions.Remove.Always,
      pullBaseImage                 = BuildOptions.Pull.IfMissing))
  .aggregate(client)
  .dependsOn(sharedJvm)

executableScriptName := projectName

lazy val root =
  (project in file("."))
		.aggregate(client, server)

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

// Exclude files that we have to access from the JAR during packaging:
//  - property files so we can change settings
mappings in (Compile, packageBin) ~= { (ms: Seq[(File, String)]) =>
  ms filter { case (file, toPath) =>
    !toPath.endsWith(".properties")       &&
    !toPath.endsWith("application.conf")  &&
    !toPath.endsWith("log4j2.xml")
  }
}