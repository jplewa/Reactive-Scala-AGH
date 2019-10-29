package EShop.lab2.message

import akka.actor.Cancellable

sealed trait Data

case object Uninitialized extends Data

case class SelectingDeliveryStarted(timer: Cancellable) extends Data

case class ProcessingPaymentStarted(timer: Cancellable) extends Data
