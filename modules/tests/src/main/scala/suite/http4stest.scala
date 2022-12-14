package suite

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.scalatest.Assertion

import scala.util.control.NoStackTrace

trait HttpTestSuite extends PureTestSuite {

  case object DummyError extends NoStackTrace

  def assertHttp[A: Encoder](routes: HttpRoutes[IO], req: Request[IO])(expectedStatus: Status, expectedBody: A): IO[Assertion] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.as[Json].map { json =>
          assert(
            resp.status === expectedStatus &&
              json.dropNullValues === expectedBody.asJson.dropNullValues
          )
        }
      case None => fail("route not found")
    }

  def assertHttpFailure(routes: HttpRoutes[IO], req: Request[IO]): IO[Assertion] =
    routes.run(req).value.attempt.map {
      case Left(_) => assert(true)
      case Right(_) => fail("expected a failure")
    }

}