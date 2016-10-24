package com.actyx.challenge.config

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import com.actyx.challenge.util._

class Config private(
	val actyxParkConfig:  Config.ActyxMachineParkAPIConfig,
	val serverConfig:     Config.ServerConfig,
	val envCtxt:          Config.EnvContext)

object Config {
	case class ActyxMachineParkAPIConfig(
		apiRoot:              String,
		reqFrequency:         Double,
		movingAvgWindowSize:  FiniteDuration)

	case class ServerConfig(
		host: String,
		port: Int)

	sealed trait EnvContext
	case object EnvContext {
		def apply(in: String): EnvContext = in.toLowerCase match {
			case "test"     ⇒ Test
			case "prod"     ⇒ Prod
			case o: String  ⇒ sys.error(s"Unrecognized $o context. Test and Prod are allowed.")
		}

		case object Test extends EnvContext
		case object Prod extends EnvContext {
			override def toString: String = "Production"
		}
	}

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

		val envCtxt = conf.systemProperty("env")
			.map(EnvContext(_))
		  .map { ctxt ⇒
				Logger.info(s"Running in $ctxt context.")
				ctxt
			}.get

		new Config(actyxConfig, serverConfig, envCtxt)
	}

}

class TypeSafeConfig(config: com.typesafe.config.Config) extends SystemConfig {
	override def systemProperty(name: String): Option[String] =
		{{if (config.hasPath(name)) {
					Some(config.getString(name))
				} else None
			} orElse super.systemProperty(name)
			.orElse(Option(System.getenv("env")))}
			.filter(_ != "")
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