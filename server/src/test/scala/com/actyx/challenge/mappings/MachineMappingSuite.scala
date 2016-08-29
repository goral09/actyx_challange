package com.actyx.challenge.mappings

import com.actyx.challenge.models.Machine
import org.joda.time.LocalDateTime
import org.scalatest.FunSuite

class MachineMappingSuite
  extends FunSuite {

  test("Json representing Machine should be properly parsed") {
    val js =
      """
        |{
        |"name":"DMG NTX 2000 (MAPS) [#23]",
        |"type":"lathe",
        |"state":"working",
        |"location":"0.0,0.0",
        |"timestamp":"2015-11-13T16:04:53.128550",
        |"current":14.42,
        |"current_alert":18.0
        |}
      """.stripMargin

    val expected = Machine("DMG NTX 2000 (MAPS) [#23]", "lathe", "working",
                            Machine.Location(0.0, 0.0), LocalDateTime.parse("2015-11-13T16:04:53.128550"),
                            14.42, 18.0)
    val got = Machine.fromString(js)
    assert(got.isRight, s"$got should be parsed correctly.")
    assert(Machine.fromString(js).right.get === expected)
  }
}
