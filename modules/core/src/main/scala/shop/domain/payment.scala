package shop.domain

import shop.domain.auth.UserId
import squants.market.Money

object payment {

  type Card = Int // TBD

  case class Payment(id: UserId, total: Money, card: Card)

}
