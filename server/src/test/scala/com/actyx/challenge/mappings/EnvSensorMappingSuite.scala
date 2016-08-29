package com.actyx.challenge.mappings

import com.actyx.challenge.models._
import io.circe.{Decoder, Json}
import org.joda.time.LocalDateTime
import org.scalatest.FunSuite

/**
  * Created by mateusz on 23.08.16.
  */
class EnvSensorMappingSuite
  extends FunSuite {

  test("Properly parser environment sensor readings") {
    val input =
      """
        |{"pressure":["2016-08-23T09:48:00",1004.46],"temperature":["2016-08-23T09:48:00",21.579999999999998],"humidity":["2016-08-23T09:48:00",87.060000000000002]}
      """.stripMargin

    val expected = EnvSensor(
      SensorReading[PressureSensor](LocalDateTime.parse("2016-08-23T09:48:00"),1004.46),
      SensorReading[TemperatureSensor](LocalDateTime.parse("2016-08-23T09:48:00"),21.579999999999998),
      SensorReading[HumiditySensor](LocalDateTime.parse("2016-08-23T09:48:00"),87.060000000000002)
    )

    val decoded = io.circe.parser.decode[EnvSensor](input)

    assert(decoded.exists(_ === expected))
  }

}
