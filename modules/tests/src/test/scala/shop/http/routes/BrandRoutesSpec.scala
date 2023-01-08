package shop.http.routes

import cats.effect._
import org.http4s._
import org.http4s.Method._
import org.http4s.client.dsl.io._
import shop.algebras.Brands
import shop.arbitraries._
import shop.domain.brand._
import shop.http.json._
import suite._

class BrandRoutesSpec extends HttpTestSuite {

  def dataBrands(brands: List[Brand]): Brands[IO] = new TestBrands {
    override def findAll: IO[List[Brand]] =
      IO.pure(brands)
  }

  test("GET brands [OK]") {
    forAll { (brands: List[Brand]) =>
      GET(Uri.uri("brands")).flatMap { req =>
        val routes = new BrandRoutes[IO](dataBrands(brands)).routes
        assertHttp(routes, req)(Status.Ok, brands)
      }
    }
  }

  protected class TestBrands extends Brands[IO] {
    override def create(name: BrandName): IO[Unit] = IO.unit
    override def findAll: IO[List[Brand]] = IO.pure(List.empty)
  }

}
