package com.actyx.challenge.config

class Config private(
	val actyxParkConfig: Config.ActyxMachineParkAPIConfig,
	val serverConfig: Config.ServerConfig
)

object Config {
	case class ActyxMachineParkAPIConfig(
		apiRoot: String,
		reqFrequency: Double)

	case class ServerConfig(
		host: String,
		port: Int)

	def apply()(implicit conf: TypeSafeConfig) = {
		val actyxConfig = {
			val apiRoot = conf.getSystemProperty("actyx.api.root")
			val reqFreq = conf.systemProperty("actyx.machine.frequency").map(_.toDouble).getOrElse(0.21)
			ActyxMachineParkAPIConfig(apiRoot,reqFreq)
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