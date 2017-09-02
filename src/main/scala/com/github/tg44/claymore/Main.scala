package com.github.tg44.claymore

import com.github.tg44.claymore.TcpClient.PollReq

object Main extends App {

  import AkkaImplicits._

  Config.SERVER
  Config.RIGS

  println(s"Starting with these rigs: ${Config.RIGS}")

  println(s"Auth started")
  ServerService.jwt
  println(s"Auth ended")

  val client = system.actorOf(ClaymoreClient.props)

  Config.RIGS.foreach(nameAndRig => client ! PollReq(nameAndRig._1, nameAndRig._2.host, nameAndRig._2.port))

}
