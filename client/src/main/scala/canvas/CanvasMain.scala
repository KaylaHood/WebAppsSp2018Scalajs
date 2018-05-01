package canvas

import org.scalajs.dom
import dom.document
import scala.scalajs.js.annotation.JSExportTopLevel
import org.querki.jquery._
import scala.scalajs.js.JSON
import scala.scalajs.js
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSGlobal
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

@JSGlobal
@js.native
class Rectangle (
    var x: Double,
    var y: Double,
    var halfWidth: Double,
    var halfHeight: Double
  ) extends js.Object
  
@JSGlobal
@js.native
class Circle (
    var x: Double,
    var y: Double,
    var radius: Double
  ) extends js.Object
  
@JSGlobal
@js.native
class Line (
    var x: Double,
    var y: Double,
    var endX: Double,
    var endY: Double
  ) extends js.Object
  
@JSExportTopLevel("CanvasObject")
object CanvasMain {
  @JSExportTopLevel("CanvasMain")
  def main(canvas: html.Canvas): Unit = {
    var socket = null
    var socketIsInitialized = false
    var socketTimer = null
    var ctx: dom.CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    var rects: ArrayBuffer[Rectangle] = ArrayBuffer()
    var circles: ArrayBuffer[Circle] = ArrayBuffer()
    var lines: ArrayBuffer[Line] = ArrayBuffer()
    
    def drawRect(rect: Rectangle): Unit = {
      ctx.beginPath()
      ctx.fillRect(rect.x,rect.y,rect.halfWidth * 2.0,rect.halfHeight * 2.0)
    }
    
    def drawEmpty(): Unit = {
      ctx.font = "30px Papyrus San MS"
      ctx.fillStyle = "black"
      ctx.fillText("Click on me!", 10, 50, 300)
    }
    
    def drawCanvas(): Unit = {
      ctx.fillStyle = "white"
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      if(rects.length == 0 && circles.length == 0 && lines.length == 0) {
        drawEmpty()
      }
      else if (rects.length != 0) {
        for (i <- 0 until rects.length) {
          var r = rects.lift(i)
          if(r.isDefined) {
            drawRect(r.get)
          }
        }
      }
    }
    
    def resizeCanvas(): Unit = {
      var canvasHolderBounds = dom.window.document.getElementById("canvasHolder").getBoundingClientRect()
      canvas.width = canvasHolderBounds.width.toInt
      canvas.height = canvasHolderBounds.height.toInt
      drawCanvas()
    }
    
    def addRect(_x: Double, _y: Double, _halfWidth: Double, _halfHeight: Double): Unit = {
      var rect = js.Dynamic.literal().asInstanceOf[Rectangle]
      rect.x = _x
      rect.y = _y
      rect.halfWidth = _halfWidth
      rect.halfHeight = _halfHeight
      rects.append(rect)
    }
    
    dom.window.onresize = (e : dom.Event) => { resizeCanvas() }
    canvas.onclick = (e: dom.MouseEvent) => {
      var vpOffset = canvas.getBoundingClientRect()
      var x: Double = (e.pageX - (vpOffset.left + dom.window.pageXOffset))
      var y: Double = (e.pageY - (vpOffset.top + dom.window.pageYOffset))
      //addRipple(x,y,true)
    }
    dom.window.setInterval(() => drawCanvas(), 80)
    resizeCanvas()
  }

}