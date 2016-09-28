package com.actyx.challenge.util

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

case class Frequency(interval: Double) {
	def asFiniteDuration: FiniteDuration =
		FiniteDuration((1 / interval).toLong * 1000, TimeUnit.MILLISECONDS)
}