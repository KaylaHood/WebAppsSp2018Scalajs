package controllers

import javax.inject._
import java.util.concurrent.atomic.AtomicInteger
import play.api.mvc._
import play.api.libs.json.Writes
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.JsPath
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.util.concurrent.atomic.AtomicReference

@Singleton
class SinglePageApp @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  private val cnt = new AtomicInteger(0)
  private val circle = new AtomicReference(Circle(4, 5, 6))
  
  case class Circle(x: Int, y: Int, r: Int)

  implicit val circleWrites = new Writes[Circle] {
    def writes(circle: Circle) = Json.obj(
      "x" -> circle.x,
      "y" -> circle.y,
      "radius" -> circle.r)
  }
  
  implicit val circleReads : Reads[Circle] = (
    (JsPath \ "x").read[Int] and
    (JsPath \ "y").read[Int] and
    (JsPath \ "radius").read[Int]
  )(Circle.apply _)

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    Ok(views.html.singlePageApp())
  }

  def button1Call = Action { implicit request =>
    Ok(Json.toJson(circle.get))
  }

  def button2Call = Action { implicit request =>
    Ok(s"This is plain text. Count = ${cnt.incrementAndGet()}. Message is ${spa.SharedMessages.itWorks}")
  }

  def setCircle = Action(parse.json) { implicit request =>
    println(request.body)
    val c = request.body.validate[Circle].get
    circle.set(c)
    Ok("Circle set to ")//+c)
  }
}
