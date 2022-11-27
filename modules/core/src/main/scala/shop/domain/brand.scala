package shop.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import java.util.UUID

object brand {

  @newtype case class BrandId(value: UUID)

  @newtype case class BrandName(value: String)

  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName = BrandName(value.value.toLowerCase)
  }

  case class Brand(uuid: BrandId, name: BrandName)

}
