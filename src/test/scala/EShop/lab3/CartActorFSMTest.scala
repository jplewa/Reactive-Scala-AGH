package EShop.lab3

import EShop.lab2.CartActor._
import EShop.lab2.{Cart, CartActor}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class CartActorFSMTest
  extends TestKit(ActorSystem("CartActorFSMTest"))
  with FlatSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  //use GetItems command which was added to make test easier
  it should "add item properly" in {
    val cartActor: TestActorRef[CartActor] = TestActorRef(new CartActor)
    cartActor ! AddItem("Harry Potter")
    cartActor.receive(GetItems, self)
    expectMsg(Cart(Seq("Harry Potter")))
  }

  it should "be empty after adding and removing the same item" in {
    val cartActor: TestActorRef[CartActor] = TestActorRef(new CartActor)
    cartActor ! AddItem("Harry Potter")
    cartActor ! RemoveItem("Harry Potter")
    cartActor.receive(GetItems, self)
    expectMsg(Cart(Seq.empty))
  }

  it should "start checkout" in {
    val cartActor: TestActorRef[CartActor] = TestActorRef(new CartActor)
    cartActor ! AddItem("Harry Potter")
    cartActor ! StartCheckout
    fishForMessage() {
      case _: CheckoutStarted => true
      case _                  => false
    }
  }
}
