package com.actyx.challenge.api

import com.actyx.challenge.api.MachinesObserver.MachinesState
import com.actyx.challenge.alarms.AlarmLogger
import com.actyx.challenge.alarms.AlarmLogger.StdOutLogger
import com.actyx.challenge.models.{Machine, MachineAlarm}
import com.actyx.challenge.models.Machine._
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive.Observer

import scala.concurrent.Future

class MachinesObserver[T <: AlarmLogger](loggers: T*)
  extends Observer[(MachineID, Machine)] {

  private val machines = new MachinesState()

  override def onNext(elem: (MachineID, Machine)): Future[Ack] = {
    val (mId, m) = elem
    val (_, alarm) = machines.update(mId, m.current, m.currentAlert)
    alarm foreach { case (id, curr, al_curr) =>
      loggers.foreach(_.log(MachineAlarm(id, m.timestamp, curr, al_curr)))
    }
    Continue
  }

  override def onError(ex: Throwable): Unit =
    ex.printStackTrace()

  override def onComplete(): Unit =
    println("onComplete")
}

object MachinesObserver {
  def apply[T <: AlarmLogger](loggers: T*) = new MachinesObserver[T](loggers:_*)
  def apply[T <: AlarmLogger](logger: T = StdOutLogger) = new MachinesObserver[T](logger)

  class MachinesState {
    type AvgCurrent = Double
    type Current = Double
    type AlarmCurrent = Double
    type MachineRegistry = Map[MachineID, (AvgCurrent, AlarmCurrent)]
    type Alarm = (MachineID, AvgCurrent, AlarmCurrent)

    private var registry: MachineRegistry = Map.empty

    def update(m: (MachineID, Current, AlarmCurrent)): (MachineRegistry, Option[Alarm]) = {
      registry = registry.updated(m._1, (m._2, m._3))
      val aO = registry.get(m._1).flatMap { case (_, alarmCurr) =>
        if (m._2 > alarmCurr)
          Some(m)
        else None
      }
      (registry, aO)
    }
  }
}