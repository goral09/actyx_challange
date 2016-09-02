package client

import scala.scalajs.js
import scala.scalajs.js.Dynamic._

object Utils {
  def log(message: String) = {
    val canLog = !js.isUndefined(global.console) &&
      !js.isUndefined(global.console.log)
    if (canLog) global.console.log(message)
  }
}