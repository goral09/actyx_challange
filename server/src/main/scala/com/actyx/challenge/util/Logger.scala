package com.actyx.challenge.util

import org.slf4j.LoggerFactory

trait Logger {
	def logger = LoggerFactory.getLogger(this.getClass())
}

object Logger extends Logger {
	def debug(msg: String) = logger.debug(msg)
	def info(msg: String) = logger.info(msg)
	def error(msg: String) = logger.error(msg)
	def warn(msg: String) = logger.warn(msg)
	def trace(msg: String) = logger.trace(msg)
}
