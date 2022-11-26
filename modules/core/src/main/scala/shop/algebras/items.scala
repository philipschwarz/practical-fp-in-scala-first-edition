import Brand.{Brand, BrandName}
import Item.{CreateItem, Item, ItemId, UpdateItem}

trait Items [F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(id: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[Unit]
  def update(item: UpdateItem): F[Unit]
}