package EShop.lab2.message

import akka.actor.ActorRef

sealed trait Event

case class ItemAdded(id: String) extends Event

case class ItemRemoved(id: String) extends Event

case class ItemNotFound(id: String) extends Event

case class CheckoutStarted(checkoutRef: ActorRef) extends Event

case object CheckoutCancelled extends Event

case object CheckoutClosed extends Event

case class PaymentStarted(payment: ActorRef) extends Event
