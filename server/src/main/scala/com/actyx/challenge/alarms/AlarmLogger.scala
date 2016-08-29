package com.actyx.challenge.alarms

import com.actyx.challenge.models.MachineAlarm

trait AlarmLogger {
  def log(alarm: MachineAlarm): Unit
}

object AlarmLogger {

  object StdOutLogger
    extends AlarmLogger {
    override def log(alarm: MachineAlarm): Unit = {
      val msg = "Machine %35s exceeded current alarm level: %2.3f / %2.3f".format(alarm.id, alarm.current, alarm.currentAlert)
      println(msg)
    }
  }

}