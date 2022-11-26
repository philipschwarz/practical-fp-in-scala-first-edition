package shop.domain

import io.estatico.newtype.macros.newtype

import java.util.UUID

object auth {

  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)
  @newtype case class JwtToken(value: String)

  case class User(id: UserId, name: UserName)
}
