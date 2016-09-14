package com.actyx.challenge

import java.net.URL

import com.actyx.challenge.api.MachinesEndpoint
import com.actyx.challenge.config.Config
import com.actyx.challenge.util.Frequency

class CompositionRoot(conf: Config) {
	val apiRoot = new URL(conf.actyxParkConfig.apiRoot)
	val freq     = Frequency(conf.actyxParkConfig.reqFrequency)
	// TODO: `discover` the endpoints
	val machinesApiEndpoint = new URL(apiRoot.toString + "/machines")
	val machinesState = new MachinesEndpoint(apiRoot, machinesApiEndpoint, freq)
}

object CompositionRoot {
	def apply()(implicit conf: Config) =
		new CompositionRoot(conf)
}
