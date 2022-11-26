import Brand.{Brand, BrandName}

trait Brands[F[_]]{
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Unit]
}