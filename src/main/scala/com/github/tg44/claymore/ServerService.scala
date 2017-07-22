package com.github.tg44.claymore

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.Sink
import com.github.tg44.claymore.TcpClient.PollReq
import spray.json._

import concurrent.duration._
import scala.concurrent.Await
import collection.immutable.Seq
import scala.util.Success

object ServerService extends JsonSupport {

  import AkkaImplicits._

  lazy val jwt: String = if (Config.SERVER.needAuth) getJwt else ""
  lazy val authHeader: Seq[HttpHeader] = if (Config.SERVER.needAuth) Seq(RawHeader("Authentication", s"Bearer $jwt")) else Nil

  val jwtUrl: String = Config.SERVER.url + Config.SERVER.jwtEndpoint
  val dataUrl: String = Config.SERVER.url + Config.SERVER.dataEndpoint

  def postData(statData: StatisticDataDto, ref: ActorRef): Unit = {
    val result = Http().singleRequest(
      HttpRequest(method = HttpMethods.POST,
                  uri = dataUrl,
                  headers = authHeader,
                  entity = HttpEntity(ContentTypes.`application/json`, statData.toJson.compactPrint))
    )
    //TODO: maybe json?
    val delay = result.flatMap(x => x._3.dataBytes.map(_.utf8String).runWith(Sink.seq).map(_.mkString("").toInt))
    delay.onComplete {
      case Success(d) =>
        system.scheduler.scheduleOnce(d.millis, ref, PollReq(statData.name, statData.remoteAddress, statData.remotePort))
      case _ =>
    }
  }

  def getJwt: String = {
    val json = ApiSecurityDto(Config.SERVER.apiKey).toJson.compactPrint
    val result =
      Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = jwtUrl, headers = Nil, entity = HttpEntity(ContentTypes.`application/json`, json)))
    //TODO: maybe json?
    Await.result(result.flatMap(x => x._3.dataBytes.map(_.utf8String).runWith(Sink.seq).map(_.mkString(""))), 5.seconds)
  }

}
