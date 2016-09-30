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

	lazy val `akka-cors` = Seq(
    "ch.megard" %% "akka-http-cors" % VersionOf.`akka-cors`)

	lazy val `akka-actor` = Seq(
		"com.typesafe.akka" %% "akka-actor" % VersionOf.akka,
		"com.typesafe.akka" %% "akka-slf4j" % VersionOf.akka)

	lazy val `akka-sse` = Seq(
		"de.heikoseeberger" %% "akka-sse" % VersionOf.`akka-sse`)

  lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % VersionOf.`scala-test` % "test")

  lazy val http4sDependencies = Seq(
		"org.http4s" %% "http4s-dsl",
		"org.http4s" %% "http4s-blaze-client",
		"org.http4s" %% "http4s-circe"
	).map(_ % VersionOf.`http4s`)

  lazy val dateTimeDependencies = Seq(
    "joda-time" % "joda-time" % VersionOf.`joda-time`)

	lazy val shapelessLibs = Seq(
    "com.chuusai" %% "shapeless" % VersionOf.shapeless)

	lazy val loggingLibs = Seq(
		// The SLF4J, LOG4J and ScalaLogging combination is dependent on the
		// order of deps, the exact packages, and the alignment of Venus; it may
		// collapse if you look at it wrong. Adjust with care and reload often...
		// (just watch out for libraries that override `log4j-slf4j-impl`, or
		//  bring in another package with the same purpose)

		"org.apache.logging.log4j" % "log4j-core",
		"org.apache.logging.log4j" % "log4j-api",
		"org.apache.logging.log4j" % "log4j-web",
		"org.apache.logging.log4j" % "log4j-slf4j-impl").map(_ % VersionOf.log4j) ++
	Seq(
		"com.typesafe.scala-logging" %% "scala-logging" % VersionOf.`scala-logging`,
		"com.lmax" % "disruptor" % VersionOf.disruptor) // async logging

	lazy val elasticLibs = Seq(
		"com.sksamuel.elastic4s" %% "elastic4s-core" % VersionOf.elastic4s)

  lazy val `typesafe-config` = Seq(
    "com.typesafe" % "config" % VersionOf.`typesafe-config`)

  lazy val dependencies =
	  monixDependencies ++
    circeDependencies ++
	  `akka-sse` ++
    `akka-http`  ++
    `akka-cors`  ++
    http4sDependencies ++
    testDependencies ++
		`typesafe-config` ++
	  loggingLibs ++
    dateTimeDependencies ++
		shapelessLibs ++
	  elasticLibs

}