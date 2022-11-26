package shop.algebras

import shop.domain.cart.{ Cart, CartTotal, Quantity }
import shop.domain.item.ItemId
import shop.domain.auth.UserId

trait ShoppingCart[F[_]] {
  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit]
  def delete(userId: UserId): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def remove(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]
}
