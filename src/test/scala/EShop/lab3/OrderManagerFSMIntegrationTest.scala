package EShop.lab3

import EShop.lab3.OrderManager._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

class OrderManagerFSMIntegrationTest
  extends TestKit(ActorSystem("OrderManagerFSMIntegrationTest"))
  with FlatSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  implicit val timeout: Timeout = 1.second

  it should "supervise whole order process" in {

    def sendMessageAndValidateState(
      orderManager: TestFSMRef[State, Data, OrderManagerFSM],
      message: OrderManager.Command,
      expectedState: OrderManager.State
    ): Unit = {
      (orderManager ? message).mapTo[OrderManager.Ack].futureValue shouldBe Done
      orderManager.stateName shouldBe expectedState
    }

    val orderManager = TestFSMRef[State, Data, OrderManagerFSM](new OrderManagerFSM())
    orderManager.stateName shouldBe Uninitialized

    sendMessageAndValidateState(orderManager, AddItem("rollerblades"), Open)

    sendMessageAndValidateState(orderManager, Buy, InCheckout)

    sendMessageAndValidateState(orderManager, SelectDeliveryAndPaymentMethod("paypal", "inpost"), InPayment)

    sendMessageAndValidateState(orderManager, Pay, Finished)

    (orderManager ? Pay).mapTo[String].futureValue shouldBe "order manager finished job"
    orderManager.stateName shouldBe Finished
  }
}
