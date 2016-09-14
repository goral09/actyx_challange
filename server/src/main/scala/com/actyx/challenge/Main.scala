package com.actyx.challenge

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actyx.challenge.api.MachineAlarmsService
import com.actyx.challenge.util.Logger

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{getClass ⇒ _, _}
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.CorsSettings
import com.actyx.challenge.config.{Config, TypeSafeConfig}
import com.actyx.challenge.models.MachineAlarm
import com.typesafe.config.ConfigFactory

object Main extends App with Logger {
	val typesafeConfig = ConfigFactory.load
	implicit val config = Config()(new TypeSafeConfig(typesafeConfig))

	val compositionRoot = CompositionRoot()

	val machines = compositionRoot.machinesState

	val machineAlarms = for {
		(mId, m) ← machines
		if m.current > m.currentAlert
	} yield MachineAlarm(mId, m.timestamp.toDateTime.getMillis, m.current, m.currentAlert)

	implicit val system = ActorSystem()
	implicit val executor = system.dispatcher
	implicit val materializer = ActorMaterializer()

	val alarmsRoute = new MachineAlarmsService(config.serverConfig)(machineAlarms).route

	val corsSettings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = true,
		                                                    allowedOrigins = HttpOriginRange.*)

	val route =
		get {
			pathEndOrSingleSlash {
				getFromResource("web/index.html")
			} ~ path("client-fastopt.js")(getFromResource("client-fastopt.js")) ~
				  path("client-jsdeps.js")(getFromResource("client-jsdeps.js"))
		} ~ ch.megard.akka.http.cors.CorsDirectives.cors(corsSettings) { alarmsRoute }


	val serverBindings = Http()
	  .bindAndHandle(Route.handlerFlow(route), "0.0.0.0", config.serverConfig.port)
		.map { binding ⇒
		  logger.info("Entrypoint UP")
	    binding
		}

	sys.addShutdownHook {
		serverBindings.foreach(_.unbind)
		logger.info("Entrypoint DOWN")
	}
}
