package EShop.lab2

import EShop.lab2.CheckoutFSM.Status
import akka.actor.{ActorRef, Cancellable, LoggingFSM, Props}
import EShop.lab2.message.{StartCheckout, _}

import scala.concurrent.duration._
import scala.language.postfixOps

object CheckoutFSM {

  def props(cartActor: ActorRef) = Props(new CheckoutFSM(cartActor))

  object Status extends Enumeration {
    type Status = Value
    val NotStarted, SelectingDelivery, SelectingPaymentMethod, Cancelled, ProcessingPayment, Closed = Value
  }
}

class CheckoutFSM(cartActor: ActorRef) extends LoggingFSM[Status.Value, Data] {
  import EShop.lab2.CheckoutFSM.Status._

  val checkoutTimerDuration: FiniteDuration = 1 seconds
  val paymentTimerDuration: FiniteDuration  = 1 seconds
  private val scheduler = context.system.scheduler

  // useful for debugging, see: https://doc.akka.io/docs/akka/current/fsm.html#rolling-event-log
  override def logDepth = 12

  startWith(NotStarted, Uninitialized)

  when(NotStarted) {
    case Event(StartCheckout, _) =>
      goto(SelectingDelivery).using(SelectingDeliveryStarted(scheduleTimer(checkoutTimerDuration, ExpireCheckout)))
  }

  when(SelectingDelivery) {
    case Event(ExpireCheckout | CancelCheckout, SelectingDeliveryStarted(timer)) =>
      timer.cancel()
      context.parent ! CancelCheckout
      goto(Cancelled)
    case Event(SelectDeliveryMethod(_), _) =>
      goto(SelectingPaymentMethod)
  }

  when(SelectingPaymentMethod) {
    case Event(ExpireCheckout | CancelCheckout, SelectingDeliveryStarted(timer)) =>
      timer.cancel()
      context.parent ! CancelCheckout
      goto(Cancelled)
    case Event(SelectPayment(_), SelectingDeliveryStarted(timer)) =>
      timer.cancel()
      goto(ProcessingPayment).using(ProcessingPaymentStarted(scheduleTimer(paymentTimerDuration, ExpirePayment)))
  }

  when(ProcessingPayment) {
    case Event(ExpirePayment | CancelCheckout, ProcessingPaymentStarted(timer)) =>
      timer.cancel()
      context.parent ! CancelCheckout
      goto(Cancelled)
    case Event(ReceivePayment, ProcessingPaymentStarted(timer)) =>
      timer.cancel()
      context.parent ! CloseCheckout
      goto(Closed)
  }

  when(Cancelled) {
    case Event(CancelCheckout, _) => stop()
  }

  when(Closed) {
    case Event(CloseCheckout, _) => stop()
  }

  private def scheduleTimer(finiteDuration: FiniteDuration, command: message.Command): Cancellable =
    scheduler.scheduleOnce(finiteDuration, self, command)(context.dispatcher, self)
}
