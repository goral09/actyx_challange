package client

import java.net.URI

import com.actyx.challenge.models.MachineAlarm
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import monix.reactive.OverflowStrategy.DropNew
import org.scalajs.dom

import scala.scalajs.js.JSApp

object Sample extends JSApp {
  override def main(): Unit = {
	  val protocol = dom.window.location.protocol
	  val host = dom.window.location.host
	  val alarms: Observable[MachineAlarm] =
	    new AlarmConsumer(s"$protocol//$host/api/v1/alarms", DropNew(1000))

	  alarms
		  .subscribe(new AlarmList("alarms-list"))

  }

}
