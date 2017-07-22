package com.github.tg44.claymore

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.github.tg44.claymore.TcpClient.{ConnectionClosed, ConnectionFailed, ConnectionOpened, WriteFailed}

object TcpClient {
  case class ConnectionFailed(addr: InetSocketAddress)
  case class ConnectionClosed(addr: InetSocketAddress)
  case class ConnectionOpened(addr: InetSocketAddress)
  case class WriteFailed(addr: InetSocketAddress)
  case class Received(data: ByteString, addr: InetSocketAddress, name: String)
  case class PollReq(name: String, host: String, port: Int)
  case object Close

  def props(remote: InetSocketAddress, replies: ActorRef, name: String) =
    Props(classOf[TcpClient], remote, replies, name)
}

class TcpClient(remote: InetSocketAddress, listener: ActorRef, name: String) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(connect: Connect) =>
      listener ! ConnectionFailed(connect.remoteAddress)
      context stop self

    case c @ Connected(remote, local) =>
      listener ! ConnectionOpened(remote)
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! WriteFailed(remote)
        case Received(data) =>
          listener ! TcpClient.Received(data, remote, name)
        case TcpClient.Close =>
          connection ! Close
        case _: Tcp.ConnectionClosed =>
          listener ! TcpClient.ConnectionClosed(remote)
          context stop self
      }
  }
}
