package EShop.lab2

import akka.actor.{Actor, ActorRef, Cancellable, Props, Scheduler}
import akka.event.LoggingReceive
import EShop.lab2.message._

import scala.concurrent.duration._
import scala.language.postfixOps

object Checkout {

  def props(cart: ActorRef) = Props(new Checkout())
}

class Checkout extends Actor {

  val checkoutTimerDuration: FiniteDuration = 1 seconds
  val paymentTimerDuration: FiniteDuration  = 1 seconds
  private val scheduler: Scheduler          = context.system.scheduler

  def receive: Receive = LoggingReceive {
    case message.StartCheckout =>
      context become selectingDelivery(scheduleTimer(checkoutTimerDuration, ExpireCheckout))
  }

  def selectingDelivery(timer: Cancellable): Receive = LoggingReceive {
    case CancelCheckout | ExpireCheckout => {
      timer.cancel()
      context.parent ! CancelCheckout
      context become cancelled
    }
    case SelectDeliveryMethod(_) =>
      context become selectingPaymentMethod(timer)
  }

  def selectingPaymentMethod(timer: Cancellable): Receive = LoggingReceive {
    case CancelCheckout | ExpireCheckout => {
      timer.cancel()
      context.parent ! CancelCheckout
      context become cancelled
    }
    case SelectPayment(_) =>
      timer.cancel()
      context become processingPayment(scheduleTimer(paymentTimerDuration, ExpirePayment))
  }

  def processingPayment(timer: Cancellable): Receive = LoggingReceive {
    case CancelCheckout | ExpirePayment => {
      timer.cancel()
      context.parent ! CancelCheckout
      context become cancelled
    }
    case ReceivePayment => {
      timer.cancel()
      context.parent ! CloseCheckout
      context become closed
    }
  }

  def closed: Receive = LoggingReceive {
    case CloseCheckout => {
      context.stop(self)
    }
  }

  def cancelled: Receive = LoggingReceive {
    case CancelCheckout => {
      context.stop(self)
    }
  }

  private def scheduleTimer(finiteDuration: FiniteDuration, command: message.Command): Cancellable =
    scheduler.scheduleOnce(finiteDuration, self, command)(context.dispatcher, self)
}
