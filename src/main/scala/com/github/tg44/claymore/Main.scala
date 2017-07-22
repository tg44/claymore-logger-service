package com.github.tg44.claymore

import com.github.tg44.claymore.TcpClient.PollReq

object Main extends App {

  import AkkaImplicits._

  Config.SERVER
  Config.RIGS
  ServerService.jwt

  val client = system.actorOf(ClaymoreClient.props)

  Config.RIGS.foreach(nameAndRig => client ! PollReq(nameAndRig._1, nameAndRig._2.host, nameAndRig._2.port))

}
