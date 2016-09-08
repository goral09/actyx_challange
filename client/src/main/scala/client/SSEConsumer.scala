package client

import monix.execution.Ack.Stop
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive.observers.Subscriber
import monix.reactive.{Observable, OverflowStrategy}
import org.scalajs.dom.raw.{Event, EventSource, MessageEvent}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.js.JSON
import scala.util.control.NonFatal

final class SSEConsumer private(uri: String,  os: OverflowStrategy.Synchronous[String])
  extends Observable[String] { self =>

  private val events: Observable[String] =
    Observable.create[String](os) { downstream =>
      def closeConnection(es: EventSource): Unit = {
        Utils.log("Closing SSE connection.")
        es.close()
      }

      try {
        Utils.log(s"Creating SSE events source from $uri")
        val es = new EventSource(uri,JSON.parse("{ \"withCredentials\" : \"true\"}"))

        es.onopen = (e: Event) => {
          Utils.log(s"Established connection with $uri")
        }
        es.onmessage = (e: MessageEvent) => {
	        val ack = downstream.onNext(e.data.asInstanceOf[String])
          if(ack == Stop) closeConnection(es)
        }
        es.onerror = (e: Event) => ()

        Cancelable(() => closeConnection(es))
      } catch {
        case NonFatal(ex) =>
          downstream.onError(ex)
          Cancelable.empty
      }
    }

  override def unsafeSubscribeFn(subscriber: Subscriber[String]): Cancelable =
    events
      .unsafeSubscribeFn(new Subscriber[String] {
        override implicit def scheduler: Scheduler = subscriber.scheduler

        def onError(ex: Throwable): Unit = {
          scheduler.reportFailure(ex)
          // Retry connection in a couple of secs
          self
            .delaySubscription(3.seconds)
            .unsafeSubscribeFn(subscriber)
        }

        def onComplete(): Unit = {
          // Retry connection in a couple of secs
          self
            .delaySubscription(3.seconds)
            .unsafeSubscribeFn(subscriber)
        }

        override def onNext(elem: String): Future[Ack] =
          subscriber.onNext(elem)
      })
}

object SSEConsumer {
  def apply(url: String, os: OverflowStrategy.Synchronous[String]) =
    new SSEConsumer(url, os)

}
