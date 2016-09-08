package client

import java.util.UUID

import com.actyx.challenge.models.MachineAlarm
import monix.execution.Cancelable
import monix.reactive.observers.Subscriber
import monix.reactive.{Observable, OverflowStrategy}

import scala.scalajs.js.Dynamic._
import scala.util.Try

final class AlarmConsumer(uri: String, os: OverflowStrategy.Synchronous[String])
  extends Observable[MachineAlarm] { self =>

  private val source = SSEConsumer(uri, os)

  override def unsafeSubscribeFn(subscriber: Subscriber[MachineAlarm]): Cancelable =
    source
      .collect { case IsAlarm(al) => al }
      .unsafeSubscribeFn(subscriber)

  object IsAlarm {
    def unapply(s: String): Option[MachineAlarm] = {
      val json = global.JSON.parse(s)
      Try {
        val id = UUID.fromString(json.id.asInstanceOf[String])
        val timestamp = json.timestamp.asInstanceOf[Double].toLong
        val current = json.current.asInstanceOf[Double]
        val currentAlert = json.currentAlert.asInstanceOf[Double]

        MachineAlarm(id, timestamp, current, currentAlert)
      }.toOption
    }
  }
}

