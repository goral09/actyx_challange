package com.actyx.challenge.api

import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import com.actyx.challenge.config.Config
import com.actyx.challenge.models.MachineAlarm
import de.heikoseeberger.akkasse.{EventStreamMarshalling, ServerSentEvent, _}
import EventStreamMarshalling._
import io.circe.Encoder
import com.actyx.challenge.mappings._
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

class MachineAlarmsService(config: Config.ServerConfig)(
	alarms: Observable[MachineAlarm]) {

	val route =
		pathPrefix("api" / "v1") {
			pathPrefix("alarms") {
				pathEndOrSingleSlash {
					get {
						complete {
							Source.fromPublisher(alarms.toReactivePublisher)
								.map(alarm ⇒ ServerSentEvent(Encoder[MachineAlarm].apply(alarm).toString))
								.keepAlive(2.seconds, () ⇒ ServerSentEvent.Heartbeat)
						}
					}
				}
			}
		}
}
