package client

import java.util.UUID

import com.actyx.challenge.api.MachinesObserver.MachinesState
import org.scalatest.FunSuite

class MachinesStateRegistrySuite
  extends FunSuite {

  test("State tracking should raise an alarm for current above alarm level.") {
    val ms = new MachinesState()
    val mid = UUID.randomUUID()
    val alarm_level = 20.0
    val no_alarm_curr = 10.0
    val alarm_curr = 22.0

    ms.update(mid, no_alarm_curr, alarm_level)
    assert(ms.update(mid, alarm_curr, alarm_level)._2 === Some((mid, alarm_curr, alarm_level)))
  }

}
