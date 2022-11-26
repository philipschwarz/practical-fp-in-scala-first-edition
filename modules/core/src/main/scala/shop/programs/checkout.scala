package shop.programs

import cats.Monad
import cats.implicits.{ toFlatMapOps, toFunctorOps }
import shop.algebras.{ Orders, ShoppingCart }
import shop.domain.auth.UserId
import shop.domain.order.OrderId
import shop.domain.payment.{ Card, Payment }
import shop.http.clients.PaymentClient

final class CheckoutProgram[F[_]: Monad](
    paymentClient: PaymentClient[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F]
) {
  def checkout(userId: UserId, card: Card): F[OrderId] =
    for {
      cart <- shoppingCart.get(userId)
      paymentId <- paymentClient.process(Payment(userId, cart.total, card))
      orderId <- orders.create(userId, paymentId, cart.items, cart.total)
      _ <- shoppingCart.delete(userId)
    } yield orderId
}
