package shop.algebras

import shop.domain.auth.{ JwtToken, Password, User, UserId, UserName }

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(userName: UserName, password: Password): F[Option[UserId]]
}

trait Auth[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(userName: UserName, password: Password): F[JwtToken]
  def login(userName: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, userName: UserName): F[Unit]
}
