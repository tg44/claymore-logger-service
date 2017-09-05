package com.github.tg44.claymore

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.Sink
import com.github.tg44.claymore.TcpClient.PollReq
import spray.json._

import concurrent.duration._
import scala.concurrent.Await
import collection.immutable.Seq
import scala.util.{Failure, Success}

object ServerService extends JsonSupport {

  import AkkaImplicits._

  lazy val jwt: String = if (Config.SERVER.needAuth) getJwt else ""
  lazy val authHeader: Seq[HttpHeader] = if (Config.SERVER.needAuth) Seq(RawHeader("Authorization", s"Bearer $jwt")) else Nil

  val jwtUrl: String = Config.SERVER.url + Config.SERVER.jwtEndpoint
  val dataUrl: String = Config.SERVER.url + Config.SERVER.dataEndpoint

  var schedulerMap = Map.empty[ActorRef,Seq[(String,Long,Cancellable)]]

  //todo would be nicer if its immutable and tested
  private def adjustSchedule(ref: ActorRef, d: Long, statData: StatisticDataDto) = {
    if(schedulerMap.get(ref).isDefined) {
      val element = schedulerMap(ref).find(_._1 == statData.name)
      if (element.isDefined) {
        if (element.get._2 == d) {
          //ok do nothing
        } else {
          element.get._3.cancel()
          val list = schedulerMap(ref).filterNot(_._1 == statData.name)
          schedulerMap = schedulerMap + (ref -> list)
          scheduleActor(ref, d, statData)
        }
      } else {
        scheduleActor(ref, d, statData)
      }
    } else {
      scheduleActor(ref, d, statData)
    }
  }

  private def scheduleActor(ref: ActorRef, d: Long, statData: StatisticDataDto) = {
    val cancellable = system.scheduler.schedule(d.seconds, d.seconds, ref, PollReq(statData.name, statData.remoteAddress, statData.remotePort))
    val tuple = (statData.name, d, cancellable)
    val list =  schedulerMap.getOrElse(ref, Seq.empty) :+ tuple
    schedulerMap = schedulerMap + (ref -> list)
  }

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
        println(s"data sended, delay is $d seconds")
        adjustSchedule(ref,d,statData)
      case Failure(e) =>
        e.printStackTrace()

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
