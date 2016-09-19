package com.actyx.challenge

import scala.concurrent.duration.Duration.Infinite
import scala.concurrent.duration.{Duration, FiniteDuration}

package object util {
	implicit class StringOps(val s: String) extends AnyVal {
		def toFiniteDuration: FiniteDuration =
			Duration(s) match {
				case _: Infinite => throw new IllegalArgumentException("Moving average window size cannot be infinite.")
				case d: FiniteDuration => d
			}
	}
}