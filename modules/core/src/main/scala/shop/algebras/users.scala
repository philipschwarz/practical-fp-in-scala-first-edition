package shop.algebras

import shop.domain.auth.{Password, User, UserId, UserName}

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(userName: UserName, password: Password): F[Option[UserId]]
}