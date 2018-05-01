package models

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

abstract class Shape(val x: Double, val y: Double) {
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

class Rectangle(x: Double, y: Double, val halfWidth: Double, val halfHeight: Double) extends Shape(x, y) {
  override def companion = Rectangle
}

object Rectangle extends ShapeCompanion {
  override val id = 0
  override type T = Rectangle
  val shapeReader: Reads[Shape] = (
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "halfWidth").read[Double] and
      (JsPath \ "halfHeight").read[Double])(Rectangle.apply _) 
      
  override val reader: Reads[Rectangle] = (
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "halfWidth").read[Double] and
      (JsPath \ "halfHeight").read[Double])(Rectangle.apply _)  
      
  override val writer: Writes[Rectangle] = new Writes[Rectangle] {
    def writes(rect: Rectangle) = Json.obj(
        "x" -> rect.x,
        "y" -> rect.y,
        "halfWidth" -> rect.halfWidth,
        "halfHeight" -> rect.halfHeight)
  }
  
  override implicit val format: Format[Rectangle] = Format(reader, writer)
  
  def apply(x: Double, y: Double, halfWidth: Double, halfHeight: Double): Rectangle = {
    new Rectangle(x, y, halfWidth, halfHeight)
  }
}

class Circle(x: Double, y: Double, val radius: Double) extends Shape(x, y) {
  override def companion = Circle
}

object Circle extends ShapeCompanion {
  override val id = 1
  override type T = Circle
  val shapeReader: Reads[Shape] = (
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "radius").read[Double])(Circle.apply _) 
      
  override val reader: Reads[Circle] = (
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "radius").read[Double])(Circle.apply _)  
      
  override val writer: Writes[Circle] = new Writes[Circle] {
    def writes(circle: Circle) = Json.obj(
        "x" -> circle.x,
        "y" -> circle.y,
        "radius" -> circle.radius)
  }
  
  override implicit val format: Format[Circle] = Format(reader, writer)
  
  def apply(x: Double, y: Double, radius: Double): Circle = {
    new Circle(x, y, radius)
  }
}

class Line(x: Double, y: Double, val endX: Double, val endY: Double) extends Shape(x, y) {
  override def companion = Line
}

object Line extends ShapeCompanion {
  override val id = 2
  override type T = Line
  val shapeReader: Reads[Shape] = (
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "endX").read[Double] and
      (JsPath \ "endY").read[Double])(Line.apply _)
      
  override val reader: Reads[Line] = (
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "endX").read[Double] and
      (JsPath \ "endY").read[Double])(Line.apply _)  
      
  override val writer: Writes[Line] = new Writes[Line] {
    def writes(line: Line) = Json.obj(
        "x" -> line.x,
        "y" -> line.y,
        "endX" -> line.endX,
        "endY" -> line.endY)
  }
  
  override implicit val format: Format[Line] = Format(reader, writer)
  
  def apply(x: Double, y: Double, endX: Double, endY: Double): Line = {
    new Line(x, y, endX, endY)
  }
}