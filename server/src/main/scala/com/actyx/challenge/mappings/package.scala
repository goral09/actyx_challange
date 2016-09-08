package com.actyx.challenge

import java.net.URI

import com.actyx.challenge.api.MachinesEndpoint.MachineEndpoint
import com.actyx.challenge.models._
import io.circe._
import io.circe.generic.semiauto._
import org.joda.time.LocalDateTime

package object mappings {
  implicit val decodeMachine: Decoder[Machine] = Decoder.instance { c =>
    for {
      name <- c.downField("name").as[String]
      tpe  <- c.downField("type").as[String]
      state <- c.downField("state").as[String]
      loc <- c.downField("location").as[String].map { t =>
        val loc = t.split(",").map(_.toDouble).take(2)
        Machine.Location(loc(0), loc(1))}
      tmstmp <- c.downField("timestamp").as[LocalDateTime](commons.localDateTimeDecoder)
      current <- c.downField("current").as[Double]
      current_alert <- c.downField("current_alert").as[Double]
    } yield Machine(name, tpe, state, loc, tmstmp, current, current_alert)
  }

  implicit def decodeSensorReading[T]: Decoder[SensorReading[T]] = Decoder.instance { c =>
    val a = c.as[Tuple2[LocalDateTime, Double]](Decoder.decodeTuple2[LocalDateTime, Double](commons.localDateTimeDecoder, Decoder.decodeDouble))
    a.map { case (timestamp, value) => SensorReading[T](timestamp, value)}
  }

  implicit val decodeEnvSensor: Decoder[EnvSensor] = Decoder.instance { c =>
    for {
      pressure <- c.downField("pressure").as[SensorReading[PressureSensor]]
      temperature <- c.downField("temperature").as[SensorReading[TemperatureSensor]]
      humidity <- c.downField("humidity").as[SensorReading[HumiditySensor]]
    } yield EnvSensor(pressure, temperature, humidity)
  }

  implicit val decoderMachinesEndpoint: Decoder[MachineEndpoint] = Decoder[String].map(MachineEndpoint)
  implicit val decodeListMachines: Decoder[List[MachineEndpoint]] = Decoder[Set[MachineEndpoint]].map(_.toList)
  implicit val encoderMachineAlarm: Encoder[MachineAlarm] = io.circe.generic.semiauto.deriveEncoder[MachineAlarm]
  implicit val decoderMachineAlarm: Decoder[MachineAlarm] = io.circe.generic.semiauto.deriveDecoder[MachineAlarm]
}

object commons {
  implicit val localDateTimeDecoder = Decoder[String].map(LocalDateTime.parse)
  implicit val uriDecoder = Decoder[String].map(ip => new URI(ip))
}