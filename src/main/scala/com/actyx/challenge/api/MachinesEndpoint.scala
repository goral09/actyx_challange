package com.actyx.challenge.api

import java.net.URL
import java.util.concurrent.TimeUnit

import com.actyx.challenge.api.MachinesEndpoint._
import com.actyx.challenge.models.Machine
import com.actyx.challenge.util.Frequency
import com.actyx.challenge.mappings._
import io.circe.{Decoder, Json}
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe.jsonOf

import scala.concurrent.duration.FiniteDuration
import scalaz.{EitherT, \/}
import scalaz.concurrent.Task

class MachinesEndpoint(
  apiRoot: URL,
  machinesEndpoint: URL,
  samplingFrequency: Frequency) extends Observable[Machine] {

  require(samplingFrequency.interval > 0.2,
           s"Minimal sampling frequency is 0.2Hz ( 1/(5 seconds) ). Was ${samplingFrequency.interval}")

  private lazy val client = PooledHttp1Client()
  private lazy val tickSource =
    Observable.interval(FiniteDuration(samplingFrequency.interval.toLong * 1000, TimeUnit.MILLISECONDS))

  private val machinesList: Task[List[MachineEndpoint]] =
    client.expect[List[MachineEndpoint]](machinesEndpoint.toString)

  private def machineRequest(url: URL): Task[Machine] =
    client.expect[Machine](url.toString)

  override def unsafeSubscribeFn(subscriber: Subscriber[Machine]): Cancelable =
    tickSource.flatMap { _ =>
      Observable.fromTask(monix.eval.Task.evalAlways(machinesList.run))
        .map(_.map(_.toURL(apiRoot)))
        .flatMap { endpoints =>
          val requests = endpoints.map(machineRequest)
          Observable.merge(requests.map(r => Observable.fromTask(monix.eval.Task.evalAlways(r.run))):_*)
        }
    }.subscribe(subscriber)
}

object MachinesEndpoint {
  case class MachineEndpoint(url: String) {
    def toURL(apiRoot: URL): URL = new URL(apiRoot + url.drop("$API_ROOT".length))
  }
  implicit def `circeDecoder => EntityDecoder`[T : Decoder]: EntityDecoder[T] = jsonOf[T]
  final val MACHINES_LIST = "machines_list"
  final val ENV_SENSOR = "env_sensor"
  final val MACHINE_DETAIL = "machine_detail"
}
