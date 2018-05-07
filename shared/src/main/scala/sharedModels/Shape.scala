package sharedModels

import play.api.libs.json.JsValue
import play.api.libs.json.Writes
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.JsPath
import play.api.libs.json.Reads._
import play.api.libs.json.Format
import play.api.libs.functional.syntax._

sealed trait ShapeCompanion {
  type T <: Any
  val id: Int
  val reader: Reads[T]
  val writer: Writes[T]
  implicit val format: Format[T] = Format(reader, writer)
}

abstract class Shape(val userId: Int, val color: String, val x: Double, val y: Double) {
  def companion: ShapeCompanion = Shape
  def getId(): Int = companion.id
}

object Shape extends ShapeCompanion {
  override val id: Int = -1
  override type T = Shape
  override val reader: Reads[Shape] = Rectangle.shapeReader or Circle.shapeReader or Line.shapeReader
  override val writer: Writes[Shape] = new Writes[Shape] {
    def writes(shape: Shape) = shape match {
      case r: Rectangle => Rectangle.writer.writes(r)
      case c: Circle => Circle.writer.writes(c)
      case l: Line => Line.writer.writes(l)
    }
  }
  override implicit val format: Format[Shape] = Format(reader, writer)
}

class Rectangle(userId: Int, color: String, x: Double, y: Double, val width: Double, val height: Double) extends Shape(userId, color, x, y) {
  override def companion = Rectangle
}

object Rectangle extends ShapeCompanion {
  override val id = 0
  override type T = Rectangle
  val shapeReader: Reads[Shape] = (
      (JsPath \ "userId").read[Int] and
      (JsPath \ "color").read[String] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "width").read[Double] and
      (JsPath \ "height").read[Double])(Rectangle.apply _) 
      
  override val reader: Reads[Rectangle] = (
      (JsPath \ "userId").read[Int] and
      (JsPath \ "color").read[String] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "width").read[Double] and
      (JsPath \ "height").read[Double])(Rectangle.apply _)  
      
  override val writer: Writes[Rectangle] = new Writes[Rectangle] {
    def writes(rect: Rectangle) = Json.obj(
        "userId" -> rect.userId,
        "color" -> rect.color,
        "x" -> rect.x,
        "y" -> rect.y,
        "width" -> rect.width,
        "height" -> rect.height)
  }
  
  override implicit val format: Format[Rectangle] = Format(reader, writer)
  
  def apply(userId: Int, color: String, x: Double, y: Double, width: Double, height: Double): Rectangle = {
    new Rectangle(userId, color, x, y, width, height)
  }
}

class Circle(userId: Int, color: String, x: Double, y: Double, val radius: Double) extends Shape(userId, color, x, y) {
  override def companion = Circle
}

object Circle extends ShapeCompanion {
  override val id = 1
  override type T = Circle
  val shapeReader: Reads[Shape] = (
      (JsPath \ "userId").read[Int] and
      (JsPath \ "color").read[String] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "radius").read[Double])(Circle.apply _) 
      
  override val reader: Reads[Circle] = (
      (JsPath \ "userId").read[Int] and
      (JsPath \ "color").read[String] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "radius").read[Double])(Circle.apply _)  
      
  override val writer: Writes[Circle] = new Writes[Circle] {
    def writes(circle: Circle) = Json.obj(
        "userId" -> circle.userId,
        "color" -> circle.color,
        "x" -> circle.x,
        "y" -> circle.y,
        "radius" -> circle.radius)
  }
  
  override implicit val format: Format[Circle] = Format(reader, writer)
  
  def apply(userId: Int, color: String, x: Double, y: Double, radius: Double): Circle = {
    new Circle(userId, color, x, y, radius)
  }
}

class Line(userId: Int, color: String, x: Double, y: Double, val endX: Double, val endY: Double) extends Shape(userId, color, x, y) {
  override def companion = Line
}

object Line extends ShapeCompanion {
  override val id = 2
  override type T = Line
  val shapeReader: Reads[Shape] = (
      (JsPath \ "userId").read[Int] and
      (JsPath \ "color").read[String] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "endX").read[Double] and
      (JsPath \ "endY").read[Double])(Line.apply _)
      
  override val reader: Reads[Line] = (
      (JsPath \ "userId").read[Int] and
      (JsPath \ "color").read[String] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "endX").read[Double] and
      (JsPath \ "endY").read[Double])(Line.apply _)  
      
  override val writer: Writes[Line] = new Writes[Line] {
    def writes(line: Line) = Json.obj(
        "userId" -> line.userId,
        "color" -> line.color,
        "x" -> line.x,
        "y" -> line.y,
        "endX" -> line.endX,
        "endY" -> line.endY)
  }
  
  override implicit val format: Format[Line] = Format(reader, writer)
  
  def apply(userId: Int, color: String, x: Double, y: Double, endX: Double, endY: Double): Line = {
    new Line(userId, color, x, y, endX, endY)
  }
}