package com.actyx.challenge.models

import java.util.UUID

import org.joda.time.LocalDateTime

case class MachineAlarm(
  id: UUID,
  timestamp: LocalDateTime,
  current: Double,
  currentAlert: Double) extends Product with Serializable
