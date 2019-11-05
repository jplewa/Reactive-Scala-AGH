package EShop.lab3

import EShop.lab2.CartActor.CloseCheckout
import EShop.lab2.Checkout
import EShop.lab2.Checkout._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class CheckoutTest
  extends TestKit(ActorSystem("CheckoutTest"))
  with FlatSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  it should "send close confirmation to parent cart" in {
    val cartActor = TestProbe()
    val checkout  = cartActor.childActorOf(Props(new Checkout(cartActor.ref)))
    checkout ! StartCheckout
    checkout ! SelectDeliveryMethod("fedex")
    checkout ! SelectPayment("cash")
    checkout ! ReceivePayment
    cartActor.expectMsg(CloseCheckout)
  }
}
