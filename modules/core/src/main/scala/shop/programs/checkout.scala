package shop.programs

import cats.effect.Timer
import cats.implicits.catsSyntaxMonadError
import cats.syntax.all._
import io.chrisdavenport.log4cats.Logger
import retry.RetryDetails._
import retry._
import shop.algebras._
import shop.domain.auth.UserId
import shop.domain.cart.{ CartItem, CartTotal }
import shop.domain.order._
import shop.domain.payment._
import shop.effects.{ Background, MonadThrow }
import shop.http.clients.PaymentClient
import squants.market.Money

import scala.concurrent.duration.DurationInt

final class CheckoutProgram[F[_]: Background: Logger: MonadThrow: Timer](
    paymentClient: PaymentClient[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F],
    retryPolicy: RetryPolicy[F]
) {

  def checkout(userId: UserId, card: Card): F[OrderId] =
    shoppingCart
      .get(userId)
      .ensure(EmptyCartError)(_.items.nonEmpty)
      .flatMap {
        case CartTotal(items, total) =>
          for {
            paymentId <- processPayment(Payment(userId, total, card))
            orderId <- createOrder(userId, paymentId, items, total)
            _ <- shoppingCart.delete(userId).attempt.void
          } yield orderId
      }

  private def processPayment(payment: Payment): F[PaymentId] = {
    val action = retryingOnAllErrors[PaymentId](
      policy = retryPolicy,
      onError = logError("Payments")
    )(paymentClient.process(payment))
    action.adaptError {
      case e => PaymentError(Option(e.getMessage).getOrElse("Unknown"))
    }
  }

  private def createOrder(
      userId: UserId,
      paymentId: PaymentId,
      items: List[CartItem],
      total: Money
  ): F[OrderId] = {
    val action = retryingOnAllErrors[OrderId](
      policy = retryPolicy,
      onError = logError("Order")
    )(orders.create(userId, paymentId, items, total))
    def bgAction(fa: F[OrderId]): F[OrderId] =
      fa.adaptError {
          case e => OrderError(Option(e.getMessage).getOrElse("Unknown"))
        }
        .onError { _ =>
          Logger[F].error(s"Failed to create order for: $paymentId.") *> Background[F].schedule(bgAction(fa), 1.hour)
        }
    bgAction(action)
  }

  private def logError(action: String)(e: Throwable, details: RetryDetails): F[Unit] = details match {
    case r: WillDelayAndRetry =>
      Logger[F].error(
        s"Failed to process $action with ${e.getMessage}. So far we have retried ${r.retriesSoFar} times."
      )
    case g: GivingUp => Logger[F].error(s"Giving up on $action after ${g.totalRetries} retries.")
  }
}
