package EShop.lab5

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.{ask, PipeToSupport}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import EShop.lab5.ProductCatalog.GetItems
import akka.routing.RoundRobinPool

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class ProductCatalogHttpApp(actorSystem: ActorSystem) extends HttpApp {
  private implicit val timeout: Timeout                   = Timeout(10 seconds)
  private implicit val executionContext: ExecutionContext = ExecutionContext.global

  private val workers: ActorRef = actorSystem.actorOf(RoundRobinPool(3).props(Props[RequestHandler]))

  import EShop.lab5.ProductCatalog._
  override protected def routes: Route = {
    path("search") {
      post {
        entity(as[GetItems]) { query: Query =>
          complete {
            (workers ? query).mapTo[Items]
          }
        }
      }
    }
  }
}

class RequestHandler() extends Actor with PipeToSupport {
  private implicit val timeout: Timeout                   = Timeout(10 seconds)
  private implicit val executionContext: ExecutionContext = ExecutionContext.global

  private val searchServicePath: String = "akka.tcp://ProductCatalog@127.0.0.1:2555/user/searchService-*"

  override def receive: Receive = {
    case get: GetItems =>
      (for {
        actor    <- context.actorSelection(searchServicePath).resolveOne()
        response <- actor ? get
      } yield response).pipeTo(sender())
  }
}

object ProductCatalogHttpApp extends App {
  private val config: Config = ConfigFactory.load()

  val httpActorSystem: ActorSystem =
    ActorSystem("ProductCatalogApp", config.getConfig("productcatalogapp").withFallback(config))

  private val productCatalogSystem: ActorSystem =
    ActorSystem("ProductCatalog", config.getConfig("productcatalog").withFallback(config))

  val productCatalogWorkers = (0 until 2).map { i =>
    productCatalogSystem.actorOf(ProductCatalog.props(new SearchService()), s"searchService-$i")
  }

  val server = new ProductCatalogHttpApp(httpActorSystem)

  server.startServer("localhost", 8080, httpActorSystem)
}
