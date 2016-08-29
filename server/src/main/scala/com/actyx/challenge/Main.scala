package com.actyx.challenge

import java.net.URL

import com.actyx.challenge.api.{MachinesEndpoint, MachinesObserver}
import com.actyx.challenge.util.Frequency
import monix.execution.Scheduler.Implicits.global

object Main extends App {
  // TODO: load from config file
  val apiRoot = new URL("http://machinepark.actyx.io/api/v1")
  val freq     = Frequency(0.21)
  // TODO: `discover` the endpoints
  val machinesEndpoint = new URL("http://machinepark.actyx.io/api/v1/machines")
  val machines = new MachinesEndpoint(apiRoot, machinesEndpoint, freq)

  machines
    .unsafeSubscribeFn(MachinesObserver())
}
