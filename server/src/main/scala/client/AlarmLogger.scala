package client

import com.actyx.challenge.models.MachineAlarm
import com.actyx.challenge.util.Logger

trait AlarmLogger
  extends Logger {
  def log(alarm: MachineAlarm): Unit
}

object AlarmLogger {

  object StdOutLogger
    extends AlarmLogger {
    override def log(alarm: MachineAlarm): Unit = {
      val msg = "Machine %35s exceeded current alarm level: %2.3f . Average %2.3f".format(alarm.id, alarm.current, alarm.average)
      logger.info(msg)
    }
  }

}