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
class Ripple(
    var xPos: Double,
    var yPos: Double,
    var radius: Double,
    var lineWidth: Double,
    var gradient: org.scalajs.dom.raw.CanvasGradient
  ) extends js.Object
  
@JSExportTopLevel("CanvasObject")
object CanvasMain {
  @JSExportTopLevel("CanvasMain")
  def main(canvas: html.Canvas): Unit = {
    var ctx: dom.CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    var ripples: ArrayBuffer[Ripple] = ArrayBuffer()
    
    def drawRipple(ripple: Ripple): Unit = {
      ctx.beginPath()
      ctx.arc(ripple.xPos,ripple.yPos,ripple.radius,0,2*Math.PI)
      ctx.strokeStyle = ripple.gradient
      ctx.lineWidth = ripple.lineWidth
      ctx.stroke()
      ripple.radius = ripple.radius + 1.5
      ripple.lineWidth = ripple.lineWidth + 0.05
    }
    
    def drawEmpty(): Unit = {
      ctx.font = "30px Papyrus San MS"
      ctx.fillStyle = "black"
      ctx.fillText("Click on me!", 10, 50, 300)
    }
    
    def drawCanvas(): Unit = {
      ctx.fillStyle = "white"
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      if(ripples.length == 0) {
        drawEmpty()
      }
      else {
        for (i <- 0 until ripples.length) {
          var r = ripples.lift(i)
          if(r.isDefined) {
            drawRipple(r.get)
            if(r.get.radius > 150) {
              ripples.patch(i,Nil,1)
            }
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
    
    def addRipple(x: Double, y: Double, recur: Boolean): Unit = {
      var ripple = js.Dynamic.literal().asInstanceOf[Ripple]
      ripple.xPos = x
      ripple.yPos = y
      ripple.radius = 0
      ripple.lineWidth = 2
      ripple.gradient = ctx.createRadialGradient(x, y, 5, x, y, 100)
      ripple.gradient.addColorStop(0, "#0000FF")
      ripple.gradient.addColorStop(0.5, "#0001C0")
      ripple.gradient.addColorStop(1, "#FFFFFF")
      ripples.append(ripple)
      if(recur) {
        dom.window.setTimeout((() => addRipple(x,y,false)),500)
        dom.window.setTimeout((() => addRipple(x,y,false)),1000)
      }
    }
    
    dom.window.onresize = (e : dom.Event) => { resizeCanvas() }
    canvas.onclick = (e: dom.MouseEvent) => {
      var vpOffset = canvas.getBoundingClientRect()
      var x: Double = (e.pageX - (vpOffset.left + dom.window.pageXOffset))
      var y: Double = (e.pageY - (vpOffset.top + dom.window.pageYOffset))
      addRipple(x,y,true)
    }
    dom.window.setInterval(() => drawCanvas(), 80)
    resizeCanvas()
  }

}