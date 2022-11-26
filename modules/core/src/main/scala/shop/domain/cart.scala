package shop.domain

import item.{ Item, ItemId }
import io.estatico.newtype.macros.newtype
import squants.market.Money

import java.util.UUID

object cart {

  @newtype case class Quantity(value: Int)
  @newtype case class Cart(items: Map[ItemId, Quantity])
  @newtype case class CartId(value: UUID)

  case class CartItem(item: Item, quantity: Quantity)

  case class CartTotal(items: List[CartItem], total: Money)
}
