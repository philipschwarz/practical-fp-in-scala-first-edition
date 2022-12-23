package shop

import eu.timepit.refined.api.Refined
import io.estatico.newtype.ops._
import io.estatico.newtype.Coercible
import java.util.UUID
import org.scalacheck.Gen
import shop.domain.brand._
import shop.domain.cart._
import shop.domain.category._
import shop.domain.checkout._
import shop.domain.item._
import squants.market._

object generators {

  def cbUuid[A: Coercible[UUID, *]]: Gen[A] =
    Gen.uuid.map(_.coerce[A])

  def cbStr[A: Coercible[String, *]]: Gen[A] =
    genNonEmptyString.map(_.coerce[A])

  def cbInt[A: Coercible[Int, *]]: Gen[A] =
    Gen.posNum[Int].map(_.coerce[A])

  val genMoney: Gen[Money] =
    Gen.posNum[Long].map(n => USD(BigDecimal(n)))

  val genNonEmptyString: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  val brandGen: Gen[Brand] =
    for {
      uuid <- cbUuid[BrandId]
      brandName <- cbStr[BrandName]
    } yield Brand(uuid, brandName)

  val categoryGen: Gen[Category] =
    for {
      uuid <- cbUuid[CategoryId]
      categoryName <- cbStr[CategoryName]
    } yield Category(uuid, categoryName)

  val itemGen: Gen[Item] =
    for {
      itemId <- cbUuid[ItemId]
      itemName <- cbStr[ItemName]
      itemDescription <- cbStr[ItemDescription]
      price <- genMoney
      brand <- brandGen
      category <- categoryGen
    } yield Item(itemId, itemName, itemDescription, price, brand, category)

  val cartItemGen: Gen[CartItem] =
    for {
      item <- itemGen
      quantity <- cbInt[Quantity]
    } yield CartItem(item, quantity)

  val cartTotalGen: Gen[CartTotal] =
    for {
      items <- Gen.nonEmptyListOf(cartItemGen)
      total <- genMoney
    } yield CartTotal(items, total)

  val itemMapGen: Gen[(ItemId, Quantity)] =
    for {
      i <- cbUuid[ItemId]
      q <- cbInt[Quantity]
    } yield i -> q

  val cartGen: Gen[Cart] =
    Gen.nonEmptyMap(itemMapGen).map(Cart.apply)

  val cardGen: Gen[Card] =
    for {
      n <- genNonEmptyString.map[CardNamePred](Refined.unsafeApply)
      u <- Gen.posNum[Long].map[CardNumberPred](Refined.unsafeApply)
      x <- Gen.posNum[Int].map[CardExpirationPred](x => Refined.unsafeApply(x.toString))
      c <- Gen.posNum[Int].map[CardCVVPred](Refined.unsafeApply)
    } yield Card(CardName(n), CardNumber(u), CardExpiration(x), CardCVV(c))
}
