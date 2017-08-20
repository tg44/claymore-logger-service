package com.github.tg44.claymore

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends DefaultJsonProtocol {

  implicit val claymoreRequestJsonFormatter: RootJsonFormat[ClaymoreRequestDto] = jsonFormat3(ClaymoreRequestDto)
  implicit val claymoreStatResponseJsonFormatter: RootJsonFormat[ClaymoreStatisticResponseDto] = jsonFormat1(ClaymoreStatisticResponseDto)

  implicit val cardStatisticsJsonFormatter: RootJsonFormat[CardStatistic] = jsonFormat3(CardStatistic)
  implicit val currencyInformationJsonFormatter: RootJsonFormat[CurrencyInformation] = jsonFormat8(CurrencyInformation)
  implicit val parsedStatisticResponseJsonFormatter: RootJsonFormat[ParsedStatisticResponse] = jsonFormat6(ParsedStatisticResponse)

  implicit val statisticDataDtoJsonFormatter: RootJsonFormat[StatisticDataDto] = jsonFormat4(StatisticDataDto)
  implicit val apiSecurityDtoJsonFormatter: RootJsonFormat[ApiSecurityDto] = jsonFormat1(ApiSecurityDto)
}
