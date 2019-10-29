package EShop.lab2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.LoggingReceive
import EShop.lab2.message.{AddItem, CheckoutStarted, RemoveItem, StartCheckout, _}

class ShoppingApp extends Actor {

  val cart: ActorRef = context.actorOf(Props[CartActor], "cart")

  cart ! AddItem("Item1")
  cart ! AddItem("Item2")
  cart ! RemoveItem("Item1")
  cart ! StartCheckout

  override def receive: Receive = LoggingReceive {
    case CheckoutStarted(checkout) =>
      checkout ! SelectDeliveryMethod("dpd")
      checkout ! SelectPayment("transfer")
      checkout ! CancelCheckout
    case _ =>
  }
}

object ShoppingApp extends App {
  val system    = ActorSystem("shopping")
  val mainActor = system.actorOf(Props[ShoppingApp], "app")
}
