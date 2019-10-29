package EShop.lab2

import akka.actor.{Actor, Cancellable, Props, Timers}
import akka.event.LoggingReceive
import EShop.lab2.message._

import scala.concurrent.duration._
import scala.language.postfixOps

object CartActor {

  def props = Props(new CartActor())
}

class CartActor extends Actor with Timers {

  val cartTimerDuration: FiniteDuration = 5 seconds

  def receive: Receive = LoggingReceive {
    empty
  }

  def empty: Receive = LoggingReceive {
    case AddItem(item) =>
      context become nonEmpty(Cart.empty.addItem(item), scheduleTimer)
  }

  def nonEmpty(cart: Cart, timer: Cancellable): Receive = LoggingReceive {
    case AddItem(item) =>
      timer.cancel()
      context become nonEmpty(cart.addItem(item), scheduleTimer)
    case RemoveItem(item) if cart.contains(item) && cart.size > 1 =>
      timer.cancel()
      context become nonEmpty(cart.removeItem(item), scheduleTimer)
    case RemoveItem(item) if cart.contains(item) && cart.size == 1 =>
      timer.cancel()
      context become empty
    case ExpireCart =>
      context become empty
    case StartCheckout =>
      timer.cancel()
      val checkout = context.actorOf(Checkout.props(self), "checkout")
      checkout ! StartCheckout
      context become inCheckout(cart)
  }

  def inCheckout(cart: Cart): Receive = LoggingReceive {
    case CancelCheckout =>
      context.child("checkout").foreach(_ ! CancelCheckout)
      context become nonEmpty(cart, scheduleTimer)
    case CloseCheckout =>
      context.child("checkout").foreach(_ ! CloseCheckout)
      context become empty
  }

  private def scheduleTimer: Cancellable =
    context.system.scheduler.scheduleOnce(cartTimerDuration, self, ExpireCart)(context.dispatcher, self)
}
