package com.actyx.challenge.api

import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.actyx.challenge.api.MachinesEndpoint._
import com.actyx.challenge.mappings._
import com.actyx.challenge.models.Machine
import com.actyx.challenge.models.Machine.MachineID
import com.actyx.challenge.util.Frequency
import io.circe.Decoder
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.client.blaze._

import scala.concurrent.duration.FiniteDuration

class MachinesEndpoint(
  apiRoot: URL,
  machinesEndpoint: URL,
  samplingFrequency: Frequency) extends Observable[(MachineID, Machine)] {

  require(samplingFrequency.interval > 0.2,
           s"Minimal sampling frequency is 0.2Hz ( 1/(5 seconds) ). Was ${samplingFrequency.interval}")

  private lazy val client = PooledHttp1Client()
  private lazy val tickSource =
    Observable.interval(FiniteDuration(samplingFrequency.interval.toLong * 1000, TimeUnit.MILLISECONDS))

  private val machinesList: monix.eval.Task[List[MachineEndpoint]] =
    monix.eval.Task.evalAlways(client.expect[List[MachineEndpoint]](machinesEndpoint.toString).run)

  private def machineRequest(url: URL): monix.eval.Task[Machine] =
    monix.eval.Task.evalAlways(client.expect[Machine](url.toString).run)
    .onErrorHandleWith {
      case ex@org.http4s.client.UnexpectedStatus(status) if status.code == 429 =>
        machineRequest(url).delayExecution(FiniteDuration(5000L, TimeUnit.MILLISECONDS))
    }

  override def unsafeSubscribeFn(subscriber: Subscriber[(MachineID, Machine)]): Cancelable =
    Observable.fromTask(machinesList).flatMap { endpoints =>
      val requests = endpoints.map { e => e.getID -> machineRequest(e.toURL(apiRoot))}
      tickSource.flatMap { _ =>
        Observable.merge(requests.map { case (uuid, r) =>
          Observable.fromTask(r.map(uuid -> _)) }:_*)
      }
    }.subscribe(subscriber)
}

object MachinesEndpoint {
  case class MachineEndpoint(url: String) {
    //"$API_ROOT/machine/0e079d74-3fce-42c5-86e9-0a4ecc9a26c5"
    private val EndpointR = "(\\$API_ROOT)\\/machine\\/([a-zA-z0-9\\-]+)".r
    def getID: MachineID = {
      val EndpointR(_, id) = url
      UUID.fromString(id)
    }
    def toURL(apiRoot: URL): URL = new URL(apiRoot + url.drop("$API_ROOT".length))
  }

  implicit def `circeDecoder => EntityDecoder`[T : Decoder]: EntityDecoder[T] =
    jsonOf[T]

  final val MACHINES_LIST = "machines_list"
  final val ENV_SENSOR = "env_sensor"
  final val MACHINE_DETAIL = "machine_detail"
}