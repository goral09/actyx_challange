package client

import java.net.URI

import com.actyx.challenge.models.MachineAlarm
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import monix.reactive.OverflowStrategy.DropNew

import scala.scalajs.js.JSApp

object Sample extends JSApp {
  override def main(): Unit = {
	  // TODO: get from config
	  val alarms: Observable[MachineAlarm] =
	    new AlarmConsumer("http://localhost:8888/api/v1/alarms", DropNew(1000))

	  alarms
		  .subscribe(new AlarmList("alarms-list"))

  }

}
