import sbt.Project.projectToRef

version := "1.0"

lazy val scalaV = "2.11.8"

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.4.1")

lazy val monixDependencies = Seq(
  "io.monix" %% "monix" % "2.0-RC10"
)

lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9-RC2"
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test")

lazy val http4sDependencies = Seq(
   "org.http4s" %% "http4s-dsl",
   "org.http4s" %% "http4s-blaze-client",
   "org.http4s" %% "http4s-circe"
).map(_ % "0.14.2")

lazy val dateTimeDependencies = Seq(
  "joda-time" % "joda-time" % "2.9.4")

lazy val dependencies = monixDependencies ++
  circeDependencies ++
  akkaDependencies  ++
  http4sDependencies ++
  testDependencies ++
  dateTimeDependencies

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
  .dependsOn(sharedJs)
  .settings(
    scalaVersion := scalaV,
    persistLauncher := true,
    persistLauncher in Test := false,
    //    sourceMapsDirectories += sharedJs.base / "..",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0",
      "io.monix" %%% "monix" % "2.0-RC9"))

lazy val server = (project in file("server"))
  .settings(commonSettings:_*)
  .settings(
    resolvers ++= Seq(
      "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")),
    libraryDependencies ++= dependencies)
  .aggregate(client)
  .dependsOn(projectToRef(sharedJvm))
