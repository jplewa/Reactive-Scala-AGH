package EShop.lab2

import EShop.lab2.CartActorFSM.Status
import akka.actor.{ActorRef, LoggingFSM, Props}

import scala.concurrent.duration._
import scala.language.postfixOps

object CartActorFSM {

  def props() = Props(new CartActorFSM())

  object Status extends Enumeration {
    type Status = Value
    val Empty, NonEmpty, InCheckout = Value
  }
}

class CartActorFSM extends LoggingFSM[Status.Value, Cart] {

  import CartActor._
  import EShop.lab2.CartActorFSM.Status._

  val cartTimerDuration: FiniteDuration = 1 seconds

  // useful for debugging, see: https://doc.akka.io/docs/akka/current/fsm.html#rolling-event-log
  override def logDepth = 12

  startWith(Empty, Cart.empty)

  when(Empty) {
    case Event(AddItem(item), cart) =>
      goto(NonEmpty).using(cart.addItem(item))
    case Event(GetItems, cart) =>
      sender ! cart
      stay
  }

  when(NonEmpty, stateTimeout = cartTimerDuration) {
    case Event(AddItem(item), cart) =>
      goto(NonEmpty).using(cart.addItem(item))
    case Event(RemoveItem(item), cart) if cart.contains(item) && cart.size > 1 =>
      goto(NonEmpty).using(cart.removeItem(item))
    case Event(RemoveItem(item), cart) if cart.contains(item) && cart.size == 1 =>
      goto(Empty).using(cart.removeItem(item))
    case Event(StartCheckout, cart) =>
      val checkout: ActorRef = context.actorOf(Props(new CheckoutFSM(self)), "checkout")
      checkout ! Checkout.StartCheckout
      sender ! CheckoutStarted(checkout)
      goto(InCheckout).using(cart)
    case Event(StateTimeout, _) =>
      goto(Empty).using(Cart.empty)
    case Event(GetItems, cart) =>
      sender ! cart
      stay
  }

  when(InCheckout) {
    case Event(CancelCheckout, cart) =>
      context.child("checkout").foreach(_ ! Checkout.CancelCheckout)
      goto(NonEmpty).using(cart)
    case Event(CloseCheckout, _) =>
      context.child("checkout").foreach(_ ! CloseCheckout)
      goto(Empty).using(Cart.empty)
    case Event(GetItems, cart) =>
      sender ! cart
      stay
  }
}
