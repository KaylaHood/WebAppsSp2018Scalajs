package models

object Tables extends {
  val profile = slick.jdbc.MySQLProfile
  import profile.api._
  
  class Books(tag: Tag) extends Table[Book](tag, "book") {
    def title = column[String]("title")
    def isbn = column[String]("isbn")
    def price = column[Double]("price")
    def * = (title, isbn, price) <> (Book.tupled, Book.unapply)
  }
  val books = TableQuery[Books]
}