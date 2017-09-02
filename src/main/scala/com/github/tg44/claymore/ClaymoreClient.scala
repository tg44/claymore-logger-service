package com.github.tg44.claymore

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.util.ByteString
import com.github.tg44.claymore.TcpClient._

import scala.util.Try

object ClaymoreClient {
  def props =
    Props(classOf[ClaymoreClient])

  def parseStatResponse(claymoreStatisticResponseDto: ClaymoreStatisticResponseDto): ParsedStatisticResponse = {

    val data: Seq[String] = claymoreStatisticResponseDto.result
    val tempAndFanDataSeq = data(6).split(";")
    val tempSeq =
      tempAndFanDataSeq.sliding(1, 2).flatten.map(str => Try(str.toDouble).getOrElse(0.0)).toList
    val fanSeq =
      tempAndFanDataSeq.drop(1).sliding(1, 2).flatten.map(str => Try(str.toDouble).getOrElse(0.0)).toList
    val eth = collectEthData(data)
    val dcr = collectDcrData(data)

    ParsedStatisticResponse(
      minerVersion = data(0),
      runTimeInMins = Try(data(1).toDouble).getOrElse(0.0),
      currencyInformations = Seq(eth, dcr),
      tempsPerCard = tempSeq,
      fansPerCard = fanSeq,
      cards = collectCardsData(tempSeq, fanSeq, eth, dcr)
    )
  }

  def collectEthData(data: Seq[String]): CurrencyInformation = {
    val detaildRates = data(2).split(";").map(str => Try(str.toDouble).getOrElse(0.0)).toList
    val poolInfos = data(8).split(";").map(str => Try(str.toInt).getOrElse(0)).toList

    CurrencyInformation(
      currency = "eth",
      sumHR = detaildRates(0),
      shares = Try(detaildRates(1).toInt).getOrElse(0),
      sharesRejected = Try(detaildRates(2).toInt).getOrElse(0),
      invalidShares = poolInfos(0),
      poolSwitches = poolInfos(1),
      perCardHR = data(3).split(";").map(str => Try(str.toDouble).getOrElse(0.0)),
      currentPool = data(7).split(";")(0)
    )
  }

  def collectDcrData(data: Seq[String]): CurrencyInformation = {
    val detaildRates = data(4).split(";").map(str => Try(str.toDouble).getOrElse(0.0)).toList
    val poolInfos = data(8).split(";").map(str => Try(str.toInt).getOrElse(0)).toList

    CurrencyInformation(
      currency = "dcr",
      sumHR = detaildRates(0),
      shares = detaildRates(1).toInt,
      sharesRejected = Try(detaildRates(2).toInt).getOrElse(0),
      invalidShares = poolInfos(2),
      poolSwitches = poolInfos(3),
      perCardHR = data(5).split(";").map(str => Try(str.toDouble).getOrElse(0.0)),
      currentPool = Try(data(7).split(";")(1)).getOrElse("")
    )
  }

  def collectCardsData(tempSeq: Seq[Double], fanSeq: Seq[Double], ethData: CurrencyInformation, dcrData: CurrencyInformation): Seq[CardStatistic] = {

    tempSeq zip fanSeq zip ethData.perCardHR zip dcrData.perCardHR map {
      case (((a, b), c), d) => CardStatistic(Map("eth" -> c, "dcr" -> d), a, b)
    }
  }

}

class ClaymoreClient extends Actor with JsonSupport {
  import spray.json._

  override def receive: Receive = {
    case PollReq(name, address, port) =>
      println(s"started polling $name")
      context.actorOf(TcpClient.props(new InetSocketAddress(address, port), self, name))
    case ConnectionOpened(inetAddr) =>
      println(s"connection opened to $inetAddr")
      sender ! ByteString(ClaymoreRequestDto(id = 0, method = "miner_getstat1").toJson.compactPrint)
    case Received(data, inetAddr, name) =>
      val dto = data.utf8String.parseJson.convertTo[ClaymoreStatisticResponseDto]
      val model = ClaymoreClient.parseStatResponse(dto)
      println(s"data came back from $name")
      ServerService.postData(StatisticDataDto(name, inetAddr.getHostName, inetAddr.getPort, model), self)
      sender ! Close
    //TODO: logs
    case ConnectionClosed(inetAddr) =>
      println(s"connection closed $inetAddr")
    case WriteFailed(inetAddr) =>
      println(s"write failed $inetAddr")
    case ConnectionFailed(inetAddr) =>
      println(s"connection failed $inetAddr")
  }
}
