package shop.programs

import cats.effect._
//import cats.effect.concurrent.Ref
//import cats.implicits.{ catsSyntaxEq => _, _ }
import retry.RetryPolicy
import retry.RetryPolicies._
import shop.algebras._
import shop.arbitraries._
import shop.domain._
import shop.domain.auth._
import shop.domain.cart._
import shop.domain.checkout._
import shop.domain.item._
import shop.domain.order._
import shop.domain.payment._
import shop.http.clients._
import squants.market._
import suite._

final class CheckoutSpec extends PureTestSuite {

  val MaxRetries = 3

  val retryPolicy: RetryPolicy[IO] = limitRetries[IO](MaxRetries)

  def successfulClient(paymentId: PaymentId): PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] =
        IO.pure(paymentId)
    }

  def successfulCart(cartTotal: CartTotal): ShoppingCart[IO] = new TestCart {
    override def get(userId: UserId): IO[CartTotal] =
      IO.pure(cartTotal)
    override def delete(userId: UserId): IO[Unit] = IO.unit
  }

  def successfulOrders(orderId: OrderId): Orders[IO] = new TestOrders {
    override def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): IO[OrderId] =
      IO.pure(orderId)
  }

  test(s"successful checkout") {
    implicit val bg = shop.background.NoOp
    import shop.logger.NoOp
    forAll { (uid: UserId, pid: PaymentId, oid: OrderId, ct: CartTotal, card: Card) =>
      IOAssertion {
        new CheckoutProgram[IO](successfulClient(pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
          .checkout(uid, card)
          .map { id =>
            assert(id === oid)
          }
      }
    }
  }

}

protected class TestOrders() extends Orders[IO] {
  def get(userId: UserId, orderId: OrderId): IO[Option[Order]]                                       = ???
  def findBy(userId: UserId): IO[List[Order]]                                                        = ???
  def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): IO[OrderId] = ???
}

protected class TestCart() extends ShoppingCart[IO] {
  def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = ???
  def get(userId: UserId): IO[CartTotal]                                = ???
  def delete(userId: UserId): IO[Unit]                                  = ???
  def removeItem(userId: UserId, itemId: ItemId): IO[Unit]              = ???
  def update(userId: UserId, cart: Cart): IO[Unit]                      = ???
}
