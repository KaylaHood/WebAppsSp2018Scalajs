package spa

import org.scalajs.dom
import dom.document
import scala.scalajs.js.annotation.JSExportTopLevel
import org.querki.jquery._
import scala.scalajs.js.JSON
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@JSGlobal
@js.native
class Circle(
    var x: Int,
    var y: Int,
    var radius: Int
  ) extends js.Object

object SPAMain {

  def main(args: Array[String]): Unit = {
    val originalContent = $("#mainContent").text
    anotherMethod()
    document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
    $("body").append("<p>Added via jQuery in Scala.js.</p>")
    $("#button0").click(() => restoreContents(originalContent))
    $("#button1").click(() => requestButton1Contents())
    $("#button2").click(() => requestButton2Contents())
    $("#sendCircle").click(() => sendCircle())
  }

  def anotherMethod(): Unit = {
    println("Printing stuff.")
  }
  
  def restoreContents(str: String): Unit = {
    $("#mainContent").text(str)
  }

  def route(n: Int): String = {
    $("#route" + n).value().toString
  }

  def requestButton1Contents(): Unit = {
    println("Button 1 clicked.")
    $.get(route(1), success = { (obj, _, _) =>
      val circle = obj.asInstanceOf[Circle]
      println(circle.x, circle.y, circle.radius)
      $("#mainContent").text(s"""Got a circle as JSON at (${circle.x}, ${circle.y}) with radius ${circle.radius}""")
    })
  }

  def requestButton2Contents(): Unit = {
    $.get(route(2), success = { (obj, _, _) => 
      println(obj)
      $("#mainContent").text(obj.toString)
    })
  }

  def sendCircle(): Unit = {
    val x = $("#xValue").value().toString.toInt
    val y = $("#yValue").value().toString.toInt
    val rad = $("#radValue").value().toString.toInt
    println(s"$x $y $rad")
    val c = js.Dynamic.literal(x = x, y = y, radius = rad)
    val route = $("#setCircleRoute").value().toString
    println("Sending "+JSON.stringify(c)+" to "+route)
    $.ajax(route, js.Dynamic.literal(data = JSON.stringify(c), contentType = "application/json", `type` = "POST", dataType = "json").asInstanceOf[JQueryAjaxSettings])
  }
}
