package shop.domain

import io.estatico.newtype.macros.newtype

import java.util.UUID

object auth {

  @newtype case class UserId(value: UUID)

}
