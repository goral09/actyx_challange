import sbt._

object Dependencies {
  lazy val circeDependencies = Seq(
		"io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
    ).map(_ % VersionOf.circe)

  lazy val monixDependencies = Seq(
    "io.monix" %% "monix" % VersionOf.monix)

  lazy val `akka-http` = Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % VersionOf.akka,
    "com.typesafe.akka" %% "akka-http-core"         % VersionOf.akka)

  lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % VersionOf.`scala-test` % "test")

  lazy val http4sDependencies = Seq(
		"org.http4s" %% "http4s-dsl",
		"org.http4s" %% "http4s-blaze-client",
		"org.http4s" %% "http4s-circe"
	).map(_ % VersionOf.`http4s`)

  lazy val dateTimeDependencies = Seq(
    "joda-time" % "joda-time" % VersionOf.`joda-time`)

  lazy val loggingDependencies = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % VersionOf.`scala-logging`)

  lazy val `typesafe-config` = Seq(
    "com.typesafe" % "config" % VersionOf.`typesafe-config`)

  lazy val dependencies = monixDependencies ++
    circeDependencies ++
    `akka-http`  ++
    http4sDependencies ++
    testDependencies ++
    dateTimeDependencies

}