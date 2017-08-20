package com.github.tg44.claymore

import org.scalatest.{Matchers, WordSpecLike}
import spray.json._

class ClaymoreClientSpec extends WordSpecLike with Matchers with JsonSupport {

  val exampleJson =
    """{"result": [
      | "9.3 - ETH",
      | "21",
      | "182724;51;0",
      | "30502;30457;30297;30481;30479;30505",
      | "0;0;0",
      | "off;off;off;off;off;off",
      | "53;71;57;67;61;72;55;70;59;71;61;70",
      | "eth-eu1.nanopool.org:9999", 
      | "0;0;0;0"
      | ]}""".stripMargin

  val exampleDto =
    exampleJson.parseJson.convertTo[ClaymoreStatisticResponseDto]

  "ClaymoreClient" must {

    "parse an example without error" in {
      try {
        ClaymoreClient.parseStatResponse(exampleDto)
      } catch {
        case ex: Throwable =>
          ex.printStackTrace()
          fail("That expression shouldn't have thrown an exception")
      }
    }

    "parse fan and temp correctly" in {
      val result = ClaymoreClient.parseStatResponse(exampleDto)
      result.tempsPerCard.head shouldBe 53
      result.fansPerCard.head shouldBe 71
    }

    "parse cards correctly" in {
      val result = ClaymoreClient.parseStatResponse(exampleDto)
      result.cards.size shouldBe 6
      result.cards.head shouldBe CardStatistic(Map("eth" -> 30502, "dcr" -> 0), 53, 71)
    }
  }
}
