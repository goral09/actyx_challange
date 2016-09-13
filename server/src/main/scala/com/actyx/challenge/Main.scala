package com.actyx.challenge

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.actyx.challenge.api.MachinesEndpoint
import com.actyx.challenge.util.{Frequency, Logger}
import monix.execution.Scheduler.Implicits.global
import org.reactivestreams.{Publisher, Subscriber}

import scala.concurrent.duration._
import de.heikoseeberger.akkasse.{EventStreamMarshalling, ServerSentEvent}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{getClass ⇒ _, _}
import EventStreamMarshalling._
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.CorsSettings
import client.AlarmLogger.StdOutLogger
import com.actyx.challenge.models.MachineAlarm
import io.circe.Encoder
import monix.reactive.Observable
import com.actyx.challenge.mappings._

object Main extends App with Logger {
  // TODO: load from config file
  val apiRoot = new URL("http://machinepark.actyx.io/api/v1")
  val freq     = Frequency(0.21)
  // TODO: `discover` the endpoints
  val machinesApiEndpoint = new URL("http://machinepark.actyx.io/api/v1/machines")
  val machinesState = new MachinesEndpoint(apiRoot, machinesApiEndpoint, freq)

	val machinesAlarms = for {
		(mId, m) ← machinesState
		if m.current > m.currentAlert
	} yield MachineAlarm(mId, m.timestamp.toDateTime.getMillis, m.current, m.currentAlert)

//	val consoleLogger = machinesAlarms.foreach(StdOutLogger.log)

	implicit val system = ActorSystem()
	implicit val executor = system.dispatcher
	implicit val materializer = ActorMaterializer()

	val corsSettings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = true,
		                                                    allowedOrigins = HttpOriginRange.*)

	val port = Option(System.getProperty("http.port")).map(_.toInt).getOrElse(8888)

	val route =
		get {
			pathEndOrSingleSlash {
				getFromResource("web/index.html")
			} ~ path("client-fastopt.js")(getFromResource("client-fastopt.js")) ~
				  path("client-jsdeps.js")(getFromResource("client-jsdeps.js"))
		} ~
		ch.megard.akka.http.cors.CorsDirectives.cors(corsSettings) {
			pathPrefix("api" / "v1") {
				pathPrefix("alarms") {
					pathEndOrSingleSlash {
						get {
							complete {
								Source.fromPublisher(machinesAlarms.toReactivePublisher)
									.map(alarm ⇒ ServerSentEvent(Encoder[MachineAlarm].apply(alarm).toString))
									.keepAlive(2.seconds, () ⇒ ServerSentEvent.Heartbeat)
							}
						}
					}
				}
			}
		}

	val serverBindings = Http()
	  .bindAndHandle(Route.handlerFlow(route), "0.0.0.0", port)
		.map { binding ⇒
		  logger.info("Entrypoint UP")
	    binding
		}(executor)

	sys.addShutdownHook {
		serverBindings.foreach(_.unbind)(executor)
//		consoleLogger.cancel()
		logger.info("Entrypoint DOWN")
	}
}
