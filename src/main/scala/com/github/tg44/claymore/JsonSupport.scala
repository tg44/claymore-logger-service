package com.github.tg44.claymore

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends DefaultJsonProtocol {

  implicit val claymoreRequestJsonFormatter: RootJsonFormat[ClaymoreRequestDto] = jsonFormat3(ClaymoreRequestDto)
  implicit val claymoreStatResponseJsonFormatter: RootJsonFormat[ClaymoreStatisticResponseDto] = jsonFormat1(ClaymoreStatisticResponseDto)

  implicit val cardStatisticsJsonFormatter: RootJsonFormat[CardStatistic] = jsonFormat4(CardStatistic)
  implicit val currencyInformationJsonFormatter: RootJsonFormat[CurrencyInformation] = jsonFormat7(CurrencyInformation)
  implicit val parsedStatisticResponseJsonFormatter: RootJsonFormat[ParsedStatisticResponse] = jsonFormat7(ParsedStatisticResponse)

  implicit val statisticDataDtoJsonFormatter: RootJsonFormat[StatisticDataDto] = jsonFormat4(StatisticDataDto)
  implicit val apiSecurityDtoJsonFormatter: RootJsonFormat[ApiSecurityDto] = jsonFormat1(ApiSecurityDto)
}
