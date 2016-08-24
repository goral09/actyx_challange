package com.actyx.challenge.models

import org.joda.time.LocalDateTime

case class EnvSensor(
  pressure: SensorReading[PressureSensor],
  temperature: SensorReading[TemperatureSensor],
  humidity: SensorReading[HumiditySensor]
)

sealed trait SensorType extends Product with Serializable
sealed trait PressureSensor extends SensorType
sealed trait TemperatureSensor extends SensorType
sealed trait HumiditySensor extends SensorType

case class SensorReading[T](timestamp: LocalDateTime,reading: Double)