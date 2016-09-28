package com.actyx.challenge.config

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import com.actyx.challenge.util._

class Config private(
	val actyxParkConfig: Config.ActyxMachineParkAPIConfig,
	val serverConfig: Config.ServerConfig
)

object Config {
	case class ActyxMachineParkAPIConfig(
		apiRoot: String,
		reqFrequency: Double,
		movingAvgWindowSize: FiniteDuration)

	case class ServerConfig(
		host: String,
		port: Int)

	def apply()(implicit conf: TypeSafeConfig) = {
		val actyxConfig = {
			val apiRoot = conf.getSystemProperty("actyx.api.root")
			val reqFreq = conf.systemProperty("actyx.machine.frequency").map(_.toDouble).getOrElse(0.21)
			val movingAvgWindowSize = {
				val dur = conf.systemProperty("actyx.machine.averagePeriod")
				if(dur.nonEmpty) dur.get.toFiniteDuration
				else FiniteDuration(5, TimeUnit.SECONDS)
			}
			ActyxMachineParkAPIConfig(apiRoot,reqFreq, movingAvgWindowSize)
		}

		val serverConfig = {
			val host = conf.getSystemProperty("api.http.host")
			val port = conf.systemProperty("api.http.port").map(_.toInt).get
			ServerConfig(host, port)
		}

		new Config(actyxConfig, serverConfig)
	}

}

class TypeSafeConfig(config: com.typesafe.config.Config) extends SystemConfig {
	override def systemProperty(name: String): Option[String] =
		{ if (config.hasPath(name)) {
				Some(config.getString(name))
			} else None } filter(_ != "")
}



trait SystemConfig {
	def systemProperty(name: String): Option[String] =
		Option(System.getProperty(name))
			.flatMap(_.trim match {
				case "" => None
				case x  => Some(x)
			})

	def getSystemProperty(name: String) =
		systemProperty(name) getOrElse {
			sys.error(s"Missing system property '$name'.")
		}
}