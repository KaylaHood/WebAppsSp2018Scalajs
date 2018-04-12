package controllers

import javax.inject._
import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcCapabilities
import slick.jdbc.MySQLProfile.api._
import models.BookQueries
import scala.concurrent.ExecutionContext
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import scala.concurrent.Future

case class NewBook(title: String, isbn: String, price: BigDecimal)

@Singleton
class BookController @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  mcc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  val newBookForm = Form(mapping(
    "title" -> nonEmptyText,
    "isbn" -> text(minLength = 10, maxLength = 13),
    "price" -> bigDecimal(5, 2))(NewBook.apply)(NewBook.unapply))

  def allBooks = Action.async { implicit request =>
    val booksFuture = BookQueries.allBooks(db)
    booksFuture.map(books => Ok(views.html.bookTable(books, newBookForm)))
  }

  def addBook = Action.async { implicit request =>
    newBookForm.bindFromRequest().fold(
      formWithErrors => {
        val booksFuture = BookQueries.allBooks(db)
        booksFuture.map(books => BadRequest(views.html.bookTable(books, formWithErrors)))
      },
      newBook => {
        val addFuture = BookQueries.addBook(newBook, db)
        addFuture.map { cnt =>
          if(cnt == 1) Redirect(routes.BookController.allBooks).flashing("message" -> "New book added.")
          else Redirect(routes.BookController.allBooks).flashing("error" -> "Failed to add book.")
        }
      })
  }
}