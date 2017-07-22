package com.github.tg44.claymore

import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

object Config {

  case class Server(url: String, apiKey: String, needAuth: Boolean, jwtEndpoint: String, dataEndpoint: String)
  case class Rig(host: String, port: Int)

  private[this] implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  import pureconfig.loadConfigOrThrow

  lazy val SERVER: Server = loadConfigOrThrow[Server]("endpoint")

  lazy val RIGS: Map[String, Rig] = loadConfigOrThrow[Map[String, Rig]]("rigs")
}
