package com.actyx.challenge

import akka.http.scaladsl.server.Directives.{getClass ⇒ _}
import com.actyx.challenge.config.{Config, TypeSafeConfig}
import com.actyx.challenge.util.Logger
import com.actyx.challenge.web.WebMain
import com.typesafe.config.ConfigFactory

object Main extends App with Logger {
	val typesafeConfig = ConfigFactory.load
	implicit val config = Config()(new TypeSafeConfig(typesafeConfig))

	implicit val compositionRoot = CompositionRoot()

	val web = new WebMain(compositionRoot, config)

	val closeableWeb = web.start()

	sys.addShutdownHook {
		closeableWeb.close()
	}
}
