package client

import com.actyx.challenge.models.MachineAlarm
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive.Observer

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic._

final class AlarmList(element: String, listSize: Int = 100)
  extends Observer[MachineAlarm] {

  private var list: js.Dynamic = _

  private def initList(): js.Dynamic =
	  global.jQuery(s"#$element").append("<ul></ul>").find("ul")

	private def serialize(alarm: MachineAlarm):js.Dynamic =
		global.jQuery(s"<li>${alarm.toString}</li>")

	private def appendAlarm(alarm: js.Dynamic)(list: js.Dynamic) =
		alarm appendTo list

	private def prependAlarm(alarm: js.Dynamic)(list: js.Dynamic) =
		alarm prependTo list

	private def exceedsLimitSize(list: js.Dynamic): Boolean =
		global.jQuery(s"#$element ul").children().length.asInstanceOf[Int] > listSize

	private def dropLastElement(): Unit =
		global.jQuery(s"#$element ul li:last").remove()

  override def onNext(alarm: MachineAlarm): Future[Ack] = {
    if(list == null)
      list = initList()
		prependAlarm(serialize(alarm))(list)
	  if(exceedsLimitSize(list))
			dropLastElement()

    Continue
  }

  override def onComplete(): Unit = ()

  override def onError(ex: Throwable): Unit = {
    Utils.log(s"ERROR: $ex")
  }
}
