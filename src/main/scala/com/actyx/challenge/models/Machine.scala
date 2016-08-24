package com.actyx.challenge.models

import io.circe.parser
import org.joda.time.LocalDateTime

case class Machine(
  name: String,
  `type`: String,
  state: String,
  location: Machine.Location,
  timestamp: LocalDateTime,
  current: Double,
  current_alert: Double)

object Machine {
  case class Location(lat: Double, long: Double)
  def fromString(in: String): Either[io.circe.Error, Machine] =
    parser.decode(in)(com.actyx.challenge.mappings.decodeMachine).toEither
}