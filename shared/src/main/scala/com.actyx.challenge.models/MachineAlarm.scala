package com.actyx.challenge.models

import java.util.UUID

case class MachineAlarm(
  id: UUID,
  timestamp: Long,
  current: Double,
  currentAlert: Double,
  average: Double) extends Product with Serializable

object MachineAlarm {
  def sample = MachineAlarm(UUID.randomUUID(), scala.util.Random.nextLong(),
                             scala.util.Random.nextDouble() % 10,
	                           scala.util.Random.nextDouble() % 10,
                             scala.util.Random.nextDouble() % 10)
}
