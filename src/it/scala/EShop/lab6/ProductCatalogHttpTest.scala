package EShop.lab6

import io.gatling.core.Predef.{rampUsers, scenario, Simulation, StringBody, _}
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class ProductCatalogHttpTest extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
//    .baseUrls("http://localhost:8080")
    .baseUrls("http://localhost:9001", "http://localhost:9002", "http://localhost:9003")
    .acceptHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  def random: Int = Random.nextInt(5)

  def request: HttpRequestBuilder = {
    http("search in product catalog")
      .post("/search")
      .body(StringBody {
        """
          |{
          | "brand": "Coca-Cola",
          | "productKeyWords": ["Coke Classic Bottles"]
          |}
          |""".stripMargin
      })
      .asJson
  }

  val testScenario: ScenarioBuilder = scenario("ProductCatalogHttpTest")
    .exec(request)
    .pause(random)
    .exec(request)
    .pause(random)
    .exec(request)
    .pause(random)
    .exec(request)

  setUp(
    testScenario.inject(rampUsers(5000).during(1 minutes))
  ).protocols(httpProtocol)
}
