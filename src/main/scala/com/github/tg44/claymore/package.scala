package com.github.tg44

package object claymore {

  case class ClaymoreRequestDto(id: Int, jsonrpc: String = "2.0", method: String)

  case class ClaymoreStatisticResponseDto(result: Seq[String])

  case class ParsedStatisticResponse(
      minerVersion: String,
      runTimeInMins: Double,
      currencyInformations: Seq[CurrencyInformation],
      tempsPerCard: Seq[Double],
      fansPerCard: Seq[Double],
      cards: Seq[CardStatistic]
  )

  case class CurrencyInformation(
      currency: String,
      sumHR: Double,
      shares: Int,
      sharesRejected: Int,
      invalidShares: Int,
      poolSwitches: Int,
      perCardHR: Seq[Double],
      currentPool: String
  )

  case class CardStatistic(
      hashRate: Map[String, Double],
      temp: Double,
      fan: Double
  )

  case class StatisticDataDto(
      name: String,
      remoteAddress: String,
      remotePort: Int,
      data: ParsedStatisticResponse
  )

  case class ApiSecurityDto(
      secret: String
  )

}
