import sbt._

object Dependencies {
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

  lazy val loggingDependencies = Seq(
                                      "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0")

  lazy val dependencies = monixDependencies ++
    circeDependencies ++
    akkaDependencies  ++
    http4sDependencies ++
    testDependencies ++
    dateTimeDependencies

}