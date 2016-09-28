package com.actyx.challenge.api

import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import com.actyx.challenge.config.Config
import com.actyx.challenge.models.{Machine, MachineAlarm}
import de.heikoseeberger.akkasse.{EventStreamMarshalling, ServerSentEvent, _}
import EventStreamMarshalling._
import com.actyx.challenge.api.MachineAlarmsService.State
import io.circe.Encoder
import com.actyx.challenge.mappings._
import com.actyx.challenge.models.Machine.MachineID
import com.actyx.challenge.util.Logger
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import monix.execution.atomic.AtomicAny

import scala.concurrent.duration._

class MachineAlarmsService(
	config: Config.ServerConfig,
	movingAvgWindowSize: FiniteDuration)(
	machines: Observable[(MachineID, Machine)]) extends Logger {

	private val averages = AtomicAny[State](new State())

	val alarms = for {
		(mId, m) ← machines
		avg = averages.get.getForId(mId).getOrElse(m.current)
		if m.current > m.currentAlert
	} yield MachineAlarm(mId, m.timestamp.toDateTime.getMillis, m.current, m.currentAlert, avg)

	machines
		.bufferTimed(movingAvgWindowSize)
		.map(_.groupBy(_._1)
		      .mapValues(_.map(_._2))
		      .map { case (mId, readings) ⇒
		        mId -> MachineAlarmsService.computeAverage(readings)
		      }
    )
		.foreach(_.foreach(d ⇒ averages.get.update(d._1 → d._2)))

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

object MachineAlarmsService {
	class State(init: Map[MachineID, Double] = Map.empty) {
		private var state: Map[MachineID, Double] = init

		def update(value: (MachineID,  Double)): (State, Double) = {
			val (id, current) = value
			val c = state.getOrElse(id, current)
			val newState = state + (id → c)
			state = newState
			(new State(state), current)
		}

		def getState: Map[MachineID, Double] = state

		def getForId(id: MachineID) = state.get(id)

	}

	private val computeAverage: Seq[Machine] ⇒ Double = seq ⇒
		if   (seq.isEmpty) 0.0
		else (seq.map(_.current).sum * 1.0) / seq.length
}
