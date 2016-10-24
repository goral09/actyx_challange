logLevel := Level.Warn

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.11")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

resolvers += Resolver.url("heroku-sbt-plugin-releases",
	                         url("https://dl.bintray.com/heroku/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.heroku" % "sbt-heroku" % "1.0.1")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")