package com.actyx.challenge.web

import java.io.Closeable

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.CorsSettings
import com.actyx.challenge.CompositionRoot
import com.actyx.challenge.Main._
import com.actyx.challenge.web.api.MachineAlarmsService
import com.actyx.challenge.config.Config

class WebMain(
	root: CompositionRoot,
	config:Config) {

	implicit val system = ActorSystem()
	implicit val executor = system.dispatcher
	implicit val materializer = ActorMaterializer()


	val alarmsRoute = new MachineAlarmsService(config.serverConfig, config.envCtxt,
		                                          config.actyxParkConfig.movingAvgWindowSize)(
                                               root.machinesState).route

	val corsSettings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = true,
		                                                    allowedOrigins = HttpOriginRange.*)

	def start() : java.io.Closeable = {

		val route =
			get {
				pathEndOrSingleSlash {
					getFromResource("web/index.html")
				} ~ path("client-fastopt.js")(getFromResource("client-fastopt.js")) ~
					path("client-jsdeps.js")(getFromResource("client-jsdeps.js"))
			} ~ ch.megard.akka.http.cors.CorsDirectives.cors(corsSettings) { alarmsRoute }

		val serverBindings = Http()
			.bindAndHandle(Route.handlerFlow(route),
				              config.serverConfig.host,
				              config.serverConfig.port)
			.map { binding ⇒
				logger.info(s"Entrypoint UP on ${config.serverConfig.host}:${config.serverConfig.port}")
				binding
			}

		new Closeable {
			override def close(): Unit = {
				serverBindings.foreach(_.unbind)
				logger.info("Entrypoint DOWN")
			}
		}
	}
}
