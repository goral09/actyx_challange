package com.actyx.challenge

import java.net.URL

import com.actyx.challenge.api.MachinesEndpoint
import com.actyx.challenge.util.Frequency
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observer

object Main extends App {
  val apiRoot = new URL("http://machinepark.actyx.io/api/v1")
  val machinesEndpoint = new URL("http://machinepark.actyx.io/api/v1/machines")
  val freq     = Frequency(0.21)
  val machines = new MachinesEndpoint(apiRoot, machinesEndpoint, freq)

  machines
    .unsafeSubscribeFn(Observer.dump(""))
}
