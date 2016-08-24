package com.actyx.challenge.api

import java.net.URL
import java.nio.charset.Charset

import com.actyx.challenge.api.MachinesEndpoint.MachineEndpoint
import com.actyx.challenge.mappings._
import org.http4s.Response
import org.scalatest.{FunSuite, Matchers}
import scodec.bits.ByteVector

class MachinesEndpointEntityBodyDecoderSuite
  extends FunSuite
  with Matchers {

  test("Should properly decode HTTP response, containing list of machines endpoints, to case class-es.") {
    val endpoints   = List(
      "$API_ROOT/machine/0e079d74-3fce-42c5-86e9-0a4ecc9a26c5",
      "$API_ROOT/machine/e9c8ae10-a943-49e0-979e-71d125132c64",
      "$API_ROOT/machine/95134efd-a6a2-4eb5-9b68-c1bfef18b66c")

    val responseStr = endpoints.mkString("[\"", "\",\"", "\"]")

    val response = Response(body = scalaz.stream.Process(ByteVector.view(responseStr.getBytes(Charset.forName("UTF-8")))))

    // XXX: could probably make use of `AsyncFunSuite` and the fact that `scalaz.stream.Process` represents async computation
    val decoded  = MachinesEndpoint.`circeDecoder => EntityDecoder`[List[MachineEndpoint]].decode(response, strict = true).run.run

    assert(decoded.isRight, s"$decoded wasn't decoded correctly.")

    val expected = endpoints.map(MachineEndpoint)

    assert(decoded.exists(_ === expected))
  }

  test("Given API_ROOT return proper URLs for machines.") {
    val API_ROOT = new URL("http://foo.bar")

    val endpoints = List(
      "$API_ROOT/machine/0e079d74-3fce-42c5-86e9-0a4ecc9a26c5",
      "$API_ROOT/machine/e9c8ae10-a943-49e0-979e-71d125132c64",
      "$API_ROOT/machine/95134efd-a6a2-4eb5-9b68-c1bfef18b66c")
      .map(MachineEndpoint)
      .map(_.toURL(API_ROOT))

    noException should be thrownBy endpoints
    assert(endpoints.forall(url => url.toString.startsWith("http://foo.bar")))
  }
}
