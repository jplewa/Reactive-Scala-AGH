package EShop.lab2.message

sealed trait Command

case class AddItem(item: Any) extends Command

case class RemoveItem(item: Any) extends Command

case object ExpireCart extends Command

case object StartCheckout extends Command

case object CloseCheckout extends Command

case class SelectDeliveryMethod(method: String) extends Command

case object CancelCheckout extends Command

case object ExpireCheckout extends Command

case class SelectPayment(payment: String) extends Command

case object ExpirePayment extends Command

case object ReceivePayment extends Command
