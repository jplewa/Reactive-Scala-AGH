package EShop.lab6

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import EShop.lab5.{JsonSupport, ProductCatalog, SearchService}
import EShop.lab5.ProductCatalog.{GetItems, Items}

import scala.concurrent.duration._
import scala.util.Try

object ProductCatalogClusterNodeApp extends App {

  private val config = ConfigFactory.load()

  val system = ActorSystem(
    "ClusterWorkRouters",
    config
      .getConfig(Try(args(0)).getOrElse("cluster-default"))
      .withFallback(config.getConfig("cluster-default"))
  )
}

object ProductCatalogHttpClusterApp extends App {
  println()
  println()
  println()
  println()
  println(args(0).toInt)
  new ProductCatalogHttpClusterApp().startServer("localhost", args(0).toInt)
}

class ProductCatalogHttpClusterApp() extends HttpApp with JsonSupport {
  private val config: Config = ConfigFactory.load()
  private val system: ActorSystem = ActorSystem(
    "ClusterWorkRouters",
    config.getConfig("cluster-default")
  )

  implicit val timeout: Timeout = 5.seconds

  val clusterWorkers: ActorRef = system.actorOf(
    ClusterRouterPool(
      RoundRobinPool(3),
      ClusterRouterPoolSettings(totalInstances = 100, maxInstancesPerNode = 3, allowLocalRoutees = false)
    ).props(ProductCatalog.props(new SearchService())),
    name = "productCatalogRouter"
  )

  override protected def routes: Route = {
    path("search") {
      post {
        entity(as[GetItems]) { query: ProductCatalog.Query =>
          complete {
            (clusterWorkers ? query).mapTo[Items]
          }
        }
      }
    }
  }
}
