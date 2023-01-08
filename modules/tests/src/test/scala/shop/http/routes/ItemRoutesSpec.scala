package shop.http.routes

import cats.effect._
import cats.implicits.none
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import shop.algebras.Items
import shop.arbitraries._
import shop.domain.brand._
import shop.domain.item
import shop.domain.item.{CreateItem, Item, UpdateItem}
import shop.http.json._
import suite._

class ItemRoutesSpec extends HttpTestSuite {

  def dataItems(items: List[Item]): Items[IO] = new TestItems {
    override def findAll: IO[List[Item]] =
      IO.pure(items)
  }

  def dataItemsForBrand(items: List[Item]): Items[IO] = new TestItems {
    override def findBy(brand: BrandName): IO[List[Item]] =
      IO.pure(items)
  }

  def failingItems(items: List[Item]) = new TestItems {
    override def findAll: IO[List[Item]] =
      IO.raiseError(DummyError) *> IO.pure(items)
  }

  test("GET items [OK]") {
    forAll { (items: List[Item]) =>
      IOAssertion {
        GET(Uri.uri("/items")).flatMap { req =>
          val routes = new ItemRoutes[IO](dataItems(items)).routes
          assertHttp(routes, req)(Status.Ok, items)
        }
      }
    }
  }

  test("GET items by brand [OK]") {
    forAll { (items: List[Item], brand: Brand) =>
      IOAssertion {
        GET(Uri.uri("/items").withQueryParam("brand",brand.name.value)).flatMap { req =>
          val routes = new ItemRoutes[IO](dataItemsForBrand(items)).routes
          assertHttp(routes, req)(Status.Ok, items)
        }
      }
    }
  }

  test("GET items [ERROR]") {
    forAll { (it: List[Item]) =>
      IOAssertion {
        GET(Uri.uri("/items")).flatMap { req =>
          val routes = new ItemRoutes[IO](failingItems(it)).routes
          assertHttpFailure(routes, req)
        }
      }
    }
  }


  protected class TestItems extends Items[IO] {
    override def findAll: IO[List[Item]]                      = IO.pure(List.empty)
    override def findBy(brand: BrandName): IO[List[Item]]     = IO.pure(List.empty)
    override def findById(id: item.ItemId): IO[Option[Item]]  = IO.pure(none[Item])
    override def create(item: CreateItem): IO[Unit]           = IO.unit
    override def update(item: UpdateItem): IO[Unit]           = IO.unit
  }

}
